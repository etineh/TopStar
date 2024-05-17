package com.pixel.chatapp.model;

public class SupportChatM {

    private final String idKey;

    private final String fromUid;
    private final int type;
    private final String message;
    private final String from;
    private final String replyFrom;
    private final String replyMsg;
    private final long timeSent;
    private final String deliveryStatus;
    private final String imageSize;
    private final String replyID;
    private final String photoThumb;
    private final String photoUri;


    public SupportChatM(String idKey, String fromUid, int type, String message, String from,
                        String replyFrom, String replyMsg, long timeSent, String deliveryStatus,
                        String imageSize, String replyID, String photoThumb, String photoUri)
    {
        this.idKey = idKey;
        this.fromUid = fromUid;
        this.type = type;
        this.message = message;
        this.from = from;
        this.replyFrom = replyFrom;
        this.replyMsg = replyMsg;
        this.timeSent = timeSent;
        this.deliveryStatus = deliveryStatus;
        this.imageSize = imageSize;
        this.replyID = replyID;
        this.photoThumb = photoThumb;
        this.photoUri = photoUri;
    }

    public String getIdKey() {
        return idKey;
    }

    public String getFromUid() {
        return fromUid;
    }

    public int getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getFrom() {
        return from;
    }

    public String getReplyFrom() {
        return replyFrom;
    }

    public String getReplyMsg() {
        return replyMsg;
    }

    public long getTimeSent() {
        return timeSent;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public String getImageSize() {
        return imageSize;
    }

    public String getReplyID() {
        return replyID;
    }

    public String getPhotoThumb() {
        return photoThumb;
    }

    public String getPhotoUri() {
        return photoUri;
    }
}










