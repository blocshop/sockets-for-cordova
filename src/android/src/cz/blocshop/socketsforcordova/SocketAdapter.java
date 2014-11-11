package cz.blocshop.socketsforcordova;

import java.io.IOException;
import java.net.SocketException;


public interface SocketAdapter {
	public void open(String host, int port) throws Throwable;
	public void write(byte[] data) throws IOException;
	public void shutdownWrite() throws IOException;
	public void close() throws IOException;	
	public void setOptions(SocketAdapterOptions options) throws SocketException;
	public void setDataConsumer(Consumer<byte[]> dataConsumer);
	public void setCloseEventHandler(Consumer<Boolean> closeEventHandler);
	public void setErrorHandler(Consumer<String> errorHandler);
}