package com.pixel.chatapp.model;

public class PinMessageModel {

    private String msgId;
    private String message;
    private Object pinTime;
    private String pinByWho;

    public PinMessageModel() {
    }

    public PinMessageModel(String msgId, String message, Object pinTime, String pinByWho) {
        this.msgId = msgId;
        this.message = message;
        this.pinTime = pinTime;
        this.pinByWho = pinByWho;
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
}
