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
import android.graphics.Rect;
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
import android.view.ViewTreeObserver;
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
import com.pixel.chatapp.R;
import com.pixel.chatapp.adapters.ChatListAdapter;
import com.pixel.chatapp.chats.MessageAdapter;
import com.pixel.chatapp.chats.MessageModel;
import com.pixel.chatapp.model.EditMessageModel;
import com.pixel.chatapp.model.PinMessageModel;
import com.pixel.chatapp.signup_login.LoginActivity;
import com.pixel.chatapp.general.ProfileActivity;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiPopup;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements FragmentListener {

    private TabLayout tabLayoutGeneral;
    private ViewPager2 viewPager2General;
    private ImageView menuOpen, menuClose, imageViewLogo, imageViewUserPhoto;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    DatabaseReference refUser = FirebaseDatabase.getInstance().getReference("Users");
    ConstraintLayout scrollMenu, topMainContainer, mainViewConstraint;
    private TextView logout, textLightAndDay, textViewDisplayName, textViewUserName;
    Switch darkMoodSwitch;
    CardView cardViewSettings;
    SharedPreferences sharedPreferences;
    private Boolean nightMood;

    private MainActivity mainActivityContext = MainActivity.this;

    public MainActivity getMainActivityContext() {
        return mainActivityContext;
    }

    //    ------- message/chat declares
    private ImageView imageViewBack;
    public static CircleImageView circleImageOnline, circleImageLogo;
    private ImageView imageViewOpenMenu, imageViewCloseMenu, imageViewCancelDel, replyOrEditCancel_IV;
    public static ConstraintLayout conTopUserDetails, conUserClick, typeMsgContainer;
    private ImageView editOrReplyIV, imageViewCalls;
    public static TextView textViewOtherUser, textViewLastSeen, textViewMsgTyping, textViewReplyOrEdit, nameReply, replyVisible;
    private ConstraintLayout chatMenuProfile, constraintDelBody;
    private static ImageView emoji_IV, file_IV, camera_IV;
    private Context mContext;

    // -----------      pin declares
    private static ConstraintLayout pinIconsContainer, pinMsgBox_Constr;
    public static ConstraintLayout pinMsgContainer, pinOptionBox, line, chatContainer;
    private static ImageView hidePinMsg_IV, pinClose_IV, pinPrivateIcon_IV, pinLockPrivate_IV, pinPublicIcon_IV, pinLockPublic_IV;
    private int pinNextPublic, pinNextPrivate, pinScrollPrivate, pinScrollPublic;
    private String msgId, message, pinByWho, pinStatus = "null", chatNotFoundID = "null";
    private MessageAdapter.MessageViewHolder holderPin, holderEmoji;
    private final String PRIVATE = "private";
    private final String PUBLIC = "public";
    private Object timeStamp;
    private ImageView arrowUp, arrowDown, cancelPinOption;
    private static TextView totalPinPrivate_TV, pinCount_TV, pinMsg_TV, totalPinPublic_TV, newPinIndicator_TV;
    public static TextView pinMineTV, pinEveryoneTV, pinByTV;


    //  ---------       Forward chat declares
    private ConstraintLayout forwardTopContainer, forwardDownContainer;
    private ImageView cancleForward_IV, searchUserForward_IV;
    public static TextView totalUser_TV;
    public static int selectCount;
    public static CircleImageView circleForwardSend;
    public static boolean onForward;
    public static List<ChatListAdapter.ChatViewHolder> myHolder_;
    List<ChatListAdapter.ChatViewHolder> myHolderNew;
    public static List <String> selectedUsernames;
    public static Map<String, Object> forwardMessageMap;
    private int forwardType;
    private long forwardRandomID;
    private String forwardChat;

    //  ---------   Delete User from ChatList Declares
    private ConstraintLayout deleteUserOrClearChatContainer;
    private ImageView cancelUserDelete_IV;
    private TextView deleteUserForMe_TV, deleteUserForAll_TV, otherUserName_TV;
    private String myUserName_Del, otherUserName_Del, otherUid_Del;

    //  --------    Chat Box Menu Declares
    private TextView clearChat_TV;

    //  ----------------

    private TextView textViewDelMine, textViewDelOther, textViewDelAll;
    private static EditText editTextMessage, et_emoji;
    private static CircleImageView circleSendMessage;
    private CardView cardViewMsg, cardViewReplyOrEdit;
    public static ImageView scrollPositionIV, sendIndicator;
    public static TextView scrollCountTV, receiveIndicator;

    // network settings
    private ConstraintLayout constrNetConnect, constrNetork;
    public static String otherUserUid, otherUserName, myUserName, imageUri;
//    public static String goToLastMessage;
    public static int goToNum;
    public static Boolean goToLastMessage = false;

    private static DatabaseReference refMessages, refMsgFast, refLastDetails, refChecks, refUsers,
            refChatList, refEditMsg, refDeleteMsg, refPrivatePinChat, refPublicPinChat, refClearSign;
    public static FirebaseUser user;
    public static int scrollNum = 0;
    private long count = 0, newMsgCount = 0;
    private static String idKey, listener = "no", replyFrom, replyText, networkListener = "yes", insideChat = "no";
    private static int replyVisibility = 8;    // gone as dafault
    private long randomKey;     // use to fetch randomID when on edit mode

    //  --------   voice note declares

    private String audioPath;
    private MediaRecorder mediaRecorder;
    private Permission permissions;
    private static final int PAGE_SIZE = 20; // Number of items to fetch per page
    private int currentPage; // Current page number
    static RecordView recordView;
    static RecordButton recordButton;
    public static ConstraintLayout constraintMsgBody;

    Handler handler = new Handler(Looper.getMainLooper());

    //  checks declares
    public static int downMsgCount, readDatabase;  // 0 is read, 1 is no_read
    public static boolean loadMsg = true, isKeyboardVisible = false;
    private Boolean clearOnlyChatHistory = false, isEmojiVisible = false, clearHighLight = false;

    //  -----------     All Maps declares
    public static Map<String, List<PinMessageModel>> pinPrivateChatMap, pinPublicChatMap;
    private Map<String, Object> editMessageMap;
    private Map<String, Integer> dateNum, dateMonth;
    private Map<String, Object> deleteMap;
    private Map<String, List<MessageModel>> modelListMap;
    public static Map<String, MessageAdapter> adapterMap;
    public static Map<String, RecyclerView> recyclerMap;
    private static List<String> otherNameList, otherUidList;
    public static Map<String, Object> downMsgCountMap, scrollNumMap;
    private Map<String, Object>  notifyCountMap, scrollPositionMap;

    public static ConstraintLayout recyclerContainer;

    NetworkChangeReceiver networkChangeReceiver;
    private static Handler handler1;
    static Runnable internetCheckRunnable;

    int currentImageResource = R.drawable.baseline_add_reaction_24; // Initialize with the default image resource

    //  ------- emoji declares
    private EmojiPopup popup;
    private Handler handlerEmoji = new Handler();
    private int clearNumb = 0;
    private String chatID;
    private Runnable emojiRunnable;
    private boolean isChatKeyboardON;
    private static ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener;

    //  ---------- msg end


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //      --------- message ids starts        ------------------------

        conTopUserDetails = findViewById(R.id.contraintTop9);
        conUserClick = findViewById(R.id.constraintNextTop9);
        chatContainer = findViewById(R.id.chatBoxContainer);
        recyclerContainer = findViewById(R.id.constraintRecyler);
        imageViewCalls = findViewById(R.id.imageViewCalls9);
        constraintMsgBody = findViewById(R.id.constraintMsgBody);
        imageViewBack = findViewById(R.id.imageViewBackArrow9);
        textViewOtherUser = findViewById(R.id.textViewName9);
        editTextMessage = findViewById(R.id.editTextMessage9);
        et_emoji = findViewById(R.id.et_emoji);
        circleSendMessage = findViewById(R.id.fab9);
        imageViewOpenMenu = findViewById(R.id.imageViewUserMenu29);
        imageViewCloseMenu = findViewById(R.id.imageViewCancel9);
        chatMenuProfile = findViewById(R.id.chatMenuConstraint);
        textViewLastSeen = findViewById(R.id.textViewStatus9);
        textViewMsgTyping = findViewById(R.id.textViewTyping29);
        circleImageOnline = findViewById(R.id.circleImageOnline9);
        circleImageLogo = findViewById(R.id.circleImageLogo9);
        typeMsgContainer = findViewById(R.id.typeMsgContainer);

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
        pinLockPrivate_IV = findViewById(R.id.private_IV);
        pinLockPublic_IV = findViewById(R.id.public_IV);
        line = findViewById(R.id.line_);
        newPinIndicator_TV = findViewById(R.id.newPinIndicator_TV);
        pinClose_IV = findViewById(R.id.pinClose_IV);
        pinByTV = findViewById(R.id.pinByWho_TV);

        // pin option (only me or everyone)
        pinOptionBox = findViewById(R.id.pinOptionBoxConstr);
        pinMineTV = findViewById(R.id.textViewPinMine);
        pinEveryoneTV = findViewById(R.id.textViewPinEveryone);
        cancelPinOption = findViewById(R.id.imageViewCancelPin);

        // Forward chat ids
        forwardTopContainer = findViewById(R.id.forwardConstraint);
        forwardDownContainer = findViewById(R.id.forwardLastConstr);
        cancleForward_IV = findViewById(R.id.forwardCancel_IV);
        searchUserForward_IV = findViewById(R.id.forwardSearchIV);
        totalUser_TV = findViewById(R.id.userSelectedCount_TV);
        circleForwardSend = findViewById(R.id.circleForwardSend);

        // reply and edit settings
        cardViewReplyOrEdit = findViewById(R.id.cardViewReply9);
        textViewReplyOrEdit = findViewById(R.id.textViewReplyText9);
        replyOrEditCancel_IV = findViewById(R.id.imageViewCancleReply9);
        editOrReplyIV = findViewById(R.id.editOrReplyImage9);
        nameReply = findViewById(R.id.fromTV9);
        replyVisible = findViewById(R.id.textReplying9);

        // scroll position and network ids
        constrNetork = findViewById(R.id.loadingPageContainer);
        constrNetConnect = findViewById(R.id.constrNetCheck);
        scrollPositionIV = findViewById(R.id.scrollToPositionIV);
        scrollCountTV = findViewById(R.id.scrollCountTV);
        receiveIndicator = findViewById(R.id.receiveIndicatorTV);
        sendIndicator = findViewById(R.id.sendIndicatorIV);

        // documents ids
        emoji_IV = findViewById(R.id.emoji_IV);
        file_IV = findViewById(R.id.files_IV);
        camera_IV = findViewById(R.id.camera_IV);

        //  delete user from chat list ids
        deleteUserOrClearChatContainer = findViewById(R.id.deleteUserOrClearChatContainer);
        deleteUserForMe_TV = findViewById(R.id.deleteForMe_TV);
        deleteUserForAll_TV = findViewById(R.id.deleteForEveryone_TV);
        cancelUserDelete_IV = findViewById(R.id.cancelDelete_IV);
        otherUserName_TV = findViewById(R.id.otherUserName_TV);

        //  Chat Box Menu ids
        clearChat_TV = findViewById(R.id.clearChat_TV);

        // audio swipe button ids   --  voice note
        recordView = (RecordView) findViewById(R.id.record_view9);
        recordButton = (RecordButton) findViewById(R.id.record_button9);
        recordButton.setRecordView(recordView);

        // database references
        refMessages = FirebaseDatabase.getInstance().getReference("Messages");
        refChatList = FirebaseDatabase.getInstance().getReference("ChatList");
        refMsgFast = FirebaseDatabase.getInstance().getReference("MsgFast");
        refChecks = FirebaseDatabase.getInstance().getReference("Checks");
        refUsers = FirebaseDatabase.getInstance().getReference("Users");
        refLastDetails = FirebaseDatabase.getInstance().getReference("UsersList");
        refEditMsg = FirebaseDatabase.getInstance().getReference("EditMessage");
        refDeleteMsg = FirebaseDatabase.getInstance().getReference("DeleteMessage");
        refPrivatePinChat = FirebaseDatabase.getInstance().getReference("PinChatPrivate");
        refPublicPinChat = FirebaseDatabase.getInstance().getReference("PinChatPublic");
        refClearSign = FirebaseDatabase.getInstance().getReference("ClearSign");

        user = FirebaseAuth.getInstance().getCurrentUser();

        pinPrivateChatMap = new HashMap<>();
        pinPublicChatMap = new HashMap<>();
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
        pinNextPublic = 1;
        pinNextPrivate = 1;
        pinScrollPrivate = 0;
        pinScrollPublic = 0;

        //  forward
        myHolderNew = new ArrayList<>();
        selectedUsernames = new ArrayList<>();
        forwardMessageMap = new HashMap<>();
        selectCount = 0;

        // -------------    msg id ends     ------------------------------

        // Home page (User ChatList) ids
        tabLayoutGeneral = findViewById(R.id.tabLayerMain);
        viewPager2General = findViewById(R.id.viewPageMain);
        menuOpen = findViewById(R.id.imageViewMenu);
        menuClose = findViewById(R.id.imageViewMenuClose);
        scrollMenu = findViewById(R.id.profileMenuContainer);
        logout = findViewById(R.id.textViewLogOut);
        imageViewLogo = findViewById(R.id.circleUserImage);
        imageViewUserPhoto = findViewById(R.id.imageViewUserPhoto);
        textViewDisplayName = findViewById(R.id.textViewDisplayName2);
        textViewUserName = findViewById(R.id.textViewUserName2);
        mainViewConstraint = findViewById(R.id.mainViewConstraint);
        darkMoodSwitch = findViewById(R.id.switch1);
        textLightAndDay = findViewById(R.id.textView13);
        topMainContainer = findViewById(R.id.HomeTopConstr);
        cardViewSettings = findViewById(R.id.cardViewSettings);

        hideKeyboard();
        new CountDownTimer(20_000, 1000){
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
        refUser.child(user.getUid()).child("presence").setValue(1);

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

        darkMoodSwitch.setOnClickListener(view -> {
            if(nightMood){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                sharedPreferences.edit().putBoolean("MoodStatus", false).apply();
            } else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                sharedPreferences.edit().putBoolean("MoodStatus", true).apply();
            }
        });


        //  Return back, close msg container
        imageViewBack.setOnClickListener(view -> {
            hideKeyboard();
            insideChat = "no";

            clearEmojiReactSetting();
            onBackPressed();
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
        imageViewOpenMenu.setOnClickListener(view -> chatMenuProfile.setVisibility(View.VISIBLE));

        // close user menu
        imageViewCloseMenu.setOnClickListener(view -> chatMenuProfile.setVisibility(View.GONE));
        chatMenuProfile.setOnClickListener(view -> chatMenuProfile.setVisibility(View.GONE));


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
            Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
        });

        // set files(documents)
        file_IV.setOnClickListener(view -> {
            Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
        });


        // --------- emoji  settings    ----------------------

        popup = EmojiPopup.Builder.fromRootView( recyclerContainer).build(editTextMessage);

        globalLayoutListener = () -> {
            Rect r = new Rect();
            emoji_IV.getWindowVisibleDisplayFrame(r);
            int screenHeight = emoji_IV.getRootView().getHeight();

            // Calculate the height difference between the root view and the visible display frame
            int keypadHeight = screenHeight - r.bottom;
            // Check if the keyboard is hidden
            if (keypadHeight < screenHeight * 0.15) {
                currentImageResource = R.drawable.baseline_add_reaction_24; // Change to the emoji icon or another image
                emoji_IV.setImageResource(currentImageResource);
                isEmojiVisible = false;

                if(!isKeyboardVisible){
                    typeMsgContainer.setVisibility(View.VISIBLE);
                    // activate runnable to get the emoji clicked
                    handlerEmoji.removeCallbacks(emojiRunnable);
                    holderEmoji = null;
                }

                if(clearHighLight){

                    if(clearNumb == 2){
                        for (int i = 0; i < recyclerMap.get(otherUserName).getChildCount(); i++) {
                            View itemView = recyclerMap.get(otherUserName).getChildAt(i);
                            itemView.setBackgroundColor(Color.TRANSPARENT);
                        }
                        clearHighLight = false;
                        isChatKeyboardON = false;
                        //  close chat keyboard if it's on display
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(editTextMessage.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        editTextMessage.requestFocus();
                        //  reverse the emoji initialization back to the emoji button icon
                        popup = EmojiPopup.Builder.fromRootView( recyclerContainer).build(editTextMessage);
                        System.out.println("check here 1");

                    }
                    clearNumb+=1;

                }
                System.out.println("check here 2");
                isKeyboardVisible = true;
            } else {
                isKeyboardVisible = false;
            }

        };

        emoji_IV.setOnClickListener(view -> {

            isChatKeyboardON = true;
            // toggle keyboard and emoji icons
            popup.toggle();

            if(!isEmojiVisible){
                currentImageResource = R.drawable.baseline_keyboard_alt_24;
                isEmojiVisible = true;

            } else{
                currentImageResource = R.drawable.baseline_add_reaction_24;
                isEmojiVisible = false;
            }
            emoji_IV.setImageResource(currentImageResource);
        });

        editTextMessage.setOnClickListener(view -> {

            isChatKeyboardON = true;
            boolean isEmojiVisible_ = popup.isShowing();

            if(isEmojiVisible_){
                popup.toggle();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editTextMessage, InputMethodManager.SHOW_IMPLICIT);
            }
            isEmojiVisible = false;
            emoji_IV.setImageResource(R.drawable.baseline_add_reaction_24);

        });

        // close and cancel reply and edit box
        replyOrEditCancel_IV.setOnClickListener(view -> cancelEditOrReplySetting());


        // Close delete message option
        constraintDelBody.setOnClickListener(view -> {
            constraintDelBody.setVisibility(View.GONE);
            idKey = null;
        });
        imageViewCancelDel.setOnClickListener(view -> {
            constraintDelBody.setVisibility(View.GONE);
            idKey = null;
        });

        //  ----------  delete message onClicks -------------------------

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

        //  -------------------------------------------------------------

        // scroll to previous position of reply message
        scrollPositionIV.setOnClickListener(view -> {

            if(goToLastMessage) {
                recyclerMap.get(otherUserName).scrollToPosition(goToNum - 2);
                adapterMap.get(otherUserName).highlightItem(goToNum); // notify Colour
                MessageAdapter.highlightedPositions.add(goToNum);    // change color

                goToLastMessage = false;
            } else {
                recyclerMap.get(otherUserName).scrollToPosition(adapterMap.get(otherUserName).getItemCount()-1);
                // clear highlight background if any
                MessageAdapter.highlightedPositions.clear();
                // Clear previous highlight, if any.
                for (int i = 0; i < recyclerMap.get(otherUserName).getChildCount(); i++) {
                    View itemView = recyclerMap.get(otherUserName).getChildAt(i);
                    itemView.setBackgroundColor(Color.TRANSPARENT);
                }
            }

        });


        //  ----------  pin message onClicks ---------------------------

        // hide pin message view
        View.OnClickListener closePinBox = view -> {  // personalise later
            pinIconsContainer.setVisibility(View.VISIBLE);
            pinMsgBox_Constr.setVisibility(View.INVISIBLE);
            pinMsgContainer.setClickable(false);    //  allow item on the background clickable

            // hide public pin icons if map is empty
            if(pinPublicChatMap.get(otherUserName).size() < 1 ){
                pinPublicIcon_IV.setVisibility(View.GONE);
                pinLockPublic_IV.setVisibility(View.GONE);
                totalPinPublic_TV.setVisibility(View.GONE);
                newPinIndicator_TV.setVisibility(View.GONE);
            } else {
                pinPublicIcon_IV.setVisibility(View.VISIBLE);
                pinLockPublic_IV.setVisibility(View.VISIBLE);
                totalPinPublic_TV.setVisibility(View.VISIBLE);
            }

            if(!chatNotFoundID.equals("null")){
                if(pinStatus.equals(PUBLIC)){
                    refPublicPinChat.child(user.getUid()).child(otherUserUid).child(chatNotFoundID).removeValue();
                    if(pinNextPublic > 1)   pinNextPublic-=1;
                } else {
                    refPrivatePinChat.child(user.getUid()).child(otherUserUid).child(chatNotFoundID).removeValue();
                    if(pinNextPrivate > 1)   pinNextPrivate-=1;
                    // remove from local list
                    pinPrivateChatMap.get(otherUserName).removeIf(pinMessageModel ->
                            pinMessageModel.getMsgId().equals(chatNotFoundID));
                    totalPinPrivate_TV.setText("" + pinPrivateChatMap.get(otherUserName).size());
                }
                
                chatNotFoundID = "null";    // return back to default null
            }
            pinStatus = "null";

        };
        hidePinMsg_IV.setOnClickListener(closePinBox);
        pinClose_IV.setOnClickListener(closePinBox);

        // open private pin message box
        View.OnClickListener openPrivatePinMsg = view -> { // personalise later
            pinIconsContainer.setVisibility(View.GONE);
            pinMsgBox_Constr.setVisibility(View.VISIBLE);
            pinStatus = PRIVATE;      // indicate to show private pins
            hidePinMsg_IV.setImageResource(R.drawable.lock);  // indicate private icon
            pinMsgContainer.setClickable(true);   //  stop item on the background clickable

            // show current pin message and total pin number
            int pinNum = pinPrivateChatMap.get(otherUserName).size();
            pinCount_TV.setText("(" + pinNextPrivate + "/" + (pinNum) + ")");
            int currentPinNumber = pinNum - pinNextPrivate;
            String getChat;
            try{
                getChat = pinPrivateChatMap.get(otherUserName).get(currentPinNumber).getMessage();
            } catch (Exception e){
                getChat = pinPrivateChatMap.get(otherUserName).get(pinNum-1).getMessage();
            }
            pinMsg_TV.setText(getChat);
            // hide pinByWho visibility
            pinByTV.setVisibility(View.GONE);
            pinClose_IV.setVisibility(View.GONE);

        };
        pinPrivateIcon_IV.setOnClickListener(openPrivatePinMsg);
        totalPinPrivate_TV.setOnClickListener(openPrivatePinMsg);

        // open public pins
        View.OnClickListener openPublicPinMsg = view ->{
            pinIconsContainer.setVisibility(View.GONE);
            pinMsgBox_Constr.setVisibility(View.VISIBLE);
            pinStatus = PUBLIC;      // indicate to show public pins
            hidePinMsg_IV.setImageResource(R.drawable.baseline_public_24);  // indicate public icon
            pinMsgContainer.setClickable(true); // stop item on the background clickable

            // show current pin message and total pin number
            int pinNum = pinPublicChatMap.get(otherUserName).size();
            pinCount_TV.setText("(" + pinNextPublic + "/" + (pinNum) + ")");
            int currentPinNumber = pinNum - pinNextPublic;
            String getChat, getPinBy;
            try{
                getChat = pinPublicChatMap.get(otherUserName).get(currentPinNumber).getMessage();
                getPinBy = pinPublicChatMap.get(otherUserName).get(currentPinNumber).getPinByWho();
            } catch (Exception e){
                getChat = pinPublicChatMap.get(otherUserName).get(pinNum-1).getMessage();
                getPinBy = pinPublicChatMap.get(otherUserName).get(pinNum -1).getPinByWho();
            }
            // display pin chat and pinByWho on the UI
            pinMsg_TV.setTypeface(null);
            pinMsg_TV.setText(getChat);
            pinByTV.setText(getPinBy);
            newPinIndicator_TV.setVisibility(View.GONE);

            // make pinByWho visible and close pin box option
            pinByTV.setVisibility(View.VISIBLE);
            pinClose_IV.setVisibility(View.VISIBLE);

        };
        pinPublicIcon_IV.setOnClickListener(openPublicPinMsg);
        totalPinPublic_TV.setOnClickListener(openPublicPinMsg);

        //  close pin option box
        View.OnClickListener closePinOption = view -> {
            pinOptionBox.setVisibility(View.GONE);
        };
        cancelPinOption.setOnClickListener(closePinOption); // close when the cancel pin is click
        pinOptionBox.setOnClickListener(closePinOption);    // close when the background is click

        // pin message for only me -- private
        pinMineTV.setOnClickListener(view -> pinAndUnpinChatPrivately());
        
        // pin message for everyone
        pinEveryoneTV.setOnClickListener(view -> {
            pinAndUnpinChatForEveryone();   // call pin/unpin method
        });

        // arrow up, scroll to upper previous pins
        arrowUp.setOnClickListener(view -> {    // go to next upper pin message

            if(pinStatus.equals(PRIVATE)){    // show private pins
                pinNextPrivate += 1;
                pinScrollPrivate += 1;
                int pinNum = pinPrivateChatMap.get(otherUserName).size();
                int reduceNumber = pinNum - pinNextPrivate;
                int scrollPosition = pinNum - pinScrollPrivate;

                // only scroll when pin is between 0 to pinNum
                if(scrollPosition >= 0) {
                    scrollToPinMessage(scrollPosition); // call method to scroll to message
                } else {
                    pinScrollPrivate -= 1;
                    Toast.makeText(this, "No more pin message! Scroll down!", Toast.LENGTH_SHORT).show();
                }

                // only update UI when pin is between 0 to pinNum
                if(reduceNumber >= 0){
                    pinCount_TV.setText("(" + pinNextPrivate + "/" + (pinNum) + ")");
                    pinMsg_TV.setText(pinPrivateChatMap.get(otherUserName).get(reduceNumber).getMessage());
                } else{
                    pinNextPrivate -= 1;
                }

            } else {        // show public pins
                pinNextPublic += 1;
                pinScrollPublic += 1;
                int pinNum = pinPublicChatMap.get(otherUserName).size();
                int reduceNumber = pinNum - pinNextPublic;
                int scrollPosition = pinNum - pinScrollPublic;
                String getChat, getPinByWho;
                try{
                    getChat = pinPublicChatMap.get(otherUserName).get(reduceNumber).getMessage();
                    getPinByWho = pinPublicChatMap.get(otherUserName).get(reduceNumber).getPinByWho();
                } catch (Exception e ){
                    getChat = pinPublicChatMap.get(otherUserName).get(pinNum - 1).getMessage();
                    getPinByWho = pinPublicChatMap.get(otherUserName).get(pinNum - 1).getPinByWho();
                }

                if(scrollPosition >= 0) {   // only scroll when pin is between 0 to pinNum
                    scrollToPinMessage(scrollPosition); // call method to scroll to message
                } else {
                    pinScrollPublic -= 1;
                    pinMsg_TV.setText("...");
                    pinByTV.setText("");
//                    Toast.makeText(this, "No more pin message! Scroll down!", Toast.LENGTH_SHORT).show();
                }

                if(reduceNumber >= 0){  // only update UI when pin is between 0 to pinNum
                    pinCount_TV.setText("(" + pinNextPublic + "/" + (pinNum) + ")");
                    pinMsg_TV.setText(getChat);
                    pinByTV.setText("Pin by " + getPinByWho);
                } else{
                    pinNextPublic -= 1;
//                    Toast.makeText(this, "No more pin message!", Toast.LENGTH_SHORT).show();
                }
            }

        });

        // arrow down, scroll to recent pin messages
        arrowDown.setOnClickListener(view -> {    // go to next down pin message

            if(pinStatus.equals(PRIVATE)){
                pinNextPrivate -= 1;
                pinScrollPrivate -= 1;
                int pinNum = pinPrivateChatMap.get(otherUserName).size();
                int increaseNumber = pinNum - pinNextPrivate;
                int scrollPosition = pinNum - pinScrollPrivate;

                if(scrollPosition < pinNum){
                    scrollToPinMessage(increaseNumber); // call method to scroll to message
                } else {
                    pinScrollPrivate += 1; // to enable it stop decreasing
                    Toast.makeText(this, "No more pin message!", Toast.LENGTH_SHORT).show();
                }

                if (increaseNumber < pinNum){
                    pinCount_TV.setText("(" + pinNextPrivate + "/" + (pinNum) + ")");
                    pinMsg_TV.setText(pinPrivateChatMap.get(otherUserName).get(increaseNumber).getMessage());
                } else {
                    pinNextPrivate += 1; // to enable it stop decreasing
                }

            } else {
                pinNextPublic -= 1;
                pinScrollPublic -= 1;
                int pinNum = pinPublicChatMap.get(otherUserName).size();
                int increaseNumber = pinNum - pinNextPublic;
                int scrollPosition = pinNum - pinScrollPublic;
                String getChat, getPinByWho;
                try{
                    getChat = pinPublicChatMap.get(otherUserName).get(increaseNumber).getMessage();
                    getPinByWho = pinPublicChatMap.get(otherUserName).get(increaseNumber).getPinByWho();
                }catch (Exception e){
                    getChat = pinPublicChatMap.get(otherUserName).get(pinNum - 1).getMessage();
                    getPinByWho = pinPublicChatMap.get(otherUserName).get(pinNum - 1).getPinByWho();
                }

                // only scroll when pin is between 0 to pinNum
                if (scrollPosition < pinNum ) {
                    scrollToPinMessage(scrollPosition); // call method to scroll to message
                } else {
                    pinScrollPublic += 1;
                    Toast.makeText(this, "Scroll up for more pin chats!", Toast.LENGTH_SHORT).show();
                }

                // only update UI when pin is between 0 to pinNum
                if (increaseNumber < pinNum){
                    pinCount_TV.setText("(" + pinNextPublic + "/" + (pinNum) + ")");
                    pinMsg_TV.setText(getChat);
                    pinByTV.setText("Pin by " + getPinByWho);
                } else {
                    pinNextPublic += 1; // to enable it stop decreasing
                }
            }

        });

        //  -------------------------------------------------------------

        //  ----------  forward message onClicks ---------------------------

        // cancel forward option
        cancleForward_IV.setOnClickListener(view -> cancelForwardSettings() );

        // send forward message
        circleForwardSend.setOnClickListener(view -> {

            for (String userDetails : selectedUsernames ) {

                // split the user details to get each user name and uid
                String[] eachUser = userDetails.split(" ");
                String otherName = eachUser[0];
                String otherUid = eachUser[1];

                // save to local list for fast update
                MessageModel messageModel = new MessageModel(forwardChat, myUserName, "", 0, "", null,
                        8, "", 700033, forwardType, forwardRandomID, idKey, false, true);

                MessageAdapter adapter = adapterMap.get(otherName);
                adapter.addNewMessageDB(messageModel);

                // scroll to new position only if scrollCheck int is < 20
                int scrollCheck = 0;
                try{
                    scrollCheck = adapter.getItemCount() - (int) scrollNumMap.get(otherName);
                } catch (Exception e) {
                    scrollCheck = adapter.getItemCount();
                }
                int lastPosition = adapterMap.get(otherName).getItemCount()-1;
                if(scrollCheck < 20){    // scroll to last position on new message update.
                    recyclerMap.get(otherName).scrollToPosition(lastPosition);
                }   // else don't scroll.


                String key = refMsgFast.child(myUserName).child(otherName).push().getKey();  // create an id for each message

                // save to new message db for fast response
                refMsgFast.child(myUserName).child(otherName).child(key).setValue(forwardMessageMap);
                refMsgFast.child(otherName).child(myUserName).child(key).setValue(forwardMessageMap);

                // save to main message
                refMessages.child(myUserName).child(otherName).child(key).setValue(forwardMessageMap);
                refMessages.child(otherName).child(myUserName).child(key).setValue(forwardMessageMap);

                // save last msg for outside chat display
                refLastDetails.child(user.getUid()).child(otherUid).setValue(forwardMessageMap);
                refLastDetails.child(otherUid).child(user.getUid()).setValue(forwardMessageMap);

            }

            cancelForwardSettings();
            Toast.makeText(this, "Chat forwarded", Toast.LENGTH_SHORT).show();

        });

        //  -------------------------------------------------------------


        //  ----------  delete user from ChatList  And Clear Chat History onClicks ---------------------------

        //  delete user container   -- close the container when click and cancel button click
        deleteUserOrClearChatContainer.setOnClickListener(view -> cancelUserDeleteOption());
        cancelUserDelete_IV.setOnClickListener(view -> cancelUserDeleteOption());

        // delete user for only me
        deleteUserForMe_TV.setOnClickListener(view -> {

//            refChatList.child(user.getUid()).child(otherUid_Del).removeValue();
            if(!clearOnlyChatHistory){
                refLastDetails.child(user.getUid()).child(otherUid_Del).removeValue();
                refChecks.child(user.getUid()).child(otherUid_Del).removeValue();
            } else {
                adapterMap.get(otherUserName_Del).notifyDataSetChanged();
                refLastDetails.child(user.getUid()).child(otherUid_Del)
                        .child("message").setValue("...");
                Toast.makeText(this, "Chats cleared for me!", Toast.LENGTH_SHORT).show();
            }

            refPublicPinChat.child(user.getUid()).child(otherUid_Del).removeValue();
            refPrivatePinChat.child(user.getUid()).child(otherUid_Del).removeValue();

            refMessages.child(myUserName_Del).child(otherUserName_Del).removeValue();
            refMsgFast.child(myUserName_Del).child(otherUserName_Del).removeValue();

            adapterMap.get(otherUserName_Del).clearChats(); // delete from chats
            cancelUserDeleteOption();
            clearOnlyChatHistory = false;

        });

        // delete user or clear chat for everyone
        deleteUserForAll_TV.setOnClickListener(view -> {

            if(!clearOnlyChatHistory){
                refChatList.child(user.getUid()).child(otherUid_Del).removeValue();
                refLastDetails.child(user.getUid()).child(otherUid_Del).removeValue();
                refChecks.child(user.getUid()).child(otherUid_Del).removeValue();
                
                refChatList.child(otherUid_Del).child(user.getUid()).removeValue();
                refLastDetails.child(otherUid_Del).child(user.getUid()).removeValue();
                refChecks.child(otherUid_Del).child(user.getUid()).removeValue();
            } else {
                refLastDetails.child(user.getUid()).child(otherUid_Del)
                        .child("message").setValue("...");
                refLastDetails.child(otherUid_Del).child(user.getUid())
                        .child("message").setValue("...");

                adapterMap.get(otherUserName_Del).notifyDataSetChanged();
                Toast.makeText(this, "Chats cleared for everyone!", Toast.LENGTH_SHORT).show();
            }

            // delete pin message from database
            refPublicPinChat.child(user.getUid()).child(otherUid_Del).removeValue();
            refPrivatePinChat.child(user.getUid()).child(otherUid_Del).removeValue();
            refPublicPinChat.child(otherUid_Del).child(user.getUid()).removeValue();
            refPrivatePinChat.child(otherUid_Del).child(user.getUid()).removeValue();
            
            //  delete chats from  database
            refMessages.child(myUserName_Del).child(otherUserName_Del).removeValue();
            refMsgFast.child(myUserName_Del).child(otherUserName_Del).removeValue();
            refMessages.child(otherUserName_Del).child(myUserName_Del).removeValue();
            refMsgFast.child(otherUserName_Del).child(myUserName_Del).removeValue();

            refClearSign.child(user.getUid()).child(otherUid_Del).setValue("clear");

            adapterMap.get(otherUserName_Del).clearChats(); // delete chats
            cancelUserDeleteOption();
            clearOnlyChatHistory = false;
        });

        //  -------------------------------------------------------------

        //  ----------  chat box user menu options onClicks---------------------------
        clearChat_TV.setOnClickListener(view -> {
            
            if (adapterMap.get(otherUserName) != null && adapterMap.get(otherUserName).getItemCount() > 0){
                chatMenuProfile.setVisibility(View.GONE);   // hide profile option
                otherUserName_TV.setText(R.string.clear_history );
//                otherUserName_TV.append(" \uD83D\uDE01");
                deleteUserForMe_TV.setText(R.string.clear_for_me);
                deleteUserForAll_TV.setText(R.string.clear_for_everyone);
                deleteUserOrClearChatContainer.setVisibility(View.VISIBLE);

                clearOnlyChatHistory = true;
                otherUserName_Del = otherUserName;
                myUserName_Del = myUserName;
                otherUid_Del = otherUserUid;

            } else Toast.makeText(this, "Chat is empty...", Toast.LENGTH_SHORT).show();
           
        });

        //  -------------------------------------------------------------

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
    public void callAllMethods(String otherName, String userName, String otherUid) {
        getMyUserTyping();
        tellUserAmTyping_AddUser();
        getPreviousCounts();
        convertUnreadToReadMessage(otherName, userName, otherUid);
        pinIconsVisibility(otherName);
        checkClearChatsDB(otherName, otherUid);

        popup = EmojiPopup.Builder.fromRootView( recyclerContainer).build(editTextMessage);
        // activate the listener
        emoji_IV.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
    }

    @Override
    public void firstCallLoadPage(String otherName) {
//        constraintMsgBody.setVisibility(View.INVISIBLE);
//        conTopUserDetails.setVisibility(View.INVISIBLE);
//        conUserClick.setVisibility(View.INVISIBLE);
        System.out.println("M970, What is name " + recyclerMap.size());
        recyclerViewChatVisibility(otherName);

    }

    @Override
    public void chatBodyVisibility(String otherName, String imageUrl, String userName, String uID, Context mContext_,
                                  RecyclerView recyclerChat) {

        editTextMessage.requestFocus();

        // make only the active recyclerView to be visible
        recyclerViewChatVisibility(otherName);

        if(adapterMap.get(otherName) != null){
            System.out.println("Total adapter (M1070) " + adapterMap.get(otherName).getItemCount());
            if(Objects.requireNonNull(adapterMap.get(otherName)).getItemCount() == 0){
                readDatabase = 0;
                getMessage(userName, otherName, uID, mContext);
                offMainDatabase();
                System.out.println("(M1070)  I just reload message for " + otherName);
            }
        }

        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerMap.get(otherName).getLayoutManager();

        assert layoutManager != null;
        downMsgCount = (layoutManager.getItemCount() - 1) - layoutManager.findLastVisibleItemPosition();
        String numb = ""+ downMsgCount;
        scrollCountTV.setText(numb);           // set down msg count
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
        if (imageUrl == null || imageUrl.equals("null")) {
            circleImageLogo.setImageResource(R.drawable.person_round);
        }
        else Picasso.get().load(imageUrl).into(circleImageLogo);

        otherUserName = otherName;
        myUserName = userName;
        imageUri = imageUrl;
        otherUserUid = uID;

    }

    @Override       // run only once    -- also store otherName and otherUid on List
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
    public void getMessage(String userName, String otherName, String otherUID, Context mContext_){

        retrieveMessages(userName, otherName, otherUID, mContext);

        // store myUserName for looping through load message status and change to delivery status
        myUserName = userName;

        mContext = mContext_;

        // get pinMessage once
        getPinChats(user.getUid(), otherUID, otherName);
    }

    @Override
    public void onDeleteMessage(String id, String fromWho, long randomID) {

        clearEmojiReactSetting();
        hideKeyboard();

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
        clearEmojiReactSetting();
        isChatKeyboardON = true;

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
    public void onForwardChat(int forwardType_, long forwardRandomID_, String chat) {

        clearEmojiReactSetting();
        hideKeyboard();

        pinMsgContainer.setVisibility(View.GONE);
        conTopUserDetails.setVisibility(View.INVISIBLE);
        conUserClick.setVisibility(View.INVISIBLE);
        constraintMsgBody.setVisibility(View.INVISIBLE);

        tabLayoutGeneral.setVisibility(View.GONE);
        forwardTopContainer.setVisibility(View.VISIBLE);
        forwardDownContainer.setVisibility(View.VISIBLE);
        forwardTopContainer.setClickable(true);
        forwardDownContainer.setClickable(true);

        onForward = true;

        forwardType = forwardType_;
        forwardRandomID = forwardRandomID_;
        forwardChat = chat;

//        List<ChatListAdapter.ChatViewHolder> myHolderNew = new ArrayList<>();
        myHolderNew.addAll(myHolder_);  // add all users holder that was gotten from chatListAdapter

        // call forward setting method
        ChatListAdapter.getInstance().forwardCheckBoxVisibility(myHolderNew);

    }

    @Override
    public void onPinData(String msgId_, String message_, Object timeStamp_, String pinByWho_, MessageAdapter.MessageViewHolder holder) {
        
        clearEmojiReactSetting();
        hideKeyboard();

        msgId = msgId_;
        message = message_;
        timeStamp = timeStamp_;
        pinByWho = pinByWho_;
        holderPin = holder;
        // show pin option
        pinOptionBox.setVisibility(View.VISIBLE);
    }

    @Override
    public void onEmojiReact(MessageAdapter.MessageViewHolder holder, String chatID_) {

        if (isKeyboardVisible) {
            clearNumb = 0;
            popup.dismiss();
            popup = EmojiPopup.Builder.fromRootView(recyclerContainer).build(et_emoji);
            popup.toggle();

            addClickableEmoji();

        } else {
            if(isChatKeyboardON){
                boolean isEmojiVisible_ = popup.isShowing();
                if(!isEmojiVisible_){
                    popup.toggle();     //  if it's keyboard that's displaying, toggle it to emoji
                    popup.dismiss();    //  then dismiss it and initialise a new EmojiPopup below
                } else {
                    popup.dismiss();
                }

                popup = EmojiPopup.Builder.fromRootView(recyclerContainer).build(et_emoji);
                popup.toggle();

                addClickableEmoji();    // trigger runnable to observe
                isChatKeyboardON = false;
            }
            clearNumb = 1;  // make it 1 since the keyboard is already pop up
        }


        holderEmoji = holder;
        chatID = chatID_;

//        isKeyboardVisible = false;
        clearHighLight = true;
        typeMsgContainer.setVisibility(View.GONE);
        et_emoji.setText("");

    }

    private void addClickableEmoji() {

        handlerEmoji = new Handler();
        emojiRunnable = new Runnable() {
            @Override
            public void run() {

                if(et_emoji.length() > 0){

                    String getEmoji = et_emoji.getText().toString();

                    boolean containsEmoji = false;

                    for (int i = 0; i < getEmoji.length(); i++) {
                        char c = getEmoji.charAt(i);
                        // Check if the character is within any of the emoji character ranges using Unicode escape sequences
                        if ((c >= '\uD83C' && c <= '\uDBFF') || (c >= '\uDC00' && c <= '\uDFFF') ||
                                (c >= '\uD83D' && c <= '\uDE4F') || (c >= '\uD83E' && c <= '\uDEFF') ||
                                (c >= '\u2700' && c <= '\u27BF')) {
                            containsEmoji = true;
                            break;
                        }
                    }
                    if (containsEmoji) {      // add emoji
                        // add to local list
                        adapterMap.get(otherUserName).addEmojiReact(holderEmoji, getEmoji);
                        // add to database so the other user can be notify
                        refMessages.child(myUserName).child(otherUserName).child(chatID).child("emoji").setValue(getEmoji);
                        refMessages.child(otherUserName).child(myUserName).child(chatID).child("emoji").setValue(getEmoji);

                    }
                    et_emoji.setText("");
                    popup.dismiss();
                    hideKeyboard();
                    typeMsgContainer.setVisibility(View.VISIBLE);
                    editTextMessage.requestFocus();
                    //  reverse the emoji initialization back to the emoji button icon
                    popup = EmojiPopup.Builder.fromRootView( recyclerContainer).build(editTextMessage);

                    MessageAdapter.highlightedPositions.clear();
                    //  remove highlight from selected chat
                    if(recyclerMap.get(otherUserName) != null) {
                        for (int i = 0; i < recyclerMap.get(otherUserName).getChildCount(); i++) {
                            View itemView = recyclerMap.get(otherUserName).getChildAt(i);
                            itemView.setBackgroundColor(Color.TRANSPARENT);
                        }
                    }   // remove callBack after processing everything
                    handlerEmoji.removeCallbacks(emojiRunnable);
                }

System.out.println("M1564 I am running");
                handlerEmoji.postDelayed(this, 1000); // Re-schedule the runnable
            }
        };

        handlerEmoji.post(emojiRunnable);

    }

    @Override
    public void msgBackgroundActivities(String otherUid) {

        new Thread(() -> {

            // set my status to be true in case I receive msg, it will be tick as seen
            Map <String, Object> statusAndMSgCount = new HashMap<>();
            statusAndMSgCount.put("status", true);
            statusAndMSgCount.put("unreadMsg", 0);

            this.refChecks.child(this.user.getUid()).child(otherUid).updateChildren(statusAndMSgCount);

            // set responds to pend always      ------- will change later to check condition if user is still an active call
            this.refChecks.child(this.user.getUid()).child(otherUid).child("vCallResp").setValue("pending");

            insideChat = "yes";

        }).start();
    }

    // get last seen and set inbox status to be true
    @Override
    public void getLastSeenAndOnline(String otherUid) {

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

    @Override
    public void onUserDelete(String otherName, String myUserName, String otherUid) {
        String name = "@"+otherName;
        otherUserName_TV.setText(name);
        deleteUserForMe_TV.setText(R.string.delete_for_me);
        deleteUserForAll_TV.setText(R.string.delete_for_everyone);
        deleteUserOrClearChatContainer.setVisibility(View.VISIBLE);
        otherUserName_Del = otherName;
        myUserName_Del = myUserName;
        otherUid_Del = otherUid;
    }


    //  ------------    methods     ---------------
    public static Map<String, Object> setMessage(String message, int type, long randomID){
        // 700024 --- tick one msg  // 700016 -- seen msg   // 700033 -- load

        int msgStatus = 700024;
//        int msgStatus = 700033;

        if(networkListener == "no"){
            msgStatus = 700033;
        }

        Map<String, Object> messageMap = new HashMap<>();

        messageMap.put("from", myUserName);
        messageMap.put("type", type);            // 0 is for text while 1 is for voice note
        messageMap.put("randomID", randomID);
        messageMap.put("message", message);
//            messageMap.put("voicenote", vn);
        messageMap.put("msgStatus", msgStatus);
        messageMap.put( "timeSent", ServerValue.TIMESTAMP);
        messageMap.put("replyFrom", replyFrom);
        messageMap.put("visibility", replyVisibility);
        messageMap.put("replyID", idKey);
        messageMap.put("replyMsg", replyText);
        messageMap.put("isChatPin", false);
        messageMap.put("isChatForward", false);

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
            MessageModel messageModel = new MessageModel(message, myUserName, replyFrom, 0, "", null,
                    replyVisibility, replyText, 700033, type, randomID, idKey, false, false);

            MessageAdapter adapter = adapterMap.get(otherUserName);
            if(adapter != null){
                adapter.addNewMessageDB(messageModel);  // add new chat

                // scroll to new position only if scrollCheck int is < 20
                int scrollCheck = adapter.getItemCount() - (int) scrollNumMap.get(otherUserName);
                int lastPosition = adapterMap.get(otherUserName).getItemCount()-1;
                if(scrollCheck < 20){    // scroll to last position on new message update.
                    recyclerMap.get(otherUserName).scrollToPosition(lastPosition);
                }   // else don't scroll.
            }

            // add one to the dowm message number
            int increaseScroll = (int) downMsgCountMap.get(otherUserName) + 1;
            scrollCountTV.setText(""+increaseScroll);
            downMsgCountMap.put(otherUserName, increaseScroll); // save new position

            // show indicator that msg is sent
            if(scrollPositionIV.getVisibility() == View.VISIBLE){
                sendIndicator.setVisibility(View.VISIBLE);
                receiveIndicator.setVisibility(View.GONE);
            }

            String key = refMsgFast.child(myUserName).child(otherUserName).push().getKey();  // create an id for each message

            // save to main message
            refMessages.child(myUserName).child(otherUserName).child(key).setValue(setMessage(message, type, randomID));
            refMessages.child(otherUserName).child(myUserName).child(key).setValue(setMessage(message, type, randomID));

            // save to new message db for fast response
            refMsgFast.child(myUserName).child(otherUserName).child(key).setValue(setMessage(message, type, randomID));
            refMsgFast.child(otherUserName).child(myUserName).child(key).setValue(setMessage(message, type, randomID));


            // save last msg for outside chat display
            refLastDetails.child(user.getUid()).child(otherUserUid).setValue(setMessage(message, type, randomID));
            refLastDetails.child(otherUserUid).child(user.getUid()).setValue(setMessage(message, type, randomID));

            checkAndSaveCounts_SendMsg();   // save the number of new message I am sending

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

    public void retrieveMessages(String userName, String otherName, String otherUID, Context mContext){

        List<MessageModel> modelListAllMsg = new ArrayList<>();     // save all messages (read and unread)
        List<MessageModel> msgListNotRead = new ArrayList<>();      // save all unread messages from refFastMsg db to get total Count

        MessageAdapter adapter = new MessageAdapter(modelListAllMsg, userName, otherUID, mContext); // initialise adapter

        // loop through New Message (MsgFast) and Old Message and compare the "read" status state before proceeding
        new CountDownTimer(1500, 750){
            @Override
            public void onTick(long l) {

                // retrieve the last previous scroll position
                getLastScrollPosition(otherUID, otherName);
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
                getEditMessage(userName, otherName, modelListAllMsg, adapter, otherUID);

                // delete local list with idkey
                getDeleteMsgId(userName, otherName, modelListAllMsg, adapter, otherUID);

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

                        try{
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

                        } catch(Exception e) {
                            System.out.println("Error at M1775 getAllMes() " + e.getMessage());
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

                        // add the new msg to the modelList method at MessageAdapter
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

    private void pinIconsVisibility(String otherName){

        // make private pin details invisible if no pin chat yet
        if(pinPrivateChatMap.get(otherName) != null){
            int totalPins = pinPrivateChatMap.get(otherName).size();
            if(totalPins > 0){  // make pin icon visible
                pinPrivateIcon_IV.setVisibility(View.VISIBLE);
                pinLockPrivate_IV.setVisibility(View.VISIBLE);
                totalPinPrivate_TV.setVisibility(View.VISIBLE);
                totalPinPrivate_TV.setText(""+ totalPins);
                line.setVisibility(View.VISIBLE);
            }else {    // make pin icon invisible
                pinPrivateIcon_IV.setVisibility(View.INVISIBLE);
                pinLockPrivate_IV.setVisibility(View.INVISIBLE);
                totalPinPrivate_TV.setVisibility(View.INVISIBLE);
            }
        } else {    // make invisible if null
            pinMsgContainer.setVisibility(View.INVISIBLE);
        }

        // make public pin details invisible if no pin chat yet
        if(pinPublicChatMap.get(otherName) != null){
            int totalPins = pinPublicChatMap.get(otherName).size();
            if(totalPins > 0){  // make pin icon visible
                pinPublicIcon_IV.setVisibility(View.VISIBLE);
                pinLockPublic_IV.setVisibility(View.VISIBLE);
                totalPinPublic_TV.setVisibility(View.VISIBLE);
                totalPinPublic_TV.setText(""+ totalPins);
            } else {    // make pin icon invisible
                pinPublicIcon_IV.setVisibility(View.INVISIBLE);
                pinLockPublic_IV.setVisibility(View.INVISIBLE);
                totalPinPublic_TV.setVisibility(View.INVISIBLE);
            }
        } else {
            pinMsgContainer.setVisibility(View.INVISIBLE);
        }

        pinMsgContainer.setClickable(false);    //  allow item on the background clickable
        pinIconsContainer.setVisibility(View.VISIBLE);
        pinMsgContainer.setVisibility(View.VISIBLE);
    }

    // get all users pin message and store them in each user map
    private void getPinChats(String myID, String otherID, String otherName){

        List<PinMessageModel> pinPrivateList = new ArrayList<>();
        List<PinMessageModel> pinEveryoneList = new ArrayList<>();

        // get private pin chats
        refPrivatePinChat.child(myID).child(otherID).orderByChild("pinTime").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // read only once
                if(readDatabase == 0){
                    pinPrivateList.clear();

                    for (DataSnapshot snapshotPin : snapshot.getChildren()) {
                        // check if pin message still exist
                        if(snapshotPin.child("msgId").exists()){
                            PinMessageModel pinMsgModel = snapshotPin.getValue(PinMessageModel.class);
                            pinPrivateList.add(pinMsgModel);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // get and updates new pin chats
        refPublicPinChat.child(myID).child(otherID).orderByChild("pinTime").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                pinEveryoneList.clear();

                for (DataSnapshot snapshotPin : snapshot.getChildren()) {
                    // check if pin message still exist
                    if(snapshotPin.child("msgId").exists()){
                        PinMessageModel pinMsgModel = snapshotPin.getValue(PinMessageModel.class);
                        pinEveryoneList.add(pinMsgModel);
                    }
                }

                int newCount = pinEveryoneList.size();
                totalPinPublic_TV.setText("" + newCount);

                pinPublicChatMap.put(otherName, pinEveryoneList);   // keep updating the map

                // trigger pinIcon visible if pinMsgBox is invisible
                if(pinStatus.equals("null")){
                    // shows icon if pin container in invisible and pin size is greater than 1
                    if(pinMsgBox_Constr.getVisibility() != View.VISIBLE && pinEveryoneList.size() > 0){
                        pinPublicIcon_IV.setVisibility(View.VISIBLE);
                        pinLockPublic_IV.setVisibility(View.VISIBLE);
                        totalPinPublic_TV.setVisibility(View.VISIBLE);

                    } else {
                        //  hide pin_icon if pin map is empty
                        pinPublicIcon_IV.setVisibility(View.GONE);
                        pinLockPublic_IV.setVisibility(View.GONE);
                        totalPinPublic_TV.setVisibility(View.GONE);
                        newPinIndicator_TV.setVisibility(View.GONE);
                    }

                } else if(pinStatus.equals(PUBLIC)){    // update UI
//                    pinMsg_TV.setText("" + pinEveryoneList.get(pinEveryoneList.size()-1).getMessage());
                    pinCount_TV.setText("(" + pinNextPublic + "/" + newCount + ")");
//                    pinNextPublic = 1;    // return to default, 1
                    newPinIndicator_TV.setVisibility(View.VISIBLE);
                    pinMsg_TV.setTypeface(null);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        pinPrivateChatMap.put(otherName, pinPrivateList);
        pinPublicChatMap.put(otherName, pinEveryoneList);

    }

    private void pinAndUnpinChatForEveryone(){

        pinStatus = PUBLIC;      // indicate to show public pins

        //  hide pin_icon if pin map is empty
        if(pinPublicChatMap.get(otherUserName).size() <= 1){
            pinPublicIcon_IV.setVisibility(View.GONE);
            pinLockPublic_IV.setVisibility(View.GONE);
            totalPinPublic_TV.setVisibility(View.GONE);
            newPinIndicator_TV.setVisibility(View.GONE);

        }

        // get total number of pin messages
        int totalPinMsgCount = pinPublicChatMap.get(otherUserName).size();

        // get data to save to database
        Map<String, Object> pinDetails = new HashMap<>();
        pinDetails.put("msgId", msgId);
        pinDetails.put("message", message);
        pinDetails.put("pinTime", timeStamp);
        pinDetails.put("pinByWho", pinByWho);

        boolean found = false;
        String idFound = null;
        for (PinMessageModel pinMes : pinPublicChatMap.get(otherUserName)) {
            if (pinMes.getMsgId().equals(msgId)) {
                found = true;
                idFound = pinMes.getMsgId();
                break;
            }
        }

        boolean foundPrivate = false;
        // check if message has already been pin or not in public pin
        for (PinMessageModel pinMes : pinPrivateChatMap.get(otherUserName)) {
            if (pinMes.getMsgId().equals(msgId)) {
                foundPrivate = true;
                break;
            }
        }

        if (found) {
            // Delete message from the local map
            pinPublicChatMap.get(otherUserName)
                    .removeIf(pinMesExist -> pinMesExist.getMsgId().equals(msgId));

            //  Delete message from firebase database
            refPublicPinChat.child(user.getUid()).child(otherUserUid).child(idFound).removeValue();
            refPublicPinChat.child(otherUserUid).child(user.getUid()).child(idFound).removeValue();

            //  Decrement the UI count
            if(pinStatus.equals(PUBLIC)){
                int newCount = totalPinMsgCount - 1;
                totalPinPublic_TV.setText("" + newCount);
                pinCount_TV.setText("(1/" + newCount + ")");
            }

            // check if message that is unpin is same with what is on the UI
            if(pinMsg_TV.getText().equals(message) && pinStatus.equals(PUBLIC)){
                pinMsg_TV.setText("Chat unpin...");
                pinMsg_TV.setTypeface(null, Typeface.BOLD_ITALIC);
            }

            // change isChatPin to false in database
            if(!foundPrivate){
                refMessages.child(myUserName).child(otherUserName).child(msgId)
                        .child("isChatPin").setValue(false);

                //  show pin icon on the chat UI
                adapterMap.get(otherUserName).pinIconHide(holderPin);
            }

            Toast.makeText(this, "Chat unpin!", Toast.LENGTH_SHORT).show();

        } else {
            // Add the new pin message to the local map
            PinMessageModel newPin = new PinMessageModel(msgId, message, timeStamp, pinByWho);
            pinPublicChatMap.get(otherUserName).add(newPin);

            // Add the new pin message to firebase database
            refPublicPinChat.child(user.getUid()).child(otherUserUid).child(msgId)
                    .setValue(pinDetails);
            refPublicPinChat.child(otherUserUid).child(user.getUid()).child(msgId)
                    .setValue(pinDetails);

            // change isChatPin to true in database
            refMessages.child(myUserName).child(otherUserName).child(msgId)
                    .child("isChatPin").setValue(true);
            refMessages.child(otherUserName).child(myUserName).child(msgId)
                    .child("isChatPin").setValue(true);

            //  show pin icon on the chat UI
            adapterMap.get(otherUserName).pinIconDisplay(holderPin);

            //  Increment the count
            int newCount = totalPinMsgCount + 1;
            totalPinPublic_TV.setText("" + newCount);

            // update to new msg on UI
            if(pinStatus.equals(PUBLIC)){
                pinMsg_TV.setText(message);
                pinCount_TV.setText("(1/" + newCount + ")");
                pinNextPublic = 1;    // return to default, 1
                pinScrollPublic = 0;    // return to default, 0
            }

            if(pinMsgBox_Constr.getVisibility() != View.VISIBLE){
                pinPublicIcon_IV.setVisibility(View.VISIBLE);
                pinLockPublic_IV.setVisibility(View.VISIBLE);
                totalPinPublic_TV.setVisibility(View.VISIBLE);
            }
            Toast.makeText(this, "Chat pin!", Toast.LENGTH_SHORT).show();

        }

        // close the pin box option
        pinOptionBox.setVisibility(View.GONE);
        pinStatus = "null";      // indicate to show public pins

    }

    private void pinAndUnpinChatPrivately(){

        pinStatus = PRIVATE;      // indicate to show public pins

        //  hide pin_icon if pin map is empty
        if(pinPrivateChatMap.get(otherUserName).size() <= 1){
            pinPrivateIcon_IV.setVisibility(View.GONE);
            pinLockPrivate_IV.setVisibility(View.GONE);
            totalPinPrivate_TV.setVisibility(View.GONE);
        }

        // get total number of pin messages
        int totalPinMsgCount = pinPrivateChatMap.get(otherUserName).size();

        // get data to save to database
        Map<String, Object> pinDetails = new HashMap<>();
        pinDetails.put("msgId", msgId);
        pinDetails.put("message", message);
        pinDetails.put("pinTime", timeStamp);

        boolean found = false;
        String idFound = null;

        // check if message has already been pin or not
        for (PinMessageModel pinMes : pinPrivateChatMap.get(otherUserName)) {

            if (pinMes.getMsgId().equals(msgId)) {
                found = true;
                idFound = pinMes.getMsgId();
                break;
            }
        }

        boolean foundPublic = false;
        for (PinMessageModel pinMes : pinPublicChatMap.get(otherUserName)) {

            if (pinMes.getMsgId().equals(msgId)) {
                foundPublic = true;
                break;
            }
        }

        if (found) {
            // Delete message from the local map
            pinPrivateChatMap.get(otherUserName)
                    .removeIf(pinMesExist -> pinMesExist.getMsgId().equals(msgId));

            //  Delete message from firebase database
            refPrivatePinChat.child(user.getUid()).child(otherUserUid).child(idFound).removeValue();

            //  Decrement the UI count
            if(pinStatus.equals(PRIVATE)){
                int newCount = totalPinMsgCount - 1;
                totalPinPrivate_TV.setText("" + newCount);
                pinCount_TV.setText("(1/" + newCount + ")");
            }

            // check if message that is unpin is same with what is on the UI
            if(pinMsg_TV.getText().equals(message) && pinStatus.equals(PRIVATE)){
                pinMsg_TV.setText("Chat unpin...");
                pinMsg_TV.setTypeface(null, Typeface.BOLD_ITALIC);
            }

                // change isChatPin to false in database
            if(!foundPublic){
                refMessages.child(myUserName).child(otherUserName).child(msgId)
                        .child("isChatPin").setValue(false);

                //  show pin icon on the chat UI
                adapterMap.get(otherUserName).pinIconHide(holderPin);
            }

            Toast.makeText(this, "Chat unpin!", Toast.LENGTH_SHORT).show();

        } else {
            // Add the new pin message to the local map
            PinMessageModel newPin = new PinMessageModel(msgId, message, timeStamp, pinByWho);
            pinPrivateChatMap.get(otherUserName).add(newPin);

            // Add the new pin message to firebase database
            refPrivatePinChat.child(user.getUid()).child(otherUserUid).child(msgId)
                    .setValue(pinDetails);

            // change isChatPin to true in database
            refMessages.child(myUserName).child(otherUserName).child(msgId)
                    .child("isChatPin").setValue(true);

            //  show pin icon on the chat UI
            adapterMap.get(otherUserName).pinIconDisplay(holderPin);

            //  Increment the count
            int newCount = totalPinMsgCount + 1;
            totalPinPrivate_TV.setText("" + newCount);

            // update to new msg on UI
            if(pinStatus.equals(PRIVATE)){
                pinMsg_TV.setText(message);
                pinCount_TV.setText("(1/" + newCount + ")");
                pinNextPrivate = 1;    // return to default, 1
                pinScrollPrivate = 0;    // return to default, 1
            }

            // trigger pinIcon visible if pinMsgBox is invisible
            if(pinMsgBox_Constr.getVisibility() != View.VISIBLE){
                pinPrivateIcon_IV.setVisibility(View.VISIBLE);
                pinLockPrivate_IV.setVisibility(View.VISIBLE);
                totalPinPrivate_TV.setVisibility(View.VISIBLE);
            }
            pinMsg_TV.setTypeface(null);
            Toast.makeText(this, "Chat pin!", Toast.LENGTH_SHORT).show();
        }

        // close the pin box option
        pinOptionBox.setVisibility(View.GONE);
        pinStatus = "null";      // indicate to show public pins

    }

    private void scrollToPinMessage(int i){
        pinMsg_TV.setTypeface(null);    // remove italic style if any

        // get the msg id you want to scroll to
        String findMsgId = pinStatus.equals(PRIVATE) ? pinPrivateChatMap.get(otherUserName).get(i).
                getMsgId() : pinPublicChatMap.get(otherUserName).get(i).getMsgId();

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

//            System.out.println("What is position " + scrollNum + " and " + positionDiff);
            // highlight the message found
            adapterMap.get(otherUserName).highlightItem(position);
            MessageAdapter.highlightedPositions.clear();
            MessageAdapter.highlightedPositions.add(position);  // add to color list

        } else {
            Toast.makeText(this, "Chat not found", Toast.LENGTH_SHORT).show();
            chatNotFoundID = findMsgId;
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
                public void onDataChange(@NonNull DataSnapshot snapshot) {;

                    if(snapshot.child("msgStatus").getValue() != null){
                        long readStatus = (long) snapshot.child("msgStatus").getValue();
                        if(insideChat == "yes" && readStatus != 700016){
                            refLastDetails.child(otherUid).child(user.getUid()).child("msgStatus").setValue(700016);
                        }
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
        this.editTextMessage.addTextChangedListener(new TextWatcher() {
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

                new Thread(() -> {
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

    // cancel the forward checkbox visible and restore the chatList user state
    public void cancelForwardSettings(){

        onForward = false;
        selectCount = 0;
        totalUser_TV.setText("0 selected");
        selectedUsernames.clear();
        editTextMessage.requestFocus();

        pinMsgContainer.setVisibility(View.VISIBLE);
        conTopUserDetails.setVisibility(View.VISIBLE);
        conUserClick.setVisibility(View.VISIBLE);
        constraintMsgBody.setVisibility(View.VISIBLE);
        tabLayoutGeneral.setVisibility(View.VISIBLE);

        forwardTopContainer.setVisibility(View.INVISIBLE);
        forwardDownContainer.setVisibility(View.INVISIBLE);

        // call forward setting method
        ChatListAdapter.getInstance().forwardCheckBoxVisibility(myHolderNew);

    }

    // cancel delete option for user from chat list
    private void cancelUserDeleteOption(){
        otherUserName_Del = null;
        myUserName_Del = null;
        otherUid_Del = null;
        otherUserName_TV.setText("");
        deleteUserOrClearChatContainer.setVisibility(View.GONE);
    }

    // turn of readDatabase to 1 (non-read)
    private void offMainDatabase(){
        new CountDownTimer(3000, 1000){
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                readDatabase = 1;
            }
        }.start();
    }

    // check if other user deleted me from his chat list, if yes, then clear all the user chat
    public static void checkClearChatsDB(String otherUid, String otherName){
        refClearSign.child(otherUid).child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.getValue() != null && snapshot.getValue().toString().equals("clear")){
                    if(adapterMap.get(otherName) != null ){
                        // clear local list -- adapter
                        adapterMap.get(otherName).clearChats();
                        adapterMap.get(otherName).notifyDataSetChanged();
                    }
                    //delete from DB
                    refClearSign.child(otherUid).child(user.getUid()).removeValue();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void cancelEditOrReplySetting(){
        nameReply.setVisibility(View.GONE);
        replyVisible.setVisibility(View.GONE);
        cardViewReplyOrEdit.setVisibility((int) 8);   // 8 is for GONE

        listener = "no";
        idKey = null;
//        editTextMessage.setText("");
        replyText = null;
        replyFrom = null;
        idKey = null;
        replyVisibility = 8;
        textViewReplyOrEdit.setText("");
    }

    private void clearEmojiReactSetting(){
        handlerEmoji.removeCallbacks(emojiRunnable);
        et_emoji.clearFocus();
        editTextMessage.requestFocus();
        if(popup != null) popup.dismiss();
        popup = EmojiPopup.Builder.fromRootView( recyclerContainer).build(editTextMessage);
        typeMsgContainer.setVisibility(View.VISIBLE);

        // clear highlight
        MessageAdapter.highlightedPositions.clear();
        for (int i = 0; i < recyclerMap.get(otherUserName).getChildCount(); i++) {
            View itemView = recyclerMap.get(otherUserName).getChildAt(i);
            itemView.setBackgroundColor(Color.TRANSPARENT);
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

        if(popup != null) popup.dismiss();
        typeMsgContainer.setVisibility(View.VISIBLE);
        handlerEmoji.removeCallbacks(emojiRunnable);
        //  hide emoji keyboard
        et_emoji.clearFocus();
        editTextMessage.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et_emoji.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

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
        //  reverse the emoji initialization back to the emoji button icon
        popup = EmojiPopup.Builder.fromRootView( recyclerContainer).build(editTextMessage);

        if(constraintMsgBody.getVisibility() == View.VISIBLE){
            try{
                editTextMessage.requestFocus();
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

            boolean isEmojiVisible_ = popup.isShowing();

            if(isEmojiVisible_){
                popup.dismiss();
                isEmojiVisible = false;
                typeMsgContainer.setVisibility(View.VISIBLE);

            } else{
                emoji_IV.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
                handlerEmoji.removeCallbacks(emojiRunnable);
                et_emoji.clearFocus();
                editTextMessage.clearFocus();

                // General settings
                constraintMsgBody.setVisibility(View.INVISIBLE);
                topMainContainer.setVisibility(View.VISIBLE);
                conTopUserDetails.setVisibility(View.INVISIBLE);
                conUserClick.setVisibility(View.INVISIBLE);
                emoji_IV.setImageResource(R.drawable.baseline_add_reaction_24);
                constraintDelBody.setVisibility(View.GONE); // close delete options
                textViewLastSeen.setText("");   // clear last seen
                circleImageOnline.setVisibility(View.INVISIBLE);
                chatMenuProfile.setVisibility(View.GONE); // close profile menu
                isEmojiVisible = false;

                // edit and reply settings cancel
                cancelEditOrReplySetting();
                // cancel user or clear_chat container if visible
                cancelUserDeleteOption();


                // Pin Chat Settings
                pinIconsContainer.setVisibility(View.GONE);
                pinMsgBox_Constr.setVisibility(View.GONE);
                pinMsgContainer.setVisibility(View.GONE);   // hide pin Msg container
                totalPinPrivate_TV.setText(""); // make pin count null
                totalPinPublic_TV.setText(""); // make pin count null
                pinNextPrivate = 1;  // return pinNumber to default
                pinNextPublic = 1;  // return public pinNumber to default
                pinScrollPublic = 0;
                pinScrollPrivate = 0;
                pinOptionBox.setVisibility(View.GONE);  // close the option box
                line.setVisibility(View.GONE);
                pinStatus = "null";

                // highlight send message and new receive message indicator
                receiveIndicator.setVisibility(View.GONE);
                sendIndicator.setVisibility(View.GONE);

                MessageAdapter.highlightedPositions.clear(); // clear the highlight if any
                // ClearMessageAdapter

                new Thread(() -> {

                    int scroll = 0;
                    if(adapterMap.get(otherUserName) != null){
                        scroll = scrollNum > 20 ? scrollNum: adapterMap.get(otherUserName).getItemCount() - 1;
                    }
                    Map<String, Object> mapUpdate = new HashMap<>();
                    mapUpdate.put("status", false);
                    mapUpdate.put("newMsgCount", 0);
                    // save scroll to database to recover the recycler position it was
                    mapUpdate.put("scrollPosition", otherUserName+(scroll));
                    if(scroll > 5) {
                        refChecks.child(user.getUid()).child(otherUserUid).updateChildren(mapUpdate);

                        scrollPositionMap.put(otherUserName, scroll);
                        System.out.println("M3052 I have saved scroll " + scroll);
                    }

                    // set responds to pend always      ------- will change later to check condition if user is still an active call
//                    refChecks.child(user.getUid()).child(otherUserUid).child("vCallResp").setValue("pending");

                    insideChat = "no";
                    idKey = null;

                }).start();
            }

        } else if (onForward) {
            cancelForwardSettings();
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

















