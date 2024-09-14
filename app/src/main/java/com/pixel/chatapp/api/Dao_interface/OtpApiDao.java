package com.pixel.chatapp.api.Dao_interface;

import com.pixel.chatapp.api.model.outgoing.TwoValueM;
import com.pixel.chatapp.api.model.incoming.ResultApiM;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface OtpApiDao {

    @POST("/otp/email")
    Call<Void> sendOTP(@Body TwoValueM OTPModel);

    @POST("/otp/confirm-otp")
    Call<ResultApiM> getOTP(@Body TwoValueM OTPModel);

}
