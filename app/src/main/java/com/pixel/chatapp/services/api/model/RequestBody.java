package com.pixel.chatapp.services.api.model;

import com.google.gson.annotations.SerializedName;

public class RequestBody {

    @SerializedName("reference")
    private String reference;

    public RequestBody(String reference) {
        this.reference = reference;
    }

    public String getReference() {
        return reference;
    }
}
