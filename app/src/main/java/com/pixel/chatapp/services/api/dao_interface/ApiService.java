package com.pixel.chatapp.services.api.dao_interface;

import com.pixel.chatapp.services.api.model.VerificationResponse;
import com.pixel.chatapp.services.api.model.RequestBody;

import retrofit2.http.POST;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;

public interface ApiService {

    @POST("status")
    Call<VerificationResponse> getStatus(@Header("Authorization") String authorization, @Body RequestBody body);

}
