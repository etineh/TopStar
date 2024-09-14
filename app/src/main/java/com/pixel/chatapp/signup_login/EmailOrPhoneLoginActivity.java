package com.pixel.chatapp.signup_login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
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

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.pixel.chatapp.R;
import com.pixel.chatapp.utils.CountryNumCodeUtils;
import com.pixel.chatapp.utils.PhoneUtils;
import com.pixel.chatapp.api.Dao_interface.UserDao;
import com.pixel.chatapp.api.model.incoming.UserSearchM;
import com.pixel.chatapp.constants.AllConstants;
import com.pixel.chatapp.home.MainActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmailOrPhoneLoginActivity extends AppCompatActivity {

    private TextInputEditText editTextPassword;
    private EditText emailOrNumber_ET;
    private Button buttonSignIn;
    private TextView textViewForgetPassword, buttonSignUp;

    TextView emailOption_TV, phoneOption_TV, markEmail_TV, markPhone_TV;

    TextView openCountryCode_Click;
    ImageView arrowCountryCode, closePage_IV;
    Spinner spinnerCountryCode;
    ProgressBar progressBarLogin;
    String countryCode;

    int close = 0;
    FirebaseAuth auth;
    FirebaseUser user;

    List<String> countryCodeList;

    AdapterView.OnItemSelectedListener addSpinnerListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_phone_login);

        emailOrNumber_ET = findViewById(R.id.emailOrNumber_ET);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSignIn = findViewById(R.id.buttonSignIn);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        textViewForgetPassword = findViewById(R.id.textViewForgetPassword);

        emailOption_TV = findViewById(R.id.emailOption_TV);
        phoneOption_TV = findViewById(R.id.phoneOption_TV);
        markEmail_TV = findViewById(R.id.markEmail_TV);
        markPhone_TV = findViewById(R.id.markPhone_TV);
        progressBarLogin = findViewById(R.id.progressBarLogin);
        closePage_IV = findViewById(R.id.closePage_IV);

        openCountryCode_Click = findViewById(R.id.openCountryCode_Click);
        arrowCountryCode = findViewById(R.id.arrowCountryCode);
        spinnerCountryCode = findViewById(R.id.spinnerCountryCode);
        auth = FirebaseAuth.getInstance();

        countryCodeList = new ArrayList<>();

        countryCode = CountryNumCodeUtils.getUserCountry(this);
        openCountryCode_Click.setText(countryCode);

        CountryNumCodeUtils.getCountryCode(countryCodes -> countryCodeList = countryCodes);
        spinnerListener();

        new Handler().postDelayed(()-> emailOrNumber_ET.requestFocus(), 2000);

        emailOption_TV.setOnClickListener(v -> {
            markEmail_TV.setVisibility(View.VISIBLE);
            markPhone_TV.setVisibility(View.GONE);
            openCountryCode_Click.setVisibility(View.GONE);
            arrowCountryCode.setVisibility(View.GONE);
            emailOrNumber_ET.setHint(getString(R.string.email));
            emailOrNumber_ET.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            emailOrNumber_ET.setText(null);

        });

        phoneOption_TV.setOnClickListener(v -> {
            markEmail_TV.setVisibility(View.GONE);
            markPhone_TV.setVisibility(View.VISIBLE);

            openCountryCode_Click.setVisibility(View.VISIBLE);
            arrowCountryCode.setVisibility(View.VISIBLE);
            emailOrNumber_ET.setHint(getString(R.string.number));
            emailOrNumber_ET.setInputType(InputType.TYPE_CLASS_PHONE);

            emailOrNumber_ET.setText(null);

        });

        openCountryCode_Click.setOnClickListener(v -> {

            v.setBackgroundColor(ContextCompat.getColor(this, R.color.cool_orange));
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(10).withEndAction(()->{

                if(countryCodeList.size() == 0){
                    Toast.makeText(this,  getString(R.string.clickAgain), Toast.LENGTH_SHORT).show();
                } else {
                    if(countryCodeMethod() < 1){
                        countryCodeMethod();
                    }
                    spinnerCountryCode.performClick();
                    spinnerCountryCode.setOnItemSelectedListener(addSpinnerListener);
                }

                v.setBackgroundColor(0);

            });

        });


        buttonSignIn.setOnClickListener(view -> {

            PhoneUtils.hideKeyboard(this, editTextPassword);

            String valueEmailOrNumber = emailOrNumber_ET.getText().toString().replaceAll("\\s+", "");
            String password = editTextPassword.getText().toString();

            if(openCountryCode_Click.getVisibility() == View.GONE){

                if (valueEmailOrNumber.contains("@") && valueEmailOrNumber.contains("."))
                {
                    progressBarLogin.setVisibility(View.VISIBLE);
                    buttonSignIn.setVisibility(View.INVISIBLE);

                    signInWithEmail(valueEmailOrNumber, password);

                } else Toast.makeText(this, getString(R.string.emailError), Toast.LENGTH_SHORT).show();

            } else {    // it is number

                if(!countryCode.startsWith("+")){   // (NG) +234
                    String[] splitCode = countryCode.split(" ");
                    countryCode = splitCode[1];
                }

                String finalCodeNumber = countryCode + valueEmailOrNumber;

                if(valueEmailOrNumber.startsWith("0"))
                {
                    finalCodeNumber = countryCode + valueEmailOrNumber.substring(1);
                } else if (valueEmailOrNumber.startsWith(countryCode))
                {
                    finalCodeNumber = valueEmailOrNumber;
                }

                if(password.length() > 3){
                    progressBarLogin.setVisibility(View.VISIBLE);
                    buttonSignIn.setVisibility(View.INVISIBLE);

                    signInWithPhoneNumber(finalCodeNumber, password);

                } else Toast.makeText(this, getString(R.string.invalidPassword), Toast.LENGTH_SHORT).show();

            }

        });

        buttonSignUp.setOnClickListener(view -> {
            startActivity(new Intent(EmailOrPhoneLoginActivity.this, PhoneLoginActivity.class));
            finish();
        });

        textViewForgetPassword.setOnClickListener(view -> {
            startActivity(new Intent(EmailOrPhoneLoginActivity.this, ForgetActivity.class));
        });

        closePage_IV.setOnClickListener(v -> onBackPressed());

    }


    // -----------------------  methods  ---------------------

    private int countryCodeMethod(){

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, countryCodeList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountryCode.setAdapter(adapter);

        return adapter.getCount();
    }

    private void spinnerListener() {

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

                    openCountryCode_Click.setText(countryCodeAndPrefix);

                    String[] splitCode = countryCodeAndPrefix.split(" ");
                    countryCode = splitCode[1];

                } else {
                    Toast.makeText(EmailOrPhoneLoginActivity.this, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        };

    }


    // sign in method
    public void signInWithEmail(String userEmail, String userPassword){
        auth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(task -> {

            if(task.isSuccessful()){
                startActivity(new Intent(EmailOrPhoneLoginActivity.this, MainActivity.class));
                Toast.makeText(EmailOrPhoneLoginActivity.this, getString(R.string.loginSuccessful), Toast.LENGTH_SHORT).show();
                finish();
            } else if (task.getException().getMessage().contains("invalid"))
            {
                Toast.makeText(this, getString(R.string.invalidDetails), Toast.LENGTH_SHORT).show();
                progressBarLogin.setVisibility(View.GONE);
                buttonSignIn.setVisibility(View.VISIBLE);

            } else if(task.getException().getMessage().startsWith("We have blocked"))
            {
                Toast.makeText(this, getString(R.string.tooManyAttempt), Toast.LENGTH_SHORT).show();
                progressBarLogin.setVisibility(View.GONE);
                buttonSignIn.setVisibility(View.VISIBLE);
            }else if (task.getException().getMessage().contains("timeout"))
            {
                Toast.makeText(this, getString(R.string.isNetwork), Toast.LENGTH_SHORT).show();
                progressBarLogin.setVisibility(View.GONE);
                buttonSignIn.setVisibility(View.VISIBLE);

            } else{
                Toast.makeText(EmailOrPhoneLoginActivity.this, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show();
                progressBarLogin.setVisibility(View.GONE);
                buttonSignIn.setVisibility(View.VISIBLE);
            }

        });
    }

    private void signInWithPhoneNumber(String number, String userPassword){

        UserDao userDao = AllConstants.retrofit.create(UserDao.class);

        userDao.fineUser(number).enqueue(new Callback<UserSearchM>() {
            @Override
            public void onResponse(Call<UserSearchM> call, Response<UserSearchM> response) {

                if(response.isSuccessful()){
                    UserSearchM userSearchM = response.body();
                    String email = userSearchM.getEmail();

                    if(email != null) {
                        signInWithEmail(email, userPassword);
                    } else {
                        Intent intent = new Intent(EmailOrPhoneLoginActivity.this, NumberWithoutEmailActivity.class);
                        intent.putExtra("number", number);
                        startActivity(intent);  // number exist, send user to password page
                        finish();
                    }

                } else {
                    try {
                        progressBarLogin.setVisibility(View.GONE);
                        buttonSignIn.setVisibility(View.VISIBLE);

                        String error = response.errorBody().string();

                        JsonObject jsonObject = new Gson().fromJson(error, JsonObject.class);
                        String message = jsonObject.get("message").getAsString();

                        if(message.equals(getString(R.string.userNotFound))) {
                            Toast.makeText(EmailOrPhoneLoginActivity.this, getString(R.string.userNotFound), Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(EmailOrPhoneLoginActivity.this, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show();

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

            }

            @Override
            public void onFailure(Call<UserSearchM> call, Throwable throwable) {

                progressBarLogin.setVisibility(View.GONE);
                buttonSignIn.setVisibility(View.VISIBLE);

                if(throwable.getMessage().contains("Failed to connect")) {  // server error
                    Toast.makeText(EmailOrPhoneLoginActivity.this, getString(R.string.serverError), Toast.LENGTH_SHORT).show();

                } else{     // no internet connection | timeout
                    Toast.makeText(EmailOrPhoneLoginActivity.this, getString(R.string.isNetwork), Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        user = FirebaseAuth.getInstance().getCurrentUser();
//        if(user != null){
//            startActivity(new Intent(EmailOrPhoneLoginActivity.this, MainActivity.class));
//        }
//
//    }

    @Override
    protected void onPause() {
        super.onPause();
        spinnerCountryCode.setOnItemSelectedListener(null);
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














