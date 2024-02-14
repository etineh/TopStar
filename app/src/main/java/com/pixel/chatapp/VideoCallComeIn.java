package com.pixel.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
//
//import org.jitsi.meet.sdk.JitsiMeetActivity;
//import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;

public class VideoCallComeIn extends AppCompatActivity {

    TextView tvName;
    CircleImageView imageView, decline, answerCall;
    String imageUri, otherUid;

    FirebaseUser user;
    DatabaseReference refChecks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call_come_in);

        tvName = findViewById(R.id.outCallNameTV2);
        imageView = findViewById(R.id.circleImageVideoIn);
        decline = findViewById(R.id.ImageCancel2);
        answerCall = findViewById(R.id.circleImageAnswer);

        user = FirebaseAuth.getInstance().getCurrentUser();
        refChecks = FirebaseDatabase.getInstance().getReference("Checks");

        otherUid = getIntent().getStringExtra("otherUid");
        imageUri = getIntent().getStringExtra("imageUri");

        if (imageUri.equals("null")) {
            imageView.setImageResource(R.drawable.person_round);
        }
        else Picasso.get().load(imageUri).into(imageView);

        decline.setOnClickListener(view -> {
            refChecks.child(user.getUid()).child(otherUid).child("vCallResp").setValue("no");
            Toast.makeText(VideoCallComeIn.this, "You decline the call", Toast.LENGTH_SHORT).show();
            finish();
        });

        answerCall.setOnClickListener(view -> {
            refChecks.child(user.getUid()).child(otherUid).child("vCallResp").setValue("yes");
            joinCall();
        });

        userEndCall();

    }



    //      ------------        methods        --------------

    //  Join call when I pick
    private void joinCall(){}
//    {
//        try {
//            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
//                    .setServerURL(new URL("https://meet.jit.si"))
//                    .setRoom(otherUid)
//                    .build();
//
//            JitsiMeetActivity.launch(VideoCallComeIn.this, options);
//            finish();
//
//        } catch (Exception e){
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
////        Toast.makeText(VideoCallComeIn.this, "You have accepted the call", Toast.LENGTH_SHORT).show();
//
//    }

    // Check if other user end their call
    public void userEndCall(){

        refChecks.child(otherUid).child(user.getUid()).child("vCall")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                       if(snapshot.getValue().equals("off")){
                           finish();
                       }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


}






