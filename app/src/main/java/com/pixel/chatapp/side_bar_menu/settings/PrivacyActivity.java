package com.pixel.chatapp.side_bar_menu.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pixel.chatapp.R;
import com.pixel.chatapp.utils.OpenActivityUtil;

public class PrivacyActivity extends AppCompatActivity {

    ImageView arrowBackS;
    TextView lastSeenTV, lastSeenInfo;
    TextView hintTV, hintInfoTv;
    TextView profilePhotoTV, profilePhotoInfo;
    TextView phoneNumberTV, photoNumberInfo;
    TextView societyTV, societyInfo;
    TextView readAlertTV, readAlertInfo_TV;
    TextView callsTV, callInfo;
    TextView gameTV, gameInfo;
    TextView alertTV, alertInfo;
    TextView blockChatsTV, blockChatInfo;
    TextView dispalyMsg_TV, dispalyMsgInfo_TV, valuePrivacy;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        arrowBackS = findViewById(R.id.arrowBackS);
        lastSeenTV = findViewById(R.id.lastSeenTV);
        lastSeenInfo = findViewById(R.id.lastSeenInfoTV);
        hintTV = findViewById(R.id.hintTV);
        hintInfoTv = findViewById(R.id.hintInfoTv);
        profilePhotoTV = findViewById(R.id.profilePhotoTV);
        profilePhotoInfo = findViewById(R.id.profilePhotoInfo);
        phoneNumberTV = findViewById(R.id.phoneNumberTV);
        photoNumberInfo = findViewById(R.id.photoNumberInfo);
        societyTV = findViewById(R.id.societyTV);
        societyInfo = findViewById(R.id.societyInfo);
        readAlertTV = findViewById(R.id.readAlertTV);
        readAlertInfo_TV = findViewById(R.id.readAlertInfo_TV);
        callsTV = findViewById(R.id.callsTV);
        callInfo = findViewById(R.id.callInfo);
        gameTV = findViewById(R.id.gameTV);
        gameInfo = findViewById(R.id.gameInfo);
        alertTV = findViewById(R.id.alertTV);
        alertInfo = findViewById(R.id.alertInfo);
        blockChatsTV = findViewById(R.id.blockChatsTV);
        blockChatInfo = findViewById(R.id.blockChatInfo);
        valuePrivacy = findViewById(R.id.valuePrivacy);

        dispalyMsg_TV = findViewById(R.id.dispalyMsg_TV);
        dispalyMsgInfo_TV = findViewById(R.id.dispalyMsgInfo_TV);


        arrowBackS.setOnClickListener( v -> onBackPressed());


        View.OnClickListener photo = v ->
        {
            Intent intent = new Intent(this, PrivacyOptionsActivity.class);
            intent.putExtra("heading", getString(R.string.profilePhoto));
            intent.putExtra("subHeading", getString(R.string.noteLastPhoto));

//            startActivity(intent);
            OpenActivityUtil.openColorHighlight(valuePrivacy, this, intent);

        };

        profilePhotoTV.setOnClickListener(photo);
        profilePhotoInfo.setOnClickListener(photo);


        View.OnClickListener lastSeen = v ->
        {
            Intent intent = new Intent(this, PrivacyOptionsActivity.class);

            intent.putExtra("heading", getString(R.string.lastSeen_));
            intent.putExtra("subHeading", getString(R.string.noteLastSeen));

//            startActivity(intent);
            OpenActivityUtil.openColorHighlight(valuePrivacy, this, intent);

        };

        lastSeenTV.setOnClickListener(lastSeen);
        lastSeenInfo.setOnClickListener(lastSeen);

        View.OnClickListener hint = v ->
        {
            Intent intent = new Intent(this, PrivacyOptionsActivity.class);

            intent.putExtra("heading", getString(R.string.hintTitle));
            intent.putExtra("subHeading", getString(R.string.noteHint));

//            startActivity(intent);
            OpenActivityUtil.openColorHighlight(valuePrivacy, this, intent);
        };
        hintTV.setOnClickListener(hint);
        hintInfoTv.setOnClickListener(hint);

