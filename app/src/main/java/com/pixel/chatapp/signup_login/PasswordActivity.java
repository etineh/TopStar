package com.pixel.chatapp.signup_login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.pixel.chatapp.R;
import com.pixel.chatapp.utils.PhoneUtils;
import com.pixel.chatapp.home.MainActivity;

public class PasswordActivity extends AppCompatActivity {

    ProgressBar progressBarLog;
    TextView codeError_TV, login_TV, forgetPassword_TV;
    int close = 0;
    String email;
//    String password;
    String username;

    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        ImageView arrowBack_IV = findViewById(R.id.arrowBack_IV);
        ImageView support_IV = findViewById(R.id.support_IV);
        TextView username_TV = findViewById(R.id.username_TV);
        EditText password_ET = findViewById(R.id.password_ET);
        login_TV = findViewById(R.id.login_TV);
        progressBarLog = findViewById(R.id.progressBarLog);
        codeError_TV = findViewById(R.id.codeError_TV);
        forgetPassword_TV = findViewById(R.id.forgetPassword_TV);

        new Handler().postDelayed(password_ET::requestFocus, 500);

        email = getIntent().getStringExtra("email");
        username = "@" + getIntent().getStringExtra("username");

        username_TV.setText(username);

        login_TV.setOnClickListener(v -> {
            login_TV.setVisibility(View.GONE);
            progressBarLog.setVisibility(View.VISIBLE);

            PhoneUtils.hideKeyboard(this, password_ET);

            signInWithEmail(email, password_ET.getText().toString());

        });

        support_IV.setOnClickListener(v -> {
            Toast.makeText(this, "in progress", Toast.LENGTH_SHORT).show();
        });

        forgetPassword_TV.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgetActivity.class));
        });

        arrowBack_IV.setOnClickListener(v -> onBackPressed());


    }


    //  =========   methods ============

    public void signInWithEmail(String userEmail, String userPassword){
        auth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(task -> {

                    if(task.isSuccessful()){
                        startActivity(new Intent(PasswordActivity.this, MainActivity.class));
                        Toast.makeText(PasswordActivity.this, getString(R.string.loginSuccessful), Toast.LENGTH_SHORT).show();
                        finish();

                    } else if (task.getException().getMessage().contains("invalid"))
                    {
                        codeError_TV.setVisibility(View.VISIBLE);
                        codeError_TV.setText(getString(R.string.invalidPassword));
                        progressBarLog.setVisibility(View.GONE);
                        login_TV.setVisibility(View.VISIBLE);

                    } else if (task.getException().getMessage().contains("timeout"))
                    {
                        codeError_TV.setVisibility(View.VISIBLE);
                        codeError_TV.setText(getString(R.string.isNetwork));
                        progressBarLog.setVisibility(View.GONE);
                        login_TV.setVisibility(View.VISIBLE);

                    } else if (task.getException().getMessage().contains("We have blocked"))
                    {
                        codeError_TV.setVisibility(View.VISIBLE);
                        codeError_TV.setText(getString(R.string.accountDisable));
                        progressBarLog.setVisibility(View.GONE);
                        login_TV.setVisibility(View.VISIBLE);
                    }else{
                        codeError_TV.setVisibility(View.VISIBLE);
                        codeError_TV.setText(getString(R.string.errorOccur));
                        progressBarLog.setVisibility(View.GONE);
                        login_TV.setVisibility(View.VISIBLE);
                    }

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
            startActivity(new Intent(PasswordActivity.this, PhoneLoginActivity.class));
            finish();
        }
    }
}





