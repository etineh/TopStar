package com.pixel.chatapp.utilities;

import android.content.Context;
import android.util.Patterns;
import android.widget.Toast;

import com.pixel.chatapp.R;

public class EmailValidator {

    public static boolean isEmailValidUtil(String email, Context context) {
        if (email == null || email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, context.getString(R.string.emailError), Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }
}
