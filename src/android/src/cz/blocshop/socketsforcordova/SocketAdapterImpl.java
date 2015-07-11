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
    
    private Consumer<Void> openEventHandler;
    private Consumer<String> openErrorEventHandler;
    private Consumer<byte[]> dataConsumer;
    private Consumer<Boolean> closeEventHandler;
    private Consumer<String> errorEventHandler;
    
    private ExecutorService executor;

    public SocketAdapterImpl() {
        this.socket = new Socket();
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void open(final String host, final int port) {
        this.executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
					socket.connect(new InetSocketAddress(host, port));
					invokeOpenEventHandler();
					submitReadTask();
				} catch (IOException e) {
					Logging.Error(SocketAdapterImpl.class.getName(), "Error during connecting of socket", e.getCause());
					invokeOpenErrorEventHandler(e.getMessage());
				}
            }
        });
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
    	this.invokeCloseEventHandler(false);
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
	public void setOpenEventHandler(Consumer<Void> openEventHandler) {
		this.openEventHandler = openEventHandler;
	}

	@Override
	public void setOpenErrorEventHandler(Consumer<String> openErrorEventHandler) {
		this.openErrorEventHandler = openErrorEventHandler;
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
    public void setErrorEventHandler(Consumer<String> errorEventHandler) {
        this.errorEventHandler = errorEventHandler;
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

    private void invokeOpenEventHandler() {
        if (this.openEventHandler != null) {
            this.openEventHandler.accept((Void)null);
        }
    }
    
    private void invokeOpenErrorEventHandler(String errorMessage) {
        if (this.openErrorEventHandler != null) {
            this.openErrorEventHandler.accept(errorMessage);
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
        if (this.errorEventHandler != null) {
            this.errorEventHandler.accept(errorMessage);
        }
    }
}
