package com.pixel.chatapp.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

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
    private String senderName; // changed from "from"
    private String replyFrom;
    private String edit;
    private String replyMsg;

    @NonNull
    private long timeSent;
    private String newChatNumberID;
    private int msgStatus;
    private int type;
    private String imageSize;
    private String replyID;
    private boolean chatIsPin; // changed from "isChatPin"
    private boolean chatIsForward; // changed from "isChatForward"
    private String emoji;
    private String otherUid;    // otherUid
    private String voiceNote;
    private String vnDuration;
    private String photoUriPath;
    private String photoUriOriginal;

    // Default constructor
    public MessageModel() {
    }

    // Constructor with parameters
    public MessageModel(String message, String senderName, String fromUid, String replyFrom, long timeSent, String idKey,
                        String edit, String newChatNumberID, String replyMsg, int msgStatus, int type, String imageSize,
                        String replyID, boolean chatIsPin, boolean chatIsForward, String emoji, String emojiOnly,
                        String voiceNote, String vnDuration, String photoUriPath, String photoUriOriginal) {
        this.message = message;
        this.senderName = senderName;
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
        this.chatIsPin = chatIsPin;
        this.chatIsForward = chatIsForward;
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

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
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

    public boolean getChatIsPin() {
        return chatIsPin;
    }

    public void setChatIsPin(boolean chatIsPin) {
        this.chatIsPin = chatIsPin;
    }

    public boolean getChatIsForward() {
        return chatIsForward;
    }

    public void setChatIsForward(boolean chatIsForward) {
        this.chatIsForward = chatIsForward;
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

    public String getOtherUid() {
        return otherUid;
    }

    public void setOtherUid(String otherUid) {
        this.otherUid = otherUid;
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
