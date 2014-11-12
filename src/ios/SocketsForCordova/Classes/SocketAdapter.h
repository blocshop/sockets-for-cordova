#import <Foundation/Foundation.h>

@interface SocketAdapter : NSObject <NSStreamDelegate> {
@public
}

- (void)open:(NSString *)host port:(NSNumber*)port;
- (void)write:(NSArray *)dataArray;
- (void)shutdownWrite;
- (void)close;
- (void)stream:(NSStream *)stream handleEvent:(NSStreamEvent)event;

@property (copy) void (^openEventHandler)();
@property (copy) void (^openErrorEventHandler)(NSString*);
@property (copy) void (^dataConsumer)(NSArray*);
@property (copy) void (^closeEventHandler)(BOOL);
@property (copy) void (^errorEventHandler)(NSString*);

@end