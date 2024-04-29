package com.pixel.chatapp.side_bar_menu.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.pixel.chatapp.R;

public class ResetSecurityActivity extends AppCompatActivity {

    String heading;
    ImageView arrowBack_IV;
    TextView titleTV, forget_TV;

    TextInputEditText valueOne_ET, valueTwo_ET, valueThree_ET;
    TextInputLayout valueOneLayout, valueTwoLayout, valueThreeLayout;
    Button resetButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_security);

        resetButton = findViewById(R.id.resetButton);
        arrowBack_IV = findViewById(R.id.arrowBack_IV);
        titleTV = findViewById(R.id.titleTV);
        valueOne_ET = findViewById(R.id.valueOne_ET);
        valueTwo_ET = findViewById(R.id.valueTwo_ET);
        valueThree_ET = findViewById(R.id.valueThree_ET);
        valueOneLayout = findViewById(R.id.valueOneLayout);
        valueTwoLayout = findViewById(R.id.valueTwoLayout);
        valueThreeLayout = findViewById(R.id.valueThreeLayout);
        forget_TV = findViewById(R.id.forget_TV);
        heading = getIntent().getStringExtra("title");
        resetButton.setText(heading);

        if(heading.equals(getString(R.string.changePin)))
        {
//            titleTV.setText(R.string.changePin);
//            valueOne_ET.setHint(R.string.newPin);
//            valueOne_ET.setHint(R.string.changePin);
            
        } else if (heading.equals(getString(R.string.createPin)))
        {
            titleTV.setText(R.string.createPin);
            valueOne_ET.setVisibility(View.GONE);
            
        } else if (heading.equals(getString(R.string.changePassword)))
        {
            titleTV.setText(R.string.changePassword);
            valueOneLayout.setHint(R.string.oldPassword);
            valueTwoLayout.setHint(R.string.newPassword);
            valueThreeLayout.setHint(getString(R.string.confirmPassword));
            forget_TV.setText(getString(R.string.forgetPassword));

        }

        forget_TV.setOnClickListener(v -> {
            Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();

        });

        resetButton.setOnClickListener(v -> {
            Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();

        });

        arrowBack_IV.setOnClickListener(v -> onBackPressed());

    }



}





