#include <sys/socket.h>
#import "SocketAdapter.h"

CFReadStreamRef readStream;
CFWriteStreamRef writeStream;

NSInputStream *inputStream;
NSOutputStream *outputStream;

@implementation SocketAdapter

- (void)connect:(NSString *)host port:(NSNumber*)port {
    
    NSLog(@"Setting up connection to %@ : %@", host, [port stringValue]);
    
    CFStreamCreatePairWithSocketToHost(kCFAllocatorDefault, (__bridge CFStringRef)host, [port intValue], &readStream, &writeStream);
    
    CFReadStreamSetProperty(readStream, kCFStreamPropertyShouldCloseNativeSocket, kCFBooleanTrue);
    CFWriteStreamSetProperty(writeStream, kCFStreamPropertyShouldCloseNativeSocket, kCFBooleanTrue);
    
    inputStream = (__bridge NSInputStream *)readStream;
    [inputStream setDelegate:self];
    [inputStream scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
    [inputStream open];
    
    outputStream = (__bridge NSOutputStream *)writeStream;
    [outputStream open];
}

- (void)close {
    NSLog(@"Closing socket.");
    
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
        case NSStreamEventHasBytesAvailable: {
            if(stream == inputStream) {
                uint8_t buf[65535];
                unsigned int len = 0;
                
                len = [inputStream read:buf maxLength:65535];
                NSLog(@"%d", len);
                if(len > 0) {
                    NSMutableArray *dataArray = [[NSMutableArray alloc] init];
                    for (int i = 0; i < len; i++) {
                        
                        [dataArray addObject:[NSNumber numberWithUnsignedChar:buf[i]]];
                    }
                    self.dataConsumer(dataArray);
                }
            }
            break;
        }
        case NSStreamEventEndEncountered: {
            [self closeInputStream];
            
            self.closeEventHandler(FALSE);
            break;
        }
        case NSStreamEventErrorOccurred:
        {
            self.errorHandler([[stream streamError] localizedDescription]);
            
            [self abort];
            
            self.closeEventHandler(TRUE);
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

- (void)abort {
    [self closeOutputStream];
    [self closeInputStream];
}

@end