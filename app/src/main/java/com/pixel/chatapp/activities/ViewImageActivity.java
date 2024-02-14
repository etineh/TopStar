package com.pixel.chatapp.activities;

import static com.pixel.chatapp.home.MainActivity.chatModelList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

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
import com.pixel.chatapp.home.MainActivity;
import com.pixel.chatapp.listeners.ImageListener;
import com.pixel.chatapp.model.MessageModel;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ViewImageActivity extends AppCompatActivity implements ImageListener {

    private static TextView fromWho_TV, timeAndDate_TV;
    private ImageView forwardPhoto_IV, sharePhoto_TV;
    MainActivity mainActivity = new MainActivity();

    public static MessageModel currentModelChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        CustomViewPager viewPager = findViewById(R.id.viewPager_Photo);
        ImageView arrowBack = findViewById(R.id.arrowBack);
        fromWho_TV = findViewById(R.id.from_TV);
        timeAndDate_TV = findViewById(R.id.date_time_TV);
        forwardPhoto_IV = findViewById(R.id.forwardPhoto_IV);
        sharePhoto_TV = findViewById(R.id.sharePhoto_IV);

        // get the details of the items
        List<MessageModel> getChatsList = (List<MessageModel>) getIntent().getSerializableExtra("modelList");
        String photoId = getIntent().getStringExtra("photoId");

        // initialise the adapter
        ViewImageAdapter adapter = new ViewImageAdapter(this, getChatsList);
        // activate the interface listener
        adapter.setImageListener(this);
        // set the viewPager Adapter
        viewPager.setAdapter(adapter);

        // Set the initial position based on the current image you want to display
        int initialPosition = findCurrentImagePosition(getChatsList, photoId);
        viewPager.setCurrentItem(initialPosition);

        //  ========  onClicks
        arrowBack.setOnClickListener(view -> onBackPressed());

        forwardPhoto_IV.setOnClickListener(view -> {
            view.animate().scaleX(1.2f).scaleY(1.2f).setDuration(50).withEndAction(() ->
            {
                chatModelList.add(currentModelChat);
                mainActivity.setForwardChat();

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

    }

    //  ======= methods
    private void sharePhoto(View view) {    // send photo to another app
        view.animate().scaleX(1.2f).scaleY(1.2f).setDuration(50).withEndAction(() ->
        {
                System.out.println("what is path : " + currentModelChat.getPhotoUriOriginal());
            if (currentModelChat != null && currentModelChat.getPhotoUriOriginal() != null) {
                // send app logo in case any error getting the photo uri path
                Uri photoUri = Uri.parse("android.resource://" + this.getPackageName() + "/" + R.drawable.logo);

                if(currentModelChat.getPhotoUriOriginal().startsWith("file:/")) {
                    try {
                        File file = new File(new URI( currentModelChat.getPhotoUriOriginal() ));
                        photoUri = FileProvider.getUriForFile(this, "com.pixel.chatapp.fileprovider", file);
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }

                } else if (currentModelChat.getPhotoUriOriginal().startsWith("content:/")) {
                    photoUri = Uri.parse(currentModelChat.getPhotoUriOriginal());
                }

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/*");
                shareIntent.putExtra(Intent.EXTRA_STREAM, photoUri);
                if(currentModelChat.getMessage() != null)
                    shareIntent.putExtra(Intent.EXTRA_TEXT, currentModelChat.getMessage());

                //  content://media/external/images/media/1000143399  -- from device
                //  file:///storage/emulated/0/Android/data/com.pixel.chatapp/files/Media/Photos/WinnerChat_1707594880207.jpg -- from app storage
                startActivity(Intent.createChooser(shareIntent, getString(R.string.app_name)));
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
        // set from who
        String from = "";
        if(messageModel.getFrom() != null){
            from = messageModel.getFrom().length() > 10 ? messageModel.getFrom().substring(0, 10) : messageModel.getFrom();
        }
        fromWho_TV.setText("From: " + from);

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();

        // Start or bring MainActivity to the front
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(mainActivityIntent);
    }

    @Override
    public void sendImageData(Uri imageUriPath) {

    }

    @Override
    public void onColorChange(int color) {

    }

}