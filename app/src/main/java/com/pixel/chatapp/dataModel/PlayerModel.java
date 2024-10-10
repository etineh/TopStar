package com.pixel.chatapp.dataModel;

public class PlayerModel {

    private final String playerName;
    private final String gameType;
    private final String mode;
    private final String amount;
    private final long timeCreated;
    private final String fromUID;
    private final String photoLink;
    private final int type;
    private String fromWhere;



    public PlayerModel(String playerName, String gameType, String mode, String amount,
                       long timeCreated, String fromUID, String photoLink, int type, String fromWhere)
    {
        this.playerName = playerName;
        this.gameType = gameType;
        this.mode = mode;
        this.amount = amount;
        this.timeCreated = timeCreated;
        this.fromUID = fromUID;
        this.photoLink = photoLink;
        this.type = type;
        this.fromWhere = fromWhere;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getGameType() {
        return gameType;
    }

    public String getMode() {
        return mode;
    }

    public String getAmount() {
        return amount;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public String getFromUID() {
        return fromUID;
    }

    public int getType() {
        return type;
    }

    public String getPhotoLink() {
        return photoLink;
    }

    public String getFromWhere() {
        return fromWhere;
    }

    public void setFromWhere(String fromWhere) {
        this.fromWhere = fromWhere;
    }

}
