package com.pixel.chatapp.model;

public class EditMessageModel {

    private String from;
    private String edit;
    private String message;
    private long timeSent;
    private String id;
    private long randomID;

    public EditMessageModel(){

    }

    public EditMessageModel(String from, String edit, String message, long timeSent, String id, long randomID){

        this.from = from;
        this.edit = edit;
        this.message = message;
        this.timeSent = timeSent;
        this.id = id;
        this.randomID = randomID;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getEdit() {
        return edit;
    }

    public String getMessage() {
        return message;
    }

    public long getTimeSent() {
        return timeSent;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getRandomID() {
        return randomID;
    }
}
