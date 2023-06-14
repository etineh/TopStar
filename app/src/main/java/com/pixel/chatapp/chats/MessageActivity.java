package com.pixel.chatapp.chats;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChat;
    private ImageView imageViewBack;
    private TextView textViewOtherUser;
    private EditText editTextMessage;
    private FloatingActionButton fab;

    String userName, otherName, uID;
    FirebaseDatabase fbDatabase;
    DatabaseReference dbReference;
    DatabaseReference refBackResetCount, refTyping;
    DatabaseReference referenceMsgCount2, referenceMsgCount;
    FirebaseAuth auth;
    FirebaseUser user;
    MessageAdapter adapter;
    List<MessageModel> modelList;
    SharedPreferences sharedPreferences;
    private Handler handler;
    private Runnable runnable;
    private long count = 0;
    private Boolean runnerChaeck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        imageViewBack = findViewById(R.id.imageViewBackArrow);
        textViewOtherUser = findViewById(R.id.textViewChat);
        editTextMessage = findViewById(R.id.editTextMessage);
        fab = findViewById(R.id.fab);
        recyclerViewChat = findViewById(R.id.recyclerViewChat);

        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        modelList = new ArrayList<>();

        sharedPreferences = this.getSharedPreferences("MessageCount", Context.MODE_PRIVATE); // SharePreference Storage

        fbDatabase = FirebaseDatabase.getInstance();
        dbReference = fbDatabase.getReference();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        refBackResetCount = fbDatabase.getReference("Checks");

        // get users details sents from userlist via intent
        userName = getIntent().getStringExtra("userName");
        otherName = getIntent().getStringExtra("otherName");
        uID = getIntent().getStringExtra("Uid");

        textViewOtherUser.setText(otherName);   // display their userName on top of their page

        // arrow back
        imageViewBack.setOnClickListener(new View.OnClickListener() {   // return back when the arrow is clicked
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                onBackPressed();
            }
        });


        // show my user that I am typing
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {

                String message2 = editTextMessage.getText().toString();
                refTyping = FirebaseDatabase.getInstance().getReference("Checks")
                        .child(uID).child(user.getUid()).child("typing");
                if(!message2.equals("")){
                    refTyping.setValue(1);
                } else {
                    refTyping.setValue(0);
                }

                if(!runnerChaeck) handler.postDelayed(runnable, 1000);
                else {
                    refTyping.setValue(0);
                    handler.removeCallbacks(runnable);
                }
            }
        };
        handler.post(runnable);


        // add user id to db when user start typing
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

        // send message
        fab.setOnClickListener(new View.OnClickListener() {     // send message when the button is clicked
            @Override
            public void onClick(View view) {
                String message = editTextMessage.getText().toString();
                if (!message.equals("")){
                    sendMessage(message);
                    editTextMessage.setText("");
                }
            }
        });


//        Check if the unreadMessage count is 0
        DatabaseReference referenceMsgCountCheck = FirebaseDatabase.getInstance().getReference("Checks")
                .child(uID).child(user.getUid()).child("unreadMsg");
        referenceMsgCountCheck.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(!snapshot.exists()){

                    referenceMsgCountCheck.setValue(0);

                } else {
                    // if last msg count is not 0, then get the count
                    if(!snapshot.getValue().equals(0)){
                        count = (long) snapshot.getValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        getMessage();

    }

    //---------------------- methods -----------------
    public void sendMessage(String message){

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("message", message);
        messageMap.put("from", userName);
        messageMap.put("timeSent", ServerValue.TIMESTAMP);
        //  now save the message to the database
        String key = dbReference.child("Messages").child(userName).child(otherName).push().getKey();  // create an id for each message
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
                .child(uID).child(user.getUid()).child("unreadMsg");
        referenceMsgCount.setValue(count+=1);

        referenceMsgCount2 = FirebaseDatabase.getInstance().getReference("Checks")
                .child(user.getUid()).child(uID).child("unreadMsg");
        referenceMsgCount2.setValue(0);


        // ---------- use .addListenerForSingleValueEvent when you want it to change only when clicked.
//        dbReference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                // check if the other user is not in your page to view your msg
//                if(snapshot.child(uID).child(user.getUid()).child("show").getValue().equals("out")){
//                    dbReference.child("Users").child(user.getUid()).child(uID)
//                            .child("realr").setValue("out");
//
//                    // unread msg count
//                    long lastCount = (long) snapshot.child(user.getUid()).child(uID).child("msgCount").getValue();
//                    count = 1;
//                    dbReference.child("Users").child(user.getUid()).child(uID)
//                            .child("msgCount").setValue(lastCount + count);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

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

        adapter = new MessageAdapter(modelList, userName, uID);
        recyclerViewChat.setAdapter(adapter);

    }

    @Override
    public void onBackPressed() {

        refBackResetCount.child(user.getUid()).child(uID).child("unreadMsg").setValue(0);
//        refBackResetCount.child(user.getUid()).child(uID).child("typing").setValue(0);
        runnerChaeck = true;

        finish();
        super.onBackPressed();
    }
//
    @Override
    protected void onPause() {
        refBackResetCount.child(user.getUid()).child(uID).child("unreadMsg").setValue(0);
//        refBackResetCount.child(user.getUid()).child(uID).child("typing").setValue(0);
        runnerChaeck = true;
//        finish();
        super.onPause();
    }
//
    @Override
    protected void onResume() {

        runnerChaeck = false;
        super.onResume();
    }

}









