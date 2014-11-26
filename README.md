sockets-for-cordova
===================
This Cordova plugin provides JavaScript API, that allows you to communicate with server through TCP protocol.

Currently we support these platforms: iOS, Android, WP8.

## Sample usage
TODO

## API
### Event handlers
#### `onData: (data: Uint8Array) => void`
Called when new batch of data was received by the client. Data are represented as typed array of bytes (`Uint8Array`).

#### `onClose: (hasError: boolean) => void`
Called after connection was successfully closed. Parameter `hasError` indicates whether connection was closed as a result of some error.

#### `onError: (message: string) => void`
Called  when some error occurs on opened connection.

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
