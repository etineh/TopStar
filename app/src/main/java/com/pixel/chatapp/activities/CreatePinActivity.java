package com.pixel.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pixel.chatapp.R;
import com.pixel.chatapp.utils.PhoneUtils;
import com.pixel.chatapp.side_bar_menu.wallet.WalletActivity;

public class CreatePinActivity extends AppCompatActivity {

    ImageView cancelCreatePin_IV;
    EditText new_pin_et, confirmNewPin_ET;
    CheckBox checkBoxTerms_;
    TextView createPinButton, pinError_TV;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pin);

        cancelCreatePin_IV = findViewById(R.id.cancelCreatePin_IV);
        new_pin_et = findViewById(R.id.new_pin_et);
        confirmNewPin_ET = findViewById(R.id.confirmNewPin_ET);
        checkBoxTerms_ = findViewById(R.id.checkBoxTerms_);
        createPinButton = findViewById(R.id.createPinButton);
        pinError_TV = findViewById(R.id.pinError_TV);


        createPinButton.setOnClickListener(v -> {

            if(new_pin_et.length() == 4 ) {

                if(new_pin_et.getText().toString().equals(confirmNewPin_ET.getText().toString()))
                {
                    if(checkBoxTerms_.isChecked()){
                        Toast.makeText(this, getString(R.string.pin_create), Toast.LENGTH_SHORT).show();
                        PhoneUtils.hideKeyboard(this, confirmNewPin_ET);

                        v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(10).withEndAction(() ->
                        {
                            Intent intent = new Intent(this, WalletActivity.class);
                            startActivity(intent);

                            // Reset the scale
                            new Handler().postDelayed(()-> {
                                v.setScaleX(1.0f);
                                v.setScaleY(1.0f);
                                finish();
                            }, 200);

                        }).start();

                    } else {
                        pinError_TV.setVisibility(View.VISIBLE);
                        pinError_TV.setText(getString(R.string.tick_box_fingerprint));
                        Toast.makeText(this, getString(R.string.tick_box_fingerprint), Toast.LENGTH_SHORT).show();
                    }

                } else
                {
                    pinError_TV.setVisibility(View.VISIBLE);
                    pinError_TV.setText(getString(R.string.pin_not_match));
                    Toast.makeText(this, R.string.pin_not_match, Toast.LENGTH_SHORT).show();
                }

            } else {
                pinError_TV.setVisibility(View.VISIBLE);
                pinError_TV.setText(getString(R.string.pin_4));
                Toast.makeText(this, getString(R.string.pin_4), Toast.LENGTH_SHORT).show();
            }

        });


        cancelCreatePin_IV.setOnClickListener(v -> onBackPressed());

    }


    // methods


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}













