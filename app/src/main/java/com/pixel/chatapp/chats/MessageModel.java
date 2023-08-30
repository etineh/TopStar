package com.pixel.chatapp.chats;

import android.view.View;

import java.io.Serializable;
import java.sql.Timestamp;

public class MessageModel implements Serializable {
//public class MessageModel {

    private String message;
    private String from, replyFrom;
    private String idKey;
    private String edit;
    private String replyMsg;
    private long timeSent;
    private int visibility;
    private  int msgStatus;

    private int type;
    private long randomID;
    private String replyID;
//    private String voicenote;

    public MessageModel() {
    }

    public MessageModel(String message, String from, String replyFrom, long timeSent, String idKey, String edit,
                        int visibility, String replyMsg, int msgStatus, int type, long randomID, String replyID) {
        this.message = message;
        this.from = from;
        this.timeSent = timeSent;
        this.idKey = idKey;
        this.edit = edit;
        this.visibility = visibility;
        this.replyMsg = replyMsg;
        this.msgStatus = msgStatus;
        this.replyFrom = replyFrom;
        this.type = type;
        this.randomID = randomID;
        this.replyID = replyID;

//        this.voicenote = voicenote;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getRandomID() {
        return randomID;
    }

    public String getReplyID() {
        return replyID;
    }

    public void setReplyID(String replyID) {
        this.replyID = replyID;
    }

    //    public String getVoicenote() {
//        return voicenote;
//    }
//
//    public void setVoicenote(String voicenote) {
//        this.voicenote = voicenote;
//    }
}
