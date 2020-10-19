/**
 * Copyright (c) 2019, kitolog
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
 * THIS SOFTWARE IS PROVIDED `AS IS` AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 */

#import "SocketPlugin.h"
#import "SocketAdapter.h"
#import <cordova/CDV.h>
#import <Foundation/Foundation.h>

@implementation SocketPlugin : CDVPlugin

- (void) open : (CDVInvokedUrlCommand*) command {
    
    NSString *socketKey = [command.arguments objectAtIndex:0];
    NSString *host = [command.arguments objectAtIndex:1];
    NSNumber *port = [command.arguments objectAtIndex:2];

    NSLog(@"[NATIVE] OPEN socket for port: %@", port);

    if (socketAdapters == nil) {
        self->socketAdapters = [[NSMutableDictionary alloc] init];
    }

    if (socketAdaptersPorts == nil) {
        self->socketAdaptersPorts = [[NSMutableDictionary alloc] init];
    }

    NSString *existsPortSocketKey = [self->socketAdaptersPorts objectForKey:port];
    if(existsPortSocketKey != nil){
        NSLog(@"[NATIVE] OLD socket exists for port: %@", port);
        [self closeSocketInstance:existsPortSocketKey];
    }

    __block SocketAdapter* socketAdapter = [[SocketAdapter alloc] init];
    socketAdapter.openEventHandler = ^ void () {
        [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];

        [self->socketAdapters setObject:socketAdapter forKey:socketKey];
        [self->socketAdaptersPorts setObject:socketKey forKey:port];

        socketAdapter = nil;
    };
    socketAdapter.openErrorEventHandler = ^ void (NSString *error, NSInteger code){
        NSLog(@"[NATIVE] openErrorEventHandler. Code %ld", code);

        NSMutableDictionary *errorDictionaryData = [[NSMutableDictionary alloc] init];
        [errorDictionaryData setObject:error forKey:@"message"];
        [errorDictionaryData setObject:socketKey forKey:@"socketKey"];
        [errorDictionaryData setObject:[NSString stringWithFormat: @"%ld", (long)code] forKey:@"code"];

        [self.commandDelegate
         sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:errorDictionaryData]
         callbackId:command.callbackId];
    };
    socketAdapter.errorEventHandler = ^ void (NSString *error, NSString *errorType, NSInteger code){
        NSMutableDictionary *errorDictionaryData = [[NSMutableDictionary alloc] init];
        [errorDictionaryData setObject:@"Error" forKey:@"type"];
        [errorDictionaryData setObject:errorType forKey:@"errorType"];
        [errorDictionaryData setObject:error forKey:@"errorMessage"];
        [errorDictionaryData setObject:socketKey forKey:@"socketKey"];
        [errorDictionaryData setObject:[NSString stringWithFormat: @"%ld", (long)code] forKey:@"code"];

        [self dispatchEventWithDictionary:errorDictionaryData];
    };
    socketAdapter.dataConsumer = ^ void (NSArray* dataArray) {
        NSMutableDictionary *dataDictionary = [[NSMutableDictionary alloc] init];
        [dataDictionary setObject:@"DataReceived" forKey:@"type"];
        [dataDictionary setObject:dataArray forKey:@"data"];
        [dataDictionary setObject:socketKey forKey:@"socketKey"];

        [self dispatchEventWithDictionary:dataDictionary];
    };
    socketAdapter.closeEventHandler = ^ void (BOOL hasErrors) {
        NSLog(@"[NATIVE] closeEventHandler");
        NSMutableDictionary *closeDictionaryData = [[NSMutableDictionary alloc] init];
        [closeDictionaryData setObject:@"Close" forKey:@"type"];
        [closeDictionaryData setObject:(hasErrors == TRUE ? @"true": @"false") forKey:@"hasError"];
        [closeDictionaryData setObject:socketKey forKey:@"socketKey"];

        [self dispatchEventWithDictionary:closeDictionaryData];

        [self removeSocketAdapter:socketKey];
    };

    [self.commandDelegate runInBackground:^{
        @try {
            [socketAdapter open:host port:port];
        }
        @catch (NSException *e) {
            [self.commandDelegate
             sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:e.reason]
             callbackId:command.callbackId];

            socketAdapter = nil;
        }
    }];
}

- (void) write:(CDVInvokedUrlCommand *) command {

    NSString* socketKey = [command.arguments objectAtIndex:0];
    NSArray *data = [command.arguments objectAtIndex:1];

    SocketAdapter *socket = [self getSocketAdapter:socketKey];

    [self.commandDelegate runInBackground:^{
        @try {
            if (socket != nil) {
                [socket write:data];
            }else{
                NSLog(@"[NATIVE] Write: socket is nil. SocketKey: %@", socketKey);
            }
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

- (void) shutdownWrite:(CDVInvokedUrlCommand *) command {

    NSString* socketKey = [command.arguments objectAtIndex:0];

    SocketAdapter *socket = [self getSocketAdapter:socketKey];

    [self.commandDelegate runInBackground:^{
        @try {
            if (socket != nil) {
                [socket shutdownWrite];
            }else{
                NSLog(@"[NATIVE] ShutdownWrite: socket is nil. SocketKey: %@", socketKey);
            }

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
    [self.commandDelegate runInBackground:^{
        @try {
            [self closeSocketInstance:socketKey];
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

- (void) closeSocketInstance:(NSString*) socketKey {
    SocketAdapter *socket = [self getSocketAdapter:socketKey];
    @try {
        if (socket != nil) {
            [socket close];
            NSLog(@"[NATIVE] Close: SUCCESS. SocketKey: %@", socketKey);
        }else{
            NSLog(@"[NATIVE] Close: socket is nil. SocketKey: %@", socketKey);
        }
    }
    @catch (NSException *e) {
        NSLog(@"[NATIVE] Close exception: %@. SocketKey: %@", e,  socketKey);
    }
}

- (void) setOptions: (CDVInvokedUrlCommand *) command {
}

- (SocketAdapter*) getSocketAdapter: (NSString*) socketKey {
    SocketAdapter* socketAdapter = [self->socketAdapters objectForKey:socketKey];
    if (socketAdapter == nil) {
        NSLog(@"[NATIVE] Cannot find socketKey: %@. Connection is probably closed.", socketKey);
        //NSString *exceptionReason = [NSString stringWithFormat:@"Cannot find socketKey: %@. Connection is probably closed.", socketKey];

        //@throw [NSException exceptionWithName:@"IllegalArgumentException" reason:exceptionReason userInfo:nil];
    }
    return socketAdapter;
}

- (void) removeSocketAdapter: (NSString*) socketKey {
    NSLog(@"[NATIVE] Removing socket adapter from storage.");
    [self->socketAdapters removeObjectForKey:socketKey];
}

- (BOOL) socketAdapterExists: (NSString*) socketKey {
    SocketAdapter* socketAdapter = [self->socketAdapters objectForKey:socketKey];
    return socketAdapter != nil;
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
