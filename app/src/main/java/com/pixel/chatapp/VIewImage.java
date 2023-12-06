package com.pixel.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

public class VIewImage extends AppCompatActivity {

    private PhotoView imageZoom;
    private TextView userNameDisplay;
    private ImageView arrowBack;
    private String imageLink;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        imageZoom = findViewById(R.id.myimage);
        userNameDisplay = findViewById(R.id.userNameDisplay);
        arrowBack = findViewById(R.id.arrowBack);

        name = getIntent().getStringExtra("otherName");
        imageLink = getIntent().getStringExtra("imageLink");

        userNameDisplay.setText(name);

        if (!imageLink.equals("null")) {
            Picasso.get().load(imageLink).into(imageZoom);

        } else {
            imageZoom.setImageResource(R.drawable.person_round);
        }


        arrowBack.setOnClickListener(view -> onBackPressed());

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}