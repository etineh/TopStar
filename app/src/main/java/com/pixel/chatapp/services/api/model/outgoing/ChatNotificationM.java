package com.pixel.chatapp.services.api.model.outgoing;

import java.util.Map;

public class ChatNotificationM {

    String fcmToken;
    String otherUid;
    Map<String, Object> messageMap;

    public ChatNotificationM(String fcmToken, String otherUid, Map<String, Object> messageMap) {
        this.fcmToken = fcmToken;
        this.otherUid = otherUid;
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

    public Map<String, Object> getMessageMap() {
        return messageMap;
    }

    public void setMessageMap(Map<String, Object> messageMap) {
        this.messageMap = messageMap;
    }


}
