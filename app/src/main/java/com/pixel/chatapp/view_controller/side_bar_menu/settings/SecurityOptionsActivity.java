package com.pixel.chatapp.view_controller.side_bar_menu.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.pixel.chatapp.R;

public class SecurityOptionsActivity extends AppCompatActivity {

    ImageView arrowBackS;
    TextView headingOptionTV, subHeading_TV, goPremiumButton;
    RadioButton fingerprintRadio, pinRadio, emailOtpRadio,pinAndEmailRadio, fingerprint_pinRadio, fingerprint_emailRadio;
    RadioGroup radioGroup;

    String heading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_options);

        arrowBackS = findViewById(R.id.arrowBackS);
        headingOptionTV = findViewById(R.id.headingOptionTV);
        subHeading_TV = findViewById(R.id.subHeading_TV);
        goPremiumButton = findViewById(R.id.goPremiumButton);
        fingerprintRadio = findViewById(R.id.fingerprintRadio);
        pinRadio = findViewById(R.id.pinRadio);
        emailOtpRadio = findViewById(R.id.emailOtpRadio);
        pinAndEmailRadio = findViewById(R.id.pinAndEmailRadio);
        fingerprint_pinRadio = findViewById(R.id.fingerprint_pinRadio);
        fingerprint_emailRadio = findViewById(R.id.fingerprint_emailRadio);
        radioGroup = findViewById(R.id.radioGroup);


        heading = getIntent().getStringExtra("heading");

        headingOptionTV.setText(heading);

        if(heading.equals(getString(R.string.walletSecurity)))
        {
            fingerprintRadio.setChecked(true);
            fingerprintRadio.setText(getString(R.string.fingerprintDefault));

        } else if (heading.equals(getString(R.string.gameSecurity)))
        {
            fingerprintRadio.setChecked(true);
            fingerprintRadio.setText(getString(R.string.fingerprintDefault));
            subHeading_TV.setText(getString(R.string.gameSec));

        } else if (heading.equals(getString(R.string.usdtTrans)))
        {
            pinRadio.setChecked(true);
            pinRadio.setText(getString(R.string.pinDefault));
            subHeading_TV.setText(getString(R.string.usdtSec));

        }else if (heading.equals(getString(R.string.p2pTrans)))
        {
            pinRadio.setChecked(true);
            pinRadio.setText(getString(R.string.pinDefault));
            subHeading_TV.setText(getString(R.string.p2pSec));
        }

        goPremiumButton.setOnClickListener(v -> {
            Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
        });

        arrowBackS.setOnClickListener(v -> onBackPressed());

    }







}














