//    "type": "privileged",
//    "permissions": {
//      "tcp-socket": {
//        "description" : "Create TCP sockets and communicate over them."
//      }
//    },
// or add below setting to your cordova project config.xml
//    <platform name="firefoxos">
//        <permission name="tcp-socket" description="Create TCP sockets and communicate over them." privileged="true"/>
//    </platform> //
//var socket_status = Socket().State;

function TcpSocket() {
  this.onData = null;
  this.onError = null;
  this.onClose = null;
  this.socket = null;
  this.writable = false;
  this.state = TcpSocket.State.CLOSED;
}

TcpSocket.State = {};
TcpSocket.State[Socket.State.CLOSED = 0] = "CLOSED";
TcpSocket.State[Socket.State.OPENING = 1] = "OPENING";
TcpSocket.State[Socket.State.OPENED = 2] = "OPENED";
TcpSocket.State[Socket.State.CLOSING = 3] = "CLOSING";

TcpSocket.prototype={
  //Ref: https://developer.mozilla.org/en-US/docs/Web/API/TCPSocket
  open: function(host, port, onStartRequest, onSocketError){
    this.onStartRequest = onStartRequest;
    this.onSocketError = onSocketError;
    this.state = TcpSocket.State.OPENING;
    this.socket = navigator.mozTCPSocket.open(host, port, {binaryType: 'arraybuffer'});

    this.socket.onopen = function(){
      this.writable = true;
      this.state = TcpSocket.State.OPENED;
      if(this.onStartRequest)
        this.onStartRequest();
    }.bind(this);

    this.socket.onerror = function(e){
      if(this.onSocketError)
        this.onSocketError(e.data);
    }.bind(this);

    this.socket.ondata = function(e){
      if(this.onData) {
        this.onData(new Uint8Array(e.data));
      }
    }.bind(this);

    this.socket.onclose = function(){
      this.state = TcpSocket.State.CLOSED;
      if(this.onClose) {
        this.onClose();
      }
    }.bind(this);
  },

  write: function(data, onSuccess, onError){
    if(this.socket && this.writable) {
      try{
        this.socket.send(data.buffer);
        onSuccess();
      } catch (ex) {
        onError(ex.toString());
      }
    }
  },

  shutdownWrite: function(onSuccess, onError){
    this.writable = false;
  },

  close: function(onSuccess, onError){
    this.state = TcpSocket.State.CLOSING;
    if(this.socket) {
      try{
        this.socket.close();
        this.socket = null;
        if(onSuccess) {
          onSuccess();
        }
      } catch (ex) {
        if(onError) {
          onError(ex.toString());
        }
      }
    }
  }
};

module.exports = TcpSocket;

require("cordova/exec/proxy").add("Socket", module.exports);
