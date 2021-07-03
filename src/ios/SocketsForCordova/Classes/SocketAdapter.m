/**
 * Copyright (c) 2015, Blocshop s.r.o.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms are permitted
 * provided that the above copyright notice and this paragraph are
 * duplicated in all such forms and that any documentation,
 * advertising materials, and other materials related to such
 * distribution and use acknowledge that the software was developed
 * by the Blocshop s.r.o.. The name of the
 * Blocshop s.r.o. may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 */

#include <sys/socket.h>
#include <netdb.h>
#include <arpa/inet.h>
#include <math.h>
#import "SocketAdapter.h"

CFReadStreamRef readStream;
CFWriteStreamRef writeStream;

NSInputStream *inputStream;
NSOutputStream *outputStream;

NSTimer *openTimer;
NSTimer *writeTimer;

BOOL wasOpenned = FALSE;

int const WRITE_BUFFER_SIZE = 10 * 1024;

int openTimeoutSeconds = 5.0;
int writeTimeoutSeconds = 5.0;

@implementation SocketAdapter

- (void)open:(NSString *)host port:(NSNumber*)port {

    CFReadStreamRef readStream2;
    CFWriteStreamRef writeStream2;

    NSLog(@"[NATIVE] Setting up connection to %@ : %@", host, [port stringValue]);

    if (![self isIp:host]) {
        host = [self resolveIp:host];
    }

    CFStreamCreatePairWithSocketToHost(kCFAllocatorDefault, (__bridge CFStringRef)host, [port intValue], &readStream2, &writeStream2);

    CFReadStreamSetProperty(readStream2, kCFStreamPropertyShouldCloseNativeSocket, kCFBooleanTrue);
    CFWriteStreamSetProperty(writeStream2, kCFStreamPropertyShouldCloseNativeSocket, kCFBooleanTrue);

    if(!CFWriteStreamOpen(writeStream2) || !CFReadStreamOpen(readStream2)) {
        NSLog(@"[NATIVE] Error, streams not open");

        @throw [NSException exceptionWithName:@"SocketException" reason:@"Cannot open streams." userInfo:nil];
    }

    inputStream1 = (__bridge NSInputStream *)readStream2;
    [inputStream1 setDelegate:self];
    [inputStream1 open];

    NSTimer *timer = [NSTimer timerWithTimeInterval:openTimeoutSeconds target:self selector:@selector(onOpenTimeout:) userInfo:nil repeats:NO];
    [[NSRunLoop mainRunLoop] addTimer:timer forMode:NSDefaultRunLoopMode];
    openTimer = timer;

    outputStream1 = (__bridge NSOutputStream *)writeStream2;
    [outputStream1 open];

    [self performSelectorOnMainThread:@selector(runReadLoop) withObject:nil waitUntilDone:NO];
}

-(void)onOpenTimeout:(NSTimer *)timer {
    NSLog(@"[NATIVE] Open timeout: %d", openTimeoutSeconds);
    //self.errorEventHandler(@"Socket open timeout", @"openTimeout");
    self.openErrorEventHandler(@"Socket open timeout", 0);
    openTimer = nil;
    [self close];
}

-(void)onWriteTimeout:(NSTimer *)timer {
    NSLog(@"[NATIVE] Write timeout: %d", writeTimeoutSeconds);
    self.errorEventHandler(@"Socket write timeout", @"writeTimeout", 0);
    writeTimer = nil;
}

-(BOOL)isIp:(NSString*) host {
    const char *utf8 = [host UTF8String];

    // Check valid IPv4.
    struct in_addr dst;
    int success = inet_pton(AF_INET, utf8, &(dst.s_addr));
    if (success != 1) {
        // Check valid IPv6.
        struct in6_addr dst6;
        success = inet_pton(AF_INET6, utf8, &dst6);
    }
    return (success == 1);
}

-(NSString*)resolveIp:(NSString*) host {

    NSLog(@"[NATIVE] Resolving host: %@", host);

    const char *buff = [host cStringUsingEncoding:NSUTF8StringEncoding];
    struct hostent *host_entry = gethostbyname(buff);

    if(host_entry == NULL) {
        @throw [NSException exceptionWithName:@"NSException" reason:@"Cannot resolve hostname." userInfo:nil];
    }

    char *hostCstring = inet_ntoa(*((struct in_addr *)host_entry->h_addr_list[0]));
    host = [NSString stringWithUTF8String:hostCstring];

    NSLog(@"[NATIVE] Resolved ip: %@", host);

    return host;
}

