package com.pixel.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

public class ZoomImage extends AppCompatActivity {

    private PhotoView imageZoom;
    private TextView userNameDisplay;
    private ImageView arrowBack;
    private String imageLink;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom_image);

        imageZoom = findViewById(R.id.zoomPhoto_PView);
        userNameDisplay = findViewById(R.id.userNameDisplay);
        arrowBack = findViewById(R.id.arrowBack);

        name = getIntent().getStringExtra("otherName");
        imageLink = getIntent().getStringExtra("imageLink");

        userNameDisplay.setText(name);

        if (imageLink != null) {
            if(!imageLink.equals("null"))
                Picasso.get().load(imageLink).into(imageZoom);

        } else {
            imageZoom.setImageResource(R.drawable.person_round);
            name = "Image not Found";
        }

//        Picasso.get().load(imageLink).into(new Target() {
//            @Override
//            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                imageZoom.setImageBitmap(bitmap);
//            }
//
//            @Override
//            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
//                imageZoom.setImageResource(R.drawable.person_round);
//                name = "Image not Found";
//            }
//
//            @Override
//            public void onPrepareLoad(Drawable placeHolderDrawable) {
////                holder.showImage.setImageResource(R.color.transparent_orangeLow);
//            }
//        });


        arrowBack.setOnClickListener(view -> onBackPressed());

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}