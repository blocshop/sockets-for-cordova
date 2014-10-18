package cz.blocshop.socketsforcordova;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class SocketAdapterImpl implements SocketAdapter {
    
    private final int INPUT_STREAM_BUFFER_SIZE = 16 * 1024;
    private final Socket socket;
    
    private Consumer<byte[]> dataConsumer;
    private Consumer<Boolean> closeEventHandler;
    private Consumer<String> exceptionHandler;
    
    private ExecutorService executor;

    public SocketAdapterImpl() {
        this.socket = new Socket();
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void open(String host, int port) throws Throwable {
    	this.openWithBackgroundThread(host, port);
        this.submitReadTask();
    }
    
    @Override
    public void write(byte[] data) throws IOException {
        this.socket.getOutputStream().write(data);
    }

    @Override
    public void shutdownWrite() throws IOException {
    	this.socket.shutdownOutput();
    }
    
    @Override
    public void close() throws IOException {
    	this.socket.close();
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
    public void setErrorHandler(Consumer<String> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }
    
    private void openWithBackgroundThread(final String host, final int port) throws Throwable {
        Future<?> future = this.executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
					socket.connect(new InetSocketAddress(host, port));
				} catch (IOException e) {
					Logging.Error(SocketAdapterImpl.class.getName(), "Error during connecting of socket", e.getCause());
					throw new RuntimeException(e);
				}
            }
        });
        
        try {
			future.get();
        } 
        catch (ExecutionException e) {
			throw e.getCause();
		}
    }
    
    private void submitReadTask() {
        this.executor.submit(new Runnable() {
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
        } catch (Throwable e) {
        	Logging.Error(SocketAdapterImpl.class.getName(), "Error during reading of socket input stream", e);
            hasError = true;
            invokeExceptionHandler(e.getMessage());
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
    
    private void invokeExceptionHandler(String errorMessage) {
        if (this.exceptionHandler != null) {
            this.exceptionHandler.accept(errorMessage);
        }
    }
}
