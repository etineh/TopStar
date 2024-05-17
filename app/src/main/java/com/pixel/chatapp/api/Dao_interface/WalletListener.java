package com.pixel.chatapp.api.Dao_interface;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;

public interface WalletListener {

    @GET("/wallet/test")
    Call<Long> test();


}
