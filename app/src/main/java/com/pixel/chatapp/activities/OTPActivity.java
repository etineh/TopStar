package com.pixel.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.pixel.chatapp.R;
import com.pixel.chatapp.api.Dao_interface.ProfileApiListener;
import com.pixel.chatapp.api.model.OTP_Model;
import com.pixel.chatapp.constants.AllConstants;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OTPActivity extends AppCompatActivity implements OTPGenerator.CallFinishMethod {

    TextView sendOTP_TV, otpHint_TV, done;
    EditText getOTP_ET;

    // Get instance of FirebaseAuth
    FirebaseAuth auth = FirebaseAuth.getInstance();
    ProgressBar progressBarOTP;

    int close = 0;
    public static int cancleOTP = 0;

    String type, value, token, pass;
    CountDownTimer countDownTimer;

    // interface listener
    public interface UpdateFieldListener{
        void updateEmail(String value);
    }

    public static UpdateFieldListener updateFieldListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpactivity);

        otpHint_TV = findViewById(R.id.otpHint_TV);
        sendOTP_TV = findViewById(R.id.sendOTP_TV);
        getOTP_ET = findViewById(R.id.getOTP_ET);
        progressBarOTP = findViewById(R.id.progressBarOTP);
        ImageView closeOTPpage= findViewById(R.id.closeOTPpage);
        done = findViewById(R.id.verifyOTP);
        TextView otpStatus_TV = findViewById(R.id.otpStatus_TV);
        TextView verifyTopInfo = findViewById(R.id.verifyDetails_TV);

        type = getIntent().getStringExtra("type");
        token = getIntent().getStringExtra("token");
        value = getIntent().getStringExtra("value");
        pass = getIntent().getStringExtra("pass");

        OTPGenerator.finishMethod = this;

        if(type.equals("email")){
            otpHint_TV.setText(getString(R.string.enterOTPEmail));
            verifyTopInfo.setText(getString(R.string.verifyEmail));
            OTPGenerator.generateOtp(token, value, this, sendOTP_TV, otpStatus_TV);

        } else  if(type.equals("phoneNumber")){
            otpHint_TV.setText(getString(R.string.enterOTPNumber));
            verifyTopInfo.setText(getString(R.string.verifyPhoneNumber));
        }

        getOTP_ET.requestFocus();

        sendOTP_TV.setOnClickListener(v -> {
            if(sendOTP_TV.getText() == getString(R.string.resend_)){
                otpStatus_TV.setText(getString(R.string.sendingOtp));
                countOTPtime(); // button click send
                OTPGenerator.generateOtp(token, value, this, sendOTP_TV, otpStatus_TV);
            } else {
                Toast.makeText(this, getString(R.string.waitForTime), Toast.LENGTH_SHORT).show();
            }

        });


        closeOTPpage.setOnClickListener(v -> onBackPressed());

        done.setOnClickListener(v -> {

            if(getOTP_ET.length() > 4){     // check for verify OTP later

                // Save to database later
                if(type.equals("phoneNumber")){

                    Toast.makeText(OTPActivity.this, getString(R.string.phoneNumberUpdated), Toast.LENGTH_SHORT).show();
                    finish();

                } else if (type.equals("email")){
                    progressBarOTP.setVisibility(View.VISIBLE);
                    OTPGenerator.getOtp(token, getOTP_ET.getText().toString(), this, otpStatus_TV, progressBarOTP);
//                    updateEmail();
                }
            } else Toast.makeText(this, getString(R.string.invalidOTP), Toast.LENGTH_SHORT).show();

        });

        countOTPtime();

    }



    //  ======= methods ==========

    public void updateEmail(){
        ProfileApiListener profileApiListener = AllConstants.retrofit.create(ProfileApiListener.class);

        OTP_Model OTPModel = new OTP_Model(token, value);

        // Make PUT request
        Call<Void> call = profileApiListener.updateEmail(OTPModel);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.isSuccessful()){

                    auth.signInWithEmailAndPassword(value, pass);
                    updateFieldListener.updateEmail(value);

                    Toast.makeText(OTPActivity.this, getString(R.string.emailNumberUpdated), Toast.LENGTH_SHORT).show();
                    finish();

                } else {
                    try {
                        String errorMessage = response.errorBody().string();
                        if(errorMessage.contains("EMAIL_EXISTS")){
                            Toast.makeText(OTPActivity.this, getString(R.string.emailExist), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(OTPActivity.this, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                progressBarOTP.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable throwable) {
                Toast.makeText(OTPActivity.this, getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                progressBarOTP.setVisibility(View.GONE);
                System.out.println("what is error OTPActivity L103 " + throwable.getMessage());
            }
        });
    }

    private void countOTPtime(){

        countDownTimer = new CountDownTimer(60_000, 1_000){
            @Override
            public void onTick(long millisUntilFinished) {
                String timeLeft = String.valueOf(millisUntilFinished / 1000);
                if(cancleOTP == 0 ) sendOTP_TV.setText(timeLeft);
                else {
                    sendOTP_TV.setText(getString(R.string.resend_));
                    countDownTimer.cancel();
                    cancleOTP = 0;
                }
            }

            @Override
            public void onFinish() {
                sendOTP_TV.setText(getString(R.string.resend_));
            }
        }.start();

    }


    @Override
    public void onBackPressed() {
        if(close == 0){
            Toast.makeText(this, getString(R.string.pressAgain), Toast.LENGTH_SHORT).show();
            close = 1;
            new Handler().postDelayed( ()-> close = 0, 5_000);
        } else {
            super.onBackPressed();
            finish();
        }
    }


    // =====    interface
    @Override
    public void finishOTPVerify() {
        updateEmail();
    }


}











