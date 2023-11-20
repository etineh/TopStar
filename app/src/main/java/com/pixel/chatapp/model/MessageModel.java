package com.pixel.chatapp.model;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.room.Entity;

import java.io.Serializable;
import java.sql.Timestamp;

@Entity(tableName = "chats", primaryKeys = {"idKey"})

//public class MessageModel implements Serializable {
public class MessageModel {

    @NonNull
    private String idKey;

    private String fromUid;
    private String message;
    private String emojiOnly;
    private String from, replyFrom;
    private String edit;
    private String replyMsg;
    private long timeSent;
    private int visibility;
    private int msgStatus;
    private int type;
    private long randomID;
    private String replyID;
    private boolean isChatPin;
    private boolean isChatForward;

    private String emoji;

    private String id;      // link id between the user and chat

//    private String voicenote;

    public MessageModel() {
    }

    public MessageModel(String message, String from, String fromUid, String replyFrom, long timeSent, String idKey,
                        String edit, int visibility, String replyMsg, int msgStatus, int type, long randomID,
                        String replyID, Boolean isChatPin, Boolean isChatForward, String emoji, String emojiOnly) {
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
        this.isChatPin = isChatPin;
        this.isChatForward = isChatForward;
        this.emoji = emoji;
        this.emojiOnly = emojiOnly;
        this.fromUid = fromUid;

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

    public String getFromUid() {
        return fromUid;
    }

    public void setFromUid(String fromUid) {
        this.fromUid = fromUid;
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

    public void setRandomID(long randomID) {
        this.randomID = randomID;
    }

    public String getReplyID() {
        return replyID;
    }

    public void setReplyID(String replyID) {
        this.replyID = replyID;
    }

    public Boolean getIsChatPin() {
        return isChatPin;
    }

    public void setChatPin(boolean chatPin) {
        isChatPin = chatPin;
    }

    public Boolean getIsChatForward() {
        return isChatForward;
    }

    public void setChatForward(boolean chatForward) {
        isChatForward = chatForward;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public String getEmojiOnly() {
        return emojiOnly;
    }

    public void setEmojiOnly(String emojiOnly) {
        this.emojiOnly = emojiOnly;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    //    public String getVoicenote() {
//        return voicenote;
//    }
//
//    public void setVoicenote(String voicenote) {
//        this.voicenote = voicenote;
//    }
}
