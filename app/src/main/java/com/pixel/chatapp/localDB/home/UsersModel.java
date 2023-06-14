package com.pixel.chatapp.localDB.home;

import com.pixel.chatapp.chats.MessageModel;

public class UsersModel {
    MessageModel messageModel;
    String mes;

    public UsersModel(String mes) {
        this.mes = mes;
    }

    public String getMes() {
        return mes;
    }
}
