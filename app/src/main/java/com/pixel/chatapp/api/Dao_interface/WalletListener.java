package com.pixel.chatapp.api.Dao_interface;

import com.pixel.chatapp.api.model.incoming.AssetsModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface WalletListener {

    @GET("/wallet/test")
    Call<Long> test();

    @GET("/wallet/asset/{idToken}")
    Call<AssetsModel> assets(@Path("idToken") String idToken);

}
