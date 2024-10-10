package com.pixel.chatapp.view_controller.side_bar_menu.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pixel.chatapp.R;
import com.pixel.chatapp.utilities.OpenActivityUtil;

public class SoundActivity extends AppCompatActivity {

    LinearLayout privateChatLayout, societyLayout, TeamLayout, newChatLayout, p2pTradeLayout, gameLayout, callLayout;
    TextView privateChatResult, societyResult, teamResult, newMessageResult, p2pCallsResult, gameResult, alertCallsResult;
    ImageView arrowBackSo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound);

        privateChatLayout = findViewById(R.id.privateChatLayout);
        societyLayout = findViewById(R.id.societyLayout);
        TeamLayout = findViewById(R.id.TeamLayout);
        newChatLayout = findViewById(R.id.newChatLayout);
        p2pTradeLayout = findViewById(R.id.p2pTradeLayout);
        callLayout = findViewById(R.id.callLayout);
        alertCallsResult = findViewById(R.id.alertCallsResult);
        societyResult = findViewById(R.id.societyResult);
        gameLayout = findViewById(R.id.gameLayout);
        gameResult = findViewById(R.id.gameResult);
        privateChatResult = findViewById(R.id.privateChatResult);
        teamResult = findViewById(R.id.teamResult);
        newMessageResult = findViewById(R.id.newMessageResult);
        p2pCallsResult = findViewById(R.id.p2pCallsResult);
        arrowBackSo = findViewById(R.id.arrowBackSo);

        privateChatLayout.setOnClickListener(v ->
        {
            Intent intent = new Intent(this, SoundOptionsActivity.class);
            intent.putExtra("heading", getString(R.string.privateAlert__));
            intent.putExtra("subHeading", getString(R.string.notePrvAlert));

            OpenActivityUtil.openColorHighlight(v, this, intent);
        });

        societyLayout.setOnClickListener(v ->
        {
            Intent intent = new Intent(this, SoundOptionsActivity.class);
            intent.putExtra("heading", getString(R.string.societies));
            intent.putExtra("subHeading", getString(R.string.noteSocieties));

            OpenActivityUtil.openColorHighlight(v, this, intent);
        });

        TeamLayout.setOnClickListener(v ->
        {
            Intent intent = new Intent(this, SoundOptionsActivity.class);
            intent.putExtra("heading", getString(R.string.players));
            intent.putExtra("subHeading", getString(R.string.noteTeam));

            OpenActivityUtil.openColorHighlight(v, this, intent);
        });

        newChatLayout.setOnClickListener(v ->
        {
            Intent intent = new Intent(this, SoundOptionsActivity.class);
            intent.putExtra("heading", getString(R.string.newMessage));
            intent.putExtra("subHeading", getString(R.string.noteMSG));

            OpenActivityUtil.openColorHighlight(v, this, intent);
        });

        p2pTradeLayout.setOnClickListener(v ->
        {
            Intent intent = new Intent(this, SoundOptionsActivity.class);
            intent.putExtra("heading", getString(R.string.p2pTradeCall));
            intent.putExtra("subHeading", getString(R.string.noteTrading));

            OpenActivityUtil.openColorHighlight(v, this, intent);
        });

        gameLayout.setOnClickListener(v ->
        {
            Intent intent = new Intent(this, SoundOptionsActivity.class);
            intent.putExtra("heading", getString(R.string.games));
            intent.putExtra("subHeading", getString(R.string.noteGame));

            OpenActivityUtil.openColorHighlight(v, this, intent);
        });

        callLayout.setOnClickListener(v ->
        {
            Intent intent = new Intent(this, SoundOptionsActivity.class);
            intent.putExtra("heading", getString(R.string.alertCalls));
            intent.putExtra("subHeading", getString(R.string.noteCalls__));
            OpenActivityUtil.openColorHighlight(v, this, intent);
        });

        arrowBackSo.setOnClickListener(v -> onBackPressed());

    }





}