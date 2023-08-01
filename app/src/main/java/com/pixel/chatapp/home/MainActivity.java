package com.pixel.chatapp.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

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
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import com.pixel.chatapp.chats.MessageAdapter;
import com.pixel.chatapp.chats.MessageModel;
import com.pixel.chatapp.home.fragments.ChatsListFragment;
import com.pixel.chatapp.signup_login.LoginActivity;
import com.pixel.chatapp.general.ProfileActivity;
import com.pixel.chatapp.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
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
    private ConstraintLayout conTopUserDetails, conUserClick;
    private ImageView editOrReplyIV, imageViewCalls;
    private ConstraintLayout constraintProfileMenu, constraintDelBody;
    private TextView textViewOtherUser, textViewLastSeen, textViewMsgTyping, textViewReply, nameReply, replyVisible;
    private TextView textViewDelMine, textViewDelOther, textViewDelAll;
    private EditText editTextMessage;
    private CircleImageView circleSendMessage;
    private CardView cardViewMsg, cardViewReply;

    private String otherUserUid, otherUserName, myUserName, imageUri;

    List<Object> msgBodyArray = new ArrayList<>();
    DatabaseReference refMessages, refMsgFast, refChecks, refUsers, refMsgCheck, refMsgSeen;
    DatabaseReference referenceMsgCount2, referenceMsgCount;
    FirebaseUser user;
//    private MessageAdapter adapter;
    int scrollPosition;

    private boolean checkMsg = true;

    private long count = 0, offCount = 0, newMsgCount = 0;
    private Boolean runnerCheck = false;
    private Map<String, Integer> dateNum, dateMonth;
    private String idKey, listener = "no", replyFrom, networkListener = "yes", insideChat = "no";
    private String audioPath;
    private MediaRecorder mediaRecorder;
    private Permission permissions;
    private static final int PAGE_SIZE = 20; // Number of items to fetch per page
    private int currentPage; // Current page number
    ConstraintLayout constraintMsgBody;
    private ChatsListFragment chatsListFragment;

    Handler handler = new Handler(Looper.getMainLooper());

    RecordView recordView;
    RecordButton recordButton;

//    private Bundle savedChildViewState; // Store the state of the child view

    public static int readMsgDb;  // 1 is read, 0 is no_read
    public static int readMsgDb2;  // 1 is read, 0 is no_read

    private Map<String, List<MessageModel>> modelListMap;
    private Map<String, RecyclerView> recyclerMap;

    private List<String> stringList;
    ConstraintLayout recyclerContainer;

    public interface ChatVisibilityListener {
        void constraintChatVisibility(int position, int isVisible);
    }

    //  ---------- msg end


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //      --------- message ids

        conTopUserDetails = findViewById(R.id.contraintTop9);
        conUserClick = findViewById(R.id.constraintNextTop9);
        recyclerContainer = findViewById(R.id.constraintRecyler);

        imageViewBack = findViewById(R.id.imageViewBackArrow9);
        textViewOtherUser = findViewById(R.id.textViewName9);
        editTextMessage = findViewById(R.id.editTextMessage9);
        circleSendMessage = findViewById(R.id.fab9);
        imageViewOpenMenu = findViewById(R.id.imageViewUserMenu29);
        imageViewCloseMenu = findViewById(R.id.imageViewCancel9);
        constraintProfileMenu = findViewById(R.id.constraintProfileMenu9);
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
        refMsgFast = FirebaseDatabase.getInstance().getReference("MsgFast");

        user = FirebaseAuth.getInstance().getCurrentUser();

        refChecks = FirebaseDatabase.getInstance().getReference("Checks");
        refUsers = FirebaseDatabase.getInstance().getReference("Users");
        refMsgCheck = FirebaseDatabase.getInstance().getReference("MsgCheck");
        refMsgSeen = FirebaseDatabase.getInstance().getReference("MsgSeen");

        stringList = new ArrayList<>();
        recyclerMap = new HashMap<>();
        modelListMap = new HashMap<>();
        readMsgDb = 0;  // 1 is read, 0 is no_read
