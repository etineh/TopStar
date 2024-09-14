package com.pixel.chatapp.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.pixel.chatapp.R;
import com.pixel.chatapp.activities.OTPActivity;
import com.pixel.chatapp.api.Dao_interface.OtpApiDao;
import com.pixel.chatapp.api.model.outgoing.TwoValueM;
import com.pixel.chatapp.api.model.incoming.ResultApiM;
import com.pixel.chatapp.constants.AllConstants;

import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OTPGenerator {

    //  ======= Email address =========

    public static void generateOtpEmail(String token, String email, Context context, TextView resend, TextView otpStatus_TV){
        OtpApiDao otpApiDao = AllConstants.retrofit.create(OtpApiDao.class);

        TwoValueM OTPModel = new TwoValueM(token, email);

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


    public static void verifyEmailOtp(String token, String otpValue, Context context, TextView otpStatus_TV,
                                      ProgressBar progressBarOTP, CallFinishMethod finishMethod)
    {
        OtpApiDao otpApiDao = AllConstants.retrofit.create(OtpApiDao.class);

        TwoValueM OTPModel = new TwoValueM(token, otpValue);

        // Make POST request
        Call<ResultApiM> call = otpApiDao.getOTP(OTPModel);

        call.enqueue(new Callback<ResultApiM>() {
            @Override
            public void onResponse(Call<ResultApiM> call, Response<ResultApiM> response) {

                if (response.isSuccessful()) {
                    ResultApiM resultApiM = response.body();

                    if(resultApiM.getResult() != null){
                        String getResult = resultApiM.getResult();

                        if(otpStatus_TV != null){
                            if(getResult.contains("Success")) {

                                finishMethod.finishOTPVerify();     // let the listener finish the work

                            } else {
                                otpStatus_TV.setText(resultApiM.getResult());
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
            public void onFailure(Call<ResultApiM> call, Throwable throwable) {
                otpStatus_TV.setText(context.getString(R.string.errorWithOtp));
                System.out.println("what is error OTPActivity OTPGe. L85 " + throwable.getMessage());

            }
        });

    }

    //  ======= phone number =========

    public static void sendOTPToNumber(String number, FirebaseAuth auth, PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks, Activity activity){
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(number)       // Phone number to verify
                        .setTimeout(30L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(activity)                 // (optional) Activity for callback binding
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
//                        authenticateAndProceed(number);   // for phone number
    }

    public static PhoneAuthProvider.OnVerificationStateChangedCallbacks otpListener
            (GetOtpCode otpCodeListener)
    {
        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                System.out.println("what is 'completed' : " + phoneAuthCredential.getSmsCode());
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                otpCodeListener.otpFailed();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                otpCodeListener.getOtpSent(s);
                System.out.println("what is code sent : " + s);
            }
        };

        return mCallbacks;
    }


    public interface CallFinishMethod {

        void finishOTPVerify();
    }

    public interface GetOtpCode {
        void getOtpSent(String otp);
        void otpFailed();
    }
}




