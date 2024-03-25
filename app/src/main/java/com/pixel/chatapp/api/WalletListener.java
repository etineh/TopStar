package com.pixel.chatapp.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;

public interface WalletListener {

    @GET("/dailywork")
    Call<List<String>> getName();
}
