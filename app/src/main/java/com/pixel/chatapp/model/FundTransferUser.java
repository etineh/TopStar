package com.pixel.chatapp.model;

public class FundTransferUser {

    String imagePath, displayName, username, userUid;

    public FundTransferUser(String imagePath, String displayName, String username, String userUid) {
        this.imagePath = imagePath;
        this.displayName = displayName;
        this.username = username;
        this.userUid = userUid;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUsername() {
        return username;
    }

    public String getUserUid() {
        return userUid;
    }
}
