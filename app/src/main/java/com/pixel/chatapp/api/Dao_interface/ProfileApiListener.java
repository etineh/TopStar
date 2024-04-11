package com.pixel.chatapp.api.Dao_interface;

import com.pixel.chatapp.api.model.OTP_Model;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ProfileApiListener {

    @POST("/setting/profile/email")
    Call<Void> updateEmail(@Body OTP_Model OTPModel);


}