//        modelListAllMsg = new ArrayList<>();


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

        new CountDownTimer(15000, 1000){
            @Override
            public void onTick(long l) {
                readMsgDb2 = 0;
            }

            @Override
            public void onFinish() {
                readMsgDb2 = 1;
            }
        }.start();

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

        //  Return back, close msg container
        imageViewBack.setOnClickListener(view -> {
            hideKeyboard();
            onBackPressed();
            insideChat = "no";
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

        // open user menu
        imageViewOpenMenu.setOnClickListener(view -> constraintProfileMenu.setVisibility(View.VISIBLE));

        // close user menu
        imageViewCloseMenu.setOnClickListener(view -> constraintProfileMenu.setVisibility(View.GONE));
        constraintProfileMenu.setOnClickListener(view -> constraintProfileMenu.setVisibility(View.GONE));


        // send message
        circleSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = editTextMessage.getText().toString().trim();
                if (!message.equals("")){

                    sendMessage(message, 0);    // 0 is for text while 1 is for voice note
                    saveLastMsgDetails(message, 0);
                    editTextMessage.setText("");
                    listener = "no";
                    scrollPosition = 0;

                    cardViewReply.setVisibility(View.GONE);
                    nameReply.setVisibility(View.GONE);
                    replyVisible.setVisibility(View.GONE);
                }
            }
        });

        setUserDetails();

//        getMessage();
//        sendMsgAdapter(adapter, 1);
//        System.out.println("MainActivity create2 ");
    }

    //  --------------- methods --------------------

    // after writing your method in the main activity, declare it on the FragmentListener interface and fetch it from the fragment
    //  ---------- interface

    @Override
    public void callAllMethods() {
        reloadUnsentMsg();
        getMyUserTyping();
        tellUserAmTyping_AddUser();
        getPreviousCounts();
    }

    @Override
    public void msgBodyVisibility(int data, String otherName, String imageUrl, String userName, String uID) {

        // edit and activate later
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerMap.get(otherName).getLayoutManager();
        int lastPosition = layoutManager.getItemCount() - 1;   // retrieve previous position and scroll
        layoutManager.scrollToPosition(lastPosition);

        constraintMsgBody.setVisibility(data);
        conTopUserDetails.setVisibility(View.VISIBLE);
        conUserClick.setVisibility(View.VISIBLE);

        textViewOtherUser.setText(otherName);   // display their userName on top of their page

//         get user image
        if (imageUrl.equals("null")) {
            circleImageLogo.setImageResource(R.drawable.person_round);
        }
        else Picasso.get().load(imageUrl).into(circleImageLogo);

        otherUserName = otherName;
        myUserName = userName;
        imageUri = imageUrl;

        checkMsg = false;

        try{
            recyclerViewChatVisibility(otherName);
        } catch (Exception e){
            System.out.println("Error occur " + e.getMessage());
        }

        topMainContainer.setVisibility(View.INVISIBLE);
        tabLayoutGeneral.setVisibility(View.INVISIBLE);

    }

    public void recyclerViewChatVisibility(String otherName){
        for (int i = 0; i < recyclerContainer.getChildCount(); i++) {
            View child = recyclerContainer.getChildAt(i);
            if (child == recyclerMap.get(otherName)) {

                child = recyclerMap.get(otherName);
                child.setVisibility(View.VISIBLE);  // make only child layer visible

            } else{
                child.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override       // run only once
    public void sendRecyclerView(RecyclerView recyclerChat, String otherName) {

        recyclerMap.put(otherName, recyclerChat);  // save empty recyclerView of each user to their username

        if (recyclerChat.getParent() != null) {
            // Remove the clicked RecyclerView from its current parent
            ((ViewGroup) recyclerChat.getParent()).removeView(recyclerChat);
        }
        recyclerContainer.addView(recyclerChat);

    }

    @Override       // run only once
    public void getMessage(String userName, String otherName, String uID, Context mContext){

        retrieveMessages(userName, otherName, uID, mContext);

    }

    private Map<String, Object> setMessage(String message, int type){
        // 700024 --- tick one msg  // 700016 -- seen msg   // 700033 -- load

        int visibility = 8;
        int msgStatus = 700024;

        if(networkListener == "no"){
            msgStatus = 700033;
            //            refMsgCheck.child(user.getUid()).push().child("loadKey").setValue(key);     // push the un-deliver msg key to db
        }

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

        return messageMap;
    }

    public void sendMessage(String message, int type) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String key = refMsgFast.child(myUserName).child(otherUserName).push().getKey();  // create an id for each message

                refMsgFast.child(myUserName).child(otherUserName).child(key).setValue(setMessage(message, type));
                refMsgFast.child(otherUserName).child(myUserName).child(key).setValue(setMessage(message, type));
// condition attach here for edit and delete reference purpose
//                if(readMsgDb == 0){
//
//                    //  now save the message to the database
//                    refMessages.child(myUserName).child(otherUserName).child(key).setValue(setMessage(message, type));
//                    refMessages.child(otherUserName).child(myUserName).child(key).setValue(setMessage(message, type))
//                            .addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            if(task.isSuccessful()) readMsgDb = 1;
//                        }
//                    });
//                }
//                else {      // save to new msg database for fast response and data fetch
//                    refMsgFast.child(myUserName).child(otherUserName).child(key).setValue(setMessage(message, type));
//                    refMsgFast.child(otherUserName).child(myUserName).child(key).setValue(setMessage(message, type));
//                }

            }
        }).start();

