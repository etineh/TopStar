package com.pixel.chatapp.chats;

import java.sql.Timestamp;

public class MessageModel {

    String message;
    String from;
    long timeSent;

    public MessageModel() {
    }

    public MessageModel(String message, String from, long timeSent) {
        this.message = message;
        this.from = from;
        this.timeSent = timeSent;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
//
    public long getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(long timeSent) {
        this.timeSent = timeSent;
    }
}
