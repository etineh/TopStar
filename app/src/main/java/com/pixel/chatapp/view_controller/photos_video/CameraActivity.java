package com.pixel.chatapp.view_controller.photos_video;

import static com.pixel.chatapp.utilities.FileUtils.getFileName;
import static com.pixel.chatapp.utilities.FolderUtils.getThumbnailFolder;
import static com.pixel.chatapp.view_controller.MainActivity.chatModelList;
import static com.pixel.chatapp.view_controller.MainActivity.forwardChatUserId;
import static com.pixel.chatapp.view_controller.MainActivity.handlerTyping;
import static com.pixel.chatapp.view_controller.MainActivity.runnableTyping;
import static com.pixel.chatapp.view_controller.MainActivity.selectedUserNames;
import static com.pixel.chatapp.view_controller.MainActivity.sharingPhotoActivated;
import static com.pixel.chatapp.view_controller.MainActivity.user;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.TorchState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pixel.chatapp.permission.AppPermission;
import com.pixel.chatapp.R;
import com.pixel.chatapp.adapters.GalleryAdapter;
import com.pixel.chatapp.constants.Ki;
import com.pixel.chatapp.utilities.FileUtils;
import com.pixel.chatapp.utilities.FolderUtils;
import com.pixel.chatapp.utilities.PhoneUtils;
import com.pixel.chatapp.view_controller.MainActivity;
import com.pixel.chatapp.dataModel.MessageModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {

    ImageCapture imageCapture;
    private ProcessCameraProvider cameraProvider;
    Preview preview;
    private CameraControl cameraControl;

    // Define a CameraInfo object to query camera characteristics
    private CameraInfo cameraInfo;

    PreviewView previewView;

    Recording recording = null;

    VideoCapture<Recorder> videoCapture = null; // Initialize VideoCapture object

    private static AppPermission appPermission = new AppPermission();

    ProgressBar progressBarCamera;
    ImageView arrowBack_IV, switchCamera_IV, flash_IV, addPhoto, scrollToLast_IV;
    ImageView snapSingle_IV, snapMultiple_IV, showSnapPhoto_IV;
    public static TextView photoSelected_TV, sendALLPhoto_TV, onPhoto_TV, onVideo_TV;
    RecyclerView photoRecycler;
    private GalleryAdapter mAdapter;

    private DatabaseReference refMsgFast;

    List<Uri> imageUris;
    public static List<Uri> checkIfUriExist;
    private boolean isOnMultiple;

    private static ActivityResultLauncher<Intent> activityResultLauncherForSelectImage;

    ExecutorService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        progressBarCamera = findViewById(R.id.progressBarCamera);
        arrowBack_IV = findViewById(R.id.arrowBack_IV);
        snapSingle_IV = findViewById(R.id.snapSingle_IV);
        snapMultiple_IV = findViewById(R.id.snapMultiple_IV);
        switchCamera_IV = findViewById(R.id.switchCamera_IV);
        flash_IV = findViewById(R.id.flash_IV);
        addPhoto = findViewById(R.id.addPhotoIV);
        photoRecycler = findViewById(R.id.photoRecyclerView);
        photoSelected_TV = findViewById(R.id.photoSelected_TV);
        sendALLPhoto_TV = findViewById(R.id.sendALLPhoto_TV);
        scrollToLast_IV = findViewById(R.id.scrollToLast_IV);
        onPhoto_TV = findViewById(R.id.onPhoto_IV);
        onVideo_TV = findViewById(R.id.onVideo_IV);
//        showSnapPhoto_IV = findViewById(R.id.showSnapPhoto_IV);

        // Get a reference to the PreviewView in your layout
        previewView = findViewById(R.id.previewView);

        photoRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
//        photoRecycler.setLayoutManager(new GridLayoutManager(this, 3));

        refMsgFast = FirebaseDatabase.getInstance().getReference("MsgFast");

        imageUris = new ArrayList<>();
        checkIfUriExist = new ArrayList<>();

        // Initialize the ImageCapture object
        imageCapture = new ImageCapture.Builder().build();

        // Set up CameraX with lifecycle owner and preview use case
        if(appPermission.isCameraOk(this)){
            setUpCameraX();
        } else {
            appPermission.requestCamera(this);
        }


//        String fileName = getString(R.string.app_name) + "_photo_" + System.currentTimeMillis() +".mp4";
//        File videoFile = new File(FolderUtils.getPhotoFolder(this), fileName);
//// Create a video capture configuration using the builder pattern
//        VideoCaptureConfig videoCaptureConfig = new VideoCaptureConfig.Builder().build();
//
//// Create an output file options specifying the file where the video will be saved
//        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(videoFile).build();
//
//// Set up the video capture use case using the configuration and output file options
//        videoCapture = new VideoCapture.Builder(this).setVideoCaptureConfig(videoCaptureConfig)
//                .setOutputFileOptions(outputFileOptions)
//                .build();


        // Load image URIs from gallery (replace this with your actual code)
        new Handler().postDelayed( () -> {
            imageUris = loadGalleryMedia(10);
            imageUris.add(0, null);
            imageUris.add(null);

            mAdapter = new GalleryAdapter(this, this, imageUris);
            photoRecycler.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();

        }, 2000);

        new Handler().postDelayed(()-> progressBarCamera.setVisibility(View.GONE), 1500);

        // go back
        arrowBack_IV.setOnClickListener(view -> {
            onBackPressed();
        });

        // snap a photo
        snapSingle_IV.setOnClickListener(view -> {
            progressBarCamera.setVisibility(View.VISIBLE);
//            PhoneAccess.vibrateDevice(this);
            isOnMultiple = false;
            view.animate().scaleX(1.2f).scaleY(1.2f).setDuration(20).withEndAction(()->
            {
                captureImage();

                new Handler().postDelayed(()-> {
                    view.setScaleX(1f);
                    view.setScaleY(1f);
                }, 1000);
            });
        });

        // snap multiple photo
        snapMultiple_IV.setOnClickListener(view -> {
            progressBarCamera.setVisibility(View.VISIBLE);
            isOnMultiple = true;
            view.animate().scaleX(1.2f).scaleY(1.2f).setDuration(20).withEndAction(()->
            {
                captureImage();

                new Handler().postDelayed(()-> {
                    view.setScaleX(1f);
                    view.setScaleY(1f);
                    progressBarCamera.setVisibility(View.GONE);
                }, 1000);
            });
        });

        // switch camera
        switchCamera_IV.setOnClickListener(view -> {
            view.animate().scaleX(1.2f).scaleY(1.2f).setDuration(30).withEndAction(()->
            {
                if (cameraProvider != null) {
                    // Create a CameraSelector for the front camera
                    CameraSelector cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                            .build();

                    // Rebind the use cases with the new camera selector
                    cameraProvider.unbindAll();
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture, imageCapture);
                }

                new Handler().postDelayed(()-> {
                    view.setScaleX(1f);
                    view.setScaleY(1f);
                }, 300);

            });
        });

        // switch to photo mood
        onPhoto_TV.setOnClickListener(v -> {
            snapSingle_IV.setVisibility(View.VISIBLE);
            snapMultiple_IV.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Work in progress", Toast.LENGTH_SHORT).show();
        });

        // switch to video mood
        onVideo_TV.setOnClickListener(v -> {

            snapSingle_IV.setVisibility(View.GONE);
            snapMultiple_IV.setVisibility(View.GONE);

            if(appPermission.isRecordingOk(this)){
                captureVideo();
            } else {
                appPermission.requestRecording(this);
            }
//            Toast.makeText(this, "Work in progress", Toast.LENGTH_SHORT).show();
        });

        // scroll to last position of the recycler view
        scrollToLast_IV.setOnClickListener(v -> photoRecycler.scrollToPosition(imageUris.size()-1) );

        // set all the photos selected
        sendALLPhoto_TV.setOnClickListener(v -> {
            progressBarCamera.setVisibility(View.VISIBLE);
            v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(10).withEndAction(()->
            {
                sharingPhotoActivated = true;   // which mean the chatListModel is ready, so don't open gallery at SendImageOrVideoActivity
                startActivity(new Intent(CameraActivity.this, SendImageOrVideoActivity.class));
                finish();
            });
        });

        // go to gallery
        View.OnClickListener openGallery = view -> {
            view.animate().scaleX(1.2f).scaleY(1.2f).setDuration(30).withEndAction(()->
            {
                selectImageFromGallery(this, this);
                new Handler().postDelayed(()-> {
                    view.setScaleX(1f);
                    view.setScaleY(1f);
                }, 500);
            });
        };
        addPhoto.setOnClickListener(openGallery);

        // on flashlight
        flash_IV.setOnClickListener(view -> {
            // Check the current torch state
            boolean isFlashlightEnabled = cameraInfo.getTorchState().getValue() == TorchState.ON;

            // Toggle the flashlight state
            if (isFlashlightEnabled) {
                // Turn off the flashlight
                cameraControl.enableTorch(false);
                view.setBackgroundColor(0);
                flash_IV.setImageResource(R.drawable.baseline_flash_off_24);
            } else {
                // Turn on the flashlight
                cameraControl.enableTorch(true);
                view.setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                flash_IV.setImageResource(R.drawable.baseline_flash_on_24);
            }
        });

        registerActivityForSelectImage();

        service = Executors.newSingleThreadExecutor();

    }

    //  ================    method      ===================

    public void captureVideo() {
        Recording recording1 = recording;
        if (recording1 != null) {
            recording1.stop();
            recording = null;
            return;
        }
        String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault()).format(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, getString(R.string.app_name) + name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/"+getString(R.string.app_name));

        // initialise the path to save the video to
        MediaStoreOutputOptions options = new MediaStoreOutputOptions.Builder(getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues).build();

        // check if audio is permitted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "No audio permission granted", Toast.LENGTH_SHORT).show();
            return;
        }

        recording = videoCapture.getOutput().prepareRecording(CameraActivity.this, options).withAudioEnabled()
                .start(ContextCompat.getMainExecutor(CameraActivity.this), videoRecordEvent -> {
            if (videoRecordEvent instanceof VideoRecordEvent.Start) {
                onVideo_TV .setEnabled(true);
            } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                if (!((VideoRecordEvent.Finalize) videoRecordEvent).hasError()) {


                    sharingPhotoActivated = true;   // which mean the chatListModel is ready, so don't open gallery at SendImageOrVideoActivity

                    Uri videoUri = ((VideoRecordEvent.Finalize) videoRecordEvent).getOutputResults().getOutputUri();
                    // Create a temporary file to store the compressed video

                    String newChatNumId = refMsgFast.child(user.getUid()).push().getKey();
                    String chatId = refMsgFast.child(user.getUid()).push().getKey();  // create an id for each message
                    String size = FileUtils.getEstimateVideoSize(videoUri, CameraActivity.this);   // get the size of the image on compressing
                    String size2 = FileUtils.getFileSize(videoUri, CameraActivity.this);   // get the size of the image
//System.out.println("what is size " + size + " size2 " + size2);
                    MessageModel messageModel = new MessageModel(null, null, user.getUid(), null,
                            System.currentTimeMillis(), chatId, null, newChatNumId,
                            null, 700033, 5, size, null, false, false,
                            null, "record", null, null, videoUri.toString(), videoUri.toString());
                    //  0 is text, 1 is voice note, 2 is photo, 3 is document, 4 is audio (mp3), 5 is video

                    MainActivity.chatModelList.add(messageModel);

                    if(isOnMultiple) {
                        if(imageUris.get(0) == null) imageUris.remove(0);
                        mAdapter.addPhotoUri(videoUri);

                        imageUris.add(0, null);
                        String getCount = String.valueOf(chatModelList.size());
                        photoSelected_TV.setVisibility(View.VISIBLE);
                        sendALLPhoto_TV.setVisibility(View.VISIBLE);
                        photoSelected_TV.setText(getCount);
                        progressBarCamera.setVisibility(View.GONE);
                        mAdapter.notifyDataSetChanged();

                    } else {
                        startActivity(new Intent(CameraActivity.this, SendImageOrVideoActivity.class));
                        finish();
                    }

                } else {
                    recording.close();
                    recording = null;
                    String msg = "Error: " + ((VideoRecordEvent.Finalize) videoRecordEvent).getError();
                    System.out.println(msg);
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private void setUpCameraX(){
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {

                // Obtain the ProcessCameraProvider instance to enable switching camera
                cameraProvider = cameraProviderFuture.get();

                // Set up Preview
                preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                Recorder recorder = new Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                        .build();
                videoCapture = VideoCapture.withOutput(recorder);

                // Select back camera as the default
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                // Bind the use cases to the lifecycle
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture, imageCapture);

                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture, imageCapture);

                // Obtain the CameraControl object
                cameraControl = camera.getCameraControl();


                // Obtain the CameraInfo object after binding use cases
                cameraInfo = camera.getCameraInfo();

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));

    }

    // capture photo, prepare the model into the chatListModel and proceed to sendImageActivity class
    private void captureImage() {
        // Create an output file to save the captured image
        String fileName = getString(R.string.app_name) + "_photo_" + System.currentTimeMillis() +".jpg";
        File photoFile = new File(FolderUtils.getPhotoFolder(this), fileName);

//        // Set up image capture use case
        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        // Capture the image
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {

                        sharingPhotoActivated = true;   // which mean the chatListModel is ready, so don't open gallery at SendImageOrVideoActivity
                        Uri cameraUri = FileProvider.getUriForFile(CameraActivity.this, "com.pixel.chatapp.fileprovider", photoFile);

                        String chatId = refMsgFast.child(user.getUid()).push().getKey();  // create an id for each message
                        String size = FileUtils.getFileSize(cameraUri, CameraActivity.this);   // get the size of the image

                        MessageModel messageModel = new MessageModel(null, null, user.getUid(), null,
                                System.currentTimeMillis(), chatId, null, null,
                                null, 700033, 2, size, null, false, false,
                                null, photoFile.toString(), null, null, cameraUri.toString(), cameraUri.toString());
                        //  0 is text, 1 is voice note, 2 is photo, 3 is document, 4 is audio (mp3), 5 is video

                        MainActivity.chatModelList.add(messageModel);

                        if(isOnMultiple) {
                            if(imageUris.get(0) == null) imageUris.remove(0);
                            mAdapter.addPhotoUri(cameraUri);

                            imageUris.add(0, null);
                            String getCount = String.valueOf(chatModelList.size());
                            photoSelected_TV.setVisibility(View.VISIBLE);
                            sendALLPhoto_TV.setVisibility(View.VISIBLE);
                            photoSelected_TV.setText(getCount);
                            progressBarCamera.setVisibility(View.GONE);
                            PhoneUtils.vibrateDevice(CameraActivity.this, 80);
                            mAdapter.notifyDataSetChanged();

                        } else {
                            startActivity(new Intent(CameraActivity.this, SendImageOrVideoActivity.class));
                            finish();
                        }

                        PhoneUtils.vibrateDevice(CameraActivity.this, 80);
// file:///storage/emulated/0/Android/data/com.pixel.chatapp/files/Media/Photos/Topper_photo_1708539683370.jpg

                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        // Image capture failed, handle error
                        Toast.makeText(CameraActivity.this, "Error capturing image", Toast.LENGTH_SHORT).show();
                        System.out.println("what is cam: " + exception.getMessage());

                    }
                });
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

            // save the low photo or thumbnail to phone app memory
            File saveImageToPhoneUri = new File(getThumbnailFolder(this), getString(R.string.app_name) + "_" + System.currentTimeMillis() + ".jpg");
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
            e.printStackTrace();
            return null;
        }
    }

    private List<Uri> loadGalleryMedia(int limit) {
        List<Uri> mediaUris = new ArrayList<>();

        // Specify the columns you want to retrieve from the MediaStore for both photos and videos
        String[] projection = {MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.MIME_TYPE};

        // Create a selection for both photos and videos
        String selection = MediaStore.MediaColumns.MIME_TYPE + "=? OR " + MediaStore.MediaColumns.MIME_TYPE + "=?";
        String[] selectionArgs = new String[]{"image/jpeg", "video/mp4"};

        // Create a cursor to query the MediaStore for both photos and videos
        Cursor cursor = getContentResolver().query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                MediaStore.MediaColumns.DATE_ADDED + " DESC");

        // Check if the cursor is not null
        if (cursor != null) {
            int count = 0;
            // Iterate over the cursor to extract media URIs
            while (cursor.moveToNext() && count < limit) {
                // Retrieve the media URI from the cursor
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
                Uri mediaUri = Uri.withAppendedPath(MediaStore.Files.getContentUri("external"), String.valueOf(id));
                // Add the media URI to the list
                mediaUris.add(mediaUri);
                count++;
            }
            // Close the cursor after use
            cursor.close();
        }
        return mediaUris;
    }

    public void selectImageFromGallery(Context context, Activity activity){
        if(!appPermission.isStorageOk(context)){
            appPermission.requestStorage(activity);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("*/*"); // Specify all MIME types
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"}); // Specify MIME types for photos and videos
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Allow multiple selection
            activityResultLauncherForSelectImage.launch(intent);

        }
    }

    // perform action when user select a photo from Gallery
    private void registerActivityForSelectImage() {
        activityResultLauncherForSelectImage = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getClipData() != null) {
                            // Handle the result data here
                            int countPhotoPick = data.getClipData().getItemCount();

                            for (int i = 0; i < countPhotoPick; i++) {

                                Uri eachUri = data.getClipData().getItemAt(i).getUri();
                                String fileName = "🌃 " + getFileName(eachUri, this);
                                int type = 2;//  0 is text, 1 is voice note, 2 is photo, 3 is document, 4 is audio (mp3), 5 is video
                                String videoDuration = null;
                                if( FileUtils.isVideoFile(eachUri, this) ){   //  content://media/external/video/media/1000142346
                                    type = 5;
                                    videoDuration = FileUtils.getVideoDuration(eachUri, this);
                                    fileName = "🎥 " + getFileName(eachUri, this);
                                }

                                if( !checkIfUriExist.contains(eachUri) ){   // only add if photo is not on chatList
                                    String chatId = refMsgFast.child(user.getUid()).push().getKey();  // create an id for each message
                                    String size = FileUtils.getFileSize(eachUri, CameraActivity.this);   // get the size of the image

                                    MessageModel messageModel = new MessageModel(null, null, user.getUid(), null,
                                            System.currentTimeMillis(), chatId, null, null,
                                            null, 700033, type, size, null, false, false,
                                            null, fileName, null, videoDuration, eachUri.toString(), eachUri.toString());
                                    //  0 is text, 1 is voice note, 2 is photo, 3 is document, 4 is audio (mp3), 5 is video

                                    MainActivity.chatModelList.add(messageModel);

                                    // in case user select image from gallery that is already added to the chatList
                                    checkIfUriExist.add(eachUri);

                                    if(imageUris.get(0) == null) imageUris.remove(0);
                                    mAdapter.addPhotoUri(eachUri); //  content://media/external/images/media/1000147348

                                    if (i == countPhotoPick-1) {
                                        imageUris.add(0, null);
                                        mAdapter.notifyDataSetChanged();
                                        String getCount = String.valueOf(chatModelList.size());
                                        photoSelected_TV.setVisibility(View.VISIBLE);
                                        sendALLPhoto_TV.setVisibility(View.VISIBLE);
                                        photoSelected_TV.setText(getCount);
                                    }
                                }
                            }

                        }
                    }
                }
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Ki.CAMERA_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            setUpCameraX();

        } else if (requestCode == Ki.STORAGE_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            selectImageFromGallery(this, this);

        } else if (requestCode == Ki.RECORDING_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            captureVideo();

        }
        else {
            Toast.makeText(this, getString(R.string.permission), Toast.LENGTH_SHORT).show();
            if(requestCode == Ki.CAMERA_REQUEST_CODE || requestCode == Ki.RECORDING_REQUEST_CODE){
                finish();
            }
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MainActivity.isSendingFile = false;
        selectedUserNames.clear();
        forwardChatUserId.clear();
        handlerTyping.removeCallbacks(runnableTyping);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        service.shutdown();
    }
}