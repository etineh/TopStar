package com.pixel.chatapp.chats;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.squareup.picasso.Picasso;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChat;
    private ImageView imageViewBack, imageViewTick;
    private CircleImageView circleImageOnline, circleImageLogo;
    private ImageView imageViewOpenMenu, imageViewCloseMenu;
    private ConstraintLayout constraintProfileMenu;
    private TextView textViewOtherUser, textViewLastSeen, textViewTyping;
    private EditText editTextMessage;
    private CircleImageView fab;
    private CardView cardViewMsg;
    String userName, otherName, uID, imageUrl;
    DatabaseReference dbReference, refChecks, refUsers;
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
    private Boolean runnerChaeck = false;
    private Map<String, Integer> dateNum, dateMonth;

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
        imageViewTick = findViewById(R.id.imageViewTick);
        cardViewMsg = findViewById(R.id.cardViewMsg);
        constraintProfileMenu = findViewById(R.id.constraintProfileMenu);
        textViewLastSeen = findViewById(R.id.textViewStatus);
        textViewTyping = findViewById(R.id.textViewTyping2);
        circleImageOnline = findViewById(R.id.circleImageOnline);
        circleImageLogo = findViewById(R.id.circleImageLogo);

        recyclerViewChat = findViewById(R.id.recyclerViewChat);

        recyclerViewChat.setHasFixedSize(true);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        modelList = new ArrayList<>();

//        sharedPreferences = this.getSharedPreferences("MessageCount", Context.MODE_PRIVATE); // SharePreference Storage

        dbReference = FirebaseDatabase.getInstance().getReference("Messages");
        user = FirebaseAuth.getInstance().getCurrentUser();

        refChecks = FirebaseDatabase.getInstance().getReference("Checks");
        refUsers = FirebaseDatabase.getInstance().getReference("Users");

        // get users details sents from userlist via intent
        userName = getIntent().getStringExtra("userName");
        otherName = getIntent().getStringExtra("otherName");
        uID = getIntent().getStringExtra("Uid");
        imageUrl = getIntent().getStringExtra("ImageUrl");
        scrollPosition = getIntent().getIntExtra("recyclerScroll", 0);

        textViewOtherUser.setText(otherName);   // display their userName on top of their page

