package com.pixel.chatapp.view_controller.signup_login;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.pixel.chatapp.R;
import com.pixel.chatapp.utilities.NumberSpacing;
import com.pixel.chatapp.utilities.OTPGenerator;
import com.pixel.chatapp.utilities.OpenActivityUtil;
import com.pixel.chatapp.constants.Ki;
import com.pixel.chatapp.view_controller.MainActivity;
import com.pixel.chatapp.view_controller.side_bar_menu.support.SupportActivity;

public class NumberWithoutEmailActivity extends AppCompatActivity {

    int close = 0;
    ImageView arrowBack_IV, support_IV;
    TextView sendCode_TV_, number_TV, codeError_TV, verifyAccount;
    EditText editTextCode;
    ProgressBar progressBarOTP;

    String getNumber;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    String getCodeSent;

    FirebaseAuth auth = FirebaseAuth.getInstance();

    CountDownTimer countDownTimer;

    int cancelOTP = 0;
    SharedPreferences resetLoginSharePref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number_without_email);

        arrowBack_IV = findViewById(R.id.arrowBack_IV);
        support_IV = findViewById(R.id.support_IV);
        sendCode_TV_ = findViewById(R.id.sendCode_TV_);
        number_TV = findViewById(R.id.number_TV);
        editTextCode = findViewById(R.id.editTextCode);

        codeError_TV = findViewById(R.id.codeError_TV);
        progressBarOTP = findViewById(R.id.progressBarOTP);
        verifyAccount = findViewById(R.id.verifyAccount);

        resetLoginSharePref = this.getSharedPreferences(Ki.RESET_LOGIN, Context.MODE_PRIVATE);


        new Handler().postDelayed(()-> editTextCode.requestFocus(), 1000);

        getNumber = getIntent().getStringExtra("number");

        assert getNumber != null;
        String spaceNumber = NumberSpacing.formatPhoneNumber(getNumber, 3, 3);
        number_TV.setText(spaceNumber);

        mCallbacksInitialise(); // for sending otp firebase

        sendCode_TV_.setOnClickListener(v -> {
            if(sendCode_TV_.getText().equals(getString(R.string.resend_)) || sendCode_TV_.getText().equals(getString(R.string.sendOTP)))
            {
                countOTPTime(); // button click send

                OTPGenerator.sendOTPToNumber(getNumber, auth, mCallbacks, this);

            } else {
                Toast.makeText(this, getString(R.string.waitForTime), Toast.LENGTH_SHORT).show();
            }

        });

        verifyAccount.setOnClickListener(v -> {

            if(editTextCode.length() > 5)
            {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(getCodeSent, editTextCode.getText().toString());
                loginAccount(credential);

            } else Toast.makeText(this, getText(R.string.invalidOTP), Toast.LENGTH_SHORT).show();

        });

        arrowBack_IV.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        support_IV.setOnClickListener(v -> {

            Intent intent = new Intent(this, SupportActivity.class);
            intent.putExtra("reason", "No email link to my number yet");
            OpenActivityUtil.openColorHighlight(v, this, intent);
        });

        getOnBackPressedDispatcher().addCallback(callback);

    }

    //  =======     methods     ======

    private void loginAccount(PhoneAuthCredential credential) {

        verifyAccount.setVisibility(View.INVISIBLE);
        progressBarOTP.setVisibility(View.VISIBLE);

        auth.signInWithCredential(credential).addOnCompleteListener(task -> {

            codeError_TV.setVisibility(View.VISIBLE);

            if (task.isSuccessful()) {

                resetLoginSharePref.edit().putBoolean(auth.getUid(), true).apply();

                startActivity(new Intent(NumberWithoutEmailActivity.this, MainActivity.class));
                Toast.makeText(NumberWithoutEmailActivity.this, getString(R.string.loginSuccessful), Toast.LENGTH_SHORT).show();

                finish();

            } else if (task.getException().getMessage().contains("expired"))
            {
                Toast.makeText(NumberWithoutEmailActivity.this, getString(R.string.smsCodeExpired), Toast.LENGTH_SHORT).show();
                codeError_TV.setText(getString(R.string.smsCodeExpired));
                progressBarOTP.setVisibility(View.GONE);
                verifyAccount.setVisibility(View.VISIBLE);
                verifyAccount.setVisibility(View.VISIBLE);

            }  else if (task.getException().getMessage().contains("SMS/TOTP is invalid"))
            {
                Toast.makeText(NumberWithoutEmailActivity.this, getString(R.string.invalidOTP), Toast.LENGTH_SHORT).show();
                codeError_TV.setText(getString(R.string.invalidOTP));
                progressBarOTP.setVisibility(View.GONE);
                verifyAccount.setVisibility(View.VISIBLE);

            } else if (task.getException().getMessage().contains("timeout"))
            {
                Toast.makeText(NumberWithoutEmailActivity.this, getString(R.string.isNetwork), Toast.LENGTH_SHORT).show();
                codeError_TV.setText(getString(R.string.isNetwork));
                progressBarOTP.setVisibility(View.GONE);
                verifyAccount.setVisibility(View.VISIBLE);

            } else {
                Toast.makeText(NumberWithoutEmailActivity.this, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show();
                codeError_TV.setText(getString(R.string.errorOccur));
                progressBarOTP.setVisibility(View.GONE);
                verifyAccount.setVisibility(View.VISIBLE);

            }

        }).addOnFailureListener(e -> {
            codeError_TV.setVisibility(View.VISIBLE);
            codeError_TV.setText(getString(R.string.errorOccur));
            progressBarOTP.setVisibility(View.GONE);
        });

    }


    private void mCallbacksInitialise(){
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                loginAccount(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                cancelOTP = 1;
                Toast.makeText(NumberWithoutEmailActivity.this, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                getCodeSent = s;
                System.out.println("what is code sent : " + s);
            }
        };
    }

    private void countOTPTime()
    {
        countDownTimer = new CountDownTimer(60_000, 1_000){
            @Override
            public void onTick(long millisUntilFinished) {
                String timeLeft = String.valueOf(millisUntilFinished / 1000);
                if(cancelOTP == 0 ) sendCode_TV_.setText(timeLeft);
                else {
                    sendCode_TV_.setText(getString(R.string.resend_));
                    countDownTimer.cancel();
                    cancelOTP = 0;
                }
            }

            @Override
            public void onFinish() {
                sendCode_TV_.setText(getString(R.string.resend_));
            }
        }.start();
    }


    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if(close == 0){
                Toast.makeText(NumberWithoutEmailActivity.this, getString(R.string.pressAgain), Toast.LENGTH_SHORT).show();
                close = 1;
                new Handler().postDelayed( ()-> close = 0, 5_000);
            } else {
                startActivity(new Intent(NumberWithoutEmailActivity.this, PhoneLoginActivity.class));
                finish();
            }
        }
    };
}








