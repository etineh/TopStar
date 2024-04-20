package com.pixel.chatapp.signup_login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.pixel.chatapp.R;
import com.pixel.chatapp.all_utils.CountryNumCodeUtils;
import com.pixel.chatapp.all_utils.PhoneUtils;
import com.pixel.chatapp.api.Dao_interface.UserDao;
import com.pixel.chatapp.constants.AllConstants;
import com.pixel.chatapp.api.model.UserSearchM;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhoneLoginActivity extends AppCompatActivity {


    ImageView supportLogin, closePage;
    EditText number_ET;
    TextView openCountryCode_TV, errorInfo_TV;
    Spinner spinnerCountryCode;
    Button proceedButton;

    List<String> allCountryCode;
    String countryCode;
    ProgressBar progressBarPhoneLogin;

    int close = 0;
    AdapterView.OnItemSelectedListener addSpinnerListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);


        supportLogin = findViewById(R.id.supportLogin);
        closePage = findViewById(R.id.closePage);
        number_ET = findViewById(R.id.number_ET);
        openCountryCode_TV = findViewById(R.id.openCountryCode_TV);
        spinnerCountryCode = findViewById(R.id.spinnerCountryCode);
        proceedButton = findViewById(R.id.proceedButton);
        supportLogin = findViewById(R.id.supportLogin);
        supportLogin = findViewById(R.id.supportLogin);
        supportLogin = findViewById(R.id.supportLogin);
        progressBarPhoneLogin = findViewById(R.id.progressBarPhoneLogin);
        errorInfo_TV = findViewById(R.id.errorInfo_TV);

        allCountryCode = new ArrayList<>();

        countryCode = CountryNumCodeUtils.getUserCountry(this);
        openCountryCode_TV.setText(countryCode);

        CountryNumCodeUtils.getCountryCode(countryCodes -> allCountryCode = countryCodes);

        spinnerListener();

        new Handler().postDelayed(()-> number_ET.requestFocus(), 500);

        openCountryCode_TV.setOnClickListener(v -> {

            v.setBackgroundColor(ContextCompat.getColor(this, R.color.cool_orange));
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(10).withEndAction(()->{

                if(allCountryCode.size() == 0){
                    Toast.makeText(this,  getString(R.string.clickAgain), Toast.LENGTH_SHORT).show();
                } else {
                    if(setSpinnerCountryCOdeAdapter() < 1){
                        setSpinnerCountryCOdeAdapter();
                    }
                    spinnerCountryCode.performClick();
                    spinnerCountryCode.setOnItemSelectedListener(addSpinnerListener);
                }

                v.setBackgroundColor(0);

            });

        });


        supportLogin.setOnClickListener(v -> {
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(20).withEndAction(()->{

                Toast.makeText(this,  getString(R.string.inProgress), Toast.LENGTH_SHORT).show();

            });
        });


        proceedButton.setOnClickListener(v -> {

            PhoneUtils.hideKeyboard(this, number_ET);

            checkPhoneNumber();
//            Intent intent = new Intent(PhoneLoginActivity.this, CreateAccountActivity.class);
//            intent.putExtra("number", number_ET.getText().toString());
//            startActivity(intent);
//            finish();
        });

        closePage.setOnClickListener(v -> onBackPressed());

    }

    //  ========    methods     ===========

    private void checkPhoneNumber(){

        String number = number_ET.getText().toString().replaceAll("\\s+", "");

        if(number.length() > 7){

            progressBarPhoneLogin.setVisibility(View.VISIBLE);
            proceedButton.setVisibility(View.INVISIBLE);
            errorInfo_TV.setVisibility(View.GONE);

            String countryCodeNumber = countryCode + number;

            if(number.startsWith("0")){
                countryCodeNumber = countryCode + number.substring(1);
            } else if (number.startsWith(countryCode)) {
                countryCodeNumber = number;
            }

            String finalCodeNumber = countryCodeNumber;

            UserDao userDao = AllConstants.retrofit.create(UserDao.class);

            userDao.fineUser(finalCodeNumber).enqueue(new Callback<UserSearchM>() {
                @Override
                public void onResponse(Call<UserSearchM> call, Response<UserSearchM> response) {

                    if(response.isSuccessful()){    // user found

                        UserSearchM userSearchM = response.body();
                        String email = userSearchM.getEmail();
                        String username = userSearchM.getUsername();

                        if(email != null)
                        {
                            Intent intent = new Intent(PhoneLoginActivity.this, PasswordActivity.class);
                            intent.putExtra("email", email);
                            intent.putExtra("username", username);
                            startActivity(intent);  // number exist, send user to password page
                            finish();

                        } else {
                            Intent intent = new Intent(PhoneLoginActivity.this, NumberWithoutEmailActivity.class);
                            intent.putExtra("number", finalCodeNumber);
                            startActivity(intent);  // number exist, send user to password page
                            finish();
                        }

                    } else {    // user not found, go to create account page

                        try {
                            String error = response.errorBody().string();

                            JsonObject jsonObject = new Gson().fromJson(error, JsonObject.class);
                            String message = jsonObject.get("message").getAsString();

                            if(message.equals(getString(R.string.userNotFound))){
                                Intent intent = new Intent(PhoneLoginActivity.this, CreateAccountActivity.class);
                                intent.putExtra("number", finalCodeNumber);
                                startActivity(intent);
                                finish();
                            } else {
                                errorInfo_TV.setVisibility(View.VISIBLE);
                                errorInfo_TV.setText(message);
                            }


                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    }

                    progressBarPhoneLogin.setVisibility(View.GONE);
                    proceedButton.setVisibility(View.VISIBLE);

                }

                @Override
                public void onFailure(Call<UserSearchM> call, Throwable throwable) {

                    if(throwable.getMessage().contains("Failed to connect")) {  // server error
                        errorInfo_TV.setText(getString(R.string.serverError));
                        Toast.makeText(PhoneLoginActivity.this, getString(R.string.serverError), Toast.LENGTH_SHORT).show();

                    } else{     // no internet connection | timeout
                        errorInfo_TV.setText(getString(R.string.isNetwork));
                        Toast.makeText(PhoneLoginActivity.this, getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                    }

                    System.out.println("what is err : " + throwable.getMessage());
                    errorInfo_TV.setVisibility(View.VISIBLE);
                    progressBarPhoneLogin.setVisibility(View.GONE);
                    proceedButton.setVisibility(View.VISIBLE);
                }
            });

        }
        else {
            errorInfo_TV.setVisibility(View.VISIBLE);
            errorInfo_TV.setText(getString(R.string.phoneError));
            Toast.makeText(this, getString(R.string.phoneError), Toast.LENGTH_SHORT).show();
        }

    }

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
                    Toast.makeText(PhoneLoginActivity.this, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }

        };
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
        spinnerCountryCode.setOnItemSelectedListener(null);
    }
}













