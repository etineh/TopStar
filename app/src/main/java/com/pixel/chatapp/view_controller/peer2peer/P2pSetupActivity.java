package com.pixel.chatapp.view_controller.peer2peer;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pixel.chatapp.R;

public class P2pSetupActivity extends AppCompatActivity {

    ImageView arrowBackS;
    Button addButton, applyButton;
    ProgressBar progressBar4;
    TextView verifyKYC;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p2p_setup);

        arrowBackS = findViewById(R.id.arrowBackS);
        addButton = findViewById(R.id.addButton);
        applyButton = findViewById(R.id.applyButton);
        progressBar4 = findViewById(R.id.progressBar4);
        verifyKYC = findViewById(R.id.verifyKYC);
//        arrowBackS = findViewById(R.id.arrowBackS);


        addButton.setOnClickListener(v -> {
            startActivity(new Intent(this, AddP2pAccountActivity.class));
        });


        getOnBackPressedDispatcher().addCallback(callback);

        arrowBackS.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

    }


    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {

            finish();
        }
    };
}



