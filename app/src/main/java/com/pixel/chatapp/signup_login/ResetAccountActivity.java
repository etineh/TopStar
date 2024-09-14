package com.pixel.chatapp.signup_login;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pixel.chatapp.R;
import com.pixel.chatapp.activities.OTPActivity;
import com.pixel.chatapp.utils.IdTokenUtil;
import com.pixel.chatapp.utils.OpenActivityUtil;
import com.pixel.chatapp.side_bar_menu.support.SupportActivity;

public class ResetAccountActivity extends AppCompatActivity {

    int close = 0;

    ImageView arrowBack_IV,support_IV;
    TextInputEditText email_ET, newPassword_ET, confirmPassword;
    Button resetButton;
    ProgressBar progressBarReset;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_account);

        email_ET = findViewById(R.id.email_ET);
        newPassword_ET = findViewById(R.id.newPassword_ET);
        confirmPassword = findViewById(R.id.confirmPassword);
        progressBarReset = findViewById(R.id.progressBarReset);
        resetButton = findViewById(R.id.resetButton);
        arrowBack_IV = findViewById(R.id.arrowBack_IV);
        support_IV = findViewById(R.id.support_IV);


        arrowBack_IV.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        support_IV.setOnClickListener(v -> {
            Intent intent = new Intent(this, SupportActivity.class);
            intent.putExtra("reason", "reset account issue");
            OpenActivityUtil.openColorHighlight(v, this, intent);
        });


        resetButton.setOnClickListener(v -> {

            String email = email_ET.getText().toString();
            String password = newPassword_ET.getText().toString();
            String confirmPass = confirmPassword.getText().toString();

            if(currentUser != null)
            {
                if(!email.isEmpty() && !password.isEmpty() && !confirmPass.isEmpty())
                {
                    if (email.contains("@") && email.contains(".")) {

                        if(!currentUser.getEmail().equals(email))
                        {
                            if(password.equals(confirmPass))
                            {
                                if(password.length() > 5)
                                {
                                    IdTokenUtil.generateToken(token ->
                                    {
                                        Intent intent = new Intent(this, OTPActivity.class);
                                        intent.putExtra("type", "reset");
                                        intent.putExtra("token", token);
                                        intent.putExtra("previousData", "");
                                        intent.putExtra("newData", email);
                                        intent.putExtra("pass", password);

                                        startActivity(intent);
                                        finish();
                                    }, this);


                                } else Toast.makeText(this, getString(R.string.passwordAbove5), Toast.LENGTH_SHORT).show();

                            } else Toast.makeText(this, getString(R.string.passwordDontMatch), Toast.LENGTH_SHORT).show();

                        } else Toast.makeText(this, getString(R.string.newEmailRequired), Toast.LENGTH_SHORT).show();

                    } else Toast.makeText(this, getString(R.string.emailError), Toast.LENGTH_SHORT).show();

                } else Toast.makeText(this, getString(R.string.fieldEmpty), Toast.LENGTH_SHORT).show();

            } else Toast.makeText(this, getString(R.string.userNotFound), Toast.LENGTH_SHORT).show();


        });

        getOnBackPressedDispatcher().addCallback(callback);

    }



    //  =========== methods =========


    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if(close == 0){
                Toast.makeText(ResetAccountActivity.this, getString(R.string.pressAgain), Toast.LENGTH_SHORT).show();
                close = 1;
                new Handler().postDelayed( ()-> close = 0, 5_000);
            } else {
                finish();
            }
        }
    };

}