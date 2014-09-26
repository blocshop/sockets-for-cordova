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
