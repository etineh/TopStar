package com.pixel.chatapp.activities;

import static com.pixel.chatapp.all_utils.NumberSpacing.formatPhoneNumber;
import static com.pixel.chatapp.home.MainActivity.resetLoginSharePref;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.pixel.chatapp.R;
import com.pixel.chatapp.all_utils.IdTokenUtil;
import com.pixel.chatapp.all_utils.OTPGenerator;
import com.pixel.chatapp.all_utils.PhoneUtils;
import com.pixel.chatapp.api.Dao_interface.ProfileApiDao;
import com.pixel.chatapp.api.model.incoming.ResultApiM;
import com.pixel.chatapp.api.model.ThreeValueM;
import com.pixel.chatapp.constants.AllConstants;
import com.pixel.chatapp.signup_login.EmailOrPhoneLoginActivity;
import com.pixel.chatapp.signup_login.ResetAccountActivity;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OTPActivity extends AppCompatActivity{

    TextView sendOTP_TV, otpHint_TV, done;
    EditText getOTP_ET;

    // Get instance of FirebaseAuth
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();

    ProgressBar progressBarOTP;

    int close = 0;
    TextView otpStatus_TV;
    public static int cancleOTP = 0;

    String type, token, pass, previousData, newData;
    CountDownTimer countDownTimer;

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    String getCodeSent;


    // create a interface listener
    public interface UpdateFieldListener{
        void updateEmail(String value);
        void updatePhoneNumber(String value);

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
        otpStatus_TV = findViewById(R.id.otpStatus_TV);
        TextView verifyTopInfo = findViewById(R.id.verifyDetails_TV);

        mCallbacksInitialise();
        
        type = getIntent().getStringExtra("type");
        token = getIntent().getStringExtra("token");
        newData = getIntent().getStringExtra("newData");
        previousData = getIntent().getStringExtra("previousData");
        pass = getIntent().getStringExtra("pass");

        String topDataDisplay = getString(R.string.verify) + "\n" + newData;

        if(type.equals("email") || type.equals("reset"))
        {
            otpHint_TV.setText(getString(R.string.enterOTPEmail));
            
            OTPGenerator.generateOtpEmail(token, newData, this, sendOTP_TV, otpStatus_TV);

        } else  if(type.equals("phoneNumber"))
        {
            topDataDisplay =  getString(R.string.verify) + "\n" + formatPhoneNumber(newData, 3, 3);

            otpHint_TV.setText(getString(R.string.enterOTPNumber));

            OTPGenerator.sendOTPToNumber(newData, auth, mCallbacks, this);

            new Handler().postDelayed(()-> otpStatus_TV.setText(getString(R.string.otpSent)),20_000);
        }

        verifyTopInfo.setText(topDataDisplay);

        new Handler().postDelayed(() -> getOTP_ET.requestFocus(), 500);

        sendOTP_TV.setOnClickListener(v -> {
            if(sendOTP_TV.getText() == getString(R.string.resend_)){
                otpStatus_TV.setText(getString(R.string.sendingOtp));
                countOTPTime(); // button click send
                
                if(type.equals("email") || type.equals("reset"))
                {
                    OTPGenerator.generateOtpEmail(token, newData, this, sendOTP_TV, otpStatus_TV);

                } else{
                    OTPGenerator.sendOTPToNumber(newData, auth, mCallbacks, this);
                }
                
            } else {
                Toast.makeText(this, getString(R.string.waitForTime), Toast.LENGTH_SHORT).show();
            }

        });


        closeOTPpage.setOnClickListener(v -> onBackPressed());

        done.setOnClickListener(v -> {

            if(getOTP_ET.length() > 5)
            {
                PhoneUtils.hideKeyboard(this, getOTP_ET);
                String otp = getOTP_ET.getText().toString();

                // Save to database later
                if(type.equals("phoneNumber")){

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(getCodeSent, otp);

                    verifyAndUpdatePhoneNumber(credential);

                } else if (type.equals("email") || type.equals("reset")){

                    progressBarOTP.setVisibility(View.VISIBLE);
                    OTPGenerator.verifyEmailOtp(token, otp, this, otpStatus_TV, progressBarOTP,
                            this::updateEmail);
                }
                
            } else Toast.makeText(this, getString(R.string.invalidOTP), Toast.LENGTH_SHORT).show();

        });

        countOTPTime();

    }

    //  ==========  method  ==========

    private void mCallbacksInitialise(){
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                verifyAndUpdatePhoneNumber(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                cancleOTP = 1;
                otpStatus_TV.setText(getString(R.string.errorWithOtp));
                Toast.makeText(OTPActivity.this, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                getCodeSent = s;
                System.out.println("what is code sent : " + s);
            }
        };
    }

    private void verifyAndUpdatePhoneNumber(PhoneAuthCredential credential){

        if (user != null) {
            progressBarOTP.setVisibility(View.VISIBLE);

            user.updatePhoneNumber(credential).addOnCompleteListener(task -> {

                if (task.isSuccessful()) {

                    auth.signInWithCredential(credential).addOnCompleteListener(task1 -> updateApiNumber() );

                } else if(task.getException().getMessage().toString().contains("User has already been linked"))
                {
                    Toast.makeText(this, getString(R.string.numberExist), Toast.LENGTH_SHORT).show();
                    otpStatus_TV.setText(getString(R.string.numberExist));
                    progressBarOTP.setVisibility(View.GONE);

                } else if (task.getException().getMessage().contains("expired"))
                {
                    Toast.makeText(this, getString(R.string.smsCodeExpired), Toast.LENGTH_SHORT).show();
                    otpStatus_TV.setText(getString(R.string.smsCodeExpired));
                    progressBarOTP.setVisibility(View.GONE);

                } else if (task.getException().getMessage().contains("different user"))
                {
                    Toast.makeText(this, getString(R.string.anotherUserNumber), Toast.LENGTH_SHORT).show();
                    otpStatus_TV.setText(getString(R.string.anotherUserNumber));
                    progressBarOTP.setVisibility(View.GONE);

                } else if (task.getException().getMessage().contains("SMS/TOTP is invalid"))
                {
                    Toast.makeText(this, getString(R.string.invalidOTP), Toast.LENGTH_SHORT).show();
                    otpStatus_TV.setText(getString(R.string.invalidOTP));
                    progressBarOTP.setVisibility(View.GONE);

                } else if (task.getException().getMessage().contains("timeout"))
                {
                    Toast.makeText(this, getString(R.string.isNetwork), Toast.LENGTH_SHORT).show();
                    otpStatus_TV.setText(getString(R.string.isNetwork));
                    progressBarOTP.setVisibility(View.GONE);

                }  else {
                    Toast.makeText(this, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show();
                    otpStatus_TV.setText(getString(R.string.errorOccur));
                    progressBarOTP.setVisibility(View.GONE);
                }

            });

        } else Toast.makeText(this, getString(R.string.userNotFound), Toast.LENGTH_SHORT).show();

    }

    private void updateApiNumber(){

        IdTokenUtil.generateToken(token->{

            ProfileApiDao profileApiDao = AllConstants.retrofit.create(ProfileApiDao.class);
            ThreeValueM valueM = new ThreeValueM(token, previousData, newData);

            profileApiDao.number(valueM).enqueue(new Callback<ResultApiM>() {
                @Override
                public void onResponse(Call<ResultApiM> call, Response<ResultApiM> response)
                {
                    if(response.isSuccessful()){

                        if(response.body().getResult().equals("success"))
                        {
                            updateFieldListener.updatePhoneNumber(newData);
                            Toast.makeText(OTPActivity.this, getString(R.string.phoneNumberUpdated), Toast.LENGTH_SHORT).show();
                            finish();

                        } else {
                            progressBarOTP.setVisibility(View.GONE);
                            Toast.makeText(OTPActivity.this, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        progressBarOTP.setVisibility(View.GONE);
                        Toast.makeText(OTPActivity.this, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailure(Call<ResultApiM> call, Throwable throwable) {
                    progressBarOTP.setVisibility(View.GONE);
                    if(throwable.getMessage().contains("Failed to connect")) {  // server error
                        Toast.makeText(OTPActivity.this, getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                    } else{     // no internet connection | timeout
                        Toast.makeText(OTPActivity.this, getString(R.string.isNetwork), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        });
    }


    private void unlinkAndLink(PhoneAuthCredential credential){
        if (user != null) {
            user.unlink(PhoneAuthProvider.PROVIDER_ID) // Unlink phone number
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Link the new Phone number successfully unlinked
                            linkNewNumber(credential);
                        } else {
                            // Unlinking phone number failed, handle the error
                            System.out.println("what is - Failed to change phone number: " + task.getException().getMessage());
                            Toast.makeText(OTPActivity.this, "Failed to change phone number: " +
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void linkNewNumber(PhoneAuthCredential credential){
        if (user != null) {
            user.linkWithCredential(credential)
                    .addOnCompleteListener(this, task -> {

                        if (task.isSuccessful()) {
                            // send to database via api later
                            updateFieldListener.updatePhoneNumber(newData);
                            Toast.makeText(OTPActivity.this, getString(R.string.phoneNumberUpdated), Toast.LENGTH_SHORT).show();
                            finish();

                        } else if (task.getException().getMessage().contains("different user")) {
                            Toast.makeText(this, getString(R.string.anotherUserNumber), Toast.LENGTH_SHORT).show();
                            otpStatus_TV.setText(getString(R.string.anotherUserNumber));
                        } else {
                            Toast.makeText(this, getString(R.string.invalidOTP), Toast.LENGTH_SHORT).show();
                            otpStatus_TV.setText(getString(R.string.invalidOTP));
                        }
                        System.out.println("what is fail : OTPA L219 " + task.getException().getMessage());
                        progressBarOTP.setVisibility(View.GONE);

                    });
        }
    }
    
    public void updateEmail(){
        ProfileApiDao profileApiDao = AllConstants.retrofit.create(ProfileApiDao.class);

        ThreeValueM valueM = new ThreeValueM(token, newData, pass);

        // Make POST request
        Call<Void> call = profileApiDao.updateEmail(valueM);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.isSuccessful()){

                    if(type.equals("email"))
                    {
                        auth.signInWithEmailAndPassword(newData, pass);
                        updateFieldListener.updateEmail(newData);

                        Toast.makeText(OTPActivity.this, getString(R.string.emailNumberUpdated), Toast.LENGTH_SHORT).show();

                    } else if (type.equals("reset"))
                    {
                        // clear share preference
                        resetLoginSharePref.edit().remove(auth.getUid()).apply();
                        Toast.makeText(OTPActivity.this, getString(R.string.loginReset), Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(OTPActivity.this, EmailOrPhoneLoginActivity.class));

                    }

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

    private void countOTPTime(){

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
            if (type.equals("reset"))
            {
                startActivity(new Intent(this, ResetAccountActivity.class));

            } else {
                super.onBackPressed();
            }

            finish();
        }
    }

}











