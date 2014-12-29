package cz.blocshop.socketsforcordova;

import java.io.IOException;
import java.net.SocketException;


public interface SocketAdapter {
	public void open(String host, int port);
	public void write(byte[] data) throws IOException;
	public void shutdownWrite() throws IOException;
	public void close() throws IOException;	
	public void setOptions(SocketAdapterOptions options) throws SocketException;
	public void setOpenEventHandler(Consumer<Void> openEventHandler);
	public void setOpenErrorEventHandler(Consumer<String> openErrorEventHandler);
	public void setDataConsumer(Consumer<byte[]> dataConsumer);
	public void setCloseEventHandler(Consumer<Boolean> closeEventHandler);
	public void setErrorEventHandler(Consumer<String> errorEventHandler);
}