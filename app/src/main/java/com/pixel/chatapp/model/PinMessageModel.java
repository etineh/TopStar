package com.pixel.chatapp.model;

public class PinMessageModel {

    private String msgId;
    private String message;
    private long pinTime;

    public PinMessageModel() {
    }

    public PinMessageModel(String msgId, String message, long pinTime) {
        this.msgId = msgId;
        this.message = message;
        this.pinTime = pinTime;
    }

    public String getMsgId() {
        return msgId;
    }

    public String getMessage() {
        return message;
    }

    public long getPinTime() {
        return pinTime;
    }

    public void setPinTime(long pinTime) {
        this.pinTime = pinTime;
    }
}
