package com.pixel.chatapp.dataModel;

public class LiveGameM {

    String totalWatch;
    String gameTitle;
    String gameName;
    String totalPlayer;
    String currentPlayer;
    String player1Photo;
    String player2Photo;
    String winnerPrize;
    String sponsoredBy;
//    TextView sponsoredBy;


    public LiveGameM(String totalWatch, String gameTitle, String gameName, String totalPlayer,
                     String currentPlayer, String player1Photo, String player2Photo,
                     String winnerPrize, String sponsoredBy)
    {
        this.totalWatch = totalWatch;
        this.gameTitle = gameTitle;
        this.gameName = gameName;
        this.totalPlayer = totalPlayer;
        this.currentPlayer = currentPlayer;
        this.player1Photo = player1Photo;
        this.player2Photo = player2Photo;
        this.winnerPrize = winnerPrize;
        this.sponsoredBy = sponsoredBy;
    }

    public String getTotalWatch() {
        return totalWatch;
    }

    public String getGameTitle() {
        return gameTitle;
    }

    public String getGameName() {
        return gameName;
    }

    public String getTotalPlayer() {
        return totalPlayer;
    }

    public String getCurrentPlayer() {
        return currentPlayer;
    }

    public String getPlayer1Photo() {
        return player1Photo;
    }

    public String getPlayer2Photo() {
        return player2Photo;
    }

    public String getWinnerPrize() {
        return winnerPrize;
    }

    public String getSponsoredBy() {
        return sponsoredBy;
    }
}
