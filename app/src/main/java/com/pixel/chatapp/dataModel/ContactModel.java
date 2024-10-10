package com.pixel.chatapp.dataModel;

public class ContactModel {

    private String otherUid;
    private String image;
    private String otherUserName;
    private Long presence;
    private String bio;
    private String myUserName;
    private String contactName;
    private String otherDisplayName;
    private String number;

    public ContactModel() {
    }

    public ContactModel(String otherUid, String image, String otherUserName, String otherDisplayName,
                        Long presence, String bio, String myUserName, String contactName, String number)
    {
        this.otherUid = otherUid;
        this.image = image;
        this.otherUserName = otherUserName;
        this.otherDisplayName = otherDisplayName;
        this.presence = presence;
        this.bio = bio;
        this.myUserName = myUserName;
        this.contactName = contactName;
        this.number = number;
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

    public Long getPresence() {
        return presence;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getMyUserName() {
        return myUserName;
    }

    public void setMyUserName(String myUserName) {
        this.myUserName = myUserName;
    }

    public String getContactName() {
        return contactName;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}



















