package com.pixel.chatapp.api.model;

public class LoginDetailM {

    private final String uid;
    private final String number;
//    private final String token;

    public LoginDetailM(String uid, String number /** String token */) {
        this.uid = uid;
        this.number = number;
//        this.token = token;
    }

    public String getUid() {
        return uid;
    }

    public String getNumber() {
        return number;
    }

//    public String getToken() {
//        return token;
//    }
}

