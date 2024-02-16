package com.pixel.chatapp.home;

import static com.pixel.chatapp.constants.AllConstants.ACCEPTED_MIME_TYPES;
import static com.pixel.chatapp.constants.AllConstants.REQUEST_PICK_PDF_FILE;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfRenderer;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.devlomi.record_view.OnRecordListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.gson.Gson;
import com.pixel.chatapp.NetworkChangeReceiver;
import com.pixel.chatapp.Permission.Permission;
import com.pixel.chatapp.R;
import com.pixel.chatapp.SendImageActivity;
import com.pixel.chatapp.ZoomImage;
import com.pixel.chatapp.activities.AppLifecycleHandler;
import com.pixel.chatapp.adapters.ChatListAdapter;
import com.pixel.chatapp.chats.MessageAdapter;
import com.pixel.chatapp.constants.AllConstants;
import com.pixel.chatapp.listeners.FragmentListener;
import com.pixel.chatapp.side_bar_menu.settings.ProfileActivity;
import com.pixel.chatapp.home.fragments.ChatsListFragment;
import com.pixel.chatapp.model.EditMessageModel;
import com.pixel.chatapp.model.MessageModel;
import com.pixel.chatapp.model.PinMessageModel;
import com.pixel.chatapp.roomDatabase.entities.EachUserChats;
import com.pixel.chatapp.roomDatabase.viewModels.UserChatViewModel;
import com.pixel.chatapp.signup_login.LoginActivity;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiPopup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
//import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements FragmentListener {

    private static TabLayout tabLayoutGeneral;
    private static ViewPager2 viewPager2General;

    //  ---------   SideBar Menu     -----------------
    private ImageView sideBarMenuOpen, sideBarMenuClose, imageViewLogo, imageViewUserPhoto;
    private ConstraintLayout sideBarMenuContainer;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    private TextView logout, textLightAndDay, textViewDisplayName, textViewUserName, tapImage_TV;
    Switch darkMoodSwitch;
    CardView cardViewSettings;
    String imageLink;

    //  ---------   sharepreference     -----------------
    public static SharedPreferences moodPreferences, myUserNamePreferences, lastPositionPreference,
            offlineChat, photoIdShareRef, voiceNoteIdShareRef, unusedPhotoShareRef;
    public static String getMyUserName;
    private Boolean nightMood;

    private MainActivity mainActivityContext = MainActivity.this;

    public MainActivity getMainActivityContext() {
        return mainActivityContext;
    }

    //    ------- message/chat declares
    private ImageView imageViewBack;
    public static CircleImageView circleImageLogo;
    private ImageView imageViewOpenMenu, imageViewCloseMenu, imageViewCancelDel, replyOrEditCancel_IV;
    public static ConstraintLayout conTopUserDetails, typeMsgContainer;
    private static ConstraintLayout mainViewConstraint, topMainContainer;
    private ImageView editOrReplyIV, imageViewCalls;
    private MessageAdapter.MessageViewHolder replyHolder;
    public static TextView textViewOtherUser, textViewLastSeen, textViewMsgTyping, textViewReplyOrEdit, nameReply, replyVisible;
    private ConstraintLayout chatMenuProfile, constraintDelBody;
    private static ImageView emoji_IV, file_IV, camera_IV, gameMe_IV;
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
    private static ConstraintLayout forwardTopContainer, forwardDownContainer;
    private ImageView cancleForward_IV, searchUserForward_IV;
    public static TextView totalUser_TV;
    public static int selectCount;
    public static CircleImageView circleForwardSend;
    private static ProgressBar progressBarForward;
    public static boolean onForward;
    public static List<ChatListAdapter.ChatViewHolder> myHolder_;
    public static List<ChatListAdapter.ChatViewHolder> myHolderNew;
    public static List <String> forwardChatUserId;
    public static List <String> selectedUserNames;


    //  ---------   Delete User from ChatList Declares
    private ConstraintLayout deleteUserOrClearChatContainer;
    private ImageView cancelUserDelete_IV;
    private TextView deleteUserForMe_TV, deleteUserForAll_TV, otherUserName_TV;
    private String otherUid_Del;

    //  --------    Chat Box Menu Declares
    private TextView clearChat_TV;

    //  ----------------

    private static TextView deleteChatForOnlyMe_TV, deleteChatForOnlyOther_TV, deleteChatsForEveryone_TV;
    private static EditText editTextMessage, et_emoji;
    private static CircleImageView sendMessageButton;
    private static CardView cardViewMsg, cardViewReplyOrEdit;
    public static ImageView scrollPositionIV, sendIndicator;
    public static TextView scrollCountTV, receiveIndicator;

    //  -------------   network settings    -----------
    public static ConstraintLayout constrNetConnect;
    public static String otherUserUid, otherUserName, myUserName, imageUri;

    public static Handler handlerInternet = new Handler(), handlerTyping = new Handler();
    public static Runnable internetCheckRunnable, runnableTyping;

    public static int goToNum;
    public static Boolean goToLastMessage = false;

    //  -------------- database ------------
    public static UserChatViewModel chatViewModel;
    private static MessageModel messageModel;
    private static DatabaseReference refMessages, refMsgFast, refLastDetails, refChecks, refUsers,
            refEditMsg, refDeleteMsg, refPrivatePinChat, refPublicPinChat, refClearSign,
            refDeleteUser, refDeletePin, refEmojiReact, refOnReadRequest, refChatIsRead;

    private ValueEventListener chatReadListener; // Declare the listener as a class variable

//    public static FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    public static FirebaseUser user;

    public static int scrollNum = 0, chatDeliveryStatus = 700033;
    private static long count = 0, newMsgCount = 0;
    private static String idKey, listener = "no", replyFrom, replyText;
    private static boolean insideChat = false;     // for checking when user wants to send photo from other app
    public static String networkListener = "no";
    public static Boolean networkOk = true, networkTypingOk = true;
    private static int replyVisibility = 8;    // gone as dafault


    //  --------   voice note declares
    private String fileNamePath;
    private MediaRecorder mediaRecorder;
    private static MediaPlayer mediaPlayer;
    private Permission permissions = new Permission();
    private static RecordView recordView;
    private static RecordButton recordButton;
    public static ConstraintLayout constraintMsgBody;

    Handler handler = new Handler(Looper.getMainLooper());

    //  --------   chat Options declares
    public static ConstraintLayout chatOptionsConstraints;
    public static TextView chatSelected_TV;
    private static ImageView forwardChatOption_IV, copyChatOption_IV, deleteChatOption_IV, cancelChatOption_IV;

    public static ImageView replyChatOption_IV, emojiChatOption_IV, pinChatOption_IV, editChatOption_IV, pinAllIndicator;
    public static MessageAdapter.MessageViewHolder chatHolder;
    public static MessageModel modelChatsOption;
    public static int chatPosition;
    public static List<MessageModel> chatModelList;


    //  checks declares
    public static int downMsgCount, readDatabase, scNum = 0;// 0 is read, 1 is no_read
    public static boolean loadMsg = true, isKeyboardVisible = false, isSendingFile = false, isSendingVoiceNote = false;
    private Boolean clearOnlyChatHistory = false, isEmojiVisible = false, clearHighLight = false,
            fiveSecondsWait = false;

    //  -----------     All Maps declares
    public static Map<String, List<PinMessageModel>> pinPrivateChatMap, pinPublicChatMap;
    private Map<String, Object> editMessageMap;
    private Map<String, Integer> dateNum, dateMonth;
    private Map<String, Object> deleteMap;
    private static Map<String, List<MessageModel>> modelListMap;
    public static Map<String, MessageAdapter> adapterMap; // to prevent it from re-creating all the time
    public static Map<String, RecyclerView> recyclerMap;
    private static List<String> otherNameList, otherUidList;
    public static Map<String, Object> downMsgCountMap, scrollNumMap;
    private Map<String, Object>  notifyCountMap;
    private static Map<String, Boolean> insideChatMap;

    public static ConstraintLayout recyclerContainer;
    private ConstraintLayout constraintLayoutAdjust;

    private NetworkChangeReceiver networkChangeReceiver;
    int currentImageResource = R.drawable.baseline_add_reaction_24; // Initialize with the default image resource

    //  ------- emoji declares
    private EmojiPopup popup;
    private Handler handlerEmoji = new Handler();
    public static boolean isLoadViewRunnableRunning = false;

    public static Handler handlerLoadViewLayout = new Handler();

    private int clearNumb = 0;
    private String chatID;
    private Runnable emojiRunnable;
    public static Runnable loadViewRunnable;

    private boolean isChatKeyboardON;
    private static ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener;

    private static ItemTouchHelper itemTouchSwipe;

    private static String myId;

    long startTimeMillis;
    int audioBitrate = 128000; // 128 kbps

    //  ========== sharing from other app
    public static boolean appActivity = false;
    public static boolean sharing = false; // previous Main Activity from replace the currentActivity when user is sharing photo from other app or gallery
    public static Boolean sharingPhotoActivated;    // indicate when user is sharing photo from outside app.

    private List<MessageModel> photoModelList;

    //  ---------- msg end

    private ActivityResultLauncher<String[]> pickPdfLauncher;

    ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Dark mood setting
        moodPreferences = this.getSharedPreferences("MOOD", Context.MODE_PRIVATE);
        nightMood = moodPreferences.getBoolean("MoodStatus", false);

        if(nightMood){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textLightAndDay = findViewById(R.id.lightAndDark_TV);
        darkMoodSwitch = findViewById(R.id.switch1);

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
                moodPreferences.edit().putBoolean("MoodStatus", false).apply();
            } else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                moodPreferences.edit().putBoolean("MoodStatus", true).apply();
            }
            recreate();
        });

        user = FirebaseAuth.getInstance().getCurrentUser();

        //      --------- message ids starts        ------------------------
