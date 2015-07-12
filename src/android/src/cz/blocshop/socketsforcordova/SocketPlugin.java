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

package cz.blocshop.socketsforcordova;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;

public class SocketPlugin extends CordovaPlugin {

    private String WEB_VIEW_DATA_STRING_ENCODING = "ISO-8859-1";

	private Map<String, SocketAdapter> socketAdapters = new HashMap<String, SocketAdapter>();
    private CallbackContext eventDispatcherCallbackContext = null;
	
	@Override
	public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("write")) {
            this.write(args, callbackContext);
        } else if (action.equals("open")) {
            this.open(args, callbackContext);
        } else if (action.equals("close")) {
            this.close(args, callbackContext);
		} else if (action.equals("shutdownWrite")) {
			this.shutdownWrite(args, callbackContext);
		} else if (action.equals("registerEventDispatcher")) {
            this.registerEventDispatcher(args, callbackContext);
        } else if (action.equals("setOptions")) {
			this.setOptions(args, callbackContext);
		} else {
			callbackContext.error("SocketPlugin - invalid action: " + action);
			return false;
		}
		return true;
	}

    private void registerEventDispatcher(CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
        this.eventDispatcherCallbackContext = callbackContext;
    }

	private void open(CordovaArgs args, CallbackContext callbackContext) throws JSONException {
		String socketKey = args.getString(0);
		String host = args.getString(1);
		int port = args.getInt(2);
		
		SocketAdapter socketAdapter = new SocketAdapterImpl();
		socketAdapter.setCloseEventHandler(new CloseEventHandler(socketKey));
		socketAdapter.setDataConsumer(new DataConsumer(socketKey));
		socketAdapter.setErrorEventHandler(new ErrorEventHandler(socketKey));
		socketAdapter.setOpenErrorEventHandler(new OpenErrorEventHandler(callbackContext));
		socketAdapter.setOpenEventHandler(new OpenEventHandler(socketKey, socketAdapter, callbackContext));
		
		socketAdapter.open(host, port);
	}
	
	private void write(CordovaArgs args, CallbackContext callbackContext) throws JSONException {
		String socketKey = args.getString(0);
		String dataString = args.getString(1);

        byte[] dataBuffer;
        try {
            dataBuffer = dataString.getBytes(WEB_VIEW_DATA_STRING_ENCODING);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            callbackContext.error(e.toString());
            return;
        }

        SocketAdapter socket = this.getSocketAdapter(socketKey);
		
		try {
			socket.write(dataBuffer);
			callbackContext.success();
		} catch (IOException e) {
			callbackContext.error(e.toString());
		}
	}

	private void shutdownWrite(CordovaArgs args, CallbackContext callbackContext) throws JSONException {
		String socketKey = args.getString(0);
		
		SocketAdapter socket = this.getSocketAdapter(socketKey);
		
		try {
			socket.shutdownWrite();
			callbackContext.success();
		} catch (IOException e) {
			callbackContext.error(e.toString());
		}
	}
	
	private void close(CordovaArgs args, CallbackContext callbackContext) throws JSONException {
		String socketKey = args.getString(0);
		
		SocketAdapter socket = this.getSocketAdapter(socketKey);
		
		try {
			socket.close();
			callbackContext.success();
		} catch (IOException e) {
			callbackContext.error(e.toString());
		}
	}
	
	private void setOptions(CordovaArgs args, CallbackContext callbackContext) throws JSONException {
		
		String socketKey = args.getString(0);
		JSONObject optionsJSON = args.getJSONObject(1);
		
		SocketAdapter socket = this.getSocketAdapter(socketKey);
		
		SocketAdapterOptions options = new SocketAdapterOptions();
		options.setKeepAlive(getBooleanPropertyFromJSON(optionsJSON, "keepAlive"));
		options.setOobInline(getBooleanPropertyFromJSON(optionsJSON, "oobInline"));
		options.setReceiveBufferSize(getIntegerPropertyFromJSON(optionsJSON, "receiveBufferSize"));
		options.setSendBufferSize(getIntegerPropertyFromJSON(optionsJSON, "sendBufferSize"));
		options.setSoLinger(getIntegerPropertyFromJSON(optionsJSON, "soLinger"));
		options.setSoTimeout(getIntegerPropertyFromJSON(optionsJSON, "soTimeout"));
		options.setTrafficClass(getIntegerPropertyFromJSON(optionsJSON, "trafficClass"));
		
		try {
			socket.close();
			callbackContext.success();
		} catch (IOException e) {
			callbackContext.error(e.toString());
		}
	}
	
	private Boolean getBooleanPropertyFromJSON(JSONObject jsonObject, String propertyName) throws JSONException {
		return jsonObject.has(propertyName) ? jsonObject.getBoolean(propertyName) : null;
	}
	
	private Integer getIntegerPropertyFromJSON(JSONObject jsonObject, String propertyName) throws JSONException {
		return jsonObject.has(propertyName) ? jsonObject.getInt(propertyName) : null;
	}
	
	private SocketAdapter getSocketAdapter(String socketKey) {
		if (!this.socketAdapters.containsKey(socketKey)) {
			throw new IllegalStateException("Socket isn't connected.");
		}
		return this.socketAdapters.get(socketKey);
	}
	
	private void dispatchEvent(final JSONObject jsonEventObject) {
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PluginResult result = new PluginResult(PluginResult.Status.OK, jsonEventObject);
                result.setKeepCallback(true);
                eventDispatcherCallbackContext.sendPluginResult(result);
            }
        });
	}
	
	private class CloseEventHandler implements Consumer<Boolean> {
		private String socketKey;
		public CloseEventHandler(String socketKey) {
			this.socketKey = socketKey;
		}
		@Override
		public void accept(Boolean hasError) {			
			socketAdapters.remove(this.socketKey);
			
			try {
				JSONObject event = new JSONObject();
				event.put("type", "Close");
				event.put("hasError", hasError.booleanValue());
				event.put("socketKey", this.socketKey);
		
				dispatchEvent(event);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	private class DataConsumer implements Consumer<byte[]> {
		private String socketKey;
		public DataConsumer(String socketKey) {
			this.socketKey = socketKey;
		}
		@SuppressLint("NewApi") 
		@Override
		public void accept(byte[] data) {
			try {
				JSONObject event = new JSONObject();
				event.put("type", "DataReceived");
				event.put("data", this.byteArrayToString(data));
				event.put("socketKey", socketKey);
				
				dispatchEvent(event);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

        private String byteArrayToString(byte[] array) {
            try {
                return new String(array, WEB_VIEW_DATA_STRING_ENCODING);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return "";
            }
        }
	}
	
	private class ErrorEventHandler implements Consumer<String> {
		private String socketKey;
		public ErrorEventHandler(String socketKey) {
			this.socketKey = socketKey;
		}
		@Override
		public void accept(String errorMessage) {
			try {
				JSONObject event = new JSONObject();
				event.put("type", "Error");
				event.put("errorMessage", errorMessage);
				event.put("socketKey", socketKey);
				
				dispatchEvent(event);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	private class OpenErrorEventHandler implements Consumer<String> {
		private CallbackContext openCallbackContext;
		public OpenErrorEventHandler(CallbackContext openCallbackContext) {
			this.openCallbackContext = openCallbackContext;
		}
		@Override
		public void accept(String errorMessage) {
			this.openCallbackContext.error(errorMessage);
		}
	}
	
	private class OpenEventHandler implements Consumer<Void> {
		private String socketKey;
		private SocketAdapter socketAdapter;
		private CallbackContext openCallbackContext;
		public OpenEventHandler(String socketKey, SocketAdapter socketAdapter, CallbackContext openCallbackContext) {
			this.socketKey = socketKey;
			this.socketAdapter = socketAdapter;
			this.openCallbackContext = openCallbackContext;
		}
		@Override
		public void accept(Void voidObject) {
			socketAdapters.put(socketKey, socketAdapter);
			this.openCallbackContext.success();
		}
	}
}
