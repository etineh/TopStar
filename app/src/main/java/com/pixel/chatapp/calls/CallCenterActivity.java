package com.pixel.chatapp.calls;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Rational;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.pixel.chatapp.R;
import com.pixel.chatapp.utils.AnimUtils;
import com.pixel.chatapp.utils.CallUtils;
import com.pixel.chatapp.utils.PhoneUtils;
import com.pixel.chatapp.constants.AllConstants;
import com.pixel.chatapp.home.MainActivity;
import com.pixel.chatapp.interface_listeners.CallListenerNext;
import com.pixel.chatapp.interface_listeners.CallsListener;
import com.pixel.chatapp.interface_listeners.DataModelType;
import com.pixel.chatapp.model.CallModel;

//import org.jitsi.meet.sdk.JitsiMeetActivity;
//import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import org.webrtc.SurfaceViewRenderer;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class CallCenterActivity extends AppCompatActivity implements MainRepository.Listener, CallsListener {

    private static TextView isRinging_TV;
    private TextView tvName, durationTV;
    public static CircleImageView imageView;
    public static boolean isOnVideo;
    public static boolean isOtherUserCameraOn;
    private String otherUid, myId, otherName, myUserName;
    public static String callType;
    private boolean answerCall, videoCall;
    private static ImageView mic_button, video_button, endCall_IV, switch_camera_button,
            arrowBackButton, speakerButton, addUser;
    private FirebaseUser user;
    private DatabaseReference refCall;

    private final Gson gson = new Gson();

    private ConstraintLayout remoteViewContainer, localViewContainer;
    SurfaceViewRenderer remoteViewBigFrame, localViewSmallFrame, localView2;
    private static ConstraintLayout containerBody;
    private static ConstraintLayout videoRequestLayout;
    public static Handler handlerVibrate = new Handler();
    public static Runnable runnableVibrate;

    private static TextView whoIsRequestingForVideoCall;
    private static ImageView acceptVideoRequest_IV, rejectVideoRequest_IV, videoIcon;
    private static TextView acceptTV, rejectTV;

    private static ConstraintLayout topContainer;
    private static ConstraintLayout controller;
    private static RelativeLayout audioBackground;

    private boolean bigLocalView;

    int seconds = 0;

    private Boolean isCameraMuted = false;
    private Boolean isMicrophoneMuted = false;
    private Boolean isSwitching = false;
    private Boolean isConnected = false;
    private Boolean onBackPress = false;

    MainRepository mainRepository;
    ValueEventListener valueEventListener;

    CallUtils callUtils;
    private AudioManager audioManager;

    Handler handlerOnline, handlerRinging, handlerDuration;
    Runnable runnableRinging, updateTimeRunnable;

    String connectionStatus, isUserAvailable;

    private PictureInPictureParams.Builder pipBuilder;
    private boolean isInPictureInPictureMode = false;
    
    private Runnable delayedEndCallRunnable;
    private Handler handlerDelayEndCall;

    public static CallListenerNext callListenerNext;

    Runnable runnableAnim;
    Handler handlerAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_center);

        containerBody = findViewById(R.id.containerBody);
        topContainer = findViewById(R.id.topContainer);
        audioBackground = findViewById(R.id.audioBackground);

        tvName = findViewById(R.id.outCallNameTV);
        isRinging_TV = findViewById(R.id.isRinging_TV);
        imageView = findViewById(R.id.circleImageVideoOut);

        controller = findViewById(R.id.control_layout);
        mic_button = controller.findViewById(R.id.mic_button);
        video_button = controller.findViewById(R.id.video_button);
        speakerButton = controller.findViewById(R.id.speaker_button);
        switch_camera_button = controller.findViewById(R.id.switch_camera_button);
        endCall_IV = controller.findViewById(R.id.endCall_IV);

        videoRequestLayout = findViewById(R.id.requestVideoCall_layout);
        acceptVideoRequest_IV = videoRequestLayout.findViewById(R.id.acceptVideoCall_IV);
        rejectVideoRequest_IV = videoRequestLayout.findViewById(R.id.rejectVideoCall_IV);
        whoIsRequestingForVideoCall = videoRequestLayout.findViewById(R.id.whoIsRequestingVideoCall_TV);
        acceptTV = videoRequestLayout.findViewById(R.id.acceptTV);
        rejectTV = videoRequestLayout.findViewById(R.id.rejectTV);
        videoIcon = videoRequestLayout.findViewById(R.id.videoOrAudioIcon);

        remoteViewContainer = findViewById(R.id.remoteViewContainer);
        localViewContainer = findViewById(R.id.localViewContainer);
        remoteViewBigFrame = findViewById(R.id.remote_view);
        localViewSmallFrame = findViewById(R.id.local_view);
        localView2 = findViewById(R.id.local_view2);

        MainActivity.callsListener = this;

        durationTV = findViewById(R.id.duration_TV);
        arrowBackButton = findViewById(R.id.arrowBack);
        addUser = findViewById(R.id.addUser_IV);

        user = FirebaseAuth.getInstance().getCurrentUser();
        refCall = FirebaseDatabase.getInstance().getReference("Calls");
        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        callUtils = new CallUtils(this);

        handlerOnline = new Handler();
        handlerRinging = new Handler();
        handlerDuration = new Handler();
        handlerDelayEndCall = new Handler();

        // Initialize Picture-in-Picture builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            pipBuilder = new PictureInPictureParams.Builder();
        }

        connectionStatus = getString(R.string.initialising);
        isUserAvailable = getString(R.string.notAvailable);

        // get all intent data
        myId = getIntent().getStringExtra("myId");
        otherUid = getIntent().getStringExtra("otherUid");
        otherName = getIntent().getStringExtra("otherName");
        myUserName = getIntent().getStringExtra("myUsername");
        callType = getIntent().getStringExtra("callType");
        answerCall = getIntent().getBooleanExtra("answerCall", false);
        new Handler().postDelayed(() -> {
            if(callType == null) finish();
            else if (callType.equals("video")) videoCall = true;
        }, 2000);

        setSpeaker();   // loud or normal speaker

