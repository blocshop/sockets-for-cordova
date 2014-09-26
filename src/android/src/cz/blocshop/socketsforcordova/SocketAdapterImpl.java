package cz.blocshop.socketsforcordova;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SocketAdapterImpl implements SocketAdapter {
    
    private final int INPUT_STREAM_BUFFER_SIZE = 16 * 1024;
    private final Socket socket;
    
    private Consumer<byte[]> dataConsumer;
    private Consumer<Boolean> closeEventHandler;
    private Consumer<IOException> exceptionHandler;

    public SocketAdapterImpl() {
        this.socket = new Socket();
    }

    @Override
    public void connect(String host, int port) throws IOException {
        this.socket.connect(new InetSocketAddress(host, port));
        this.submitReadTask();
    }
    
    @Override
    public void write(byte[] data) throws IOException {
        this.socket.getOutputStream().write(data);
    }

    @Override
    public void close() throws IOException {
    	if (!this.socket.isClosed()) {
    		this.socket.shutdownOutput();
    	}
    }

    @Override
    public void setOptions(SocketAdapterOptions options) throws SocketException {
        if (options.getKeepAlive() != null) {
            this.socket.setKeepAlive(options.getKeepAlive());
        }
        if (options.getOobInline() != null) {
            this.socket.setOOBInline(options.getOobInline());
        }
        if (options.getSoLinger() != null) {
            this.socket.setSoLinger(true, options.getSoLinger());
        }
        if (options.getSoTimeout() != null) {
            this.socket.setSoTimeout(options.getSoTimeout());
        }
        if (options.getReceiveBufferSize() != null) {
            this.socket.setReceiveBufferSize(options.getReceiveBufferSize());
        }
        if (options.getSendBufferSize() != null) {
            this.socket.setSendBufferSize(options.getSendBufferSize());
        }
        if (options.getTrafficClass() != null) {
            this.socket.setTrafficClass(options.getTrafficClass());
        }
    }
    
    @Override
    public void setDataConsumer(Consumer<byte[]> dataConsumer) {
        this.dataConsumer = dataConsumer;
    }

    @Override
    public void setCloseEventHandler(Consumer<Boolean> closeEventHandler) {
        this.closeEventHandler = closeEventHandler;
    }

    @Override
    public void setErrorHandler(Consumer<IOException> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }
    
    private void submitReadTask() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                runRead();
            }
        });
    }
    
    private void runRead() {
        boolean hasError = false;
        try {
        	runReadLoop();
        } catch (IOException e) {
        	Logging.Error(SocketAdapterImpl.class.getName(), "Error during reading of socket input stream", e);
            hasError = true;
            invokeExceptionHandler(e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
            	Logging.Error(SocketAdapterImpl.class.getName(), "Error during closing of socket", e);
            } finally {
                invokeCloseEventHandler(hasError);
            }
        }
    }    
    
    private void runReadLoop() throws IOException {
        byte[] buffer = new byte[INPUT_STREAM_BUFFER_SIZE];
        int bytesRead = 0;
        
        while ((bytesRead = socket.getInputStream().read(buffer)) >= 0) {
        	byte[] data = buffer.length == bytesRead 
        			? buffer
        			: Arrays.copyOfRange(buffer, 0, bytesRead);
        	
            this.invokeDataConsumer(data);
        }
    }
    
    private void invokeDataConsumer(byte[] data) {
        if (this.dataConsumer != null) {
            this.dataConsumer.accept(data);
        }
    }
    
    private void invokeCloseEventHandler(boolean hasError) {
        if (this.closeEventHandler != null) {
            this.closeEventHandler.accept(hasError);
        }
    }
    
    private void invokeExceptionHandler(IOException exception) {
        if (this.exceptionHandler != null) {
            this.exceptionHandler.accept(exception);
        }
    }
}
