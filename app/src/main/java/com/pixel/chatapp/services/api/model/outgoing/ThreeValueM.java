package com.pixel.chatapp.services.api.model.outgoing;

public class ThreeValueM {

    private final String IdToken;
    private final String valueOne;
    private final String valueTwo;


    public ThreeValueM(String IdToken, String valueOne, String valueTwo) {
        this.IdToken = IdToken;
        this.valueOne = valueOne;
        this.valueTwo = valueTwo;
    }

    public String getIdToken() {
        return IdToken;
    }

    public String getValueOne() {
        return valueOne;
    }

    public String getValueTwo() {
        return valueTwo;
    }
}
