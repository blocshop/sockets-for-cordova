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

public class SocketAdapterOptions {
    
    private Boolean keepAlive;
    private Boolean oobInline;
    private Integer soLinger;
    private Integer soTimeout;
    private Integer receiveBufferSize;
    private Integer sendBufferSize;
    private Integer trafficClass;

    /**
     * @return the keepAlive
     */
    public Boolean getKeepAlive() {
        return keepAlive;
    }

    /**
     * @param keepAlive the keepAlive to set
     */
    public void setKeepAlive(Boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    /**
     * @return the oobInline
     */
    public Boolean getOobInline() {
        return oobInline;
    }

    /**
     * @param oobInline the oobInline to set
     */
    public void setOobInline(Boolean oobInline) {
        this.oobInline = oobInline;
    }

    /**
     * @return the soLinger
     */
    public Integer getSoLinger() {
        return soLinger;
    }

    /**
     * @param soLinger the soLinger to set
     */
    public void setSoLinger(Integer soLinger) {
        this.soLinger = soLinger;
    }
    
    /**
     * @return the soTimeout
     */
    public Integer getSoTimeout() {
        return soTimeout;
    }

    /**
     * @param soTimeout the soTimeout to set
     */
    public void setSoTimeout(Integer soTimeout) {
        this.soTimeout = soTimeout;
    }

    /**
     * @return the receiveBufferSize
     */
    public Integer getReceiveBufferSize() {
        return receiveBufferSize;
    }

    /**
     * @param receiveBufferSize the receiveBufferSize to set
     */
    public void setReceiveBufferSize(Integer receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    /**
     * @return the sendBufferSize
     */
    public Integer getSendBufferSize() {
        return sendBufferSize;
    }

    /**
     * @param sendBufferSize the sendBufferSize to set
     */
    public void setSendBufferSize(Integer sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    /**
     * @return the trafficClass
     */
    public Integer getTrafficClass() {
        return trafficClass;
    }

    /**
     * @param trafficClass the trafficClass to set
     */
    public void setTrafficClass(Integer trafficClass) {
        this.trafficClass = trafficClass;
    }
}
