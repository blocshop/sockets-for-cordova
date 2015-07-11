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