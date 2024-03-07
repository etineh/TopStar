package com.pixel.chatapp.all_utils;

import android.content.Context;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;

public class PhoneUtils {
    public static void vibrateDevice(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator != null && vibrator.hasVibrator()) {
            // Vibrate for 100 milliseconds
            vibrator.vibrate(100);
        }
    }

    public static void hideKeyboard(Context context, View view) {
        // Get a reference to the InputMethodManager
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

    }

}
