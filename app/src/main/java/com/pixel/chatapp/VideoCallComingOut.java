package com.pixel.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
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
import com.pixel.chatapp.activities.SpeakerManager;
import com.pixel.chatapp.all_utils.CallUtils;
import com.pixel.chatapp.home.MainActivity;
import com.pixel.chatapp.interface_listeners.DataModelType;
import com.pixel.chatapp.interface_listeners.SuccessCallBack;
import com.pixel.chatapp.model.DataModel;
import com.pixel.chatapp.webrtc.MyPeerConnectionObserver;
import com.pixel.chatapp.webrtc.WebRTCClient;
import com.squareup.picasso.Picasso;

//import org.jitsi.meet.sdk.JitsiMeetActivity;
//import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.SurfaceViewRenderer;

import java.util.List;
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

    SpeakerManager speakerManager;

    MainRepository mainRepository;

    ValueEventListener valueEventListener;

    CallUtils callUtils;

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
        speakerManager = new SpeakerManager(this);
        callUtils = new CallUtils(this);

        // get all intent data
        myId = getIntent().getStringExtra("myId");
        otherUid = getIntent().getStringExtra("otherUid");
        otherName = getIntent().getStringExtra("otherName");
        myUserName = getIntent().getStringExtra("myUsername");
        answerCall = getIntent().getBooleanExtra("answerCall", false);
        videoCall = getIntent().getBooleanExtra("videoCall", false);

        if(!videoCall){  // set it on loud speaker by default
            speakerManager.setSpeakerphoneOn(true);
        } else {
            speakerManager.setSpeakerphoneOn(false);
        }

        //  toggle speaker
        speakerButton.setOnClickListener(v -> {
            if(onLoudSpeaker){
                speakerManager.setSpeakerphoneOn(true);
                onLoudSpeaker = false;
                // change icon
            } else {
                speakerManager.setSpeakerphoneOn(false);
                onLoudSpeaker = true;
                // change icon
            }
//            speakerManager.toggleSpeakerphone();
        });

        tvName.setText(otherName);

        // check if it is ringing on the other user
        isRinging_TV.setText("Ringing...");


        mainRepository = MainRepository.getInstance();
        mainRepository.initialiseWebRTC(myUserName, getApplicationContext(), otherUid, otherName, myId);

        mainRepository.listener = this;

        mainRepository.initLocalView(local_view);
        mainRepository.initRemoteView(remoteView);

        // start call if I am the one receiving the call
        if(answerCall){
//            System.out.println("what is uid " + myId);
            mainRepository.startCall(otherUid);
            isRinging_TV.setText("connecting...");
        } else {
            // which mean I am the one calling.
            callUtils.startRingingIndicator();
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
//            if(!isSwitching){
//                for (int i = 0; i < 2; i++) {
//                    mainRepository.switchCamera();
//                    if(i == 1){
//                        isSwitching = true;
//                    }
//                }
//            } else {
//            }
            mainRepository.switchCamera();
        });

        // go back to home activity
        arrowBackButton.setOnClickListener(v -> {
            onBackPressed();
        });

        //  end call
        endCall_IV.setOnClickListener(view -> {
            callEnd();
            finish();
        });

        // end call in 30 sec if user did not pick
        new Handler().postDelayed(()-> {
            if(!isConnected){
                callEnd();
            }
        }, 30_000);

        new Handler().postDelayed(() -> {
            observeWhenAnyoneEndCall(myId, otherUid);
        }, 3000);

    }


    // ------------  methods -----------


    private void observeWhenAnyoneEndCall(String myId, String otherUid){

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String data = Objects.requireNonNull(snapshot.getValue()).toString();
                    DataModel dataModel = gson.fromJson(data, DataModel.class);
                    if(dataModel.getType().equals(DataModelType.None)){
                        callEnd();
                        finish();
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

        // Calculate minutes and seconds
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;

        // Format the time count as "00:00"
        String timeText = String.format("%02d:%02d", minutes, remainingSeconds);

        // Update the TextView with the formatted time text
        durationTV.setText(timeText);
    }


    @Override
    public void webrtcConnected() {
        runOnUiThread(()->{
            imageView.setVisibility(View.GONE);
            isRinging_TV.setVisibility(View.GONE);
            tvName.setVisibility(View.GONE);

            Handler handler = new Handler();
            Runnable updateTimeRunnable = new Runnable() {
                @Override
                public void run() {
                    // Update the time/duration TextView here
                    updateTextView();

                    // Post the Runnable again after a delay (1 second)
                    handler.postDelayed(this, 1000);
                }
            };

            // Start the initial post of the Runnable
            handler.post(updateTimeRunnable);

        });
    }

    public void webrtcClosed() {
        callEnd();
        runOnUiThread(this::finish);
    }

    private void callEnd(){
        callUtils.stopRingingIndicator();
        DataModel dataModel = new DataModel(otherUid, otherName, user.getUid(), myUserName, null, DataModelType.None);
        refCall.child(otherUid).child(myId).setValue(gson.toJson(dataModel));
        refCall.child(myId).child(otherUid).setValue(gson.toJson(dataModel));
        mainRepository.endCall();

        MainActivity.run = 0;   // restart to 0 so it can create new instead

        if(valueEventListener != null){
            refCall.child(otherUid).child(myId).removeEventListener(valueEventListener);
        }

    }

    @Override
    public void isConnecting() {
        runOnUiThread(()->{
            isConnected = true;
            callUtils.stopRingingIndicator();
            isRinging_TV.setText("pairing...");
            durationTV.setText("00:00");
        });
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

//    @Override
//    protected void onStop() {
//        super.onStop();
//    }

    //    @Override
//    protected void onPause() {
//        super.onPause();
//        Intent intent = new Intent(this, MainActivity.class);
////        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//
////        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(intent);
////        Toast.makeText(this, "i am called pause", Toast.LENGTH_SHORT).show();
//    }
}









