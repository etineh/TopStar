package com.pixel.chatapp.signup_login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.window.OnBackInvokedDispatcher;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.pixel.chatapp.R;
import com.pixel.chatapp.all_utils.PhoneUtils;

public class ForgetActivity extends AppCompatActivity {

    TextInputEditText emailForgetPassword;
    Button resetButton;
    FirebaseAuth auth;

    ImageView closeP_IV;
    TextView resetInfo_TV, goToLoginPageTV;
    ProgressBar progressBar_;

    int close = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget);

        emailForgetPassword = findViewById(R.id.editTextEmailForgetPassword);
        resetButton = findViewById(R.id.resetButton_);
        closeP_IV = findViewById(R.id.closeP_IV);
        resetInfo_TV = findViewById(R.id.resetInfo_TV);
        goToLoginPageTV = findViewById(R.id.goToLoginPageTV);
        progressBar_ = findViewById(R.id.progressBar_);

        auth = FirebaseAuth.getInstance();

        resetButton.setOnClickListener(view ->
        {
            PhoneUtils.hideKeyboard(this , emailForgetPassword);
            progressBar_.setVisibility(View.VISIBLE);

            String email = emailForgetPassword.getText().toString();
            if (!email.isEmpty()) forgetPassword(email);
        });


        goToLoginPageTV.setOnClickListener(v ->
        {
            v.setBackgroundColor(ContextCompat.getColor(this, R.color.cool_orange));
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(10).withEndAction(()->
            {
                startActivity(new Intent(ForgetActivity.this, EmailOrPhoneLoginActivity.class));

                v.setBackgroundColor(0);
            });
        });

        closeP_IV.setOnClickListener(v -> onBackPressed());


    }
    
    public void forgetPassword(String email){

        auth.sendPasswordResetEmail(email).addOnCompleteListener(task ->
        {
            if (task.isSuccessful())
            {
                resetInfo_TV.setVisibility(View.VISIBLE);
                goToLoginPageTV.setVisibility(View.VISIBLE);
                resetButton.setText(R.string.resend_);
                Toast.makeText(ForgetActivity.this, getText(R.string.resetPasswordSent), Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(ForgetActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
            progressBar_.setVisibility(View.GONE);
        });
    }

    @Override
    public void onBackPressed() {
        if(close == 0){
            Toast.makeText(this, getString(R.string.pressAgain), Toast.LENGTH_SHORT).show();
            close = 1;
            new Handler().postDelayed( ()-> close = 0, 5_000);
        } else {
            super.onBackPressed();
            finish();
        }
    }


}









