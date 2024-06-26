package com.pixel.chatapp.signup_login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.pixel.chatapp.R;
import com.pixel.chatapp.all_utils.NumberSpacing;
import com.pixel.chatapp.all_utils.OTPGenerator;
import com.pixel.chatapp.all_utils.PhoneUtils;
import com.pixel.chatapp.api.Dao_interface.UserDao;
import com.pixel.chatapp.api.model.LoginDetailM;
import com.pixel.chatapp.api.model.incoming.ResultApiM;
import com.pixel.chatapp.constants.AllConstants;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateAccountActivity extends AppCompatActivity {

    int close = 0;

    LinearLayout goToLoginPage;
    ImageView closePage_IV, support_IV;
    TextView number_TV, changeNumber_TV, sendOTP_TV, verifyOTPAndCreate, codeError_TV;
    ProgressBar progressBarOTP;
    EditText editTextCode;
    String getNumber;

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    String getCodeSent;

    FirebaseAuth auth = FirebaseAuth.getInstance();

    CountDownTimer countDownTimer;

    int cancelOTP = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        goToLoginPage = findViewById(R.id.goToLoginPage);
        closePage_IV = findViewById(R.id.arrowBack_IV);
        support_IV = findViewById(R.id.support_IV);
        number_TV = findViewById(R.id.number_TV);
        changeNumber_TV = findViewById(R.id.changeNumber_TV);
        sendOTP_TV = findViewById(R.id.sendOTP_TV);
        verifyOTPAndCreate = findViewById(R.id.verifyOTPAndCreate);
        progressBarOTP = findViewById(R.id.progressBarOTP);
        editTextCode = findViewById(R.id.editTextCode);
        codeError_TV = findViewById(R.id.codeError_TV);

        new Handler().postDelayed(()-> editTextCode.requestFocus(), 1000);

        getNumber = getIntent().getStringExtra("number");

        String spaceNumber = NumberSpacing.formatPhoneNumber(getNumber, 3, 3);
        number_TV.setText(spaceNumber);

        mCallbacksInitialise(); // for sending otp firebase

        sendOTP_TV.setOnClickListener(v -> {
            if(sendOTP_TV.getText().equals(getString(R.string.resend_)) || sendOTP_TV.getText().equals(getString(R.string.sendOTP)))
            {
                countOTPTime(); // button click send

                OTPGenerator.sendOTPToNumber(getNumber, auth, mCallbacks, this);

            } else {
                Toast.makeText(this, getString(R.string.waitForTime), Toast.LENGTH_SHORT).show();
            }
        });

        verifyOTPAndCreate.setOnClickListener(v -> {

            if(editTextCode.length() > 5){
                PhoneUtils.hideKeyboard(this, editTextCode);

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(getCodeSent, editTextCode.getText().toString());

                createAccount(credential);  // verify button onClick

            } else Toast.makeText(this, getText(R.string.invalidOTP), Toast.LENGTH_SHORT).show();

        });

        support_IV.setOnClickListener(v -> {
            Toast.makeText(this, "in progress", Toast.LENGTH_SHORT).show();
        });

        goToLoginPage.setOnClickListener(v -> {
            startActivity(new Intent(this, EmailOrPhoneLoginActivity.class));
            finish();
        });

        closePage_IV.setOnClickListener(v -> onBackPressed());

        changeNumber_TV.setOnClickListener(v -> {
            close = 1;
            startActivity(new Intent(this, PhoneLoginActivity.class));
            finish();
        });

    }


    //  =======     method      ============

    private void mCallbacksInitialise(){
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                createAccount(phoneAuthCredential); // callBack method
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                cancelOTP = 1;
                Toast.makeText(CreateAccountActivity.this, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                getCodeSent = s;
                System.out.println("what is code sent : " + s);
            }
        };
    }

    private void createAccount(PhoneAuthCredential credential) {

        verifyOTPAndCreate.setVisibility(View.INVISIBLE);
        progressBarOTP.setVisibility(View.VISIBLE);

        auth.signInWithCredential(credential).addOnCompleteListener(task -> {

            codeError_TV.setVisibility(View.VISIBLE);

            if (task.isSuccessful()) {

                loginApi();

            } else if (task.getException().getMessage().contains("expired"))
            {
                Toast.makeText(CreateAccountActivity.this, getString(R.string.smsCodeExpired), Toast.LENGTH_SHORT).show();
                codeError_TV.setText(getString(R.string.smsCodeExpired));
                progressBarOTP.setVisibility(View.GONE);
                verifyOTPAndCreate.setVisibility(View.VISIBLE);
                verifyOTPAndCreate.setVisibility(View.VISIBLE);

            }  else if (task.getException().getMessage().contains("SMS/TOTP is invalid"))
            {
                Toast.makeText(CreateAccountActivity.this, getString(R.string.invalidOTP), Toast.LENGTH_SHORT).show();
                codeError_TV.setText(getString(R.string.invalidOTP));
                progressBarOTP.setVisibility(View.GONE);
                verifyOTPAndCreate.setVisibility(View.VISIBLE);

            } else if (task.getException().getMessage().contains("timeout"))
            {
                Toast.makeText(CreateAccountActivity.this, getString(R.string.isNetwork), Toast.LENGTH_SHORT).show();
                codeError_TV.setText(getString(R.string.isNetwork));
                progressBarOTP.setVisibility(View.GONE);
                verifyOTPAndCreate.setVisibility(View.VISIBLE);

            } else {
                Toast.makeText(CreateAccountActivity.this, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show();
                codeError_TV.setText(getString(R.string.errorOccur));
                progressBarOTP.setVisibility(View.GONE);
                verifyOTPAndCreate.setVisibility(View.VISIBLE);

            }

        }).addOnFailureListener(e -> {
            codeError_TV.setVisibility(View.VISIBLE);
            codeError_TV.setText(getString(R.string.errorOccur));
            progressBarOTP.setVisibility(View.GONE);
        });

    }

    private void loginApi() {

        UserDao userDao = AllConstants.retrofit.create(UserDao.class);

        LoginDetailM loginDetailM = new LoginDetailM(auth.getUid(), getNumber, null, null);

        userDao.login(loginDetailM).enqueue(new Callback<ResultApiM>() {
            @Override
            public void onResponse(Call<ResultApiM> call, Response<ResultApiM> response) {

                if(response.isSuccessful()){

                    startActivity(new Intent(CreateAccountActivity.this, SetUpProfileActivity.class));
                    Toast.makeText(CreateAccountActivity.this, getString(R.string.accountCreated), Toast.LENGTH_SHORT).show();

                    finish();

                } else {

                    try {
                        String error = response.errorBody().string();

                        JsonObject jsonObject = new Gson().fromJson(error, JsonObject.class);
                        String message = jsonObject.get("message").getAsString();

                        codeError_TV.setVisibility(View.VISIBLE);
                        codeError_TV.setText(message);
                        verifyOTPAndCreate.setVisibility(View.VISIBLE);
                        progressBarOTP.setVisibility(View.GONE);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }

            }

            @Override
            public void onFailure(Call<ResultApiM> call, Throwable throwable) {

                verifyOTPAndCreate.setVisibility(View.VISIBLE);
                progressBarOTP.setVisibility(View.GONE);
                codeError_TV.setVisibility(View.VISIBLE);

                if(throwable.getMessage().contains("Failed to connect")) {  // server error
                    codeError_TV.setText(getString(R.string.serverError));

                } else{     // no internet connection | timeout
                    codeError_TV.setText(getString(R.string.isNetwork));
                }

                System.out.println("what is err CreateAcc: L220 " + throwable.getMessage());

            }
        });
    }


    private void countOTPTime()
    {
        countDownTimer = new CountDownTimer(60_000, 1_000){
            @Override
            public void onTick(long millisUntilFinished) {
                String timeLeft = String.valueOf(millisUntilFinished / 1000);
                if(cancelOTP == 0 ) sendOTP_TV.setText(timeLeft);
                else {
                    sendOTP_TV.setText(getString(R.string.resend_));
                    countDownTimer.cancel();
                    cancelOTP = 0;
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
            startActivity(new Intent(this, PhoneLoginActivity.class));
            finish();
        }
    }
}