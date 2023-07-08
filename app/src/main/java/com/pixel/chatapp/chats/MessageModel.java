package com.pixel.chatapp.chats;

import android.view.View;

import java.sql.Timestamp;

public class MessageModel {

    private String message;
    private String from, replyFrom;
    private String idKey;
    private String edit;
    private String replyMsg;
    private long timeSent;
    private int visibility;
    private  int msgStatus;
    public MessageModel() {
    }

    public MessageModel(String message, String from, String replyFrom, long timeSent, String idKey, String edit, int visibility, String replyMsg, int msgStatus) {
        this.message = message;
        this.from = from;
        this.timeSent = timeSent;
        this.idKey = idKey;
        this.edit = edit;
        this.visibility = visibility;
        this.replyMsg = replyMsg;
        this.msgStatus = msgStatus;
        this.replyFrom = replyFrom;
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

    public String getIdKey() {
        return idKey;
    }

    public void setIdKey(String idKey) {
        this.idKey = idKey;
    }

    public String getEdit() {
        return edit;
    }

    public void setEdit(String edit) {
        this.edit = edit;
    }

    public String getReplyMsg() {
        return replyMsg;
    }

    public void setReplyMsg(String replyMsg) {
        this.replyMsg = replyMsg;
    }

    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }

    public int getMsgStatus() {
        return msgStatus;
    }

    public void setMsgStatus(int msgStatus) {
        this.msgStatus = msgStatus;
    }

    public String getReplyFrom() {
        return replyFrom;
    }

    public void setReplyFrom(String replyFrom) {
        this.replyFrom = replyFrom;
    }
}
