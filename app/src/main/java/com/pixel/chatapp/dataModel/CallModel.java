package com.pixel.chatapp.dataModel;

import com.pixel.chatapp.constants.DataModelType;

public class CallModel {
    private String otherUid;
    private String targetName;
    private String senderUid;
    private String senderName;
    private String data;
    private DataModelType type;
    boolean isRinging;

    public CallModel(String otherUid, String targetName, String senderUid, String senderName,
                     String data, DataModelType type, boolean isRinging) {
        this.otherUid = otherUid;
        this.targetName = targetName;
        this.senderUid = senderUid;
        this.senderName = senderName;
        this.data = data;
        this.type = type;
        this.isRinging = isRinging;
    }

    public String getOtherUid() {
        return otherUid;
    }

    public void setOtherUid(String target) {
        this.otherUid = target;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public void setSenderUid(String sender) {
        this.senderUid = sender;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public DataModelType getType() {
        return type;
    }

    public void setType(DataModelType type) {
        this.type = type;
    }

    public boolean getIsRinging() {
        return isRinging;
    }

    public void setIsRinging(boolean ringing) {
        isRinging = ringing;
    }
}
