package com.pixel.chatapp.view_controller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.pixel.chatapp.R;

public class RedirectToHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redirect_to_home);

        new Handler().postDelayed(()->{
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra("otherUid"))
            {
                Intent mainActivityIntent = new Intent(RedirectToHomeActivity.this, MainActivity.class);
                mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(mainActivityIntent);
                finish();
            }
        }, 100);

    }
}