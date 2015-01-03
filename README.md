sockets-for-cordova
===================
This Cordova plugin provides JavaScript API, that allows you to communicate with server through TCP protocol.

Currently we support these platforms: iOS, Android, WP8.

## Installation

Install this plugin simply by:

`cordova plugin add cz.blocshop.socketsforcordova`

or you can use GIT repository for most recent version:

`cordova plugin add https://github.com/blocshop/sockets-for-cordova`

## Sample usage
Here is simple example of how to connect to remote server, consume data from it and close the connection.

Create instance of Socket type:
```
var socket = new Socket();
```

Set data consumer, error and close handlers:
```
socket.onData = function(data) {
  // invoked after new batch of data is received (typed array of bytes Uint8Array)
};
socket.onError = function(errorMessage) {
  // invoked after error occurs during connection
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
    // invoked after successful opening of socket
  },
  function(errorMessage) {
    // invoked after unsuccessful opening of socket
  });
```

Send "Hello world" to server:
```
var dataString = "Hello world";
var data = new Uint8Array(dataString.length);
for (var i = 0; i < data.length; i++) {
  data[i] = dataString.charCodeAt(i);
}
socket.write(data);
```

Close the connection gracefully by sending FIN to server:
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
Invoked after new batch of data is received by the client. Data are represented as typed array of bytes (`Uint8Array`).

#### `onClose: (hasError: boolean) => void`
Invoked after connection close. Native resources are released after this handler is invoked. Parameter `hasError` indicates whether connection was closed as a result of some error.

#### `onError: (message: string) => void`
Invoked when some error occurs during connection.

### Properties
#### `state: Socket.State`
Provides state of the socket. It can have 4 values represented by `Socket.State` enum:
- `Socket.State.CLOSED`
- `Socket.State.OPENING`
- `Socket.State.OPENED`
- `Socket.State.CLOSING`

Initial state of socket is CLOSED. Invoking `open` method changes state to OPENING. If it's successfuly opened, it goes to OPENED state. If opening fails, it goes back to CLOSED. Socket goes to CLOSING state immediately after `close` method is called. When socket is closed (by the server or by calling close method), it goes to CLOSED state.

##### Example
Check if socket is connected:
```
if (socket.state == Socket.State.OPENED) {
  console.log("Socket is opened");
}
```

### Methods
#### `open(host, port, onSuccess?, onError?): void`
Establishes connection with the remote host.

| parameter   | type          | description |
| ----------- |-----------------------------|--------------|
| `host`      | `string`                    | Remote host/ip address |
| `port`      | `number`                    | Tcp port number |
| `onSuccess` | `() => void`                | Success callback - called after successfull connection to the remote host. (optional)|
| `onError`   | `(message: string) => void` | Error callback - called when some error occurs during connecting to the remote host. (optional)|

#### `write(data, onSuccess?, onError?): void`
Sends data to remote host.

| parameter   | type          | description |
| ----------- |-----------------------------|--------------|
| `data`      | `Uint8Array`                | Typed array of bytes, that will be written to output stream. |
| `onSuccess` | `() => void`                | Success callback - called after data are successfully written to the output stream. (optional)|
| `onError`   | `(message: string) => void` | Error callback - called when some error occurs during writing of data to the output stream. (optional)|

#### `shutdownWrite(onSuccess?, onError?): void`
Sends `FIN` to remote host and finishes data sending. You cannot call `write` method after you call `shutdownWrite`, otherwise `onError` callback (of `write` method) will be called.

| parameter   | type          | description |
| ----------- |-----------------------------|--------------|
| `onSuccess` | `() => void`                | Success callback - called after sending of data is finished. (optional)|
| `onError`   | `(message: string) => void` | Error callback - called when some error occurs during this procedure. (optional)|

#### `close(onSuccess?, onError?): void`
Closes the connection. `onClose` event handler is called when connection is successfuly closed.

| parameter   | type          | description |
| ----------- |-----------------------------|--------------|
| `onSuccess` | `() => void`                | Success callback, called after connection is successfully closed. `onClose` event handler is called before that callback. (optional)|
| `onError`   | `(message: string) => void` | Error callback, called when some error occurs during this procedure. (optional)|
