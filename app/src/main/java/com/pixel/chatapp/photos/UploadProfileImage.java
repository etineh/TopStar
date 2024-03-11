package com.pixel.chatapp.photos;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.pixel.chatapp.Permission.Permission;
import com.pixel.chatapp.R;
import com.pixel.chatapp.constants.AllConstants;
import com.pixel.chatapp.interface_listeners.ImageListener;
import com.pixel.chatapp.side_bar_menu.settings.ProfileActivity;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.UUID;

public class UploadProfileImage extends AppCompatActivity {

    ImageView arrowBack, cropper;
    PhotoView zoomPhoto;
    ImageListener imageListener;
    Button buttonSave;
    Permission permissions = new Permission();

    ActivityResultLauncher<Intent> activityResultLauncherForSelectImage;
    Bitmap selectedImage;
    Uri imageUriPath;
    String oldUriPath;
    Boolean backPress = true;
    ProfileActivity profileActivityInstance = new ProfileActivity();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_profile_image);

        zoomPhoto = findViewById(R.id.zoomPhoto_PV);
        arrowBack = findViewById(R.id.arrowBack_IV);
        buttonSave = findViewById(R.id.buttonSave);
        cropper = findViewById(R.id.crop_IV);

        // initialize the activity launcher for listening
        registerActivityForSelectImage();

        Intent intent = getIntent();
        imageUriPath = Uri.parse(intent.getStringExtra(AllConstants.PICKED_IMAGE_URI_PATH));

        // Get the ImageListener instance from MainActivity
        if (profileActivityInstance.profileActivity instanceof ImageListener) {
            imageListener = (ImageListener) profileActivityInstance.profileActivity;
        }

        activateCrop();

        // open the crop option when the crop icon is clicked
        cropper.setOnClickListener(view -> {
            activateCrop();
            backPress = false; // stop it from going back in-case image wasn't cropped
        });

        buttonSave.setOnClickListener(view -> {
            imageListener.sendImageData(imageUriPath);
            finish();
        });

        arrowBack.setOnClickListener(view -> {
            onBackPressed();
        });

    }

    //  ------- methods --------

    private void activateCrop(){

        String dest_uri_path = new StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString();

        UCrop.of( imageUriPath, Uri.fromFile(new File(getCacheDir(), dest_uri_path)) )
                .withAspectRatio(16, 16)
                .withMaxResultSize(2000, 2000)
                .start(this);
    }

    private void registerActivityForSelectImage() {

        activityResultLauncherForSelectImage = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                    int resultCode = result.getResultCode();
                    Intent data = result.getData();

                    if (resultCode == RESULT_OK && data != null) {

                        imageUriPath = data.getData();
                        activateCrop();

                    } else {
                        // close the activity if no image is selected
                        finish();
                    }
                }
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP && data !=null) {

            imageUriPath = UCrop.getOutput(data);

            if(oldUriPath != null){
                // Create a File object for the file you want to delete
                File fileToDelete = new File(oldUriPath);
                // get the size of the image
//                double fileSizeKB = fileToDelete.length() / 1024.0; // Size in kilobytes
//                double fileSizeMB = fileSizeKB / 1024.0; // Size in megabytes

                // Delete the previous file so not to occupy too much user memory
                boolean isDeleted = fileToDelete.delete();
                if(isDeleted){
                    oldUriPath = imageUriPath.getPath();
//                    username_ET.setText(fileSizeMB + " " + fileSizeKB);
                }

            } else oldUriPath = imageUriPath.getPath();

            zoomPhoto.setImageURI(imageUriPath);

            backPress = false;
//            username_ET.setText(imageUriPath.getPath());

        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            Toast.makeText(this, "Error Occur " + cropError, Toast.LENGTH_SHORT).show();
        } else {
            // go back only once
            if(backPress){
                onBackPressed();
                backPress = false;
            } else {
                backPress = true;
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activityResultLauncherForSelectImage.launch(intent);
    }
}


