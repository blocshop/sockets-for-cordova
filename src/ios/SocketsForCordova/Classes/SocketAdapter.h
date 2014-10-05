#import <Foundation/Foundation.h>

@interface SocketAdapter : NSObject <NSStreamDelegate> {
@public
}

- (void)connect:(NSString *)host port:(NSNumber*)port;
- (void)write:(NSArray *)dataArray;
- (void)close;
- (void)stream:(NSStream *)stream handleEvent:(NSStreamEvent)event;

@property (copy) void (^dataConsumer)(NSArray*);
@property (copy) void (^closeEventHandler)(BOOL);
@property (copy) void (^errorHandler)(NSString*);

@end