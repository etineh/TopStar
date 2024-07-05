package com.pixel.chatapp.activities;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.pixel.chatapp.R;
import com.pixel.chatapp.utils.OpenActivityUtil;
import com.pixel.chatapp.side_bar_menu.support.SupportActivity;

public class CreateLeagueActivity extends AppCompatActivity {

    Spinner spinnerSelectGame, spinnerEntryMood;
    ImageView arrowBackS, support;
    EditText startDate, entryFee_ET, entrySlot_ET, reward_ET;
    TextView moreSettingClick;
    CheckBox checkBoxTerms;
    Button createButton;
    ConstraintLayout whoParticipate;

    TextWatcher textRewardListerner, textWatcherSlot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_league);

        spinnerSelectGame = findViewById(R.id.spinnerSelectGame);
        spinnerSelectGame.setSelection(0);
        spinnerEntryMood = findViewById(R.id.spinnerEntryMood);

        arrowBackS = findViewById(R.id.arrowBackS);
        entryFee_ET = findViewById(R.id.entryFee_ET);
        entrySlot_ET = findViewById(R.id.entrySlot_ET);
        reward_ET = findViewById(R.id.reward_ET);
        moreSettingClick = findViewById(R.id.moreSettingClick);
        checkBoxTerms = findViewById(R.id.checkBoxTerms);
        createButton = findViewById(R.id.createButton);
        whoParticipate = findViewById(R.id.whoParticipateContainer);
        support = findViewById(R.id.support);

        getOnBackPressedDispatcher().addCallback(callback);

        entryFeeAndSlotListener();

        whoParticipate.setOnClickListener(v -> {
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(20).withEndAction(() ->
            {
                startActivity(new Intent(this, WhoCanParticipateActivity.class));

                new Handler().postDelayed(() -> {
                    v.setScaleX(1f);
                    v.setScaleY(1f);
                }, 300);
            });
        });

        moreSettingClick.setOnClickListener(v -> {
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(20).withEndAction(() ->
            {
                startActivity(new Intent(this, LMoreSettingsActivity.class));

                new Handler().postDelayed(() -> {
                    v.setScaleX(1f);
                    v.setScaleY(1f);
                }, 300);
            });
        });

        support.setOnClickListener(v ->
        {
            Intent intent = new Intent(this, SupportActivity.class);
            OpenActivityUtil.openColorHighlight(v, this, intent);
        });


        arrowBackS.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

    }

    //  ==========  methods

    private void entryFeeAndSlotListener()
    {
        textRewardListerner = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String slot = entrySlot_ET.getText().toString();
                String fee = entryFee_ET.getText().toString();

                if(!slot.isEmpty() && !fee.isEmpty())
                {
                    int rewardMin = ( Integer.parseInt(slot) * Integer.parseInt(fee) ) / 3;
                    reward_ET.setHint(getString(R.string.minWinnerReward) + rewardMin);

                } else {
                    reward_ET.setHint(getString(R.string.winnerReward));
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        entryFee_ET.addTextChangedListener(textRewardListerner);
        entrySlot_ET.addTextChangedListener(textRewardListerner);

    }


    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            entrySlot_ET.removeTextChangedListener(textRewardListerner);
            entryFee_ET.removeTextChangedListener(textRewardListerner);
            finish();
        }
    };

}







