package com.pixel.chatapp.view_controller;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.pixel.chatapp.R;

public class WhoCanParticipateActivity extends AppCompatActivity {

    ImageView arrowBack, doneClick;

    CheckBox everyoneCheck, myCommunityCheck, idVerifyCheck, premiumUserCheck, myContactCheck;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_who_can_participate);

        everyoneCheck = findViewById(R.id.everyoneCheck);
        arrowBack = findViewById(R.id.arrowBackS);
        doneClick = findViewById(R.id.doneClick);
        myCommunityCheck = findViewById(R.id.myCommunityCheck);
        idVerifyCheck = findViewById(R.id.idVerifyCheck);
        premiumUserCheck = findViewById(R.id.premiumUserCheck);
        myContactCheck = findViewById(R.id.myContactCheck);

        getOnBackPressedDispatcher().addCallback(callback);


        everyoneCheck.setOnClickListener(v -> {
            myCommunityCheck.setChecked(false);
            idVerifyCheck.setChecked(false);
            premiumUserCheck.setChecked(false);
            myContactCheck.setChecked(false);
        });

        myCommunityCheck.setOnClickListener(v -> {
            everyoneCheck.setChecked(false);
        });

        idVerifyCheck.setOnClickListener(v -> {
            everyoneCheck.setChecked(false);

        });

        premiumUserCheck.setOnClickListener(v -> {
            everyoneCheck.setChecked(false);

        });

        myContactCheck.setOnClickListener(v -> {
            everyoneCheck.setChecked(false);

        });

        arrowBack.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });


        doneClick.setOnClickListener(v -> {
            finish();
        });

    }




    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {

            finish();
        }
    };

}








