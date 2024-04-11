package com.pixel.chatapp.api.Dao_interface;

import com.pixel.chatapp.api.model.OTP_Model;
import com.pixel.chatapp.api.model.ResultApiModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface OtpApiDao {

    @POST("/otp/email")
    Call<Void> sendOTP(@Body OTP_Model OTPModel);

    @POST("/otp/confirm-otp")
    Call<ResultApiModel> getOTP(@Body OTP_Model OTPModel);

}
