package com.pixel.chatapp.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.pixel.chatapp.FragmentListener;
import com.pixel.chatapp.Permission.Permission;
import com.pixel.chatapp.chats.MessageActivity;
import com.pixel.chatapp.chats.MessageAdapter;
import com.pixel.chatapp.chats.MessageModel;
import com.pixel.chatapp.home.fragments.ChatsListFragment;
import com.pixel.chatapp.signup_login.LoginActivity;
import com.pixel.chatapp.general.ProfileActivity;
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

public class MainActivity extends AppCompatActivity implements FragmentListener {

    private TabLayout tabLayoutGeneral;
    private ViewPager2 viewPager2General;
    private ImageView menuOpen, home, menuClose, imageViewLogo, imageViewUserPhoto;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    DatabaseReference refUser = FirebaseDatabase.getInstance().getReference("Users");
    ConstraintLayout scrollMenu, topMainContainer, v;
    private TextView logout, textLightAndDay, textViewDisplayName, textViewUserName;
    Switch darkMoodSwitch;
    CardView cardViewSettings;
    SharedPreferences sharedPreferences;
    private Boolean nightMood;


    //    ------- message declares
    private RecyclerView recyclerViewChat;
    private ImageView imageViewBack;
    private CircleImageView circleImageOnline, circleImageLogo;
    private ImageView imageViewOpenMenu, imageViewCloseMenu, imageViewCancelDel, imageViewCancelReply;
    private ImageView editOrReplyIV, imageViewCalls;
    private ConstraintLayout constraintProfileMenu, constraintDelBody;
    private TextView textViewOtherUser, textViewLastSeen, textViewMsgTyping, textViewReply, nameReply, replyVisible;
    private TextView textViewDelMine, textViewDelOther, textViewDelAll;
    private EditText editTextMessage;
    private CircleImageView circleSendMessage;
    private CardView cardViewMsg, cardViewReply;

    private String otherUserUid, otherUserName, myUserName, imageUri;

    List<Object> msgBodyArray = new ArrayList<>();
    DatabaseReference refMessages, refChecks, refUsers, refMsgCheck, refMsgSeen;
    DatabaseReference referenceMsgCount2, referenceMsgCount;
    FirebaseUser user;
    private MessageAdapter adapter;
    private List<MessageModel> modelList;
    int scrollPosition;

    private boolean checkMsg = false;
    //    SharedPreferences sharedPreferences;

    private long count = 0, offCount = 0, newMsgCount = 0;
    private Boolean runnerCheck = false;
    private Map<String, Integer> dateNum, dateMonth;
    private String idKey, listener = "no", replyFrom, networkListener = "yes", insideChat = "no";
    private String audioPath;
    private MediaRecorder mediaRecorder;
    private Permission permissions;
    private static final int PAGE_SIZE = 20; // Number of items to fetch per page
    private int currentPage = 1; // Current page number
    ConstraintLayout constraintMsgBody;
    private ChatsListFragment chatsListFragment;

    Handler handler = new Handler(Looper.getMainLooper());

    RecordView recordView;
    RecordButton recordButton;

    //  ---------- msg end


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //      --------- message ids


        imageViewBack = findViewById(R.id.imageViewBackArrow9);
        textViewOtherUser = findViewById(R.id.textViewName9);
        editTextMessage = findViewById(R.id.editTextMessage9);
        circleSendMessage = findViewById(R.id.fab9);
        imageViewOpenMenu = findViewById(R.id.imageViewUserMenu29);
//        imageViewCloseMenu = findViewById(R.id.imageViewCancel);
//        constraintProfileMenu = findViewById(R.id.constraintProfileMenu9);
        textViewLastSeen = findViewById(R.id.textViewStatus9);
        textViewMsgTyping = findViewById(R.id.textViewTyping29);
        circleImageOnline = findViewById(R.id.circleImageOnline9);
        circleImageLogo = findViewById(R.id.circleImageLogo9);
//        constraintDelBody = findViewById(R.id.constDelBody9);
//        textViewDelMine = findViewById(R.id.textViewDelMine9);
//        textViewDelOther = findViewById(R.id.textViewDelOther9);
//        textViewDelAll = findViewById(R.id.textViewDelEveryone9);
//        imageViewCancelDel = findViewById(R.id.imageViewCancelDel9);
        cardViewReply = findViewById(R.id.cardViewReply9);
        textViewReply = findViewById(R.id.textViewReplyText9);
        imageViewCancelReply = findViewById(R.id.imageViewCancleReply9);
        editOrReplyIV = findViewById(R.id.editOrReplyImage9);
        nameReply = findViewById(R.id.fromTV9);
        replyVisible = findViewById(R.id.textReplying9);
        imageViewCalls = findViewById(R.id.imageViewCalls9);
        constraintMsgBody = findViewById(R.id.constraintMsgBody);

