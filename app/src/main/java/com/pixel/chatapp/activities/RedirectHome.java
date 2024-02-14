package com.pixel.chatapp.activities;

import static com.pixel.chatapp.home.MainActivity.appActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pixel.chatapp.R;
import com.pixel.chatapp.constants.AllConstants;
import com.pixel.chatapp.home.MainActivity;
import com.pixel.chatapp.model.MessageModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RedirectHome extends AppCompatActivity {

    MainActivity mainActivity = new MainActivity();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.redirect_home);

        MainActivity.sharingPhotoActivated = true;

        new Handler().postDelayed(()-> getIntentPhoto(getIntent()), 50);

    }

    //  ======  image methods ====================

    private void getIntentPhoto(Intent intent) {

        List<MessageModel> getUriList = new ArrayList<>();  // when the app is launching the first time

            // check if it contain single image or multiple images
        if (Intent.ACTION_SEND.equals(intent.getAction()) && intent.getType() != null) {    // if single image
            onlyOnePhotoShared(intent, getUriList);

        } else if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction()) && intent.hasExtra(Intent.EXTRA_STREAM))
        {
            // it contain multiple image below
            containMultiplePhoto(intent, getUriList);
        }

    }

    private void onlyOnePhotoShared(Intent intent, List<MessageModel> getUriList)
    {
        if ((Objects.requireNonNull(intent.getType())).contains("image")) {

            if (MainActivity.chatModelList != null) MainActivity.chatModelList.clear();

            Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            String getText = intent.getStringExtra(Intent.EXTRA_TEXT);

            // save the original image to app directory if it is shared from external app like telegram of whatsapp
            assert imageUri != null;
            if(!imageUri.toString().startsWith("content://media")){
                imageUri = saveHighQualityImageToStorage(null, imageUri, 50);
                String size = getImageSize(imageUri);
                // no need to rotate photo since it's coming from other app. Everything must have been done there.
                Uri lowImage = reduceImageSize(null, imageUri, 400);

                MessageModel messageModel = new MessageModel(getText, null, user.getUid(), null,
                        System.currentTimeMillis(), null, null, 8, null,
                        700033, 0, size, null, false, false,
                        null, null, null, null, lowImage.toString(), imageUri.toString());

                if (MainActivity.chatModelList != null)     // call MainActivity
                    MainActivity.chatModelList.add(messageModel);

                getUriList.add(messageModel);  // this is when the app launch the first time
                finishSinglePhoto(getUriList);

            } else // it's coming from phone storage
            {
                Glide.with(this).load(imageUri);
                // save low image via Glide to correct rotation
                Glide.with(this)
                        .asBitmap()
                        .load(imageUri)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                Uri imageUri2 = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                                String size = getImageSize(imageUri2);
                                Uri lowImage = reduceImageSize(resource, null, 500);

                                assert imageUri2 != null;
                                MessageModel messageModel = new MessageModel(getText, null, user.getUid(), null,
                                        System.currentTimeMillis(), null, null, 8, null,
                                        700033, 0, size, null, false, false,
                                        null, null, null, null, lowImage.toString(), imageUri2.toString());

                                if (MainActivity.chatModelList != null) MainActivity.chatModelList.add(messageModel);

                                getUriList.add(messageModel);  // this is when the app launch the first time

                                finishSinglePhoto(getUriList);
                            }

                        });
            }

        }

    }

    private void containMultiplePhoto(Intent intent, List<MessageModel> getUriList)
    {
        List<Uri> imageUrisList = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);  // get the images
        // check if MainActivity is already activity to avoid null error
        if (MainActivity.chatModelList != null) MainActivity.chatModelList.clear();

        // loop through the photos
        for (int i = 0; i < imageUrisList.size(); i++) {
            Uri eachUri = imageUrisList.get(i);
            // save the original image to app directory if it is shared from external app like telegram of whatsapp
            if(!eachUri.toString().startsWith("content://media")){
                eachUri = saveHighQualityImageToStorage(null, eachUri, 50);
                String size = getImageSize(eachUri);
                // no need to rotate photo since it's coming from other app. Everything must have been done there.
                Uri lowImage = reduceImageSize(null, eachUri, 400);

                MessageModel messageModel = new MessageModel(null, user.getUid(), null, null,
                        System.currentTimeMillis(), null, null, 8,
                        null, 700033, 0, size, null, false, false,
                        null, null, null, null, lowImage.toString(), eachUri.toString());

                if (MainActivity.chatModelList != null)     // call MainActivity
                    MainActivity.chatModelList.add(messageModel);

                getUriList.add(messageModel);  // this is when the app launch the first time

                finishMultiplePhoto(i, imageUrisList, getUriList);

            } else {    // it it coming from phone storage
                int position = i;
                Uri eachUri2 = imageUrisList.get(position);
                Glide.with(this).load(eachUri2); // to save the memory first
                // save low image via Glide to correct rotation
                Glide.with(this)
                        .asBitmap()
                        .load(eachUri2)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                String size = getImageSize(eachUri2);   // get the size of the image
                                Uri lowImage = reduceImageSize(resource, null, 500);    // convert it to low quality

                                MessageModel messageModel = new MessageModel(null, null, user.getUid(), null,
                                        System.currentTimeMillis(), null, null, 8,
                                        null, 700033, 0, size, null, false, false,
                                        null, null, null, null, lowImage.toString(), eachUri2.toString());

                                if (MainActivity.chatModelList != null) MainActivity.chatModelList.add(messageModel);

                                getUriList.add(messageModel);  // this is when the app launch the first time

                                finishMultiplePhoto(position, imageUrisList, getUriList);
                            }
                        });
            }

        }
    }

    private void finishSinglePhoto(List<MessageModel> getUriList){
        new Handler().postDelayed(() -> {
            if (MainActivity.chatModelList == null){    // app is launching first time
                onAppFirstLaunch(getUriList);
            } else{
                // it's on another activity
                if (!AppLifecycleHandler.isActivityInForeground(MainActivity.class, RedirectHome.class, this)) {
                    bringMainActivityToFront();
                }
            }
            if(mainActivity != null) mainActivity.setForwardChat();
            finish();
        }, 300);
    }

    private void finishMultiplePhoto(int position, List<Uri> imageUrisList, List<MessageModel> getUriList){
        if(position == imageUrisList.size()-1 ) {
            new Handler().postDelayed(() -> {
                if (MainActivity.chatModelList == null){    // app is lunching first time
                    onAppFirstLaunch(getUriList);
                } else {
                    // user is not on main activity
                    if (!AppLifecycleHandler.isActivityInForeground(MainActivity.class, RedirectHome.class, RedirectHome.this)) {
                        bringMainActivityToFront();
                    }
                }
                if(mainActivity != null) mainActivity.setForwardChat();
                finish();
            }, 300);
        }
    }
    
    public Uri reduceImageSize(Bitmap bitmapImage, Uri originalImageUri, int maxSize) {
        try {
            // Get the original bitmap from the Uri
            Bitmap originalBitmap = bitmapImage == null ? MediaStore.Images.Media.getBitmap(getContentResolver(), originalImageUri) : bitmapImage;

            int width = originalBitmap.getWidth();
            int height = originalBitmap.getHeight();

            float ratio = (float) width / (float) height;
            // reduce the length and width
            if (ratio > 1) {
                width = maxSize;
                height = (int) (width / ratio);
            } else {
                height = maxSize;
                width = (int) (height * ratio);
            }

            // Resize the original bitmap
            Bitmap reduceSize = Bitmap.createScaledBitmap(originalBitmap, width, height, true);

            // Calculate the cropping dimensions based on the smaller dimension
            int lowDimension = Math.min(width, height);

            // Calculate the center coordinates for cropping
            int centerX = reduceSize.getWidth() / 2;
            int centerY = reduceSize.getHeight() / 2;

            // Crop the reduced size image
            Bitmap croppedBitmap = Bitmap.createBitmap(reduceSize, centerX - (lowDimension / 2),
                    centerY - (lowDimension / 2), lowDimension, lowDimension);

            // save the photo to phone app memory
            File saveImageToPhoneUri = new File(MainActivity.getPhotoFolder(this), "WinnerChat_" + System.currentTimeMillis() + ".jpg");
            OutputStream outputStream;
            try {
                outputStream = new FileOutputStream(saveImageToPhoneUri);
                // save the image to the phone
                croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            return Uri.fromFile(saveImageToPhoneUri);

        } catch (IOException e) {
            System.out.println("Error in file ()RH166 " + e.getMessage());
            return null;
        }
    }

    private Uri saveHighQualityImageToStorage(Bitmap bitmapImage, Uri imageUri, int compressionPercentage) {

        String fileName = "WinnerChat_" + System.currentTimeMillis() + ".jpg";
        // create the path where you want to save the image on phone storage
        File saveImageToPhoneUri = new File(MainActivity.getPhotoFolder(this), fileName);

        // download the blur thump image to phone
        OutputStream outputStream;
        try {
            // Get the original bitmap from the Uri
//            Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            Bitmap originalBitmap = bitmapImage == null ? MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri) : bitmapImage;

            // activate the path to the phone storage
            outputStream = new FileOutputStream(saveImageToPhoneUri);

            // save the image to the phone
            originalBitmap.compress(Bitmap.CompressFormat.JPEG, compressionPercentage, outputStream);

        } catch (IOException e) {
            System.out.println("Error in file ()RH285 " + e.getMessage());
            throw new RuntimeException(e);
        }

        return Uri.fromFile(saveImageToPhoneUri);

    }

    // get each of the image size
    private String getImageSize(Uri imageUri) {
        try {
            // Open an InputStream to the image content
            InputStream inputStream = getContentResolver().openInputStream(imageUri);

            if (inputStream != null) {
                // Get the total image length
                int fileSizeBytes = inputStream.available();
                int fileSizeKB = fileSizeBytes / 1024; // Size in kilobytes
                int fileSizeMB = fileSizeKB / 1024; // Size in megabytes

                String sizeString = fileSizeKB < 1000 ? fileSizeKB + " KB" : fileSizeMB + " MB";

                inputStream.close();

                return sizeString;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ""; // Return an empty string or another appropriate default value in case of an error
    }


    // when user is not on main activity but on another activity
    private void bringMainActivityToFront(){

        appActivity = true;     // to enable return to previous activity after sharing the photo

        // bring the main activity to front without creating new instance
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(mainActivityIntent);
    }

    public File getPhotoFolder(){
        File appSpecificFolder = new File(getExternalFilesDir(null), AllConstants.ALL_PHOTOS);
        if (!appSpecificFolder.exists()) {
            appSpecificFolder.mkdirs();
        }
        return appSpecificFolder;
    }

    //   when the app is launching for the first time
    private void onAppFirstLaunch(List<MessageModel> getUriList){
        Intent intent1 = new Intent(this, MainActivity.class);
        intent1.putExtra("photoModel", new ArrayList<>(getUriList));  // send the photoModel
        intent1.putExtra("isSharing", true);  // to enable calling the setForward method
        startActivity(intent1);

    }


    //  ======  multiple image methods ====================


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getIntentPhoto(intent);
    }


}