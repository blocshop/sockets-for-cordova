//TODO: we need add below setting to platforms/firefoxos/www/manifest.webapp
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

function TcpSocket() {
  this.onData = null;
  this.onError = null;
  this.onClose = null;
  this.socket = null;
}

TcpSocket.prototype={
  open: function(host, port, onStartRequest, onSocketError){
    this.onStartRequest = onStartRequest;
    this.onSocketError = onSocketError;
    this.socket = navigator.mozTCPSocket.open(host, port, {binaryType: 'arraybuffer'});

    this.socket.onopen = function(){
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
      if(this.onClose) {
        this.onClose();
      }
    }.bind(this);
  },

  write: function(data){
    if(this.socket)
      this.socket.send(data.buffer);
  },

  shutdownWrite: function(){
    if(this.socket) {
      this.socket.close();
      this.socket = null;
    }
  },

  close: function(){
    if(this.socket) {
      this.socket.close();
      this.socket = null;
    }
  }
};

module.exports = TcpSocket;

require("cordova/exec/proxy").add("Socket", module.exports);