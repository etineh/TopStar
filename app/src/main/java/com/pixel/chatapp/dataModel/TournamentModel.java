package com.pixel.chatapp.dataModel;

public class TournamentModel {

    String title;
    String entryFee;
    String reward;
    String gameType;
    String startDate;
    String numOfParticipant;
    String stage;
    String tourLink;
    String communityLink;
    String sponsoredBy;
    String remark;
    int type;
    String sessionState;

//    public TournamentModel() {
//    }

    public TournamentModel(String title, String entryFee, String reward, String gameType, String startDate,
                           String numOfParticipant, String stage, String tourLink, String communityLink,
                           String sponsoredBy, String remark, int type, String sessionState)
    {
        this.title = title;
        this.entryFee = entryFee;
        this.reward = reward;
        this.gameType = gameType;
        this.startDate = startDate;
        this.numOfParticipant = numOfParticipant;
        this.stage = stage;
        this.tourLink = tourLink;
        this.communityLink = communityLink;
        this.sponsoredBy = sponsoredBy;
        this.remark = remark;
        this.type = type;
        this.sessionState = sessionState;

    }


    public String getTitle() {
        return title;
    }

    public String getEntryFee() {
        return entryFee;
    }

    public String getReward() {
        return reward;
    }

    public String getGameType() {
        return gameType;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getNumOfParticipant() {
        return numOfParticipant;
    }

    public String getStage() {
        return stage;
    }

    public String getTourLink() {
        return tourLink;
    }

    public String getCommunityLink() {
        return communityLink;
    }

    public String getSponsoredBy() {
        return sponsoredBy;
    }

    public String getRemark() {
        return remark;
    }

    public int getType() {
        return type;
    }

    public String getSessionState() {
        return sessionState;
    }
}
