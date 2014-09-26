package cz.blocshop.socketsforcordova;

import java.io.IOException;
import java.net.SocketException;


public interface SocketAdapter {
	public void connect(String host, int port) throws IOException;
	public void write(byte[] data) throws IOException;
	public void close() throws IOException;	
	public void setOptions(SocketAdapterOptions options) throws SocketException;
	public void setDataConsumer(Consumer<byte[]> dataConsumer);
	public void setCloseEventHandler(Consumer<Boolean> closeEventHandler);
	public void setErrorHandler(Consumer<IOException> errorHandler);
}