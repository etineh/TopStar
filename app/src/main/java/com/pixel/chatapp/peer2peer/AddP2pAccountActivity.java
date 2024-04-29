package com.pixel.chatapp.peer2peer;

import static com.pixel.chatapp.home.MainActivity.nightMood;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.pixel.chatapp.R;
import com.pixel.chatapp.all_utils.BankNamesUtils;
import com.pixel.chatapp.all_utils.CountryNumCodeUtils;

import java.util.ArrayList;
import java.util.List;

public class AddP2pAccountActivity extends AppCompatActivity {


    TextView legalNameTV, sendButton, countryCodeTV, selectedBankTV;
    EditText accountNo_ET, pinET, branchET;
    ImageView cancelButton;
    ProgressBar progressBar5;
    ConstraintLayout bankContainer;
    Spinner spinnerBank, spinnerCountryCode_;
    String countryCode;
    List<String> bankList, allCountryCode;

    AdapterView.OnItemSelectedListener addSpinnerBankListener, addSpinnerCountryListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_p2p_account);

        legalNameTV = findViewById(R.id.legalNameTV);
        cancelButton = findViewById(R.id.cancleTransferButton);
        selectedBankTV = findViewById(R.id.selectedBankTV);
        accountNo_ET = findViewById(R.id.accountNo_ET);
        pinET = findViewById(R.id.pinET);
        branchET = findViewById(R.id.branchET);
        sendButton = findViewById(R.id.sendButton);
        progressBar5 = findViewById(R.id.progressBar5);
        countryCodeTV = findViewById(R.id.countryCodeTV);
        spinnerBank = findViewById(R.id.spinnerBanks);
        spinnerCountryCode_ = findViewById(R.id.spinnerCountryCode_);
        bankContainer = findViewById(R.id.bankContainer);


        bankList = new ArrayList<>();

        allCountryCode = new ArrayList<>();

        CountryNumCodeUtils.getCountryCode(countryCodes -> allCountryCode = countryCodes);

        spinnerListener();

        countryCode = CountryNumCodeUtils.getUserCountry(this);
        countryCodeTV.setText(countryCode);

        bankList = BankNamesUtils.banksList(this, null);


        countryCodeTV.setOnClickListener(v ->
        {
            if(nightMood) v.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_orange2));
            else v.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent_orange));

            new Handler().postDelayed(()-> {

                if(allCountryCode.size() == 0){
                    Toast.makeText(this,  getString(R.string.clickAgain), Toast.LENGTH_SHORT).show();
                } else {
                    if(setSpinnerCountryCodeAdapter() < 1){
                        setSpinnerCountryCodeAdapter();
                    }
                    spinnerCountryCode_.performClick();
                    spinnerCountryCode_.setOnItemSelectedListener(addSpinnerCountryListener);
                }

                new Handler().postDelayed(()-> v.setBackgroundColor(0), 50);

            }, 1);

        });


        bankContainer.setOnClickListener(v ->
        {
            if(bankList != null)
            {
                if(bankList.size() == 0){
                    Toast.makeText(this,  getString(R.string.clickAgain), Toast.LENGTH_SHORT).show();
                } else {
                    v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(20).withEndAction(()->
                    {
                        if(setSpinnerBanksAdapter() < 1){
                            setSpinnerBanksAdapter();
                        }
                        spinnerBank.performClick();
                        spinnerBank.setOnItemSelectedListener(addSpinnerBankListener);

                        v.setScaleX(1.0f);
                        v.setScaleY(1.0f);

                    }).start();
                }
            }

        });


        sendButton.setOnClickListener(v ->
        {
            sendButton.setVisibility(View.INVISIBLE);
            progressBar5.setVisibility(View.VISIBLE);

//            System.out.println("what is list " + BankNamesUtils.banksList(this));
        });

        cancelButton.setOnClickListener(v -> onBackPressed());


    }

    private void spinnerListener(){
        // Set a listener for spinner item selection
        addSpinnerCountryListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Get the selected item
                String selectedItem = parentView.getItemAtPosition(position).toString();

                // Extract only the country code and phone number prefix
                int startIndex = selectedItem.indexOf("(");
                if (startIndex != -1) {
                    String countryCodeAndPrefix = selectedItem.substring(startIndex).trim();

                    countryCodeTV.setText(countryCodeAndPrefix);    // (NG) +234

                    if(bankList != null) bankList.clear();
                    bankList = BankNamesUtils.banksList(AddP2pAccountActivity.this, countryCodeAndPrefix);

//                    String[] splitCode = countryCodeAndPrefix.split(" ");
//                    countryCode = splitCode[1];

                } else {
                    Toast.makeText(AddP2pAccountActivity.this, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }

        };


        addSpinnerBankListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Get the selected item
                String selectedItem = parentView.getItemAtPosition(position).toString();

                selectedBankTV.setText(selectedItem);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }

        };

    }

    private int setSpinnerCountryCodeAdapter(){

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, allCountryCode);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountryCode_.setAdapter(adapter);

        return adapter.getCount();
    }

    private int setSpinnerBanksAdapter(){

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bankList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBank.setAdapter(adapter);

        return adapter.getCount();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}












