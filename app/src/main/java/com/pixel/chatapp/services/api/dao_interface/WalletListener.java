package com.pixel.chatapp.services.api.dao_interface;

import com.pixel.chatapp.services.api.model.incoming.AssetsModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface WalletListener {

    @GET("/wallet/test")
    Call<Long> test();

    @GET("/wallet/asset/{idToken}")
    Call<AssetsModel> assets(@Path("idToken") String idToken);

}
