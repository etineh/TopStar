package com.pixel.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pixel.chatapp.chats.MessageActivity;
import com.squareup.picasso.Picasso;

//import org.jitsi.meet.sdk.JitsiMeetActivity;
//import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;

public class VideoCallComingOut extends AppCompatActivity {

    TextView tvName;
    CircleImageView imageView, decline;
    String imageUri, otherUid;

    FirebaseUser user;
    DatabaseReference refChecks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call_coming_out);

        tvName = findViewById(R.id.outCallNameTV);
        imageView = findViewById(R.id.circleImageVideoOut);
        decline = findViewById(R.id.ImageCancelCall);

        user = FirebaseAuth.getInstance().getCurrentUser();
        refChecks = FirebaseDatabase.getInstance().getReference("Checks");

        Bundle bundle = getIntent().getExtras();

        if (bundle != null)
        {
            otherUid= getIntent().getStringExtra("uid");
            imageUri = getIntent().getStringExtra("imageUri");

            tvName.setText(getIntent().getStringExtra("otherName"));

            if (imageUri.equals("null")) {
                imageView.setImageResource(R.drawable.person_round);
            }
            else Picasso.get().load(imageUri).into(imageView);

        }
        else Toast.makeText(this, "Data not found", Toast.LENGTH_SHORT).show();
//        Log.i("Check ", "name1 " +receiver_uid);

        //  end call
        decline.setOnClickListener(view -> {
            refChecks.child(user.getUid()).child(otherUid).child("vCall").setValue("off");
            onBackPressed();
        });

//        joinMeeting(otherUid);
        sendInvitation();

        checkResponse();
    }


    // ------------  methods -----------

    private void sendInvitation() {

        refChecks.child(user.getUid()).child(otherUid).child("vCall").setValue("on");

        // turn off my vCall if user don't pick in 20s
        new CountDownTimer(25000, 1000){
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                refChecks.child(user.getUid()).child(otherUid).child("vCall").setValue("off");
//                Toast.makeText(VideoCallComingOut.this, "User not available", Toast.LENGTH_SHORT).show();
                finish();
            }
        }.start();
    }

    private void checkResponse(){

        refChecks.child(otherUid).child(user.getUid()).child("vCallResp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(!snapshot.exists()){
                            refChecks.child(otherUid).child(user.getUid())
                                    .child("vCallResp").setValue("pending");
                        } else {

                            if (snapshot.getValue().equals("yes")){

                                Toast.makeText(VideoCallComingOut.this, "Connecting...", Toast.LENGTH_SHORT).show();
                                joinMeeting();
                                refChecks.child(user.getUid()).child(otherUid).child("vCall").setValue("off");

                            } else{
                                if(snapshot.getValue().equals("no")){
                                    Toast.makeText(VideoCallComingOut.this, "Call decline", Toast.LENGTH_SHORT).show();
                                    refChecks.child(user.getUid()).child(otherUid).child("vCall").setValue("off");
                                    finish();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void joinMeeting(){}
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
}









