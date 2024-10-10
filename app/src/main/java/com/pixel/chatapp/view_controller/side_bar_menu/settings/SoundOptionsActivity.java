package com.pixel.chatapp.view_controller.side_bar_menu.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.pixel.chatapp.R;

public class SoundOptionsActivity extends AppCompatActivity {

    ImageView arrowBackS;
    TextView headingOptionTV, subHeading_TV, goPremiumButton;
    RadioGroup radioGroup;
    RadioButton phoneDefaultRadio,muteRadio, appRingtoneRadio, vibrateRadio;
    String heading, subHeading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_options);

        arrowBackS = findViewById(R.id.arrowBackS);
        headingOptionTV = findViewById(R.id.headingOptionTV);
        subHeading_TV = findViewById(R.id.subHeading_TV);
        goPremiumButton = findViewById(R.id.goPremiumButton);
        radioGroup = findViewById(R.id.radioGroup);
        phoneDefaultRadio = findViewById(R.id.phoneDefaultRadio);
        muteRadio = findViewById(R.id.muteRadio);
        appRingtoneRadio = findViewById(R.id.appRingtoneRadio);
        vibrateRadio = findViewById(R.id.vibrateRadio);

        goPremiumButton.setOnClickListener(v -> Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show());


        heading = getIntent().getStringExtra("heading");
        subHeading = getIntent().getStringExtra("subHeading");

        headingOptionTV.setText(heading);
        subHeading_TV.setText(subHeading);

        if(heading.equals(getString(R.string.p2pTradeCall)) || heading.equals(getString(R.string.calls)))
        {
            appRingtoneRadio.setText(getString(R.string.ringtone));

        } else if (heading.equals(getString(R.string.societies)))
        {

        }



            arrowBackS.setOnClickListener(v -> onBackPressed());
        
    }
}











