package com.pixel.chatapp.view_controller.photos_video;

import static com.pixel.chatapp.view_controller.MainActivity.chatModelList;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pixel.chatapp.R;
import com.pixel.chatapp.adapters.ViewImageAdapter;
import com.pixel.chatapp.interface_listeners.SuccessAndFailureListener;
import com.pixel.chatapp.interface_listeners.TriggerOnForward;
import com.pixel.chatapp.utilities.Photo_Video_Utils;
import com.pixel.chatapp.utilities.ProfileUtils;
import com.pixel.chatapp.view_controller.MainActivity;
import com.pixel.chatapp.interface_listeners.ImageListener;
import com.pixel.chatapp.dataModel.MessageModel;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ViewImageActivity extends AppCompatActivity implements ImageListener {

    private TextView fromWho_TV, timeAndDate_TV, showChatTV;
    public static TriggerOnForward triggerOnForward;

    ConstraintLayout transContainer, moreOptionContainer, headingContainer;
    TextView saveToGalleryTV,openInChat_TV;

    public static MessageModel currentModelChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        headingContainer = findViewById(R.id.headingContainer_);

        CustomViewPager viewPager = findViewById(R.id.viewPager_Photo);
        ImageView arrowBack = findViewById(R.id.arrowBack);
        fromWho_TV = findViewById(R.id.from_TV);
        timeAndDate_TV = findViewById(R.id.date_time_TV);
        showChatTV = findViewById(R.id.showChat_TV);
        ImageView forwardPhoto_IV = findViewById(R.id.forwardPhoto_IV);
        ImageView sharePhoto_TV = findViewById(R.id.sharePhoto_IV);
        transContainer = findViewById(R.id.transContainer);
        moreOptionContainer = findViewById(R.id.moreOptionContainer);
        saveToGalleryTV = findViewById(R.id.saveToGalleryTV);
        openInChat_TV = findViewById(R.id.openInChat_TV);
        ProgressBar progressBar10 = findViewById(R.id.progressBar10);
        ImageView openOption = findViewById(R.id.openOption);

        // get the details of the items
        List<MessageModel> getChatsList = (List<MessageModel>) getIntent().getSerializableExtra("modelList");
        String photoId = getIntent().getStringExtra("photoId");
        assert getChatsList != null;
        int getCurrentPosition = getIntent().getIntExtra("photoIdPosition", findCurrentImagePosition(getChatsList, photoId));

        // initialise the adapter
        ViewImageAdapter adapter = new ViewImageAdapter(this, getChatsList);
        // activate the interface listener
        adapter.setImageListener(this);
        // set the viewPager Adapter
        viewPager.setAdapter(adapter);

        // Set the initial position based on the current image you want to display
        viewPager.setCurrentItem(getCurrentPosition);

        forwardPhoto_IV.setOnClickListener(view ->
                view.animate().scaleX(1.2f).scaleY(1.2f).setDuration(50).withEndAction(() ->
        {
            chatModelList.add(currentModelChat);
            triggerOnForward.openOnForwardView();

            // Reset the scale
            new Handler().postDelayed(() ->{
                view.setScaleX(1.0f);
                view.setScaleY(1.0f);
            }, 100);

            finish();

        }).start());

        // Method to share the current photo to another app
        sharePhoto_TV.setOnClickListener(this::sharePhoto);

        // open more options
        openOption.setOnClickListener(v -> {
            if(moreOptionContainer.getVisibility() == View.VISIBLE){
                transContainer.setVisibility(View.GONE);
                moreOptionContainer.setVisibility(View.GONE);
            } else {
                transContainer.setVisibility(View.VISIBLE);
                moreOptionContainer.setVisibility(View.VISIBLE);
            }
        });

        transContainer.setOnClickListener(v -> {
            transContainer.setVisibility(View.GONE);
            moreOptionContainer.setVisibility(View.GONE);
        });

        // save the file to phone gallery
        saveToGalleryTV.setOnClickListener(v -> {

            progressBar10.setVisibility(View.VISIBLE);

            Photo_Video_Utils.saveMediaToGallery(this, currentModelChat.getPhotoUriOriginal(), new SuccessAndFailureListener() {
                @Override
                public void onSuccess(String success) {
                    transContainer.setVisibility(View.GONE);
                    moreOptionContainer.setVisibility(View.GONE);
                    progressBar10.setVisibility(View.GONE);
                    Toast.makeText(ViewImageActivity.this, success, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String error) {
                    progressBar10.setVisibility(View.GONE);
                    System.out.println("what is error: ViewImageActivity L140 " + error);
                    Toast.makeText(ViewImageActivity.this, getString(R.string.failToSaveFile), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // open the file to where it's located on chat room position
        openInChat_TV.setOnClickListener(v -> {

            MainActivity.openInChat(currentModelChat.getIdKey(), this);

            finish();

        });

        getOnBackPressedDispatcher().addCallback(callback);

        //  ========  onClicks
        arrowBack.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

    }

    //  ==========     methods

    private void sharePhoto(View view) {    // send photo to another app
        view.animate().scaleX(1.2f).scaleY(1.2f).setDuration(50).withEndAction(() ->
        {
//                System.out.println("what is path : " + currentModelChat.getPhotoUriOriginal());
            if (currentModelChat != null && currentModelChat.getPhotoUriOriginal() != null) {

                Photo_Video_Utils.shareImageUsingContentUri(this, currentModelChat, null, null);

                // Reset the scale
                new Handler().postDelayed(() ->{
                    view.setScaleX(1.0f);
                    view.setScaleY(1.0f);
                }, 100);

            }
        });
    }


    @Override
    public void getCurrentModelChat(MessageModel messageModel, int position) {
        currentModelChat = messageModel;    // to enable user forward the right photo

        // get the contact name or displayed name of other user
        String getFromWho = "From: " + ProfileUtils.getOtherDisplayOrUsername(messageModel.getFromUid(), messageModel.getSenderName());

        fromWho_TV.setText(getFromWho);
        showChatTV.setVisibility(View.VISIBLE);
        showChatTV.setText(messageModel.getMessage());
        // set the date and time
        Date d = new Date(messageModel.getTimeSent()); //complete Data -- Mon 2023 -03 - 06 12.32pm
        DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());
        String time = timeFormatter.format(d);

        String convertDate = String.valueOf(d);
        String getDay = convertDate.substring(8, 10);   // 1 - 30 days
        String getMonth = convertDate.substring(4,7);    // month, Jan - Dec
        String getYear = convertDate.substring(30, 34);  // year

        String dateAndTime = getDay + " " + getMonth + " " + getYear + " | " + time;    // 25 Jan 2024 | 10:05 am
        timeAndDate_TV.setText(dateAndTime);
    }

    private int findCurrentImagePosition(List<MessageModel> chatItems, String photoId) {
        for (int i = 0; i < chatItems.size(); i++) {
            if (chatItems.get(i).getIdKey().equals(photoId)) {
                return i;
            }
        }
        return 0; // Default to the first position if not found
    }


    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed()
        {
            // Start or bring MainActivity to the front
            Intent mainActivityIntent = new Intent(ViewImageActivity.this, MainActivity.class);
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(mainActivityIntent);

            finish();
        }
    };

    @Override
    public void sendImageData(Uri imageUriPath) {

    }

    @Override
    public void onColorChange(int color) {

    }

    @Override
    public void onImageClick() {

        if(headingContainer.getVisibility() == View.VISIBLE) {
            headingContainer.setVisibility(View.GONE);
            showChatTV.setVisibility(View.GONE);
        } else {
            headingContainer.setVisibility(View.VISIBLE);
            showChatTV.setVisibility(View.VISIBLE);
        }

    }

}