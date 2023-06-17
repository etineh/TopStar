package com.pixel.chatapp.chats;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.pixel.chatapp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChat;
    private ImageView imageViewBack;
    private ImageView imageViewOpenMenu, imageViewCloseMenu;
    private ConstraintLayout constraintProfileMenu;
    private TextView textViewOtherUser, textViewLastSeen, textViewTyping;
    private EditText editTextMessage;
    private FloatingActionButton fab;
    String userName, otherName, uID;
    DatabaseReference dbReference, refChecks;
    DatabaseReference referenceMsgCount2, referenceMsgCount;
    FirebaseUser user;
    MessageAdapter adapter;
    List<MessageModel> modelList;
    SharedPreferences sharedPreferences;
    private Handler handler;
    private Runnable runnable;
    private long count = 0, offCount = 0;
    private Boolean runnerChaeck = false, networkMood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        imageViewBack = findViewById(R.id.imageViewBackArrow);
        textViewOtherUser = findViewById(R.id.textViewName);
        editTextMessage = findViewById(R.id.editTextMessage);
        fab = findViewById(R.id.fab);
        imageViewOpenMenu = findViewById(R.id.imageViewUserMenu2);
        imageViewCloseMenu = findViewById(R.id.imageViewCancel);
        constraintProfileMenu = findViewById(R.id.constraintProfileMenu);
        textViewLastSeen = findViewById(R.id.textViewStatus);
        textViewTyping = findViewById(R.id.textViewTyping2);

        recyclerViewChat = findViewById(R.id.recyclerViewChat);

        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        modelList = new ArrayList<>();

        sharedPreferences = this.getSharedPreferences("MessageCount", Context.MODE_PRIVATE); // SharePreference Storage

        dbReference = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        refChecks = FirebaseDatabase.getInstance().getReference("Checks");

        // get users details sents from userlist via intent
        userName = getIntent().getStringExtra("userName");
        otherName = getIntent().getStringExtra("otherName");
        uID = getIntent().getStringExtra("Uid");

        textViewOtherUser.setText(otherName);   // display their userName on top of their page

        // send message
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = editTextMessage.getText().toString();
                if (!message.equals("")){
                    sendMessage(message);
                    editTextMessage.setText("");
                }
            }
        });

        // arrow back
        imageViewBack.setOnClickListener(new View.OnClickListener() {   // return back when the arrow is clicked
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                onBackPressed();
            }
        });

        // open user menu
        imageViewOpenMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                constraintProfileMenu.setVisibility(View.VISIBLE);
            }
        });

        // close user menu
        imageViewCloseMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                constraintProfileMenu.setVisibility(View.GONE);
            }
        });
        constraintProfileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                constraintProfileMenu.setVisibility(View.GONE);
            }
        });


        // alert database when user is typing
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {

                String message2 = editTextMessage.getText().toString();

                if(!message2.equals("")){
                    refChecks.child(uID).child(user.getUid()).child("typing").setValue(1);
                } else {
                    refChecks.child(uID).child(user.getUid()).child("typing").setValue(0);
                }

                if(checkConnection()){
                    refChecks.child(uID).child(user.getUid()).child("offCount").setValue(0);
                }

                if(!runnerChaeck) handler.postDelayed(runnable, 1000);
                else {
                    refChecks.child(uID).child(user.getUid()).child("typing").setValue(0);
                    handler.removeCallbacks(runnable);
                }
            }
        };
        handler.post(runnable);


