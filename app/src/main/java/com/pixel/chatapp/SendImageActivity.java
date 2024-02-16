package com.pixel.chatapp;

import static com.pixel.chatapp.home.MainActivity.chatModelList;
import static com.pixel.chatapp.home.MainActivity.conTopUserDetails;
import static com.pixel.chatapp.home.MainActivity.deleteOldUriFromAppMemory;
import static com.pixel.chatapp.home.MainActivity.deleteUnusedPhotoFromSharePrefsAndAppMemory;
import static com.pixel.chatapp.home.MainActivity.forwardChatUserId;
import static com.pixel.chatapp.home.MainActivity.handlerTyping;
import static com.pixel.chatapp.home.MainActivity.runnableTyping;
import static com.pixel.chatapp.home.MainActivity.selectedUserNames;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.gson.Gson;
import com.pixel.chatapp.Permission.Permission;
import com.pixel.chatapp.activities.ColorSeekBar;
import com.pixel.chatapp.activities.CustomViewPager;
import com.pixel.chatapp.activities.ImagePainter;
import com.pixel.chatapp.adapters.SendImageAdapter;
import com.pixel.chatapp.adapters.ViewImageAdapter;
import com.pixel.chatapp.constants.AllConstants;
import com.pixel.chatapp.home.MainActivity;
import com.pixel.chatapp.listeners.ImageListener;
import com.pixel.chatapp.model.MessageModel;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiPopup;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SendImageActivity extends AppCompatActivity implements ImageListener {

    ImageView arrowBack, cropper, delete_IV, addPhoto_IV, paintBrush_IV, emoji_IV, oneTimeView, reset_IV;
    private static EditText message_ET;
    TextView allUserNames_TV;
    CircleImageView buttonSend;
    ConstraintLayout topLayerConstraint, brushContainer, typeMessageContainer;
    private static ImageView brushSize25, brushSize50, brushSize75, arrowBackBrush, resetPaint_IV,
            currentBrush, brushDoneButton;
    public static ImageView undoPaint_IV;
    private static int currentColor= 0, currentSize = 10;
    private static ColorSeekBar colorSeekBar;

    ActivityResultLauncher<Intent> activityResultLauncherForSelectImage;

    EmojiPopup popup;

    MainActivity mainActivity = new MainActivity();
    Permission permissions_ =new Permission();

    RecyclerView recyclerPhoto;
    CustomViewPager viewPager;

    SendImageAdapter adapterRecycler;
    ViewImageAdapter adapterViewPager;

    public static MessageModel previousModel;
    int position;
    boolean addNewPhoto;
    private Bitmap currentBitmap;
    public static Bitmap paintedBitmap;
    private ImagePainter imagePainter;
    private ImageView imageView;
    private ProgressBar progressBar;

    private View.OnTouchListener viewTouchListener;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private Map<String, String> firstModelMap;
    private Map<String, String> secondModelMap;
    private Uri tempCropUri;
    private List<String> photoUriToDelete;
    public static List<String> allOldUriList;
    public static List<String> tempUri;

    private DatabaseReference refMsgFast;

    private SharedPreferences unusedPhotoShareRef;
    Gson gson = new Gson();
    private boolean isSendingPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_image);

        arrowBack = findViewById(R.id.arrowBack_IV);
        typeMessageContainer = findViewById(R.id.typePaintContainer);
        message_ET = findViewById(R.id.message_ET);
        emoji_IV = findViewById(R.id.emojiPaint_IV);
        oneTimeView = findViewById(R.id.oneTimeView_IV);
        progressBar = findViewById(R.id.progressBar3);

        // top layer option ids
        topLayerConstraint = findViewById(R.id.topLayerConstraint);
        buttonSend = findViewById(R.id.buttonSend);
        cropper = findViewById(R.id.crop_IV);
        delete_IV = findViewById(R.id.deleteShare);
        addPhoto_IV = findViewById(R.id.addPhotoIV);
        paintBrush_IV = findViewById(R.id.paintBrush_IV);
        reset_IV = findViewById(R.id.resetIV);
        // recyclerView and viewPager ids
        recyclerPhoto = findViewById(R.id.recyclerPhoto);
        viewPager = findViewById(R.id.viewPagerP);
        allUserNames_TV = findViewById(R.id.displayNames_TV);
        // brush paint ids
        brushContainer = findViewById(R.id.brushContainer);
        brushSize25 = findViewById(R.id.brushSize25_IV);
        brushSize50 = findViewById(R.id.brushSize50_IV);
        brushSize75 = findViewById(R.id.brushSize75_IV);
        colorSeekBar = findViewById(R.id.seek_bar_color);
        brushDoneButton = findViewById(R.id.brushDone);
        arrowBackBrush = findViewById(R.id.arrowBackBrush_IV);
        imageView = findViewById(R.id.imageViewPaint);
        resetPaint_IV = findViewById(R.id.resetPaint_IV);
        undoPaint_IV = findViewById(R.id.undoPaint_IV);

        firstModelMap = new HashMap<>();
        secondModelMap = new HashMap<>();
        photoUriToDelete = new ArrayList<>();
        tempUri = new ArrayList<>();
        allOldUriList = new ArrayList<>();

        refMsgFast = FirebaseDatabase.getInstance().getReference("MsgFast");

        //  store each photo cropping or painting uri to enable delete from onCreate when app is onDestroy
        unusedPhotoShareRef = getSharedPreferences(AllConstants.URI_PREF, Context.MODE_PRIVATE);

        recyclerPhoto.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // initialise emoji
        popup = EmojiPopup.Builder.fromRootView( typeMessageContainer ).build(message_ET);

        // show the users name
        allUserNames_TV.setText(selectedUserNames.toString());

        registerActivityForSelectImage();
        
        // pick image from gallery
        if(!MainActivity.sharingPhotoActivated) selectImageFromGallery();

        //  send the image to the firebase and home adapter UI
        buttonSend.setOnClickListener(view -> {

            isSendingPause = true; // Prevent present model photo uri adding to the delete sharePrefs

            MainActivity.sharingPhotoActivated = false; // make it false so as to send the chat

            if(message_ET.length() > 0 && previousModel != null) {
                previousModel.setMessage(message_ET.getText().toString());
            }   // update the model list

            mainActivity.sendSharedChat(this);  // send the chat

            handlerTyping.removeCallbacks(runnableTyping); // remove the network checking runnable

            finish();

            MainActivity.isSendingFile = false;

            deleteUnusedPhotoFromSharePrefsAndAppMemory(this);

        });

        // activate when user is sharing photo from other app or when app is onPause or not active.
        if(MainActivity.sharingPhotoActivated){
            // activate adapter to the recycler view
            adapterRecycler = new SendImageAdapter(this, chatModelList);
            adapterRecycler.setImageListener(this);
            recyclerPhoto.setAdapter(adapterRecycler);

            // initialise the adapter for ViewPager
            adapterViewPager = new ViewImageAdapter(this, chatModelList);
            adapterViewPager.setImageListener(this); // activate the interface listener
            viewPager.setAdapter(adapterViewPager);

            progressBar.setVisibility(View.GONE); // make load bar gone

            // make the recycler visible only when the photo on the list is above 1
            if(chatModelList.size() > 1) {
                recyclerPhoto.setVisibility(View.VISIBLE);
            }

        }

        //  initialise the paint listener picker
        paintPickColorListener();

        //  onClicks    ---- back press
        arrowBack.setOnClickListener(view -> onBackPressed());  // onBackPress
        arrowBackBrush.setOnClickListener(view -> onBackPressed());

        // pop up emoji
        emoji_IV.setOnClickListener(view ->{
            if(popup.isShowing()){
                popup.dismiss();
                emoji_IV.setImageResource(R.drawable.baseline_add_reaction_24);
            } else{
                popup.show();
                emoji_IV.setImageResource(R.drawable.baseline_keyboard_alt_24);
            }
        });

        // hide emoji when the editText is click
        message_ET.setOnClickListener(view -> {
            popup.dismiss();
            emoji_IV.setImageResource(R.drawable.baseline_add_reaction_24);
        });

        // reset to original image
        reset_IV.setOnClickListener(view -> {
            // call cropping method
            new Handler().postDelayed(()-> {
                // first check if uri are not same before adding to the deleteList
                if(!previousModel.getPhotoUriOriginal().equals(firstModelMap.get(previousModel.getIdKey())))
                {
                    Uri originalUri = Uri.parse(firstModelMap.get(previousModel.getIdKey()));
                    // update the adapter and local list with the original photo
                    Glide.with(this)
                            .asBitmap()
                            .load(originalUri)
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    updateNewPhoto(originalUri, resource);    // for reset
                                }
                            });
                    // reset the secondModelMap to the original uri photo
                    secondModelMap.put(previousModel.getIdKey(), originalUri.toString());
                }

            }, 10);

            view.animate().setDuration(500).scaleX(1.2f).scaleY(1.2f).withEndAction(()->
            {
                view.setScaleX(1.0f);
                view.setScaleY(1.0f);
            }).start();

        });

        // crop photo
        cropper.setOnClickListener(view -> {
            view.animate().scaleX(1.3f).scaleY(1.3f).setDuration(30).withEndAction(()->
            {
                // call cropping method
                activateCrop();
                
                new Handler().postDelayed(()-> {
                    view.setScaleX(1.0f);
                    view.setScaleY(1.0f);
                }, 500);
            }).start();

        });

        // delete a photo from the view and list
        delete_IV.setOnClickListener(view -> {
            chatModelList.remove(previousModel);    // remove from list
            mainActivity.deleteFileFromPhoneStorage(previousModel); // delete from app memory
            // update adapters
            adapterRecycler.notifyDataSetChanged();
            adapterViewPager.notifyDataSetChanged();
            
            viewPager.setCurrentItem(0); // set the current position photo if recycler adapter is click

            // make the recycler visible only when the photo on the list is above 1
            if(chatModelList.size() > 1) {
                recyclerPhoto.setVisibility(View.VISIBLE);
            } else {
                recyclerPhoto.setVisibility(View.GONE);
            }

        });

        addPhoto_IV.setOnClickListener(view -> {
            addNewPhoto = true;
            selectImageFromGallery();
        });

        //  reset the paint drawing
        resetPaint_IV.setOnClickListener(view -> {
            view.animate().setDuration(30).scaleX(1.2f).scaleY(1.2f).withEndAction(()->{
                imagePainter.reset(imageView, currentBitmap);
                deleteOldUriFromAppMemory(tempUri); // for reset
                new Handler().postDelayed(()->{
                    view.setScaleX(1.0f);
                    view.setScaleY(1.0f);
                }, 200);
            }).start();
        });

        // undo last paint
        undoPaint_IV.setOnClickListener(view -> {
            view.animate().setDuration(30).scaleX(1.2f).scaleY(1.2f).withEndAction(()->{

                imagePainter.undo(imageView, currentBitmap);

                new Handler().postDelayed(()->{
                    view.setScaleX(1.0f);
                    view.setScaleY(1.0f);
                }, 100);
            }).start();
        });

        brushSize25.setOnClickListener(view -> {
            currentBrush.setBackgroundColor(0); // remove the previous color
            imagePainter.setBrushSize(10);
            if(currentColor != 0) {
                brushSize25.setBackgroundColor(currentColor);
            } else {
                brushSize25.setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
            }
            currentBrush = brushSize25;
            currentSize = 10;
        });

        brushSize50.setOnClickListener(view -> {
            currentBrush.setBackgroundColor(0); // remove the previous color
            imagePainter.setBrushSize(30);
            if(currentColor != 0) {
                brushSize50.setBackgroundColor(currentColor);
            } else {
                brushSize50.setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
            }
            currentBrush = brushSize50;
            currentSize = 30;
        });

        brushSize75.setOnClickListener(view -> {
            currentBrush.setBackgroundColor(0); // remove the previous color
            imagePainter.setBrushSize(50);
            if(currentColor != 0) {
                brushSize75.setBackgroundColor(currentColor);
            } else {
                brushSize75.setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
            }
            currentBrush = brushSize75;
            currentSize = 50;
        });

        // paint or brush a photo
        paintBrush_IV.setOnClickListener(view -> {

            topLayerConstraint.setVisibility(View.GONE);
            typeMessageContainer.setVisibility(View.GONE);
            recyclerPhoto.setVisibility(View.GONE);
            viewPager.setVisibility(View.GONE);

            brushContainer.setVisibility(View.VISIBLE);
            colorSeekBar.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);

            activatePainting(); // send the photo to the imageView and initialise the painting class

            viewTouchListener = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getPointerCount() <= 1) {
                        // Handle other touch events (e.g., painting)
                        imageView.setImageBitmap(paintedBitmap);
                        imagePainter.onTouchEvent(event);
                    }
                    return true;
                }
            };

            imageView.setOnTouchListener(viewTouchListener);

            int fadedOrangeColor = ContextCompat.getColor(this, R.color.transparent_orange);
            undoPaint_IV.setColorFilter(fadedOrangeColor);

        });

        // save the paint photo to phone app memory
        brushDoneButton.setOnClickListener(view -> {

            brushContainer.setVisibility(View.GONE);
            colorSeekBar.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);

            topLayerConstraint.setVisibility(View.VISIBLE);
            typeMessageContainer.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.VISIBLE);

            // make the recycler visible only when the photo on the list is above 1
            if(chatModelList.size() > 1) {
                recyclerPhoto.setVisibility(View.VISIBLE);
            }

            deleteOldUriFromAppMemory(tempUri); // for saving

            //  delete previous photo from phone app storage
            mainActivity.deleteFileFromPhoneStorage(previousModel);

            // upload the new photo to the viewPager and recycler adapter list
            Uri getNewPhotoUri = savePaintPhotoUri(this, paintedBitmap);
            updateNewPhoto(getNewPhotoUri, null);  // for painting

            secondModelMap.put(previousModel.getIdKey(), getNewPhotoUri.toString());    // update the uri map
        });

    }

    public static Uri savePaintPhotoUri(Context context, Bitmap paintBit){

        File savePaintPhotoUri = new File(MainActivity.getPhotoFolder(context), "WinnerChat_" + System.currentTimeMillis() + ".jpg");
        OutputStream outputStream;
        try {
            outputStream = new FileOutputStream(savePaintPhotoUri);
            paintBit.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.fromFile(savePaintPhotoUri);
    }

    //  ====== interface

    @Override
    public void getCurrentModelChat(MessageModel messageModel, int position) {
        viewPager.setCurrentItem(position); // set the current position photo if recycler adapter is click

        adapterRecycler.highLightView(position);
        recyclerPhoto.scrollToPosition(position);

        // update the model list
        if(message_ET.length() > 0 && previousModel != null) {
            previousModel.setMessage(message_ET.getText().toString());
        }
        message_ET.setText(null);

        // get the text if not null
        if(messageModel.getMessage() != null) message_ET.setText(messageModel.getMessage());
        // set the censor at the end of the text
        message_ET.setSelection(message_ET.getText().length());

        // save the original model in-case user reset a crop or paint photo
        if(firstModelMap.get(messageModel.getIdKey()) == null){
            firstModelMap.put(messageModel.getIdKey(), messageModel.getPhotoUriOriginal());
            secondModelMap.put(messageModel.getIdKey(), messageModel.getPhotoUriOriginal());
        }

        // save this current model as the previous in case user scroll to another photo model
        previousModel = messageModel;
        this.position =position;
    }

    @Override
    public void onColorChange(int color) {

    }

    @Override
    public void sendImageData(Uri imageUriPath) {

    }

    //  --------    methods    ----------

    public Bitmap reduceImageDimension(Bitmap image, int maxSize){
        int width = image.getWidth();
        int height = image.getHeight();

        float ratio = (float) width / (float) height;

        if(ratio > 1){
            width = maxSize;
            height = (int) (width/ratio);
        } else{
            height = maxSize;
            width = (int) (height*ratio);
        }

        return Bitmap.createScaledBitmap(image,width,height,true);
    }

    private void paintPickColorListener() {

        colorSeekBar.setImageListener(new ImageListener() {
            @Override
            public void sendImageData(Uri imageUriPath) {

            }

            @Override
            public void getCurrentModelChat(MessageModel messageModel, int position) {

            }

            @Override
            public void onColorChange(int color) {
                currentBrush.setBackgroundColor(color);
                currentColor = color;
                imagePainter.setColor(color);
            }
        });
    }


    private  void selectImageFromGallery(){
        if(!permissions_.isStorageOk(this)){
            permissions_.requestStorage(this);
        } else {
//            Intent intent = new Intent(Intent.EXTRA_ALLOW_MULTIPLE, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*"); // Limit to images only
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Enable multiple selection
            activityResultLauncherForSelectImage.launch(intent);
        }
    }

    // crop photo
    private void activateCrop(){
//        String dest_uri_path = new StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString();
        File imageFileUri = new File(MainActivity.getPhotoFolder(this), "WinnerChat_" + System.currentTimeMillis() + ".jpg");

        UCrop.Options options = new UCrop.Options();

        String getPhotoUri = previousModel.getPhotoUriOriginal();
        if(secondModelMap.get(previousModel.getIdKey()) != null){
            getPhotoUri = secondModelMap.get(previousModel.getIdKey());
        }

        UCrop.of(Uri.parse(getPhotoUri), Uri.fromFile(imageFileUri) )
                .withOptions(options)
                .withAspectRatio(0, 0)
                .useSourceImageAspectRatio()
                .withMaxResultSize(2000, 2000)
                .start(this);

        // in case user didn't perform any action, this will help to delete the file from app memory
        tempCropUri = Uri.fromFile(imageFileUri);
        // add it to sharePref too, and delete from sharePref if use perform action on the photo
        allOldUriList.add(tempCropUri.toString());
        String uriToJson = gson.toJson(allOldUriList);
        unusedPhotoShareRef.edit().putString(AllConstants.OLD_URI_LIST, uriToJson).apply();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP && data !=null)
        {
            Uri croppedImageUri = UCrop.getOutput(data);   // get the image uri path
            // save the new crop photo
            updateNewPhoto(croppedImageUri, null);  //  for cropping

            allOldUriList.remove(tempCropUri.toString());
            // remove the uri from gson - sharePref, since user perform action on the photo
            String uriToJson = gson.toJson(allOldUriList);
            unusedPhotoShareRef.edit().putString(AllConstants.OLD_URI_LIST, uriToJson).apply();

        } else if (requestCode == UCrop.REQUEST_CROP && data ==null)
        {
            // delete the uri from app memory if user doesn't perform any action on the photo
            tempUri.add(tempCropUri.toString());
            deleteOldUriFromAppMemory(tempUri);     //  for cropping

            allOldUriList.remove(tempCropUri.toString());
            // remove the uri from gson - sharePref, since user perform action on the photo
            String uriToJson = gson.toJson(allOldUriList);
            unusedPhotoShareRef.edit().putString(AllConstants.OLD_URI_LIST, uriToJson).apply();

        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            Toast.makeText(this, "Error Occur " + cropError, Toast.LENGTH_SHORT).show();
        }

    }

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

                                int position = i;
                                Uri eachUri = data.getClipData().getItemAt(position).getUri();

                                Glide.with(this).load(eachUri); // to save the memory first
                                // save low image via Glide to correct rotation
                                Glide.with(this)
                                        .asBitmap()
                                        .load(eachUri)
                                        .into(new SimpleTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                                                String size = MainActivity.getFileSize(eachUri, SendImageActivity.this);   // get the size of the image
                                                Uri lowImage = reduceImageSize(resource, null, 500);    // convert it to low quality

                                                String chatId = refMsgFast.child(user.getUid()).push().getKey();  // create an id for each message

                                                MessageModel messageModel = new MessageModel(null, null, user.getUid(), null,
                                                        System.currentTimeMillis(), chatId, null, 8,
                                                        null, 700033, 0, size, null, false, false,
                                                        null, null, null, null, lowImage.toString(), eachUri.toString());

                                                if (MainActivity.chatModelList != null) {
                                                    MainActivity.chatModelList.add(messageModel);
                                                }

                                                adapterRecycler = new SendImageAdapter(SendImageActivity.this, chatModelList);
                                                adapterRecycler.setImageListener(SendImageActivity.this);
                                                recyclerPhoto.setAdapter(adapterRecycler);
                                                adapterRecycler.notifyDataSetChanged();

                                                // initialise the adapter for ViewPager
                                                adapterViewPager = new ViewImageAdapter(SendImageActivity.this, chatModelList);
                                                adapterViewPager.setImageListener(SendImageActivity.this); // activate the interface listener
                                                viewPager.setAdapter(adapterViewPager);
                                                adapterViewPager.notifyDataSetChanged();

                                                // true false when user new photo has been added and make progressBar gone
                                                if(position == countPhotoPick-1) {
                                                    addNewPhoto = false;
                                                    progressBar.setVisibility(View.GONE);
                                                }

                                                // make the recycler visible only when the photo on the list is above 1
                                                if(countPhotoPick > 1 || chatModelList.size() > 1) {
                                                    recyclerPhoto.setVisibility(View.VISIBLE);
                                                }

                                            }
                                        });
                            }
                        }

                    } else {    // check if user is adding new photo to the existing ones
                        if(!addNewPhoto){
                            finish();
                            chatModelList.clear();
                            forwardChatUserId.clear();
                            selectedUserNames.clear();
                        }
                        addNewPhoto = false;
                    }
                }
        );
    }

    // upload the new image from crop or paint to the model list and adapter
    private void updateNewPhoto(Uri newPhotoUri, Bitmap bitmapImage){

        // first add the previous Uri path to delete after all done.
        photoUriToDelete.add(previousModel.getPhotoUriOriginal());
        photoUriToDelete.add(previousModel.getPhotoUriPath());

        // save edited photo uri to sharePref via gson to enable app first launch onCreate to delete the photos in case photo was not deleted
        allOldUriList.addAll(photoUriToDelete);
        String json = gson.toJson(allOldUriList);
        unusedPhotoShareRef.edit().putString(AllConstants.OLD_URI_LIST, json).apply();

        String imageSize = MainActivity.getFileSize(newPhotoUri, this);
        // first check for bitmap image to convert, if not available, then convert the uri
        Uri lowQualityUri = bitmapImage != null ? reduceImageSize(bitmapImage, null, 500) :
                reduceImageSize(null, newPhotoUri, 500);    // save the low quality

        // update the new list with the new crop photos
        previousModel.setPhotoUriOriginal(newPhotoUri.toString());
        previousModel.setPhotoUriPath(lowQualityUri.toString());
        previousModel.setImageSize(imageSize);

        chatModelList.remove(position);
        chatModelList.add(position, previousModel);
        adapterRecycler.notifyItemChanged(position, new Object());

        adapterRecycler.highLightView(position);
        recyclerPhoto.scrollToPosition(position);

        adapterViewPager.notifyDataSetChanged();
//                viewPager.setCurrentItem(position);

    }

    private void activatePainting(){

        Uri photoUri = Uri.parse(previousModel.getPhotoUriOriginal());
        Glide.with(this)
                .asBitmap()
                .load(photoUri)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                        if(resource.getWidth() > 750 ){     //   reduce the image size to enable accurate painting alignment
                            resource = reduceImageDimension(resource, 740);
                        }
                        currentBitmap = resource;   // save the real bitmap in case user reset the paint image

                        paintedBitmap = Bitmap.createBitmap(resource.getWidth(), resource.getHeight(), resource.getConfig());

                        // Initialize canvas with the paintedBitmap
                        Canvas canvas = new Canvas(paintedBitmap);
                        canvas.drawBitmap(resource, 0, 0, null);

                        // Display the painting image on the imageVIew
                        imageView.setImageBitmap(paintedBitmap);

                        // initialise the custom image painter
                        imagePainter = new ImagePainter(paintedBitmap, SendImageActivity.this);
                        imagePainter.setColor(ContextCompat.getColor(SendImageActivity.this, R.color.orange));

                        // set the brush colour and size
                        if(currentBrush != null){
                            if(currentColor != 0) {
                                currentBrush.setBackgroundColor(currentColor);
                                imagePainter.setColor(currentColor);
                            }
                            imagePainter.setBrushSize(currentSize);
                        } else {
                            brushSize25.setBackgroundResource(R.color.orange);
                            currentBrush = brushSize25;
                        }

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
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);

        // Create a shader with the original bitmap
        Shader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        paint.setShader(shader);

        // Create a path with rounded corners
        Path path = new Path();
        path.addRoundRect(rectF, roundPx, roundPx, Path.Direction.CW);

        // Clip the canvas with the path
        canvas.clipPath(path);

        // Draw the original bitmap on the canvas
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }


    // Delete the previous file so not to occupy too much user memory
    private void deleteFile(){
        if(chatModelList != null ){ // delete photo from app memory if not sent and clear list
            for (int i = 0; i < chatModelList.size(); i++){

                MessageModel model = chatModelList.get(i);
                mainActivity.deleteFileFromPhoneStorage(model);

                if(i == chatModelList.size()-1) chatModelList.clear();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == AllConstants.STORAGE_REQUEST_CODE && grantResults.length > 0 
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncherForSelectImage.launch(intent);

        } else {
            Toast.makeText(this, "Go to phone setting and allow photo permission", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    @Override
    public void onBackPressed() {
        if(brushContainer.getVisibility() == View.VISIBLE){

            brushContainer.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
            colorSeekBar.setVisibility(View.GONE);

            topLayerConstraint.setVisibility(View.VISIBLE);
            typeMessageContainer.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.VISIBLE);

            // make the recycler visible only when the photo on the list is above 1
            if(chatModelList.size() > 1) {
                recyclerPhoto.setVisibility(View.VISIBLE);
            }

            deleteOldUriFromAppMemory(tempUri); //  for onBackPress

            new Handler().postDelayed(() -> message_ET.requestFocus(), 300);

        } else {
            if(MainActivity.sharingPhotoActivated){
                finish();
//            super.onBackPressed();
            } else {
                deleteFile();   // for onBackPress
                deleteUnusedPhotoFromSharePrefsAndAppMemory(this);    // for onBackPress
                selectImageFromGallery();   // go back to pick image gallery
                message_ET.clearFocus();
            }
        }

        popup.dismiss();
        emoji_IV.setImageResource(R.drawable.baseline_add_reaction_24);

    }

    @Override
    protected void onPause() {
        super.onPause();
        message_ET.clearFocus();

        if(!isSendingPause){
            for (MessageModel modelChats : chatModelList) {
                try {
                    allOldUriList.add(modelChats.getPhotoUriOriginal());
                    allOldUriList.add(modelChats.getPhotoUriPath());
                    // update the sharePrefs list
                    String convertListToJson = gson.toJson(allOldUriList);
                    unusedPhotoShareRef.edit().putString(AllConstants.OLD_URI_LIST, convertListToJson).apply();
                } catch (Exception e) {}
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Handler().postDelayed(() -> message_ET.requestFocus(), 1000);

        if(!isSendingPause){
            for (MessageModel modelChats : chatModelList) {
                try{
                    allOldUriList.remove(modelChats.getPhotoUriOriginal());
                    allOldUriList.remove(modelChats.getPhotoUriPath());
                    // update the sharePrefs list
                    String convertListToJson = gson.toJson(allOldUriList);
                    unusedPhotoShareRef.edit().putString(AllConstants.OLD_URI_LIST, convertListToJson).apply();

                } catch (Exception e){}
            }
        }

    }

}