package com.pixel.chatapp.photos;

import static com.pixel.chatapp.home.MainActivity.chatModelList;
import static com.pixel.chatapp.home.MainActivity.contactNameShareRef;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pixel.chatapp.R;
import com.pixel.chatapp.adapters.ViewImageAdapter;
import com.pixel.chatapp.interface_listeners.TriggerOnForward;
import com.pixel.chatapp.utils.SharePhotoUtil;
import com.pixel.chatapp.home.MainActivity;
import com.pixel.chatapp.interface_listeners.ImageListener;
import com.pixel.chatapp.model.MessageModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ViewImageActivity extends AppCompatActivity implements ImageListener {

    private static TextView fromWho_TV, timeAndDate_TV, showChatTV;
    private ImageView forwardPhoto_IV, sharePhoto_TV, openOption;
    public static TriggerOnForward triggerOnForward;

    ConstraintLayout optionContainer, moreOptionContainer;
    TextView saveToGalleryTV,openInChat_TV;

    public static MessageModel currentModelChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        CustomViewPager viewPager = findViewById(R.id.viewPager_Photo);
        ImageView arrowBack = findViewById(R.id.arrowBack);
        fromWho_TV = findViewById(R.id.from_TV);
        timeAndDate_TV = findViewById(R.id.date_time_TV);
        showChatTV = findViewById(R.id.showChat_TV);
        forwardPhoto_IV = findViewById(R.id.forwardPhoto_IV);
        sharePhoto_TV = findViewById(R.id.sharePhoto_IV);
        optionContainer = findViewById(R.id.optionContainer);
        moreOptionContainer = findViewById(R.id.moreOptionContainer);
        saveToGalleryTV = findViewById(R.id.saveToGalleryTV);
        openInChat_TV = findViewById(R.id.openInChat_TV);
        openOption = findViewById(R.id.openOption);

        // get the details of the items
        List<MessageModel> getChatsList = (List<MessageModel>) getIntent().getSerializableExtra("modelList");
        String photoId = getIntent().getStringExtra("photoId");
        int getCurrentPosition = getIntent().getIntExtra("photoIdPosition", findCurrentImagePosition(getChatsList, photoId));

        // initialise the adapter
        ViewImageAdapter adapter = new ViewImageAdapter(this, getChatsList);
        // activate the interface listener
        adapter.setImageListener(this);
        // set the viewPager Adapter
        viewPager.setAdapter(adapter);

        // Set the initial position based on the current image you want to display
        viewPager.setCurrentItem(getCurrentPosition);

        //  ========  onClicks
        arrowBack.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        forwardPhoto_IV.setOnClickListener(view -> {
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

            }).start();

        });

        sharePhoto_TV.setOnClickListener(view -> {
            // Method to share the current photo to another app
            sharePhoto(view);

        });

        // open more options
        openOption.setOnClickListener(v -> {
            if(moreOptionContainer.getVisibility() == View.VISIBLE){
                optionContainer.setVisibility(View.GONE);
                moreOptionContainer.setVisibility(View.GONE);
            } else {
                optionContainer.setVisibility(View.VISIBLE);
                moreOptionContainer.setVisibility(View.VISIBLE);
            }
        });

        // close option
        optionContainer.setOnClickListener(v -> {
            optionContainer.setVisibility(View.GONE);
            moreOptionContainer.setVisibility(View.GONE);
        });

        // save the file to phone gallery
        saveToGalleryTV.setOnClickListener(v -> {
            Toast.makeText(this, "Work in progress", Toast.LENGTH_SHORT).show();
        });

        // open the file to where it's located on chat room position
        openInChat_TV.setOnClickListener(v -> {
            Toast.makeText(this, "Work in progress", Toast.LENGTH_SHORT).show();
        });

        getOnBackPressedDispatcher().addCallback(callback);

    }

    //  ==========     methods

    private void sharePhoto(View view) {    // send photo to another app
        view.animate().scaleX(1.2f).scaleY(1.2f).setDuration(50).withEndAction(() ->
        {
//                System.out.println("what is path : " + currentModelChat.getPhotoUriOriginal());
            if (currentModelChat != null && currentModelChat.getPhotoUriOriginal() != null) {

                SharePhotoUtil.shareImageUsingContentUri(this, currentModelChat, null, null);

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
        String getFromWho = contactNameShareRef.getString(messageModel.getFromUid(), messageModel.getSenderName());

        fromWho_TV.setText("From: " + getFromWho);

        showChatTV.setText(messageModel.getMessage());

        // set the date and time
        Date d = new Date(messageModel.getTimeSent()); //complete Data -- Mon 2023 -03 - 06 12.32pm
        DateFormat formatter = new SimpleDateFormat("h:mm a");
        String time = formatter.format(d);

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

}