- (void)runReadLoop {
    [inputStream1 scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
}

- (void)shutdownWrite {
    NSLog(@"[NATIVE] Shuting down write on socket.");

    [self closeOutputStream];

    int socket = [self socknumForStream: inputStream1];
    shutdown(socket, 1);
}

- (void)closeInputStream {
    [inputStream1 close];
    [inputStream1 removeFromRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
    [inputStream1 setDelegate:nil];
    inputStream1 = nil;
}

- (void)closeOutputStream {
    [outputStream1 close];
    outputStream1 = nil;
}

-(int) socknumForStream: (NSStream *)stream
{
    int sock = -1;
    NSData *sockObj = [stream propertyForKey:
                       (__bridge NSString *)kCFStreamPropertySocketNativeHandle];
    if ([sockObj isKindOfClass:[NSData class]] &&
        ([sockObj length] == sizeof(int)) ) {
        const int *sockptr = (const int *)[sockObj bytes];
        sock = *sockptr;
    }
    return sock;
}

- (void)stream:(NSStream *)stream handleEvent:(NSStreamEvent)event {

    switch(event) {
        case NSStreamEventOpenCompleted: {
            self.openEventHandler();
            wasOpenned = TRUE;
            if(openTimer != nil){
                NSLog(@"[NATIVE] openTimer invalidate on open event");
                [openTimer invalidate];
                openTimer = nil;
            }
            break;
        }
        case NSStreamEventHasBytesAvailable: {
            if(stream == inputStream1) {
                if(writeTimer != nil){
                    NSLog(@"[NATIVE] writeTimer invalidate on has bytes event");
                    [writeTimer invalidate];
                    writeTimer = nil;
                }

                uint8_t buf[65535];
                long len = [inputStream1 read:buf maxLength:65535];

                if(len > 0) {
                    NSMutableArray *dataArray = [[NSMutableArray alloc] init];
                    for (long i = 0; i < len; i++) {

                        [dataArray addObject:[NSNumber numberWithUnsignedChar:buf[i]]];
                    }
                    self.dataConsumer(dataArray);
                }
            }
            break;
        }
        case NSStreamEventEndEncountered: {

            if(stream == inputStream1) {
                [self closeInputStream];

                self.closeEventHandler(FALSE);
                break;
            }
        }
        case NSStreamEventErrorOccurred:
        {
            NSLog(@"[NATIVE] Stream event error: %@", [[stream streamError] localizedDescription]);

            NSInteger code = [[stream streamError] code];

            if (wasOpenned) {
                self.errorEventHandler([[stream streamError] localizedDescription], @"general", code);
                self.openErrorEventHandler([[stream streamError] localizedDescription],
                    code);
                if(openTimer != nil){
                    NSLog(@"[NATIVE] openTimer invalidate on open event");
                    [openTimer invalidate];
                    openTimer = nil;
                }
                self.closeEventHandler(TRUE);
            }
            else {
                self.errorEventHandler([[stream streamError] localizedDescription], @"general", code);
                self.openErrorEventHandler([[stream streamError] localizedDescription],
                    code);
            }
            //[self closeStreams];
            break;
        }
        default: {

            break;
        }
    }
}

- (void)write:(NSArray *)dataArray {
    int numberOfBatches = ceil((float)dataArray.count / (float)WRITE_BUFFER_SIZE);
    for (int i = 0; i < (numberOfBatches - 1); i++) {
        [self writeSubarray:dataArray offset:i * WRITE_BUFFER_SIZE length:WRITE_BUFFER_SIZE];
    }
    int lastBatchPosition = (numberOfBatches - 1) * WRITE_BUFFER_SIZE;

    NSTimer *timer = [NSTimer timerWithTimeInterval:writeTimeoutSeconds target:self selector:@selector(onWriteTimeout:) userInfo:nil repeats:NO];
    [[NSRunLoop mainRunLoop] addTimer:timer forMode:NSDefaultRunLoopMode];
    writeTimer = timer;

    [self writeSubarray:dataArray offset:lastBatchPosition length:(dataArray.count - lastBatchPosition)];
}

- (void)writeSubarray:(NSArray *)dataArray offset:(long)offset length:(long)length {
    uint8_t buf[length];
    for (long i = 0; i < length; i++) {
        unsigned char byte = (unsigned char)[[dataArray objectAtIndex:(offset + i)] integerValue];
        buf[i] = byte;
    }
    NSInteger bytesWritten = [outputStream1 write:buf maxLength:length];
    if (bytesWritten == -1) {
        @throw [NSException exceptionWithName:@"SocketException" reason:[outputStream1.streamError localizedDescription] userInfo:nil];
    }
    if (bytesWritten != length) {
        [self writeSubarray:dataArray offset:(offset + bytesWritten) length:(length - bytesWritten)];
    }
}

- (void)close {
    [self closeStreams];
    self.closeEventHandler(FALSE);
}

- (void)closeStreams {
    [self closeOutputStream];
    [self closeInputStream];

    if(writeTimer != nil){
        [writeTimer invalidate];
        writeTimer = nil;
        NSLog(@"[NATIVE] writeTimer invalidate on close");
    }

    if(openTimer != nil){
        [openTimer invalidate];
        openTimer = nil;
        NSLog(@"[NATIVE] openTimer invalidate on close");
    }
}

@end
