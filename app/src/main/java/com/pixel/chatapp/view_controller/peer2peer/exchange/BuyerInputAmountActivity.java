package com.pixel.chatapp.view_controller.peer2peer.exchange;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pixel.chatapp.R;
import com.pixel.chatapp.utilities.PhoneUtils;

public class BuyerInputAmountActivity extends AppCompatActivity {

    ImageView cancleP2PBuyButton, refresh_IV;
    EditText enterAmount_ET;
    Spinner choosePaymentMethod_Spinner;
    TextView traderFee_TV, amountRange_TV, allAmount_TV, amountToBeCredited_TV, traderRemark_TV, proceedButton;
    CheckBox checkBoxTerms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_input_amount);

        enterAmount_ET = findViewById(R.id.enterAmount_ET);
        choosePaymentMethod_Spinner = findViewById(R.id.choosePaymentMethod_Spinner);
        traderFee_TV = findViewById(R.id.traderFee_TV);
        amountRange_TV = findViewById(R.id.amountRange_TV);
        allAmount_TV = findViewById(R.id.allAmount_TV);
        amountToBeCredited_TV = findViewById(R.id.amountToBeCredited_TV);
        checkBoxTerms = findViewById(R.id.checkBoxTerms);
        traderRemark_TV = findViewById(R.id.traderRemark_TV);
        proceedButton = findViewById(R.id.proceedButton);
        cancleP2PBuyButton = findViewById(R.id.cancleP2PBuyButton);
        refresh_IV = findViewById(R.id.refresh_IV);
        // spinner listener

        // all listener
        allAmount_TV.setOnClickListener(v -> {
            Toast.makeText(this, "Work in progress", Toast.LENGTH_SHORT).show();
        });

        // proceed to payment info
        proceedButton.setOnClickListener(v -> {
            if(checkBoxTerms.isChecked()){
                PhoneUtils.hideKeyboard(this, enterAmount_ET);

                Intent intent = new Intent(this, BuyerPaymentInfoActivity.class);

                startActivity(intent);

                finish();
            } else {
                Toast.makeText(this, getString(R.string.termNotAccepted), Toast.LENGTH_SHORT).show();
            }

        });

        refresh_IV.setOnClickListener(v -> {
            Toast.makeText(this, "work in process", Toast.LENGTH_SHORT).show();
        });


        cancleP2PBuyButton.setOnClickListener(v -> onBackPressed());

    }


    //   ====   methods ===================

    private void spinnerListener(){
        choosePaymentMethod_Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                // Do something with the selected item
//                Toast.makeText(getApplicationContext(), "Selected spiner: " + selectedItem, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case where nothing is selected (optional)
            }
        });

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}













