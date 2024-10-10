package com.pixel.chatapp.services.api.model.outgoing;

public class LoginDetailM {

    private final String uid;
    private final String number;
    private final String email;
    private final String username;

    public LoginDetailM(String uid, String number, String email, String username) {
        this.uid = uid;
        this.number = number;
        this.email = email;
        this.username = username;
    }

    public String getUid() {
        return uid;
    }

    public String getNumber() {
        return number;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }
}

