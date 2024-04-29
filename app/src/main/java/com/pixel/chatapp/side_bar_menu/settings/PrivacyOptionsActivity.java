package com.pixel.chatapp.side_bar_menu.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.pixel.chatapp.R;

public class PrivacyOptionsActivity extends AppCompatActivity {

    ImageView arrowBackS;
    TextView headingTV, subHeading_TV, lastSeenNotice, goPremiumButton;
    RadioGroup radioGroup;
    RadioButton everyoneRadio, myContactRadio, contactExceptRadio, nobodyRadio, nobodyExceptRadio;

    String heading, subHeading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_options);

        radioGroup = findViewById(R.id.radioGroup);
        headingTV = findViewById(R.id.headingOptionTV);
        subHeading_TV = findViewById(R.id.subHeading_TV);
        everyoneRadio = findViewById(R.id.everyoneRadio);
        myContactRadio = findViewById(R.id.myContactRadio);
        contactExceptRadio = findViewById(R.id.contactExceptRadio);
        nobodyExceptRadio = findViewById(R.id.nobodyExceptRadio);
        nobodyRadio = findViewById(R.id.nobodyRadio);
        lastSeenNotice = findViewById(R.id.lastSeenNotice);
        arrowBackS = findViewById(R.id.arrowBackS);
        goPremiumButton = findViewById(R.id.goPremiumButton);

        heading = getIntent().getStringExtra("heading");
        subHeading = getIntent().getStringExtra("subHeading");

        headingTV.setText(heading);
        subHeading_TV.setText(subHeading);

        if(heading.equals(getString(R.string.lastSeen_)))
        {
            lastSeenNotice.setVisibility(View.VISIBLE);

        } else if (heading.equals(getString(R.string.readAlert)))
        {
            everyoneRadio.setText(getString(R.string.on));
            myContactRadio.setText(getString(R.string.off));

            contactExceptRadio.setVisibility(View.GONE);
            nobodyRadio.setVisibility(View.GONE);
            nobodyExceptRadio.setVisibility(View.GONE);

            lastSeenNotice.setText(R.string.unlessPremiumReadReceipt);
            lastSeenNotice.setVisibility(View.VISIBLE);

        } else if (heading.equals(getString(R.string.disappear_msg)))
        {
            everyoneRadio.setText(getString(R.string.hour24));
            myContactRadio.setText(getString(R.string.hour90));
            contactExceptRadio.setText(getString(R.string.hour180));
            nobodyRadio.setText(getString(R.string.off));

            nobodyExceptRadio.setVisibility(View.GONE);
        }
        else if (heading.equals(getString(R.string.phoneNumber)))
        {
            everyoneRadio.setText(getString(R.string.privateNumber));
            myContactRadio.setText(getString(R.string.publicNumber));

            contactExceptRadio.setVisibility(View.GONE);
            nobodyRadio.setVisibility(View.GONE);
            nobodyExceptRadio.setVisibility(View.GONE);
        } else if (
                heading.equals(getString(R.string.calls)) ||
                        heading.equals(getString(R.string.games)) ||
                        heading.equals(getString(R.string.alertChat))
        ) {
            goPremiumButton.setVisibility(View.VISIBLE);

        } else if (heading.equals(getString(R.string.blockContact))){
            radioGroup.setVisibility(View.GONE);
        }


        goPremiumButton.setOnClickListener(v -> {
            Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
        });

        arrowBackS.setOnClickListener(v -> onBackPressed());


    }



}