imageView = findViewById(R.id.imageView3);
        conTopUserDetails = findViewById(R.id.firstTopContainer);
        chatContainer = findViewById(R.id.chatBoxContainer);
        recyclerContainer = findViewById(R.id.constraintRecyler);
        constraintLayoutAdjust = findViewById(R.id.constraintLayoutAdjust);
        imageViewCalls = findViewById(R.id.callMeet_IV);
        constraintMsgBody = findViewById(R.id.constraintMsgBody);
        imageViewBack = findViewById(R.id.backArrow_IV);
        textViewOtherUser = findViewById(R.id.userName_TV);
        editTextMessage = findViewById(R.id.editTextMessage9);
        et_emoji = findViewById(R.id.et_emoji);
        sendMessageButton = findViewById(R.id.fab9);
        imageViewOpenMenu = findViewById(R.id.chatMenu_IV);
        imageViewCloseMenu = findViewById(R.id.imageViewCancel9);
        textViewLastSeen = findViewById(R.id.onlineStatus_TV);
        textViewMsgTyping = findViewById(R.id.isTyping_TV);
        circleImageLogo = findViewById(R.id.circleImageLogo9);
        typeMsgContainer = findViewById(R.id.typeMsgContainer);

        //  side bar menu
        sideBarMenuContainer = findViewById(R.id.sideBarMenuContainer);
        logout = findViewById(R.id.textViewLogOut);
        sideBarMenuClose = findViewById(R.id.imageViewMenuClose);
        tapImage_TV = findViewById(R.id.tapImage_TV);
        imageViewUserPhoto = findViewById(R.id.imageViewUserPhoto);
        textViewDisplayName = findViewById(R.id.textViewDisplayName2);
        textViewUserName = findViewById(R.id.textViewUserName2);
        cardViewSettings = findViewById(R.id.cardViewSettings);


        // delete ids
        constraintDelBody = findViewById(R.id.constDelBody);
        deleteChatForOnlyMe_TV = findViewById(R.id.deleteChatForOnlyMe_TV);
        deleteChatForOnlyOther_TV = findViewById(R.id.deleteChatForOnlyOther_TV);
        deleteChatsForEveryone_TV = findViewById(R.id.deleteChatsForEveryone_TV);
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
        progressBarForward = findViewById(R.id.progressBarForward);

        // reply and edit settings
        cardViewReplyOrEdit = findViewById(R.id.cardViewReply9);
        textViewReplyOrEdit = findViewById(R.id.textViewReplyText9);
        replyOrEditCancel_IV = findViewById(R.id.imageViewCancleReply9);
        editOrReplyIV = findViewById(R.id.editOrReplyImage9);
        nameReply = findViewById(R.id.fromTV9);
        replyVisible = findViewById(R.id.textReplying9);

        // scroll position and network ids
        constrNetConnect = findViewById(R.id.constrNetCheck);
        scrollPositionIV = findViewById(R.id.scrollToPositionIV);
        scrollCountTV = findViewById(R.id.scrollCountTV);
        receiveIndicator = findViewById(R.id.receiveIndicatorTV);
        sendIndicator = findViewById(R.id.sendIndicatorIV);

        // documents ids
        emoji_IV = findViewById(R.id.emoji_IV);
        file_IV = findViewById(R.id.files_IV);
        camera_IV = findViewById(R.id.camera_IV);
        gameMe_IV = findViewById(R.id.playGame_IV);

        //  delete user from chat list ids
        deleteUserOrClearChatContainer = findViewById(R.id.deleteUserOrClearChatContainer);
        deleteUserForMe_TV = findViewById(R.id.deleteForMe_TV);
        deleteUserForAll_TV = findViewById(R.id.deleteForEveryone_TV);
        cancelUserDelete_IV = findViewById(R.id.cancelDelete_IV);
        otherUserName_TV = findViewById(R.id.otherUserName_TV);

        //  Chat Box Menu ids
        chatMenuProfile = findViewById(R.id.chatMenuConstraint);
        clearChat_TV = findViewById(R.id.clearChat_TV);

        //  chatOptions ids
        chatOptionsConstraints = findViewById(R.id.chatOptions);
        editChatOption_IV = findViewById(R.id.edit_IV);
        replyChatOption_IV = findViewById(R.id.reply_IV);
        emojiChatOption_IV = findViewById(R.id.react_IV);
        pinChatOption_IV = findViewById(R.id.pin_IV);
        forwardChatOption_IV = findViewById(R.id.forward_IV);
        copyChatOption_IV = findViewById(R.id.copyText_IV);
        deleteChatOption_IV = findViewById(R.id.delete_IV);
        pinAllIndicator = findViewById(R.id.pinALL_IV);
        chatSelected_TV = findViewById(R.id.count_TV);
        cancelChatOption_IV = findViewById(R.id.cancel_IV);


        // audio swipe button ids   --  voice note
        recordView = (RecordView) findViewById(R.id.record_view9);
        recordButton = (RecordButton) findViewById(R.id.record_button9);
        recordButton.setRecordView(recordView);


        tabLayoutGeneral = findViewById(R.id.tabLayerMain);
        viewPager2General = findViewById(R.id.viewPageMain);
        sideBarMenuOpen = findViewById(R.id.imageViewMenu);
        imageViewLogo = findViewById(R.id.circleUserImage);
        mainViewConstraint = findViewById(R.id.mainViewConstraint);
        topMainContainer = findViewById(R.id.HomeTopConstr);

        // database references
        refMessages = FirebaseDatabase.getInstance().getReference("Messages");
        refMsgFast = FirebaseDatabase.getInstance().getReference("MsgFast");
        refChecks = FirebaseDatabase.getInstance().getReference("Checks");
        refUsers = FirebaseDatabase.getInstance().getReference("Users");
        refLastDetails = FirebaseDatabase.getInstance().getReference("UsersList");
        refEditMsg = FirebaseDatabase.getInstance().getReference("EditMessage");
        refDeleteMsg = FirebaseDatabase.getInstance().getReference("DeleteMessage");
        refPrivatePinChat = FirebaseDatabase.getInstance().getReference("PinChatPrivate");
        refPublicPinChat = FirebaseDatabase.getInstance().getReference("PinChatPublic");
        refClearSign = FirebaseDatabase.getInstance().getReference("ClearSign");
        refDeleteUser = FirebaseDatabase.getInstance().getReference("DeleteUser");
        refDeletePin = FirebaseDatabase.getInstance().getReference("DeletePinChat");
        refEmojiReact = FirebaseDatabase.getInstance().getReference("EmojiReact");
        refOnReadRequest = FirebaseDatabase.getInstance().getReference("OnReadRequest");
        refChatIsRead = FirebaseDatabase.getInstance().getReference("ChatIsRead");

        chatViewModel = new ViewModelProvider.AndroidViewModelFactory(getApplication())
        // initialise room database
                .create(UserChatViewModel.class);

        pinPrivateChatMap = new HashMap<>();
        pinPublicChatMap = new HashMap<>();
        deleteMap = new HashMap<>();
        editMessageMap = new HashMap<>();
        notifyCountMap = new HashMap<>();
        scrollNumMap = new HashMap<>();
        modelListMap = new HashMap<>();
        downMsgCountMap = new HashMap<>();
        insideChatMap = new HashMap<>();
        otherNameList = new ArrayList<>();
        otherUidList = new ArrayList<>();
        chatModelList = new ArrayList<>();
        adapterMap = new HashMap<>();
        recyclerMap = new HashMap<>();
        readDatabase = 0;  // 0 is read, 1 is no_read

        // pins
        pinNextPublic = 1;
        pinNextPrivate = 1;
        pinScrollPrivate = 0;
        pinScrollPublic = 0;

        //  forward
        myHolderNew = new ArrayList<>();
        myHolder_ = new ArrayList<>();
        forwardChatUserId = new ArrayList<>();
        selectedUserNames = new ArrayList<>();
        selectCount = 0;

        // XXX  =================   get intent when file is share from other app    ================
        sharingPhotoActivated = getIntent().getBooleanExtra("isSharing", false);
        photoModelList = (List<MessageModel>) getIntent().getSerializableExtra("photoModel");   // get all the photos model and add it to chatList

        adjustRecyclerViewToPhoneScreen();

        hideKeyboard();

        if(user == null){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            Toast.makeText(this, "User is null (M540) " + user, Toast.LENGTH_SHORT).show();
        }
        else{

            myId = user.getUid();

            lastPositionPreference = this.getSharedPreferences(AllConstants.SCROLLPOSITION, Context.MODE_PRIVATE);

            // store and retrieve my username for ChatList usage
            myUserNamePreferences = this.getSharedPreferences(AllConstants.MYUSERNAME, Context.MODE_PRIVATE);
            getMyUserName = myUserNamePreferences.getString(AllConstants.USERNAME, null);
            if(getMyUserName == null){
                refUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String fetchUserName = snapshot.child(myId).child("userName").getValue().toString();
                        myUserNamePreferences.edit().putString(AllConstants.USERNAME, fetchUserName).apply();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            // store failed chat in local sharePreference
            offlineChat = this.getSharedPreferences(AllConstants.OFFLINECHAT, Context.MODE_PRIVATE);
            // store each photo with their user uid
            photoIdShareRef = getSharedPreferences(AllConstants.PHOTO_OTHERUID, Context.MODE_PRIVATE);
            voiceNoteIdShareRef = getSharedPreferences(AllConstants.VOICENOTE_UID, Context.MODE_PRIVATE);

            // Register the NetworkChangeReceiver to receive network connectivity changes
            networkChangeReceiver = new NetworkChangeReceiver(this);
            registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

            // manually call and check for the network
            internetCheckRunnable = () -> {
                networkChangeReceiver.onReceive(MainActivity.this,
                        new Intent(ConnectivityManager.CONNECTIVITY_ACTION));

                // Repeat the network check everything 3 sce till network is okay
                handlerInternet.postDelayed(internetCheckRunnable, 3000);
            };

            handlerInternet.post(internetCheckRunnable);

            // check internet every 15sec only while typing
            runnableTyping = () -> {
                if(networkListener.equals("yes")) {
                    handlerInternet.post(internetCheckRunnable);
                }
                Toast.makeText(mainActivityContext, "Checking for network", Toast.LENGTH_SHORT).show();;
                handlerTyping.postDelayed(runnableTyping, 15_000);
            };

            ViewPagerMainAdapter adapterV = new ViewPagerMainAdapter(getSupportFragmentManager(), getLifecycle());

            viewPager2General.setAdapter(adapterV);

            TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayoutGeneral, viewPager2General, true, true,
                    (tab, position) -> {

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
                                tab.setText("Tour 🏆");
                                break;
                        }
                    });
            tabLayoutMediator.attach();


            // set my online presence to be true
            refUsers.child(myId).child("presence").setValue(1);

            //  Return back, close msg container
            imageViewBack.setOnClickListener(view -> {
                hideKeyboard();
                insideChat = false;      // back button

                clearEmojiReactSetting();
                onBackPressed();
            });


            //  ==================      side bar menu option     ===========================

            sideBarMenuOpen.setOnClickListener(view -> {    // open the side bar menu option
                sideBarMenuContainer.setVisibility(View.VISIBLE);
            });

            // open menu option via logo too
            imageViewLogo.setOnClickListener(view -> sideBarMenuContainer.setVisibility(View.VISIBLE));

            // close the side bar menu option
            sideBarMenuClose.setOnClickListener(view -> {
                sideBarMenuContainer.setVisibility(View.GONE);
            });
            sideBarMenuContainer.setOnClickListener(view -> {
                sideBarMenuContainer.setVisibility(View.GONE);
            });

            // view my profile photo @sideBar menu
            View.OnClickListener viewImage = view -> {
                Intent i = new Intent(this, ZoomImage.class);
                i.putExtra("otherName", "My Profile Photo");
                i.putExtra("imageLink", imageLink);
                startActivity(i);
            };
            imageViewUserPhoto.setOnClickListener(viewImage);
            tapImage_TV.setOnClickListener(viewImage);

            //logout
            logout.setOnClickListener(view -> logoutOption());

            // settings
            cardViewSettings.setOnClickListener(view -> {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                cardViewSettings.setClickable(false);
                new Handler().postDelayed(() -> {
                    sideBarMenuContainer.setVisibility(View.GONE);
                    cardViewSettings.setClickable(true);
                }, 1000);
            });


            //  ==============  chat box user menu options onClicks---------------------------

            imageViewOpenMenu.setOnClickListener(view -> chatMenuProfile.setVisibility(View.VISIBLE));

            // close user menu
            imageViewCloseMenu.setOnClickListener(view -> chatMenuProfile.setVisibility(View.GONE));
            chatMenuProfile.setOnClickListener(view -> chatMenuProfile.setVisibility(View.GONE));

            //  clear user chats
            clearChat_TV.setOnClickListener(view -> {

                if (adapterMap.get(otherUserUid) != null && adapterMap.get(otherUserUid).getItemCount() > 0){
                    chatMenuProfile.setVisibility(View.GONE);   // hide profile option
                    otherUserName_TV.setText(R.string.clear_history );
                    deleteUserForMe_TV.setText(R.string.clear_for_me);
                    deleteUserForAll_TV.setText(R.string.clear_for_everyone);
                    deleteUserOrClearChatContainer.setVisibility(View.VISIBLE);

                    clearOnlyChatHistory = true;
                    otherUid_Del = otherUserUid;

                } else Toast.makeText(this, "Chat is empty...", Toast.LENGTH_SHORT).show();

            });


            //  ==================    Long Press chat option settings     ===========================

            cancelChatOption_IV.setOnClickListener(view -> {
                view.animate().scaleX(1.3f).scaleY(1.3f).setDuration(50)
                        .withEndAction(() -> {

                            cancelChatOption();

                            // Reset the scale
                            view.setScaleX(1.0f);
                            view.setScaleY(1.0f);

                        }).start();
            });

            // reply button on long press
            replyChatOption_IV.setOnClickListener(view -> {
                view.animate().scaleX(1.3f).scaleY(1.3f).setDuration(50).withEndAction(() ->
                {

                    onEditOrReplyMessage(modelChatsOption, "reply", "replying...",
                            R.drawable.reply,1, chatHolder);

                    // Reset the scale
                    view.setScaleX(1.0f);
                    view.setScaleY(1.0f);

                    pinMsgContainer.setVisibility(View.VISIBLE);
                }).start();

            });

            // edit button on long press
            editChatOption_IV.setOnClickListener(view -> {
                view.animate().scaleX(1.3f).scaleY(1.3f).setDuration(50)
                        .withEndAction(() -> {

                            onEditOrReplyMessage(modelChatsOption,"edit", "editing...",
                                    android.R.drawable.ic_menu_edit, View.GONE, chatHolder);

                            // Reset the scale
                            view.setScaleX(1.0f);
                            view.setScaleY(1.0f);

                            pinMsgContainer.setVisibility(View.VISIBLE);
                        }).start();
            });

            // emoji button on long press
            emojiChatOption_IV.setOnClickListener(view -> {
                view.animate().scaleX(1.3f).scaleY(1.3f).setDuration(50)
                        .withEndAction(() -> {

                            onEmojiReact(chatHolder, modelChatsOption.getIdKey());

                            // Reset the scale
                            view.setScaleX(1.0f);
                            view.setScaleY(1.0f);

                            pinMsgContainer.setVisibility(View.VISIBLE);
                        }).start();

            });

            //  pin or share chat/photo
            pinChatOption_IV.setOnClickListener(view -> {
                view.animate().scaleX(1.3f).scaleY(1.3f).setDuration(50)
                        .withEndAction(() -> {

                            // Get the current Drawable set on the ImageView
                            Drawable currentDrawable = pinChatOption_IV.getDrawable();

                            if (currentDrawable != null && currentDrawable.getConstantState()
                                    .equals(getResources().getDrawable(R.drawable.baseline_share_24, null).getConstantState())) 
                            {
                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("image/*");
                                shareIntent.putExtra(Intent.EXTRA_STREAM, modelChatsOption.getPhotoUriOriginal());
                                shareIntent.putExtra(Intent.EXTRA_TEXT, modelChatsOption.getMessage());

                                startActivity(Intent.createChooser(shareIntent, "WinnerChat"));

                                Toast.makeText(this, "Working on it", Toast.LENGTH_SHORT).show();
                            } else {
                                onPinData(modelChatsOption.getIdKey(), modelChatsOption.getMessage(),
                                        ServerValue.TIMESTAMP, myUserName, chatHolder);
                            }


                            // Reset the scale
                            view.setScaleX(1.0f);
                            view.setScaleY(1.0f);

                            pinMsgContainer.setVisibility(View.VISIBLE);
                        }).start();

            });

            //  forward chat
            forwardChatOption_IV.setOnClickListener(view -> {

                view.animate().scaleX(1.3f).scaleY(1.3f).setDuration(50)
                        .withEndAction(() -> {

                            onForwardChat();

                            // Reset the scale
                            view.setScaleX(1.0f);
                            view.setScaleY(1.0f);

                        }).start();

                // call runnable to check for network
                handlerTyping.post(runnableTyping);

            });

            // delete chat
            deleteChatOption_IV.setOnClickListener(view -> {

                view.animate().scaleX(1.3f).scaleY(1.3f).setDuration(50)
                        .withEndAction(() -> {

                            onDeleteMessage();

                            // Reset the scale
                            view.setScaleX(1.0f);
                            view.setScaleY(1.0f);

                            pinMsgContainer.setVisibility(View.VISIBLE);
                        }).start();

            });

            // copy chat
            copyChatOption_IV.setOnClickListener(view -> {

                view.animate().scaleX(1.3f).scaleY(1.3f).setDuration(50)
                    .withEndAction(() -> {

                        copyChats();

                        // Reset the scale
                        view.setScaleX(1.0f);
                        view.setScaleY(1.0f);

                    }).start();


            });


            //  ================      Documents and camera onClick Settings      ===================

            activityPdfLauncher();

            camera_IV.setOnClickListener(view -> {
                view.animate().scaleX(1.2f).scaleY(1.2f).setDuration(30).withEndAction(()->
                {
                    isSendingFile = true;
                    selectedUserNames.add(otherUserName);
                    forwardChatUserId.add(otherUserUid);
                    Intent i = new Intent(this, SendImageActivity.class);
                    startActivity(i);

                    // call runnable to check for network
                    handlerTyping.post(runnableTyping);

                    new Handler().postDelayed(()-> {
                        view.setScaleX(1f);
                        view.setScaleY(1f);
                    }, 500);
                });
            });

            // set files(documents) -> camera - photo - documents
            file_IV.setOnClickListener(view -> {
                view.animate().scaleX(1.2f).scaleY(1.2f).setDuration(30).withEndAction(()->
                {

                    pickPdfLauncher.launch(ACCEPTED_MIME_TYPES);

                    new Handler().postDelayed(()-> {
                        view.setScaleX(1f);
                        view.setScaleY(1f);
                    }, 500);
                });
            });

            // send message
            sendMessageButton.setOnClickListener(view -> {

                String message = editTextMessage.getText().toString().trim();

                if (!message.isEmpty()) {
                    if (containsOnlyEmojis(message)) {
                        // send as emoji text to increase the size
                        sendMessage(null, message, 8, null, null,
                                null, null, null, otherUserUid);
                    } else {
                        // Send as normal text
                        sendMessage(message, null, 8, null, null,
                                null, null, null, otherUserUid);
                    }
                }

                sendMessageButton.setVisibility(View.INVISIBLE);
                recordButton.setVisibility(View.VISIBLE);
                camera_IV.setVisibility(View.VISIBLE);
                refChecks.child(otherUserUid).child(myId).child("typing").setValue(0);

            });

            gameMe_IV.setOnClickListener(view -> {
                Toast.makeText(this, "Work in progress", Toast.LENGTH_SHORT).show();
            });

            // ==================    react emoji settings    ====================================

            popup = EmojiPopup.Builder.fromRootView( recyclerContainer ).build(editTextMessage);

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
//                            clearAllHighlights();
                            clearHighLight = false;
                            isChatKeyboardON = false;
                            //  close chat keyboard if it's on display
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(editTextMessage.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                            editTextMessage.requestFocus();
                            //  reverse the emoji initialization back to the emoji button icon
                            popup = EmojiPopup.Builder.fromRootView( recyclerContainer).build(editTextMessage);

                        }
                        clearNumb+=1;

                    }
                    isKeyboardVisible = true;

                } else {
                    isKeyboardVisible = false;
                }

            };


            emoji_IV.setOnClickListener(v -> {

                // Perform the button's function    ----------------------------
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
            replyOrEditCancel_IV.setOnClickListener(view -> clearInputFields());


            //  ==================    delete message onClicks     ============================

            deleteChatForOnlyMe_TV.setOnClickListener(view -> {
                int size = chatModelList.size();
                for (int i = 0; i < size; i++){
                    MessageModel chatModel = chatModelList.get(i);

                    // check if photo uri is not null and delete if it exist
                    deleteFileFromPhoneStorage(chatModel); // delete mine

                    // delete from my local list
                    MessageAdapter adapter = adapterMap.get(otherUserUid);
                    adapter.deleteMessage(chatModel.getIdKey());

                    // update user UI chatList model
                    ChatsListFragment.findUserAndDeleteChat(otherUserUid, chatModel.getIdKey());

                    // delete the ROOM outside UI
                    chatViewModel.editOutsideChat( otherUserUid, AllConstants.DELETE_ICON + " ...",
                            null, chatModel.getIdKey() );

                    // delete inside chat from ROOM
                    chatViewModel.deleteChat(chatModel);

//                refMessages.child(myUserName).child(otherUserName).child(idKey).getRef().removeValue();
                    refMsgFast.child(myId).child(otherUserUid).child(chatModel.getIdKey()).getRef().removeValue();
                    refEditMsg.child(myId).child(otherUserUid).child(chatModel.getIdKey()).getRef().removeValue();

                    constraintDelBody.setVisibility(View.GONE);

                    if(i == size-1){
                        cancelChatOption();
                        adapter.notifyDataSetChanged();
                        Toast.makeText(MainActivity.this, "Message deleted for only you.", Toast.LENGTH_SHORT).show();
                    }
                }

            });

            // Delete for others only
            deleteChatForOnlyOther_TV.setOnClickListener(view -> {

                int size = chatModelList.size();
                for (int i = 0; i < size; i++){
                    MessageModel chatModel = chatModelList.get(i);
                    // save to delete database to loop through the other user local list and delete if idkey is found
                    deleteMap.put("idKey", chatModel.getIdKey());
                    refDeleteMsg.child(otherUserUid).child(myId).child(chatModel.getIdKey()).setValue(deleteMap);

                    try{
                        refMsgFast.child(otherUserUid).child(myId).child(chatModel.getIdKey()).getRef().removeValue();
                        refEditMsg.child(otherUserUid).child(myId).child(chatModel.getIdKey()).getRef().removeValue();
                    } catch (Exception e){
                        System.out.println("message key not found to delete for other (M474) " + e.getMessage());
                    }

                    if(i == size-1){
                        cancelChatOption();
                        constraintDelBody.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "Message deleted for "+otherUserName+".", Toast.LENGTH_SHORT).show();
                    }
                }

            });

            // Delete for everyone
            deleteChatsForEveryone_TV.setOnClickListener(view -> {

                int size = chatModelList.size();
                for (int i = 0; i < size; i++) {
                    MessageModel chatModel = chatModelList.get(i);

                    // check if photo uri is not null and delete if it exist
                    deleteFileFromPhoneStorage(chatModel); // delete for every

                    // delete from my local list
                    MessageAdapter adapter = adapterMap.get(otherUserUid);
                    adapter.deleteMessage(chatModel.getIdKey());

                    // save to delete database to loop through the other user local list and delete if idKey is found
                    deleteMap.put("idKey", chatModel.getIdKey());
                    refDeleteMsg.child(otherUserUid).child(myId).child(chatModel.getIdKey()).setValue(deleteMap);


                    try{
                        // delete chat from ROOM - outside UI
                        chatViewModel.editOutsideChat(otherUserUid, AllConstants.DELETE_ICON + "  ...",
                                null, chatModel.getIdKey());

                        // update outside user UI chatList model
                        ChatsListFragment.findUserAndDeleteChat(otherUserUid, chatModel.getIdKey());

                        // delete from ROOM - inside chat room
                        chatViewModel.deleteChat(chatModel);

                        refEditMsg.child(myId).child(otherUserUid).child(chatModel.getIdKey()).getRef().removeValue();
                        refEditMsg.child(otherUserUid).child(myId).child(chatModel.getIdKey()).getRef().removeValue();

                        refMsgFast.child(myId).child(otherUserUid).child(chatModel.getIdKey()).getRef().removeValue();
                        refMsgFast.child(otherUserUid).child(myId).child(chatModel.getIdKey()).getRef().removeValue();

                    } catch (Exception e){
                        System.out.println("message key not found to deleteAll (M490) " + e.getMessage());
                    }

                    if(i == size-1){
                        cancelChatOption();
                        adapter.notifyDataSetChanged();
                        constraintDelBody.setVisibility(View.GONE);
//                        Toast.makeText(MainActivity.this, "Message deleted for everyone.", Toast.LENGTH_SHORT).show();
                    }
                }

            });

            // Close delete message option
            constraintDelBody.setOnClickListener(view -> cancelChatDeleteOption());
            imageViewCancelDel.setOnClickListener(view -> cancelChatDeleteOption());


            // ============ scroll to previous position of reply message
            scrollPositionIV.setOnClickListener(view -> {

                if(goToLastMessage) {

                    if(adapterMap.get(otherUserUid).getItemCount() - goToNum > 3){
                        recyclerMap.get(otherUserUid).scrollToPosition(goToNum + 2);
                    } else {
                        //scroll to last position
                        recyclerMap.get(otherUserUid).scrollToPosition(adapterMap.get(otherUserUid).getItemCount() - 1);
                    }

                    adapterMap.get(otherUserUid).highlightItem(goToNum); // notify Colour
                    MessageAdapter.lastPosition = goToNum;
                    goToLastMessage = false;
                    // remove all highlight in 2 secs
                    new Handler().postDelayed(MainActivity::clearAllHighlights, 2000);

                } else {
                    recyclerMap.get(otherUserUid).scrollToPosition(adapterMap.get(otherUserUid).getItemCount()-1);
                }

            });


            //  ==================    pin message onClicks settings    ============================

            View.OnClickListener closePinBox = view -> {  // personalise later
                pinIconsContainer.setVisibility(View.VISIBLE);
                pinMsgBox_Constr.setVisibility(View.INVISIBLE);
                pinMsgContainer.setClickable(false);    //  allow item on the background clickable

                // hide public pin icons if map is empty
                if(pinPublicChatMap.get(otherUserUid).size() < 1 ){
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
                        refPublicPinChat.child(myId).child(otherUserUid).child(chatNotFoundID).removeValue();
                        if(pinNextPublic > 1)   pinNextPublic-=1;
                    } else {
                        refPrivatePinChat.child(myId).child(otherUserUid).child(chatNotFoundID).removeValue();
                        if(pinNextPrivate > 1)   pinNextPrivate-=1;
                        // remove from local list
                        pinPrivateChatMap.get(otherUserUid).removeIf(pinMessageModel ->
                                pinMessageModel.getMsgId().equals(chatNotFoundID));
                        totalPinPrivate_TV.setText("" + pinPrivateChatMap.get(otherUserUid).size());
                    }

                    chatNotFoundID = "null";    // return back to default null
                }
                pinStatus = "null";

            };
            // hide pin message view
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
                int pinNum = pinPrivateChatMap.get(otherUserUid).size();
                pinCount_TV.setText("(" + pinNextPrivate + "/" + (pinNum) + ")");
                int currentPinNumber = pinNum - pinNextPrivate;
                String getChat;
                if(pinPrivateChatMap.get(otherUserUid).size() != 0){
                    try{
                        getChat = pinPrivateChatMap.get(otherUserUid).get(currentPinNumber).getMessage();
                    } catch (Exception e){

                        getChat = pinPrivateChatMap.get(otherUserUid).get(pinNum-1).getMessage();

                    }
                    pinMsg_TV.setText(getChat);
                } else {
                    pinMsg_TV.setText("No pin message yet!");
                }

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
                int pinNum = pinPublicChatMap.get(otherUserUid).size();
                pinCount_TV.setText("(" + pinNextPublic + "/" + (pinNum) + ")");
                int currentPinNumber = pinNum - pinNextPublic;
                String getChat, getPinBy;
                try{
                    getChat = pinPublicChatMap.get(otherUserUid).get(currentPinNumber).getMessage();
                    getPinBy = pinPublicChatMap.get(otherUserUid).get(currentPinNumber).getPinByWho();
                } catch (Exception e){
                    getChat = pinPublicChatMap.get(otherUserUid).get(pinNum-1).getMessage();
                    getPinBy = pinPublicChatMap.get(otherUserUid).get(pinNum -1).getPinByWho();
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
                cancelChatOption();
            };
            cancelPinOption.setOnClickListener(closePinOption); // close when the cancel pin is click
            pinOptionBox.setOnClickListener(closePinOption);    // close when the background is click

            // pin message for only me -- private
            pinMineTV.setOnClickListener(view -> {
                pinAndUnpinChatPrivately();
                clearAllHighlights();
            });

            // pin message for everyone
            pinEveryoneTV.setOnClickListener(view -> {
                pinAndUnpinChatForEveryone();   // call pin/unpin method
                clearAllHighlights();
            });

            // arrow up, scroll to upper previous pins
            arrowUp.setOnClickListener(view -> {    // go to next upper pin message

                if(pinStatus.equals(PRIVATE)){    // show private pins
                    pinNextPrivate += 1;
                    pinScrollPrivate += 1;
                    int pinNum = pinPrivateChatMap.get(otherUserUid).size();
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
                        pinMsg_TV.setText(pinPrivateChatMap.get(otherUserUid).get(reduceNumber).getMessage());
                    } else{
                        pinNextPrivate -= 1;
                    }

                } else {        // show public pins
                    pinNextPublic += 1;
                    pinScrollPublic += 1;
                    int pinNum = pinPublicChatMap.get(otherUserUid).size();
                    int reduceNumber = pinNum - pinNextPublic;
                    int scrollPosition = pinNum - pinScrollPublic;
                    String getChat, getPinByWho;
                    try{
                        getChat = pinPublicChatMap.get(otherUserUid).get(reduceNumber).getMessage();
                        getPinByWho = pinPublicChatMap.get(otherUserUid).get(reduceNumber).getPinByWho();
                    } catch (Exception e ){
                        getChat = pinPublicChatMap.get(otherUserUid).get(pinNum - 1).getMessage();
                        getPinByWho = pinPublicChatMap.get(otherUserUid).get(pinNum - 1).getPinByWho();
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
                    int pinNum = pinPrivateChatMap.get(otherUserUid).size();
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
                        pinMsg_TV.setText(pinPrivateChatMap.get(otherUserUid).get(increaseNumber).getMessage());
                    } else {
                        pinNextPrivate += 1; // to enable it stop decreasing
                    }

                } else {
                    pinNextPublic -= 1;
                    pinScrollPublic -= 1;
                    int pinNum = pinPublicChatMap.get(otherUserUid).size();
                    int increaseNumber = pinNum - pinNextPublic;
                    int scrollPosition = pinNum - pinScrollPublic;
                    String getChat, getPinByWho;
                    try{
                        getChat = pinPublicChatMap.get(otherUserUid).get(increaseNumber).getMessage();
                        getPinByWho = pinPublicChatMap.get(otherUserUid).get(increaseNumber).getPinByWho();
                    }catch (Exception e){
                        getChat = pinPublicChatMap.get(otherUserUid).get(pinNum - 1).getMessage();
                        getPinByWho = pinPublicChatMap.get(otherUserUid).get(pinNum - 1).getPinByWho();
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


            //  ==================    forward message onClicks    ============================

            cancleForward_IV.setOnClickListener(view -> {
                cancelForwardSettings(this);
            } );

            // send forward message
            circleForwardSend.setOnClickListener(view -> {
                progressBarForward.setVisibility(View.VISIBLE);

                if(MainActivity.sharingPhotoActivated) {
                    sharing = true;
                    // open send_image activity and prepare the photo
                    startActivity(new Intent(this, SendImageActivity.class));
                } else {
                    // send forward or share chat
                    view.animate().scaleX(1.2f).scaleY(1.2f)
                            .setDuration(50).withEndAction(() -> {

                        sendSharedChat(this);

                        // Reset the scale
                        view.setScaleX(1.0f);
                        view.setScaleY(1.0f);

                    }).start();
                }

            });


            //  ===========  delete user from ChatList  And Clear Chat History onClicks =========

            deleteUserOrClearChatContainer.setOnClickListener(view -> cancelUserDeleteOption());
            //  delete user container   -- close the container when click and cancel button click
            cancelUserDelete_IV.setOnClickListener(view -> cancelUserDeleteOption());

            // delete user for only me
            deleteUserForMe_TV.setOnClickListener(view -> {

                if(!clearOnlyChatHistory){  // delete user from chat list
                    refLastDetails.child(myId).child(otherUid_Del).removeValue();
                    refChecks.child(myId).child(otherUid_Del).removeValue();
                    // delete user from adapter list
                    ChatsListFragment.findUserPositionByUID(otherUid_Del);
                    // delete user from ROOM
                    chatViewModel.deleteUserById(otherUid_Del);
                } else {
                    // delete only chats
                    refLastDetails.child(myId).child(otherUid_Del)
                            .child("message").setValue("...");
                    adapterMap.get(otherUid_Del).notifyDataSetChanged();
                    Toast.makeText(this, "Chats cleared for me!", Toast.LENGTH_SHORT).show();
                }

                refPublicPinChat.child(myId).child(otherUid_Del).removeValue();
                refPrivatePinChat.child(myId).child(otherUid_Del).removeValue();

                refMsgFast.child(myId).child(otherUid_Del).removeValue();

                // clear chats from ROOM
                chatViewModel.deleteChatByUserId(otherUid_Del);
                // delete from adapter chat list
                adapterMap.get(otherUid_Del).clearChats();
                cancelUserDeleteOption();
                clearOnlyChatHistory = false;

            });

            // delete user or clear chat for everyone
            deleteUserForAll_TV.setOnClickListener(view -> {

                if(!clearOnlyChatHistory){  // delete user from chat list
                    refLastDetails.child(myId).child(otherUid_Del).removeValue();
                    refChecks.child(myId).child(otherUid_Del).removeValue();

                    refLastDetails.child(otherUid_Del).child(myId).removeValue();
                    refChecks.child(otherUid_Del).child(myId).removeValue();

                    // delete user from adapter list
                    ChatsListFragment.findUserPositionByUID(otherUid_Del);
                    // delete user from ROOM
                    chatViewModel.deleteUserById(otherUid_Del);
                } else {
                    // clear only chat from outside
                    refLastDetails.child(myId).child(otherUid_Del)
                            .child("message").setValue("...");
                    refLastDetails.child(otherUid_Del).child(myId)
                            .child("message").setValue("...");

                    adapterMap.get(otherUid_Del).notifyDataSetChanged();
                    Toast.makeText(this, "Chats cleared for everyone!", Toast.LENGTH_SHORT).show();
                }

                // delete pin message from database
                refPublicPinChat.child(myId).child(otherUid_Del).removeValue();
                refPrivatePinChat.child(myId).child(otherUid_Del).removeValue();
                refPublicPinChat.child(otherUid_Del).child(myId).removeValue();
                refPrivatePinChat.child(otherUid_Del).child(myId).removeValue();

                //  delete chats from  database
                refMsgFast.child(otherUid_Del).child(myId).removeValue();
                refMsgFast.child(myId).child(otherUid_Del).removeValue();

                refClearSign.child(myId).child(otherUid_Del).setValue("clear");
                refDeleteUser.child(myId).child(otherUid_Del).setValue("clear");

                // clear chats from ROOM
                chatViewModel.deleteChatByUserId(otherUid_Del);

                adapterMap.get(otherUid_Del).clearChats(); // delete chats
                cancelUserDeleteOption();
                clearOnlyChatHistory = false;
            });


            setUserDetails();

            fiveSecondsDelay();
            swipeReply();
            deleteUnusedPhotoFromSharePrefsAndAppMemory(this);

            // call the method last so as to give room for all user list to finish load
            if(sharingPhotoActivated){
                onForwardChat();
                chatModelList.addAll(photoModelList);
            }

        }
//        Picasso.get().load("content://media/external/images/media/1000124641").into(imageViewLogo);

//        imageViewLogo.setImageURI(Uri.parse("content://media/external/images/media/1000124641"));
// /external/images/media/1000123946
        //  content://media/external/images/media/1000124641

    }

    //  --------------- methods && interface --------------------


    @Override
    public void callAllMethods(String otherUid, Context context) {
        getMyUserTyping();
        tellUserAmTyping_AddUser();
        getPreviousCounts();

        getChatReadRequest(otherUid);

        pinIconsVisibility(otherUid);

        popup = EmojiPopup.Builder.fromRootView( recyclerContainer).build(editTextMessage);
        // activate the listener
        emoji_IV.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);

        insideChatMap.put(otherUid, true);

        fileNamePath = getRecordFilePath(context);     // regenerate filePathName

        if(networkOk)  // reload failed chat if network is okay
            new Handler().postDelayed(() -> reloadFailedMessagesWhenNetworkIsOk(), 500);

        // initialize voice note
        voiceNote(context);
    }

