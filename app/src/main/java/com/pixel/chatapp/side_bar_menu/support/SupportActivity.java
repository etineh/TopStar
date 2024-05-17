package com.pixel.chatapp.side_bar_menu.support;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.pixel.chatapp.R;
import com.pixel.chatapp.all_utils.OpenActivityUtil;

public class SupportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        ImageView arrowBackS = findViewById(R.id.arrowBackS);
        ConstraintLayout liveSupport = findViewById(R.id.liveChatClick);
        ImageView search_ = findViewById(R.id.search_);

        liveSupport.setOnClickListener(v -> {
            Intent intent = new Intent(this, SupportUserActivity.class);
            OpenActivityUtil.openColorHighlight(v, this, intent);
        });

        search_.setOnClickListener(v -> {
            Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
        });

        arrowBackS.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());


    }



}