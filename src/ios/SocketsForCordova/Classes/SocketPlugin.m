#import "SocketPlugin.h"
#import "SocketAdapter.h"
#import <cordova/CDV.h>
#import <Foundation/Foundation.h>

@implementation SocketPlugin : CDVPlugin

- (void) create : (CDVInvokedUrlCommand*) command {
    NSString* socketKey = [command.arguments objectAtIndex : 0];
	
	if (socketAdapters == nil) {
		self->socketAdapters = [[NSMutableDictionary alloc] init];
	}
	
	SocketAdapter* socketAdapter = [[SocketAdapter alloc] init];
    
    socketAdapter.dataConsumer = ^ void (NSArray* dataArray) {
        NSMutableDictionary *dataDictionary = [[NSMutableDictionary alloc] init];
        [dataDictionary setObject:@"DataReceived" forKey:@"type"];
        [dataDictionary setObject:dataArray forKey:@"data"];
        [dataDictionary setObject:socketKey forKey:@"socketKey"];
        
        [self dispatchEventWithDictionary:dataDictionary];
    };
    socketAdapter.closeEventHandler = ^ void (BOOL hasErrors) {
        NSMutableDictionary *closeDictionaryData = [[NSMutableDictionary alloc] init];
        [closeDictionaryData setObject:@"Close" forKey:@"type"];
        [closeDictionaryData setObject:(hasErrors == TRUE ? @"true": @"false") forKey:@"hasError"];
        [closeDictionaryData setObject:socketKey forKey:@"socketKey"];
        
        [self dispatchEventWithDictionary:closeDictionaryData];
    };
    socketAdapter.errorHandler = ^ void (NSString *error){
        NSMutableDictionary *errorDictionaryData = [[NSMutableDictionary alloc] init];
        [errorDictionaryData setObject:@"Error" forKey:@"type"];
        [errorDictionaryData setObject:error forKey:@"errorMessage"];
        [errorDictionaryData setObject:socketKey forKey:@"socketKey"];
        
        [self dispatchEventWithDictionary:errorDictionaryData];
    };
    
	[self->socketAdapters
     setObject:socketAdapter
     forKey:socketKey];
    
	[self.commandDelegate
     sendPluginResult: [CDVPluginResult resultWithStatus : CDVCommandStatus_OK]
     callbackId: command.callbackId];
}

- (SocketAdapter*) getSocketAdapter: (NSString*) socketKey {
	SocketAdapter* socketAdapter = [self->socketAdapters objectForKey:socketKey];
	if (socketAdapter == nil) {
		NSString *exceptionReason = [NSString stringWithFormat:@"Cannot find socketKey: %@. Connection is probably closed.", socketKey];
		
		@throw [NSException exceptionWithName:@"IllegalArgumentException" reason:exceptionReason userInfo:nil];
	}
	return socketAdapter;
}

- (void) connect : (CDVInvokedUrlCommand*) command {
    
	NSString *socketKey = [command.arguments objectAtIndex:0];
	NSString *host = [command.arguments objectAtIndex:1];
	NSNumber *port = [command.arguments objectAtIndex:2];
    
	SocketAdapter *socket = [self getSocketAdapter:socketKey];
    
    [self.commandDelegate runInBackground:^{
        @try {
            [socket connect:host port:port];
            
            [self.commandDelegate
             sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK]
             callbackId:command.callbackId];
        }
        @catch (NSException *e) {
            [self.commandDelegate
             sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:e.reason]
             callbackId:command.callbackId];
        }
    }];
}

- (void) write:(CDVInvokedUrlCommand *) command {
	
    NSString* socketKey = [command.arguments objectAtIndex:0];
    NSArray *data = [command.arguments objectAtIndex:1];
    
    SocketAdapter *socket = [self getSocketAdapter:socketKey];
    
	[self.commandDelegate runInBackground:^{
        @try {
            [socket write:data];
            [self.commandDelegate
             sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK]
             callbackId:command.callbackId];
        }
        @catch (NSException *e) {
            [self.commandDelegate
             sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:e.reason]
             callbackId:command.callbackId];
        }
    }];
}

- (void) close:(CDVInvokedUrlCommand *) command {
    
    NSString* socketKey = [command.arguments objectAtIndex:0];
	
	SocketAdapter *socket = [self getSocketAdapter:socketKey];
    
    [self.commandDelegate runInBackground:^{
        @try {
            [socket close];
            [self.commandDelegate
             sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK]
             callbackId:command.callbackId];
        }
        @catch (NSException *e) {
            [self.commandDelegate
             sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:e.reason]
             callbackId:command.callbackId];
        }
    }];
}

- (void) setOptions: (CDVInvokedUrlCommand *) command {
}

- (void) dispatchEventWithDictionary: (NSDictionary*) dictionary {
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dictionary options:0 error:nil];
    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    
    [self dispatchEvent:jsonString];
}

- (void) dispatchEvent: (NSString *) jsonEventString {
    NSString *jsToEval = [NSString stringWithFormat : @"window.Socket.dispatchEvent(%@);", jsonEventString];
    [self.commandDelegate evalJs:jsToEval];
}

@end