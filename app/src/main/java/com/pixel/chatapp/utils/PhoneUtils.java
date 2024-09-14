package com.pixel.chatapp.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class PhoneUtils {
    public static void vibrateDevice(Context context, int volume) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator != null && vibrator.hasVibrator()) {
            // Vibrate for 100 milliseconds
            vibrator.vibrate(volume);
        }
    }


    public static void hideKeyboard(Context context, View view) {
        // Get a reference to the InputMethodManager
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void showKeyboard(Context context, View view) {
        // Get a reference to the InputMethodManager
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            // Request focus for the view
            view.requestFocus();
            // Show the soft keyboard
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }


    public static void copyText(Context context, TextView textView)
    {
        ClipboardManager clipboard =  (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", textView.getText());

        if (clipboard == null || clip == null) return;
        clipboard.setPrimaryClip(clip);
    }

    public static void hasInternetConnectivity(CheckInternet checkInternet) {
        new Thread(() -> {
            try {
                // Check internet reachability by pinging a known server (e.g., google.com)
                HttpURLConnection urlc = (HttpURLConnection) (new URL("https://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(2000); // Timeout in milliseconds
                urlc.connect();
                int getResponse = urlc.getResponseCode();

                // Switch to main thread before interacting with UI
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (getResponse == 200 || getResponse == 204) {
                        checkInternet.networkIsTrue();
                    } else {
                        checkInternet.networkIsFalse();
                    }
                });

            } catch (IOException e) {
                // Error while checking internet access, switch to main thread for UI interactions
                new Handler(Looper.getMainLooper()).post(() -> {
                    System.out.println("what is error PhoneUtils L84: " + e.getMessage());
                    checkInternet.networkIsFalse();
                });
                e.printStackTrace();
            }
        }).start();
    }

    public interface CheckInternet{
        void networkIsTrue();
        void networkIsFalse();
    }

}
