sockets-for-cordova
===================
This Cordova plugin provides JavaScript API, that allows you to communicate with the server through TCP protocol.

Currently, we support these platforms: iOS, Android, WP8.

You can also get information about this plugin from our blog post http://www.blocshop.cz/2015/01/12/tcp-networking-in-cordova/

## Installation

Install this plugin simply by:

`cordova plugin add cordova-plugin-socket-tcp`

for the Ionic Framework

`ionic cordova plugin add cordova-plugin-socket-tcp`

## Sample usage
Here is a simple example of how to connect to a remote server, consume data from it, and close the connection.

Create an instance of Socket type:
```
var socket = new Socket();
```

Set data consumer, error, and close handlers:
```
socket.onData = function(data) {
  // invoked after new batch of data is received (typed array of bytes Uint8Array)
};
socket.onError = function(errorMessage) {
  // invoked after an error occurs during connection
};
socket.onClose = function(hasError) {
  // invoked after connection close
};
```
Connect to server someremoteserver.com, with port 1234:
```
socket.open(
  "someremoteserver.com",
  1234,
  function() {
    // invoked after the successful opening of the socket
  },
  function(errorMessage) {
    // invoked after unsuccessful opening of the socket
  });
```

Send "Hello world" to the server:
```
var dataString = "Hello world";
var data = new Uint8Array(dataString.length);
for (var i = 0; i < data.length; i++) {
  data[i] = dataString.charCodeAt(i);
}
socket.write(data);
```

Close the connection gracefully by sending FIN to the server:
```
socket.shutdownWrite();  
```

or close the connection immediately:
```
socket.close();
```

## API
### Event handlers
#### `onData: (data: Uint8Array) => void`
Invoked after a new batch of data is received by the client. Data are represented as a typed array of bytes (`Uint8Array`).

#### `onClose: (hasError: boolean) => void`
Invoked after connection closed. Native resources are released after this handler is invoked. The parameter `hasError` indicates whether the connection was closed as a result of some error.

#### `onError: (message: string) => void`
Invoked when some error occurs during connection.

### Properties
#### `state: Socket.State`
Provides state of the socket. It can have 4 values represented by `Socket.State` enum:
- `Socket.State.CLOSED`
- `Socket.State.OPENING`
- `Socket.State.OPENED`
- `Socket.State.CLOSING`

The initial state of the socket is CLOSED. Invoking `open` method changes the state to OPENING. If it's successfully opened, it goes to OPENED state. If the opening fails, it goes back to CLOSED. Socket goes to CLOSING state immediately after `close` method is called. When the socket is closed (by the server or by calling the close method), it goes to CLOSED state.

##### Example
Check if the socket is connected:
```
if (socket.state == Socket.State.OPENED) {
  console.log("Socket is opened");
}
```

### Methods
#### `open(host, port, onSuccess?, onError?): void`
Establishes a connection with the remote host.

| parameter   | type          | description |
| ----------- |-----------------------------|--------------|
| `host`      | `string`                    | Remote host/ip address |
| `port`      | `number`                    | Tcp port number |
| `onSuccess` | `() => void`                | Success callback - called after successful connection to the remote host. (optional)|
| `onError`   | `(message: string) => void` | Error callback - called when some error occurs during connecting to the remote host. (optional)|

#### `write(data, onSuccess?, onError?): void`
Sends data to the remote host.

| parameter   | type          | description |
| ----------- |-----------------------------|--------------|
| `data`      | `Uint8Array`                | Typed array of bytes, that will be written to the output stream. |
| `onSuccess` | `() => void`                | Success callback - called after data are successfully written to the output stream. (optional)|
| `onError`   | `(message: string) => void` | Error callback - called when some error occurs during the writing of data to the output stream. (optional)|

#### `shutdownWrite(onSuccess?, onError?): void`
Sends `FIN` to the remote host and finishes data sending. You cannot call `write` method after you call `shutdownWrite`, otherwise `onError` callback (of `write` method) will be called.

| parameter   | type          | description |
| ----------- |-----------------------------|--------------|
| `onSuccess` | `() => void`                | Success callback - called after sending of data is finished. (optional)|
| `onError`   | `(message: string) => void` | Error callback - called when some error occurs during this procedure. (optional)|

#### `close(onSuccess?, onError?): void`
Closes the connection. `onClose` event handler is called when the connection is successfully closed.

| parameter   | type          | description |
| ----------- |-----------------------------|--------------|
| `onSuccess` | `() => void`                | Success callback, called after the connection is successfully closed. `onClose` event handler is called before that callback. (optional)|
| `onError`   | `(message: string) => void` | Error callback, called when some error occurs during this procedure. (optional)|

## BSD License
Copyright (c) 2015, Blocshop s.r.o.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions, and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions, and the following disclaimer in the documentation and/or other materials provided with the distribution.
3. All advertising materials mentioning features or use of this software must display the following acknowledgment: This product includes software developed by the Blocshop s.r.o.
4. Neither the name of the Blocshop s.r.o. nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY Blocshop s.r.o. ''AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER, CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.  

## What's new
 - 1.2.3 - fixed iOS socket closing crashes [iOS]
 - 1.5.0 - added iOS open and write timeouts, changed js errors format [iOS]
 - 1.5.1 - fixed cordova js bridge implementation [js]
 - 1.5.2 - fixed iOS open timeout [iOS]
 - 1.5.3 - added Android open and write timeouts [Android]
 - 1.5.4 - fixed iOS closing sockets on open timeout [iOS]
 - 1.6.0 - close old existing sockets on reopen by destination ports. Removed iOS trash sources [iOS, Android]
 - 1.7.0 - added codes to error handlers [iOS, Android]
 - 1.7.1 - error handler bugfixes [Android]
  
Appelian, 2015  
