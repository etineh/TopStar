package com.pixel.chatapp.side_bar_menu.premium;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pixel.chatapp.R;

public class PremiumActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium);


        ImageView arrowBackS = findViewById(R.id.arrowBackS);
        TextView vipMonthlyAmount = findViewById(R.id.vipMonthlyAmount);
        TextView vipAnnuAmount = findViewById(R.id.vipAnnuAmount);
        Button vipMonthPaybutton = findViewById(R.id.vipMonthPaybutton);
        Button vipYearPaybutton = findViewById(R.id.vipYearPaybutton);
        TextView premMonthAmount = findViewById(R.id.premMonthAmount);
        TextView premiumAnnuAmount = findViewById(R.id.premiumAnnuAmount);
        Button premMonthPaybutton = findViewById(R.id.premMonthPaybutton);
        Button premYearPaybutton = findViewById(R.id.premYearPaybutton);
//        TextView premMonthAmount = findViewById(R.id.premMonthAmount);


        vipMonthlyAmount.setText("$100");
        vipAnnuAmount.setText("$1050");
        premMonthAmount.setText("$5.3");
        premiumAnnuAmount.setText("$56");

        vipMonthPaybutton.setOnClickListener(v -> {
            Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
        });

        vipYearPaybutton.setOnClickListener(v -> {
            Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
        });

        premMonthPaybutton.setOnClickListener(v -> {
            Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
        });

        premYearPaybutton.setOnClickListener(v -> {
            Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
        });


        arrowBackS.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

    }



}