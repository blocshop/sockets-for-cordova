package cz.blocshop.socketsforcordova;

import android.annotation.SuppressLint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class SocketPlugin extends CordovaPlugin {
	
	Map<String, SocketAdapter> socketAdapters = new HashMap<String, SocketAdapter>(); 
	
	@Override
	public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {

		if(action.equals("create")) {
			this.create(args, callbackContext);
		} else if (action.equals("connect")) {
			this.connect(args, callbackContext);
		} else if (action.equals("write")) {
			this.write(args, callbackContext);
		} else if (action.equals("close")) {
			this.close(args, callbackContext);
		} else if (action.equals("setOptions")) {
			this.setOptions(args, callbackContext);
		} else {
			callbackContext.error(String.format("SocketPlugin - invalid action:", action));
			return false;
		}
		return true;
	}
	
	private void create(CordovaArgs args, CallbackContext callbackContext) throws JSONException {
		
		final String socketKey = args.getString(0);
		
		SocketAdapter socketAdapter = new SocketAdapterImpl();
		socketAdapter.setCloseEventHandler(new CloseEventHandler(socketKey));
		socketAdapter.setDataConsumer(new DataConsumer(socketKey));
		socketAdapter.setErrorHandler(new ErrorHandler(socketKey));
			
		this.socketAdapters.put(socketKey, socketAdapter);
		
		callbackContext.success(socketKey);
	}
	
	private void connect(CordovaArgs args, CallbackContext callbackContext) throws JSONException {
		String socketKey = args.getString(0);
		String host = args.getString(1);
		int port = args.getInt(2);
		
		SocketAdapter socket = this.getSocketAdapter(socketKey);
		
		try {
			socket.connect(host, port);
			callbackContext.success();
		} catch (IOException e) {
			callbackContext.error(e.toString());
		}
	}
	
	private void write(CordovaArgs args, CallbackContext callbackContext) throws JSONException {
		String socketKey = args.getString(0);
		JSONArray data = args.getJSONArray(1);
		
		byte[] dataBuffer = new byte[data.length()];
		for(int i = 0; i < dataBuffer.length; i++) {
			dataBuffer[i] = (byte) data.getInt(i);
		}
		
		SocketAdapter socket = this.getSocketAdapter(socketKey);
		
		try {
			socket.write(dataBuffer);
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
			throw new IllegalArgumentException(
					String.format("Cannot find socketKey: %s. Connection is probably closed.", socketKey));
		}
		return this.socketAdapters.get(socketKey);
	}
	
	private void dispatchEvent(JSONObject jsonEventObject) {
		this.webView.sendJavascript(String.format("window.Socket.dispatchEvent(%s);", jsonEventObject.toString()));		
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
				event.put("data", new JSONArray(data));
				event.put("socketKey", socketKey);
				
				dispatchEvent(event);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	private class ErrorHandler implements Consumer<IOException> {
		private String socketKey;
		public ErrorHandler(String socketKey) {
			this.socketKey = socketKey;
		}
		@Override
		public void accept(IOException exception) {
			try {
				JSONObject event = new JSONObject();
				event.put("type", "Error");
				event.put("errorMessage", exception.toString());
				event.put("socketKey", socketKey);
				
				dispatchEvent(event);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}
