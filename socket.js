var exec = require('cordova/exec');

var SOCKET_EVENT = "SOCKET_EVENT";
var CORDOVA_SERVICE_NAME = "SocketsForCordova";

function Socket() {
    this.onData = null;
    this.onClose = null;
    this.onError = null;
    this.socketKey = guid();
}

Socket.prototype.open = function (host, port, success, error) {

    var _that = this;
    function socketEventHandler(event) {

        var payload = event.payload;

        if (payload.socketKey !== _that.socketKey) {
            return;
        }

        switch(payload.type) {
            case "Close":
                //console.debug("SocketsForCordova: Close event, socket key: " + payload.socketKey);
                window.document.removeEventListener(SOCKET_EVENT, socketEventHandler);
                _that.onClose();
                break;
            case "DataReceived":
                //console.debug("SocketsForCordova: DataReceived event, socket key: " + payload.socketKey);
                _that.onData(new Uint8Array(payload.data));
                break;
            case "Error":
                //console.debug("SocketsForCordova: Error event, socket key: " + payload.socketKey);
                _that.onError(payload.errorMessage);
                break;
            default:
                console.error("SocketsForCordova: Unknown event type " + payload.type + ", socket key: " + payload.socketKey);
                break;
        }
    }

    exec(
        function() {
            //console.debug("SocketsForCordova: Socket successfully opened.");
            window.document.addEventListener(SOCKET_EVENT, socketEventHandler);
            if (success)
                success();
        },
        function(errorMessage) {
            //console.error("SocketsForCordova: Error during opening socket. Error: " + errorMessage);
            if (error)
                error(errorMessage);
        },
        CORDOVA_SERVICE_NAME,
        "open",
        [ this.socketKey, host, port ]);
};

Socket.prototype.write = function (data, success, error) {

    var dataToWrite = data instanceof Uint8Array
        ? Socket._copyToArray(data)
        : data;

    exec(
        function() {
            //console.debug("SocketsForCordova: Data successfully written to socket. Number of bytes: " + data.length);
            if (success)
                success();
        },
        function(errorMessage) {
            //console.error("SocketsForCordova: Error during writing data to socket. Error: " + errorMessage);
            if (error)
                error(errorMessage);
        },
        CORDOVA_SERVICE_NAME,
        "write",
        [ this.socketKey, dataToWrite ]);
};

Socket._copyToArray = function(array) {
    var outputArray = new Array(array.length);
    for (var i = 0; i < array.length; i++) {
        outputArray[i] = array[i];
    }
    return outputArray;
};

Socket.prototype.shutdownWrite = function (success, error) {
    exec(
         function() {
            //console.debug("SocketsForCordova: Shutdown write successfully called.");
             if (success)
                 success();
         },
         function(errorMessage) {
            //console.error("SocketsForCordova: Error when call shutdownWrite on socket. Error: " + errorMessage);
             if (error)
                 error(errorMessage);
         },
         CORDOVA_SERVICE_NAME,
         "shutdownWrite",
         [ this.socketKey ]);
};

Socket.prototype.close = function () {
    exec(
         function() {
             //console.debug("SocketsForCordova: Close successfully called.");
             if (success)
                 success();
         },
         function(errorMessage) {
             //console.error("SocketsForCordova: Error when call close on socket. Error: " + errorMessage);
             if (error)
                 error(errorMessage);
         },
         CORDOVA_SERVICE_NAME,
         "close",
         [ this.socketKey ]);
};

Socket.dispatchEvent = function(event) {
    var eventReceive = document.createEvent('Events');
    eventReceive.initEvent(SOCKET_EVENT, true, true);
    eventReceive.payload = event;

    document.dispatchEvent(eventReceive);
};

var guid = (function() {
    function s4() {
        return Math.floor((1 + Math.random()) * 0x10000)
            .toString(16)
            .substring(1);
    }
    return function() {
        return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
            s4() + '-' + s4() + s4() + s4();
    };
})();

// Register event dispatcher for Windows Phone
if (navigator.userAgent.match(/iemobile/i)) {
    window.document.addEventListener("deviceready", function() {
        exec(
            Socket.dispatchEvent,
            function(errorMessage) {
                console.error("SocketsForCordova: Cannot register WP event dispatcher, Error: " + errorMessage);
            },
            CORDOVA_SERVICE_NAME,
            "registerWPEventDispatcher",
            [ ]);
    });
}

module.exports = Socket;
