package com.pixel.chatapp.signup_login;

import static com.pixel.chatapp.home.MainActivity.nightMood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.pixel.chatapp.R;
import com.pixel.chatapp.all_utils.CountryNumCodeUtils;
import com.pixel.chatapp.all_utils.OTPGenerator;
import com.pixel.chatapp.all_utils.PhoneUtils;
import com.pixel.chatapp.api.Dao_interface.UserDao;
import com.pixel.chatapp.api.model.LoginDetailM;
import com.pixel.chatapp.api.model.ResultApiM;
import com.pixel.chatapp.api.model.UserSearchM;
import com.pixel.chatapp.constants.AllConstants;
import com.pixel.chatapp.home.MainActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LinkNumberActivity extends AppCompatActivity {

    int close = 0;

    ImageView arrowBack_IV,support_IV;
    ProgressBar progressBarLink;
    EditText number_ET, editTextCode;
    TextView openCountryCode_TV, sendCode_TV, codeError_TV, verifyOTP;

    Spinner spinnerCountryCode;
    ProgressBar progressBarNumber;

    AdapterView.OnItemSelectedListener addSpinnerListener;

    List<String> allCountryCode;
    String countryCode;

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    String getCodeSent;

    FirebaseAuth auth = FirebaseAuth.getInstance();

    CountDownTimer countDownTimer;

    int cancelOTP = 0;
    String finalCodeNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link_number);

        arrowBack_IV = findViewById(R.id.arrowBack_IV);
        support_IV = findViewById(R.id.support_IV);
        progressBarLink = findViewById(R.id.progressBarLink);
        number_ET = findViewById(R.id.number_ET);
        openCountryCode_TV = findViewById(R.id.openCountryCode_TV);
        sendCode_TV = findViewById(R.id.sendCode_TV);
        codeError_TV = findViewById(R.id.codeError_TV);
        verifyOTP = findViewById(R.id.verifyOTP);
        spinnerCountryCode = findViewById(R.id.spinnerCountryCode);
        editTextCode = findViewById(R.id.editTextCode);
        progressBarNumber = findViewById(R.id.progressBarNumber);

        mCallbacksInitialise(); // for sending otp firebase

        allCountryCode = new ArrayList<>();

        countryCode = CountryNumCodeUtils.getUserCountry(this);
        openCountryCode_TV.setText(countryCode);

        CountryNumCodeUtils.getCountryCode(countryCodes -> allCountryCode = countryCodes);

        spinnerListener();

        new Handler().postDelayed(()-> number_ET.requestFocus(), 500);

        openCountryCode_TV.setOnClickListener(v ->
        {
            if(nightMood) v.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_orange2));
            else v.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent_orange));

            new Handler().postDelayed(()-> {

                if(allCountryCode.size() == 0){
                    Toast.makeText(this,  getString(R.string.clickAgain), Toast.LENGTH_SHORT).show();
                } else {
                    if(setSpinnerCountryCOdeAdapter() < 1){
                        setSpinnerCountryCOdeAdapter();
                    }
                    spinnerCountryCode.performClick();
                    spinnerCountryCode.setOnItemSelectedListener(addSpinnerListener);
                }

                new Handler().postDelayed(()-> v.setBackgroundColor(0), 50);

            }, 1);

        });

        sendCode_TV.setOnClickListener(v -> {

            if(sendCode_TV.getText().equals(getString(R.string.resend_)) || sendCode_TV.getText().equals(getString(R.string.sendOTP)))
            {
                checkPhoneNumber();

            } else {
                Toast.makeText(this, getString(R.string.waitForTime), Toast.LENGTH_SHORT).show();
            }
        });

        verifyOTP.setOnClickListener(v ->
        {
            if(editTextCode.length() > 5){
                PhoneUtils.hideKeyboard(this, number_ET);

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(getCodeSent, editTextCode.getText().toString());

                createAccount(credential);  // verify button onClick

            } else Toast.makeText(this, getText(R.string.invalidOTP), Toast.LENGTH_SHORT).show();
        });


        arrowBack_IV.setOnClickListener(v -> {
            cancelOTP = 0;
            onBackPressed();
        });
        
        support_IV.setOnClickListener(v -> {
            Toast.makeText(this, "in progress", Toast.LENGTH_SHORT).show();
        });

    }


    //  ======  methods

    private int setSpinnerCountryCOdeAdapter(){

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, allCountryCode);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountryCode.setAdapter(adapter);

        return adapter.getCount();
    }

    private void spinnerListener(){
        // Set a listener for spinner item selection
        addSpinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Get the selected item
                String selectedItem = parentView.getItemAtPosition(position).toString();

                // Extract only the country code and phone number prefix
                int startIndex = selectedItem.indexOf("(");
                if (startIndex != -1) {
                    String countryCodeAndPrefix = selectedItem.substring(startIndex).trim();

                    openCountryCode_TV.setText(countryCodeAndPrefix);

                    String[] splitCode = countryCodeAndPrefix.split(" ");
                    countryCode = splitCode[1];

                } else {
                    Toast.makeText(LinkNumberActivity.this, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }

        };
    }

    private void mCallbacksInitialise(){
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                createAccount(phoneAuthCredential); // callback
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                cancelOTP = 1;
                Toast.makeText(LinkNumberActivity.this, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show();
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

        verifyOTP.setVisibility(View.INVISIBLE);
        progressBarLink.setVisibility(View.VISIBLE);
        codeError_TV.setVisibility(View.GONE);

        auth.signInWithCredential(credential).addOnCompleteListener(task -> {
            codeError_TV.setVisibility(View.VISIBLE);
            codeError_TV.setText(null);

            if (task.isSuccessful()) {

                loginApi();

            } else if (task.getException().getMessage().contains("expired"))
            {
                codeError_TV.setText(getString(R.string.smsCodeExpired));
                progressBarLink.setVisibility(View.GONE);
                verifyOTP.setVisibility(View.VISIBLE);

            }  else if (task.getException().getMessage().contains("SMS/TOTP is invalid"))
            {
                codeError_TV.setText(getString(R.string.invalidOTP));
                progressBarLink.setVisibility(View.GONE);
                verifyOTP.setVisibility(View.VISIBLE);

            } else if (task.getException().getMessage().contains("timeout"))
            {
                codeError_TV.setText(getString(R.string.isNetwork));
                progressBarLink.setVisibility(View.GONE);
                verifyOTP.setVisibility(View.VISIBLE);

            } else {
                codeError_TV.setText(getString(R.string.errorOccur));
                progressBarLink.setVisibility(View.GONE);
                verifyOTP.setVisibility(View.VISIBLE);

            }

        }).addOnFailureListener(e -> {
            codeError_TV.setVisibility(View.VISIBLE);
            codeError_TV.setText(getString(R.string.errorOccur));
            progressBarLink.setVisibility(View.GONE);
        });

    }

    private void loginApi() {

        UserDao userDao = AllConstants.retrofit.create(UserDao.class);

        LoginDetailM loginDetailM = new LoginDetailM(auth.getUid(), finalCodeNumber);

        userDao.login(loginDetailM).enqueue(new Callback<ResultApiM>() {
            @Override
            public void onResponse(Call<ResultApiM> call, Response<ResultApiM> response) {

                if(response.isSuccessful()){

                    close = 0;
                    onBackPressed();
                    Toast.makeText(LinkNumberActivity.this, getString(R.string.loginSuccessful), Toast.LENGTH_SHORT).show();

                } else {

                    try {
                        String error = response.errorBody().string();

                        JsonObject jsonObject = new Gson().fromJson(error, JsonObject.class);
                        String message = jsonObject.get("message").getAsString();

                        codeError_TV.setVisibility(View.VISIBLE);
                        codeError_TV.setText(message);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }

            }

            @Override
            public void onFailure(Call<ResultApiM> call, Throwable throwable) {

                verifyOTP.setVisibility(View.VISIBLE);
                progressBarLink.setVisibility(View.GONE);
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

    private void checkPhoneNumber(){

        String number = number_ET.getText().toString().replaceAll("\\s+", "");

        if(number.length() > 7){

            progressBarNumber.setVisibility(View.VISIBLE);
            sendCode_TV.setVisibility(View.INVISIBLE);
            codeError_TV.setVisibility(View.GONE);

            String countryCodeNumber = countryCode + number;

            if(number.startsWith("0")){
                countryCodeNumber = countryCode + number.substring(1);
            } else if (number.startsWith(countryCode)) {
                countryCodeNumber = number;
            }

            finalCodeNumber = countryCodeNumber;

            UserDao userDao = AllConstants.retrofit.create(UserDao.class);

            userDao.fineUser(finalCodeNumber).enqueue(new Callback<UserSearchM>() {
                @Override
                public void onResponse(Call<UserSearchM> call, Response<UserSearchM> response) {

                    if(response.isSuccessful()){    // user found

                        codeError_TV.setVisibility(View.VISIBLE);
                        codeError_TV.setText(R.string.phoneNumberExist__);

                    } else {    // user not found, go to create account page

                        try {
                            String error = response.errorBody().string();

                            JsonObject jsonObject = new Gson().fromJson(error, JsonObject.class);
                            String message = jsonObject.get("message").getAsString();

                            if(message.equals(getString(R.string.userNotFound))){

                                //send code
                                countOTPTime(); // button click send

                                OTPGenerator.sendOTPToNumber(finalCodeNumber, auth, mCallbacks, LinkNumberActivity.this);

                            } else {
                                codeError_TV.setVisibility(View.VISIBLE);
                                codeError_TV.setText(message);
                            }


                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    }

                    progressBarNumber.setVisibility(View.GONE);
                    sendCode_TV.setVisibility(View.VISIBLE);

                }

                @Override
                public void onFailure(Call<UserSearchM> call, Throwable throwable) {

                    if(throwable.getMessage().contains("Failed to connect")) {  // server error
                        codeError_TV.setText(getString(R.string.serverError));

                    } else{     // no internet connection | timeout
                        codeError_TV.setText(getString(R.string.isNetwork));
                        Toast.makeText(LinkNumberActivity.this, getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                    }

                    System.out.println("what is err : " + throwable.getMessage());
                    progressBarNumber.setVisibility(View.GONE);
                    sendCode_TV.setVisibility(View.VISIBLE);
                    codeError_TV.setVisibility(View.VISIBLE);
                }
            });

        }
        else Toast.makeText(this, getString(R.string.phoneError), Toast.LENGTH_SHORT).show();

    }

    private void countOTPTime()
    {
        countDownTimer = new CountDownTimer(60_000, 1_000){
            @Override
            public void onTick(long millisUntilFinished) {
                String timeLeft = String.valueOf(millisUntilFinished / 1000);
                if(cancelOTP == 0 ) sendCode_TV.setText(timeLeft);
                else {
                    sendCode_TV.setText(getString(R.string.resend_));
                    countDownTimer.cancel();
                    cancelOTP = 0;
                }
            }

            @Override
            public void onFinish() {
                sendCode_TV.setText(getString(R.string.resend_));
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
}