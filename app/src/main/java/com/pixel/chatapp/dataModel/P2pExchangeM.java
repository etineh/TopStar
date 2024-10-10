package com.pixel.chatapp.dataModel;

public class P2pExchangeM {

    String displayName;
    String online;
    String paySpeed;
    String totalOrder;

    String amountRange;
    String fee;

    String paymentOptions;

    String buyOrSell;
    String userImage_TV;

    public P2pExchangeM(String displayName, String totalOrder, String amountRange, String fee, String buyOrSell) {
        this.displayName = displayName;
        this.totalOrder = totalOrder;
        this.amountRange = amountRange;
        this.fee = fee;
        this.buyOrSell = buyOrSell;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public String getPaySpeed() {
        return paySpeed;
    }

    public void setPaySpeed(String paySpeed) {
        this.paySpeed = paySpeed;
    }

    public String getTotalOrder() {
        return totalOrder;
    }

    public void setTotalOrder(String totalOrder) {
        this.totalOrder = totalOrder;
    }

    public String getAmountRange() {
        return amountRange;
    }

    public void setAmountRange(String amountRange) {
        this.amountRange = amountRange;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getPaymentOptions() {
        return paymentOptions;
    }

    public void setPaymentOptions(String paymentOptions) {
        this.paymentOptions = paymentOptions;
    }

    public String getBuyOrSell() {
        return buyOrSell;
    }

    public void setBuyOrSell(String buyOrSell) {
        this.buyOrSell = buyOrSell;
    }

    public String getUserImage_TV() {
        return userImage_TV;
    }

    public void setUserImage_TV(String userImage_TV) {
        this.userImage_TV = userImage_TV;
    }
}
