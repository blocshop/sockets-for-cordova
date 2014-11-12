#include <sys/socket.h>
#include <netdb.h>
#include <arpa/inet.h>
#import "SocketAdapter.h"

CFReadStreamRef readStream;
CFWriteStreamRef writeStream;

NSInputStream *inputStream;
NSOutputStream *outputStream;

BOOL wasOpenned = FALSE;

@implementation SocketAdapter

- (void)open:(NSString *)host port:(NSNumber*)port {
    
    NSLog(@"Setting up connection to %@ : %@", host, [port stringValue]);
    
    if (![self isIp:host]) {
        host = [self resolveIp:host];
    }
    
    CFStreamCreatePairWithSocketToHost(kCFAllocatorDefault, (__bridge CFStringRef)host, [port intValue], &readStream, &writeStream);
    
    CFReadStreamSetProperty(readStream, kCFStreamPropertyShouldCloseNativeSocket, kCFBooleanTrue);
    CFWriteStreamSetProperty(writeStream, kCFStreamPropertyShouldCloseNativeSocket, kCFBooleanTrue);
    
    if(!CFWriteStreamOpen(writeStream) || !CFReadStreamOpen(readStream)) {
		NSLog(@"Error, streams not open");
		
        @throw [NSException exceptionWithName:@"SocketException" reason:@"Cannot open streams." userInfo:nil];
	}
    
    inputStream = (__bridge NSInputStream *)readStream;
    [inputStream setDelegate:self];
    [inputStream open];
    
    outputStream = (__bridge NSOutputStream *)writeStream;
    [outputStream open];

    [self performSelectorOnMainThread:@selector(runReadLoop) withObject:nil waitUntilDone:NO];
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
    
    NSLog(@"Resolving host: %@", host);
    
    const char *buff = [host cStringUsingEncoding:NSUTF8StringEncoding];
    struct hostent *host_entry = gethostbyname(buff);
    
    if(host_entry == NULL) {
        @throw [NSException exceptionWithName:@"NSException" reason:@"Cannot resolve hostname." userInfo:nil];
    }
    
    char *hostCstring = inet_ntoa(*((struct in_addr *)host_entry->h_addr_list[0]));
    host = [NSString stringWithUTF8String:hostCstring];

    NSLog(@"Resolved ip: %@", host);
    
    return host;
}

- (void)runReadLoop {
    [inputStream scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
}

- (void)shutdownWrite {
    NSLog(@"Shuting down write on socket.");
    
    [self closeOutputStream];
    
    int socket = [self socknumForStream: inputStream];
    shutdown(socket, 1);
}

- (void)closeInputStream {
    [inputStream close];
    [inputStream removeFromRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
    [inputStream setDelegate:nil];
    inputStream = nil;
}

- (void)closeOutputStream {
    [outputStream close];
    outputStream = nil;
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
            break;
        }
        case NSStreamEventHasBytesAvailable: {
            if(stream == inputStream) {
                uint8_t buf[65535];
                long len = [inputStream read:buf maxLength:65535];

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
            
            if(stream == inputStream) {
                [self closeInputStream];
                
                self.closeEventHandler(FALSE);
                break;
            }
        }
        case NSStreamEventErrorOccurred:
        {
            NSLog(@"Stream event error: %@", [[stream streamError] localizedDescription]);
            
            if (wasOpenned) {
                self.errorEventHandler([[stream streamError] localizedDescription]);
                self.closeEventHandler(TRUE);
            }
            else {
                self.errorEventHandler([[stream streamError] localizedDescription]);
                self.openErrorEventHandler([[stream streamError] localizedDescription]);
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
    uint8_t buf[dataArray.count];
    for (int i = 0; i < dataArray.count; i++) {
        buf[i] = (unsigned char)[[dataArray objectAtIndex:i] integerValue];
    }
    NSInteger bytesWritten = [outputStream write:buf maxLength:dataArray.count];
    if (bytesWritten == -1) {
        @throw [NSException exceptionWithName:@"SocketException" reason:[outputStream.streamError localizedDescription] userInfo:nil];
    }
}

- (void)close {
    self.closeEventHandler(FALSE);
    [self closeStreams];
}

- (void)closeStreams {
    [self closeOutputStream];
    [self closeInputStream];
}

@end