//        Check if the unreadMessage count and offline count is 0
        DatabaseReference referenceMsgCountCheck = FirebaseDatabase.getInstance().getReference("Checks")
                .child(uID).child(user.getUid());
        referenceMsgCountCheck.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(!snapshot.child("unreadMsg").exists() || !snapshot.child("offCount").exists()){

                    referenceMsgCountCheck.child("unreadMsg").setValue(0);
                    referenceMsgCountCheck.child("offCount").setValue(0);

                } else {
                    // if last msg count is not 0, then get the count
                    if(!snapshot.child("unreadMsg").getValue().equals(0)){
                        count = (long) snapshot.child("unreadMsg").getValue();
                    }
                    // if last msg count is not 0, then get the count
                    if(!snapshot.child("offCount").getValue().equals(0)){
                        offCount = (long) snapshot.child("offCount").getValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        getMessage();

        addUserWhenTyping();

        getMyUserTyping();

    }

    //---------------------- methods -----------------

    public boolean checkConnection()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo == null) return false;

        return networkInfo.isConnected();

    }

    public void sendMessage(String message){

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("message", message);
        messageMap.put("from", userName);
        messageMap.put("timeSent", ServerValue.TIMESTAMP);
        //  now save the message to the database
        String key = dbReference.child("Messages").child(userName).child(otherName).push().getKey();  // create an id for each message
        // set the deliver icon if successful
        dbReference.child("Messages").child(userName).child(otherName).child(key).setValue(messageMap);
        dbReference.child("Messages").child(otherName).child(userName).child(key).setValue(messageMap);

        // --- set the last send message details
        DatabaseReference fReference = FirebaseDatabase.getInstance().getReference("UsersList")
                .child(user.getUid()).child(uID);
        fReference.setValue(messageMap);

        DatabaseReference fReference2 = FirebaseDatabase.getInstance().getReference("UsersList")
                .child(uID).child(user.getUid());
        fReference2.setValue(messageMap);


        // save the number of msg sent by me
        referenceMsgCount = FirebaseDatabase.getInstance().getReference("Checks")
                .child(uID).child(user.getUid());
        referenceMsgCount.child("unreadMsg").setValue(count+=1);

        referenceMsgCount2 = FirebaseDatabase.getInstance().getReference("Checks")
                .child(user.getUid()).child(uID).child("unreadMsg");
        referenceMsgCount2.setValue(0);


        // check if there is network connection before sending message
        if(!checkConnection()){
            refChecks.child(uID).child(user.getUid()).child("offCount").setValue(offCount+=1);
        } else{
            refChecks.child(uID).child(user.getUid()).child("offCount").setValue(0);
        }

//        check if the user is in my chat box and reset the count
        DatabaseReference statusCheck = FirebaseDatabase.getInstance().getReference("Checks");
        statusCheck.child(uID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(!snapshot.child(user.getUid()).child("status").exists()){
                    statusCheck.child(uID).child(user.getUid())
                            .child("status").setValue(false);
                }
                else {
                    boolean statusState = (boolean) snapshot.child(user.getUid())
                            .child("status").getValue();

                    if(statusState == true) {
                        referenceMsgCount.child("unreadMsg").setValue(0);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void getMessage()
    {
        dbReference.child("Messages").child(userName).child(otherName).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //  create an object from the modelClass to get the value from the database
                MessageModel messageModel = snapshot.getValue(MessageModel.class);
                modelList.add(messageModel);
                adapter.notifyDataSetChanged();
                recyclerViewChat.scrollToPosition(modelList.size() - 1); // -------- to display the last message
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        adapter = new MessageAdapter(modelList, userName, uID, offCount, MessageActivity.this);
        recyclerViewChat.setAdapter(adapter);

    }

        // show when user is typing
    public void getMyUserTyping()
    {
        refChecks.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.child(uID).child("typing").exists()){
                    refChecks.child(user.getUid()).child(uID)
                            .child("typing").setValue(0);
                }
                else {

                    long typing = (long) snapshot.child(uID).child("typing").getValue();

                    if(typing == 1){
                        textViewTyping.setText("typing...");
                    } else{
                        textViewTyping.setText("");
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

        // add user id to db when user start typing
    public void addUserWhenTyping(){
        editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                // Adding User to chat fragment: Latest Chats with contacts
                final DatabaseReference chatRef = FirebaseDatabase.getInstance()
                        .getReference("ChatList")
                        .child(user.getUid()).child(uID);

                chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()){
                            chatRef.child("id").setValue(uID);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                final DatabaseReference chatRef2 = FirebaseDatabase.getInstance()
                        .getReference("ChatList")
                        .child(uID).child(user.getUid());

                chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()){
                            chatRef2.child("id").setValue(user.getUid());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {

        refChecks.child(user.getUid()).child(uID).child("status").setValue(false);
        runnerChaeck = true;

        finish();
        super.onBackPressed();
    }
//
    @Override
    protected void onPause() {
        refChecks.child(user.getUid()).child(uID).child("status").setValue(false);
        runnerChaeck = true;
//        finish();
        super.onPause();
    }
//
//    @Override
    protected void onResume() {
        refChecks.child(user.getUid()).child(uID).child("status").setValue(true);
        runnerChaeck = false;
        super.onResume();
    }

}









