package com.pixel.chatapp.chats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.pixel.chatapp.Permission.Permission;
import com.pixel.chatapp.R;
import com.pixel.chatapp.VideoCallComeIn;
import com.pixel.chatapp.VideoCallComingOut;
import com.pixel.chatapp.constants.AllConstants;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChat;
    private ImageView imageViewBack;
    private CircleImageView circleImageOnline, circleImageLogo;
    private ImageView imageViewOpenMenu, imageViewCloseMenu, imageViewCancelDel, imageViewCancelReply;
    private ImageView editOrReplyIV, imageViewCalls;
    private ConstraintLayout constraintProfileMenu, constraintDelBody;
    private TextView textViewOtherUser, textViewLastSeen, textViewTyping, textViewReply, nameReply, replyVisible;
    private TextView textViewDelMine, textViewDelOther, textViewDelAll;
    private EditText editTextMessage;
    private CircleImageView circleSendMessage;
    private CardView cardViewMsg, cardViewReply;
    String userName, otherName, uID, imageUrl;
    DatabaseReference refMessages, refChecks, refUsers, refMsgCheck, refMsgSeen;
    DatabaseReference referenceMsgCount2, referenceMsgCount;
    FirebaseUser user;
    MessageAdapter adapter;
    List<MessageModel> modelList;
    int scrollPosition;
    Map<String, Object> mapUpdate;
//    SharedPreferences sharedPreferences;
    private Handler handler;
    private Runnable runnable;
    private long count = 0, offCount = 0, newMsgCount = 0;
    private Boolean runnerCheck = false;
    private Map<String, Integer> dateNum, dateMonth;
    private String idKey, listener = "no", replyFrom, networkListener = "yes", insideChat = "no";
    private String audioPath;
    private MediaRecorder mediaRecorder;
    private Permission permissions;


    RecordView recordView;
    RecordButton recordButton;

    // receive broadcast
    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            idKey = intent.getStringExtra("id");
            listener = intent.getStringExtra("listener");
            replyFrom = intent.getStringExtra("replyFrom");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        imageViewBack = findViewById(R.id.imageViewBackArrow);
        textViewOtherUser = findViewById(R.id.textViewName);
        editTextMessage = findViewById(R.id.editTextMessage);
        circleSendMessage = findViewById(R.id.fab);
        imageViewOpenMenu = findViewById(R.id.imageViewUserMenu2);
        imageViewCloseMenu = findViewById(R.id.imageViewCancel);
        constraintProfileMenu = findViewById(R.id.constraintProfileMenu);
        textViewLastSeen = findViewById(R.id.textViewStatus);
        textViewTyping = findViewById(R.id.textViewTyping2);
        circleImageOnline = findViewById(R.id.circleImageOnline);
        circleImageLogo = findViewById(R.id.circleImageLogo);
        constraintDelBody = findViewById(R.id.constDelBody);
        textViewDelMine = findViewById(R.id.textViewDelMine);
        textViewDelOther = findViewById(R.id.textViewDelOther);
        textViewDelAll = findViewById(R.id.textViewDelEveryone);
        imageViewCancelDel = findViewById(R.id.imageViewCancelDel);
        cardViewReply = findViewById(R.id.cardViewReply);
        textViewReply = findViewById(R.id.textViewReplyText);
        imageViewCancelReply = findViewById(R.id.imageViewCancleReply);
        editOrReplyIV = findViewById(R.id.editOrReplyImage);
        nameReply = findViewById(R.id.fromTV);
        replyVisible = findViewById(R.id.textReplying);
        imageViewCalls = findViewById(R.id.imageViewCalls);

        recyclerViewChat = findViewById(R.id.recyclerViewChat);

        recyclerViewChat.setHasFixedSize(true);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        modelList = new ArrayList<>();

        // audio swipe button option
        recordView = (RecordView) findViewById(R.id.record_view);
        recordButton = (RecordButton) findViewById(R.id.record_button);
        recordButton.setRecordView(recordView);

