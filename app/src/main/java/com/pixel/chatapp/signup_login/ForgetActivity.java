package com.pixel.chatapp.signup_login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.pixel.chatapp.R;

public class ForgetActivity extends AppCompatActivity {

    TextInputEditText emailForgetPassword;
    Button buttonEmailForgetPassword;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget);

        emailForgetPassword = findViewById(R.id.editTextEmailForgetPassword);
        buttonEmailForgetPassword = findViewById(R.id.buttonEmailForgetPassword);

        auth = FirebaseAuth.getInstance();

        buttonEmailForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailForgetPassword.getText().toString();
                if (!email.equals("")) forgetPassword(email);
            }
        });
    }
    
    public void forgetPassword(String email){
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    startActivity(new Intent(ForgetActivity.this, LoginActivity.class));
                    Toast.makeText(ForgetActivity.this, "Password reset sent to your email", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(ForgetActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}









