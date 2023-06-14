package com.pixel.chatapp.model;

public class ChatListModel {
    private String id;
    private int num;

    public ChatListModel(String id, int num) {
        this.id = id;
        this.num = num;
    }

    public ChatListModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}
