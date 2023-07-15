package com.pixel.chatapp.general;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.pixel.chatapp.R;
import com.pixel.chatapp.chats.MessageActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }
}