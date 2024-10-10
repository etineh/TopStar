package com.pixel.chatapp.view_controller.side_bar_menu.settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.pixel.chatapp.R;
import com.pixel.chatapp.utilities.OpenActivityUtil;

public class SecurityActivity extends AppCompatActivity {

    ConstraintLayout createPinLayout, changePinLayout, changePassLayout,
            walletLayout, gameLayout, usdtLayout, p2pLayout;
    ImageView arrowBackSo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security);


        createPinLayout = findViewById(R.id.createPinLayout);
        changePinLayout = findViewById(R.id.changePinLayout);
        changePassLayout = findViewById(R.id.changePassLayout);
        walletLayout = findViewById(R.id.walletLayout);
        gameLayout = findViewById(R.id.gameLayout);
        usdtLayout = findViewById(R.id.usdtLayout);
        p2pLayout = findViewById(R.id.p2pLayout);
        arrowBackSo = findViewById(R.id.arrowBackSo);

        createPinLayout.setOnClickListener(v ->
        {
            Intent intent = new Intent(this, ResetSecurityActivity.class);
            intent.putExtra("title", getString(R.string.createPin));

            OpenActivityUtil.openColorHighlight(v, this, intent);
        });

        changePinLayout.setOnClickListener(v ->
        {
            Intent intent = new Intent(this, ResetSecurityActivity.class);
            intent.putExtra("title", getString(R.string.changePin));

            OpenActivityUtil.openColorHighlight(v, this, intent);
        });

        changePassLayout.setOnClickListener(v ->
        {
            Intent intent = new Intent(this, ResetSecurityActivity.class);
            intent.putExtra("title", getString(R.string.changePassword));

            OpenActivityUtil.openColorHighlight(v, this, intent);
        });

        walletLayout.setOnClickListener(v ->
        {
            Intent intent = new Intent(this, SecurityOptionsActivity.class);
            intent.putExtra("heading", getString(R.string.walletSecurity));

            OpenActivityUtil.openColorHighlight(v, this, intent);

        });

        gameLayout.setOnClickListener(v ->
        {
            Intent intent = new Intent(this, SecurityOptionsActivity.class);
            intent.putExtra("heading", getString(R.string.gameSecurity));

            OpenActivityUtil.openColorHighlight(v, this, intent);
        });

        usdtLayout.setOnClickListener(v ->
        {
            Intent intent = new Intent(this, SecurityOptionsActivity.class);
            intent.putExtra("heading", getString(R.string.usdtTrans));

            OpenActivityUtil.openColorHighlight(v, this, intent);
        });

        p2pLayout.setOnClickListener(v ->
        {
            Intent intent = new Intent(this, SecurityOptionsActivity.class);
            intent.putExtra("heading", getString(R.string.p2pTrans));

            OpenActivityUtil.openColorHighlight(v, this, intent);
        });

//        usdtLayout.setOnClickListener(v ->
//        {
//            Intent intent = new Intent(this, ResetSecurityActivity.class);
//
//            OpenActivityUtil.openColorHighlight(v, this, intent);
//        });


        arrowBackSo.setOnClickListener(v -> onBackPressed());

    }










}