//         set user image
        if (imageUrl.equals("null")) {
            circleImageLogo.setImageResource(R.drawable.person_round);
        }
        else Picasso.get().load(imageUrl).into(circleImageLogo);


        // send message
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = editTextMessage.getText().toString();
                if (!message.equals("")){
                    sendMessage(message);
                    scrollPosition = 0;
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

        getMessage();

        tellUserAmTyping();

        addUserWhenTyping();

        getMyUserTyping();

        getLastSeenAndOnline();

        resetStatusAndMsgCount();

        getPreviousCounts();

        setIsOnline();

        getMsgDeliveryStatus();

        setMsgTickVisibility();

    }

    //---------------------- methods -----------------

        // get messages
    private void getMessage(){
        DatabaseReference refMsg = FirebaseDatabase.getInstance().getReference("Messages").child(userName).child(otherName);
        refMsg.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //  create an object from the modelClass to get the value from the database
                MessageModel messageModel = snapshot.getValue(MessageModel.class);
                modelList.add(messageModel);
                adapter.notifyDataSetChanged();

                // scroll to the new message position number
                recyclerViewChat.scrollToPosition(modelList.size() - scrollPosition - 1); // -------- to display the last message
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
        adapter = new MessageAdapter(modelList, userName, uID, MessageActivity.this);
        recyclerViewChat.setAdapter(adapter);
    }
    public boolean checkConnection()
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
//        statusAndMSgCount.put("newMsgCount", 0);

        refChecks.child(user.getUid()).child(uID).updateChildren(statusAndMSgCount);
    }

    public void sendMessage(String message){

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("message", message);
        messageMap.put("from", userName);
        messageMap.put("timeSent", ServerValue.TIMESTAMP);
        //  now save the message to the database
        String key = dbReference.child(userName).child(otherName).push().getKey();  // create an id for each message
        // set the deliver icon if successful
        dbReference.child(userName).child(otherName).child(key).setValue(messageMap);
        dbReference.child(otherName).child(userName).child(key).setValue(messageMap);

        // --- set the last send message details
        DatabaseReference fReference = FirebaseDatabase.getInstance().getReference("UsersList")
                .child(user.getUid()).child(uID);
        fReference.setValue(messageMap);

        DatabaseReference fReference2 = FirebaseDatabase.getInstance().getReference("UsersList")
                .child(uID).child(user.getUid());
        fReference2.setValue(messageMap);


        // check if there is network connection before sending message
        if(!checkConnection()){
            refChecks.child(uID).child(user.getUid()).child("offCount").setValue(offCount+=1);
        } else{
            refChecks.child(uID).child(user.getUid()).child("offCount").setValue(0);
        }

//        check if the user is in my chat box and reset the count -- newMsgCount & unreadMsg
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

        // save the number of msg sent by me to receiver
        referenceMsgCount = FirebaseDatabase.getInstance().getReference("Checks")
                .child(uID).child(user.getUid());
        referenceMsgCount.child("unreadMsg").setValue(count+=1);

        referenceMsgCount2 = FirebaseDatabase.getInstance().getReference("Checks")
                .child(user.getUid()).child(uID).child("unreadMsg");
        referenceMsgCount2.setValue(0);

    }

    private void setMsgTickVisibility(){
        DatabaseReference refUsersList = FirebaseDatabase.getInstance().getReference("UsersList")
                .child(user.getUid()).child(uID);
        refUsersList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(!snapshot.child("from").exists()){
                    cardViewMsg.setVisibility(View.INVISIBLE);
                }
                else {
                    String lastSender = snapshot.child("from").getValue().toString();
                    if(lastSender.equals(userName)){
                        cardViewMsg.setVisibility(View.VISIBLE);
                    } else {
                        cardViewMsg.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    // get the message delivery status
    public void getMsgDeliveryStatus(){
        refChecks.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                long msgCount = (long) snapshot.child(uID)
                        .child(user.getUid()).child("unreadMsg").getValue();
                long offCount = (long) snapshot.child(uID)
                        .child(user.getUid()).child("offCount").getValue();

                if (msgCount == 0) {
                    imageViewTick.setImageResource(R.drawable.read_orange);
                } else{
                    if(offCount > 0){
                        imageViewTick.setImageResource(R.drawable.message_load);
                    }
                    else imageViewTick.setImageResource(R.drawable.message_tick_one);
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
                    textViewLastSeen.setText("GetMeh");
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
                        String dateString2 = String.valueOf(d);

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

                        int curMonth = dateMonth.get(dateString.substring(4,7));    // Months
                        int lastMonth = dateMonth.get(dateString2.substring(4,7));

                        int curDay = dateNum.get(dateString.substring(0,3));    // Mon - Sun
                        int lastDay = dateNum.get(dateString2.substring(0,3));

                        int dateCur = Integer.parseInt(dateString.substring(8, 10));    // day 1 - 30
                        int dateLast = Integer.parseInt(dateString2.substring(8, 10));

                        if (curMonth - lastMonth == 0)
                        {
                            if (dateCur - dateLast < 7)
                            {
                                if(curDay - lastDay == 0)
                                {
                                    textViewLastSeen.setText("Last seen: Today, \n" + time.toLowerCase());
                                } else if (curDay - lastDay == 1) {
                                    textViewLastSeen.setText("Last seen: Yesterday, \n"+time.toLowerCase());
                                } else if (curDay - lastDay == 2) {
                                    textViewLastSeen.setText("Last seen: 2days ago, \n"+time.toLowerCase());
                                } else if (curDay - lastDay == 3) {
                                    textViewLastSeen.setText("Last seen: 3days ago, \n"+time.toLowerCase());
                                } else if (curDay - lastDay == 4) {
                                    textViewLastSeen.setText("Last seen: 4days ago, \n"+time.toLowerCase());
                                } else if (curDay - lastDay == 5) {
                                    textViewLastSeen.setText("Last seen: 5days ago, \n"+time.toLowerCase());
                                } else if (curDay - lastDay == 6) {
                                    textViewLastSeen.setText("Last seen: 6days ago, \n"+time.toLowerCase());
                                }
                            } else if (dateCur - dateLast >= 7 && dateCur - dateLast < 14) {
                                textViewLastSeen.setText("Last seen: Last week, \n"+lastDay);
                            } else if (dateCur - dateLast >= 14 && dateCur - dateLast < 21) {
                                textViewLastSeen.setText("Last seen: 2 wks ago, \n"+lastDay);
                            } else if (dateCur - dateLast >= 21 && dateCur - dateLast < 27) {
                                textViewLastSeen.setText("Last seen: 3 wks ago, \n"+lastDay);
                            } else {
                                textViewLastSeen.setText("Last seen: a month \nago");
                            }
                        } else if(curMonth - lastMonth == 1){
                            textViewLastSeen.setText("Last seen: one month \nago");
                        } else if(curMonth - lastMonth == 2){
                            textViewLastSeen.setText("Last seen: two months \nago");
                        }else if(curMonth - lastMonth == 3){
                            textViewLastSeen.setText("Last seen: three months \nago");
                        }else if(curMonth - lastMonth == 4){
                            textViewLastSeen.setText("Last seen: Four months \nago");
                        }else if(curMonth - lastMonth == 5){
                            textViewLastSeen.setText("Last seen: Five months \nago");
                        } else {
                            textViewLastSeen.setText("Last seen: Long time");
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    // Alert the DB when I start typing, to notify the receiver
    // clear off msg load tick when network restores.
    private void tellUserAmTyping(){
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

                // clear off msg load tick when network restores.
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

        // add user id to db when user start typing and reset newMsgCount to 0
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
        runnerChaeck = true;

//        finish();
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

        runnerChaeck = true;
//        finish();
        super.onPause();
    }
//
//    @Override
    protected void onResume() {
//        refChecks.child(user.getUid()).child(uID).child("status").setValue(true);
        setIsOnline();
        runnerChaeck = false;
        super.onResume();
    }

}









