#import <Cordova/CDV.h>

@interface SocketPlugin : CDVPlugin {
    NSMutableDictionary *socketAdapters;
}

-(void) open: (CDVInvokedUrlCommand *) command;
-(void) write: (CDVInvokedUrlCommand *) command;
-(void) close: (CDVInvokedUrlCommand *) command;
-(void) setOptions: (CDVInvokedUrlCommand *) command;

@end