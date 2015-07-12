/**
 * Copyright (c) 2015, Blocshop s.r.o.
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
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 */


var exec = require('cordova/exec');

var SOCKET_EVENT = "SOCKET_EVENT";
var CORDOVA_SERVICE_NAME = "SocketsForCordova";

Socket.State = {};
Socket.State[Socket.State.CLOSED = 0] = "CLOSED";
Socket.State[Socket.State.OPENING = 1] = "OPENING";
Socket.State[Socket.State.OPENED = 2] = "OPENED";
Socket.State[Socket.State.CLOSING = 3] = "CLOSING";

function Socket() {
    this._state = Socket.State.CLOSED;
    this.onData = null;
    this.onClose = null;
    this.onError = null;
    this.socketKey = guid();
}

Socket.prototype.open = function (host, port, success, error) {

    success = success || function() { };
    error = error || function() { };

    if (!this._ensureState(Socket.State.CLOSED, error)) {
        return;
    }

    var _that = this;

    function socketEventHandler(event) {

        var payload = event.payload;

        if (payload.socketKey !== _that.socketKey) {
            return;
        }

        switch (payload.type) {
            case "Close":
                _that._state = Socket.State.CLOSED;
                window.document.removeEventListener(SOCKET_EVENT, socketEventHandler);
                _that.onClose(payload.hasError);
                break;
            case "DataReceived":
                var dataArray = string2Uint8Array(payload.data);
                _that.onData(dataArray);
                break;
            case "Error":
                _that.onError(payload.errorMessage);
                break;
            default:
                console.error("SocketsForCordova: Unknown event type " + payload.type + ", socket key: " + payload.socketKey);
                break;
        }
    }

    _that._state = Socket.State.OPENING;

    exec(
        function () {
            _that._state = Socket.State.OPENED;
            window.document.addEventListener(SOCKET_EVENT, socketEventHandler);
            success();
        },
        function(errorMessage) {
            _that._state = Socket.State.CLOSED;
            error(errorMessage);
        },
        CORDOVA_SERVICE_NAME,
        "open",
        [ this.socketKey, host, port ]);
};

Socket.prototype.write = function (data, success, error) {

    success = success || function() { };
    error = error || function() { };

    if (!this._ensureState(Socket.State.OPENED, error)) {
        return;
    }

    var dataToWrite = uint8ArrayToString(data);

    exec(
        success,
        error,
        CORDOVA_SERVICE_NAME,
        "write",
        [ this.socketKey, dataToWrite ]);
};

Socket.prototype.shutdownWrite = function (success, error) {

    success = success || function() { };
    error = error || function() { };

    if (!this._ensureState(Socket.State.OPENED, error)) {
        return;
    }

    exec(
        success,
        error,
        CORDOVA_SERVICE_NAME,
        "shutdownWrite",
        [ this.socketKey ]);
};

Socket.prototype.close = function (success, error) {

    success = success || function() { };
    error = error || function() { };

    if (!this._ensureState(Socket.State.OPENED, error)) {
        return;
    }

    this._state = Socket.State.CLOSING;

    exec(
        success,
        error,
        CORDOVA_SERVICE_NAME,
        "close",
        [ this.socketKey ]);
};

Object.defineProperty(Socket.prototype, "state", {
    get: function () {
        return this._state;
    },
    enumerable: true,
    configurable: true
});

Socket.prototype._ensureState = function(requiredState, errorCallback) {
    var state = this._state;
    if (state != requiredState) {
        window.setTimeout(function() {
            errorCallback("Invalid operation for this socket state: " + Socket.State[state]);
        });
        return false;
    }
    else {
        return true;
    }
};

Socket.dispatchEvent = function (event) {
    var eventReceive = document.createEvent('Events');
    eventReceive.initEvent(SOCKET_EVENT, true, true);
    eventReceive.payload = event;

    document.dispatchEvent(eventReceive);
};

var uint8ArrayToString = function (array) {
    var CHUNK_LENGTH = 1000;

    var numberOfChunks = Math.ceil(array.length / CHUNK_LENGTH);
    var stringChunks = new Array(numberOfChunks);

    for (var i = 0; i < stringChunks.length; i++) {
        var start = i * CHUNK_LENGTH;
        var end = start + CHUNK_LENGTH;

        var subArray = array.subarray(start, end);

        stringChunks[i] = String.fromCharCode.apply(String, subArray);
    }

    return stringChunks.join("");
};

var string2Uint8Array = function (str) {
    var array = new Uint8Array(str.length);
    for (var i = 0; i < array.length; i++) {
        array[i] = str.charCodeAt(i);
    }
    return array;
};

var guid = (function () {
    function s4() {
        return Math.floor((1 + Math.random()) * 0x10000)
            .toString(16)
            .substring(1);
    }

    return function () {
        return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
            s4() + '-' + s4() + s4() + s4();
    };
})();

window.document.addEventListener("deviceready", function () {
    exec(
        Socket.dispatchEvent,
        function (errorMessage) {
            console.error("SocketsForCordova: Cannot register WP event dispatcher, Error: " + errorMessage);
        },
        CORDOVA_SERVICE_NAME,
        "registerEventDispatcher",
        [ ]);
});

module.exports = Socket;
