package com.pixel.chatapp.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "usersOnChatUI", primaryKeys = {"id"})
public class UserOnChatUI_Model {

    @NonNull
    private String id;

    private String emojiOnly;
    private String idKey;
    private String message;
    private int msgStatus;
    private long timeSent;
    private String otherUserName;
    private String imageUrl;
    public UserOnChatUI_Model(String id, String emojiOnly,String message, long timeSent,
                              int msgStatus, String idKey) {
        this.id = id;
        this.emojiOnly = emojiOnly;
        this.message = message;
        this.msgStatus = msgStatus;
        this.timeSent = timeSent;
        this.idKey = idKey;
    }

    public UserOnChatUI_Model() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmojiOnly() {
        return emojiOnly;
    }

    public void setEmojiOnly(String emojiOnly) {
        this.emojiOnly = emojiOnly;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(long timeSent) {
        this.timeSent = timeSent;
    }

    public int getMsgStatus() {
        return msgStatus;
    }

    public void setMsgStatus(int msgStatus) {
        this.msgStatus = msgStatus;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getIdKey() {
        return idKey;
    }

    public void setIdKey(String idKey) {
        this.idKey = idKey;
    }
}