//        sharedPreferences = this.getSharedPreferences("MessageCount", Context.MODE_PRIVATE); // SharePreference Storage

        refMessages = FirebaseDatabase.getInstance().getReference("Messages");
        user = FirebaseAuth.getInstance().getCurrentUser();

        refChecks = FirebaseDatabase.getInstance().getReference("Checks");
        refUsers = FirebaseDatabase.getInstance().getReference("Users");
        refMsgCheck = FirebaseDatabase.getInstance().getReference("MsgCheck");
        refMsgSeen = FirebaseDatabase.getInstance().getReference("MsgSeen");

        // get users details via intent
        userName = getIntent().getStringExtra("userName");
        otherName = getIntent().getStringExtra("otherName");
        uID = getIntent().getStringExtra("Uid");
        imageUrl = getIntent().getStringExtra("ImageUrl");
        scrollPosition = getIntent().getIntExtra("recyclerScroll", 0);
        insideChat = getIntent().getStringExtra("insideChat");
        modelList = (List<MessageModel>) getIntent().getSerializableExtra("messageList");

        recyclerViewChat.scrollToPosition(modelList.size() - 1);
//        adapter = new MessageAdapter(modelList, userName, uID, MessageActivity.this, editTextMessage, constraintDelBody, textViewReply,
//                cardViewReply, textViewDelOther, editOrReplyIV, nameReply, replyVisible);
//        recyclerViewChat.setAdapter(adapter);

        textViewOtherUser.setText(otherName);   // display their userName on top of their page

//         get user image
        if (imageUrl.equals("null")) {
            circleImageLogo.setImageResource(R.drawable.person_round);
        }
        else Picasso.get().load(imageUrl).into(circleImageLogo);

        // get the idkey message from the adaptor via the broadcast intent
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("editMsg"));


        // send message
        circleSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = editTextMessage.getText().toString().trim();
                if (!message.equals("")){

                    sendMessage(message, 8, "");
                    editTextMessage.setText("");
                    listener = "no";
                    scrollPosition = 0;
                    cardViewReply.setVisibility(View.GONE);
                    nameReply.setVisibility(View.GONE);
                    replyVisible.setVisibility(View.GONE);
                }
            }
        });

        // return back when the arrow is clicked
        imageViewBack.setOnClickListener(view -> {
            onBackPressed();
        });

        // open user menu
        imageViewOpenMenu.setOnClickListener(view -> constraintProfileMenu.setVisibility(View.VISIBLE));

        // close user menu
        imageViewCloseMenu.setOnClickListener(view -> constraintProfileMenu.setVisibility(View.GONE));
        constraintProfileMenu.setOnClickListener(view -> constraintProfileMenu.setVisibility(View.GONE));

        // Close delete message option
        constraintDelBody.setOnClickListener(view -> constraintDelBody.setVisibility(View.GONE));

        // Delete for only me
        textViewDelMine.setOnClickListener(view -> {
            refMessages.child(userName).child(otherName).child(idKey).getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    constraintDelBody.setVisibility(View.GONE);
                    Toast.makeText(MessageActivity.this, "Message deleted for me.", Toast.LENGTH_SHORT).show();
                }
            });
            idKey = null;
            listener = "no";
        });

        // Delete for others only
        textViewDelOther.setOnClickListener(view -> {
            refMessages.child(otherName).child(userName).child(idKey).getRef().removeValue();
            constraintDelBody.setVisibility(View.GONE);
            Toast.makeText(MessageActivity.this, "Message deleted for "+otherName+".", Toast.LENGTH_SHORT).show();

            deleteMsgSeenKey();
            listener = "no";
        });

        // Delete for everyone
        textViewDelAll.setOnClickListener(view -> {

            refMessages.child(userName).child(otherName).child(idKey).getRef().removeValue();
            refMessages.child(otherName).child(userName).child(idKey).getRef().removeValue();
            constraintDelBody.setVisibility(View.GONE);
            Toast.makeText(MessageActivity.this, "Message deleted for everyone.", Toast.LENGTH_SHORT).show();
            refChecks.child(uID).child(user.getUid()).child("newMsgCount").setValue(0);

            deleteMsgSeenKey();                 // delete the push the un-deliver msg key from db
            listener = "no";
        });


        // close and cancel reply and edit box
        imageViewCancelReply.setOnClickListener(view -> {

            nameReply.setVisibility(View.GONE);
            replyVisible.setVisibility(View.GONE);
            cardViewReply.setVisibility((int) 8);   // 8 is for GONE

            listener = "no";
            idKey = null;
            editTextMessage.setText("");
        });

        // video call
        imageViewCalls.setOnClickListener(view -> {
            Intent intent = new Intent(MessageActivity.this, VideoCallComingOut.class);
            intent.putExtra("uid", uID);
            intent.putExtra("imageUri", imageUrl);
            intent.putExtra("otherName", otherName);

            startActivity(intent);
        });

        getMessage();

        alertMeWhenUserCallMe();

