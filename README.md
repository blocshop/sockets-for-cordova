sockets-for-cordova
===================

Cordova plugin for socket network communication

## API
### Event handlers
#### `onData: (data: Uint8Array) => void`
Called when new batch of data was send from server to client. Data are represented as typed array of bytes (`Uint8Array`).

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
| `onSuccess` | `() => void`                | Success callback, called after successfull connection to the remote host. (optional)|
| `onError`   | `(message: string) => void` | Error callback, called when some error occurs during connecting to the remote host. (optional)|

#### `write(data, onSuccess?, onError?): void`
Sends data to the remote host.

| parameter   | type          | description |
| ----------- |-----------------------------|--------------|
| `data`      | `Uint8Array`                 | Typed array of bytes to be written to the output stream |
| `onSuccess` | `() => void`                | Success callback, called after data are successfully written to the output stream. (optional)|
| `onError`   | `(message: string) => void` | Error callback, called when some error occurs during writing of data to the output stream. (optional)|

#### `shutdownWrite(onSuccess?, onError?): void`
Finishes sending of data by sending `FIN` to remote host.  If you call `write` after invoking `shutdownWrite`, callback `onError` (of `write` method) will be called.

| parameter   | type          | description |
| ----------- |-----------------------------|--------------|
| `onSuccess` | `() => void`                | Success callback, called after sending of all data is finished. (optional)|
| `onError`   | `(message: string) => void` | Error callback, called when some error occurs during this procedure. (optional)|

#### `close(onSuccess?, onError?): void`
Closes the connection. `onClose` event handler is called when closing of socket was succesfull.

| parameter   | type          | description |
| ----------- |-----------------------------|--------------|
| `onSuccess` | `() => void`                | Success callback, called after connection was successfully closed. `onClose` event handler is called after that callback. (optional)|
| `onError`   | `(message: string) => void` | Error callback, called when some error occurs during this procedure. (optional)|
