package com.pixel.chatapp.all_utils;

import static com.pixel.chatapp.home.MainActivity.nightMood;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.pixel.chatapp.R;

public class OpenActivityUtil {

    public static void openColorHighlight(View v, Context context, Intent intent)
    {
        if(nightMood) v.setBackgroundColor(ContextCompat.getColor(context, R.color.dark_orange2));
        else v.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent_orange));

        new Handler().postDelayed(()-> {

            context.startActivity(intent);

            new Handler().postDelayed(()-> v.setBackgroundColor(0), 50);

        }, 1);
    }


}
