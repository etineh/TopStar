package com.pixel.chatapp.activities;

import static com.pixel.chatapp.all_utils.FileUtils.saveFileFromContentUriToAppStorage;
import static com.pixel.chatapp.home.MainActivity.appActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.pixel.chatapp.R;
import com.pixel.chatapp.all_utils.FileUtils;
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

public class RedirectHome extends AppCompatActivity {

    MainActivity mainActivity = new MainActivity();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference refMsgFast;

    private SharedPreferences photoShareRef;
    private Gson gson;
    List<String> photoUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.redirect_home);

        refMsgFast = FirebaseDatabase.getInstance().getReference("MsgFast");
        photoShareRef = getSharedPreferences(AllConstants.URI_PREF, Context.MODE_PRIVATE);
        gson = new Gson();
        photoUri = new ArrayList<>();  // save the uri to SharePref to enable delete in case user didn't send the image

        MainActivity.sharingPhotoActivated = true;

//        for image and video and pdf selected > "*/*"
        // for image/s selected > "image/*" or "image/jpeg" or "image/png"
        // for video/s selected > "video/*" or "video/mp4"
        // for audio selected > "audio/*" or "audio/mpeg"
        // for msWord/s elected > "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        // for pdf/s selected > "application/pdf"
        // for doc and pdf > "application/*"

        new Handler().postDelayed(()-> getIntentPhoto(getIntent()), 50);

    }

    //  ======  image methods ====================

    private void getIntentPhoto(Intent intent) {

        List<MessageModel> getUriList = new ArrayList<>();  // when the app is launching the first time

            // check if it contain single image or multiple images
        if (Intent.ACTION_SEND.equals(intent.getAction()) && intent.getType() != null) {    // if single image
            onlyOneFileShared(intent, getUriList);

        } else if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction()) && intent.hasExtra(Intent.EXTRA_STREAM))
        {
            // it contain multiple image below
            containMultipleFiles(intent, getUriList);
        }

    }

    private void onlyOneFileShared(Intent intent, List<MessageModel> getUriList)
    {
        Uri fileUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        String chatId = refMsgFast.child(user.getUid()).push().getKey();  // create an id for each message
        if (MainActivity.chatModelList != null) MainActivity.chatModelList.clear();
        String getText = intent.getStringExtra(Intent.EXTRA_TEXT);

        if (isPhotoFile(fileUri))   // check link if it contains only photo
        {
            handleSinglePhoto(getUriList, chatId, getText, fileUri, true);

        } else if (isAudioFile(fileUri))
        {
            handleSingleAudio(getUriList, chatId, getText, fileUri, true); // for only Audio

        } else if (isVideoFile(fileUri))
        {
            handleSingleVideo(getUriList, chatId, getText, fileUri, true);

        } else
        {   // pdf, msWord, Cdr, photoshop
            handleSingleDocument(getUriList, chatId, getText, fileUri, true);

        }

    }

    private void containMultipleFiles(Intent intent, List<MessageModel> getUriList)
    {
        List<Uri> fileUrisList = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);  // get the images
        String chatId = refMsgFast.child(user.getUid()).push().getKey();  // create an id for each message

        // check if MainActivity is already activity to avoid null error
        if (MainActivity.chatModelList != null) MainActivity.chatModelList.clear();

        // loop through the photos
        for (int i = 0; i < fileUrisList.size(); i++) {    // might from this part later because multiple image only comes from phone Gallery
            int position = i;
            Uri fileUri = fileUrisList.get(position);

            if (isPhotoFile(fileUri))   // check link if it contains only photo
            {
                handleSinglePhoto(getUriList, chatId, null, fileUri, false);

            } else if (isAudioFile(fileUri))
            {
                handleSingleAudio(getUriList, chatId, null, fileUri, false);

            } else if (isVideoFile(fileUri))
            {
                handleSingleVideo(getUriList, chatId, null, fileUri, false);

            } else {
                // pdf, msWord, Cdr, photoshop, apk
                handleSingleDocument(getUriList, chatId, null, fileUri, false);    // for multiple
            }

            new Handler().postDelayed(()->{ // allow file to finish saving to app memory
                finishMultiplePhoto(position, fileUrisList, getUriList);
            }, 500);
        }
    }

    private void handleSinglePhoto(List<MessageModel> getUriList, String chatId, String getText, Uri fileUri, boolean isSingle)
    {
        String fileName = "🌃 " + FileUtils.getFileName(fileUri, this);

        // save the original image to app directory if it is shared from external app like telegram or whatsapp
        if(!fileUri.toString().startsWith("content://media")) {
            fileUri = saveHighQualityImageToStorage(null, fileUri, 70);
        }
        String size = getImageSize(fileUri);

        MessageModel messageModel = new MessageModel(getText, null, user.getUid(), null,
                System.currentTimeMillis(), chatId, null, null, null,
                700033, 2, size, null, false, false,
                null, fileName, null, null, fileUri.toString(), fileUri.toString());

        addPhotoUriToSharePref(fileUri.toString(), fileUri.toString());   // to enable delete if user didn't send again or app went onDestroy()

        if (MainActivity.chatModelList != null)     // call MainActivity
            MainActivity.chatModelList.add(messageModel);

        getUriList.add(messageModel);  // this is when the app launch the first time
        if (isSingle) {
            finishSingleFile(getUriList);
        }

    }

    private void handleSingleAudio(List<MessageModel> getUriList, String chatId, String getText, Uri fileUri, boolean isSingle)
    {
        String fileUriString = fileUri.toString();
        // save the original audio to app directory if it is shared from external app like telegram of whatsapp
        if(!fileUri.toString().startsWith("file:/")){
            fileUriString = saveFileFromContentUriToAppStorage(fileUri, this); // copy to app memory
//            Toast.makeText(this, "preparing audio", Toast.LENGTH_SHORT).show();
        }

        // Convert file size to kilobytes (KB) and megabytes (MB)
        int fileSizeKB = (int) getAudioFileSize(fileUri) / 1024; // Size in kilobytes
        int fileSizeMB = fileSizeKB / 1024; // Size in megabytes

        String fileName = "🔉 "+getFileName(fileUri);

        String formattedDuration = formatDuration((int) getAudioDuration(fileUri));   // don't auto download for other user if size is greater than 500kb
        String sizeOrDuration = fileSizeKB < 500.0 ? formattedDuration : formattedDuration +  " * Audio " + fileSizeMB + " MB";

        MessageModel messageModel = new MessageModel(null, null, user.getUid(), null,
                System.currentTimeMillis(), chatId, null, null, null,
                700033, 4, null, null, false, false,
                null, fileName, fileUriString, sizeOrDuration, null, null);

        addPhotoUriToSharePref(null, fileUriString);   // to enable delete if user didn't send again or app went onDestroy()

        if (MainActivity.chatModelList != null)     // call MainActivity
            MainActivity.chatModelList.add(messageModel);

        getUriList.add(messageModel);  // this is when the app launch the first time

        if (isSingle) {
            finishSingleFile(getUriList);
        }

    }

    private void handleSingleVideo(List<MessageModel> getUriList, String chatId, String getText, Uri fileUri, boolean isSingle)
    {
        String fileUriString = fileUri.toString();
        // save the original audio to app directory if it is shared from external app like telegram of whatsapp
        if(!fileUri.toString().startsWith("file:/")){
            fileUriString = saveFileFromContentUriToAppStorage(fileUri, this); // copy to app memory
//            Toast.makeText(this, "preparing audio", Toast.LENGTH_SHORT).show();
        }

        // Convert file size to kilobytes (KB) and megabytes (MB)
        int fileSizeKB = (int) getAudioFileSize(fileUri) / 1024; // Size in kilobytes
        int fileSizeMB = fileSizeKB / 1024; // Size in megabytes

//        String fileName = "🔉 "+getFileName(fileUri);
        String size = getImageSize(fileUri);

        String formattedDuration = formatDuration((int) getAudioDuration(fileUri));   // don't auto download for other user if size is greater than 500kb
        String sizeOrDuration = fileSizeKB < 500.0 ? formattedDuration : formattedDuration +  " * Audio " + fileSizeMB + " MB";

        MessageModel messageModel = new MessageModel(null, null, user.getUid(), null,
                System.currentTimeMillis(), chatId, null, null, null,
                700033, 5, size, null, false, false,
                null, null, null, null, fileUriString, fileUriString);

        addPhotoUriToSharePref(null, fileUriString);   // to enable delete if user didn't send again or app went onDestroy()

        if (MainActivity.chatModelList != null)     // call MainActivity
            MainActivity.chatModelList.add(messageModel);

        getUriList.add(messageModel);  // this is when the app launch the first time

        if (isSingle) {
            finishSingleFile(getUriList);
        }
    }

    // for pdf, docx, corel draw, apk, photoshop, etc
    private void handleSingleDocument(List<MessageModel> getUriList, String chatId, String getText, Uri fileUri, boolean isSingle)
    {
        String fileUriString = fileUri.toString();
        // save the original audio to app directory if it is shared from external app like telegram of whatsapp
        if(!fileUri.toString().startsWith("file:/")){
            fileUriString = saveFileFromContentUriToAppStorage(fileUri, this); // copy to app memory
//            Toast.makeText(this, "preparing pdf", Toast.LENGTH_SHORT).show();
        }

//  content://media/external/file/1000135295
        String pdfSize = getFileSize(fileUri);  // kb or mb
        String numberOfPages = "";
        String lowUri = null;
        if (isPdfFile(fileUri)) {
            numberOfPages = getNumberOfPdfPages(fileUri) + " " + getString(R.string.page) + " ~ ";
            lowUri = getThumbnailFromPdfUri(fileUri).toString();
        }
        String pdfDetails = getFileName(fileUri) + "\n" + numberOfPages + pdfSize + " ~ document";


        MessageModel messageModel = new MessageModel(getText, null, user.getUid(), null,
                System.currentTimeMillis(), chatId, null, null, null,
                700033, 3, pdfSize, null, false, false,
                null, pdfDetails, null, null, lowUri, fileUriString); // I used emojiOnly for the pdfDetails

        addPhotoUriToSharePref(null, fileUriString);   // to enable delete if user didn't send again or app went onDestroy()

        if (MainActivity.chatModelList != null)     // call MainActivity
            MainActivity.chatModelList.add(messageModel);

        getUriList.add(messageModel);  // this is when the app launch the first time

        if (isSingle) {
            finishSingleFile(getUriList);
        }

    }

    // save each uri to sharePref via gson
    private void addPhotoUriToSharePref(String lowerUri, String highUri){
        if(lowerUri != null)photoUri.add(lowerUri);
        if(highUri != null) photoUri.add(highUri);
        String uriToJson = gson.toJson(photoUri);
        photoShareRef.edit().putString(AllConstants.OLD_URI_LIST, uriToJson).apply();
    }

    private void finishSingleFile(List<MessageModel> getUriList){
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
                    // user is not on main activity, save the activity class it is and bring mainActivity to front,
                    if (!AppLifecycleHandler.isActivityInForeground(MainActivity.class, RedirectHome.class, RedirectHome.this)) {
                    // then return back to the activity when done sending the images
                        bringMainActivityToFront();
                    }
                }
                if(mainActivity != null) mainActivity.setForwardChat();
                finish();
            }, 300);
        }
    }


    private String formatDuration(int durationInMillis) {
        int seconds = durationInMillis / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    private long getAudioFileSize(Uri audioUri) {
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(audioUri, null, null, null, null);
        long size = 0;
        if (cursor != null && cursor.moveToFirst()) {
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            if (sizeIndex != -1) {
                size = cursor.getLong(sizeIndex); // Size in bytes
            }
            cursor.close();
        }
        return size;
    }

    private long getAudioDuration(Uri audioUri) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(getApplicationContext(), audioUri);
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long duration = Long.parseLong(durationStr); // Duration in milliseconds
            retriever.release();
            return duration;
        } catch (IOException e) {
            throw new RuntimeException(e);
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
            File saveImageToPhoneUri = new File(getThumbnailFolder(), getString(R.string.app_name) + System.currentTimeMillis() + "_.jpg");
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
        // create the path where you want to save the image on phone storage
        File saveImageToPhoneUri = new File(getPhotoFolder(), getString(R.string.app_name) + System.currentTimeMillis() + getFileName(imageUri));

        // download the blur thump image to phone
        OutputStream outputStream;
        try {
            // Get the original bitmap from the Uri
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

    private File getPhotoFolder(){
        File appSpecificFolder = new File(getExternalFilesDir(null), AllConstants.ALL_PHOTOS);
        if (!appSpecificFolder.exists()) {
            appSpecificFolder.mkdirs();
        }
        return appSpecificFolder;
    }

    private File getThumbnailFolder(){
        File appSpecificFolder = new File(getExternalFilesDir(null), AllConstants.ALL_THUMBNAIL);
        if (!appSpecificFolder.exists()) {
            appSpecificFolder.mkdirs();
        }
        return appSpecificFolder;
    }

    private File getDocumentFolder(){
        File appSpecificFolder = new File(getExternalFilesDir(null), AllConstants.ALL_DOCUMENTS);
        if (!appSpecificFolder.exists()) {
            appSpecificFolder.mkdirs();
        }
        return appSpecificFolder;
    }

    private File getAudioFolder(){
        File appSpecificFolder = new File(getExternalFilesDir(null), AllConstants.ALL_AUDIO);
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

    private Uri getThumbnailFromPdfUri(Uri uri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
            if (parcelFileDescriptor != null) {
                PdfRenderer renderer = new PdfRenderer(parcelFileDescriptor);
                PdfRenderer.Page page = renderer.openPage(0);

                // Generate thumbnail from the first page
                Bitmap thumbnail = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                page.render(thumbnail, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                // Close the page and renderer
                page.close();
                renderer.close();
                parcelFileDescriptor.close();

                // save the photo to phone app memory
                File saveImageToPhoneUri = new File(getThumbnailFolder(), getString(R.string.app_name) + System.currentTimeMillis() + "_jpg");

                OutputStream outputStream;
                try {
                    outputStream = new FileOutputStream(saveImageToPhoneUri);
                    // save the image to the phone
                    thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                return Uri.fromFile(saveImageToPhoneUri);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean isPdfFile(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        return mimeType != null && mimeType.equals("application/pdf");
    }

    private boolean isMsWordFile(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        return mimeType != null && mimeType.equals("application/msword")
                || mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }

    private boolean isPhotoFile(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        return mimeType != null && mimeType.equals("image/jpeg") || mimeType.equals("image/png") || mimeType.equals("image/*");
    }

    private boolean isAudioFile(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        return mimeType != null && mimeType.equals("audio/mpeg") || mimeType.equals("audio/mp3") || mimeType.equals("audio/*");
    }

    private boolean isVideoFile(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        return mimeType != null && mimeType.equals("video/mp4") || mimeType.equals("video/*");
    }

    private boolean isCdrFile(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        return mimeType != null && mimeType.equals("application/cdr") || mimeType.equals("image/cdr");
    }

    private boolean isPhotoshopFile(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        return mimeType != null && mimeType.equals("image/vnd.adobe.photoshop");
    }

    private String getFileName(Uri uri) {
        String displayName = getString(R.string.app_name);
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        displayName = cursor.getString(index);
                    }
                }
            }
        } else if (uri.getScheme().equals("file")) {
            displayName = uri.getLastPathSegment();
        }
        return displayName;
    }

    private int getNumberOfPdfPages(Uri pdfUri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(pdfUri, "r");
            if (parcelFileDescriptor != null) {
                PdfRenderer renderer = new PdfRenderer(parcelFileDescriptor);
                int pageCount = renderer.getPageCount();
                renderer.close();
                parcelFileDescriptor.close();
                return pageCount;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // get the total file length in kb or mb
    private String getFileSize(Uri fileUri){
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            if (inputStream != null) {
                int fileSizeBytes = inputStream.available();
                int fileSizeKB = fileSizeBytes / 1024; // Size in kilobytes
                int fileSizeMB = fileSizeKB / 1024; // Size in megabytes

                String sizeString = fileSizeKB < 1000.0 ? Math.round(fileSizeKB) + "kB" : Math.round(fileSizeMB) + "MB";

                inputStream.close();

                return sizeString;
            }

        } catch (IOException e) {
            e.printStackTrace();
//            Toast.makeText(this, "Error Occur MA3200: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return getString(R.string.app_name); // Return an empty string or another appropriate default value in case of an error

    }

    //  ======  multiple image methods ====================


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getIntentPhoto(intent);
    }


}