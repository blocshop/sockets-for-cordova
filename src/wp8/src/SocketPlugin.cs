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

using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;
using WPCordovaClassLib.Cordova;
using WPCordovaClassLib.Cordova.Commands;
using WPCordovaClassLib.Cordova.JSON;

namespace Blocshop.ScoketsForCordova
{
    public class SocketPlugin : BaseCommand
    {
        private readonly ISocketStorage socketStorage;
        private string eventDispatcherCallbackId;

        public SocketPlugin()
        {
            System.Diagnostics.Debug.WriteLine("SocketPlugin constructor: " + DateTime.Now.Ticks);
            this.socketStorage = SocketStorage.CreateSocketStorage();
        }

        public void open(string parameters)
        {
            string socketKey = JsonHelper.Deserialize<string[]>(parameters)[0];
            string host = JsonHelper.Deserialize<string[]>(parameters)[1];
            int port = int.Parse(JsonHelper.Deserialize<string[]>(parameters)[2]);

            ISocketAdapter socketAdapter = new SocketAdapter();
            socketAdapter.CloseEventHandler = (hasError) => this.CloseEventHandler(socketKey, hasError);
            socketAdapter.DataConsumer = (data) => this.DataConsumer(socketKey, data);
            socketAdapter.ErrorHandler = (ex) => this.ErrorHandler(socketKey, ex);

            try
            {
                socketAdapter.Connect(host, port).Wait();

                this.socketStorage.Add(socketKey, socketAdapter);

                this.DispatchCommandResult(new PluginResult(PluginResult.Status.OK));
            }
            catch (SocketException ex)
            {
                this.DispatchCommandResult(new PluginResult(PluginResult.Status.IO_EXCEPTION, ex.Message));
            }
            catch (AggregateException ex)
            {
                this.DispatchCommandResult(new PluginResult(PluginResult.Status.IO_EXCEPTION, ex.InnerException.Message));
            }
        }

        public void write(string parameters)
        {
            string socketKey = JsonHelper.Deserialize<string[]>(parameters)[0];
            string dataJsonArray = JsonHelper.Deserialize<string[]>(parameters)[1];
            byte[] data = JsonHelper.Deserialize<byte[]>(dataJsonArray);

            ISocketAdapter socket = this.socketStorage.Get(socketKey);
            try
            {
                socket.Write(data).Wait();

                this.DispatchCommandResult(new PluginResult(PluginResult.Status.OK));
            }
            catch (SocketException ex)
            {
                this.DispatchCommandResult(new PluginResult(PluginResult.Status.IO_EXCEPTION, ex.Message));
            }
        }

        public void shutdownWrite(string parameters)
        {
            string socketKey = JsonHelper.Deserialize<string[]>(parameters)[0];

            ISocketAdapter socket = this.socketStorage.Get(socketKey);

            socket.ShutdownWrite();
        }

        public void close(string parameters)
        {
            string socketKey = JsonHelper.Deserialize<string[]>(parameters)[0];

            ISocketAdapter socket = this.socketStorage.Get(socketKey);

            socket.Close();
        }

        public void registerWPEventDispatcher(string parameters)
        {
            this.eventDispatcherCallbackId = this.CurrentCommandCallbackId;
            PluginResult result = new PluginResult(PluginResult.Status.OK);
            result.KeepCallback = true;
            DispatchCommandResult(result, this.eventDispatcherCallbackId);
        }

        //private void setOptions(CordovaArgs args, CallbackContext callbackContext) throws JSONException {

        //    String socketKey = args.getString(0);
        //    JSONObject optionsJSON = args.getJSONObject(1);

        //    SocketAdapter socket = this.getSkocketAdapter(socketKey);

        //    SocketAdapterOptions options = new SocketAdapterOptions();
        //    options.setKeepAlive(getBooleanPropertyFromJSON(optionsJSON, "keepAlive"));
        //    options.setOobInline(getBooleanPropertyFromJSON(optionsJSON, "oobInline"));
        //    options.setReceiveBufferSize(getIntegerPropertyFromJSON(optionsJSON, "receiveBufferSize"));
        //    options.setSendBufferSize(getIntegerPropertyFromJSON(optionsJSON, "sendBufferSize"));
        //    options.setSoLinger(getIntegerPropertyFromJSON(optionsJSON, "soLinger"));
        //    options.setSoTimeout(getIntegerPropertyFromJSON(optionsJSON, "soTimeout"));
        //    options.setTrafficClass(getIntegerPropertyFromJSON(optionsJSON, "trafficClass"));

        //    try {
        //        socket.close();
        //        callbackContext.success();
        //    } catch (IOException e) {
        //        callbackContext.error(e.toString());
        //    }
        //}

        private void CloseEventHandler(string socketKey, bool hasError)
        {
            socketStorage.Remove(socketKey);
            this.DispatchEvent(new CloseSocketEvent
            {
                HasError = hasError,
                SocketKey = socketKey
            });
        }

        private void DataConsumer(string socketKey, byte[] data)
        {
            this.DispatchEvent(new DataReceivedSocketEvent
            {
                Data = data,
                SocketKey = socketKey
            });
        }

        private void ErrorHandler(string socketKey, Exception exception)
        {
            this.DispatchEvent(new ErrorSocketEvent
            {
                ErrorMessage = exception.Message,
                SocketKey = socketKey
            });
        }

        private void DispatchEvent(SocketEvent eventObject)
        {
            PluginResult result = new PluginResult(PluginResult.Status.OK, JsonHelper.Serialize(eventObject));
            result.KeepCallback = true;
            DispatchCommandResult(result, this.eventDispatcherCallbackId);
        }
    }
}
