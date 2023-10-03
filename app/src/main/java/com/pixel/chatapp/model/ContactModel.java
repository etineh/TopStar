package com.pixel.chatapp.model;

public class ContactModel {

    private String otherUid;
    private String image;
    private String userName;
    private Long presence;
    private String bio;
    private String myUserName;
    private String displayName;

    public ContactModel() {
    }

    public ContactModel(String otherUid, String image, String userName, Long presence, String bio, String myUserName, String displayName) {
        this.otherUid = otherUid;
        this.image = image;
        this.userName = userName;
        this.presence = presence;
        this.bio = bio;
        this.myUserName = myUserName;
        this.displayName = displayName;
    }

    public String getOtherUid() {
        return otherUid;
    }

    public void setOtherUid(String otherUid) {
        this.otherUid = otherUid;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getPresence() {
        return presence;
    }

    public String getBio() {
        return bio;
    }

    public String getMyUserName() {
        return myUserName;
    }

    public void setMyUserName(String myUserName) {
        this.myUserName = myUserName;
    }

    public String getDisplayName() {
        return displayName;
    }
}



















