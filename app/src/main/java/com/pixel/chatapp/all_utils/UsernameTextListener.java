package com.pixel.chatapp.all_utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pixel.chatapp.R;

public class UsernameTextListener {

    public static TextWatcher userNameListener(final EditText editText, Context context, TextView textView, ProgressBar progressBar){

        final long THROTTLE_DELAY = 1_000;
        final Handler handler = new Handler();

        final Runnable validationRunnable = () -> {
            // Validate the username
            validateUsername(editText.getText().toString().toLowerCase(), editText, context, textView, progressBar);
        };

        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // This method is called before the text is changed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // Remove any pending validation tasks
                handler.removeCallbacks(validationRunnable);

                // Post a delayed validation task
                handler.postDelayed(validationRunnable, THROTTLE_DELAY);

                progressBar.setVisibility(View.VISIBLE);
                textView.setVisibility(View.GONE);

            }

            @Override
            public void afterTextChanged(Editable s) {
                // This method is called after the text has been changed
            }
        };

    }

    private static final DatabaseReference usernameRef = FirebaseDatabase.getInstance().getReference("usernames");

    private static void validateUsername(String username, EditText editText, Context context, TextView textView, ProgressBar progressBar) {

        if(username.startsWith("@")) username = username.substring(1);

        // Regular expression pattern to allow alphabets, digits, and underscores
        String pattern = "^[a-zA-Z0-9_]+$";

        // Check if the username matches the pattern
        boolean isValid = username.matches(pattern);

        if (isValid) {
            // Username is valid
            editText.setError(null); // Clear any previous error

            if(username.length() > 3){
                usernameRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            editText.setError(context.getString(R.string.usernameExist)); // Set error message
                            progressBar.setVisibility(View.GONE);

                        } else {
                            textView.setVisibility(View.VISIBLE);
                            textView.setText(context.getString(R.string.usernameAvailable));
                            progressBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        textView.setText(context.getString(R.string.isNetwork));
                        Toast.makeText(context, context.getString(R.string.isNetwork), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                editText.setError(context.getString(R.string.userNameError)); // Set error message
                progressBar.setVisibility(View.GONE);
            }


        } else {
            // Username is invalid
            if(username.length() > 0){
                editText.setError(context.getString(R.string.invalidUsername)); // Set error message
            }
            progressBar.setVisibility(View.GONE);

        }
    }
}