//    @Override
//    public void onRequestPermission(MessageAdapter.MessageViewHolder holder, MessageModel modelChats) {
//        if(!permissions.isStorageOk(this)){
//            Picasso.get().load(modelChats.getPhotoUriPath()).into(holder.showImage);
//            Toast.makeText(mContext, "Found " + modelChats.getPhotoUriPath(), Toast.LENGTH_SHORT).show();
//        } else {
//            // Request the permission again
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                    AllConstants.STORAGE_REQUEST_CODE);
////            permissions.requestStorage(this);
//            Toast.makeText(this, "need to request here", Toast.LENGTH_SHORT).show();
//        }
//
//    }

    @Override
    public void firstCallLoadPage(String otherUid) {
//        constraintMsgBody.setVisibility(View.INVISIBLE);
//        conTopUserDetails.setVisibility(View.INVISIBLE);
        recyclerViewChatVisibility(otherUid);

    }

    @Override
    public void chatBodyVisibility(String otherName, String imageUrl, String userName, String uID, Context mContext_,
                                  RecyclerView recyclerChat) {

        editTextMessage.setFocusableInTouchMode(true);
        editTextMessage.requestFocus();

        // make only the active recyclerView to be visible
        recyclerViewChatVisibility(uID);

//        handlerLoadViewLayout.removeCallbacks(loadViewRunnable);

        if(adapterMap.get(uID) != null){  // reload message if empty later
            // check recycler position before scrolling
            int scrollNumCheck = scrollNumMap.get(uID) == null ? adapterMap.get(uID).getItemCount() - 1
                    : (int) scrollNumMap.get(uID) ;
            int scrollCheck = adapterMap.get(uID).getItemCount() - scrollNumCheck;

            if(scrollCheck < 5){   // scroll to last postion
                recyclerMap.get(uID).scrollToPosition(adapterMap.get(uID).getItemCount() - 1);
            } else {
                scrollToPreviousPosition(uID, scrollNumCheck);
            }

            System.out.println("Total adapter (M1070) " + adapterMap.get(uID).getItemCount());
        }

        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerMap.get(uID).getLayoutManager();
        assert layoutManager != null;
        downMsgCount = (layoutManager.getItemCount() - 1) - layoutManager.findLastVisibleItemPosition();
        String numb = ""+ downMsgCount;
        scrollCountTV.setText(numb);           // set down msg count
        downMsgCountMap.put(uID, downMsgCount);     // set it for "sending message method"

        // Get the position of the item for sendMessage() and retrieveMessage()
        scrollNum = layoutManager.findLastVisibleItemPosition();
        scrollNumMap.put(uID, scrollNum);

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


scNum = 20;
//         Add an OnScrollListener to the RecyclerView
        recyclerMap.get(uID).addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // keep saving the recycler position while scrolling
                scrollNum = layoutManager.findLastVisibleItemPosition();
                scrollNumMap.put(uID, scrollNum);


//                if(scNum > 1){
//                    scNum-=1;
//                    MessageModel model = modelListMap.get(otherName).get(scNum);
//                    adapterMap.get(otherName).addNewMessageDB(model);
//                    adapterMap.get(otherName).notifyItemInserted(scNum);
//                    System.out.println("what is getNumber " + scNum);
//                }

                // keep updating the number of down messages
                downMsgCount = (layoutManager.getItemCount() - 1) - layoutManager.findLastVisibleItemPosition();
                scrollCountTV.setText(""+downMsgCount);

                //  store the downMsgCount in a map for each user, to enable me
                //  add to the number on sendMessage() when I send new message
                downMsgCountMap.put(uID, downMsgCount);

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

        if(recyclerMap.get(uID) != null){
            // Attach the ItemTouchHelper to your RecyclerView
            itemTouchSwipe.attachToRecyclerView(recyclerMap.get(uID));
        }
    }

