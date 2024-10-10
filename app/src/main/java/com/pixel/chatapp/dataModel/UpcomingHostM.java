package com.pixel.chatapp.dataModel;

public class UpcomingHostM {

    String logo;
    String gameTitle;
    String startDate;

    public UpcomingHostM(String logo, String gameTitle, String startDate) {
        this.logo = logo;
        this.gameTitle = gameTitle;
        this.startDate = startDate;
    }


    public String getLogo() {
        return logo;
    }

    public String getGameTitle() {
        return gameTitle;
    }

    public String getStartDate() {
        return startDate;
    }


}