        View.OnClickListener number = v ->
        {
            Intent intent = new Intent(this, PrivacyOptionsActivity.class);

            intent.putExtra("heading", getString(R.string.phoneNumber));
            intent.putExtra("subHeading", getString(R.string.noteNumber));

//            startActivity(intent);
            OpenActivityUtil.openColorHighlight(valuePrivacy, this, intent);
        };

        phoneNumberTV.setOnClickListener(number);
        photoNumberInfo.setOnClickListener(number);

        View.OnClickListener societiesClick = v ->
        {
            Intent intent = new Intent(this, PrivacyOptionsActivity.class);

            intent.putExtra("heading", getString(R.string.societies));
            intent.putExtra("subHeading", getString(R.string.noteSociety));

            OpenActivityUtil.openColorHighlight(valuePrivacy, this, intent);
        };

        societyTV.setOnClickListener(societiesClick);
        societyInfo.setOnClickListener(societiesClick);


        View.OnClickListener readAlert = v ->
        {
            Intent intent = new Intent(this, PrivacyOptionsActivity.class);

            intent.putExtra("heading", getString(R.string.readAlert));
            intent.putExtra("subHeading", getString(R.string.noteAlertReceipt));

            OpenActivityUtil.openColorHighlight(valuePrivacy, this, intent);
        };

        readAlertTV.setOnClickListener(readAlert);
        readAlertInfo_TV.setOnClickListener(readAlert);

        View.OnClickListener calls = v ->
        {
            Intent intent = new Intent(this, PrivacyOptionsActivity.class);

            intent.putExtra("heading", getString(R.string.calls));
            intent.putExtra("subHeading", getString(R.string.noteCalls));

            OpenActivityUtil.openColorHighlight(valuePrivacy, this, intent);
        };

        callsTV.setOnClickListener(calls);
        callInfo.setOnClickListener(calls);

        View.OnClickListener games = v ->
        {
            Intent intent = new Intent(this, PrivacyOptionsActivity.class);

            intent.putExtra("heading", getString(R.string.games));
            intent.putExtra("subHeading", getString(R.string.noteGames));

            OpenActivityUtil.openColorHighlight(valuePrivacy, this, intent);
        };

        gameTV.setOnClickListener(games);
        gameInfo.setOnClickListener(games);

        View.OnClickListener alertChat = v ->
        {
            Intent intent = new Intent(this, PrivacyOptionsActivity.class);

            intent.putExtra("heading", getString(R.string.alertChat));
            intent.putExtra("subHeading", getString(R.string.noteAlertChat));

            OpenActivityUtil.openColorHighlight(valuePrivacy, this, intent);
        };

        alertTV.setOnClickListener(alertChat);
        alertInfo.setOnClickListener(alertChat);

        View.OnClickListener disappearChat = v ->
        {
            Intent intent = new Intent(this, PrivacyOptionsActivity.class);

            intent.putExtra("heading", getString(R.string.disappear_msg));
            intent.putExtra("subHeading", getString(R.string.noteDisappear_msg));

            OpenActivityUtil.openColorHighlight(valuePrivacy, this, intent);
        };

        dispalyMsg_TV.setOnClickListener(disappearChat);
        dispalyMsgInfo_TV.setOnClickListener(disappearChat);


        View.OnClickListener blockContact = v ->
        {
            Intent intent = new Intent(this, PrivacyOptionsActivity.class);

            intent.putExtra("heading", getString(R.string.blockContact));
            intent.putExtra("subHeading", getString(R.string.none));

            OpenActivityUtil.openColorHighlight(valuePrivacy, this, intent);
        };

        blockChatsTV.setOnClickListener(blockContact);
        blockChatInfo.setOnClickListener(blockContact);

    }

    //      =========       methods     ==========

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }



}