//        addUser.setOnClickListener(v -> {
//            //  send the signal to other user   //frank id
//            CallModel callModel1 = new CallModel("Edcjn9AaPGNwDGjptCDqZIhwNOk1", otherName, user.getUid(), myUserName, null, DataModelType.VideoCall, false);
//            refCall.child("Edcjn9AaPGNwDGjptCDqZIhwNOk1").child(myId).setValue(gson.toJson(callModel1));
//
//            mainRepository = new MainRepository();
//            mainRepository.initialiseWebRTC(myUserName, getApplicationContext(), "Edcjn9AaPGNwDGjptCDqZIhwNOk1", otherName, myId);
//
//            mainRepository.listener = this;
//
//            mainRepository.initLocalView(localViewSmallFrame);
////            mainRepository.initRemoteView(remoteViewBigFrame);  // connect other user video
//
////            answerCallOrSendCallRequest(DataModelType.VideoCall);
//        });

        new Handler().postDelayed(() -> {
            if(!videoCall){  // set it on loud speaker by default
                setSpeakerphoneOn(true);
            } else {
                setSpeakerphoneOn(false);
            }
        }, 2000);

        tvName.setText(otherName);

        // check if it is ringing on the other user
        isRinging_TV.setText(getString(R.string.connect));
        durationTV.setText(connectionStatus);

        mainRepository = MainRepository.getInstance();
        mainRepository.initialiseWebRTC(myUserName, getApplicationContext(), otherUid, otherName, myId);

        mainRepository.listener = this;

        mainRepository.initLocalView(localViewSmallFrame);
        mainRepository.initRemoteView(remoteViewBigFrame);  // connect other user video

        if(callType != null) {
            if(callType.equals("audio")){

                switch_camera_button.setVisibility(View.GONE);

                mainRepository.toggleVideo(false);  // off camera
                isCameraMuted= true;
                audioBackground.setVisibility(View.VISIBLE);

                answerCallOrSendCallRequest(DataModelType.AudioCall);

            } else if(callType.equals("video")) {

                audioBackground.setVisibility(View.GONE);
                switch_camera_button.setVisibility(View.VISIBLE);

                bigLocalView(); // if it is video call

                answerCallOrSendCallRequest(DataModelType.VideoCall);
                isOnVideo = true;
            }
        } else finish();


        //  =======     onClick buttons     ===========

        View.OnClickListener acceptVideo = v -> {

            videoRequestLayout.setVisibility(View.GONE);
            audioBackground.setVisibility(View.GONE);
            switch_camera_button.setVisibility(View.VISIBLE);

            handlerAnim.removeCallbacks(runnableAnim);

            mainRepository.toggleVideo(true);  // turn on camera!
            isCameraMuted= false;

            durationTV.setVisibility(View.VISIBLE);
            isRinging_TV.setVisibility(View.GONE);
            tvName.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);

            // send request to other user (the target user)
            sendSignalToTheTargetUser(DataModelType.AcceptVideo);

            handlerVibrate.removeCallbacks(runnableVibrate);
            MainActivity.goBackToCall = getString(R.string.returnToCall);

            callType = "video";
            isOnVideo = true;

        };
        acceptTV.setOnClickListener(acceptVideo);
        acceptVideoRequest_IV.setOnClickListener(acceptVideo);

        View.OnClickListener rejectVideo = v -> {
            videoRequestLayout.setVisibility(View.GONE);
            audioBackground.setVisibility(View.VISIBLE);
            switch_camera_button.setVisibility(View.GONE);

            handlerAnim.removeCallbacks(runnableAnim);

            mainRepository.toggleVideo(false);  // off camera
            isCameraMuted= true;

            sendSignalToTheTargetUser(DataModelType.RejectVideo);

            handlerVibrate.removeCallbacks(runnableVibrate);
            MainActivity.goBackToCall = getString(R.string.returnToCall);

            //  make my type "none" so I can recall again
            CallModel callModel1 = new CallModel(otherUid, otherName, user.getUid(), myUserName,
                    null, DataModelType.RecallCall, false);
            refCall.child(myId).child(otherUid).setValue(gson.toJson(callModel1));

        };
        rejectTV.setOnClickListener(rejectVideo);
        rejectVideoRequest_IV.setOnClickListener(rejectVideo);

        //  toggle video view
        localViewSmallFrame.setOnClickListener(v -> {
            if(isConnected && callType.equals("video")){
                toggleVideoView(bigLocalView);
                if(bigLocalView) remoteViewBigFrame.setClickable(false);
                else remoteViewBigFrame.setClickable(true);
                bigLocalView = !bigLocalView;

            } else {
                Toast.makeText(this, getString(R.string.waitForUser), Toast.LENGTH_SHORT).show();
            }
        });

        remoteViewBigFrame.setOnClickListener(v -> {
            if(topContainer.getVisibility() == View.VISIBLE){
                controller.setVisibility(View.GONE);
                topContainer.setVisibility(View.GONE);
            } else {
                controller.setVisibility(View.VISIBLE);
                topContainer.setVisibility(View.VISIBLE);
            }
        });

        // toggle speaker for loud or normal speaker
        speakerButton.setOnClickListener(v -> {
            setSpeaker();
        });

        // mute and un-mute video   | request for video call
        video_button.setOnClickListener(v->{

            if(callType.equals("audio") && isConnected)
            {
                // send request to other user (the target user)
                sendSignalToTheTargetUser(DataModelType.VideoCall);

                videoRequestLayout.setVisibility(View.VISIBLE);
                whoIsRequestingForVideoCall.setText(getText(R.string.waitForUser));
                acceptVideoRequest_IV.setVisibility(View.GONE);
                rejectVideoRequest_IV.setVisibility(View.GONE);
                acceptTV.setVisibility(View.GONE);
                rejectTV.setVisibility(View.GONE);

                handlerAnim.postDelayed(runnableAnim, 1000);

                isOnVideo = false;

            } else if (callType.equals("video"))
            {

                if (isCameraMuted){ // Turn on video camera
                    video_button.setImageResource(R.drawable.baseline_video_call_24);
                    sendSignalToTheTargetUser(DataModelType.VideoCall);
                    isOnVideo = true;
                }else { // Turn off video camera
                    video_button.setImageResource(R.drawable.baseline_videocam_off_24);
                    sendSignalToTheTargetUser(DataModelType.AudioCall);
                    isOnVideo = false;

                    if(!isOtherUserCameraOn){   // to go audio mood is other user camera is off
                        myUserOffCamera();
                    }
                }
                mainRepository.toggleVideo(isCameraMuted);
                isCameraMuted=!isCameraMuted;
            } else {
                Toast.makeText(this, getString(R.string.waitForUser), Toast.LENGTH_SHORT).show();
            }

        });

        // mute and un-mute audio
        mic_button.setOnClickListener(v -> {
            if (isMicrophoneMuted){
                mic_button.setImageResource(R.drawable.baseline_mic_24);
            }else {
                mic_button.setImageResource(R.drawable.baseline_mic_off_24);
            }
            mainRepository.toggleAudio(isMicrophoneMuted);
            isMicrophoneMuted=!isMicrophoneMuted;
        });

        // switch camera back and front
        switch_camera_button.setOnClickListener(v->{
            if(callType.equals("video")){
                mainRepository.switchCamera();
            } else {
                Toast.makeText(this, getString(R.string.switchToVideo), Toast.LENGTH_SHORT).show();
            }
        });

        // go back to home activity
        arrowBackButton.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        //  end call
        endCall_IV.setOnClickListener(view -> {
            if(seconds < 1){
                MainActivity.updateCallOrGameChat(AllConstants.type_call, getString(R.string.end)); // update chat UI
            }
            callEnd(false); // when I end the call myself
        });

        // end call in 30 sec if user did not pick
        runnableRinging = () -> {
            if (!isConnected) { // first check if it has rang
                Toast.makeText(this, isUserAvailable, Toast.LENGTH_SHORT).show();
                MainActivity.updateCallOrGameChat(AllConstants.type_call, getString(R.string.noResponse)); // update chat UI

                callEnd(false); // end if user is unavailable
            }
        };
        handlerRinging.postDelayed(runnableRinging, 30_000);

        // observe when user end call or didn't pick
        new Handler().postDelayed(() -> {
            System.out.println("where usss");
            observeWhenAnyoneEndCall(myId, otherUid);
        }, 3000);

        handlerAnim = new Handler();
        runnableAnim = () -> {
            videoIcon.startAnimation(AnimUtils.makeTransition());
            handlerAnim.postDelayed(runnableAnim, 1000);
        };

        getOnBackPressedDispatcher().addCallback(callback);

    }


    // ------------  methods -----------

    private void answerCallOrSendCallRequest(DataModelType type){
        // start call ringback tone or answer call
        if(answerCall){
            mainRepository.answerCall(otherUid);     // answer the call
            isRinging_TV.setText(R.string.pairing);
        } else
        {
            // signal the other user that I am calling
            sendSignalToTheTargetUser(type);

            // start the calling ringback tone
            new Handler().postDelayed(()->{
                callUtils.startRingingIndicator(false);
            }, 1000);

        }

    }

    private void sendSignalToTheTargetUser(DataModelType type) {

        //  send the signal to other user
        CallModel callModel1 = new CallModel(otherUid, otherName, user.getUid(), myUserName, null, type, false);
        refCall.child(otherUid).child(myId).setValue(gson.toJson(callModel1));
    }

    // listening to other user
    private void observeWhenAnyoneEndCall(String myId, String otherUid){

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String data = Objects.requireNonNull(snapshot.getValue()).toString();
                    CallModel callModel = gson.fromJson(data, CallModel.class);

                    if(callModel.getType().equals(DataModelType.Busy))  // when user busy my call, it change from StartCall to None
                    {
                        MainActivity.updateCallOrGameChat(AllConstants.type_call, getString(R.string.busy)); // update chat UI

                        Toast.makeText(CallCenterActivity.this, R.string.userBusy, Toast.LENGTH_SHORT).show();
                        callEnd(false); // end when user is busy
//                        Toast.makeText(CallCenterActivity.this, "busy", Toast.LENGTH_SHORT).show();

                    } else if(callModel.getIsRinging())
                    {    // call is ringing on the other user device
                        connectionStatus = getString(R.string.ringing);
                        isRinging_TV.setText(connectionStatus);
                        durationTV.setText(connectionStatus);

                        MainActivity.updateCallOrGameChat(AllConstants.type_call, getString(R.string.ringing)); // update chat UI

                        // when ringing, restart the isConnected runnable to 0
                        handlerRinging.removeCallbacks(runnableRinging);
                        handlerRinging.postDelayed(runnableRinging, 30_000);
                        // indicate user is not picking via toast and end call
                        isUserAvailable = getString(R.string.notPicking);

                    } else if (callModel.getType().equals(DataModelType.OnAnotherCall))     // other user is on another call
                    {
                        MainActivity.updateCallOrGameChat(AllConstants.type_call, getString(R.string.end)); // update chat UI

                        Toast.makeText(CallCenterActivity.this, R.string.onAnotherCall, Toast.LENGTH_SHORT).show();
                        callEnd(true);  // end when user is on another call

                    } else if (callModel.getType().equals(DataModelType.None))
                    {   // when user is on another call, alert me and end my call.

                        Toast.makeText(CallCenterActivity.this, R.string.callEnd, Toast.LENGTH_SHORT).show();
                        callEnd(true);  // end when user is on another call
//                        Toast.makeText(CallCenterActivity.this, "I end call none", Toast.LENGTH_SHORT).show();

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        refCall.child(otherUid).child(myId).addValueEventListener(valueEventListener);

    }


    // Method to update the TextView with time count
    private void updateTextView() {
        // Increment the time count by 1 second
        seconds++;

        // Format the time count as "00:00"
        String timeText = convertCountToTimeFormat(seconds);
        durationTV.setText(timeText);
        isRinging_TV.setText(timeText);

        // Update the TextView with the formatted time text
        if(callType != null){
            if (callType.equals("video")) {
                isRinging_TV.setVisibility(View.GONE);
            } else {
                durationTV.setVisibility(View.GONE);
            }
        }
        callListenerNext.callConnected(timeText);
    }

    private String convertCountToTimeFormat(int count){
        // Calculate minutes and seconds
        int minutes = count / 60;
        int remainingSeconds = count % 60;

        // Format the time count as "00:00"
        String timeText = String.format("%02d:%02d", minutes, remainingSeconds);
        return timeText;
    }

    private void callEnd(Boolean onAnotherCall){
        setSpeakerphoneOn(false);
        callUtils.stopRingingIndicator();

        if(seconds > 1){
            String timeText = convertCountToTimeFormat(seconds);
            MainActivity.updateCallOrGameChat(AllConstants.type_call, timeText); // update chat UI
        }

        CallModel callModel = new CallModel(otherUid, otherName, user.getUid(), myUserName,
                null, DataModelType.None, false);

        if(!onAnotherCall) {    // only change the other user TYPE when he is not on another call
            refCall.child(otherUid).child(myId).setValue(gson.toJson(callModel));
        }
        refCall.child(myId).child(otherUid).setValue(gson.toJson(callModel));

        mainRepository.endCall();   // close all camera settings
        callListenerNext.endCall();

        handlerRinging.removeCallbacks(runnableRinging);

        // Stop the animation
        handlerAnim.removeCallbacks(runnableAnim);

        if(updateTimeRunnable != null){
            handlerDuration.removeCallbacks(updateTimeRunnable);
        }
        if(runnableVibrate != null ){
            handlerVibrate.removeCallbacks(CallCenterActivity.runnableVibrate);
        }
        if(valueEventListener != null){
            refCall.child(otherUid).child(myId).removeEventListener(valueEventListener);
        }

//        local_view.setVisibility(View.GONE);
//        remoteView.setVisibility(View.GONE);
        finish();
    }

    private void setSpeaker(){
        if(videoCall){  // set it on loud speaker by default
            int colorRes = ContextCompat.getColor(this, R.color.transparent_orange);
            speakerButton.setBackgroundTintList(ColorStateList.valueOf(colorRes));
            setSpeakerphoneOn(true);
            videoCall = false;
        } else {
            int colorRes = ContextCompat.getColor(this, R.color.orange);
            speakerButton.setBackgroundTintList(ColorStateList.valueOf(colorRes));
            setSpeakerphoneOn(false);
            videoCall = true;
        }
    }

    //  toggle loud and normal speaker
    public void setSpeakerphoneOn(boolean on) {
        if (on) {
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            audioManager.setSpeakerphoneOn(true);
        } else {
            audioManager.setSpeakerphoneOn(false);
            audioManager.setMode(AudioManager.MODE_NORMAL);

        }
    }

    private void bigLocalView(){

        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
        );

        // Set constraints programmatically
        layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;

        // Apply the LayoutParams to localViewContainer
        localViewContainer.setLayoutParams(layoutParams);

    }

    private void smallLocalView() {

        // Set elevation for localViewContainer
//        localViewContainer.setElevation(convertDpToPixel(3));

        // Create LayoutParams for localViewContainer
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                getResources().getDimensionPixelSize(R.dimen.local_view_width), // Width
                getResources().getDimensionPixelSize(R.dimen.local_view_height) // Height
        );

        // Set startToStart and endToEnd constraints to the parent
        layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;

        // Set margins programmatically
        int marginStart = getResources().getDimensionPixelSize(R.dimen.local_view_start_margin);
        int marginBottom = getResources().getDimensionPixelSize(R.dimen.local_view_bottom_margin);
        layoutParams.setMargins(marginStart, 0, 0, marginBottom);

        // Apply the LayoutParams to localViewContainer
        localViewContainer.setLayoutParams(layoutParams);

    }


    private void bigRemoteView() {
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
        );

        // Set constraints programmatically
        layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;

        // Apply the LayoutParams to remoteViewContainer
        remoteViewContainer.setLayoutParams(layoutParams);


    }


    private void smallRemoteView(){
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                getResources().getDimensionPixelSize(R.dimen.local_view_width), // Width
                getResources().getDimensionPixelSize(R.dimen.local_view_height) // Height
        );

        // Set startToStart and endToEnd constraints to the parent
        layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;

        // Set margins programmatically
        int marginStart = getResources().getDimensionPixelSize(R.dimen.local_view_start_margin);
        int marginBottom = getResources().getDimensionPixelSize(R.dimen.local_view_bottom_margin);
        layoutParams.setMargins(marginStart, 0, 0, marginBottom);

        // Apply the LayoutParams to localViewContainer
        remoteViewContainer.setLayoutParams(layoutParams);


    }

    private void toggleVideoView(boolean bigLocalView){
        if (!bigLocalView){
            smallLocalView();
            bigRemoteView();
            localViewContainer.setElevation(convertDpToPixel(2));
            remoteViewContainer.setElevation(convertDpToPixel(1));

        } else {
            bigLocalView();
            smallRemoteView();
            localViewContainer.setElevation(convertDpToPixel(1));
            remoteViewContainer.setElevation(convertDpToPixel(2));

        }
    }

    // Method to convert dp to pixels
    private float convertDpToPixel(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }


    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        // Enter Picture-in-Picture mode when user minimizes the app
        if(isConnected && !onBackPress && isOnVideo) {
            setPictureInPictureSize();
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
//            this.isInPictureInPictureMode = isInPictureInPictureMode;
            MainActivity.onPictureMood = isInPictureInPictureMode;

            if (isInPictureInPictureMode) {
                // Hide non-essential UI elements
                controller.setVisibility(View.GONE);
                topContainer.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                tvName.setVisibility(View.GONE);
                isRinging_TV.setVisibility(View.GONE);

            } else {
                // Show UI elements when exiting Picture-in-Picture mode
                controller.setVisibility(View.VISIBLE);
                topContainer.setVisibility(View.VISIBLE);
//                video_button.setVisibility(View.VISIBLE);

                smallLocalView();
                bigRemoteView();
                bigLocalView = true;
                
                handlerDelayEndCall.postDelayed(delayedEndCallRunnable = () -> {
                    callEnd(false);
                }, 2000);

            }
        }
    }

    private void setPictureInPictureSize() {
        // Get screen dimensions
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        // Calculate PiP window size as one-third of the screen size
        int pipWidth = screenWidth / 2;
        int pipHeight = screenHeight / 3;

        // Set PiP window size
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Rational aspectRatio = new Rational(pipWidth, pipHeight);
            PictureInPictureParams.Builder pipBuilder = new PictureInPictureParams.Builder();
            pipBuilder.setAspectRatio(aspectRatio);
            enterPictureInPictureMode(pipBuilder.build());
        }

       resizeVideoViewToPictureInPitureMood(pipWidth, pipHeight);
    }

    private void resizeVideoViewToPictureInPitureMood(int pipWidth, int pipHeight){

        smallLocalView();
        bigRemoteView();
        bigLocalView = true;

        //  set the big frame video view size
        ViewGroup.LayoutParams layoutParamsBig = remoteViewContainer.getLayoutParams();
        layoutParamsBig.width = pipWidth;
        layoutParamsBig.height = pipHeight;
        remoteViewContainer.setLayoutParams(layoutParamsBig);

        ViewGroup.LayoutParams layoutParamsSmall__ = localViewContainer.getLayoutParams();
        layoutParamsSmall__.width = pipWidth/4;
        layoutParamsSmall__.height = pipHeight/4;
        localViewContainer.setLayoutParams(layoutParamsSmall__);

        // align small view to fit in the screen parent size
        ConstraintLayout.LayoutParams localParams = (ConstraintLayout.LayoutParams) localViewContainer.getLayoutParams();
        localParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID; // Align to parent bottom
        localParams.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID; // Align to parent left
        localParams.leftMargin = 2;
        localParams.bottomMargin = 2;
        localViewContainer.setLayoutParams(localParams);

    }


