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
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
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
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.pixel.chatapp.FragmentListener;
import com.pixel.chatapp.NetworkChangeReceiver;
import com.pixel.chatapp.Permission.Permission;
import com.pixel.chatapp.chats.MessageAdapter;
import com.pixel.chatapp.chats.MessageModel;
import com.pixel.chatapp.model.EditMessageModel;
import com.pixel.chatapp.model.PinMessageModel;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements FragmentListener {

    private TabLayout tabLayoutGeneral;
    private ViewPager2 viewPager2General;
    private ImageView menuOpen, home, menuClose, imageViewLogo, imageViewUserPhoto;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    DatabaseReference refUser = FirebaseDatabase.getInstance().getReference("Users");
    ConstraintLayout scrollMenu, topMainContainer, mainViewConstraint;
    private TextView logout, textLightAndDay, textViewDisplayName, textViewUserName;
    Switch darkMoodSwitch;
    CardView cardViewSettings;
    SharedPreferences sharedPreferences;
    private Boolean nightMood;


    //    ------- message declares
    private ImageView imageViewBack;
    private CircleImageView circleImageOnline, circleImageLogo;
    private ImageView imageViewOpenMenu, imageViewCloseMenu, imageViewCancelDel, replyOrEditCancel_IV;
    private ConstraintLayout conTopUserDetails, conUserClick;
    private ImageView editOrReplyIV, imageViewCalls;
    private TextView textViewOtherUser, textViewLastSeen, textViewMsgTyping, textViewReplyOrEdit, nameReply, replyVisible;
    private ConstraintLayout constraintProfileMenu, constraintDelBody;

    // -----------      pin declares
    private ConstraintLayout pinIconsContainer, pinMsgBox_Constr;
    public static ConstraintLayout pinMsgContainer, pinOptionBox;
    private ImageView hidePinMsg_IV, pinPrivateIcon_IV, pinPublicIcon_IV;
    private int pinTotalNum, pinNextPrivate;
    private String msgId, message;
    private Object timeStamp;
    private ImageView arrowUp, arrowDown, cancelPinOption;
    private TextView totalPinPrivate_TV, pinCount_TV, pinMsg_TV, totalPinPublic_TV;
    public static TextView pinMineTV, pinEveryoneTV;
    private TextView textViewDelMine, textViewDelOther, textViewDelAll;
    private EditText editTextMessage;
    private CircleImageView circleSendMessage;
    private CardView cardViewMsg, cardViewReplyOrEdit;
    private ImageView scrollPositionIV, sendIndicator;
    private TextView scrollCountTV, receiveIndicator;
    private ImageView emoji_IV, file_IV, camera_IV;
    // network settings
    private ConstraintLayout constrNetConnect, constrNetork;
    public static String otherUserUid, otherUserName, myUserName, imageUri;
//    public static String goToLastMessage;
    public static int goToNum;
    public static Boolean goToLastMessage = false;

    DatabaseReference refMessages, refMsgFast, refChecks, refUsers, refLastDetails,
            refEditMsg, refDeleteMsg, refPrivatePinChat, refPublicPinChat;
    FirebaseUser user;
    private int scrollNum = 0;
    private long count = 0, newMsgCount = 0;
    private Map<String, Integer> dateNum, dateMonth;
    private String idKey, listener = "no", replyFrom, replyText, networkListener = "yes", insideChat = "no";
    private int replyVisibility = 8;    // gone as dafault
    private long randomKey;     // use to fetch randomID when on edit mode
    private String audioPath;
    private MediaRecorder mediaRecorder;
    private Permission permissions;
    private static final int PAGE_SIZE = 20; // Number of items to fetch per page
    private int currentPage; // Current page number
    ConstraintLayout constraintMsgBody;

    Handler handler = new Handler(Looper.getMainLooper());

    RecordView recordView;
    RecordButton recordButton;

    private int readDatabase, downMsgCount;  // 0 is read, 1 is no_read
    public static Map<String, List<PinMessageModel>> pinPrivateMessageMap, PinPublicMessageMap;
    private Map<String, Object> editMessageMap;
    private Map<String, Object> deleteMap;
    private Map<String, List<MessageModel>> modelListMap;
    private Map<String, MessageAdapter> adapterMap;
    public static Map<String, RecyclerView> recyclerMap;
    private List<String> otherNameList, otherUidList;
    private Map<String, Object> downMsgCountMap, scrollNumMap;
    private Map<String, Object>  notifyCountMap, scrollPositionMap;

    ConstraintLayout recyclerContainer;

    NetworkChangeReceiver networkChangeReceiver;
    Handler handler1;
    Runnable internetCheckRunnable;

    public interface ChatVisibilityListener {
        void constraintChatVisibility(int position, int isVisible);
    }

    //  ---------- msg end


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //      --------- message ids starts        ------------------------

        conTopUserDetails = findViewById(R.id.contraintTop9);
        conUserClick = findViewById(R.id.constraintNextTop9);
        recyclerContainer = findViewById(R.id.constraintRecyler);
        imageViewCalls = findViewById(R.id.imageViewCalls9);
        constraintMsgBody = findViewById(R.id.constraintMsgBody);
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

        // delete ids
        constraintDelBody = findViewById(R.id.constDelBody);
        textViewDelMine = findViewById(R.id.textViewDelMine);
        textViewDelOther = findViewById(R.id.textViewDelOther);
        textViewDelAll = findViewById(R.id.textViewDelEveryone);
        imageViewCancelDel = findViewById(R.id.imageViewCancelDel);

        // pin message ids
        pinMsgContainer = findViewById(R.id.pinMsgConst);
        pinIconsContainer = findViewById(R.id.openPinMsg_Constr);
        pinMsgBox_Constr = findViewById(R.id.pinMsgBox);
        hidePinMsg_IV = findViewById(R.id.view_IV);
        pinPrivateIcon_IV = findViewById(R.id.pinsPrivate_IV);
        pinPublicIcon_IV = findViewById(R.id.pinsPublic_IV);
        pinCount_TV = findViewById(R.id.pinCount_TV);
        totalPinPrivate_TV = findViewById(R.id.totalPinMsgPrivate_TV);
        totalPinPublic_TV = findViewById(R.id.totalPinMsgPublic_TV);
        pinMsg_TV = findViewById(R.id.pinMsg_TV);
        arrowDown = findViewById(R.id.downArrow_IV);
        arrowUp = findViewById(R.id.upArrow_IV);

        pinOptionBox = findViewById(R.id.pinOptionBoxConstr);
        pinMineTV = findViewById(R.id.textViewPinMine);
        pinEveryoneTV = findViewById(R.id.textViewPinEveryone);
        cancelPinOption = findViewById(R.id.imageViewCancelPin);

        // reply and edit settings
        cardViewReplyOrEdit = findViewById(R.id.cardViewReply9);
        textViewReplyOrEdit = findViewById(R.id.textViewReplyText9);
        replyOrEditCancel_IV = findViewById(R.id.imageViewCancleReply9);
        editOrReplyIV = findViewById(R.id.editOrReplyImage9);
        nameReply = findViewById(R.id.fromTV9);
        replyVisible = findViewById(R.id.textReplying9);

        // scroll position and network ids
        constrNetork = findViewById(R.id.constrNetwork);
        constrNetConnect = findViewById(R.id.constrNetCheck);
        scrollPositionIV = findViewById(R.id.scrollToPositionIV);
        scrollCountTV = findViewById(R.id.scrollCountTV);
        receiveIndicator = findViewById(R.id.receiveIndicatorTV);
        sendIndicator = findViewById(R.id.sendIndicatorIV);

        // documents ids
        emoji_IV = findViewById(R.id.emoji_IV);
        file_IV = findViewById(R.id.files_IV);
        camera_IV = findViewById(R.id.camera_IV);


        // audio swipe button ids
        recordView = (RecordView) findViewById(R.id.record_view9);
        recordButton = (RecordButton) findViewById(R.id.record_button9);
        recordButton.setRecordView(recordView);

        // database references
        refMessages = FirebaseDatabase.getInstance().getReference("Messages");
        refMsgFast = FirebaseDatabase.getInstance().getReference("MsgFast");
        refChecks = FirebaseDatabase.getInstance().getReference("Checks");
        refUsers = FirebaseDatabase.getInstance().getReference("Users");
        refLastDetails = FirebaseDatabase.getInstance().getReference("UsersList");
        refEditMsg = FirebaseDatabase.getInstance().getReference("EditMessage");
        refDeleteMsg = FirebaseDatabase.getInstance().getReference("DeleteMessage");
        refPrivatePinChat = FirebaseDatabase.getInstance().getReference("PinMessages");
        refPublicPinChat = FirebaseDatabase.getInstance().getReference("PinChatPublic");

        user = FirebaseAuth.getInstance().getCurrentUser();

        pinPrivateMessageMap = new HashMap<>();
        deleteMap = new HashMap<>();
        editMessageMap = new HashMap<>();
        notifyCountMap = new HashMap<>();
        scrollNumMap = new HashMap<>();
        recyclerMap = new HashMap<>();
        modelListMap = new HashMap<>();
        adapterMap = new HashMap<>();
        scrollPositionMap = new HashMap<>();
        downMsgCountMap = new HashMap<>();
        otherNameList = new ArrayList<>();
        otherUidList = new ArrayList<>();
        readDatabase = 0;  // 0 is read, 1 is no_read
        // pins
//        pinTotalNum = 0;
        pinNextPrivate = 1;


        // -------------    msg id ends     ------------------------------

        // Home page (User ChatList) ids
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
        mainViewConstraint = findViewById(R.id.mainViewConstraint);
        darkMoodSwitch = findViewById(R.id.switch1);
        textLightAndDay = findViewById(R.id.textView13);
        topMainContainer = findViewById(R.id.constraintMsgContainer);
        cardViewSettings = findViewById(R.id.cardViewSettings);

        new CountDownTimer(25000, 1000){
            @Override
            public void onTick(long l) {
                readDatabase = 0;   // 0 is read, 1 is stop reading
            }

            @Override
            public void onFinish() {
                readDatabase = 1;
            }
        }.start();


        // Register the NetworkChangeReceiver to receive network connectivity changes
        networkChangeReceiver = new NetworkChangeReceiver(this);
        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        // manually call and check for the network
        handler1 = new Handler();   // used lamda for the runnable
        internetCheckRunnable = () -> networkChangeReceiver.onReceive(MainActivity.this,
                new Intent(ConnectivityManager.CONNECTIVITY_ACTION));

        handler1.postDelayed(internetCheckRunnable, 3000);       // Repeat the network check everything 3 sce till network is back
        handler1.post(internetCheckRunnable);


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
                                tab.setText("Players");
                                break;
                            case 2:
                                tab.setText("Team");
                                break;
                            case 3:
                                tab.setText("Tour.");
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

        darkMoodSwitch.setOnClickListener(view -> {
            if (nightMood) {
                sharedPreferences.edit().putBoolean("MoodStatus", false).apply();
            } else {
                sharedPreferences.edit().putBoolean("MoodStatus", true).apply();
            }

            //  initialize UI theme setup manually -- later
            recreate(); // call automatic UI initialization method
        });

        if(nightMood){
            // Turn on night mode
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            darkMoodSwitch.setChecked(false);
            textLightAndDay.setText("Light");
        } else {
            // Turn off night mode
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            darkMoodSwitch.setChecked(true);
            textLightAndDay.setText("Dark");
        };


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
        mainViewConstraint.setOnClickListener(view -> {

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
        logout.setOnClickListener(view -> logoutOption());

        // settings
        cardViewSettings.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));

        // open user menu
        imageViewOpenMenu.setOnClickListener(view -> constraintProfileMenu.setVisibility(View.VISIBLE));

        // close user menu
        imageViewCloseMenu.setOnClickListener(view -> constraintProfileMenu.setVisibility(View.GONE));
        constraintProfileMenu.setOnClickListener(view -> constraintProfileMenu.setVisibility(View.GONE));


        // send message
        circleSendMessage.setOnClickListener(view -> {

            String message = editTextMessage.getText().toString().trim();
            if (!message.equals("")){

                sendMessage(message, 0);    // 0 is for text while 1 is for voice note

                editTextMessage.setText("");
                textViewReplyOrEdit.setText("");
                listener = "no";
                replyText = null;
                replyFrom = null;
                replyVisibility = 8;
                idKey = null;

                cardViewReplyOrEdit.setVisibility(View.GONE);
                nameReply.setVisibility(View.GONE);
                replyVisible.setVisibility(View.GONE);

            }
        });

        // set camera
        camera_IV.setOnClickListener(view -> {
            Toast.makeText(this, "work in progess", Toast.LENGTH_SHORT).show();
        });

        // set files(documents)
        file_IV.setOnClickListener(view -> {
            Toast.makeText(this, "work in progess", Toast.LENGTH_SHORT).show();
        });

        // set emoji
        emoji_IV.setOnClickListener(view -> {
            Toast.makeText(this, "work in progess", Toast.LENGTH_SHORT).show();
        });

        // close and cancel reply and edit box
        replyOrEditCancel_IV.setOnClickListener(view -> {

            nameReply.setVisibility(View.GONE);
            replyVisible.setVisibility(View.GONE);
            cardViewReplyOrEdit.setVisibility((int) 8);   // 8 is for GONE

            listener = "no";
            idKey = null;
            editTextMessage.setText("");
            replyText = null;
            replyFrom = null;
            idKey = null;
            replyVisibility = 8;
            textViewReplyOrEdit.setText("");
        });


        // Close delete message option
        constraintDelBody.setOnClickListener(view -> {
            constraintDelBody.setVisibility(View.GONE);
            idKey = null;
        });
        imageViewCancelDel.setOnClickListener(view -> {
            constraintDelBody.setVisibility(View.GONE);
            idKey = null;
        });

        // Delete for only me
        textViewDelMine.setOnClickListener(view -> {
            // delete from my local list
            MessageAdapter adapter = adapterMap.get(otherUserName);
            adapter.deleteMessage(idKey);
            try{
                refMessages.child(myUserName).child(otherUserName).child(idKey).getRef().removeValue();
                refMsgFast.child(myUserName).child(otherUserName).child(idKey).getRef().removeValue();
                refEditMsg.child(myUserName).child(otherUserName).child(idKey).getRef().removeValue();
            } catch (Exception e){
                System.out.println("message key not found to delete (M460) " + e.getMessage());
            }

            constraintDelBody.setVisibility(View.GONE);
            Toast.makeText(MainActivity.this, "Message deleted for only you.", Toast.LENGTH_SHORT).show();
            idKey = null;

        });

        // Delete for others only
        textViewDelOther.setOnClickListener(view -> {

            // save to delete database to loop through the other user local list and delete if idkey is found
            deleteMap.put("idKey", idKey);
            deleteMap.put("randomID", randomKey);
            refDeleteMsg.child(otherUserName).child(myUserName).child(idKey).setValue(deleteMap);

            try{
                refMessages.child(otherUserName).child(myUserName).child(idKey).getRef().removeValue();
                refMsgFast.child(otherUserName).child(myUserName).child(idKey).getRef().removeValue();
                refEditMsg.child(otherUserName).child(myUserName).child(idKey).getRef().removeValue();
            } catch (Exception e){
                System.out.println("message key not found to delete for other (M474) " + e.getMessage());
            }

            constraintDelBody.setVisibility(View.GONE);
            Toast.makeText(MainActivity.this, "Message deleted for "+otherUserName+".", Toast.LENGTH_SHORT).show();

        });

        // Delete for everyone
        textViewDelAll.setOnClickListener(view -> {

            // delete from my local list
            MessageAdapter adapter = adapterMap.get(otherUserName);
            adapter.deleteMessage(idKey);

            // save to delete database to loop through the other user local list and delete if idkey is found
            deleteMap.put("idKey", idKey);
            deleteMap.put("randomID", randomKey);
            refDeleteMsg.child(otherUserName).child(myUserName).child(idKey).setValue(deleteMap);

            try{    // delete from all database
                refMessages.child(myUserName).child(otherUserName).child(idKey).getRef().removeValue();
                refMessages.child(otherUserName).child(myUserName).child(idKey).getRef().removeValue();

                refEditMsg.child(myUserName).child(otherUserName).child(idKey).getRef().removeValue();
                refEditMsg.child(otherUserName).child(myUserName).child(idKey).getRef().removeValue();

                refMsgFast.child(myUserName).child(otherUserName).child(idKey).getRef().removeValue();
                refMsgFast.child(otherUserName).child(myUserName).child(idKey).getRef().removeValue();

            } catch (Exception e){
                System.out.println("message key not found to deleteAll (M490) " + e.getMessage());
            }

            constraintDelBody.setVisibility(View.GONE);
            Toast.makeText(MainActivity.this, "Message deleted for everyone.", Toast.LENGTH_SHORT).show();

        });

        // scroll to previous position of reply message
        scrollPositionIV.setOnClickListener(view -> {

            if(goToLastMessage) {
                recyclerMap.get(otherUserName).scrollToPosition(goToNum - 2);
                adapterMap.get(otherUserName).highlightItem(goToNum); // notify Colour
                adapterMap.get(otherUserName).highlightedPositions.add(goToNum);    // change color

                goToLastMessage = false;
            } else {
                recyclerMap.get(otherUserName).scrollToPosition(adapterMap.get(otherUserName).getItemCount()-1);
                // clear highlight background if any
                adapterMap.get(otherUserName).highlightedPositions.clear();
                // Clear previous highlight, if any.
                for (int i = 0; i < recyclerMap.get(otherUserName).getChildCount(); i++) {
                    View itemView = recyclerMap.get(otherUserName).getChildAt(i);
                    itemView.setBackgroundColor(Color.TRANSPARENT);
                }
            }

        });

        // hide pin message
        hidePinMsg_IV.setOnClickListener(view -> {  // personalise later
            pinIconsContainer.setVisibility(View.VISIBLE);
            pinMsgBox_Constr.setVisibility(View.INVISIBLE);
        });

        // open private pin message box
        View.OnClickListener openPrivatePinMsg = view -> { // personalise later
            pinIconsContainer.setVisibility(View.GONE);
            pinMsgBox_Constr.setVisibility(View.VISIBLE);

            // show current pin message and total pin number
            int pinNum = pinPrivateMessageMap.get(otherUserName).size();
            pinCount_TV.setText("(" + pinNextPrivate + "/" + (pinNum) + ")");
            int currentPinNumber = pinNum - pinNextPrivate;
            pinMsg_TV.setText(pinPrivateMessageMap.get(otherUserName).get(currentPinNumber).getMessage());
        };
        pinPrivateIcon_IV.setOnClickListener(openPrivatePinMsg);
        totalPinPrivate_TV.setOnClickListener(openPrivatePinMsg);

        // open public pins
        View.OnClickListener openPublicPinMsg = view ->{
            pinIconsContainer.setVisibility(View.GONE);
            pinMsgBox_Constr.setVisibility(View.VISIBLE);

            // show current pin message and total pin number
//            int pinNum = pinPrivateMessageMap.get(otherUserName).size();
            pinCount_TV.setText("(" + pinNextPrivate + "/" + (50) + ")");
//            int currentPinNumber = pinNum - pinNextNumber;
            pinMsg_TV.setText("Work in progress");
        };
        pinPublicIcon_IV.setOnClickListener(openPublicPinMsg);
        totalPinPublic_TV.setOnClickListener(openPublicPinMsg);

        pinMsgContainer.setOnClickListener(view -> {
            pinIconsContainer.setVisibility(View.GONE);
            pinMsgBox_Constr.setVisibility(View.VISIBLE);
        });

        //  close pin option box
        View.OnClickListener closePinOption = view -> {
            pinOptionBox.setVisibility(View.GONE);
        };
        cancelPinOption.setOnClickListener(closePinOption);
        pinOptionBox.setOnClickListener(closePinOption);

        // pin message for only me -- private
        pinMineTV.setOnClickListener(view -> pinAndUnpinMsgPrivate());
        
        // pin message for everyone
        pinEveryoneTV.setOnClickListener(view -> {
            Toast.makeText(this, "Work in progress", Toast.LENGTH_SHORT).show();
        });

        // arrow up, scroll to upper previous pins
        arrowUp.setOnClickListener(view -> {    // go to next upper pin message
            pinNextPrivate += 1;
            int pinNum = pinPrivateMessageMap.get(otherUserName).size();
            int reduceNumber = pinNum - pinNextPrivate;

            if(reduceNumber >= 0){  // only scroll when pin is between 0 to pinNum

                pinCount_TV.setText("(" + pinNextPrivate + "/" + (pinNum) + ")");
                pinMsg_TV.setText(pinPrivateMessageMap.get(otherUserName).get(reduceNumber).getMessage());

                scrollToPinMessage(reduceNumber); // call method to scroll to message

            } else{
                pinNextPrivate -= 1;
                Toast.makeText(this, "No more pin message!", Toast.LENGTH_SHORT).show();
            }

        });

        // arrow down, scroll to recent pin messages
        arrowDown.setOnClickListener(view -> {    // go to next down pin message
            pinNextPrivate -= 1;
            int pinNum = pinPrivateMessageMap.get(otherUserName).size();
            int increaseNumber = pinNum - pinNextPrivate;

            if (increaseNumber < pinNum){

                pinCount_TV.setText("(" + pinNextPrivate + "/" + (pinNum) + ")");
                pinMsg_TV.setText(pinPrivateMessageMap.get(otherUserName).get(increaseNumber).getMessage());

                scrollToPinMessage(increaseNumber); // call method to scroll to message

            } else {
                pinNextPrivate += 1; // to enable it stop decreasing
                Toast.makeText(this, "No more pin message!", Toast.LENGTH_SHORT).show();
            }

        });

        // Delay 5 seconds to load message
        new CountDownTimer(5000, 1000){
            @Override
            public void onTick(long l) {
                constrNetork.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinish() {
                constrNetork.setVisibility(View.GONE);
            }
        }.start();

        setUserDetails();

    }

    //  --------------- methods && interface --------------------


    //  ----------   interface    ---------------------
    @Override
    public void callAllMethods(String otherName, String userName, String uID) {
        getMyUserTyping();
        tellUserAmTyping_AddUser();
        getPreviousCounts();
        convertUnreadToReadMessage(otherName, userName, uID);

        // get the total pin messages number
        if(pinPrivateMessageMap.get(otherName) != null){
            int totalPins = pinPrivateMessageMap.get(otherName).size();
            if(totalPins > 0){
                pinMsgContainer.setVisibility(View.VISIBLE);
                totalPinPrivate_TV.setText(""+ totalPins);
            }

        } else {
            System.out.println("Nothing in the PIN map");
            pinMsgContainer.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void firstCallLoadPage(String otherName) {
        constraintMsgBody.setVisibility(View.INVISIBLE);
        conTopUserDetails.setVisibility(View.INVISIBLE);
        conUserClick.setVisibility(View.INVISIBLE);
    }

    @Override
    public void msgBodyVisibility(String otherName, String imageUrl, String userName, String uID) {

        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerMap.get(otherName).getLayoutManager();

        // get the number of message under
        downMsgCount = (layoutManager.getItemCount() - 1) - layoutManager.findLastVisibleItemPosition();
        scrollCountTV.setText(""+ downMsgCount);           // set down msg count
        downMsgCountMap.put(otherName, downMsgCount);     // set it for "sending message method"

        // Get the position of the item for sendMessage() and retrieveMessage()
        scrollNum = layoutManager.findLastVisibleItemPosition() - 10;
        scrollNumMap.put(otherName, scrollNum);

        //  check count and display/hide the scroll arrow
        if(downMsgCount > 2){
            scrollCountTV.setVisibility(View.VISIBLE);
            scrollPositionIV.setVisibility(View.VISIBLE);
        } else {
            scrollCountTV.setVisibility(View.GONE);
            scrollPositionIV.setVisibility(View.GONE);
            receiveIndicator.setVisibility(View.GONE);
            sendIndicator.setVisibility(View.GONE);
        }

        // Add an OnScrollListener to the RecyclerView
        recyclerMap.get(otherName).addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // keep saving the recycler position while scrolling
                scrollNum = layoutManager.findLastVisibleItemPosition() - 10;
                scrollNumMap.put(otherName, scrollNum);

                // keep updating the number of down messages
                downMsgCount = (layoutManager.getItemCount() - 1) - layoutManager.findLastVisibleItemPosition();
                scrollCountTV.setText(""+downMsgCount);

                //  store the downMsgCount in a map for each user, to enable me
                //  add to the number on sendMessage() when I send new message
                downMsgCountMap.put(otherName, downMsgCount);

                //  check count and display/hide the scroll arrow
                if(downMsgCount > 2){
                    scrollCountTV.setVisibility(View.VISIBLE);
                    scrollPositionIV.setVisibility(View.VISIBLE);
                } else {
                    scrollCountTV.setVisibility(View.GONE);
                    scrollPositionIV.setVisibility(View.GONE);
                    receiveIndicator.setVisibility(View.GONE);
                    sendIndicator.setVisibility(View.GONE);
                }

            }
        });


        // make chat box visible
        constraintMsgBody.setVisibility(View.VISIBLE);
        conTopUserDetails.setVisibility(View.VISIBLE);
        conUserClick.setVisibility(View.VISIBLE);

        textViewOtherUser.setText(otherName);   // display their userName on top of their page

        //  get user image
        if (imageUrl.equals("null")) {
            circleImageLogo.setImageResource(R.drawable.person_round);
        }
        else Picasso.get().load(imageUrl).into(circleImageLogo);

        otherUserName = otherName;
        myUserName = userName;
        imageUri = imageUrl;

        try{    // make only the active recyclerView to be visible
            recyclerViewChatVisibility(otherName);
        } catch (Exception e){
            Toast.makeText(MainActivity.this, "Send your first message here! WC", Toast.LENGTH_SHORT).show();
            System.out.println("Error occur " + e.getMessage());
        }

        topMainContainer.setVisibility(View.INVISIBLE);

        System.out.println("Total adapter (M750) " + adapterMap.get(otherName).getItemCount());
    }

    @Override       // run only once
    public void sendRecyclerView(RecyclerView recyclerChat, String otherName, String otherUid) {

        // first check if recyclerView already exist and skip to the next
        if (!recyclerMap.containsKey(otherName)) {
            recyclerMap.put(otherName, recyclerChat);  // save empty recyclerView of each user to their username

            if (recyclerChat.getParent() != null) {
                // Remove the clicked RecyclerView from its current parent
                ((ViewGroup) recyclerChat.getParent()).removeView(recyclerChat);
            }
            recyclerContainer.addView(recyclerChat);

            recyclerViewChatVisibility(otherName);  // set to INVISIBLE since it's "GONE" on XML
        }

        // store otherName for looping through load message status and change to delivery status
        if (!otherNameList.contains(otherName)) {
            otherNameList.add(otherName);
        }

        // store otherUserUid to loop and change to delivery status -- of outsider chat
        if (!otherUidList.contains(otherUid)) {
            otherUidList.add(otherUid);
        }
    }

    @Override       // run only once
    public void getMessage(String userName, String otherName, String otherUID, Context mContext){

        retrieveMessages(userName, otherName, otherUID, mContext);

        // store myUserName for looping through load message status and change to delivery status
        myUserName = userName;

        // get pinMessage once
        getPinMessages(user.getUid(), otherUID, otherName);
    }

    @Override
    public void onDeleteMessage(String id, String fromWho, long randomID) {

        idKey = id;
        randomKey = randomID;
        constraintDelBody.setVisibility(View.VISIBLE);

        // user1 should be unable to delete user2 msg
        if(!fromWho.equals(myUserName)){
            textViewDelOther.setVisibility(View.GONE);
        } else {
            textViewDelOther.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onEditOrReplyMessage(String message, String editOrReply, String id, long randomID,
                                     String status, int icon, String fromWho, int visible)
    {
        // pop up keyboard
        editTextMessage.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(editTextMessage, InputMethodManager.SHOW_IMPLICIT);

        // General settings
        editOrReplyIV.setImageResource(icon);               //  show edit or reply icon at left view
        textViewReplyOrEdit.setText(message);               // set the text box with the message
        cardViewReplyOrEdit.setVisibility(View.VISIBLE);    // make the container of the text visible
        replyVisible.setVisibility(View.VISIBLE);
        replyVisible.setText(status);                       // indicating its on edit or reply mood
        nameReply.setVisibility(View.VISIBLE);
        listener = editOrReply;

        // this id will enable user to click a reply msg and scroll there
        // Edit -- it will replace the message with the id
        idKey = id;


        // edit settings
        if(editOrReply == "edit"){
            randomKey = randomID;
            nameReply.setText("");
            editTextMessage.setText(message);  // set the edit message on the text field
            editTextMessage.setSelection(editTextMessage.getText().length()); // Set focus to the end of the text
        }

        // reply setting
        if(editOrReply == "reply"){

            replyVisibility = visible;      // send visible to database to make the replied msg Visible on the UI
            replyText = message;

            if (fromWho.equals(myUserName)) {   // change fromWho from display name to username later
                replyFrom = "From You." + " (@" +fromWho+")";
                nameReply.setText(replyFrom);
            }
            else {
                // edit later to username and display name
                replyFrom = fromWho + " (@" +fromWho+")";
                nameReply.setText(replyFrom);
            }
        }

    }

    @Override
    public void sendPinData(String msgId_, String message_, Object timeStamp_) {
        msgId = msgId_;
        message = message_;
        timeStamp = timeStamp_;
    }

    @Override
    public void msgBackgroundActivities(String otherUid) {

        new Thread(() -> {

            // set my status to be true in case I receive msg, it will be tick as seen
            Map <String, Object> statusAndMSgCount = new HashMap<>();
            statusAndMSgCount.put("status", true);
            statusAndMSgCount.put("unreadMsg", 0);

            refChecks.child(user.getUid()).child(otherUserUid).updateChildren(statusAndMSgCount);

            // set responds to pend always      ------- will change later to check condition if user is still an active call
            refChecks.child(user.getUid()).child(otherUid).child("vCallResp").setValue("pending");

            insideChat = "yes";
        }).start();
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
                                        textViewLastSeen.setText("Seen: Today, " + time.toLowerCase()+".");
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

    // This method will be called by NetworkChangeReceiver whenever network status changes   // reload message loadStatus
    @Override
    public void onNetworkStatusChanged(boolean isConnected) {
        if (isConnected) {
            // Network is connected, perform actions accordingly
            constrNetConnect.setVisibility(View.INVISIBLE);
            networkListener = "yes";

            // remove runnable when network is okay to prevent continuous data usage
            handler1.removeCallbacks(internetCheckRunnable);

            reloadFailedMessagesWhenNetworkIsOk();  // reload message loadStatus

        } else {
            // Network is disconnected, handle this case as well
            constrNetConnect.setVisibility(View.VISIBLE);
            networkListener = "no";

            handler1.post(internetCheckRunnable);   // call runnable when no internet

        }
    }


    //  ------------    methods     ---------------
    private Map<String, Object> setMessage(String message, int type, long randomID){
        // 700024 --- tick one msg  // 700016 -- seen msg   // 700033 -- load

        int msgStatus = 700024;
//        int msgStatus = 700033;

        if(networkListener == "no"){
            msgStatus = 700033;
        }

        Map<String, Object> messageMap = new HashMap<>();

        messageMap.put("from", myUserName);
        messageMap.put("type", type);
        messageMap.put("randomID", randomID);
        messageMap.put("message", message);
//            messageMap.put("voicenote", vn);
        messageMap.put("msgStatus", msgStatus);
        messageMap.put( "timeSent", ServerValue.TIMESTAMP);
        messageMap.put("replyFrom", replyFrom);
        messageMap.put("visibility", replyVisibility);
        messageMap.put("replyID", idKey);
        messageMap.put("replyMsg", replyText);

        return messageMap;
    }

    public void sendMessage(String message, int type) {

        if(listener == "edit"){ // check if it's on edit mode

            editMessageMap.put("from", myUserName);
            editMessageMap.put("message", message);
            editMessageMap.put("edit", "edited");
            editMessageMap.put("randomID", randomKey);
            editMessageMap.put( "timeSent", ServerValue.TIMESTAMP);

            // save to edit message
            refEditMsg.child(myUserName).child(otherUserName).child(idKey).setValue(editMessageMap);
            refEditMsg.child(otherUserName).child(myUserName).child(idKey).setValue(editMessageMap);

        } else {
            long randomID = (long)(Math.random() * 1_010_001);

            // save to local list for fast update
            MessageModel messageModel = new MessageModel(message, myUserName, replyFrom, 0, "",
                    "", replyVisibility, replyText, 700033, type, randomID, idKey);

            MessageAdapter adapter = adapterMap.get(otherUserName);
            adapter.addNewMessageDB(messageModel);

            // add one to the dowm message number
            int increaseScroll = (int) downMsgCountMap.get(otherUserName) + 1;
            scrollCountTV.setText(""+increaseScroll);
            downMsgCountMap.put(otherUserName, increaseScroll); // save new position

            // show indicator that msg is sent
            if(scrollPositionIV.getVisibility() == View.VISIBLE){
                sendIndicator.setVisibility(View.VISIBLE);
                receiveIndicator.setVisibility(View.GONE);
            }

            // scroll to new position
            int scrollCheck = adapter.getItemCount() - (int) scrollNumMap.get(otherUserName);
            if(scrollCheck < 20){    // scroll to last position on new message update.
//                scrollToPreviousPosition(otherUserName, (adapter.getItemCount() - 1));
                recyclerMap.get(otherUserName).scrollToPosition(adapterMap.get(otherUserName).getItemCount()-1);
            }   // else don't scroll.

//            new Thread(() -> {

                String key = refMsgFast.child(myUserName).child(otherUserName).push().getKey();  // create an id for each message

                // save to new message db for fast response
                refMsgFast.child(myUserName).child(otherUserName).child(key).setValue(setMessage(message, type, randomID));
                refMsgFast.child(otherUserName).child(myUserName).child(key).setValue(setMessage(message, type, randomID));

                // save to main message
                refMessages.child(myUserName).child(otherUserName).child(key).setValue(setMessage(message, type, randomID));
                refMessages.child(otherUserName).child(myUserName).child(key).setValue(setMessage(message, type, randomID));

                // save last msg for outside chat display
                refLastDetails.child(user.getUid()).child(otherUserUid).setValue(setMessage(message, type, randomID));
                refLastDetails.child(otherUserUid).child(user.getUid()).setValue(setMessage(message, type, randomID));

                checkAndSaveCounts_SendMsg();   // save the number of new message I am sending
//            });


        }

    }

    //  get the previous count of new msg and add to it from sendMessage
    private void checkAndSaveCounts_SendMsg(){

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                // set my count to 0
                refChecks.child(user.getUid()).child(otherUserUid).child("unreadMsg").setValue(0);

                //   check if the user is in my chat box and reset the count -- newMsgCount & unreadMsg
                refChecks.child(otherUserUid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        try{
                            boolean statusState = (boolean) snapshot.child(user.getUid())
                                    .child("status").getValue();

                            if(statusState == true) {

                                refChecks.child(otherUserUid).child(user.getUid()).child("unreadMsg").setValue(0);

                            } else {
                                // increase the new msg count
                                refChecks.child(otherUserUid).child(user.getUid())
                                        .child("unreadMsg").setValue(count+=1);   // adding
                                refChecks.child(otherUserUid).child(user.getUid())
                                        .child("newMsgCount").setValue(newMsgCount+1);
                            }
                        } catch (Exception e){
                            refChecks.child(otherUserUid).child(user.getUid()).child("status").setValue(false);
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

        List<MessageModel> modelListAllMsg = new ArrayList<>();     // save all messages (read and unread)
        List<MessageModel> msgListNotRead = new ArrayList<>();      // save all unread messages from refFastMsg db to get total Count

        MessageAdapter adapter = new MessageAdapter(modelListAllMsg, userName, uID, mContext); // initialise adapter

        // loop through New Message (MsgFast) and Old Message and compare the "read" status state before proceeding
        new CountDownTimer(1500, 750){
            @Override
            public void onTick(long l) {

                // retrieve the last previous scroll position
                getLastScrollPosition(uID, otherName);
                // delete from the database when message is read and get the total number of msg not read yet
                deleteMessageWhenRead(userName, otherName, msgListNotRead);

            }


            @Override
            public void onFinish() { // call all methods

                // retrieve all message from the database just once
                getAllMessages(userName, otherName, modelListAllMsg, msgListNotRead, adapter);

                // add new message directly to local List and interact with few msg in refMsgFast database
                newMessageInteraction(userName, otherName, modelListAllMsg, adapter);

                // edit message
                getEditMessage(userName, otherName, modelListAllMsg, adapter, uID);

                // delete local list with idkey
                getDeleteMsgId(userName, otherName, modelListAllMsg, adapter, uID);

//                System.out.println("What is recyler side (M1210)" + recyclerMap.get(otherName).getAdapter().getItemCount());
            }
        }.start();

        adapter.setFragmentListener((FragmentListener) mContext);

        adapterMap.put(otherName, adapter); // save each user adapter
        recyclerMap.get(otherName).setAdapter(adapter);

    }

    // retrieve the last previous scroll position
    private void getLastScrollPosition(String uID, String otherName)
    {
        refChecks.child(user.getUid()).child(uID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                try{
                    // get the values(e.g frank4032) and split it -- frank , 4032
                    String details = snapshot.child("scrollPosition").getValue().toString();

                    // Use regular expression to split letters and numbers
                    Pattern pattern = Pattern.compile("([A-Za-z_]+)([0-9]+)");
                    Matcher matcher = pattern.matcher(details);

                    String otherName2 = "";
                    String position = "";

                    if (matcher.find()) {
                        otherName2 = matcher.group(1);  // Group 1 contains letters
                        position = matcher.group(2);    // Group 2 contains numbers

                        int convertPosition = Integer.parseInt(position);

                        if (readDatabase == 0) {  // fetch data just once
                            scrollPositionMap.put(otherName2, convertPosition);
                            scrollNumMap.put(otherName2, convertPosition);
                        }
                    }

                } catch (Exception e){
                    refChecks.child(user.getUid()).child(uID).child("scrollPosition").setValue(otherName+5);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    // delete from the database when message is read and get the total number of msg not read yet
    private void deleteMessageWhenRead(String userName, String otherName, List<MessageModel>msgListNotRead)
    {
        refMsgFast.child(userName).child(otherName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                new Thread(() -> {
                    msgListNotRead.clear();

                    for (DataSnapshot snapshotNew : snapshot.getChildren()){

                        if(insideChat == "no"){
                            // check if the message has been read
                            if((long)snapshotNew.child("msgStatus").getValue() == 700016){

//                              //  delete from the new database if the msg has been read
                                String key = snapshotNew.getKey();
                                refMsgFast.child(userName).child(otherName).child(key).removeValue();
//
                            } else {
                                // if not read yet, add to msgListNotRead to get the total of unread msg
                                if(readDatabase == 0){    // only read once to get the total number of message
                                    MessageModel messageModel = snapshotNew.getValue(MessageModel.class);
                                    msgListNotRead.add(messageModel);
                                }
                            }
                        }
                    }
                }).start();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    // retrieve all message from the database just once
    private void getAllMessages(String userName, String otherName, List<MessageModel> allMsgList,
                                List<MessageModel> msgListNotRead, MessageAdapter adapter)
    {
        refMessages.child(userName).child(otherName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(readDatabase == 0) {   // run only once

                    allMsgList.clear();

                    for (DataSnapshot snapshotOld : snapshot.getChildren()){

                        if(snapshotOld.child("from").exists()){
                            MessageModel messageModelOldMsg = snapshotOld.getValue(MessageModel.class);
                            messageModelOldMsg.setIdKey(snapshotOld.getKey());  // set msg keys to the adaptor
                            allMsgList.add(messageModelOldMsg);

                            // set old message to read status
                            if(msgListNotRead.size() < 1){
                                messageModelOldMsg.setMsgStatus(700016);    // change later to loop through only 2000 msg
                            } else {
                                // loop through only 2000 message for efficiency, and set readStatus
                                int changeOnly = allMsgList.size()-msgListNotRead.size();
                                int startCount = allMsgList.size() > 2000 ? allMsgList.size() - 2000: 0;

                                for (int i = startCount; i < changeOnly; i++) {
                                    MessageModel msgStatus = allMsgList.get(i);
                                    msgStatus.setMsgStatus(700016);
                                }
                            }

                        } else {
                            refMessages.child(userName).child(otherName).child(snapshotOld.getKey()).removeValue();
                        }
                    }

                    // scroll to previous position UI of user
                    try{
                        scrollToPreviousPosition(otherName, (int) scrollPositionMap.get(otherName));
                    } catch (Exception e){
                        scrollToPreviousPosition(otherName, adapter.getItemCount() - 1);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    // add new message to local List and interact with few msg in refMsgFast database for delivery and read status
    private void newMessageInteraction(String userName, String otherName, List<MessageModel> allMsgList, MessageAdapter adapter)
    {
        refMsgFast.child(userName).child(otherName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot snapshot1 : snapshot.getChildren()) {

                    MessageModel messageModel = snapshot1.getValue(MessageModel.class);
                    messageModel.setIdKey(snapshot1.getKey());

                    // get the total number of message in the msgFast Db for looping
                    int startCount = allMsgList.size() > 50 ? allMsgList.size() - 50: 0;  // set default to 0

                    boolean isNewMessage = true;
                    boolean isSentOrRead = true;

                    // Check if the message already exists in the current messages list
                    // loop in descending order and break, to make it fast (E.g 200 - 150)
                    for (int i = allMsgList.size()-1; i >= startCount; i--) {
                        MessageModel existingMessage = allMsgList.get(i);
                        if (messageModel.getIdKey().equals(existingMessage.getIdKey())) {
                            isNewMessage = false;
                            break;
                        }

                        if (messageModel.getIdKey().equals(existingMessage.getIdKey()) &&
                                messageModel.getMsgStatus() == existingMessage.getMsgStatus()) {
                            isSentOrRead = false;
                            break;
                        }
                    }

                    // If there's new message from otherUser, add here
                    if (isNewMessage && messageModel.getFrom().equals(otherName)) {

                        // add the new msg to the modelList method at MessageAdapter (L152)
                        adapter.addNewMessageDB(messageModel);

                        // check recycler position before scrolling
                        int scrollNumCheck = scrollNumMap.get(otherName) == null ? adapter.getItemCount() - 1: (int) scrollNumMap.get(otherName) ;
                        int scrollCheck = adapter.getItemCount() - scrollNumCheck;

                        if(scrollCheck > 20){    // don't scroll. Just update.
                            adapter.notifyItemChanged((adapter.getItemCount() - 1), new Object());
                        } else {
                            // scroll to last position on new message update
                            scrollToPreviousPosition(otherName, (adapter.getItemCount() - 1));
                        }

                        // show new msg alert text for user
                        if(scrollPositionIV.getVisibility() == View.VISIBLE){
                            receiveIndicator.setVisibility(View.VISIBLE);
                            sendIndicator.setVisibility(View.GONE);
                        }
                    }

                    //     change the load status to unread message status or read status
                    if(isSentOrRead){
                        try{
                            if(messageModel.getFrom().equals(userName)){

                                // Check if the message is "700033" ~ unread
                                for (int i = allMsgList.size()-1; i >= startCount; i--) {
                                    MessageModel msgStatus = allMsgList.get(i);

                                    // check if both message and randomID are same
                                    if (messageModel.getMessage().equals(msgStatus.getMessage())
                                            && messageModel.getRandomID() == msgStatus.getRandomID()) {

                                        // set details for unread message found to read status
                                        allMsgList.get(i).setMsgStatus(messageModel.getMsgStatus());
                                        allMsgList.get(i).setTimeSent(messageModel.getTimeSent());
                                        allMsgList.get(i).setIdKey(messageModel.getIdKey());
                                        allMsgList.get(i).setEdit(messageModel.getEdit());

                                        allMsgList.get(i).setReplyFrom(messageModel.getReplyFrom());
                                        allMsgList.get(i).setReplyMsg(messageModel.getReplyMsg());
                                        allMsgList.get(i).setReplyID(messageModel.getReplyID());

                                        // notify with empty object so it doesn't duplicate
                                        adapter.notifyItemChanged(i, new Object());
                                    }

                                    break;
                                }
                            }

                        } catch (Exception e){
                            System.out.println("Error in converting message (M879) " + e.getMessage());
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    // retrieve message when edited and delete after 3sec (after updating the Main Message DB)
    private void getEditMessage(String userName, String otherName, List<MessageModel> allMsgList, MessageAdapter adapter, String otherUid)
    {
        refEditMsg.child(userName).child(otherName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // loop through each child
                for (DataSnapshot snapshotEdit : snapshot.getChildren()) {

                    if(snapshotEdit.child("from").exists()){
                        // get the messages from the database and set the idKey
                        EditMessageModel editMessageModel = snapshotEdit.getValue(EditMessageModel.class);
                        editMessageModel.setId(snapshotEdit.getKey());

                        int startCount = allMsgList.size() > 50 ? allMsgList.size() - 50: 0;  // set default to 50

                        boolean isEditMessage = false;
                        int position = 0;

                        // Check if the message already exists in the current local messages list
                        for (int i = allMsgList.size()-1; i >= startCount; i--) {
                            MessageModel existingMessage = allMsgList.get(i);
                            if (editMessageModel.getId().equals(existingMessage.getIdKey())) {
                                isEditMessage = true;
                                position = i;
                                break;
                            }
                        }

                        if(isEditMessage){  // replace the message details if found
                            allMsgList.get(position).setMessage(editMessageModel.getMessage());
                            allMsgList.get(position).setTimeSent(editMessageModel.getTimeSent());
                            allMsgList.get(position).setEdit(editMessageModel.getEdit());

                            // notify with empty object so it doesn't duplicate
                            adapter.notifyItemChanged(position, new Object());
                        }

                        // update Main Message DB and delete from EditMessage DB after 3 secs
                        new CountDownTimer(3_000, 1_000){
                            @Override
                            public void onTick(long l) {
                            }

                            @Override
                            public void onFinish() {
                                //  check only last 100, if message exist in the main Message database
                                Query query = refMessages.child(userName).child(otherName).limitToLast(100);
                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        for(DataSnapshot snapshotAll : snapshot.getChildren()){
                                            // compare both keys (EditMsg DB and Main Message DB)
                                            if(snapshotEdit.getKey().equals(snapshotAll.getKey())){
                                                // fetch out the data via map
                                                Map<String, Object> updateMap = new HashMap<>();
                                                updateMap.put("message", editMessageModel.getMessage());
                                                updateMap.put("edit", "edited");
                                                updateMap.put( "timeSent", editMessageModel.getTimeSent());

                                                // update the Main Message DB with the map data
                                                refMessages.child(userName).child(otherName)
                                                        .child(snapshotAll.getKey())
                                                        .updateChildren(updateMap).addOnCompleteListener(task -> {
                                                            if(task.isSuccessful()){
                                                                // delete is updated successfully
                                                                refEditMsg.child(userName).child(otherName)
                                                                        .child(snapshotEdit.getKey())
                                                                        .removeValue();
                                                            }
                                                        });

                                            } else {    // delete msg from EditMsg DB if not found in Main Message DB
                                                refEditMsg.child(userName).child(otherName)
                                                        .child(snapshotEdit.getKey()).removeValue();
                                            }
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }
                        }.start();


                        // check outside message if it's same id message that was edited and update it
                        refLastDetails.child(user.getUid()).child(otherUid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshotLast) {
                                //  check if the key id is same
                                long editID = editMessageModel.getRandomID();
                                long outSideChatID = (long) snapshotLast.child("randomID").getValue();

                                if(editID == outSideChatID){
                                    //  update and replace the message
                                    refLastDetails.child(user.getUid()).child(otherUid)
                                            .child("message").setValue(editMessageModel.getMessage());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //  retrieve the delete message id and compare it my local list id and delete if found
    private void getDeleteMsgId(String userName, String otherName, List<MessageModel> allMsgList, MessageAdapter adapter, String otherUid)
    {
        refDeleteMsg.child(userName).child(otherName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot snapshotDelete: snapshot.getChildren()) {
                    // get the randomID for outside chat, and keyID for inside chat
                    long deleteRandomID = (long) snapshotDelete.child("randomID").getValue();
                    String deleteIdKey = snapshotDelete.getKey();
                    // loop through the local list and search for same idkey
                    for (int i = allMsgList.size() - 1; i >= 0; i--) {
                        String listMessageID = allMsgList.get(i).getIdKey();
                        if(deleteIdKey.equals(listMessageID)){
                            // delete from list if id key matches
                            allMsgList.remove(i);
                            adapter.notifyDataSetChanged();
                            // delete idkey from database if id key matches
                            refDeleteMsg.child(userName).child(otherName).child(deleteIdKey).removeValue();
                            break;
                        }
                    }

                    // check outside message if it's same message that was deleted and delete for both user
                    refLastDetails.child(user.getUid()).child(otherUid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshotLast) {
                            //  check if the random id is same
                            long outSideChatID = (long) snapshotLast.child("randomID").getValue();
                            if(deleteRandomID == outSideChatID){
                                //   the message
                                refLastDetails.child(user.getUid()).child(otherUid)
                                        .child("message").setValue("...");
                                refLastDetails.child(otherUid).child(user.getUid())
                                        .child("message").setValue("...");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // get all users pin message and store them in each user map
    private void getPinMessages(String myID, String otherID, String otherName){

        List<PinMessageModel> pinMsgList = new ArrayList<>();
        refPrivatePinChat.child(myID).child(otherID).orderByChild("pinTime").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(readDatabase == 0){
                    pinMsgList.clear();

                    for (DataSnapshot snapshotPin : snapshot.getChildren()) {
                        // check if pin message still exist
                        if(snapshotPin.child("msgId").exists()){
                            PinMessageModel pinMsgModel = snapshotPin.getValue(PinMessageModel.class);
                            pinMsgList.add(pinMsgModel);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        pinPrivateMessageMap.put(otherName, pinMsgList);
    }

    private void pinAndUnpinMsgPrivate(){

        // get total pin messages
        int totalPinMsgCount = pinPrivateMessageMap.get(otherUserName).size();

        // get data to save to database
        Map<String, Object> pinDetails = new HashMap<>();
        pinDetails.put("msgId", msgId);
        pinDetails.put("message", message);
        pinDetails.put("pinTime", timeStamp);

        boolean found = false;
        String idFound = null;

        // check if message has already been pin or not
        for (PinMessageModel pinMes : pinPrivateMessageMap.get(otherUserName)) {

            if (pinMes.getMsgId().equals(msgId)) {
                found = true;
                idFound = pinMes.getMsgId();
                break;
            }
        }

        if (found) {
            // Delete message from the local map
            pinPrivateMessageMap.get(otherUserName)
                    .removeIf(pinMesExist -> pinMesExist.getMsgId().equals(msgId));

            //  Delete message from firebase database
            refPrivatePinChat.child(user.getUid()).child(otherUserUid).child(idFound).removeValue();

            //  Decrement the count
            int newCount = totalPinMsgCount - 1;
            totalPinPrivate_TV.setText("" + newCount);
            pinCount_TV.setText("(1/" + newCount + ")");

            // check if message that is unpin is same with what is on the UI
            if(pinMsg_TV.getText().equals(message)){
                pinMsg_TV.setText("Message unpin...");
                pinMsg_TV.setTypeface(null, Typeface.BOLD_ITALIC);
            }

        } else {
            // Add the new pin message to the local map
            PinMessageModel newPin = new PinMessageModel(msgId,
                    message, timeStamp);
            MainActivity.pinPrivateMessageMap.get(MainActivity.otherUserName).add(newPin);

            // Add the new pin message to firebase database
            refPrivatePinChat.child(user.getUid()).child(otherUserUid).child(msgId)
                    .setValue(pinDetails);

            //  Increment the count
            int newCount = totalPinMsgCount + 1;
            totalPinPrivate_TV.setText("" + newCount);
            pinCount_TV.setText("(1/" + newCount + ")");

            // update to new msg on UI
            pinMsg_TV.setText(message);
            pinNextPrivate = 1;    // return to default, 1

            // trigger pinContainer visible if it's the pin
            pinMsgContainer.setVisibility(View.VISIBLE);

        }

        // close the pin box option
        pinOptionBox.setVisibility(View.GONE);

    }

    private void scrollToPinMessage(int i){
        pinMsg_TV.setTypeface(null);    // remove italic style if any

        // get the msg id you want to scroll to
        String findMsgId = pinPrivateMessageMap.get(otherUserName).get(i).getMsgId();
        // get the position of the message
        int position = adapterMap.get(otherUserName).findMessagePositionById(findMsgId);

        if(position != RecyclerView.NO_POSITION){

            int positionDiff = scrollNum - position;

            // scroll to position of the msgId that's found
            if(positionDiff >= 0 ) {
                recyclerMap.get(otherUserName).scrollToPosition(position - 14);
            } else {
                recyclerMap.get(otherUserName).scrollToPosition(position - 2);
            }

            System.out.println("What is position " + scrollNum + " and " + positionDiff);
            // highlight the message found
            adapterMap.get(otherUserName).highlightItem(position);
            adapterMap.get(otherUserName).highlightedPositions.clear();
            adapterMap.get(otherUserName).highlightedPositions.add(position);  // add to color list

        }
    }

    // scroll to position on new message update
    private void scrollToPreviousPosition(String otherName, int position){
        for (int i = 0; i < recyclerContainer.getChildCount(); i++) {
            View child = recyclerContainer.getChildAt(i);

            if (child == recyclerMap.get(otherName)){
                RecyclerView recyclerView = (RecyclerView) child;

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                layoutManager.scrollToPosition(position);

            }
        }
    }

    // change unread message of otherUser to read status when I open the chat box
    private void convertUnreadToReadMessage(String otherName, String userName, String otherUid){

        new Thread(() -> {
            refMsgFast.child(otherName).child(userName).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    for (DataSnapshot snapshotCheck : snapshot.getChildren()){

                        // check if otherUser message exist and change to "read status" (700016)
                        if(snapshotCheck.child("from").exists()){
                            String getKey = snapshotCheck.getKey();
                            if(insideChat == "yes"){
                                long readStatus = (long) snapshotCheck.child("msgStatus").getValue();
                                if(readStatus != 700016){
                                    refMsgFast.child(otherName).child(userName).child(getKey).child("msgStatus").setValue(700016);
                                }
                            }
                        } else {
                            refMsgFast.child(otherName).child(userName).child(snapshotCheck.getKey()).removeValue();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            // change lastMessageDetails too
            refLastDetails.child(otherUid).child(user.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    try{
                        long readStatus = (long) snapshot.child("msgStatus").getValue();
                        if(insideChat == "yes" && readStatus != 700016){
                            refLastDetails.child(otherUid).child(user.getUid()).child("msgStatus").setValue(700016);
                        }
                    }catch (Exception e){
                        System.out.println("LastMessageDetails error (M546) " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }).start();
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

                    // call handler runnable to check for internet access when typing
                    handler1.post(internetCheckRunnable);

                    circleSendMessage.setVisibility(View.VISIBLE);
                    recordButton.setVisibility(View.INVISIBLE);
                    camera_IV.setVisibility(View.INVISIBLE);
                    refChecks.child(otherUserUid).child(user.getUid()).child("typing").setValue(1);

                } else {
                    circleSendMessage.setVisibility(View.INVISIBLE);
                    recordButton.setVisibility(View.VISIBLE);
                    camera_IV.setVisibility(View.VISIBLE);
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

//                        // reset the new msg count
//                        DatabaseReference statusCheck2 = FirebaseDatabase.getInstance().getReference("Checks");
//                        statusCheck2.child(otherUserUid).addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                                try{
//                                    boolean statusState = (boolean) snapshot.child(user.getUid())
//                                            .child("status").getValue();
//
//                                    // receiver should be 0
//                                    if(statusState == true) {
//                                        statusCheck2.child(otherUserUid).child(user.getUid()).child("newMsgCount").setValue(0);
//                                    }
//
//                                    // Mine should be 0
//                                    statusCheck2.child(user.getUid()).child(otherUserUid).child("newMsgCount").setValue(0);
//
//                                } catch (Exception e){
//                                    statusCheck2.child(otherUserUid).child(user.getUid())
//                                            .child("status").setValue(false);
//                                }
//
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError error) {
//
//                            }
//                        });

                    }
                }).start();

            }
        });
    }

    // show when user is typing
    public void getMyUserTyping()
    {
        new Thread(() -> refChecks.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                try {
                    long typing = (long) snapshot.child(otherUserUid).child("typing").getValue();

                    handler.post(() -> {

                        if(typing == 1){
                            textViewMsgTyping.setText("typing...");
                        } else{
                            textViewMsgTyping.setText("");
                        }
                    });
                } catch (Exception e){
                    refChecks.child(user.getUid()).child(otherUserUid).child("typing").setValue(0);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        })).start();

    }

    // change all load status message to delivery status (700033 ---> 700024)
    private void reloadFailedMessagesWhenNetworkIsOk()
    {
        if(readDatabase == 1){  // always read

            // reload message for inside chat box
            new Thread(() -> {
                // loop through the otherUserNames stored in stringList and check if any user has load message status
                for (String names : otherNameList) {
                    refMsgFast.child(myUserName).child(names).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            for (DataSnapshot snapshotCheck : snapshot.getChildren()){

                                // check if my message exist and change to "delivery status" (700024)
                                if(snapshotCheck.child("from").exists()){
                                    String getKey = snapshotCheck.getKey();

                                    try{     // change load message to delivery status
                                        long readStatus = (long) snapshotCheck.child("msgStatus").getValue();
                                        if(readStatus == 700033 && networkListener == "yes"){

                                            refMsgFast.child(myUserName).child(names).child(getKey)
                                                    .child("msgStatus").setValue(700024);
                                        }
                                    } catch(Exception e){
                                        System.out.println("Error null (M1273) " + e.getMessage());
                                    }
                                } else {    // delete from cache if it doesn't exist
                                    refMsgFast.child(myUserName).child(names).child(snapshotCheck.getKey()).removeValue();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

            }).start();

            // reload message for outside box
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {

                for (String otherUid : otherUidList) {
                    refLastDetails.child(user.getUid()).child(otherUid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if(snapshot.child("from").exists()){
                                try{     // change load message to delivery status
                                    long readStatus = (long) snapshot.child("msgStatus").getValue();
                                    if(readStatus == 700033 && networkListener == "yes"){

                                        refLastDetails.child(user.getUid()).child(otherUid)
                                                .child("msgStatus").setValue(700024);
                                    }

                                } catch(Exception e){
                                    System.out.println("Error null (M1273) " + e.getMessage());
                                }

                            } else {
                                refLastDetails.child(user.getUid()).child(otherUid).removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

                // Shutdown the executor when it's no longer needed
                executor.shutdown();
            });
        }
    }

    //  Get all previous counts of unreadMsg and newMsgCount
    private void getPreviousCounts(){

        Thread thread = new Thread(() -> {

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
                        // if last new msg count is not 0, then get the count
                        if(!snapshot.child("newMsgCount").getValue().equals(0)){
                            newMsgCount = (long) snapshot.child("newMsgCount").getValue();
                        }
                    } catch (Exception e){
                        referenceMsgCountCheck.child("unreadMsg").setValue(0);
                        referenceMsgCountCheck.child("newMsgCount").setValue(0);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

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
        // turn off my online and set my last seen date/time
        refUser.child(user.getUid()).child("presence").setValue(ServerValue.TIMESTAMP);

        new Thread(()->{
            if(constraintMsgBody.getVisibility() == View.VISIBLE){
                try{
                    refChecks.child(otherUserUid).child(user.getUid()).child("typing").setValue(0);
                    int scroll = scrollNum > 20 ? scrollNum: adapterMap.get(otherUserName).getItemCount() - 1;

                    Map<String, Object> mapUpdate = new HashMap<>();
                    mapUpdate.put("status", false);
                    mapUpdate.put("newMsgCount", 0);
                    mapUpdate.put("scrollPosition", otherUserName+scroll);   //split when recovering the values later
                    refChecks.child(user.getUid()).child(otherUserUid).updateChildren(mapUpdate);

                    scrollPositionMap.put(otherUserName, scrollNum);

                } catch (Exception e){
                    System.out.println("Check onPause (M1497) " + e.getMessage());
                }
            }
        }).start();

        super.onPause();

    }

    @Override
    protected void onResume() {
        // change my presence back to "online"
        refUser.child(auth.getUid()).child("presence").setValue(1);

        if(constraintMsgBody.getVisibility() == View.VISIBLE){
            try{
//                Map<String, Object> mapUpdate = new HashMap<>();
//                mapUpdate.put("status", true);
                refChecks.child(user.getUid()).child(otherUserUid).child("status").setValue(true);

            } catch (Exception e){
                System.out.println("Check onResume (M1546) " + e.getMessage());
            }
        }

        super.onResume();
    }


    @Override
    public void onBackPressed() {

        if(constraintMsgBody.getVisibility() == View.VISIBLE){

            constraintMsgBody.setVisibility(View.INVISIBLE);
            cardViewReplyOrEdit.setVisibility(View.GONE);
            topMainContainer.setVisibility(View.VISIBLE);
            conTopUserDetails.setVisibility(View.INVISIBLE);
            conUserClick.setVisibility(View.INVISIBLE);

            editTextMessage.setText("");    // clear message not sent
            replyText = null;
            replyFrom = null;
            replyVisibility = 8;
            idKey = null;
            textViewReplyOrEdit.setText("");
            constraintDelBody.setVisibility(View.GONE); // close delete options
            textViewLastSeen.setText("");   // clear last seen
            circleImageOnline.setVisibility(View.INVISIBLE);
            constraintProfileMenu.setVisibility(View.GONE); // close profile menu

            // highlight send message and new receive message indicator
            receiveIndicator.setVisibility(View.GONE);
            sendIndicator.setVisibility(View.GONE);

            // close pin msg box and show only the pin icon
            pinIconsContainer.setVisibility(View.VISIBLE);
            pinMsgBox_Constr.setVisibility(View.INVISIBLE);
            totalPinPrivate_TV.setText(""); // make pin count null
            pinNextPrivate = 1;  // return pinNumber to default
            pinOptionBox.setVisibility(View.GONE);  // close the option box

            adapterMap.get(otherUserName).highlightedPositions.clear(); // clear the highlight if any
            // Clear previous highlight, if any.
            for (int i = 0; i < recyclerMap.get(otherUserName).getChildCount(); i++) {
                View itemView = recyclerMap.get(otherUserName).getChildAt(i);
                itemView.setBackgroundColor(Color.TRANSPARENT);
            }

            pinMsgContainer.setVisibility(View.GONE);// hide pin Msg container

            new Thread(() -> {

                int scroll = scrollNum > 20 ? scrollNum: adapterMap.get(otherUserName).getItemCount() - 1;

                Map<String, Object> mapUpdate = new HashMap<>();
                mapUpdate.put("status", false);
                mapUpdate.put("newMsgCount", 0);
                // save scroll to database to recover the recycler position it was
                mapUpdate.put("scrollPosition", otherUserName+(scroll));
                refChecks.child(user.getUid()).child(otherUserUid).updateChildren(mapUpdate);

                scrollPositionMap.put(otherUserName, scroll);
                System.out.println("I have saved it " + scroll);

                // set responds to pend always      ------- will change later to check condition if user is still an active call
//                    refChecks.child(user.getUid()).child(otherUserUid).child("vCallResp").setValue("pending");

                insideChat = "no";
                idKey = null;

            }).start();

        } else {
            super.onBackPressed();
        }
    }


    // later ------------- on screen rotate. Save mode or alert
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        // Save important data to the bundle
//        outState.putString("key", "Hello " + otherUserName);
//
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        // Restore data from the bundle
//        String value = savedInstanceState.getString("key");
//        Toast.makeText(this, "yes "+value, Toast.LENGTH_SHORT).show();
//    }
}

