//    private boolean isLastPage() {
//        // Determine if you have loaded all messages based on your data source
//        return adapterMap.get(otherUserName).getItemCount() >= modelListMap.get(otherUserName).size();
//    }
//
//    private void onLoadMore(String otherName) {
//        // Load older messages from your data source
//        if(scNum > 1){
//            scNum-=1;
//            MessageModel model = modelListMap.get(otherName).get(scNum);
//            adapterMap.get(otherName).addNewMessageDB(model);
//            adapterMap.get(otherName).notifyItemInserted(scNum);
//            System.out.println("what is getNumber " + scNum);
//        }
//
//    }


    @Override       // run only once    -- also store otherName and otherUid on List
    public void sendRecyclerView(RecyclerView recyclerChat, String otherUid) {
        // first check if recyclerView already exist and skip to the next
        if(recyclerMap != null){
            if (!recyclerMap.containsKey(otherUid)) {
                recyclerMap.put(otherUid, recyclerChat);  // save empty recyclerView of each user to their username

                if (recyclerChat.getParent() != null) {
                    // Remove the clicked RecyclerView from its current parent
                    ((ViewGroup) recyclerChat.getParent()).removeView(recyclerChat);
                }
                recyclerContainer.addView(recyclerChat);

                recyclerViewChatVisibility(otherUid);  // set to INVISIBLE since it's "GONE" on XML
            }
//            Toast.makeText(this, "Recycler is not null", Toast.LENGTH_SHORT).show();

        } else {
//            Toast.makeText(this, "Recycler is null", Toast.LENGTH_SHORT).show();
        }


        // store otherUserUid to loop and change to delivery status -- of outsider chat
        if (!otherUidList.contains(otherUid)) {
            otherUidList.add(otherUid);
        }

        // turn off inside chat off to prevent it from scrolling when I'm not inside
        if(!insideChatMap.containsKey(otherUid)){
            insideChatMap.put(otherUid, false);
        }
    }

    @Override       // run only once
    public void getMessage(String userName, String otherUID, Context mContext_){

        constrNetConnect.setVisibility(View.VISIBLE);

        // store myUserName for looping through load message status and change to delivery status
        myUserName = userName;

        mContext = mContext_;


        // delay for 3 secs to allow user chatlist to load users
        new CountDownTimer(3000, 1000){
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {

                retrieveMessages(userName, otherUID, mContext_);

                // get pinMessage once
                getPinChats(otherUID);
                getDeletePinId(otherUID);
                getEmojiReact(otherUID);
                getReadChatResponse(otherUID);

//                constrNetConnect.setVisibility(View.GONE);

            }
        }.start();

    }

    @Override
    public void onDeleteMessage() {

        clearEmojiReactSetting();
        hideKeyboard();

        // user1 should not be unable to delete user2 msg
        for (MessageModel model : chatModelList){
            if(!model.getFromUid().equals(myId)){
                deleteChatForOnlyOther_TV.setVisibility(View.GONE);
            }
        }

        constraintDelBody.setVisibility(View.VISIBLE);

        // hide chat option
        chatOptionsConstraints.setVisibility(View.GONE);

    }

    @Override
    public void onEditOrReplyMessage(MessageModel messageModel, String editOrReply,
                                     String status, int icon, int visible, MessageAdapter.MessageViewHolder holder)
    {
        clearEmojiReactSetting();
        isChatKeyboardON = true;

        // pop up keyboard
        editTextMessage.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(editTextMessage, InputMethodManager.SHOW_IMPLICIT);

        String vnDuration = messageModel.getVnDuration() != null ? messageModel.getVnDuration() : "";

        String micIconAndDuration_OrChat;
        if(messageModel.getMessage() == null){
            if(messageModel.getEmojiOnly() != null){
                micIconAndDuration_OrChat = messageModel.getEmojiOnly();
            } else
                micIconAndDuration_OrChat = "\uD83C\uDFA4    " +  vnDuration;
        } else {
            micIconAndDuration_OrChat = messageModel.getMessage();
//            Toast.makeText(this, "It not null", Toast.LENGTH_SHORT).show();

        }

        // General settings
        textViewReplyOrEdit.setText(micIconAndDuration_OrChat);
        editOrReplyIV.setImageResource(icon);               //  show edit or reply icon at left view
        cardViewReplyOrEdit.setVisibility(View.VISIBLE);    // make the container of the text visible
        replyVisible.setVisibility(View.VISIBLE);
        replyVisible.setText(status);                       // indicating its on edit or reply mood
        nameReply.setVisibility(View.VISIBLE);
        listener = editOrReply;
        replyFrom =null;
        replyHolder = holder;

        // this id will enable user to click a reply msg and scroll there
        // Edit -- it will replace the message with the id
        idKey = messageModel.getIdKey();
        this.messageModel = messageModel;

        // hide chat option
        chatOptionsConstraints.setVisibility(View.GONE);

        // edit settings
        if(editOrReply.equals("edit")){
            nameReply.setText("");
            editTextMessage.setText(micIconAndDuration_OrChat);  // set the edit message on the text field
            editTextMessage.setSelection(micIconAndDuration_OrChat.length()); // Set focus to the end of the text
        }

        // reply setting
        if(editOrReply == "reply"){

            replyVisibility = visible;      // send visible to database to make the replied msg Visible on the UI

            replyText = micIconAndDuration_OrChat;
            replyFrom = "From: " + messageModel.getFrom();

//            if (fromWho.equals(myUserName)) {   // change fromWho from display name to username later
//                replyFrom = "From You.";
//                nameReply.setText(replyFrom);
//            }
//            else {
//                // edit later to username and display name
//                replyFrom = fromWho ;
//                nameReply.setText(replyFrom);
//            }
        }

    }

    @Override
    public void onForwardChat() {
        setForwardChat();
    }
    public void setForwardChat(){

        // return back to the first position
        if(viewPager2General.getCurrentItem() != 0) {
            viewPager2General.setCurrentItem(0, true);
        }
        // Disable user from swiping
        viewPager2General.setUserInputEnabled(false);

        clearEmojiReactSetting();
        hideKeyboard();

        pinMsgContainer.setVisibility(View.GONE);
        conTopUserDetails.setVisibility(View.INVISIBLE);
        constraintMsgBody.setVisibility(View.INVISIBLE);

        tabLayoutGeneral.setVisibility(View.GONE);
        forwardTopContainer.setVisibility(View.VISIBLE);
        forwardDownContainer.setVisibility(View.VISIBLE);
        forwardTopContainer.setClickable(true);
        forwardDownContainer.setClickable(true);

        chatOptionsConstraints.setVisibility(View.GONE);
        progressBarForward.setVisibility(View.GONE);

        onForward = true;

        myHolderNew.addAll(myHolder_);  // add all users holder that was gotten from chatListAdapter

        // call forward setting method to add onForward checkBox container
        ChatListAdapter.getInstance().forwardCheckBoxVisibility(myHolderNew);

        if(sharingPhotoActivated){
            circleForwardSend.setImageResource(R.drawable.baseline_arrow_forward_24);
        } else {
            circleForwardSend.setImageResource(R.drawable.baseline_send_24);
        }

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

        // hide chat option
        chatOptionsConstraints.setVisibility(View.GONE);

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

        // hide chat option
        chatOptionsConstraints.setVisibility(View.GONE);

    }

    @Override
    public void msgBackgroundActivities(String otherUid) {

        AllConstants.executors.execute(() -> {

            // set my status to be true in case I receive msg, it will be tick as seen
            Map <String, Object> statusAndMSgCount = new HashMap<>();
            statusAndMSgCount.put("status", true);
            statusAndMSgCount.put("unreadMsg", 0);

            this.refChecks.child(this.myId).child(otherUid).updateChildren(statusAndMSgCount);

            // set responds to pend always      ------- will change later to check condition if user is still an active call
            this.refChecks.child(this.myId).child(otherUid).child("vCallResp").setValue("pending");

        });

        insideChat = true;     // opening chat
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
                            String online = getString(R.string.online);
                            textViewLastSeen.setText(online);
                        }
                        else {
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
                                        textViewLastSeen.setText("Seen: Yesterday, "+time.toLowerCase()+".");
                                    } else if (curDay - lastDay == 2) {
                                        textViewLastSeen.setText("Seen: 2days ago, "+time.toLowerCase()+".");
                                    } else if (curDay - lastDay == 3) {
                                        textViewLastSeen.setText("Seen: 3days ago, "+time.toLowerCase()+".");
                                    } else if (curDay - lastDay == 4) {
                                        textViewLastSeen.setText("Seen: 4days ago, "+time.toLowerCase()+".");
                                    } else if (curDay - lastDay == 5) {
                                        textViewLastSeen.setText("Seen: 5days ago, "+time.toLowerCase()+".");
                                    } else if (curDay - lastDay == 6) {
                                        textViewLastSeen.setText("Seen: 6days ago, "+time.toLowerCase()+".");
                                    }
                                } else if (dateCur - dateLast >= 7 && dateCur - dateLast < 14) {
                                    textViewLastSeen.setText("Seen: Last week "+lastDayString+".");
                                } else if (dateCur - dateLast >= 14 && dateCur - dateLast < 21) {
                                    textViewLastSeen.setText("Seen: Last 2 weeks "+lastDayString+".");
                                } else if (dateCur - dateLast >= 21 && dateCur - dateLast < 27) {
                                    textViewLastSeen.setText("Seen: Last 3 weeks "+lastDayString+".");
                                } else {
                                    textViewLastSeen.setText("Seen: a month ago");
                                }
                            } else if(curMonth - lastMonth == 1){
                                textViewLastSeen.setText("Seen: one month ago.");
                            } else if(curMonth - lastMonth == 2){
                                textViewLastSeen.setText("Seen: two months ago.");
                            }else if(curMonth - lastMonth == 3){
                                textViewLastSeen.setText("Seen: three months ago.");
                            }else if(curMonth - lastMonth == 4){
                                textViewLastSeen.setText("Seen: Four months ago.");
                            }else if(curMonth - lastMonth == 5){
                                textViewLastSeen.setText("Seen: Five months ago.");
                            } else {
                                textViewLastSeen.setText("Seen: "+dateLast +"/"+ lastMonth+"/"+ lastYear);
                            }

                        }
                    } catch (Exception e){
                        String appName = getString(R.string.app_name);
                        textViewLastSeen.setText(appName);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        }catch (Exception e){
            textViewLastSeen.setText(getString(R.string.app_name));

        }

        refUsers.child(otherUid).keepSynced(true);
    }

    // This method will be called by NetworkChangeReceiver whenever network status changes   // reload message loadStatus
    @Override
    public void onNetworkStatusChanged(boolean isConnected) {
        if (isConnected) {
            // Network is connected, perform actions accordingly
            constrNetConnect.setVisibility(View.INVISIBLE);
            networkListener = "yes";
            chatDeliveryStatus = 700024;
            networkOk = true;
            // remove runnable when network is okay to prevent continuous data usage
            handlerInternet.removeCallbacks(internetCheckRunnable);

            reloadFailedMessagesWhenNetworkIsOk();  // reload message loadStatus

        } else {
            // Network is disconnected, handle this case as well
            constrNetConnect.setVisibility(View.VISIBLE);
            networkListener = "no";
            chatDeliveryStatus = 700033;
//            Toast.makeText(this, "Network is not okay now...!", Toast.LENGTH_SHORT).show();

            if(networkOk){
                handlerInternet.post(internetCheckRunnable);   // call runnable when no internet
                networkOk = false;
            }

        }
    }

    @Override
    public void onUserDelete(String otherName, String otherUid) {
        String name = "@"+otherName;
        otherUserName_TV.setText(name);
        deleteUserForMe_TV.setText(R.string.delete_for_me);
        deleteUserForAll_TV.setText(R.string.delete_for_everyone);
        deleteUserOrClearChatContainer.setVisibility(View.VISIBLE);
        otherUid_Del = otherUid;
    }


    //  ------------    methods     ---------------

    // Check if a string message contains only emojis
    private boolean containsOnlyEmojis(String text) {
        for (int i = 0; i < text.length(); i++) {
            int type = Character.getType(text.charAt(i));
            if (type != Character.SURROGATE || text.length() > 20) {
                return false;  // Found non-emoji character
            }
        }
        return true;
    }

    // create forward message map for each loop
    private Map<String, Object> forwardChatsMap(MessageModel chatModel, String chatId){
        Map<String, Object> forwardMessageMap = new HashMap<>();

        forwardMessageMap.put("from", myUserName);
        forwardMessageMap.put("fromUid", myId);
        forwardMessageMap.put("type", chatModel.getType());
        forwardMessageMap.put("idKey", chatId);
        forwardMessageMap.put("message", chatModel.getMessage());
        forwardMessageMap.put("emojiOnly", chatModel.getEmojiOnly());
        forwardMessageMap.put("voiceNote", chatModel.getVoiceNote());
        forwardMessageMap.put("msgStatus", 700024);
        forwardMessageMap.put( "timeSent", ServerValue.TIMESTAMP);
        forwardMessageMap.put("visibility", 8);
        forwardMessageMap.put("isChatPin", false);
        forwardMessageMap.put("photoUriPath", chatModel.getPhotoUriPath());
        if(chatModel.getFromUid().equals(myId)){
            forwardMessageMap.put("isChatForward", false);
        } else {
            forwardMessageMap.put("isChatForward", true);
        }

        return forwardMessageMap;
    }

    public static Map<String, Object> setMessageMap(
            String text, String emojiOnly, int type, String chatKey,
            int msgStatus, String vnPath, String vnDuration /*String photoPath*/
    ){
        // 700024 --- tick one msg  // 700016 -- seen msg   // 700033 -- load

        Map<String, Object> messageMap = new HashMap<>();

        messageMap.put("from", myUserName);
        messageMap.put("fromUid", myId);
        messageMap.put("type", type);            // 8 is for text while 1 is for voice note
        messageMap.put("idKey", chatKey);
        messageMap.put("message", text);
        messageMap.put("emojiOnly", emojiOnly);
        messageMap.put("voiceNote", vnPath);
        messageMap.put("vnDuration", vnDuration);
        messageMap.put("msgStatus", msgStatus);
        messageMap.put( "timeSent", ServerValue.TIMESTAMP);
        messageMap.put("replyFrom", replyFrom);
        messageMap.put("visibility", replyVisibility);
        messageMap.put("replyID", idKey);
        messageMap.put("replyMsg", replyText);
        messageMap.put("isChatPin", false);
        messageMap.put("isChatForward", false);
//        messageMap.put("photoUriPath", photoPath);

        return messageMap;
    }

    public void sendMessage(String text, String emojiOnly, int type, String vnPath_, String durationOrSizeVN,
                            String lowImageUri, String originalImageUri, String imageSize, String otherId)
    {

        if(listener.equals("edit")){ // check if it's on edit mode
            sendEditChat(text, emojiOnly);
        } else {

            String chatKey = refMsgFast.child(myId).child(otherUserUid).push().getKey();  // create an id for each message

            // check if duration is more than 1mb, then split and send only duration to my model local chat
            String durationVN = durationOrSizeVN;
            if(durationVN != null){
                if(durationOrSizeVN.contains("~")) {
                    String[] splitDur = durationOrSizeVN.split("~");
                    durationVN = splitDur[1];
                }
            }
            // save otherId to a photo key or VN key, so it doesn't send to another user
            int delivery = chatDeliveryStatus;
            if(isSendingVoiceNote) {
                voiceNoteIdShareRef.edit().putString(chatKey, otherId + AllConstants.JOIN + "yes").apply();
                delivery = 700033;
            }

            // save to local list for fast update
            MessageModel messageModel = new MessageModel(text, myUserName, myId, replyFrom,
                    System.currentTimeMillis(), chatKey, null, replyVisibility,
                    replyText, delivery, type, imageSize, idKey, false, false,
                    null, emojiOnly, vnPath_, durationVN, lowImageUri, originalImageUri);

            // add chat to local list
            MessageAdapter adapter = adapterMap.get(otherId);
            if(adapter != null){
                adapter.addNewMessageDB(messageModel);  // add new chat
                // scroll to new position only if scrollCheck int is < 20
                int scrollCheck = adapter.getItemCount() - (int) scrollNumMap.get(otherId);
                int lastPosition = adapterMap.get(otherId).getItemCount()-1;

                if(scrollCheck < 6){    // scroll to last position on new message update.
                    recyclerMap.get(otherId).scrollToPosition(lastPosition);
                }   // else don't scroll.
            }

            // add one to the down message number
            int increaseScroll = (int) downMsgCountMap.get(otherId) + 1;
            scrollCountTV.setText(""+increaseScroll);
            downMsgCountMap.put(otherId, increaseScroll); // save new position

            // show indicator that msg is sent
            if(scrollPositionIV.getVisibility() == View.VISIBLE){
                sendIndicator.setVisibility(View.VISIBLE);
                receiveIndicator.setVisibility(View.GONE);
            }

            // save the chatKey to network and reload when network is okay
            if(chatDeliveryStatus == 700033 && !isSendingFile && !isSendingVoiceNote){
                offlineChat.edit().putString(chatKey, otherId).apply();
            }

            // find position and move it to top as recent chat... also update the outside chat details
            ChatListAdapter.getInstance().findUserPositionByUID(otherId, messageModel, chatKey);

            // remove the typingRunnable for checking network
            handlerTyping.removeCallbacks(runnableTyping);
            networkTypingOk = true;

            // send only text chat to other user -- not photo nor file yet
            if(!isSendingVoiceNote){
                sendToDataBase(text, emojiOnly, type, chatDeliveryStatus,
                        vnPath_, durationOrSizeVN, chatKey, otherId);
            }

            // save to local ROOM database
            chatViewModel.insertChat(otherId, messageModel);

            // Clear the input field and reset other variables
            clearInputFields();
//            AllConstants.executors.execute(() -> { });

        }

    }

    public void sendToDataBase(
            String text, String emojiOnly, int type, int chatDeliveryStatus,
            String vnPath_, String durationOrSizeVN, String chatKey, String otherUid
    ){
        // send the chat to other user
        refMsgFast.child(otherUid).child(myId).child(chatKey).setValue(
                setMessageMap(text, emojiOnly, type, chatKey,
                        chatDeliveryStatus, vnPath_, durationOrSizeVN)
//                                .put("photoUriPath", null)
        );

        // save last msg for outside chat display
        refLastDetails.child(myId).child(otherUid).setValue(
                setMessageMap(text, emojiOnly, type, chatKey,
                        chatDeliveryStatus, vnPath_, durationOrSizeVN)
        );
        refLastDetails.child(otherUid).child(myId).setValue(
                setMessageMap(text, emojiOnly, type, chatKey, 0, vnPath_, durationOrSizeVN)
        );

        //  send chatKey to other User to read  -- customise later to check user OnRead settings
        refOnReadRequest.child(otherUid).child(myId).push().setValue(chatKey);

        checkAndSaveCounts_SendMsg(otherUid);   // save the number of new message I am sending
    }

    public void sendSharedChat(Context context){
        // loop through each user
        int userSize = forwardChatUserId.size();
        for (int i = 0; i < userSize; i++ ) {
            String otherUid = forwardChatUserId.get(i);

            // loop through each selected chat
            int chatSize = chatModelList.size();
            for (int j = 0; j < chatSize; j++){
                MessageModel chatModel = chatModelList.get(j);

                // get the local list variables
                int forwardType = chatModel.getType();
                String forwardChat = chatModel.getMessage();
                String forwardChatEmojiOnly = chatModel.getEmojiOnly();
                String vnPathFile = chatModel.getVoiceNote();
                String vnDuration_ = chatModel.getVnDuration();
                String forwardPhotoPath = chatModel.getPhotoUriPath();
                String photoOriginal = chatModel.getPhotoUriOriginal();
                String imageSize_ = chatModel.getImageSize();
                boolean forwardIcon;
                if(chatModel.getFromUid() != null){
                    forwardIcon = chatModel.getFromUid().equals(myId) ? false : true;
                } else forwardIcon = false;

                String chatId = refMsgFast.child(myId).child(otherUid).push().getKey();  // create an id for each message

                int delivery = chatDeliveryStatus;
                // save otherId to a photo key, so it doesn't send to another user
                if(forwardPhotoPath != null){
                    photoIdShareRef.edit().putString(chatId, otherUid + AllConstants.JOIN + "yes").apply();
                    delivery = 700033;
                    if(imageSize_ == null)
                        imageSize_ = getFileSize(Uri.parse(photoOriginal), this);
                }

                // save to local list for fast update
                MessageModel messageModel = new MessageModel(forwardChat, myUserName, myId, "",
                        System.currentTimeMillis(), chatId, null, 8, "", delivery,
                        forwardType, imageSize_, "", false, forwardIcon, null,
                        forwardChatEmojiOnly, vnPathFile, vnDuration_, forwardPhotoPath, photoOriginal);

                MessageAdapter adapter = adapterMap.get(otherUid);
                adapter.addNewMessageDB(messageModel);

                // scroll to new position only if scrollCheck int is < 20
                int scrollCheck;
                try{
                    scrollCheck = adapter.getItemCount() - (int) scrollNumMap.get(otherUid);
                } catch (Exception e) {
                    scrollCheck = adapter.getItemCount();
                }
                int lastPosition = adapterMap.get(otherUid).getItemCount()-1;
                if(scrollCheck < 20){    // scroll to last position on new message update.
                    recyclerMap.get(otherUid).scrollToPosition(lastPosition);
                }   // else don't scroll.

                // save the chatKey to network and reload when network is okay
                if(chatDeliveryStatus == 700033){
                    offlineChat.edit().putString(chatId, otherUid).apply();
                }

                // save to ROOM database
                chatViewModel.insertChat(otherUid, messageModel);

                // send to database only when there's no photo and VN attach, adapter will do the sending for photo
                if(forwardPhotoPath == null && vnPathFile == null){
                    refMsgFast.child(otherUid).child(myId).child(chatId).setValue( forwardChatsMap(chatModel, chatId) );
                    // save last msg for outside chat display
                    refLastDetails.child(myId).child(otherUid).setValue( forwardChatsMap(chatModel, chatId) );
                    refLastDetails.child(otherUid).child(myId).setValue( forwardChatsMap(chatModel, chatId) );
                    checkAndSaveCounts_SendMsg(otherUid);
                }

                // find position and move it to top as recent chat... also update the outside chat and ROOM DB
                ChatListAdapter.getInstance().findUserPositionByUID(otherUid, messageModel, chatId);

                // cancel when all is done
                if(i == userSize-1 && j == chatSize-1 ){
                    cancelForwardSettings(context);
                    Toast.makeText(context, "Done!", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    private void sendEditChat(String text, String emojiOnly){
        editMessageMap.put("from", myUserName);
        editMessageMap.put("fromUid", myId);
        editMessageMap.put("message", text);
        editMessageMap.put("emojiOnly", emojiOnly);
        editMessageMap.put("edit", "edited");
        editMessageMap.put( "timeSent", ServerValue.TIMESTAMP);

        // save to edit message to update other user chat
        refEditMsg.child(otherUserUid).child(myId).child(idKey).setValue(editMessageMap);

        messageModel.setMessage(text);
        messageModel.setEmojiOnly(emojiOnly);
        messageModel.setTimeSent(System.currentTimeMillis());
        messageModel.setEdit("edited");

        //  send the messageModel to the adapter to update it
        adapterMap.get(otherUserUid).updateMessage(messageModel, replyHolder);

        //  save to ROOM database
        chatViewModel.updateChat(messageModel);

        // update user chatList model if it same last chat
        ChatsListFragment.findUserAndEditChat(otherUserUid, idKey,
                text, emojiOnly);

        // update the ROOM outside UI
        chatViewModel.editOutsideChat(otherUserUid, AllConstants.EDIT_ICON + text,
                emojiOnly, idKey);

        // remove the typingRunnable for checking network
        handlerTyping.removeCallbacks(runnableTyping);
        networkTypingOk = true;

        // hide chat Option menu if visible
        chatOptionsConstraints.setVisibility(View.GONE);

    }

    // record voiceNote
    private void voiceNote (Context context){

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

                if(permissions.isRecordingOk(context)){
                    isSendingVoiceNote = true;
                    //Start Recording..
                    setUpRecording(fileNamePath);

                    try {
                        mediaPlayer = new MediaPlayer();

                        startTimeMillis = SystemClock.elapsedRealtime();
                        mediaRecorder.prepare();

                        new Handler().postDelayed(()->{
                            try{
                                mediaRecorder.start();
                            } catch (Exception e){
                                System.out.println("Error in recorder " + e.getMessage());
                            }
                        }, 100);    // delay 100m to avoid capturing the starting pop sound
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    editTextMessage.setVisibility(View.INVISIBLE);   //  hide msg edit text
                    emoji_IV.setVisibility(View.INVISIBLE);
                    camera_IV.setVisibility(View.INVISIBLE);
                    gameMe_IV.setVisibility(View.INVISIBLE);
                    file_IV.setVisibility(View.INVISIBLE);
                    recordView.setVisibility(View.VISIBLE);     // show swipe mode

                    // call runnable to check for network
                    handlerTyping.post(runnableTyping);

                } else{
                    permissions.requestRecording((Activity) context);
                    onCancel();
                }

            }

            @Override
            public void onCancel() {
                //On Swipe To Cancel
                try{
                    mediaRecorder.reset();
                    mediaRecorder.release();
                    File file = new File(fileNamePath);
                    if (file.exists())
                        file.delete();
                } catch (Exception e ){
                    System.out.println("Error occur " + e.getMessage());
                }

                emoji_IV.setVisibility(View.VISIBLE);
                camera_IV.setVisibility(View.VISIBLE);
                gameMe_IV.setVisibility(View.VISIBLE);
                file_IV.setVisibility(View.VISIBLE);
                editTextMessage.setVisibility(View.VISIBLE);    //  hide msg edit text
                recordView.setVisibility(View.GONE);            // show swipe mode

                fileNamePath = getRecordFilePath(context);     // regenerate file path
                isSendingVoiceNote = false; // deactivate voice note boolean not to block sending text
                // remove runnable to check for network
                handlerTyping.removeCallbacks(runnableTyping);
            }

            @Override
            public void onFinish(long recordTime, boolean limitReached) {

                //Stop Recording..
                try {
                    mediaRecorder.stop();
                    mediaRecorder.release();
                    // initialise the musicPlayer with the new record file so as to get the total duration
                    mediaPlayer.setDataSource(fileNamePath);
                    mediaPlayer.prepare();

                    // Calculate the estimated file size
                    long durationMillis = SystemClock.elapsedRealtime() - startTimeMillis;
                    long fileSizeBytes = ( (audioBitrate / 8) * durationMillis / 1000 ) / 10; // Convert duration to seconds

                    // Convert file size to kilobytes (KB) and megabytes (MB)
                    int fileSizeKB = (int) fileSizeBytes / 1024; // Size in kilobytes
                    int fileSizeMB = fileSizeKB / 1024; // Size in megabytes

                    String formattedDuration = formatDuration(mediaPlayer.getDuration());
                    String sizeOrDuration = fileSizeKB < 1000.0 ? formattedDuration : Math.round(fileSizeMB) + " MB ~ " + formattedDuration;

                    mediaPlayer.release();  // Release the MediaPlayer resources after getting the duration

                    // send voice note to local list and room only -- adapter will do the sending to other user
                    sendMessage(null, null, 1, fileNamePath, sizeOrDuration,
                            null, null, null, otherUserUid);

                    isSendingVoiceNote = false; // deactivate voice note boolean not to block sending text

                } catch (Exception e) {
                    e.printStackTrace();
                }

                emoji_IV.setVisibility(View.VISIBLE);
                camera_IV.setVisibility(View.VISIBLE);
                gameMe_IV.setVisibility(View.VISIBLE);
                file_IV.setVisibility(View.VISIBLE);
                editTextMessage.setVisibility(View.VISIBLE);    //  hide msg edit text
                recordView.setVisibility(View.GONE);            // show swipe mode

                fileNamePath = getRecordFilePath(context);     //  regenerate audio path and System.time

                // request back the edittext focus
                editTextMessage.requestFocus();
            }

            @Override
            public void onLessThanSecond() {

                try{
                    mediaRecorder.reset();
                    mediaRecorder.release();

                    //When the record time is less than One Second, delete it
                    File file = new File(fileNamePath);

                    if (file.exists())
                        file.delete();

                } catch (Exception e){
                    System.out.println("Error occur M1404 " + e.getMessage());
                }

                emoji_IV.setVisibility(View.VISIBLE);
                camera_IV.setVisibility(View.VISIBLE);
                gameMe_IV.setVisibility(View.VISIBLE);
                file_IV.setVisibility(View.VISIBLE);
                editTextMessage.setVisibility(View.VISIBLE);    //  hide msg edit text
                recordView.setVisibility(View.GONE);            // show swipe mode
                isSendingVoiceNote = false; // deactivate voice note boolean not to block sending text
                // remove runnable to check for network
                handlerTyping.removeCallbacks(runnableTyping);
            }

            @Override
            public void onLock() {

            }
        });

    }

    private void setUpRecording(String fileName) {

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        mediaRecorder.setAudioEncodingBitRate(audioBitrate);

//        mediaRecorder.setOutputFile(getRecordFilePath());
        mediaRecorder.setOutputFile(fileName);

        // bug fix by getRecordFilePath()
//        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/WinnerChat/Media/Recording");
//        if (!file.exists())
//            file.mkdirs();
//        audioPath = file.getAbsolutePath() + File.separator + System.currentTimeMillis() + ".3gp";
//        mediaRecorder.setOutputFile(audioPath);
    }

    public static String getRecordFilePath(Context mContext){
//        ContextWrapper contextWrapper = new ContextWrapper(mContext);
        File audioDir = mContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(audioDir, "voice_note_" + System.currentTimeMillis() + ".3gp");

        return file.getPath();
    }

    public static File getPhotoFolder(Context context){
        File appSpecificFolder = new File(context.getExternalFilesDir(null), AllConstants.ALL_PHOTOS);
        if (!appSpecificFolder.exists()) {
            appSpecificFolder.mkdirs();
        }
        return appSpecificFolder;
    }

    // Function to format duration in milliseconds to "mm:ss" format
    public static String formatDuration(int durationInMillis) {
        int seconds = durationInMillis / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    // Function to format duration to "mm:ss" to milliseconds format
    public static int parseDuration(String durationString) {
        try {
            String[] parts = durationString.split(":");
            int minutes = Integer.parseInt(parts[0]);
            int seconds = Integer.parseInt(parts[1]);

            return (minutes * 60 + seconds) * 1000;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace(); // Handle parsing errors if needed
            return 0; // Default value if parsing fails
        }
    }

    //  get the previous count of new msg and add to it from sendMessage
    public static void checkAndSaveCounts_SendMsg(String otherUid){

        Thread thread = new Thread(() -> {

            // set my count to 0
            refChecks.child(myId).child(otherUid).child("unreadMsg").setValue(0);

            //   check if the user is in my chat box and reset the count -- newMsgCount & unreadMsg
            refChecks.child(otherUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    try{
                        boolean statusState = (boolean) snapshot.child(myId)
                                .child("status").getValue();

                        if(statusState == true) {

                            refChecks.child(otherUid).child(myId).child("unreadMsg").setValue(0);

                        } else {
                            // increase the new msg count
                            refChecks.child(otherUid).child(myId)
                                    .child("unreadMsg").setValue(count+=1);   // adding
                            refChecks.child(otherUid).child(myId)
                                    .child("newMsgCount").setValue(newMsgCount+1);
                        }
                    } catch (Exception e){
                        refChecks.child(otherUid).child(myId).child("status").setValue(false);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });

        thread.start();
    }

    public void retrieveMessages(String userName, String otherUID, Context mContext){

        List<MessageModel> modelListAllMsg = new ArrayList<>();     // send empty List to the adapter

        // initialise adapter
        MessageAdapter adapter = new MessageAdapter(modelListAllMsg, userName, otherUID, mContext,
                recyclerMap.get(otherUID));

        // retrieve the last previous scroll position
        getLastScrollPosition(otherUID);

        AllConstants.executors.execute(() -> {

            if(chatViewModel != null){
                // fetch chats from ROOM
                if(chatViewModel.getEachUserChat(otherUID) != null){
                    EachUserChats eachUserChat = chatViewModel.getEachUserChat(otherUID);
                    List<MessageModel> userChatModel = eachUserChat.userChatList;
                    adapter.setModelList(userChatModel);
                    modelListMap.put(otherUID, adapter.getModelList());
                }

                runOnUiThread(() -> {
                    //  delay later for like 3 sec to fetch all old data

                    // add new message directly to local List and interact with few msg in refMsgFast database
                    newMessageInteraction(adapter, otherUID);

                    // edit message
                    getEditMessage(adapter, otherUID);

                    // delete local list with idKey
                    getDeleteMsgId(adapter, otherUID);

                    checkClearChatsDB(otherUID);

                });
            }

        });


//        getAllMessages(userName, otherName, modelListAllMsg, msgListNotRead, adapter, otherUID);

        adapter.setFragmentListener((FragmentListener) mContext);

        adapterMap.put(otherUID, adapter); // save each user adapter

        recyclerMap.get(otherUID).setAdapter(adapter);

    }

    // retrieve the last previous scroll position
    private void getLastScrollPosition(String otherId)
    {
        if(lastPositionPreference != null){
            int position = lastPositionPreference.getInt(otherId, 0);
            scrollNumMap.put(otherId, (position - 10) );
        }
    }

    private void copyChats(){
        int size = chatModelList.size();
        String all_text = new Date().toString().substring(0, 19);
        for (int i = 0; i < size; i++){
            MessageModel chatModel = chatModelList.get(i);
            // check if message is not null in case user copy voice_note
            if(chatModel.getMessage() != null){
                all_text += "\n\n" + chatModel.getMessage();
            }
        }

        ClipboardManager clipboard =  (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", all_text);

        if (clipboard == null || clip == null) return;
        clipboard.setPrimaryClip(clip);

        cancelChatOption();
    }
    // retrieve all message from the database just once
    private void getAllMessages(String userName, String otherName, List<MessageModel> allMsgList,
                                List<MessageModel> msgListNotRead, MessageAdapter adapter, String otherUID)
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

                            // add to local database
                            chatViewModel.insertChat(otherUID, messageModelOldMsg);

                        } catch(Exception e) {
                            System.out.println("Error at M1775 getAllMes() " + e.getMessage());
                            refMessages.child(userName).child(otherName).child(snapshotOld.getKey()).removeValue();
                        }
                    }

                    // scroll to previous position UI of user
                    try{
                        scrollToPreviousPosition(otherUID, (int) scrollNumMap.get(otherUID));
                    } catch (Exception e){
                        scrollToPreviousPosition(otherUID, adapter.getItemCount() - 1);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    
    // add new message to local List and interact with few msg in refMsgFast database for delivery and read status
    private void newMessageInteraction(MessageAdapter adapter, String userId)
    {
        AllConstants.executors.execute(() -> refMsgFast.child(myId).child(userId)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot snapshot1 : snapshot.getChildren()) {

                    MessageModel messageModel = snapshot1.getValue(MessageModel.class);

                    // If there's new message from otherUser, add here
                    if(messageModel.getFromUid() != null){

                        if (messageModel.getFromUid().equals(userId)) {
                            // add the new msg to the modelList method at MessageAdapter
                            adapter.addNewMessageDB(messageModel);
                            // add to room database
                            chatViewModel.insertChat(userId, messageModel);
                            // find position and move it to top as recent chat // add to outside ROOM
                            ChatListAdapter.getInstance()
                                    .findUserPositionByUID(userId, messageModel, snapshot1.getKey());

                            // delete after delivery the chat
                            refMsgFast.child(myId).child(userId).child(snapshot1.getKey()).removeValue();

                            // update last msg for outside chat display chat
                            refLastDetails.child(myId).child(userId).child("msgStatus").setValue(0);

                            // check recycler position before scrolling
                            int scrollNumCheck = scrollNumMap.get(userId) == null ? adapter.getItemCount() - 1
                                    : (int) scrollNumMap.get(userId) ;
                            int scrollCheck = adapter.getItemCount() - scrollNumCheck;

                            // scroll to last position I am inside chat
                            if(insideChatMap.get(userId) != null) {
                                if(insideChatMap.get(userId) && scrollCheck < 20){
                                    scrollToPreviousPosition(userId, (adapter.getItemCount() - 1));
                                }
                            }

                            adapter.notifyItemInserted(adapter.getItemCount() - 1);

                            // show new msg alert text for user
                            if(scrollPositionIV.getVisibility() == View.VISIBLE){
                                receiveIndicator.setVisibility(View.VISIBLE);
                                sendIndicator.setVisibility(View.GONE);
                            }
                        }
                    } else{
                        refMsgFast.child(myId).child(userId).child(snapshot1.getKey()).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }));

    }

    // retrieve message when edited
    private void getEditMessage(MessageAdapter adapter, String otherUid)
    {
        refEditMsg.child(myId).child(otherUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // loop through each child
                for (DataSnapshot snapshotEdit : snapshot.getChildren()) {

                    if(!snapshotEdit.child("fromUid").equals(otherUid)){
                        // get the messages from the database and set the idKey
                        EditMessageModel editMessageModel = snapshotEdit.getValue(EditMessageModel.class);
                        editMessageModel.setId(snapshotEdit.getKey());

                        int startCount = adapter.getModelList().size() > 50 ? adapter.getModelList().size() - 50: 0;  // set default to 50

                        boolean isEditMessage = false;
                        int position = 0;

                        // Check if the message already exists in the current local messages list
                        for (int i = adapter.getModelList().size()-1; i >= startCount; i--) {
                            MessageModel existingMessage = adapter.getModelList().get(i);
                            if (editMessageModel.getId().equals(existingMessage.getIdKey())) {
                                isEditMessage = true;
                                position = i;
                                break;
                            }
                        }

                        if(isEditMessage){  // replace the message details if found
                            adapter.getModelList().get(position).setMessage(editMessageModel.getMessage());
                            adapter.getModelList().get(position).setEmojiOnly(editMessageModel.getEmojiOnly());
                            adapter.getModelList().get(position).setTimeSent(editMessageModel.getTimeSent());
                            adapter.getModelList().get(position).setEdit(editMessageModel.getEdit());

//                             save to ROOM database
                            chatViewModel.updateChat(adapter.getModelList().get(position));

                            // notify with empty object so it doesn't duplicate
                            adapter.notifyItemChanged(position, new Object());
                        }

                        // check outside message if it's same id message that was edited and update it
                        refLastDetails.child(myId).child(otherUid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshotLast) {
                                //  check if the key id is same
                                String editID = editMessageModel.getId();
                                String outSideChatID = snapshotLast.child("idKey").getValue().toString();

                                if(editID.equals(outSideChatID)){
                                    //  update and replace the message
                                    refLastDetails.child(myId).child(otherUid)
                                            .child("message").setValue(editMessageModel.getMessage());
                                    //  update and replace the emoji if only emoji is sent
                                    refLastDetails.child(myId).child(otherUid)
                                            .child("emojiOnly").setValue(editMessageModel.getEmojiOnly());

                                    // update user chatList model
                                    ChatsListFragment.findUserAndEditChat(otherUid, editID,
                                            editMessageModel.getMessage(), editMessageModel.getEmojiOnly());

                                    // update the ROOM outside UI
                                    chatViewModel.editOutsideChat(otherUid, editMessageModel.getMessage(),
                                            editMessageModel.getEmojiOnly(), editID);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        //  delete after changing
                        refEditMsg.child(myId).child(otherUid).removeValue();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //  retrieve the delete message id from other user and compare it my local list id and delete if found
    private void getDeleteMsgId(MessageAdapter adapter, String otherUid)
    {
        refDeleteMsg.child(myId).child(otherUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot snapshotDelete: snapshot.getChildren()) {
                    // get the randomID for outside chat, and keyID for inside chat
                    String deleteChatID = snapshotDelete.child("idKey").getValue().toString();
                    String deleteIdKey = snapshotDelete.getKey();
                    // loop through the local list and search for same idKey
                    for (int i = adapter.getModelList().size() - 1; i >= 0; i--) {
                        String listMessageID = adapter.getModelList().get(i).getIdKey();

                        if(deleteIdKey.equals(listMessageID)){

                            // delete from ROOM database
                            chatViewModel.deleteChat(adapter.getModelList().get(i));
                            // delete photo from my phone storage if photo exist
                            deleteFileFromPhoneStorage(adapter.getModelList().get(i)); // get id delete other user
                            // delete from local list if id key matches
                            adapter.getModelList().remove(i);
//                            adapter.notifyItemRemoved(i);
                            adapter.notifyDataSetChanged();
                            // delete idKey from database if id key matches
                            refDeleteMsg.child(myId).child(otherUid).child(deleteIdKey).removeValue();
                            break;
                        }
                    }

                    // check outside message if it's same message that was deleted and delete for both user
                    refLastDetails.child(myId).child(otherUid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshotLast) {
                            //  check if the random id is same
                            String outSideChatID = snapshotLast.child("idKey").getValue().toString();
                            if(deleteChatID.equals(outSideChatID)){
                                //   the message
                                refLastDetails.child(myId).child(otherUid)
                                        .child("message").setValue("...");
                                refLastDetails.child(otherUid).child(myId)
                                        .child("message").setValue("...");

                                // delete the ROOM outside UI
                                chatViewModel.editOutsideChat(otherUid,
                                        AllConstants.DELETE_ICON + " ...",
                                        null, outSideChatID);

                                // update user chatList model
                                ChatsListFragment.findUserAndDeleteChat(otherUid, deleteChatID);

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

    // delete photo from phone storage if a photo is delete from Chat
    public void deleteFileFromPhoneStorage(MessageModel chatModel){
        // delete the low quality photo saved on my local phone storage
        if(chatModel.getPhotoUriPath() != null){
            Uri lowPhotoUri = Uri.parse(chatModel.getPhotoUriPath());
            File searchPhotoPath = new File(lowPhotoUri.getPath());
            if(searchPhotoPath.exists()) {
                searchPhotoPath.delete();
            }
            //  delete from firebase storage if not yet deleted
            if(chatModel.getPhotoUriPath().startsWith("media/voice_note")){
                FirebaseStorage.getInstance().getReference(chatModel.getPhotoUriPath()).delete();
            }
        }

        // delete the high quality photo saved on my local phone storage
        if(chatModel.getPhotoUriOriginal() != null){
            // if id is other user, that means I downloaded the photo, so search and delete from my local phone storage
            if(!chatModel.getFromUid().equals(myId) || chatModel.getPhotoUriOriginal().startsWith("file:/") ){
                Uri highPhotoUri = Uri.parse(chatModel.getPhotoUriOriginal());
                File searchPhotoOriginal = new File(highPhotoUri.getPath());
                if(searchPhotoOriginal.exists()) {
                    searchPhotoOriginal.delete();
                }

                //  delete from firebase storage
                if(chatModel.getPhotoUriOriginal().startsWith("media/voice_note")){
                    FirebaseStorage.getInstance().getReference(chatModel.getPhotoUriOriginal()).delete();
                }
            }
        }
        // delete voice note from phone storage
        if(chatModel.getVoiceNote() != null){
            File searchVoiceNotePath = new File(chatModel.getVoiceNote());
            if(searchVoiceNotePath.exists()) {
                searchVoiceNotePath.delete();
            }
            //  delete from firebase storage if not yet deleted
            if(chatModel.getVoiceNote().startsWith("media/voice_note")){
                FirebaseStorage.getInstance().getReference(chatModel.getVoiceNote()).delete();
            }
        }

    }

    public static String getFileSize(Uri imageUri, Context context){
        try {   // get the total image length
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream != null) {
                int fileSizeBytes = inputStream.available();
                int fileSizeKB = fileSizeBytes / 1024; // Size in kilobytes
                int fileSizeMB = fileSizeKB / 1024; // Size in megabytes

                String sizeString = fileSizeKB < 1000.0 ? Math.round(fileSizeKB) + " kB" : Math.round(fileSizeMB) + " MB";

                inputStream.close();

                return sizeString;
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error Occur MA3200: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return context.getString(R.string.app_name); // Return an empty string or another appropriate default value in case of an error

    }

    // check if other user send me chat id to read
    private void getChatReadRequest(String otherId) {
        AllConstants.executors.execute(() -> {
            chatReadListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        if(snapshot1.exists()){
                            // if id exist, tell other user I have seen it
                            String getId = snapshot1.getValue().toString();
                            // return back to other user that I have read it
                            refChatIsRead.child(otherId).child(myId).push().setValue(getId);
                            // delete when done
                            refOnReadRequest.child(myId).child(otherId).child(snapshot1.getKey()).removeValue();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle errors
                }
            };

            // Add the ValueEventListener to the database reference
            refOnReadRequest.child(myId).child(otherId).addValueEventListener(chatReadListener);
        });
    }

    // check if other user has read my chat
    private void getReadChatResponse(String otherId){
        refChatIsRead.child(myId).child(otherId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    if(snapshot1.exists()){
                        String getChatId = snapshot1.getValue().toString();

                        if(adapterMap.get(otherId) != null){

                            MessageAdapter adapter = adapterMap.get(otherId);
                            // find the position of the chat
                            int chatPosition = adapter.findMessagePositionById(getChatId);
                            if(chatPosition != -1){ // check if chat exist
                                // get the current delivery status of the chat
                                int currentStatus = adapter.getModelList().get(chatPosition).getMsgStatus();
                                if(currentStatus != 700016){ // 700016 means chat is read

                                    adapter.getModelList().get(chatPosition).setMsgStatus(700016);

                                    // update the inside chat
                                    adapter.notifyItemChanged(chatPosition, new Object());

                                    // update delivery status for outSide chat
                                    ChatListAdapter.getInstance().updateDeliveryToRead(otherId);

                                    // update ROOM for inside chat
                                    chatViewModel.updateDeliveryStatus(otherId, getChatId, 700016);

                                    // update ROOM for outside chat
                                    chatViewModel.updateOutsideDelivery(otherId, 700016);

                                }
                            }
                        }

                        // delete when done
                        refChatIsRead.child(myId).child(otherId).child(snapshot1.getKey()).removeValue();

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addClickableEmoji() {

        MessageAdapter messageAdapter = adapterMap.get(otherUserUid);
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
                    if (containsEmoji) {
                        // add emoji to local list and ROOM
                        messageAdapter.addEmojiReact(holderEmoji, getEmoji, chatID, otherUserUid);

                        // send a signal to add emoji for other user also
                        Map<String, Object> emojiMap = new HashMap<>();
                        emojiMap.put("emojiReact", getEmoji);
                        emojiMap.put("chatID", chatID);
                        refEmojiReact.child(otherUserUid).child(myId).push().setValue(emojiMap);
                    }

                    et_emoji.setText("");
                    popup.dismiss();
                    hideKeyboard();
                    typeMsgContainer.setVisibility(View.VISIBLE);
                    editTextMessage.requestFocus();
                    //  reverse the emoji initialization back to the emoji button icon
                    popup = EmojiPopup.Builder.fromRootView( recyclerContainer).build(editTextMessage);

                    //  remove highlight from selected chat
                    clearAllHighlights();

                    // remove callBack after processing everything
                    handlerEmoji.removeCallbacks(emojiRunnable);
                }

                handlerEmoji.postDelayed(this, 1000); // Re-schedule the runnable
            }
        };

        handlerEmoji.post(emojiRunnable);

    }

    // get the emoji reaction from other user
    private void getEmojiReact(String otherId){
        refEmojiReact.child(myId).child(otherId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    if(snapshot1.exists()){
                        String getEmoji = snapshot1.child("emojiReact").getValue().toString();
                        String getChatId = snapshot1.child("chatID").getValue().toString();

                        adapterMap.get(otherId).emojiReactSignal(getEmoji, getChatId, otherId);

                        // delete from database once added
                        refEmojiReact.child(myId).child(otherId).child(snapshot1.getKey()).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void pinIconsVisibility(String otherId){

        // make private pin details invisible if no pin chat yet
        if(pinPrivateChatMap.get(otherId) != null){
            int totalPins = pinPrivateChatMap.get(otherId).size();
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
        if(pinPublicChatMap.get(otherId) != null){
            int totalPins = pinPublicChatMap.get(otherId).size();
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
    private void getPinChats(String otherID){

        List<PinMessageModel> pinPrivateList = new ArrayList<>();
        List<PinMessageModel> pinEveryoneList = new ArrayList<>();
        MessageAdapter messageAdapter = adapterMap.get(otherID);

        // get private pin chats
        refPrivatePinChat.child(myId).child(otherID).orderByChild("pinTime").addValueEventListener(new ValueEventListener() {
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

        // get and updates new pin chats (Also the ones the other user pin)
        refPublicPinChat.child(myId).child(otherID).orderByChild("pinTime").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot snapshotPin : snapshot.getChildren()) {
                    // check if pin message still exists
                    if (snapshotPin.child("msgId").exists()) {
                        PinMessageModel pinMsgModel = snapshotPin.getValue(PinMessageModel.class);

                        // Assume the message does not exist in the list initially
                        boolean messageExists = false;

                        // Check if pin msg is already existing in the List
                        for (PinMessageModel pinChat : pinEveryoneList) {
                            if (pinMsgModel.getMsgId().equals(pinChat.getMsgId())) {
                                messageExists = true;
                                break; // Exit the loop if the message is found
                            }
                        }

                        if (!messageExists) {
                            pinEveryoneList.add(pinMsgModel);

                            // update room
                            chatViewModel.updateChatPin(otherID, pinMsgModel.getMsgId(), true);

                            // get the position of the pin chat
                            int pinPosition = messageAdapter.findMessagePositionById(pinMsgModel.getMsgId());

                            // update the icon on the model chat list UI
                            if(pinPosition != -1){
                                messageAdapter.getModelList().get(pinPosition).setChatPin(true);
                                if (fiveSecondsWait) {
                                    // delay 5sec to allow pin chat to the list first
                                    messageAdapter.notifyItemChanged(pinPosition, new Object());;
                                }
                            }
                        }
                    }
                }


                int newCount = pinEveryoneList.size();
                totalPinPublic_TV.setText("" + newCount);

                pinPublicChatMap.put(otherID, pinEveryoneList);   // keep updating the map

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

        pinPrivateChatMap.put(otherID, pinPrivateList);
        pinPublicChatMap.put(otherID, pinEveryoneList);

    }

    // if other user unpin chat for everyone, get the chat id and unpin mine
    private void getDeletePinId(String otherId){
        MessageAdapter messageAdapter = adapterMap.get(otherId);
        refDeletePin.child(myId).child(otherId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1 : snapshot.getChildren()){

                    if(snapshot1.exists()){
                        String chatId = snapshot1.getValue().toString();

                        //update the ROOM
                        chatViewModel.updateChatPin(otherId, chatId, false);

                        // Loop through the map and remove the chat from the pinPublicChatMap
                        for (Map.Entry<String, List<PinMessageModel>> entry : pinPublicChatMap.entrySet()) {
                            String otherID = entry.getKey();
                            List<PinMessageModel> pinEveryoneList = entry.getValue();

                            // Loop through the list to find and remove the item based on some condition
                            Iterator<PinMessageModel> iterator = pinEveryoneList.iterator();
                            while (iterator.hasNext()) {
                                PinMessageModel pinChat = iterator.next();

                                // Check if this is the item you want to remove (based on some condition)
                                if (pinChat.getMsgId().equals(chatId)) {
                                    // Remove the item using the iterator
                                    iterator.remove();
                                }
                            }

                            // Update the map with the modified list
                            pinPublicChatMap.put(otherID, pinEveryoneList);
                        }

                        // get the position of the pin chat
                        int pinPosition = messageAdapter.findMessagePositionById(chatId);
                        // update the icon on the model chat list UI
                        messageAdapter.getModelList().get(pinPosition).setChatPin(false);
                        messageAdapter.notifyItemChanged(pinPosition, new Object());

                        // delete the pin Id from firebase
                        refDeletePin.child(myId).child(otherId).child(snapshot1.getKey()).removeValue();

                        // get total number of pin messages
                        int totalPinMsgCount = pinPublicChatMap.get(otherId).size();

                        totalPinPublic_TV.setText("" + totalPinMsgCount);
                        pinCount_TV.setText("(1/" + totalPinMsgCount + ")");

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void pinAndUnpinChatForEveryone(){

        pinStatus = PUBLIC;      // indicate to show public pins

        //  hide pin_icon if pin map is empty
        if(pinPublicChatMap.get(otherUserUid).size() <= 1){
            pinPublicIcon_IV.setVisibility(View.GONE);
            pinLockPublic_IV.setVisibility(View.GONE);
            totalPinPublic_TV.setVisibility(View.GONE);
            newPinIndicator_TV.setVisibility(View.GONE);

        }

        // get total number of pin messages
        int totalPinMsgCount = pinPublicChatMap.get(otherUserUid).size();

        // get data to save to database
        Map<String, Object> pinDetails = new HashMap<>();
        pinDetails.put("msgId", msgId);
        pinDetails.put("message", message);
        pinDetails.put("pinTime", timeStamp);
        pinDetails.put("pinByWho", pinByWho);

        boolean found = false;
        String idFound = null;
        for (PinMessageModel pinMes : pinPublicChatMap.get(otherUserUid)) {
            if (pinMes.getMsgId().equals(msgId)) {
                found = true;
                idFound = pinMes.getMsgId();
                break;
            }
        }

        boolean foundPrivate = false;
        // check if message has already been pin or not in public pin
        for (PinMessageModel pinMes : pinPrivateChatMap.get(otherUserUid)) {
            if (pinMes.getMsgId().equals(msgId)) {
                foundPrivate = true;
                break;
            }
        }

        if (found) {
            // Delete message from the local map
            pinPublicChatMap.get(otherUserUid)
                    .removeIf(pinMesExist -> pinMesExist.getMsgId().equals(msgId));

            //  Delete message from firebase database
            refPublicPinChat.child(myId).child(otherUserUid).child(idFound).removeValue();
            refPublicPinChat.child(otherUserUid).child(myId).child(idFound).removeValue();

            // send signal to remove pin msg from other user local map and update UI pin
            refDeletePin.child(otherUserUid).child(myId).push().setValue(msgId);

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
                //  show pin icon on the chat UI, and update ROOM and adapter list
                adapterMap.get(otherUserUid).pinIconHide(holderPin, msgId,false);
            }

            Toast.makeText(this, "Chat unpin!", Toast.LENGTH_SHORT).show();

        } else {
            // Add the new pin message to the local map
            PinMessageModel newPin = new PinMessageModel(msgId, message, timeStamp, pinByWho);
            pinPublicChatMap.get(otherUserUid).add(newPin);

            // Add the new pin message to firebase database
            refPublicPinChat.child(myId).child(otherUserUid).child(msgId)
                    .setValue(pinDetails);
            refPublicPinChat.child(otherUserUid).child(myId).child(msgId)
                    .setValue(pinDetails);

            //  show pin icon on the chat UI, and update ROOM and adapter list
            adapterMap.get(otherUserUid).pinIconDisplay(holderPin, msgId,true);

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
        if(pinPrivateChatMap.get(otherUserUid).size() <= 1){
            pinPrivateIcon_IV.setVisibility(View.GONE);
            pinLockPrivate_IV.setVisibility(View.GONE);
            totalPinPrivate_TV.setVisibility(View.GONE);
        }

        // get total number of pin messages
        int totalPinMsgCount = pinPrivateChatMap.get(otherUserUid).size();

        // get data to save to database
        Map<String, Object> pinDetails = new HashMap<>();
        pinDetails.put("msgId", msgId);
        pinDetails.put("message", message);
        pinDetails.put("pinTime", timeStamp);

        boolean found = false;
        String idFound = null;

        // check if message has already been pin or not
        for (PinMessageModel pinMes : pinPrivateChatMap.get(otherUserUid)) {

            if (pinMes.getMsgId().equals(msgId)) {
                found = true;
                idFound = pinMes.getMsgId();
                break;
            }
        }

        boolean foundPublic = false;
        for (PinMessageModel pinMes : pinPublicChatMap.get(otherUserUid)) {

            if (pinMes.getMsgId().equals(msgId)) {
                foundPublic = true;
                break;
            }
        }

        if (found) {
            // Delete message from the local map
            pinPrivateChatMap.get(otherUserUid)
                    .removeIf(pinMesExist -> pinMesExist.getMsgId().equals(msgId));

            //  Delete message from firebase database
            refPrivatePinChat.child(myId).child(otherUserUid).child(idFound).removeValue();

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
                //  show pin icon on the chat UI, and update ROOM and adapter list
                adapterMap.get(otherUserUid).pinIconHide(holderPin, msgId, false);
            }

            Toast.makeText(this, "Chat unpin!", Toast.LENGTH_SHORT).show();

        } else {
            // Add the new pin message to the local map
            PinMessageModel newPin = new PinMessageModel(msgId, message, timeStamp, pinByWho);
            pinPrivateChatMap.get(otherUserUid).add(newPin);

            // Add the new pin message to firebase database
            refPrivatePinChat.child(myId).child(otherUserUid).child(msgId)
                    .setValue(pinDetails);

            //  show and update pin icon on the chat UI, also update ROOM
            adapterMap.get(otherUserUid).pinIconDisplay(holderPin, msgId, true);

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

    private void vibrateDevice(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator != null && vibrator.hasVibrator()) {
            // Vibrate for 100 milliseconds
            vibrator.vibrate(100);
        }
    }

    private void swipeReply(){
        itemTouchSwipe = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                MessageModel modelChats = adapterMap.get(otherUserUid).getModelList()
                        .get(viewHolder.getAdapterPosition());

                adapterMap.get(otherUserUid).replyChat(modelChats, null);
                // notify it to reset the adapter in case onSwipe was called
                adapterMap.get(otherUserUid).notifyDataSetChanged();
                // close chatOption is visible
                cancelChatOption();
            }

//            @Override
//            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
//                // Access the specific view within your ViewHolder
//                View yourSpecificView = ((MessageAdapter.MessageViewHolder) viewHolder).getYourSpecificView();
//
//                if(yourSpecificView != null){
//                    if (yourSpecificView.getVisibility() == View.VISIBLE) {
//                        Toast.makeText(MainActivity.this, "Sharp call", Toast.LENGTH_SHORT).show();
//                        return 0;
//                    }
//                }
//                return super.getSwipeDirs(recyclerView, viewHolder);
//
//            }

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int screenWidth = recyclerView.getWidth();
                float quarterScreen = screenWidth * 0.2f;

                // Check if the item is beyond the quarter of the screen and Return 0 to disable both dragging and swiping
                if (viewHolder.itemView.getX() >= quarterScreen || viewHolder.itemView.getX() <= -quarterScreen) {

                    MessageModel modelChats = adapterMap.get(otherUserUid).getModelList()
                            .get(viewHolder.getAdapterPosition());

                    adapterMap.get(otherUserUid).replyChat(modelChats, null);

                    // close chatOption is visible
                    cancelChatOption();

                    return 0;
                } else {
                    // Allow normal movement
                    return super.getMovementFlags(recyclerView, viewHolder);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                // Get the width of the screen
                int screenWidth = recyclerView.getWidth();

                // Calculate the quarter of the screen
                float quarterScreen = screenWidth * 0.2f;

                // Check if the item is beyond the quarter of the screen
                if (viewHolder.itemView.getX() >= quarterScreen || viewHolder.itemView.getX() <= -quarterScreen) {
                    // indicate the colour
                    viewHolder.itemView.setBackgroundColor(ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.transparent_orangeLow));

                } else {
                    // Reset the background color or remove the indicator
                    viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
                }
            }

        });

    }

    private void scrollToPinMessage(int i){
        pinMsg_TV.setTypeface(null);    // remove italic style if any

        // get the msg id you want to scroll to
        String findMsgId = pinStatus.equals(PRIVATE) ? pinPrivateChatMap.get(otherUserUid).get(i).
                getMsgId() : pinPublicChatMap.get(otherUserUid).get(i).getMsgId();

        // get the position of the message
        int position = adapterMap.get(otherUserUid).findMessagePositionById(findMsgId);

        if(position != RecyclerView.NO_POSITION){

            recyclerMap.get(otherUserUid).scrollToPosition(position - 2);

            // highlight the message found
            adapterMap.get(otherUserUid).highlightItem(position);

        } else {
            Toast.makeText(this, "Chat not found", Toast.LENGTH_SHORT).show();
            chatNotFoundID = findMsgId;
        }
    }

    // scroll to position on new message update
    private void scrollToPreviousPosition(String otherId, int position){
        for (int i = 0; i < recyclerContainer.getChildCount(); i++) {
            View child = recyclerContainer.getChildAt(i);

            if (child == recyclerMap.get(otherId)){
                RecyclerView recyclerView = (RecyclerView) child;

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                layoutManager.scrollToPosition(position);

            }
        }
    }

    public void recyclerViewChatVisibility(String otherUid){
        for (int i = 0; i < recyclerContainer.getChildCount(); i++) {
            View child = recyclerContainer.getChildAt(i);
            if (child == recyclerMap.get(otherUid)) {

                child = recyclerMap.get(otherUid);
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
                    if(handlerInternet != null && internetCheckRunnable != null && networkListener.equals("yes")){
                        
                        if(networkTypingOk){
                            handlerTyping.post(runnableTyping);
                            networkTypingOk = false;
                        }
                    }

                    sendMessageButton.setVisibility(View.VISIBLE);
                    recordButton.setVisibility(View.INVISIBLE);
                    camera_IV.setVisibility(View.INVISIBLE);
                    gameMe_IV.setVisibility(View.INVISIBLE);
                    refChecks.child(otherUserUid).child(myId).child("typing").setValue(1);

                } else {
                    if(listener.equals("edit")){
                        sendMessageButton.setVisibility(View.VISIBLE);
                        recordButton.setVisibility(View.INVISIBLE);
                        camera_IV.setVisibility(View.INVISIBLE);
                        gameMe_IV.setVisibility(View.INVISIBLE);
                        refChecks.child(otherUserUid).child(myId).child("typing").setValue(1);
                    } else {
                        sendMessageButton.setVisibility(View.INVISIBLE);
                        recordButton.setVisibility(View.VISIBLE);
                        camera_IV.setVisibility(View.VISIBLE);
                        gameMe_IV.setVisibility(View.VISIBLE);
                        refChecks.child(otherUserUid).child(myId).child("typing").setValue(0);
                    }
                }

                isSendingFile = false;
            }

            @Override
            public void afterTextChanged(Editable editable) {

                AllConstants.executors.execute(() -> {

                    // reset the new msg count
//                    DatabaseReference statusCheck2 = FirebaseDatabase.getInstance().getReference("Checks");
//                    statusCheck2.child(otherUserUid).addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                            try{
//                                boolean statusState = (boolean) snapshot.child(myId)
//                                        .child("status").getValue();
//
//                                // receiver should be 0
//                                if(statusState == true) {
//                                    statusCheck2.child(otherUserUid).child(myId).child("newMsgCount").setValue(0);
//                                }
//
//                                // Mine should be 0
//                                statusCheck2.child(myId).child(otherUserUid).child("newMsgCount").setValue(0);
//
//                            } catch (Exception e){
//                                statusCheck2.child(otherUserUid).child(myId)
//                                        .child("status").setValue(false);
//                            }
//
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });

                });

            }
        });
    }

    // show when other user is typing
    public void getMyUserTyping()
    {
        AllConstants.executors.execute(() -> refChecks.child(myId).addValueEventListener(new ValueEventListener() {
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
                    refChecks.child(myId).child(otherUserUid).child("typing").setValue(0);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }));

    }

    // change all load status message to delivery status (700033 ---> 700024)
    private void reloadFailedMessagesWhenNetworkIsOk()
    {
        if(offlineChat != null){
            // Retrieve all keys from local preference
            Map<String, ?> allEntries = offlineChat.getAll();

            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                String chatKey = entry.getKey();
                String otherUID_ = (String) entry.getValue();

                if(adapterMap.get(otherUID_) != null){

                    MessageAdapter adapter = adapterMap.get(otherUID_);
                    // find the position of the failed chat
                    int chatPosition = adapter.findMessagePositionById(chatKey);
                    // check if chat exist
                    if(chatPosition != -1){
                        // get the current delivery status of the chat
                        int currentStatus = adapter.getModelList().get(chatPosition).getMsgStatus();
                        if(currentStatus == 700033){ // 700033 means not sent
                            // update delivery or sent status (700024 means sent) for inside chat
                            adapter.getModelList().get(chatPosition).setMsgStatus(700024);
                            adapter.notifyItemChanged(chatPosition, new Object());

                            // update delivery status for outSide chat
                            ChatListAdapter.getInstance().updateDeliveryStatus(otherUID_);

                            // update ROOM for inside chat
                            chatViewModel.updateDeliveryStatus(otherUID_, chatKey, 700024);

                            // update ROOM for outside chat
                            chatViewModel.updateOutsideDelivery(otherUID_, 700024);

                            // delete from sharePreference if done updating
                            offlineChat.edit().remove(chatKey).apply();

                        } else offlineChat.edit().remove(chatKey).apply();

                    }
//                    else offlineChat.edit().remove(chatKey).apply();

                }

            }
        }
    }

    //  Get all previous counts of unreadMsg and newMsgCount
    private void getPreviousCounts(){

        Thread thread = new Thread(() -> {

            DatabaseReference referenceMsgCountCheck = FirebaseDatabase.getInstance().getReference("Checks")
                    .child(otherUserUid).child(myId);
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

    //      ================      pdf methods           ========================
    private void activityPdfLauncher(){
        pickPdfLauncher = registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(),
                new ActivityResultCallback<List<Uri>>() {
                    @Override
                    public void onActivityResult(List<Uri> results) {
                        if (results != null && !results.isEmpty()) {
                            for (Uri result : results) {

                                String pdfName = getFileName(result);
                                String pdfSize = getFileSize(result, MainActivity.this);
                                Bitmap bitmap = getThumbnailFromPdfUri(result);

                                if(isPdfFile(result)){
                                    System.out.println("what is pdf " + pdfName + pdfSize);

                                } else if (isMsWordFile(result)){
                                    // image is sent
                                    System.out.println("what is msWord " + pdfName + pdfSize);

                                } else if (isPhotoFile(result)){
                                    // image is sent
                                    System.out.println("what is photo " + pdfName + pdfSize);
                                }

                                imageView.setImageBitmap(bitmap);
                                // Now you have the URI of each selected PDF file
                                // Get additional information
//                                Bitmap pdfThumbnail = getThumbnailFromPdfUri(result);
//
//                                // Now you can proceed with sending each file and additional information to another user
//                                sendPdfFile(result, pdfName, pdfSize, pdfThumbnail);
                            }
                        }
                    }
                });
    }

    private String getFileName(Uri uri) {
        String displayName = getString(R.string.app_name);
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        displayName = cursor.getString(index);
                    }
                }
            }
        } else if (uri.getScheme().equals("file")) {
            displayName = uri.getLastPathSegment();
        }
        return displayName;
    }

    private Bitmap getThumbnailFromPdfUri(Uri uri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
            if (parcelFileDescriptor != null) {
                PdfRenderer renderer = new PdfRenderer(parcelFileDescriptor);
                PdfRenderer.Page page = renderer.openPage(0);

                // Generate thumbnail from the first page
                Bitmap thumbnail = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                page.render(thumbnail, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                // Close the page and renderer
                page.close();
                renderer.close();
                parcelFileDescriptor.close();

                return thumbnail;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean isPdfFile(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        return mimeType != null && mimeType.equals("application/pdf");
    }

    private boolean isMsWordFile(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        return mimeType != null && mimeType.equals("application/msword")
                || mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }

    private boolean isPhotoFile(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        return mimeType != null && mimeType.equals("image/jpeg") || mimeType.equals("image/png");
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
    public void cancelForwardSettings(Context context){

        onForward = false;
        selectCount = 0;
        totalUser_TV.setText("0 selected");
        editTextMessage.requestFocus();

        pinMsgContainer.setVisibility(View.VISIBLE);
        conTopUserDetails.setVisibility(View.VISIBLE);
        constraintMsgBody.setVisibility(View.VISIBLE);
        tabLayoutGeneral.setVisibility(View.VISIBLE);

        forwardTopContainer.setVisibility(View.INVISIBLE);
        forwardDownContainer.setVisibility(View.INVISIBLE);

        // call forward setting method to remove the checkBox
        myHolderNew.clear();
        myHolderNew.addAll(myHolder_);  // add up all user holder
        ChatListAdapter.getInstance().forwardCheckBoxVisibility(myHolderNew);

        // remove the typingRunnable for checking network
        handlerTyping.removeCallbacks(runnableTyping);

        cancelChatOption(); // close the option chat menu that pop up

        if(appActivity || otherUserUid == null || !insideChat) {
            constraintMsgBody.setVisibility(View.INVISIBLE);
            topMainContainer.setVisibility(View.VISIBLE);
            conTopUserDetails.setVisibility(View.INVISIBLE);
            closePinIcons();    // for sharing photo from other app

            // open the previous Activity it was on before user shared
            if(appActivity){
                Intent mainActivityIntent = new Intent(context, AppLifecycleHandler.currentActivity);
                mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                context.startActivity(mainActivityIntent);
            }
            appActivity = false;
        }

        // delete low photo from app storage if user cancel the sending
        if(sharingPhotoActivated) {
            deleteUnusedPhotoFromSharePrefsAndAppMemory(this);  // call method to delete file
            sharingPhotoActivated = false;
            chatModelList.clear();
        } else chatModelList.clear();

        viewPager2General.setUserInputEnabled(true);    // enable the swiping
        forwardChatUserId.clear();
        selectedUserNames.clear();
        sharing = false; // previous Main Activity from replace the currentActivity when user is sharing photo from other app or gallery

    }

    public static void cancelChatOption(){
        chatOptionsConstraints.setVisibility(View.GONE);
        modelChatsOption = null;
        chatHolder = null;
        if(!sharingPhotoActivated) chatModelList.clear();
        editChatOption_IV.setVisibility(View.VISIBLE);
        replyChatOption_IV.setVisibility(View.VISIBLE);
        emojiChatOption_IV.setVisibility(View.VISIBLE);
        pinChatOption_IV.setVisibility(View.VISIBLE);
        MainActivity.editChatOption_IV.setImageResource(R.drawable.baseline_mode_edit_24);

        deleteChatForOnlyOther_TV.setVisibility(View.VISIBLE);

        pinMsgContainer.setVisibility(View.VISIBLE);

        // clear chat highlight position
        MessageAdapter.chatPositionList.clear();

        MessageAdapter.isOnlongPress = false;

        clearAllHighlights();

    }

    public static void clearAllHighlights(){
        if(otherUserUid != null){
            for (int i = 0; i < recyclerMap.get(otherUserUid).getChildCount(); i++) {
                View itemView = recyclerMap.get(otherUserUid).getChildAt(i);
                itemView.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }

    private void closePinIcons(){
        // Top Pin Chat Settings
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
    }

    // cancel delete option for user from chat list
    private void cancelUserDeleteOption(){
        otherUid_Del = null;
        otherUserName_TV.setText("");
        deleteUserOrClearChatContainer.setVisibility(View.GONE);
    }

    private void cancelChatDeleteOption(){
        constraintDelBody.setVisibility(View.GONE);
        cancelChatOption();
    }
    // turn of readDatabase to 1 (non-read)
    public static void offMainDatabase(){
        new CountDownTimer(10_000, 1000){
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                readDatabase = 1;
            }
        }.start();
    }
    private void fiveSecondsDelay(){

        new Handler().postDelayed(() -> {
            fiveSecondsWait = true;

            if(networkListener.equals("yes")) constrNetConnect.setVisibility(View.GONE);

        },5_000);
    }

    private void adjustRecyclerViewToPhoneScreen(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int screenHeight = displayMetrics.heightPixels;

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) constraintLayoutAdjust.getLayoutParams();
        layoutParams.bottomToTop = R.id.typeMsgContainer; // Replace with the actual ID

        layoutParams.bottomMargin = screenHeight-220;
        // Apply the changes
        constraintLayoutAdjust.setLayoutParams(layoutParams);

        String app_name = getString(R.string.app_name);
        Toast.makeText(this, app_name, Toast.LENGTH_SHORT).show();

    }

    // clear old photo from app memory
    public static boolean deleteOldUriFromAppMemory(List<String> uriToDelete){   // the list contain both low and hugh quality uri path
        for (int i = 0; i < uriToDelete.size(); i++) {
            Uri uriPhoto = Uri.parse(uriToDelete.get(i));
            File searchPhotoPath = new File(uriPhoto.getPath());

            if(searchPhotoPath.exists()) {
                searchPhotoPath.delete();
            }
            
            // notify that there's uri list on sharePreference so as to clear the sharePref when done
            if(i == uriToDelete.size()-1){
                return true;
            }
        }
        return false;
    }
    public static void deleteUnusedPhotoFromSharePrefsAndAppMemory(Context context){
        //  store each photo cropping or painting uri to enable delete from onCreate when app is onDestroy
        unusedPhotoShareRef = context.getSharedPreferences(AllConstants.URI_PREF, Context.MODE_PRIVATE);

        // delete the uri photo from app memory
        String json = unusedPhotoShareRef.getString(AllConstants.OLD_URI_LIST, null);
        Gson gson = new Gson();
        List<String> uriList = gson.fromJson(json, List.class);

        boolean isDoneDeleting = uriList != null && deleteOldUriFromAppMemory(uriList);
        if(isDoneDeleting){
            unusedPhotoShareRef.edit().remove(AllConstants.OLD_URI_LIST).apply();
        }
    }
    
    // check if other user deleted me from his chat list, if yes, then clear all the user chat
    public void checkClearChatsDB(String otherUid){

        AllConstants.executors.execute(() -> {

            // check if other user has deleted or cleared our chats histories from his chat list
            refClearSign.child(otherUid).child(myId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if(snapshot.getValue() != null && snapshot.getValue().toString().equals("clear")){
                        if(adapterMap.get(otherUid) != null ){
                            // clear local list -- adapter
                            adapterMap.get(otherUid).clearChats();
                            runOnUiThread(() -> adapterMap.get(otherUid).notifyDataSetChanged());
                        }
                        //delete the sign from firebase DB
                        refClearSign.child(otherUid).child(myId).removeValue();
                        // chat all chat from ROOM
                        chatViewModel.deleteChatByUserId(otherUid);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


            // check if other user has deleted me from his chat list
            refDeleteUser.child(otherUid).child(myId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if(snapshot.getValue() != null && snapshot.getValue().toString().equals("clear")){
                        if(adapterMap.get(otherUid) != null ){
                            // clear local list -- adapter
                            adapterMap.get(otherUid).clearChats();
                            //  remove user from chatList
                            runOnUiThread(() -> ChatsListFragment.findUserPositionByUID(otherUid));
                        }
                        //delete the sign from firebase DB
                        refDeleteUser.child(otherUid).child(myId).removeValue();
                        // delete user from ROOM
                        chatViewModel.deleteUserById(otherUid);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });

    }

    public void clearInputFields(){
        nameReply.setVisibility(View.GONE);
        replyVisible.setVisibility(View.GONE);
        cardViewReplyOrEdit.setVisibility((int) 8);   // 8 is for GONE

        listener = "no";
        idKey = null;
        if(replyFrom == null) editTextMessage.setText("");
        replyText = null;
        replyFrom = null;
        idKey = null;
        replyVisibility = 8;
        textViewReplyOrEdit.setText("");
        messageModel = null;

        clearAllHighlights();
    }

    private void clearEmojiReactSetting(){
        handlerEmoji.removeCallbacks(emojiRunnable);
        et_emoji.clearFocus();
        editTextMessage.requestFocus();
        if(popup != null) popup.dismiss();
        popup = EmojiPopup.Builder.fromRootView( recyclerContainer).build(editTextMessage);
        typeMsgContainer.setVisibility(View.VISIBLE);
    }
    
    private void clearGlideCache(){
        new Thread(() -> {
            // This method must be called in a background thread
            Glide.get(getApplicationContext()).clearDiskCache();
        }).start();
    }

    // set user image on settings
    private void setUserDetails(){
        refUsers.child(myId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                imageLink = snapshot.child("image").getValue().toString();
                String userName = snapshot.child("userName").getValue().toString();

                if (imageLink == null || imageLink.equals("null")) {
                    imageViewUserPhoto.setImageResource(R.drawable.person_round);
                }
                else {
                    Picasso.get().load(imageLink).into(imageViewUserPhoto);
                }

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
        builder.setTitle("WinnerChat");
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

        if(myId != null){
            if(popup != null) popup.dismiss();
            typeMsgContainer.setVisibility(View.VISIBLE);
            handlerEmoji.removeCallbacks(emojiRunnable);
            //  hide emoji keyboard
            et_emoji.clearFocus();
            editTextMessage.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(et_emoji.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

            // turn off my online and set my last seen date/time
            refUsers.child(myId).child("presence").setValue(ServerValue.TIMESTAMP);

            AllConstants.executors.execute(()->{

                if(constraintMsgBody.getVisibility() == View.VISIBLE){
                    try{
                        refChecks.child(otherUserUid).child(myId).child("typing").setValue(0);
                        int scroll = scrollNum > 20 ? scrollNum: adapterMap.get(otherUserUid).getItemCount() - 1;

                        Map<String, Object> mapUpdate = new HashMap<>();
                        mapUpdate.put("status", false);
                        mapUpdate.put("newMsgCount", 0);
                        refChecks.child(myId).child(otherUserUid).updateChildren(mapUpdate);

                        // save last scroll position to local preference
                        lastPositionPreference.edit().putInt(otherUserUid, scroll).apply();

                        // Remove the ValueEventListener when the back button is pressed
                        if (chatReadListener != null) {
                            refOnReadRequest.child(myId).child(otherUserUid).removeEventListener(chatReadListener);
                        }

                    } catch (Exception e){
                        System.out.println("Check onPause (M1497) " + e.getMessage());
                    }
                }
            });
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        // change my presence back to "online"
        if(myId != null){
            refUsers.child(auth.getUid()).child("presence").setValue(1);
            //  reverse the emoji initialization back to the emoji button icon
            popup = EmojiPopup.Builder.fromRootView( recyclerContainer).build(editTextMessage);

            if(constraintMsgBody.getVisibility() == View.VISIBLE){

                // delay the focus so it doesn't ppo up the keyboard
                new Handler().postDelayed(()-> editTextMessage.requestFocus(), 500);

                try{
                    Map<String, Object> mapUpdate = new HashMap<>();
                    mapUpdate.put("status", true);
                    refChecks.child(myId).child(otherUserUid).setValue(mapUpdate);

                    // Add the ValueEventListener to the database reference
                    refOnReadRequest.child(myId).child(otherUserUid).addValueEventListener(chatReadListener);

                } catch (Exception e){
                    System.out.println("Check onResume (M1546) " + e.getMessage());
                }
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

            } else if (chatOptionsConstraints.getVisibility() == View.VISIBLE)
            {
                cancelChatOption();
            } else if(constraintDelBody.getVisibility() == View.VISIBLE)
            {
                cancelChatDeleteOption();
            } else if (pinOptionBox.getVisibility() == View.VISIBLE)
            {
                pinOptionBox.setVisibility(View.GONE);
                cancelChatOption();
            } else{
                emoji_IV.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
                handlerEmoji.removeCallbacks(emojiRunnable);
                et_emoji.clearFocus();
                editTextMessage.clearFocus();
                editTextMessage.setText("");    // store each user unsent typed msg later

                // General settings
                constraintMsgBody.setVisibility(View.INVISIBLE);
                topMainContainer.setVisibility(View.VISIBLE);
                conTopUserDetails.setVisibility(View.INVISIBLE);
                emoji_IV.setImageResource(R.drawable.baseline_add_reaction_24);
                constraintDelBody.setVisibility(View.GONE); // close delete options
                textViewLastSeen.setText(getString(R.string.app_name));   // clear last seen
                chatMenuProfile.setVisibility(View.GONE); // close profile menu
                isEmojiVisible = false;
                
                // remove the typingRunnable for checking network
                handlerTyping.removeCallbacks(runnableTyping);
                networkTypingOk = true;
                
                // edit and reply settings cancel
                clearInputFields();
                // cancel user or clear_chat container if visible
                cancelUserDeleteOption();
                // hide the pin icons
                closePinIcons();

                // highlight send message and new receive message indicator
                receiveIndicator.setVisibility(View.GONE);
                sendIndicator.setVisibility(View.GONE);

                // clear chat highlight position
                MessageAdapter.chatPositionList.clear();
                clearAllHighlights();

                // make previous view clickable if any
                if(ChatListAdapter.previousView != null) {
                    ChatListAdapter.previousView.setClickable(true);
                    ChatListAdapter.previousView = null;    // return it back to null
                }

                AllConstants.executors.execute(() -> {

                    if(otherUserUid != null){
                        int scroll = 0;
                        if(adapterMap.get(otherUserUid) != null){
                            scroll = scrollNum > 20 ? scrollNum: adapterMap.get(otherUserUid).getItemCount() - 1;
                        }
                        Map<String, Object> mapUpdate = new HashMap<>();
                        mapUpdate.put("status", false);
                        mapUpdate.put("newMsgCount", 0);
                        refChecks.child(myId).child(otherUserUid).updateChildren(mapUpdate);
                        if(scroll > 5) {
                            // save last scroll position to local preference
                            lastPositionPreference.edit().putInt(otherUserUid, scroll).apply();

                            System.out.println("M3052 I have saved scroll " + scroll);
                        }

                        // set responds to pend always      ------- will change later to check condition if user is still an active call
//                    refChecks.child(myId).child(otherUserUid).child("vCallResp").setValue("pending");

                        insideChatMap.put(otherUserUid, false);

                        // Remove the ValueEventListener when the back button is pressed
                        if (chatReadListener != null) {
                            refOnReadRequest.child(myId).child(otherUserUid).removeEventListener(chatReadListener);
                        }

                        // call runnable to add views if list cache view layout is less than 100
                        if (!isLoadViewRunnableRunning && MessageAdapter.viewCacheSend.size() < 50) {
                            handlerLoadViewLayout.post(loadViewRunnable);
                        }

                        insideChat = false;  // onBackPress
                        idKey = null;

                    }

                });
            }

        } else if (onForward)
        {
            cancelForwardSettings(this);    // onBackPress
        } else if (sideBarMenuContainer.getVisibility() == View.VISIBLE)
        {
            sideBarMenuContainer.setVisibility(View.GONE);
        } else if (viewPager2General.getCurrentItem() != 0)
        {
            viewPager2General.setCurrentItem(0, true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == AllConstants.RECORDING_REQUEST_CODE && grantResults.length > 0
                &&  grantResults[0] == PackageManager.PERMISSION_GRANTED){
//            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            activityResultLauncherForSelectImage.launch(intent);

            //Start Recording..

//            Toast.makeText(this, "accessing microphone " + grantResults[0], Toast.LENGTH_SHORT).show();
//
//            setUpRecording(fileNamePath);
//            mediaPlayer = new MediaPlayer();
//
//            try {
//                mediaRecorder.prepare();
//                mediaRecorder.start();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            editTextMessage.setVisibility(View.INVISIBLE);   //  hide msg edit text
//            emoji_IV.setVisibility(View.INVISIBLE);
//            camera_IV.setVisibility(View.INVISIBLE);
//            file_IV.setVisibility(View.INVISIBLE);
//            recordView.setVisibility(View.VISIBLE);     // show swipe mode

//        } else if (requestCode == AllConstants.WRITE_REQUEST_CODE && grantResults.length > 0
//                &&  grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(this, "write is granted", Toast.LENGTH_SHORT).show();

        }else {
            if(requestCode == AllConstants.RECORDING_REQUEST_CODE){
                Toast.makeText(mainActivityContext, "Go to phone app settings and permit Microphone", Toast.LENGTH_SHORT).show();
            }
//            else if (requestCode == AllConstants.WRITE_REQUEST_CODE) {
//                Toast.makeText(mainActivityContext, "write not granted", Toast.LENGTH_SHORT).show();
//            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        clearGlideCache();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearGlideCache();
    }
    

    //    File appSpecificFolder = new File(getExternalFilesDir(null), "MyFolder");
//    if (!appSpecificFolder.exists()) {
//        appSpecificFolder.mkdirs();
//    }

//    File appSpecificFolder = new File(getExternalFilesDir(null), "MyFolder");
//    "/storage/emulated/0/Android/data/com.your.package.name/files/MyFolder";
//
//    File appSpecificFolder = new File(getExternalCacheDir(), "MyFolder");
//    "/storage/emulated/0/Android/data/com.your.package.name/cache/MyFolder"



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

















