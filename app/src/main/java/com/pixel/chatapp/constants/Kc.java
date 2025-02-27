package com.pixel.chatapp.constants;

import android.os.Handler;
import android.os.Looper;

import androidx.activity.OnBackPressedCallback;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Kc {

    public static final Handler handler = new Handler(Looper.getMainLooper());

    public static final ExecutorService executor = Executors.newSingleThreadExecutor();

//    private static final ExecutorService executorCached = Executors.newCachedThreadPool();

    public static OnBackPressedCallback onBackPress(Runnable action) {
        return new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                action.run();
            }
        };
    }

}
