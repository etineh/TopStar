package com.pixel.chatapp.services.api.model;

public class SetUpM {

    private final String idToken;
    private final String email;
    private final String password;
    private final String displayName;
    private final String username;

    public SetUpM(String idToken, String email, String password, String displayName, String username) {
        this.idToken = idToken;
        this.email = email;
        this.password = password;
        this.displayName = displayName;
        this.username = username;
    }

    public String getIdToken() {
        return idToken;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUsername() {
        return username;
    }
}