        // audio swipe button option
        recordView = (RecordView) findViewById(R.id.record_view9);
        recordButton = (RecordButton) findViewById(R.id.record_button9);
        recordButton.setRecordView(recordView);

        recyclerViewChat = findViewById(R.id.recyclerViewChat9);

        recyclerViewChat.setHasFixedSize(true);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));

        refMessages = FirebaseDatabase.getInstance().getReference("Messages");
        user = FirebaseAuth.getInstance().getCurrentUser();

        refChecks = FirebaseDatabase.getInstance().getReference("Checks");
        refUsers = FirebaseDatabase.getInstance().getReference("Users");
        refMsgCheck = FirebaseDatabase.getInstance().getReference("MsgCheck");
        refMsgSeen = FirebaseDatabase.getInstance().getReference("MsgSeen");


        // ----------------------

        tabLayoutGeneral = findViewById(R.id.tabLayerMain);
        viewPager2General = findViewById(R.id.viewPageMain);
        menuOpen = findViewById(R.id.imageViewMenu);
        home = findViewById(R.id.imageViewHome);
        menuClose = findViewById(R.id.imageViewMenuClose);
        scrollMenu = findViewById(R.id.constraintMenu);
        logout = findViewById(R.id.textViewLogOut);
        imageViewLogo = findViewById(R.id.circleUserImage);
        imageViewUserPhoto = findViewById(R.id.imageViewUserPhoto);
        textViewDisplayName = findViewById(R.id.textViewDisplayName2);
        textViewUserName = findViewById(R.id.textViewUserName2);
        v = findViewById(R.id.v);
        darkMoodSwitch = findViewById(R.id.switch1);
        textLightAndDay = findViewById(R.id.textView13);
        topMainContainer = findViewById(R.id.constraintMsgContainer);
        cardViewSettings = findViewById(R.id.cardViewSettings);


        ViewPagerMainAdapter adapterV = new ViewPagerMainAdapter(getSupportFragmentManager(), getLifecycle());

        viewPager2General.setAdapter(adapterV);

        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayoutGeneral, viewPager2General, true, true,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        switch (position){
                            case 0:
                                tab.setText("Chats");
                                break;
                            case 1:
                                tab.setText("Tournaments");
                                break;
                            case 2:
                                tab.setText("Hosts");
                                break;
                        }
                    }
                });
        tabLayoutMediator.attach();

        // set my online presence to be true
        refUser.child(auth.getUid()).child("presence").setValue(1);

        // Dark mood setting
        sharedPreferences = this.getSharedPreferences("MOOD", Context.MODE_PRIVATE);
        nightMood = sharedPreferences.getBoolean("MoodStatus", false);

        if(nightMood){
            darkMoodSwitch.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            textLightAndDay.setText("Light");
        } else {
            textLightAndDay.setText("Dark");
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        };

        darkMoodSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(nightMood){
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        sharedPreferences.edit().putBoolean("MoodStatus", false).apply();
                } else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    sharedPreferences.edit().putBoolean("MoodStatus", true).apply();
                }
            }
        });

        // open the menu option
        menuOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                scrollMenu.setVisibility(View.VISIBLE);
                viewPager2General.setVisibility(View.INVISIBLE);
