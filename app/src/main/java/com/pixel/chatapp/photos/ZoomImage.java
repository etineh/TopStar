package com.pixel.chatapp.photos;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.pixel.chatapp.R;
import com.pixel.chatapp.all_utils.SharePhotoUtil;
import com.squareup.picasso.Picasso;

public class ZoomImage extends AppCompatActivity {

    private PhotoView imageZoom;
    private TextView userNameDisplay;
    private ImageView arrowBack, sharePhoto_Z;
    private String imageLink;
    private String name, from;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom_image);

        imageZoom = findViewById(R.id.zoomPhoto_PView);
        userNameDisplay = findViewById(R.id.userNameDisplay);
        arrowBack = findViewById(R.id.arrowBack);
        sharePhoto_Z = findViewById(R.id.sharePhoto_Z);


        name = getIntent().getStringExtra("otherName");
        imageLink = getIntent().getStringExtra("imageLink");
        from = getIntent().getStringExtra("from");

        if(!from.equals("profilePix")) {
            sharePhoto_Z.setVisibility(View.GONE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }

        sharePhoto_Z.setOnClickListener(v -> {
            Toast.makeText(this, getText(R.string.preparingPhoto), Toast.LENGTH_SHORT).show();
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(10).withEndAction(() ->
            {
                SharePhotoUtil.downloadImageFromInternetAndShare(this, imageLink, getString(R.string.appInvite) );
            });
        });

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