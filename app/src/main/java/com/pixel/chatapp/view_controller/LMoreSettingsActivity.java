package com.pixel.chatapp.view_controller;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;

import com.pixel.chatapp.R;

public class LMoreSettingsActivity extends AppCompatActivity {

    ImageView arrowBackS, doneClick, reload_TV;
    Switch joinCommunitySwitch;
    EditText leagueTitle_ET, sponsorBy_ET, commLink_ET, remarkET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lmore_settings);

        arrowBackS = findViewById(R.id.arrowBackS);
        reload_TV = findViewById(R.id.reload_TV);
        doneClick = findViewById(R.id.doneClick);
        joinCommunitySwitch = findViewById(R.id.joinCommunitySwitch);
        leagueTitle_ET = findViewById(R.id.leagueTitle_ET);
        sponsorBy_ET = findViewById(R.id.sponsorBy_ET);
        commLink_ET = findViewById(R.id.commLink_ET);
        remarkET = findViewById(R.id.remarkET);

        getOnBackPressedDispatcher().addCallback(callback);

        arrowBackS.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        reload_TV.setOnClickListener(v -> {
            joinCommunitySwitch.setChecked(false);
            commLink_ET.setVisibility(View.GONE);
            commLink_ET.clearFocus();
            leagueTitle_ET.setText(null);
            sponsorBy_ET.setText(null);
            remarkET.setText(null);
        });

        doneClick.setOnClickListener(v -> {
            finish();
        });


        joinCommunitySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                commLink_ET.setVisibility(View.VISIBLE);
                commLink_ET.requestFocus();
            } else {
                commLink_ET.setVisibility(View.GONE);
                commLink_ET.clearFocus();
            }
        });

    }




    private final OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {

            finish();
        }
    };

}












