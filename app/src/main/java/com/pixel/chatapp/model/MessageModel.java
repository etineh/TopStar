package com.pixel.chatapp.model;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.sql.Timestamp;

@Entity(tableName = "chats")
public class MessageModel implements Serializable {
//public class MessageModel {

    @PrimaryKey
    @NonNull
    private String idKey;

    private String myUid;
    private String fromUid;
    private String message;
    private String emojiOnly;
    private String from;
    private String replyFrom;
    private String edit;
    private String replyMsg;

    @NonNull
    private long timeSent;
//    @NonNull
//    @ColumnInfo(defaultValue = "sfrssrfsffsessfesefsef")
    private String newChatNumberID;
    private int msgStatus;
    private int type;
    private String imageSize;
    private String replyID;
    private boolean isChatPin;
    private boolean isChatForward;
    private String emoji;
    private String id;
    private String voiceNote;
    private String vnDuration;
    private String photoUriPath;
    private String photoUriOriginal;


    public MessageModel() {
    }

    public MessageModel(String message, String from, String fromUid, String replyFrom, long timeSent, String idKey,
                        String edit, String newChatNumberID, String replyMsg, int msgStatus, int type, String imageSize,
                        String replyID, Boolean isChatPin, Boolean isChatForward, String emoji, String emojiOnly,
                        String voiceNote, String vnDuration, String photoUriPath, String photoUriOriginal) {
        this.message = message;
        this.from = from;
        this.timeSent = timeSent;
        this.idKey = idKey;
        this.edit = edit;
        this.newChatNumberID = newChatNumberID;
        this.replyMsg = replyMsg;
        this.msgStatus = msgStatus;
        this.replyFrom = replyFrom;
        this.type = type;
        this.imageSize = imageSize;
        this.replyID = replyID;
        this.isChatPin = isChatPin;
        this.isChatForward = isChatForward;
        this.emoji = emoji;
        this.emojiOnly = emojiOnly;
        this.fromUid = fromUid;

        this.voiceNote = voiceNote;
        this.vnDuration = vnDuration;

        this.photoUriPath = photoUriPath;
        this.photoUriOriginal = photoUriOriginal;
    }
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMyUid() {
        return myUid;
    }

    public void setMyUid(String myUid) {
        this.myUid = myUid;
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

    public String getNewChatNumberID() {
        return newChatNumberID;
    }

    public void setNewChatNumberID(String newChatNumberID) {
        this.newChatNumberID = newChatNumberID;
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

    public String getImageSize() {
        return imageSize;
    }

    public void setImageSize(String imageSize) {
        this.imageSize = imageSize;
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

    public String getVoiceNote() {
        return voiceNote;
    }

    public void setVoiceNote(String voiceNote) {
        this.voiceNote = voiceNote;
    }

    public String getVnDuration() {
        return vnDuration;
    }

    public void setVnDuration(String vnDuration) {
        this.vnDuration = vnDuration;
    }

    public String getPhotoUriPath() {
        return photoUriPath;
    }

    public void setPhotoUriPath(String photoUriPath) {
        this.photoUriPath = photoUriPath;
    }

    public String getPhotoUriOriginal() {
        return photoUriOriginal;
    }

    public void setPhotoUriOriginal(String photoUriOriginal) {
        this.photoUriOriginal = photoUriOriginal;
    }
}
