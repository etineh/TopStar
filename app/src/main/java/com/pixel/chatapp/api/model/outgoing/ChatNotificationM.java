package com.pixel.chatapp.api.model.outgoing;

import com.pixel.chatapp.model.MessageModel;

import java.util.Map;

public class ChatNotificationM {

    String fcmToken;
    String otherUid;
    String myContactName;
    Map<String, Object> messageMap;

    public ChatNotificationM(String fcmToken, String otherUid, String myContactName, Map<String, Object> messageMap) {
        this.fcmToken = fcmToken;
        this.otherUid = otherUid;
        this.myContactName = myContactName;
        this.messageMap = messageMap;
    }


    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getOtherUid() {
        return otherUid;
    }

    public void setOtherUid(String otherUid) {
        this.otherUid = otherUid;
    }

    public String getMyContactName() {
        return myContactName;
    }

    public void setMyContactName(String myContactName) {
        this.myContactName = myContactName;
    }

    public Map<String, Object> getMessageMap() {
        return messageMap;
    }

    public void setMessageMap(Map<String, Object> messageMap) {
        this.messageMap = messageMap;
    }


}
