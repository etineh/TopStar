package com.pixel.chatapp.model;

public class LeagueModel {

    String title;
    String entryFee;
    String reward;
    String gameType;
    long timeCreated;
    String whoCanParticipate;
    String minimumSlot;
    String leagueLink;
    String communityLink;
    String sponsoredBy;
    String remark;

    public LeagueModel(String title, String entryFee, String reward, String gameType, long timeCreated,
                       String whoCanParticipate, String minimumSlot, String leagueLink, String communityLink, String sponsoredBy, String remark)
    {
        this.title = title;
        this.entryFee = entryFee;
        this.reward = reward;
        this.gameType = gameType;
        this.timeCreated = timeCreated;
        this.whoCanParticipate = whoCanParticipate;
        this.minimumSlot = minimumSlot;
        this.leagueLink = leagueLink;
        this.communityLink = communityLink;
        this.sponsoredBy = sponsoredBy;
        this.remark = remark;
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

    public long getTimeCreated() {
        return timeCreated;
    }

    public String getWhoCanParticipate() {
        return whoCanParticipate;
    }

    public String getMinimumSlot() {
        return minimumSlot;
    }

    public String getLeagueLink() {
        return leagueLink;
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
}
