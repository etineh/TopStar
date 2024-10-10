package com.pixel.chatapp.dataModel;

public class SupportUserM {

    private final String supportUid;
    private final String supportName;
    private final String deliveryStatus;
    private final long time;
    private final String chat;
    private final String chatCount;


    public SupportUserM(String supportUid, String supportName, String deliveryStatus, long time, String chat, String chatCount) {
        this.supportUid = supportUid;
        this.supportName = supportName;
        this.deliveryStatus = deliveryStatus;
        this.time = time;
        this.chat = chat;
        this.chatCount = chatCount;
    }


    public String getSupportUid() {
        return supportUid;
    }

    public String getSupportName() {
        return supportName;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public long getTime() {
        return time;
    }

    public String getChat() {
        return chat;
    }

    public String getChatCount() {
        return chatCount;
    }


}
