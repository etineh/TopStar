package com.pixel.chatapp.dataModel;

public class GameRankM {

    String gameHeading;
    String totalPlay;
    String totalWin;
    String totalLoss;
    String worldRank;
//    String gameHeading;


    public GameRankM(String gameHeading, String totalPlay, String totalWin, String totalLoss, String worldRank) {
        this.gameHeading = gameHeading;
        this.totalPlay = totalPlay;
        this.totalWin = totalWin;
        this.totalLoss = totalLoss;
        this.worldRank = worldRank;
    }


    public String getGameHeading() {
        return gameHeading;
    }

    public String getTotalPlay() {
        return totalPlay;
    }

    public String getTotalWin() {
        return totalWin;
    }

    public String getTotalLoss() {
        return totalLoss;
    }

    public String getWorldRank() {
        return worldRank;
    }
}
