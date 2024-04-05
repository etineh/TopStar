package com.pixel.chatapp.all_utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Vibrator;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

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

    public static void copyText(Context context, TextView textView)
    {
        ClipboardManager clipboard =  (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", textView.getText());

        if (clipboard == null || clip == null) return;
        clipboard.setPrimaryClip(clip);
    }



}
