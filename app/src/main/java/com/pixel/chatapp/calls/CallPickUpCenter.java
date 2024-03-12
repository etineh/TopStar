package com.pixel.chatapp.calls;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;
import com.pixel.chatapp.R;
import com.pixel.chatapp.all_utils.AnimUtils;
import com.pixel.chatapp.all_utils.CallUtils;
import com.pixel.chatapp.home.MainActivity;
import com.pixel.chatapp.interface_listeners.CallListenerNext;
import com.pixel.chatapp.interface_listeners.CallsListener;
import com.pixel.chatapp.model.CallModel;

public class CallPickUpCenter extends AppCompatActivity implements CallsListener {

    private static CallUtils callUtils;

    private ConstraintLayout callInfoLayout;
    private ImageView answerCallButton, rejectCallButton, icon, arrowBack;
    private String otherUid, myId, otherName, myUserName, callType;
    private TextView whoIsRequestingVideoCall_TV;
    public static CallListenerNext callListenerNext;
    Runnable runnableAnim;
    Handler handlerAnim;
    private MainActivity mainActivity = new MainActivity();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_pickup_center);

        callInfoLayout = findViewById(R.id.callInfoLayout);
        answerCallButton = callInfoLayout.findViewById(R.id.acceptVideoCall_IV);
        rejectCallButton = callInfoLayout.findViewById(R.id.rejectVideoCall_IV);
        whoIsRequestingVideoCall_TV = callInfoLayout.findViewById(R.id.whoIsRequestingVideoCall_TV);
        icon = callInfoLayout.findViewById(R.id.videoOrAudioIcon);
        arrowBack = findViewById(R.id.arrowBackC);

        callUtils = new CallUtils(this);
        MainActivity.callsListener = this;

        // get all intent data
        myId = getIntent().getStringExtra("myId");
        otherUid = getIntent().getStringExtra("otherUid");
        otherName = getIntent().getStringExtra("otherName");
        myUserName = getIntent().getStringExtra("myUsername");
        callType = getIntent().getStringExtra("callType");

        if(callType.equals("audio")) {
            icon.setImageResource(R.drawable.baseline_volume_down_24);

            String callInfo = getString(R.string.audioCall) + " ~ " + otherName;
            whoIsRequestingVideoCall_TV.setText(callInfo);
        } else {
            String callInfo = getString(R.string.videoCall) + " ~ " + otherName;
            whoIsRequestingVideoCall_TV.setText(callInfo);
        }

        handlerAnim = new Handler();
        runnableAnim = () -> {
            icon.startAnimation(AnimUtils.makeTransition());
            handlerAnim.postDelayed(runnableAnim, 1000);
        };
        handlerAnim.postDelayed(runnableAnim, 1000);

        answerCallButton.setOnClickListener(v -> {
            answerCall();
        });

        rejectCallButton.setOnClickListener(v -> {
            rejectCall();
        });

        arrowBack.setOnClickListener(v -> finish() );

    }


    private void answerCall() {
        Intent intent = new Intent(this, CallCenterActivity.class);
        intent.putExtra("otherUid", otherUid);
        intent.putExtra("myId", myId);
        intent.putExtra("otherName", otherName);
        intent.putExtra("myUsername", myUserName);
        intent.putExtra("answerCall", true);
        intent.putExtra("callType", callType);

        mainActivity.stopRingTone();
        startActivity(intent);
        finish();
    }

    private void rejectCall() {
        callListenerNext.busyCall();
        handlerAnim.removeCallbacks(runnableAnim);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handlerAnim.removeCallbacks(runnableAnim);
        finish();
    }

    //  =========== interface

    @Override
    public void myUserEndCall() {
        handlerAnim.removeCallbacks(runnableAnim);
        finish();
    }

    @Override
    public void getRequestVideoCall(CallModel callModel) {

    }

    @Override
    public void acceptVideoCall() {

    }

    @Override
    public void getRejectVideoCallResp(CallModel callModel) {

    }

    @Override
    public void myUserOffCamera() {

    }

}