//                v.setBackgroundColor(getResources().);
            }
        });

        // open menu option via logo too
        imageViewLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollMenu.setVisibility(View.VISIBLE);
                viewPager2General.setVisibility(View.INVISIBLE);
            }
        });

        // close the open option when background is clicked
        v.setOnClickListener(view -> {

            if (scrollMenu.getVisibility() == View.VISIBLE){
                scrollMenu.setVisibility(View.GONE);
                viewPager2General.setVisibility(View.VISIBLE);
            }
        });

        // close the open option
        menuClose.setOnClickListener(view -> {

            viewPager2General.setVisibility(View.VISIBLE);
            scrollMenu.setVisibility(View.GONE);
        });

        //logout
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                logoutOption();
            }
        });

        // settings
        cardViewSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            }
        });

        // send message
        circleSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = editTextMessage.getText().toString().trim();
                if (!message.equals("")){

                    sendMessage(message, 0);    // 0 is for text while 1 is for voice note
                    editTextMessage.setText("");
                    listener = "no";
                    scrollPosition = 0;
//                    adapter
                    cardViewReply.setVisibility(View.GONE);
                    nameReply.setVisibility(View.GONE);
                    replyVisible.setVisibility(View.GONE);
                }
            }
        });

        setUserDetails();

//        sendMsgAdapter(adapter, 1);
//        System.out.println("Check this method if it calls");
    }

    //  --------------- methods --------------------

    // after writing your method in the main activity, declare it on the FragmentListener interface and fetch it from the fragment
    //  ---------- interface


    @Override
    public void callAllMethods() {
        reloadUnsentMsg();
        getMyUserTyping();
        tellUserAmTyping_AddUser();
    }

    @Override
    public void msgBodyVisibility(int data, String otherName, String imageUrl, String userName) {

        constraintMsgBody.setVisibility(data);
        menuOpen.setClickable(false);

        checkMsg = true;

        textViewOtherUser.setText(otherName);   // display their userName on top of their page

//         get user image
        if (imageUrl.equals("null")) {
            circleImageLogo.setImageResource(R.drawable.person_round);
        }
        else Picasso.get().load(imageUrl).into(circleImageLogo);

        otherUserName = otherName;
        myUserName = userName;
        imageUri = imageUrl;

    }

    @Override
    public void sendMsgAdapter(MessageAdapter adapter1, int scroll) {
        recyclerViewChat.setAdapter(adapter1);
//        adapter1.notifyDataSetChanged();
        recyclerViewChat.scrollToPosition(adapter1.getItemCount() - 1);
        adapter = adapter1;
        System.out.println("Land Count "+adapter1.getItemCount());
    }

    public void sendMessage(String message, int type) {

            // 700024 --- tick one msg  // 700016 -- seen msg   // 700033 -- load

        new Thread(new Runnable() {
            @Override
            public void run() {
                //        String key = refMessages.child(userName).child(otherName).push().getKey();  // create an id for each message
                int visibility = 8;
                int msgStatus = 700024;

//            if(networkListener == "no"){
//                msgStatus = 700033;
////            refMsgCheck.child(user.getUid()).push().child("loadKey").setValue(key);     // push the un-deliver msg key to db
//            }

                Map<String, Object> messageMap = new HashMap<>();
                messageMap.put("from", myUserName);

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
//            messageMap.put("voicenote", vn);
                messageMap.put("msgStatus", msgStatus);
                messageMap.put("visibility", visibility);
                messageMap.put( "timeSent", ServerValue.TIMESTAMP);

                //  now save the message to the database
                refMessages.child(myUserName).child(otherUserName).push().setValue(messageMap);
                refMessages.child(otherUserName).child(myUserName).push().setValue(messageMap);



                // send delivery id to MsgSeen
//        refMsgSeen.child(uID).child(user.getUid()).push().child("seenKey").setValue(key);

                if (listener == "no" || listener == "reply")
                {
// --- set the last send message details
                    DatabaseReference fReference = FirebaseDatabase.getInstance().getReference("UsersList")
                            .child(user.getUid()).child(otherUserUid);
                    fReference.setValue(messageMap);

                    DatabaseReference fReference2 = FirebaseDatabase.getInstance().getReference("UsersList")
                            .child(otherUserUid).child(user.getUid());
                    fReference2.setValue(messageMap);

//                checkAndSaveCounts_SendMsg();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        recyclerViewChat.scrollToPosition(adapter.getItemCount() - 1);
                        adapter.notifyDataSetChanged();
                        System.out.println("Check count "+adapter.getItemCount());

                    }
                });

            }
        }).start();

