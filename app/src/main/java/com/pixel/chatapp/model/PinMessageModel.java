package com.pixel.chatapp.model;

public class PinMessageModel {

    private String msgId;
    private String message;
    private Object pinTime;
    private String pinByWho;
    private String pinByUID;

    public PinMessageModel() {
    }

    public PinMessageModel(String msgId, String message, Object pinTime, String pinByWho, String pinByUID) {
        this.msgId = msgId;
        this.message = message;
        this.pinTime = pinTime;
        this.pinByWho = pinByWho;
        this.pinByUID = pinByUID;
    }

    public String getMsgId() {
        return msgId;
    }

    public String getMessage() {
        return message;
    }

    public Object getPinTime() {
        return pinTime;
    }

    public void setPinTime(long pinTime) {
        this.pinTime = pinTime;
    }

    public String getPinByWho() {
        return pinByWho;
    }

    public void setPinByWho(String pinByWho) {
        this.pinByWho = pinByWho;
    }

    public String getPinByUID() {
        return pinByUID;
    }

    public void setPinByUID(String pinByUID) {
        this.pinByUID = pinByUID;
    }
}
