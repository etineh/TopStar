package com.pixel.chatapp.model;

import java.util.Date;

public class WalletHistory {

    private String otherUid, otherUsername, otherDisplayName;
    private String transactionInfo, amount, transactionStatus, transactionID, transactionType;
    private Date date;
    private long previousAmount, current_Amount;


    public WalletHistory(String otherUid, String otherUsername, String otherDisplayName,
                         String transactionInfo, Date date, String amount, String transactionStatus)
    {
        this.otherUid = otherUid;
        this.otherUsername = otherUsername;
        this.otherDisplayName = otherDisplayName;
        this.transactionInfo = transactionInfo;
        this.date = date;
        this.amount = amount;
        this.transactionStatus = transactionStatus;
    }

    public String getOtherUid() {
        return otherUid;
    }

    public void setOtherUid(String otherUid) {
        this.otherUid = otherUid;
    }

    public String getOtherUsername() {
        return otherUsername;
    }

    public void setOtherUsername(String otherUsername) {
        this.otherUsername = otherUsername;
    }

    public String getOtherDisplayName() {
        return otherDisplayName;
    }

    public void setOtherDisplayName(String otherDisplayName) {
        this.otherDisplayName = otherDisplayName;
    }

    public String getTransactionInfo() {
        return transactionInfo;
    }

    public void setTransactionInfo(String transactionInfo) {
        this.transactionInfo = transactionInfo;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public long getPreviousAmount() {
        return previousAmount;
    }

    public void setPreviousAmount(long previousAmount) {
        this.previousAmount = previousAmount;
    }

    public long getCurrent_Amount() {
        return current_Amount;
    }

    public void setCurrent_Amount(long current_Amount) {
        this.current_Amount = current_Amount;
    }
}
