package com.pixel.chatapp.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "usersOnChatUI", primaryKeys = {"otherUid"})
public class UserOnChatUI_Model {

    @NonNull
    private String otherUid; // other user uid

//    @NonNull
//    @ColumnInfo(defaultValue = "s4QQf6riOiRcwC9HMeA3S8TgL8y1")
    private String myUid;
    private String fromUid;
    private String from;
    private String emojiOnly;
    private String idKey;   // chat id
    private String message;
    private int type;
    private int msgStatus;
    private long timeSent;
    private String otherUserName;
    private String otherDisplayName;
    private String otherContactName;
    private String imageUrl;

    public UserOnChatUI_Model(String otherUid, String fromUid, String from, String emojiOnly, String message, int type,
                              long timeSent, int msgStatus, String idKey) {
        this.otherUid = otherUid;
        this.fromUid = fromUid;
        this.from = from;
        this.emojiOnly = emojiOnly;
        this.message = message;
        this.type = type;
        this.msgStatus = msgStatus;
        this.timeSent = timeSent;
        this.idKey = idKey;
    }

    public UserOnChatUI_Model() {
    }

    @NonNull
    public String getOtherUid() {
        return otherUid;
    }

    public void setOtherUid(@NonNull String otherUid) {
        this.otherUid = otherUid;
    }

    public String getMyUid() {
        return myUid;
    }

    public void setMyUid(String myUid) {
        this.myUid = myUid;
    }

    @NonNull
    public String getFromUid() {
        return fromUid;
    }

    public void setFromUid(@NonNull String fromUid) {
        this.fromUid = fromUid;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setType(int type) {
        this.type = type;
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

    public int getType() {
        return type;
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

    public String getOtherDisplayName() {
        return otherDisplayName;
    }

    public void setOtherDisplayName(String otherDisplayName) {
        this.otherDisplayName = otherDisplayName;
    }

    public String getOtherContactName() {
        return otherContactName;
    }

    public void setOtherContactName(String otherContactName) {
        this.otherContactName = otherContactName;
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