//    public interface Listener{
//        void webrtcConnected();
//        void webrtcClosed();
//    }

    @Override
    protected void onResume() {
        super.onResume();
        handlerDelayEndCall.removeCallbacks(delayedEndCallRunnable);
        onBackPress = false;
        if(isConnected) callListenerNext.returnToCallLayoutVisibilty();
    }


//    {
//        try {
//            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
//                    .setServerURL(new URL("https://meet.jit.si"))
//                    .setRoom(user.getUid())
////                    .setAudioMuted(false)
////                    .setVideoMuted(true)
////                    .setAudioOnly(true)
//                    .setConfigOverride("requireDisplayName", "Winner")
//                    .build();
//            JitsiMeetActivity.launch(CallCenterActivity.this, options);
//            finish();
//
//        } catch (Exception e){
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }


    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            onBackPress = true;
            Intent mainActivityIntent = new Intent(CallCenterActivity.this, MainActivity.class);
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(mainActivityIntent);
        }
    };


    //  =====   interface

    @Override
    public void webrtcConnected() {
        // set up the video camera
        runOnUiThread(()->{

            if(callType.equals("video")){
                imageView.setVisibility(View.GONE);
                tvName.setVisibility(View.GONE);
                isRinging_TV.setVisibility(View.GONE);
            }

            // update time count
            updateTimeRunnable = () -> {
                // Update the time/duration TextView here
                updateTextView();

                // Post the Runnable again after a delay (1 second)
                handlerDuration.postDelayed(updateTimeRunnable, 1000);
            };

            // Start the initial post of the Runnable
            handlerDuration.post(updateTimeRunnable);

//            toggleVideoView(false);
            bigLocalView = true;

            MainActivity.updateCallOrGameChat(AllConstants.type_call, getString(R.string.ongoingCall));

        });
    }

    @Override
    public void isConnecting() {
        runOnUiThread(()->{

            callListenerNext.isConnecting(getString(R.string.returnToCall));
            
            isConnected = true;
            callUtils.stopRingingIndicator();
            connectionStatus = getString(R.string.pairing);
            isRinging_TV.setText(connectionStatus);
            durationTV.setText("00:00");

//            toggleVideoView(false);
            bigLocalView = true;
            smallLocalView();

            MainActivity.updateCallOrGameChat(AllConstants.type_call, getString(R.string.pairing));
        });
    }


    public void webrtcClosed() {
//        callEnd(false); // end when the connection is closed
//        runOnUiThread(this::finish);
    }

    @Override
    public void getRequestVideoCall(CallModel callModel) {
        videoRequestLayout.setVisibility(View.VISIBLE);
        acceptVideoRequest_IV.setVisibility(View.VISIBLE);
        rejectVideoRequest_IV.setVisibility(View.VISIBLE);
        acceptTV.setVisibility(View.VISIBLE);
        rejectTV.setVisibility(View.VISIBLE);

        String whoRequests = callModel.getSenderName() + " " + getString(R.string.requestForVideoCall);
        whoIsRequestingForVideoCall.setText(whoRequests);
        Toast.makeText(this, whoRequests, Toast.LENGTH_SHORT).show();

        if(runnableVibrate != null){
            handlerVibrate.removeCallbacks(CallCenterActivity.runnableVibrate);
        }

        CallCenterActivity.runnableVibrate = () -> {
            PhoneUtils.vibrateDevice(CallCenterActivity.this, 80);
            handlerVibrate.postDelayed(runnableVibrate, 1000);
        };

        handlerVibrate.postDelayed(runnableVibrate, 1000);

        handlerAnim.postDelayed(runnableAnim, 1000);
    }

    @Override
    public void acceptVideoCall() { // notify me when user accept my video call

        audioBackground.setVisibility(View.GONE);
        switch_camera_button.setVisibility(View.VISIBLE);

        mainRepository.toggleVideo(true);  // turn on!
        isCameraMuted= false;

        durationTV.setVisibility(View.VISIBLE);

        videoRequestLayout.setVisibility(View.GONE);
        isRinging_TV.setVisibility(View.GONE);
        tvName.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);
        handlerVibrate.removeCallbacks(CallCenterActivity.runnableVibrate);

        // Stop the animation
        handlerAnim.removeCallbacks(runnableAnim);

        callType = "video";
        isOnVideo = true;
        isOtherUserCameraOn = true;

    }

    @Override
    public void getRejectVideoCallResp(CallModel callModel) {
        String reject = callModel.getSenderName() + " " + getString(R.string.rejectVideoCall);
        Toast.makeText(this, reject, Toast.LENGTH_SHORT).show();
        videoRequestLayout.setVisibility(View.GONE);

        // Stop the animation
        handlerAnim.removeCallbacks(runnableAnim);
    }

    @Override
    public void myUserOffCamera() {
        audioBackground.setVisibility(View.VISIBLE);
        isRinging_TV.setVisibility(View.VISIBLE);
        tvName.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.VISIBLE);

        callType = "audio";
        isOnVideo = false;
    }

    @Override
    public void myUserEndCall() {

    }


}