//        stringList.add(message);

//        recyclerViewChat.setAdapter(getMsg());
//        recyclerViewChat.scrollToPosition(getMsg().getItemCount() - 1);
//        if (checkMsg)

        // check back later
//        List<MessageModel> modelList1 = new ArrayList<>();
//
//        MessageModel messageModel = new MessageModel(message, myUserName, "", 0, "",
//                "", 8, "", 700033, type);
//        modelList1.add(messageModel);
//        MessageAdapter adapter1 = new MessageAdapter(modelList1, myUserName, otherUserUid, MainActivity.this);   // check later

//        recyclerMap.get(otherUserName).setAdapter(adapter1);

    }

    private void saveLastMsgDetails(String message, int type){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (listener == "no" || listener == "reply")
                {
// --- set the last send message details
                    DatabaseReference fReference = FirebaseDatabase.getInstance().getReference("UsersList")
                            .child(user.getUid()).child(otherUserUid);
                    fReference.setValue(setMessage(message, type));

                    DatabaseReference fReference2 = FirebaseDatabase.getInstance().getReference("UsersList")
                            .child(otherUserUid).child(user.getUid());
                    fReference2.setValue(setMessage(message, type));

                    checkAndSaveCounts_SendMsg();
                }
            }
        }).start();
    }

    //  get the previous count of new msg and add to it from sendMessage
    private void checkAndSaveCounts_SendMsg(){

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // save the number of msg sent by me to receiver
                referenceMsgCount = FirebaseDatabase.getInstance().getReference("Checks")
                        .child(otherUserUid).child(user.getUid());
                referenceMsgCount.child("unreadMsg").setValue(count+=1);

                referenceMsgCount2 = FirebaseDatabase.getInstance().getReference("Checks")
                        .child(user.getUid()).child(otherUserUid).child("unreadMsg");
                referenceMsgCount2.setValue(0);

                // check if there is network connection before sending message
                if(!checkNetworkConnection()){
                    refChecks.child(otherUserUid).child(user.getUid()).child("offCount").setValue(offCount+=1);
                } else{
                    refChecks.child(otherUserUid).child(user.getUid()).child("offCount").setValue(0);
                }

                //      check if the user is in my chat box and reset the count -- newMsgCount & unreadMsg
                DatabaseReference statusCheck = FirebaseDatabase.getInstance().getReference("Checks");
                statusCheck.child(otherUserUid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        try{
                            boolean statusState = (boolean) snapshot.child(user.getUid())
                                    .child("status").getValue();

                            if(statusState == true) {
                                referenceMsgCount.child("unreadMsg").setValue(0);
                            } else if (statusState == false) {
                                // increase the new msg count
                                statusCheck.child(otherUserUid).child(user.getUid())
                                        .child("newMsgCount").setValue(newMsgCount+1);
                            }
                        } catch (Exception e){
                            statusCheck.child(otherUserUid).child(user.getUid()).child("status").setValue(false);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        thread.start();
    }

    public void retrieveMessages(String userName, String otherName, String uID, Context mContext){

        List<MessageModel> modelListNewMsg = new ArrayList<>(); // save all msg as old msg

        // create new modelList and Adapter instance so it will replace only the item that's updates
        List<MessageModel> modelListAllMsg = new ArrayList<>();

        MessageAdapter adapter = new MessageAdapter(modelListAllMsg, userName, uID, mContext); // initialise adapter

        // loop through New Message (MsgFast) and Old Message for status state before proceeding
        new CountDownTimer(2000, 1000){
            @Override
            public void onTick(long l) {
                // push new msg that has been read by user to the old Message database container
                // and delete from the new message database(refMsgFast) container
                refMsgFast.child(userName).child(otherName).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot snapshotNew : snapshot.getChildren()){

                            // check if msg has been read, then add it to the Old Message Database
                            if((long)snapshotNew.child("msgStatus").getValue() == 700024){

                                String key = snapshotNew.getKey();
                                MessageModel messageModel = snapshotNew.getValue(MessageModel.class);
                                // add to the read messages to old message db
                                refMessages.child(userName).child(otherName).child(key).setValue(messageModel);
                                refMessages.child(otherName).child(userName).child(key).setValue(messageModel)
                                        //  delete from the database if the msg has been read
                                        .addOnCompleteListener(task -> {
                                            refMsgFast.child(userName).child(otherName).child(key).removeValue();
                                            refMsgFast.child(otherName).child(userName).child(key).removeValue();
                                        });
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onFinish() {
                // retrieve all message from the database just once
                refMessages.child(userName).child(otherName).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        modelListAllMsg.clear();

                        for (DataSnapshot snapshotOld : snapshot.getChildren()){

                            MessageModel messageModelOld = snapshotOld.getValue(MessageModel.class);
                            messageModelOld.setIdKey(snapshotOld.getKey());  // set msg keys to the adaptor
                            modelListAllMsg.add(messageModelOld);
                            adapter.notifyDataSetChanged();

                            // save the message to map
//                                modelListSave.add(messageModelOld);
//                                modelListMap.put(otherName, modelListSave);
                        }

//                            checkNewMsg(otherName); // notify the recyclerView inside the map when there's update
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                refMsgFast.child(userName).child(otherName).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        modelListAllMsg.removeAll(modelListNewMsg);   // remove previous msg fetched by firebase

                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            MessageModel messageModel = snapshot1.getValue(MessageModel.class);
                            messageModel.setIdKey(snapshot1.getKey());
                            modelListAllMsg.add(messageModel);

                            // use it to delete the repeated item later since firebase do fetch all data on any update
                            modelListNewMsg.add(messageModel);
                        }

                        for (int i = 0; i < recyclerContainer.getChildCount(); i++) {
                            View child = recyclerContainer.getChildAt(i);
                            if (child == recyclerMap.get(otherName)){
                                RecyclerView recyclerView = (RecyclerView) child;
                                if ( insideChat == "yes" || readMsgDb2 == 0) {  // change later to individual
                                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                                    int lastPosition = layoutManager.getItemCount() - 1;     // retrieve previous position and scroll
                                    layoutManager.scrollToPosition(lastPosition);
                                }
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        }.start();

        adapter.setFragmentListener((FragmentListener) mContext);

        recyclerMap.get(otherName).setAdapter(adapter);

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

                // set my status to be true in case I receive msg, it will be tick as seen
                Map <String, Object> statusAndMSgCount = new HashMap<>();
                statusAndMSgCount.put("status", true);
                statusAndMSgCount.put("unreadMsg", 0);

                refChecks.child(user.getUid()).child(otherUserUid).updateChildren(statusAndMSgCount);

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

//        if (networkInfo == null || !networkInfo.isConnected()) {
//            return false;
//        }
//
//        // Check internet reachability (optional)
//        try {
//            HttpURLConnection urlc = (HttpURLConnection) (new URL("https://www.google.com").openConnection());
//            urlc.setRequestProperty("User-Agent", "Test");
//            urlc.setRequestProperty("Connection", "close");
//            urlc.setConnectTimeout(1500); // Timeout in milliseconds
//            urlc.connect();
//            return (urlc.getResponseCode() == 200);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return false;
    }

    //  Get all previous counts of unreadMsg and offCount and newMsgCount
    private void getPreviousCounts(){

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                DatabaseReference referenceMsgCountCheck = FirebaseDatabase.getInstance().getReference("Checks")
                        .child(otherUserUid).child(user.getUid());
                referenceMsgCountCheck.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        try{
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
                        } catch (Exception e){
                            referenceMsgCountCheck.child("unreadMsg").setValue(0);
                            referenceMsgCountCheck.child("offCount").setValue(0);
                            referenceMsgCountCheck.child("newMsgCount").setValue(0);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });

        thread.start();
    }

    // Method to hide the soft keyboard when needed
    private void hideKeyboard() {
        View currentFocusView = getCurrentFocus();
        if (currentFocusView != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocusView.getWindowToken(), 0);
        }
    }

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
            topMainContainer.setVisibility(View.VISIBLE);
            tabLayoutGeneral.setVisibility(View.VISIBLE);
            conTopUserDetails.setVisibility(View.INVISIBLE);
            conUserClick.setVisibility(View.INVISIBLE);

            editTextMessage.setText("");    // clear message not sent

            textViewLastSeen.setText("");   // clear last seen
            circleImageOnline.setVisibility(View.INVISIBLE);

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