//        setMsgSeen();

        tellUserAmTyping_AddUser();

        reloadUnsentMsg();

        getMyUserTyping();

        getLastSeenAndOnline();

        resetStatusAndMsgCount();

        getPreviousCounts();

        setIsOnline();

        voiceNote ();

        swipeReplyOptions();

        checkPermissions();
    }


    //---------------------- methods -----------------


        // get messages
    private void getMessage(){
        DatabaseReference refMsg = FirebaseDatabase.getInstance().getReference("Messages").child(userName).child(otherName);
        refMsg.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                modelList.clear();

                for (DataSnapshot snapshot1 : snapshot.getChildren()){

                    MessageModel messageModel = snapshot1.getValue(MessageModel.class);
                    messageModel.setIdKey(snapshot1.getKey());  // set msg keys to the adaptor
                    modelList.add(messageModel);
                    adapter.notifyDataSetChanged();
                }

                // scroll to the new message position number
//                recyclerViewChat.scrollToPosition(modelList.size() - scrollPosition - 1);
                recyclerViewChat.scrollToPosition(modelList.size() - 1);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        recyclerViewChat.scrollToPosition(modelList.size() - 1);
        adapter = new MessageAdapter(modelList, userName, uID, MessageActivity.this);
        recyclerViewChat.setAdapter(adapter);

    }
    public boolean checkNetworkConnection()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo == null) return false;

        return networkInfo.isConnected();
    }

    public void resetStatusAndMsgCount(){
        // set my status to be true in case I receive msg, it will be tick as seen
        Map <String, Object> statusAndMSgCount = new HashMap<>();
        statusAndMSgCount.put("status", true);
        statusAndMSgCount.put("unreadMsg", 0);

        refChecks.child(user.getUid()).child(uID).updateChildren(statusAndMSgCount);
    }

    public void sendMessage(String message, int type, String vn)
    {
        // 700024 --- tick one msg  // 700016 -- seen msg   // 700033 -- load

//        String key = refMessages.child(userName).child(otherName).push().getKey();  // create an id for each message
        int visibility = 8;
        int msgStatus = 700024;

        if(networkListener == "no"){
            msgStatus = 700033;
//            refMsgCheck.child(user.getUid()).push().child("loadKey").setValue(key);     // push the un-deliver msg key to db
        }

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("from", userName);

        if (listener == "reply") {
            messageMap.put("replyMsg", textViewReply.getText());
            messageMap.put("replyFrom", replyFrom);
            visibility = 1;
        } else if (listener == "yes") {
//            key = idKey;
            messageMap.put("edit", "edited");
        }

        messageMap.put("type", type);
//        messageMap.put("idKey", key);
        messageMap.put("message", message);
        messageMap.put("voicenote", vn);
        messageMap.put("msgStatus", msgStatus);
        messageMap.put("visibility", visibility);
        messageMap.put( "timeSent", ServerValue.TIMESTAMP);

        //  now save the message to the database
//        refMessages.child(userName).child(otherName).child(key).setValue(messageMap);
//        refMessages.child(otherName).child(userName).child(key).setValue(messageMap);

        refMessages.child(userName).child(otherName).push().setValue(messageMap);
        refMessages.child(otherName).child(userName).push().setValue(messageMap);

        // send delivery id to MsgSeen
//        refMsgSeen.child(uID).child(user.getUid()).push().child("seenKey").setValue(key);

        if (listener == "no" || listener == "reply")
        {
// --- set the last send message details
            DatabaseReference fReference = FirebaseDatabase.getInstance().getReference("UsersList")
                    .child(user.getUid()).child(uID);
            fReference.setValue(messageMap);

            DatabaseReference fReference2 = FirebaseDatabase.getInstance().getReference("UsersList")
                    .child(uID).child(user.getUid());
            fReference2.setValue(messageMap);

            checkAndSaveCounts_SendMsg();
        }

    }

    //  alert me when user is calling me
    public void alertMeWhenUserCallMe() {
        refChecks.child(uID).child(user.getUid()).child("vCall")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(!snapshot.exists()){
                            refChecks.child(uID).child(user.getUid()).child("vCall").setValue("off");
                        } else {
                            if(snapshot.getValue().equals("on")){
                                Intent intent = new Intent(MessageActivity.this, VideoCallComeIn.class);
                                intent.putExtra("otherUid", uID);
                                intent.putExtra("imageUri", imageUrl);
                                startActivity(intent);

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    // delete key from MsgCheckDelivery db if user delete msg b4 it delivers.
    private void deleteMsgSeenKey(){
        refMsgSeen.child(uID).child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    String seenKey = snapshot1.getKey();
                    String msgSeen = snapshot1.child("seenKey").getValue().toString();

                    if(msgSeen.equals(idKey)) {
                        refMsgSeen.child(uID).child(user.getUid()).child(seenKey).removeValue();
                    }

                }
                idKey = null;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkAndSaveCounts_SendMsg(){

        // save the number of msg sent by me to receiver
        referenceMsgCount = FirebaseDatabase.getInstance().getReference("Checks")
                .child(uID).child(user.getUid());
        referenceMsgCount.child("unreadMsg").setValue(count+=1);

        referenceMsgCount2 = FirebaseDatabase.getInstance().getReference("Checks")
                .child(user.getUid()).child(uID).child("unreadMsg");
        referenceMsgCount2.setValue(0);

        // check if there is network connection before sending message
        if(!checkNetworkConnection()){
            refChecks.child(uID).child(user.getUid()).child("offCount").setValue(offCount+=1);
        } else{
            refChecks.child(uID).child(user.getUid()).child("offCount").setValue(0);
        }

        //      check if the user is in my chat box and reset the count -- newMsgCount & unreadMsg
        DatabaseReference statusCheck = FirebaseDatabase.getInstance().getReference("Checks");
        statusCheck.child(uID).addListenerForSingleValueEvent(new ValueEventListener() {
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
                    } else if (statusState == false) {
                        // increase the new msg count
                        statusCheck.child(uID).child(user.getUid()).child("newMsgCount").setValue(newMsgCount+1);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Reload seen msg status delivery when user is online
    private void setMsgSeen(){
        refMsgSeen.child(user.getUid()).child(uID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1: snapshot.getChildren()){

                    if(insideChat == "yes"){    // activate only when I am inside
                        String keys = snapshot1.child("seenKey").getValue().toString();

                        refMessages.child(otherName).child(userName).child(keys)
                                .child("msgStatus").setValue(700016);

                        new CountDownTimer(5000, 1000){
                            @Override
                            public void onTick(long l) {

                            }

                            @Override
                            public void onFinish() {
                                refMsgSeen.child(user.getUid()).child(uID).child(snapshot1.getKey()).removeValue();
                            }
                        }.start();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Reload msg status delivery to true when network is true
    private void refreshMsgDeliveryIcon(){
        refMsgCheck.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot snapshot1: snapshot.getChildren()){
                    // refresh as soon as network connection is true
                    if(checkNetworkConnection()){
                        // get the keys of loading msg and update the Message delivery msgStatus
                        String keys = snapshot1.child("loadKey").getValue().toString();
                        refMessages.child(userName).child(otherName).child(keys)
                                .child("msgStatus").setValue(700024);

                        // delete in 5secs, not to deley the app
                        new CountDownTimer(5000, 1000){
                            @Override
                            public void onTick(long l) {

                            }

                            @Override
                            public void onFinish() {
                                refMsgCheck.child(user.getUid()).child(snapshot1.getKey()).removeValue();
                            }
                        }.start();
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // get the last seen and online presence of user
    public void getLastSeenAndOnline()
    {
        refUsers.child(uID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                if(!snapshot.child("presence").exists()){
                    textViewLastSeen.setText("WinnerChat");
                }
                else
                {
                    long onlineValue = (long) snapshot.child("presence").getValue();
                    if (onlineValue == 1){
                        textViewLastSeen.setText("Online");
                        circleImageOnline.setVisibility(View.VISIBLE);
                    }
                    else {
                        circleImageOnline.setVisibility(View.GONE);
                        //  Sat Jun 17 23:07:21 GMT+01:00 2023          //  1687042708508
                        // current date and time
                        Timestamp stamp = new Timestamp(System.currentTimeMillis());
                        Date date = new Date(stamp.getTime());
                        String dateString = String.valueOf(date);

                        // last user date and time
                        Date d = new Date(onlineValue);
                        DateFormat formatter = new SimpleDateFormat("h:mm a");
                        String time = formatter.format(d);
                        String previousDateString = String.valueOf(d);

                        dateMonth = new HashMap<>();     // months
                        dateMonth.put("Jan", 1);
                        dateMonth.put("Feb", 2);
                        dateMonth.put("Mar", 3);
                        dateMonth.put("Apr", 4);
                        dateMonth.put("May", 5);
                        dateMonth.put("Jun", 6);
                        dateMonth.put("Jul", 7);
                        dateMonth.put("Aug", 8);
                        dateMonth.put("Sep", 9);
                        dateMonth.put("Oct", 10);
                        dateMonth.put("Nov", 11);
                        dateMonth.put("Dec", 12);

                        dateNum = new HashMap<>();      // days
                        dateNum.put("Mon", 1);
                        dateNum.put("Tue", 2);
                        dateNum.put("Wed", 3);
                        dateNum.put("Thu", 4);
                        dateNum.put("Fri", 5);
                        dateNum.put("Sat", 6);
                        dateNum.put("Sun", 7);

                        String lastYear = previousDateString.substring(30, 34);  // last year

                        int curMonth = dateMonth.get(dateString.substring(4,7));    // Months
                        int lastMonth = dateMonth.get(previousDateString.substring(4,7));

                        int curDay = dateNum.get(dateString.substring(0,3));    // Mon - Sun
                        int lastDay = dateNum.get(previousDateString.substring(0,3));

                        String lastDayString = previousDateString.substring(0,3);   // get the day

                        int dateCur = Integer.parseInt(dateString.substring(8, 10));    // day 1 - 30
                        int dateLast = Integer.parseInt(previousDateString.substring(8, 10));

                        if (curMonth - lastMonth == 0)
                        {
                            if (dateCur - dateLast < 7)
                            {
                                if(curDay - lastDay == 0)
                                {
                                    textViewLastSeen.setText("Last seen: " + time.toLowerCase()+".");
                                } else if (curDay - lastDay == 1) {
                                    textViewLastSeen.setText("Last seen: Yesterday, \n"+time.toLowerCase()+".");
                                } else if (curDay - lastDay == 2) {
                                    textViewLastSeen.setText("Last seen: 2days ago, \n"+time.toLowerCase()+".");
                                } else if (curDay - lastDay == 3) {
                                    textViewLastSeen.setText("Last seen: 3days ago, \n"+time.toLowerCase()+".");
                                } else if (curDay - lastDay == 4) {
                                    textViewLastSeen.setText("Last seen: 4days ago, \n"+time.toLowerCase()+".");
                                } else if (curDay - lastDay == 5) {
                                    textViewLastSeen.setText("Last seen: 5days ago, \n"+time.toLowerCase()+".");
                                } else if (curDay - lastDay == 6) {
                                    textViewLastSeen.setText("Last seen: 6days ago, \n"+time.toLowerCase()+".");
                                }
                            } else if (dateCur - dateLast >= 7 && dateCur - dateLast < 14) {
                                textViewLastSeen.setText("Last seen: \nLast week "+lastDayString+".");
                            } else if (dateCur - dateLast >= 14 && dateCur - dateLast < 21) {
                                textViewLastSeen.setText("Last seen: \nLast 2 weeks "+lastDayString+".");
                            } else if (dateCur - dateLast >= 21 && dateCur - dateLast < 27) {
                                textViewLastSeen.setText("Last seen: \nLast 3 weeks "+lastDayString+".");
                            } else {
                                textViewLastSeen.setText("Last seen: a month \nago");
                            }
                        } else if(curMonth - lastMonth == 1){
                            textViewLastSeen.setText("Last seen: \none month ago..");
                        } else if(curMonth - lastMonth == 2){
                            textViewLastSeen.setText("Last seen: \ntwo months ago.");
                        }else if(curMonth - lastMonth == 3){
                            textViewLastSeen.setText("Last seen: \nthree months ago.");
                        }else if(curMonth - lastMonth == 4){
                            textViewLastSeen.setText("Last seen: \nFour months ago.");
                        }else if(curMonth - lastMonth == 5){
                            textViewLastSeen.setText("Last seen: \nFive months ago.");
                        } else {
                            textViewLastSeen.setText("Last seen: "+dateLast +"/"+ lastMonth+"/"+ lastYear);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    // clear off msg load tick when network restores.
    private void reloadUnsentMsg(){
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {

                // clear off msg load tick when network restores.
                if(checkNetworkConnection()){
                    refChecks.child(uID).child(user.getUid()).child("offCount").setValue(0);
//                    refreshMsgDeliveryIcon();   // reload unsentMsg
                    networkListener = "yes";
                } else {
                    networkListener = "no";
                }

                if(!runnerCheck) handler.postDelayed(runnable, 1000);
                else {
                    refChecks.child(uID).child(user.getUid()).child("typing").setValue(0);
                    handler.removeCallbacks(runnable);
                }
            }
        };
        handler.post(runnable);
    }

    // add user id to db when user start typing and (reset newMsgCount to 0  --- change later)
    // Alert the DB when I start typing, to notify the receiver     // interact with send and record buttons
    public void tellUserAmTyping_AddUser(){
        editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //  interact with both send and record buttons
                if(charSequence.length() > 0){
                    circleSendMessage.setVisibility(View.VISIBLE);
                    recordButton.setVisibility(View.INVISIBLE);
                    refChecks.child(uID).child(user.getUid()).child("typing").setValue(1);
                } else {
                    circleSendMessage.setVisibility(View.GONE);
                    recordButton.setVisibility(View.VISIBLE);
                    refChecks.child(uID).child(user.getUid()).child("typing").setValue(0);
                }
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

                // reset the new msg count
                DatabaseReference statusCheck2 = FirebaseDatabase.getInstance().getReference("Checks");
                statusCheck2.child(uID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(!snapshot.child(user.getUid()).child("status").exists()){
                            statusCheck2.child(uID).child(user.getUid())
                                    .child("status").setValue(false);
                        }
                        else {
                            boolean statusState = (boolean) snapshot.child(user.getUid())
                                    .child("status").getValue();

                            // receiver should be 0
                            if(statusState == true) {
                                statusCheck2.child(uID).child(user.getUid()).child("newMsgCount").setValue(0);
                            }

                            // Mine should be 0
                            statusCheck2.child(user.getUid()).child(uID).child("newMsgCount").setValue(0);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
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

    //  Get all previous counts of unreadMsg and offCount and newMsgCount
    private void getPreviousCounts(){

        DatabaseReference referenceMsgCountCheck = FirebaseDatabase.getInstance().getReference("Checks")
                .child(uID).child(user.getUid());
        referenceMsgCountCheck.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(!snapshot.child("unreadMsg").exists() || !snapshot.child("offCount").exists()
                    || !snapshot.child("newMsgCount").exists())
                {
                    referenceMsgCountCheck.child("unreadMsg").setValue(0);
                    referenceMsgCountCheck.child("offCount").setValue(0);
                    referenceMsgCountCheck.child("newMsgCount").setValue(0);
                } else {
                    // if last msg count is not 0, then get the count
                    if(!snapshot.child("unreadMsg").getValue().equals(0)){
                        count = (long) snapshot.child("unreadMsg").getValue();
                    }
                    // if last offline count is not 0, then get the count
                    if(!snapshot.child("offCount").getValue().equals(0)){
                        offCount = (long) snapshot.child("offCount").getValue();
                    }
                    // if last new msg count is not 0, then get the count
                    if(!snapshot.child("offCount").getValue().equals(0)){
                        newMsgCount = (long) snapshot.child("newMsgCount").getValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    // swiping for reply
    private void swipeReplyOptions(){
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {

                int position = viewHolder.getAdapterPosition();

                // pop up keyboard
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(editTextMessage, InputMethodManager.SHOW_IMPLICIT);
                editTextMessage.requestFocus();

                // fetch data
                idKey = modelList.get(position).getIdKey();
                replyFrom = modelList.get(position).getFrom();
                String msg = modelList.get(position).getMessage();
                listener = "reply";

                // set reply name and replying hint
                if (modelList.get(position).getFrom().equals(userName)) {
                    nameReply.setText("From You.");
                }
                else {
                    nameReply.setText(modelList.get(position).getFrom() +
                            " (@" +modelList.get(position).getFrom()+")");
                }
                nameReply.setVisibility(View.VISIBLE);
                replyVisible.setVisibility(View.VISIBLE);
                replyVisible.setText("replying...");

                // set data
                textViewReply.setText(msg);
                editOrReplyIV.setImageResource(R.drawable.reply);
                cardViewReply.setVisibility(View.VISIBLE);

                return super.getMovementFlags(recyclerView, viewHolder);
            }
        }).attachToRecyclerView(recyclerViewChat);

    }

    //  check for permission status
    private void checkPermissions(){
        //   audio permission
        if(MessageActivity.this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)){

            if(ContextCompat.checkSelfPermission(MessageActivity.this, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_DENIED
                    || ContextCompat.checkSelfPermission(MessageActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED
                    || ContextCompat.checkSelfPermission(MessageActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED){

                ActivityCompat.requestPermissions(MessageActivity.this, new String[]
                                {Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }
        }
    }

    // request voiceNote
    private void voiceNote (){

        //IMPORTANT
//        recordButton.setRecordView(recordView);

//        recordButton.setListenForRecord(false);
//        recordButton.setOnClickListener(view -> {
//            if (permissions.isRecordingOk(MessageActivity.this))
//                if (permissions.isStorageOk(MessageActivity.this))
//                    recordButton.setListenForRecord(true);
//                else permissions.requestStorage(MessageActivity.this);
//            else permissions.requestRecording(MessageActivity.this);
//
//        });


        recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                //Start Recording..
                Log.i("RecordView", "onStart");

                setUpRecording();

                try {
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                    Toast.makeText(MessageActivity.this, "Started record", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                editTextMessage.setVisibility(View.INVISIBLE);   //  hide msg edit text
                recordView.setVisibility(View.VISIBLE);     // show swipe mode
            }

            @Override
            public void onCancel() {
                //On Swipe To Cancel
                Log.d("RecordView", "onCancel");

                mediaRecorder.reset();
                mediaRecorder.release();
                File file = new File(getRecordFilePath());
                if (file.exists())
                    file.delete();

                editTextMessage.setVisibility(View.VISIBLE);    //  hide msg edit text
                recordView.setVisibility(View.GONE);            // show swipe mode
            }

            @Override
            public void onFinish(long recordTime, boolean limitReached) {
                //Stop Recording..
                try {
                    mediaRecorder.stop();
                    mediaRecorder.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                editTextMessage.setVisibility(View.VISIBLE);    //  hide msg edit text
                recordView.setVisibility(View.GONE);            // show swipe mode

                sendRecodingMessage();
            }

            @Override
            public void onLessThanSecond() {
                //When the record time is less than One Second
                Log.d("RecordView", "onLessThanSecond");

                mediaRecorder.reset();
                mediaRecorder.release();

                File file = new File(getRecordFilePath());
                if (file.exists())
                    file.delete();

                editTextMessage.setVisibility(View.VISIBLE);    //  hide msg edit text
                recordView.setVisibility(View.GONE);            // show swipe mode
            }

            @Override
            public void onLock() {

            }
        });

    }

    private void setUpRecording() {

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        mediaRecorder.setOutputFile(getRecordFilePath());

        // bug fix by getRecordFilePath()
//        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/WinnerChat/Media/Recording");
//        if (!file.exists())
//            file.mkdirs();
//        audioPath = file.getAbsolutePath() + File.separator + System.currentTimeMillis() + ".3gp";
//        mediaRecorder.setOutputFile(audioPath);
    }

    private String getRecordFilePath(){
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File audioDir = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(audioDir, "voice_note_" + ".3gp");
        return file.getPath();
    }

    private void sendRecodingMessage(){
        StorageReference storageReference = FirebaseStorage.getInstance()
                .getReference("media/voice_note/" + user.getUid() + "/" + System.currentTimeMillis());

        Uri audioFile = Uri.fromFile(new File(getRecordFilePath()));

        storageReference.putFile(audioFile).addOnSuccessListener(success -> {
            Task<Uri> audioUrl = success.getStorage().getDownloadUrl();

            audioUrl.addOnCompleteListener(path -> {

                String url = path.getResult().toString();

                sendMessage("", 1, url);    // 1 is for visibility

//                if (path.isSuccessful()) {
//                    if (chatID == null) {
//                        createChat(url);
//                    } else {
//                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chat").child(chatID);
//                        MessageModel messageModel = new MessageModel(myID, hisID, url, util.currentData(), "recording");
//                        databaseReference.push().setValue(messageModel);
//                    }
//                }
            });
        });
        
    }

    // Turn on my online presence on
    public void setIsOnline(){
        refUsers.child(user.getUid()).child("presence").setValue(1);
        new CountDownTimer(10500, 1000){
            @Override
            public void onTick(long l) {

            }
            @Override
            public void onFinish() {
                refUsers.child(user.getUid()).child("presence").setValue(1);
            }
        }.start();
    }

    @Override
    public void onBackPressed() {

        mapUpdate = new HashMap<>();
        mapUpdate.put("status", false);
        mapUpdate.put("newMsgCount", 0);
        refChecks.child(user.getUid()).child(uID).updateChildren(mapUpdate);
        runnerCheck = true;
        insideChat = "no";

        finish();
        super.onBackPressed();
    }
//
    @Override
    protected void onPause() {
        refChecks.child(user.getUid()).child(uID).child("status").setValue(false);

        // Turn off online presence
        new CountDownTimer(10000, 1000){
            @Override
            public void onTick(long l) {

            }
            @Override
            public void onFinish() {
                refUsers.child(user.getUid()).child("presence").setValue(ServerValue.TIMESTAMP);
            }
        }.start();

        runnerCheck = true;
        insideChat = "no";
        super.onPause();
    }
//
//    @Override
    protected void onResume() {
        refChecks.child(user.getUid()).child(uID).child("status").setValue(true);

        // set responds to pend always      ------- will change later to check condition if user is still an active call
        refChecks.child(user.getUid()).child(uID).child("vCallResp").setValue("pending");

        setIsOnline();
        runnerCheck = false;
        insideChat = "yes";
        super.onResume();
    }


    // permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            getRecordFilePath();
        }
    }

//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        switch (requestCode) {
////            case PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
////                // If request is cancelled, the result arrays are empty.
////                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
////                    getGalleryImage();
////                } else {
////                    Toast.makeText(this, "Approve permissions to open Pix ImagePicker", Toast.LENGTH_LONG).show();
////                }
////
////                break;
//
//            case  AllConstants.RECORDING_REQUEST_CODE:
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    if (ContextCompat.checkSelfPermission(MessageActivity.this,
//                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
//                        recordButton.setListenForRecord(true);
//                    else {
//                        ActivityCompat.requestPermissions(MessageActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
//                                Manifest.permission.WRITE_EXTERNAL_STORAGE}, AllConstants.STORAGE_REQUEST_CODE);
//                    };
//
//                } else
//                    Toast.makeText(this, "Recording permission denied", Toast.LENGTH_SHORT).show();
//                break;
//
////            case AllConstants.STORAGE_REQUEST_CODE:
////
////                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
////                    recordButton.setListenForRecord(true);
////                else
////                    Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
////                break;
//
//        }
//    }

}









