package com.pixel.chatapp.view_controller.photos_video;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.pixel.chatapp.R;
import com.pixel.chatapp.utilities.Photo_Video_Utils;
import com.squareup.picasso.Picasso;

public class ZoomImage extends AppCompatActivity {

    private String imageLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom_image);

        PhotoView imageZoom = findViewById(R.id.zoomPhoto_PView);
        TextView userNameDisplay = findViewById(R.id.userNameDisplay);
        ImageView arrowBack = findViewById(R.id.arrowBack);
        ImageView sharePhoto_Z = findViewById(R.id.sharePhoto_Z);


        String name = getIntent().getStringExtra("otherName");
        imageLink = getIntent().getStringExtra("imageLink");
        String from = getIntent().getStringExtra("from");

        if(from != null && !from.equals("profilePix")) {
            sharePhoto_Z.setVisibility(View.GONE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }

        sharePhoto_Z.setOnClickListener(v -> {
            Toast.makeText(this, getText(R.string.preparingPhoto), Toast.LENGTH_SHORT).show();
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(10).withEndAction(() ->
                    Photo_Video_Utils.downloadImageFromInternetAndShare(this, imageLink, getString(R.string.appInvite) ));
        });

        userNameDisplay.setText(name);

        if (imageLink != null) {
            if(!imageLink.equals("null"))
                Picasso.get().load(imageLink).into(imageZoom);

        } else {
            imageZoom.setImageResource(R.drawable.person_round);
//            name = "Image not Found";
        }


        getOnBackPressedDispatcher().addCallback(callback);

        arrowBack.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

    }


    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed()
        {
            finish();
        }
    };
}