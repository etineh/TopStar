package com.pixel.chatapp.activities;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pixel.chatapp.R;
import com.pixel.chatapp.api.Dao_interface.OtpApiDao;
import com.pixel.chatapp.api.model.OTP_Model;
import com.pixel.chatapp.api.model.ResultApiModel;
import com.pixel.chatapp.constants.AllConstants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OTPGenerator {

    public static void generateOtp(String token, String email, Context context, TextView resend, TextView otpStatus_TV){
        OtpApiDao otpApiDao = AllConstants.retrofit.create(OtpApiDao.class);

        OTP_Model OTPModel = new OTP_Model(token, email);

        // Make POST request
        Call<Void> call = otpApiDao.sendOTP(OTPModel);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.isSuccessful()){

                    Toast.makeText(context, context.getString(R.string.otpSentEmail), Toast.LENGTH_SHORT).show();
                    otpStatus_TV.setText(context.getString(R.string.otpSentEmail));
                } else {
                    Toast.makeText(context, context.getString(R.string.errorWithOtp), Toast.LENGTH_SHORT).show();
                    otpStatus_TV.setText(context.getString(R.string.errorWithOtp));
                    OTPActivity.cancleOTP = 1;
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable throwable) {
                Toast.makeText(context, context.getString(R.string.timeoutResendOtp), Toast.LENGTH_SHORT).show();
                otpStatus_TV.setText(context.getString(R.string.timeoutResendOtp));
                OTPActivity.cancleOTP = 1;
                resend.setText(context.getString(R.string.resend_));
                System.out.println("what is error OTPActivity OTPGe. L45 " + throwable.getMessage());
            }
        });

    }

    public static void getOtp(String token, String otpValue, Context context, TextView otpStatus_TV, ProgressBar progressBarOTP)
    {
        OtpApiDao otpApiDao = AllConstants.retrofit.create(OtpApiDao.class);

        OTP_Model OTPModel = new OTP_Model(token, otpValue);

        // Make POST request
        Call<ResultApiModel> call = otpApiDao.getOTP(OTPModel);

        call.enqueue(new Callback<ResultApiModel>() {
            @Override
            public void onResponse(Call<ResultApiModel> call, Response<ResultApiModel> response) {

                if (response.isSuccessful()) {
                    ResultApiModel resultApiModel = response.body();

                    if(resultApiModel.getResult() != null){
                        String getResult = resultApiModel.getResult();

                        if(otpStatus_TV != null){
                            if(getResult.contains("Success")) {
                                finishMethod.finishOTPVerify();

                            } else {
                                otpStatus_TV.setText(resultApiModel.getResult());
                                if (progressBarOTP != null) progressBarOTP.setVisibility(View.GONE);
                            }
                        }

                    }

                } else {
                    String errorMessage = response.message();
                    System.out.println("what is error OTPGen L88 : " + errorMessage);
                    if(otpStatus_TV != null) otpStatus_TV.setText(context.getString(R.string.errorOccur));

                    if (progressBarOTP != null) progressBarOTP.setVisibility(View.GONE);
                }

            }

            @Override
            public void onFailure(Call<ResultApiModel> call, Throwable throwable) {
                otpStatus_TV.setText(context.getString(R.string.errorWithOtp));
                System.out.println("what is error OTPActivity OTPGe. L85 " + throwable.getMessage());

            }
        });

    }

    public static CallFinishMethod finishMethod;

    public interface CallFinishMethod {

        void finishOTPVerify();
    }

}




