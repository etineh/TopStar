package com.pixel.chatapp;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;
import com.pixel.chatapp.activities.AppLifecycleHandler;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.google.GoogleEmojiProvider;

public class FirebaseOffline extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        EmojiManager.install(new GoogleEmojiProvider()); // Use Google-style emojis

        // Register activity lifecycle callbacks
        registerActivityLifecycleCallbacks(new AppLifecycleHandler());

        // Inside your Application class or an appropriate initialization point
//        EmojiCompat.Config config = new BundledEmojiCompatConfig(this);
//        EmojiCompat.init(config);
    }
}

