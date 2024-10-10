package com.pixel.chatapp.services.api.model.incoming;

public class UserSearchM {

    private final String username;
    private final String email;
    private final String number;
    private final String uid;


    public UserSearchM(String username, String email, String number, String uid) {
        this.username = username;
        this.email = email;
        this.number = number;
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getNumber() {
        return number;
    }

    public String getUid() {
        return uid;
    }
}
