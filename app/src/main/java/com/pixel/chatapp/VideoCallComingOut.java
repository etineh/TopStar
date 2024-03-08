package com.pixel.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import android.util.Rational;
import android.view.View;
import android.widget.ImageView;
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
import com.pixel.chatapp.activities.MainRepository;
import com.pixel.chatapp.all_utils.CallUtils;
import com.pixel.chatapp.home.MainActivity;
import com.pixel.chatapp.interface_listeners.DataModelType;
import com.pixel.chatapp.model.DataModel;

//import org.jitsi.meet.sdk.JitsiMeetActivity;
//import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import org.webrtc.SurfaceViewRenderer;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class VideoCallComingOut extends AppCompatActivity implements MainRepository.Listener{

    private static TextView isRinging_TV;
    private TextView tvName, durationTV;
    public static CircleImageView imageView;
    String otherUid, myId, otherName, myUserName;
    Boolean answerCall, videoCall;
    ImageView mic_button, video_button, endCall_IV, switch_camera_button, arrowBackButton, speakerButton;
    FirebaseUser user;
    DatabaseReference refCall;

    private final Gson gson = new Gson();

    SurfaceViewRenderer remoteView, local_view;

    int seconds = 0;

    private Boolean isCameraMuted = false;
    private Boolean isMicrophoneMuted = false;
    private Boolean isSwitching = false;
    private Boolean isConnected = false;
    private Boolean onLoudSpeaker = false;

    MainRepository mainRepository;

    ValueEventListener valueEventListener;

    CallUtils callUtils;
    private AudioManager audioManager;

    Handler handlerOnline, handlerRinging, handlerDuration;
    Runnable runnableRinging, updateTimeRunnable;

    String connectionStatus, isUserAvailable;

    private PictureInPictureParams.Builder pipBuilder;
    private boolean isInPictureInPictureMode = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call_coming_out);

        tvName = findViewById(R.id.outCallNameTV);
        isRinging_TV = findViewById(R.id.isRinging_TV);
        imageView = findViewById(R.id.circleImageVideoOut);
        mic_button = findViewById(R.id.mic_button);
        video_button = findViewById(R.id.video_button);
        speakerButton = findViewById(R.id.speaker_button);
        switch_camera_button = findViewById(R.id.switch_camera_button);
        endCall_IV = findViewById(R.id.endCall_IV);
        remoteView = findViewById(R.id.remote_view);
        local_view = findViewById(R.id.local_view);
        durationTV = findViewById(R.id.duration_TV);
        arrowBackButton = findViewById(R.id.arrowBack);

        user = FirebaseAuth.getInstance().getCurrentUser();
        refCall = FirebaseDatabase.getInstance().getReference("Calls");
        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        callUtils = new CallUtils(this);

        handlerOnline = new Handler();
        handlerRinging = new Handler();
        handlerDuration = new Handler();

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
        answerCall = getIntent().getBooleanExtra("answerCall", false);
        videoCall = getIntent().getBooleanExtra("videoCall", false);

        setSpeaker();

        new Handler().postDelayed(() -> {
            if(!videoCall){  // set it on loud speaker by default
                setSpeakerphoneOn(true);
            } else {
                setSpeakerphoneOn(false);
            }
        }, 2000);


        //  toggle speaker
        speakerButton.setOnClickListener(v -> {
            setSpeaker();
        });

        tvName.setText(otherName);

        // check if it is ringing on the other user
        isRinging_TV.setText(connectionStatus);

        mainRepository = MainRepository.getInstance();
        mainRepository.initialiseWebRTC(myUserName, getApplicationContext(), otherUid, otherName, myId);

        mainRepository.listener = this;

        // set up the video camera
        mainRepository.initLocalView(local_view);
        mainRepository.initRemoteView(remoteView);

        // send calling signal or answer call
        if(answerCall){
            mainRepository.answerCall(otherUid);     // answer the call
            isRinging_TV.setText(R.string.pairing);
        } else
        {
            // signal the other user that I am calling
            sendSignalToTheTargetUser();

            // start the calling ringback tone
            new Handler().postDelayed(()->{
                callUtils.startRingingIndicator(false);
            }, 1000);

        }

        // mute and un-mute video
        video_button.setOnClickListener(v->{
            if (isCameraMuted){
                video_button.setImageResource(R.drawable.baseline_video_call_24);
            }else {
                video_button.setImageResource(R.drawable.baseline_video_library_24);
            }
            mainRepository.toggleVideo(isCameraMuted);
            isCameraMuted=!isCameraMuted;
        });

        // mute and un-mute audio
        mic_button.setOnClickListener(v -> {
            if (isMicrophoneMuted){
                mic_button.setImageResource(R.drawable.baseline_mic_24);
            }else {
                mic_button.setImageResource(R.drawable.baseline_motion_photos_off_view_24);
            }
            mainRepository.toggleAudio(isMicrophoneMuted);
            isMicrophoneMuted=!isMicrophoneMuted;
        });

        // switch camera back and front
        switch_camera_button.setOnClickListener(v->{
            mainRepository.switchCamera();
        });

        // go back to home activity
        arrowBackButton.setOnClickListener(v -> {
            onBackPressed();
        });

        //  end call
        endCall_IV.setOnClickListener(view -> {
            callEnd(false); // when I end the call myself
        });

        // end call in 30 sec if user did not pick
        runnableRinging = () -> {
            if (!isConnected) { // first check if it rang
                Toast.makeText(this, isUserAvailable, Toast.LENGTH_SHORT).show();
                callEnd(false); // end if user is unavailable
            }
        };
        handlerRinging.postDelayed(runnableRinging, 30_000);

        // observe when user end call or didn't pick
        new Handler().postDelayed(() -> {
            observeWhenAnyoneEndCall(myId, otherUid);
        }, 3000);

    }


    // ------------  methods -----------

    private void sendSignalToTheTargetUser() {

        //  send the signal to other user
        DataModel dataModel1 = new DataModel(otherUid, otherName, user.getUid(), myUserName, null, DataModelType.StartCall, false);
        refCall.child(otherUid).child(myId).setValue(gson.toJson(dataModel1));

    }

    private void observeWhenAnyoneEndCall(String myId, String otherUid){

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String data = Objects.requireNonNull(snapshot.getValue()).toString();
                    DataModel dataModel = gson.fromJson(data, DataModel.class);

                    if(dataModel.getType().equals(DataModelType.Busy))
                    {     // when user change busy my call, it change from StartCall to None
                        Toast.makeText(VideoCallComingOut.this, R.string.userBusy, Toast.LENGTH_SHORT).show();
                        callEnd(false); // end when user is busy

                    } else if(dataModel.getIsRinging())
                    {    // call is ringing on the other user device
                        connectionStatus = getString(R.string.ringing);
                        isRinging_TV.setText(connectionStatus);
                        // when ringing, restart the isConnected runnable to 0
                        handlerRinging.removeCallbacks(runnableRinging);
                        handlerRinging.postDelayed(runnableRinging, 30_000);
                        // indicate user is not picking via toast and end call
                        isUserAvailable = getString(R.string.notPicking);

                    } else if (dataModel.getType().equals(DataModelType.OnAnotherCall))
                    {   // when user is on another call, alert me and end my call.
                        Toast.makeText(VideoCallComingOut.this, R.string.onAnotherCall, Toast.LENGTH_SHORT).show();
                        callEnd(true);  // end when user is on another call
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        refCall.child(otherUid).child(myId).addValueEventListener(valueEventListener);

    }


    @Override
    public void webrtcConnected() {
        MainActivity.activeOnCall = 1;
        runOnUiThread(()->{
            imageView.setVisibility(View.GONE);
            isRinging_TV.setVisibility(View.GONE);
            tvName.setVisibility(View.GONE);

            updateTimeRunnable = new Runnable() {
                @Override
                public void run() {
                    // Update the time/duration TextView here
                    updateTextView();

                    // Post the Runnable again after a delay (1 second)
                    handlerDuration.postDelayed(this, 1000);
                }
            };

            // Start the initial post of the Runnable
            handlerDuration.post(updateTimeRunnable);
        });
    }

    @Override
    public void isConnecting() {
        runOnUiThread(()->{
            isConnected = true;
            callUtils.stopRingingIndicator();
            connectionStatus = getString(R.string.pairing);
            isRinging_TV.setText(connectionStatus);
            durationTV.setText("00:00");
        });
    }


    public void webrtcClosed() {
//        callEnd(false); // end when the connection is closed
//        runOnUiThread(this::finish);
    }

    // Method to update the TextView with time count
    private void updateTextView() {
        // Increment the time count by 1 second
        seconds++;

        // Calculate minutes and seconds
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;

        // Format the time count as "00:00"
        String timeText = String.format("%02d:%02d", minutes, remainingSeconds);

        // Update the TextView with the formatted time text
        durationTV.setText(timeText);
    }

    private void callEnd(Boolean onAnotherCall){
        setSpeakerphoneOn(false);
        callUtils.stopRingingIndicator();
        DataModel dataModel = new DataModel(otherUid, otherName, user.getUid(), myUserName,
                null, DataModelType.None, false);

        if(!onAnotherCall) {    // only change the other user TYPE when he is not on another call
            refCall.child(otherUid).child(myId).setValue(gson.toJson(dataModel));
        }

        refCall.child(myId).child(otherUid).setValue(gson.toJson(dataModel));

        mainRepository.endCall();   // close all camera settings

        MainActivity.run = 0;   // restart to 0 so it can create new instead
        MainActivity.activeOnCall = 0;

        handlerRinging.removeCallbacks(runnableRinging);

        if(updateTimeRunnable != null){
            handlerDuration.removeCallbacks(updateTimeRunnable);
        }

        if(valueEventListener != null){
            refCall.child(otherUid).child(myId).removeEventListener(valueEventListener);
        }

        local_view.setVisibility(View.GONE);
        remoteView.setVisibility(View.GONE);
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

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        // Enter Picture-in-Picture mode when user minimizes the app
        startPictureInPictureMode();
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
            this.isInPictureInPictureMode = isInPictureInPictureMode;
            if (isInPictureInPictureMode) {
                // Hide non-essential UI elements
                local_view.setVisibility(View.GONE);
                arrowBackButton.setVisibility(View.GONE);
                video_button.setVisibility(View.GONE);
                remoteView.setVisibility(View.GONE);

                System.out.println("what is hiding");
            } else {
                // Show UI elements when exiting Picture-in-Picture mode
                local_view.setVisibility(View.VISIBLE);
                arrowBackButton.setVisibility(View.VISIBLE);
                video_button.setVisibility(View.VISIBLE);
                remoteView.setVisibility(View.GONE);
                System.out.println("what is showing");

            }
        } else {
            System.out.println("what is try again");

        }
    }

    private void startPictureInPictureMode() {
        // Enter Picture-in-Picture mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Rational aspectRatio = new Rational(local_view.getWidth(), local_view.getHeight());
            pipBuilder.setAspectRatio(aspectRatio);
            enterPictureInPictureMode(pipBuilder.build());
        }
    }

//    public interface Listener{
//        void webrtcConnected();
//        void webrtcClosed();
//    }

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
//            JitsiMeetActivity.launch(VideoCallComingOut.this, options);
//            finish();
//
//        } catch (Exception e){
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }


    @Override
    public void onBackPressed() {
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(mainActivityIntent);
    }

}