//        if (checkMsg)

    }

    public void getMsg(){
        DatabaseReference refMsg = FirebaseDatabase.getInstance().getReference("Messages").child(myUserName).child(otherUserName);
        refMsg.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                List<MessageModel> newMessages = new ArrayList<>();
                modelList.clear();

                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    MessageModel messageModel = snapshot1.getValue(MessageModel.class);
                    messageModel.setIdKey(snapshot1.getKey());
                    modelList.add(messageModel);
                    adapter.notifyDataSetChanged();
                }


                // Update the modelList2 with the new messages
//                modelList.addAll(newMessages);

                // Notify the adapter that the data has changed

                recyclerViewChat.scrollToPosition(adapter.getItemCount() - 1);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled if needed
            }
        });

        adapter = new MessageAdapter(modelList, myUserName, otherUserUid, MainActivity.this);

//        msgBodyVisibility(View.VISIBLE, adapter, 1, otherUserName, imageUri, myUserName);
//        adapter.notifyDataSetChanged();
        recyclerViewChat.setAdapter(adapter);
//        System.out.println("This is adapter" + adapter);
    }

    @Override
    public void onEditMessage(String itemList, int icon) {
        editTextMessage.setText(itemList);
        editOrReplyIV.setImageResource(icon);

        // pop up keyboard
        editTextMessage.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(editTextMessage, InputMethodManager.SHOW_IMPLICIT);

        cardViewReply.setVisibility(View.VISIBLE);

    }

    // get last seen and set inbox status to be true
    @Override
    public void getLastSeenAndOnline(String otherUid) {

        otherUserUid = otherUid;

        // get last seen
        try{
            refUsers.child(otherUid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    try{
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
                    } catch (Exception e){
                        textViewLastSeen.setText("WinnerChat");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }catch (Exception e){
            textViewLastSeen.setText("WinnerChat");

        }

    }

    @Override
    public void msgBackgroundActivities(String otherUid) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                // set in chat box status true
                refChecks.child(user.getUid()).child(otherUid).child("status").setValue(true);

                // set responds to pend always      ------- will change later to check condition if user is still an active call
                refChecks.child(user.getUid()).child(otherUid).child("vCallResp").setValue("pending");

                runnerCheck = false;
                insideChat = "yes";
            }
        }).start();
    }

    // check for network and reloadUnsent Msg       //  runnable
    private void reloadUnsentMsg(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!runnerCheck) {
                    try {
                        // clear off msg load tick when network restores.
                        if(checkNetworkConnection()){
                            refChecks.child(otherUserUid).child(user.getUid()).child("offCount").setValue(0);
                            networkListener = "yes";

//                          refreshMsgDeliveryIcon();   // reload unsentMsg

                        } else {
                            networkListener = "no";
                        }

                        if(checkMsg){
                            System.out.println("Count "+adapter.getItemCount());
                        }

                        Thread.sleep(2000); // Wait for 2 seconds before running the task again

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        refChecks.child(otherUserUid).child(user.getUid()).child("typing").setValue(0);
                    }
                }
            }
        });

        thread.start();

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
                    refChecks.child(otherUserUid).child(user.getUid()).child("typing").setValue(1);
                } else {
                    circleSendMessage.setVisibility(View.GONE);
                    recordButton.setVisibility(View.VISIBLE);
                    refChecks.child(otherUserUid).child(user.getUid()).child("typing").setValue(0);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // Adding User to chat fragment: Latest Chats with contacts
                        final DatabaseReference chatRef = FirebaseDatabase.getInstance()
                                .getReference("ChatList")
                                .child(user.getUid()).child(otherUserUid);

                        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()){
                                    chatRef.child("id").setValue(otherUserUid);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        final DatabaseReference chatRef2 = FirebaseDatabase.getInstance()
                                .getReference("ChatList")
                                .child(otherUserUid).child(user.getUid());

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
                        statusCheck2.child(otherUserUid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                try{
                                    boolean statusState = (boolean) snapshot.child(user.getUid())
                                            .child("status").getValue();

                                    // receiver should be 0
                                    if(statusState == true) {
                                        statusCheck2.child(otherUserUid).child(user.getUid()).child("newMsgCount").setValue(0);
                                    }

                                    // Mine should be 0
                                    statusCheck2.child(user.getUid()).child(otherUserUid).child("newMsgCount").setValue(0);

                                } catch (Exception e){
                                    statusCheck2.child(otherUserUid).child(user.getUid())
                                            .child("status").setValue(false);
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                }).start();


            }
        });
    }

    // show when user is typing
    public void getMyUserTyping()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                refChecks.child(user.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        try {
                            long typing = (long) snapshot.child(otherUserUid).child("typing").getValue();

                            handler.post(new Runnable() {   //
                                @Override
                                public void run() {

                                    if(typing == 1){
                                        textViewMsgTyping.setText("typing...");
                                    } else{
                                        textViewMsgTyping.setText("");
                                    }
                                }
                            });
                        } catch (Exception e){
                            refChecks.child(user.getUid()).child(otherUserUid).child("typing").setValue(0);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }).start();

    }

    public boolean checkNetworkConnection()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo == null) return false;

        return networkInfo.isConnected();
    }

    //  -----------------------------

        // set user image on settings
    private void setUserDetails(){
        refUser.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String imageUrl = snapshot.child("image").getValue().toString();
                String userName = snapshot.child("userName").getValue().toString();

                if (imageUrl.equals("null")) {
                    imageViewUserPhoto.setImageResource(R.drawable.person_round);
                }
                else Picasso.get().load(imageUrl).into(imageViewUserPhoto);

                textViewDisplayName.setText(userName);      // change later to Display name
                textViewUserName.setText("@"+userName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void logoutOption()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("GetMeh");
        builder.setMessage("Are you sure you want to logout?");
        builder.setCancelable(false);
        builder.setNegativeButton("Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                auth.signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });
        builder.setPositiveButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.create().show();
    }

//    @Override
//    public void onBackPressed() {
//        refUser.child(auth.getUid()).child("presence").setValue(ServerValue.TIMESTAMP);
//        super.onBackPressed();
//    }

    @Override
    protected void onPause() {
        refUser.child(user.getUid()).child("presence").setValue(ServerValue.TIMESTAMP);
        super.onPause();
    }

    @Override
    protected void onResume() {
        refUser.child(auth.getUid()).child("presence").setValue(1);
        super.onResume();
    }

    @Override
    public void onBackPressed() {

        if(constraintMsgBody.getVisibility() == View.VISIBLE){
            constraintMsgBody.setVisibility(View.INVISIBLE);
            cardViewReply.setVisibility(View.GONE);
            menuOpen.setClickable(true);   // set the home menu page clickable false
            editTextMessage.setText("");    // clear message not sent

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Map<String, Object> mapUpdate = new HashMap<>();
                    mapUpdate.put("status", false);
                    mapUpdate.put("newMsgCount", 0);
                    refChecks.child(user.getUid()).child(otherUserUid).updateChildren(mapUpdate);

                    // set responds to pend always      ------- will change later to check condition if user is still an active call
//                    refChecks.child(user.getUid()).child(otherUserUid).child("vCallResp").setValue("pending");

                    runnerCheck = true;
                    insideChat = "no";
                }
            }).start();

        } else {
            super.onBackPressed();
        }
    }
}










