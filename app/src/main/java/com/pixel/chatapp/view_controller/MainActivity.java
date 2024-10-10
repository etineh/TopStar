package com.pixel.chatapp.view_controller;

import static com.pixel.chatapp.constants.K.gameHostUid;
import static com.pixel.chatapp.utilities.AnimUtils.slideOutToBottom;
import static com.pixel.chatapp.utilities.FileUtils.formatDuration;
import static com.pixel.chatapp.utilities.FileUtils.getFileName;
import static com.pixel.chatapp.utilities.FileUtils.isAudioFile;
import static com.pixel.chatapp.utilities.FileUtils.isCdrFile;
import static com.pixel.chatapp.utilities.FileUtils.isMsWordFile;
import static com.pixel.chatapp.utilities.FileUtils.isPdfFile;
import static com.pixel.chatapp.utilities.FileUtils.isPhotoFile;
import static com.pixel.chatapp.utilities.FileUtils.isPhotoshopFile;
import static com.pixel.chatapp.utilities.FileUtils.isVideoFile;
import static com.pixel.chatapp.utilities.FolderUtils.getVoiceNoteFolder;
import static com.pixel.chatapp.utilities.AnimUtils.animateVisibility;
import static com.pixel.chatapp.view_controller.calls.CallCenterActivity.handlerVibrate;
import static com.pixel.chatapp.constants.K.ACCEPTED_MIME_TYPES;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.pdf.PdfRenderer;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.biometric.BiometricPrompt;
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
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.gson.Gson;
import com.pixel.chatapp.services.NetworkChangeReceiver;
import com.pixel.chatapp.permission.AppPermission;
import com.pixel.chatapp.R;
import com.pixel.chatapp.adapters.ViewPagerMainAdapter;
import com.pixel.chatapp.constants.K;
import com.pixel.chatapp.services.api.model.outgoing.TwoValueM;
import com.pixel.chatapp.view_controller.calls.CallCenterActivity;
import com.pixel.chatapp.view_controller.calls.CallPickUpCenter;
import com.pixel.chatapp.view_controller.games.whot.WhotGameActivity;
import com.pixel.chatapp.view_controller.games.whot.WhotLandscapeActivity;
import com.pixel.chatapp.dataModel.AwaitPlayerM;
import com.pixel.chatapp.dataModel.SignalPlayerM;
import com.pixel.chatapp.services.api.dao_interface.GameAPI;
import com.pixel.chatapp.services.api.model.incoming.AssetsModel;
import com.pixel.chatapp.services.api.model.incoming.ResultApiM;
import com.pixel.chatapp.services.api.model.outgoing.GameSignalM;
import com.pixel.chatapp.view_controller.games.AwaitPlayersActivity;
import com.pixel.chatapp.view_controller.games.whot.WhotOptionActivity;
import com.pixel.chatapp.view_controller.fragments.PlayersFragment;
import com.pixel.chatapp.interface_listeners.ChatListener;
import com.pixel.chatapp.interface_listeners.TriggerOnForward;
import com.pixel.chatapp.interface_listeners.WalletCallBack;
import com.pixel.chatapp.services.notification.NotificationHelper;
import com.pixel.chatapp.services.notification.ReplyReceiver;
import com.pixel.chatapp.view_controller.photos_video.ViewImageActivity;
import com.pixel.chatapp.utilities.AnimUtils;
import com.pixel.chatapp.utilities.CacheUtils;
import com.pixel.chatapp.utilities.ChatUtils;
import com.pixel.chatapp.utilities.GameUtils;
import com.pixel.chatapp.utilities.IdTokenUtil;
import com.pixel.chatapp.utilities.ProfileUtils;
import com.pixel.chatapp.utilities.NumberSpacing;
import com.pixel.chatapp.utilities.OpenActivityUtil;
import com.pixel.chatapp.utilities.Photo_Video_Utils;
import com.pixel.chatapp.services.api.dao_interface.WalletListener;
import com.pixel.chatapp.utilities.FetchContacts;
import com.pixel.chatapp.view_controller.fragments.ChatsFragment;
import com.pixel.chatapp.interface_listeners.CallListenerNext;
import com.pixel.chatapp.interface_listeners.CallsListener;
import com.pixel.chatapp.dataModel.UserOnChatUI_Model;
import com.pixel.chatapp.view_controller.peer2peer.exchange.P2pExchangeActivity;
import com.pixel.chatapp.view_controller.photos_video.SendImageOrVideoActivity;
import com.pixel.chatapp.view_controller.photos_video.ZoomImage;
import com.pixel.chatapp.view_controller.photos_video.CameraActivity;
import com.pixel.chatapp.repositories.CallRepository;
import com.pixel.chatapp.utilities.CallUtils;
import com.pixel.chatapp.utilities.FileUtils;
import com.pixel.chatapp.utilities.PhoneUtils;
import com.pixel.chatapp.adapters.MessageAdapter;
import com.pixel.chatapp.constants.DataModelType;
import com.pixel.chatapp.interface_listeners.FragmentListener;
import com.pixel.chatapp.interface_listeners.NewEventCallBack;
import com.pixel.chatapp.dataModel.CallModel;
import com.pixel.chatapp.dataModel.EditMessageModel;
import com.pixel.chatapp.dataModel.MessageModel;
import com.pixel.chatapp.dataModel.PinMessageModel;
import com.pixel.chatapp.services.roomDatabase.viewModels.UserChatViewModel;
import com.pixel.chatapp.view_controller.side_bar_menu.dashboard.DashboardActivity;
import com.pixel.chatapp.view_controller.side_bar_menu.premium.PremiumActivity;
import com.pixel.chatapp.view_controller.side_bar_menu.settings.SettingsActivity;
import com.pixel.chatapp.view_controller.side_bar_menu.support.SupportActivity;
import com.pixel.chatapp.view_controller.side_bar_menu.wallet.WalletActivity;
import com.pixel.chatapp.view_controller.signup_login.EmailOrPhoneLoginActivity;
import com.pixel.chatapp.view_controller.signup_login.LinkNumberActivity;
import com.pixel.chatapp.view_controller.signup_login.PhoneLoginActivity;
import com.pixel.chatapp.view_controller.signup_login.ResetAccountActivity;
import com.pixel.chatapp.view_controller.signup_login.SetUpProfileActivity;
import com.pixel.chatapp.utilities.UserChatUtils;
import com.pixel.chatapp.utilities.WalletUtils;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiPopup;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements FragmentListener, CallListenerNext, ChatListener, TriggerOnForward {

    private static TabLayout tabLayoutGeneral;
    private static ViewPager2 viewPager2General;
    int close = 0;

    ViewStub moreOptionHomeLayout;
    View moreOptionHomeView;
    ConstraintLayout moreHomeCont2;

    
    //  ---------   SideBar Menu     -----------------
    private ImageView imageViewUserPhoto, sideBarMenuClose;

    private ViewStub sideBarMenuContainer;
    private View sideBarView;
    CardView cardViewWallet;

    private TextView textLightAndDay, textViewDisplayName, textViewUserName, tapImage_TV, phoneNumber_TV, hint_TV;
    ImageView copyNumberIV, copyUserNameIV;

    Switch darkMoodSwitch;
    ProgressBar progressBarMood;
    CardView cardViewSettings, dashboard, premium, advertise, invite, support;
    String imageLink;

    //  =============   last home button layout
    View homeLastViews;

    //  =============   selectGame button layout
    View selectGameView;
    ImageView closePageIV, whotButton, chessButton, pokerButton, scrabbleButton, riddleButton, diceButton;


    //  ---------   sharepreference     -----------------
    public static SharedPreferences moodPreferences, myUserNamePreferences, lastPositionPreference,
            offlineChat, documentIdShareRef, voiceNoteIdShareRef, unusedPhotoShareRef, myProfileShareRef,
            resetLoginSharePref, contactNameShareRef, deviceFirstLoginRef, otherUserFcmTokenRef, otherUserHintRef,
            gameSharePref;
    public static String getMyUserName;
    public static Boolean nightMood;

    private final MainActivity mainActivityContext = MainActivity.this;

    public MainActivity getMainActivityContext() {
        return mainActivityContext;
    }

    //    ------- message/chat declares
    public static CircleImageView circleImageLogo;
    public static ConstraintLayout typeMsgContainer;
    private ImageView openChatMenu_IV, arrowBack;
    View firstTopChatViews;
    private static ConstraintLayout mainViewConstraint, topMainContainer;
    private ImageView editOrReplyIV;
    public static TextView textViewOtherUser, textViewLastSeen, textViewMsgTyping, textViewReplyOrEdit, nameReply, replyVisible, bioHint_TV;
    private View deleteForWhoView;
    private TextView deleteChatForOnlyOther_TV;

    //  --------    Chat Box Menu Declares
    private View chatMenuViews;
    ScrollView scrollViewMenu;

    private static ImageView emoji_IV, file_IV, camera_IV, gameMe_IV;

    View topEmojiView, onChatClickView;
    ImageView pinIcon, editOrShareIcon;
    TextView pinChatTV, editOrShareTV;
    ConstraintLayout editLayout, animateView;

    // -----------      pin declares
    private static ConstraintLayout pinIconsContainer, pinMsgBox_Constr;
    public static ConstraintLayout line, chatContainer;
    View pinForWhoViews;
//    public static View pinChatViews;
    private ConstraintLayout pinMsgConst;
    @SuppressLint("StaticFieldLeak")
    private static ImageView hidePinMsg_IV, pinClose_IV, pinPrivateIcon_IV, pinLockPrivate_IV, pinPublicIcon_IV, pinLockPublic_IV, closePinBox_TV;
    private int pinNextPublic, pinNextPrivate, pinScrollPrivate, pinScrollPublic;
    private String msgId, message, pinByWho, pinStatus = "null", chatNotFoundID = "null";
    private final String PRIVATE = "private";
    private final String PUBLIC = "public";
    private Object timeStamp;
    private ImageView arrowUp, arrowDown;
    private static ImageView cancelPinOption;
    private static TextView totalPinPrivate_TV, pinCount_TV, pinMsg_TV, totalPinPublic_TV, newPinIndicator_TV;
    public static TextView pinMineTV, pinEveryoneTV, pinByTV;


    //  ---------       Forward chat declares
    private View onForwardTopView;
    private ConstraintLayout forwardDownContainer;
    private TextView titleTV_;
    public static TextView totalUser_TV;
    public static int selectCount;
    public static CircleImageView circleForwardSend;
    private static ProgressBar progressBarForward;
    public static boolean onForward;
    public static boolean onSelectPlayer;
    public static boolean onSelectNewPlayer;

    private View addPlayerConfirmView;
    private TextView titleNoticeTV;
    public static boolean onUserLongPress;
    public static List <String> forwardChatUserId;
    public static List <String> selectedUserNames;
    public static List <AwaitPlayerM> selectedPlayerMList;
    public static List <AwaitPlayerM> newPlayerMList;


    //  ---------   Delete User from ChatList Declares
    private View deleteUserOrClearChatViews;
    private TextView deleteUserForMe_TV, deleteUserForAll_TV, otherUserName_TV;
//    private String otherUid_Del;
    List<UserOnChatUI_Model> userModelList;

    //  ----------------

    private View incomingGameView, joinGameViews;
    private TextView gameBalTV, notice_TV;

    private static EditText editTextMessage, et_emoji;

    private static CircleImageView sendMessageButton;
    private static CardView cardViewMsg, cardViewReplyOrEdit;
    public static ImageView scrollPositionIV, sendIndicator;
    public static TextView scrollCountTV, receiveIndicator;
    private static ProgressBar progressBarLoadChats;

    //  -------------   network settings    -----------
    public static ViewStub constrNetConnect;
    public static View checkNetworkView;
    public static String otherUserUid, otherUserName, myUserName, imageUri;
    private CallModel callModel;
    private String callType_;
    public static Handler handlerInternet, handlerTyping;
    public static Runnable internetCheckRunnable, runnableTyping;

    public static int goToNum;
    public static Boolean goToLastMessage = false;

    //  -------------- database ------------
    public static UserChatViewModel chatViewModel;
    private static MessageModel editChatModel, callChatModel, gameModel;
    public static MessageModel missCallModel;

    private static DatabaseReference refMessages, refMsgFast, refLastDetails, refChecks,
            refEditMsg, refDeleteMsg, refPrivatePinChat, refPublicPinChat, refClearSign,
            refDeleteUser, refDeletePin, refEmojiReact, refOnReadRequest, refChatIsRead, refCalls,
            refGameAlert, refGameStarts, refWallet;

    public static DatabaseReference refUsers;

    private ValueEventListener chatReadListener, typingValueListener, lastSeenValueListener; // Declare the listener as a class variable

//    public static FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    public static FirebaseUser user;

    public static int scrollNum = 0, chatDeliveryStatus = 700033;
    private static String idKey, listener = "no", replyFrom, replyText;
    public static boolean insideChat = false;     // for checking when user wants to send photo from other app
    public static String networkListener = "no";
    public static Boolean networkOk = true, networkTypingOk = true;


    //  --------   voice note declares
    private String fileNamePath;
    private MediaRecorder mediaRecorder;
    private static MediaPlayer mediaPlayer;
    private final AppPermission appPermissionCheck = new AppPermission();
    private static RecordView recordView;
    private static RecordButton recordButton;
    private static ConstraintLayout constraintMsgBody;

    Handler handler = new Handler(Looper.getMainLooper());

    //  --------   chat Options declares
    public static View chatOptionView;
    ImageView forwardChatOption_IV, copyChatOption_IV, deleteChatOption_IV, cancelChatOption_IV;

    public static ConstraintLayout generalBackground;
    
    ViewStub moreOptionViewStub;
    View moreOptionViews;
    @SuppressLint("StaticFieldLeak")
    public static TextView chatSelected_TV, pinTV, saveToGalleryTV, reportTV;

    @SuppressLint("StaticFieldLeak")
    public static ImageView replyChatOption_IV, editChatOption_IV, moreOption_IV;

    private TextView verifyLoadTV;
    private ProgressBar verifyProgressBar;

    public static MessageModel modelChatsOption;
    public static int chatPosition;
    public static List<MessageModel> chatModelList;
    public static boolean onShare;
    
    //  checks declares
    public static int downMsgCount, readDatabase;// 0 is read, 1 is no_read
    public static boolean loadMsg, isKeyboardVisible = false, isSendingFile = false, isSendingVoiceNote = false;
    private Boolean clearOnlyChatHistory = false, isEmojiVisible = false, clearHighLight = false,
            fiveSecondsWait = false;

    //  -----------     All Maps declares
    public static Map<String, List<PinMessageModel>> pinPrivateChatMap, pinPublicChatMap;
    private Map<String, Object> editMessageMap;
    private Map<String, Integer> dateNum, dateMonth;
    private Map<String, Object> deleteMap;
    public static Map<String, Long> getLastTimeChat;
    public static Map<String, MessageAdapter> adapterMap; // to prevent it from re-creating all the time
    public static Map<String, RecyclerView> recyclerMap;
    public static Map<String, Boolean> loopOnceMap;

    private static List<String> otherUidList;
    public static Map<String, Object> downMsgCountMap, scrollNumMap;
    private static Map<String, Boolean>  userRecyclerActiveMap;
    public static Map<String, Boolean> insideChatMap;
    public static Map<String, List<MessageModel>>  photoAndVideoMap;    // add all photo and video with their key and position in chat
    public static Map<String, Integer>  filePositionMap;

    public static ConstraintLayout recyclerContainer;
    private ConstraintLayout constraintLayoutAdjust;

    private NetworkChangeReceiver networkChangeReceiver;
    int currentImageResource = R.drawable.baseline_add_reaction_24; // Initialize with the default image resource

    //  ------- emoji declares
    private EmojiPopup popup;
    private Handler handlerEmoji = new Handler();

    private int clearNumb = 0;
    private String chatID;
    private Runnable emojiRunnable;

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
    public static boolean isSharingDocument;

    private List<MessageModel> photoModelList;

    //  ---------- msg end

    private ActivityResultLauncher<String[]> pickDocumentLauncher;
    private ActivityResultLauncher<Intent> pickMultipleAudioLauncher;


    //  ----------  file option declare
    private ConstraintLayout fileContainerAnim;
    private ViewStub fileAttachOptionContainer;
    private View fileOptionViews;


    //  ============    wallet pin verify   =======================
    ViewStub walletVerifyLayout;
    View walletVerifyView;
    ConstraintLayout pinContainerHome, pinOptionContainer;
    ImageView cancelPinOption_IV, fingerprintIcon;
    TextView or_TV, openPinBox_TV, verifyViaTV, openWalletButton, forgetPin_Button;
    EditText enterAckPin_ET;


    //  =============   video and audio call declares
    Gson gson = new Gson();
    private static CallRepository callRepository;

    private View incomingCallView;
    private static ConstraintLayout returnToCallLayout;

    private static ViewStub audioOrVideoOptionConatiner;
    private View audioOrVideoView;
    private static TextView returnToCallWithDuration;
    private static TextView whoIsCallingTV;
    private static ImageView answerCall_IV, rejectCall_IV;
    private static ImageView callButton;
    private static CallUtils callUtils;

    private static int run = 0;
    public static boolean onPictureMood;
    private boolean makeCall;
    public static int activeOnCall = 0;
    public static String goBackToCall;
    
    private static String currentUserUidOnCall;

    public static Handler handlerOnAnotherCall;
    public static Runnable runnableOnAnotherCall;

    public static CallsListener callsListener;

    private Executor executor;
    private final Executor executorSendDB = Executors.newSingleThreadExecutor();

    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;


    // card views to messageAdapter
    public static List<View> viewCacheSend; // List to store views for caching
    public static List<View> viewCacheReceive; // List to store views for caching
    public static List<View> viewPhotoSend; // List to store views for caching
    public static List<View> viewPhotoReceive; // List to store views for caching

    public static int isOnLongClick = 0;

    public static int newChatNumberPosition = 0;

    public static List<UserOnChatUI_Model> allUsersFromRoom;

    // =============    onclick
    View.OnClickListener openWallet, onEdit, onPin, onCopy, forwardChat, onDelete;

    //  ====== onUserLongPress
    View onUserLongPressView, onUserMoreLongPressView;
    TextView count_TV, viewProfile_TV;
    ConstraintLayout userLongPressMoreSub;
    ImageView deleteUser_IV, pinUser_IV;

    boolean deviceOnNightMode;


    //  ====== onGame
    String gameMode, stakeAmount, hostNote, totalStake, numberOfPlayers, hostUid, gameID, myGameKey = "344";
    String newPlayerUid;
    TextView expectedPlayerNumTV, hostByTV, gameModeTV, gameTypeTV, stakeAmountTV, rewardPoolTV, hostNoteTV;
    ConstraintLayout minimizedContainer;
    TextView minimizedTV;
    boolean addNewPlayer;
    public static boolean targetPlayer;
    CardView signalCardView;
    public static boolean onGameNow = false;
    private boolean doneSelectingPlayers = false;
    private boolean onAwaitActivity = false;
    public static boolean isOnGameNow = false;
    private boolean hostRelaunchApp = false;
    private boolean iRelaunchApp = false;
    private List<AwaitPlayerM> playersInGameList;
    private boolean hostReopen = false;
    private String whichGameActivity_ = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        deviceOnNightMode = (nightModeFlags == Configuration.UI_MODE_NIGHT_YES);

        // Dark mood setting
        moodPreferences = this.getSharedPreferences("MOOD", Context.MODE_PRIVATE);
        nightMood = moodPreferences.getBoolean("MoodStatus", false);

        if(nightMood){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);     //  activate view
        setContentView(R.layout.activity_main);

        user = FirebaseAuth.getInstance().getCurrentUser();

        // local variables
        ImageView sideBarMenuOpen, imageViewLogo;

        ImageView viewWalletIcon = findViewById(R.id.viewWalletIcon);
        et_emoji = findViewById(R.id.et_emoji);

        //      ==============   chatContainer

        constraintMsgBody = findViewById(R.id.chatBodyLayout);
        chatContainer = findViewById(R.id.chatBoxContainer);
        constraintLayoutAdjust = findViewById(R.id.constraintLayoutAdjust);
        recyclerContainer = findViewById(R.id.constraintRecyler);
        editTextMessage = findViewById(R.id.editTextMessage9);
        sendMessageButton = findViewById(R.id.fab9);
        typeMsgContainer = findViewById(R.id.typeMsgContainer);
        progressBarLoadChats = findViewById(R.id.progressBarLoadChats);

        // audio swipe button ids   --  voice note
        recordView = (RecordView) findViewById(R.id.record_view9);
        recordButton = (RecordButton) findViewById(R.id.record_button9);
        recordButton.setRecordView(recordView);

        // documents ids
        emoji_IV = findViewById(R.id.emoji_IV);
        file_IV = findViewById(R.id.files_IV);
        camera_IV = findViewById(R.id.camera_IV);
        gameMe_IV = findViewById(R.id.playGame_IV);

        // reply and edit settings
        cardViewReplyOrEdit = findViewById(R.id.cardViewReply9);
        textViewReplyOrEdit = findViewById(R.id.textViewReplyText9);
        ImageView replyOrEditCancel_IV = findViewById(R.id.imageViewCancleReply9);
        editOrReplyIV = findViewById(R.id.editOrReplyImage9);
        nameReply = findViewById(R.id.fromTV9);
        replyVisible = findViewById(R.id.textReplying9);

        // scroll position and network ids
        scrollPositionIV = findViewById(R.id.scrollToPositionIV);
        scrollCountTV = findViewById(R.id.scrollCountTV);
        receiveIndicator = findViewById(R.id.receiveIndicatorTV);
        sendIndicator = findViewById(R.id.sendIndicatorIV);

        // card views to messageAdapter
        viewCacheSend = new ArrayList<>(); // List to store views for caching
        viewCacheReceive = new ArrayList<>(); // List to store views for caching
        viewPhotoSend = new ArrayList<>(); // List to store views for caching
        viewPhotoReceive = new ArrayList<>(); // List to store views for caching

        // calls
        returnToCallLayout = findViewById(R.id.returnBackToCAll);
        returnToCallWithDuration = returnToCallLayout.findViewById(R.id.tapInfoTV);


        //  backgrounLoad Ids
        generalBackground = findViewById(R.id.generalBackground);
        verifyProgressBar = findViewById(R.id.verifyProgressBar);
        verifyLoadTV = findViewById(R.id.verifyLoadTV);

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
        refCalls = FirebaseDatabase.getInstance().getReference("Calls");
        refGameAlert = FirebaseDatabase.getInstance().getReference("GameAlert");
        refGameStarts = FirebaseDatabase.getInstance().getReference("GameStarts");
        refWallet = FirebaseDatabase.getInstance().getReference("WalletClient");

        // initialise room database
        chatViewModel = new ViewModelProvider.AndroidViewModelFactory(getApplication())
                .create(UserChatViewModel.class);

        pinPrivateChatMap = new HashMap<>();
        pinPublicChatMap = new HashMap<>();
        deleteMap = new HashMap<>();
        getLastTimeChat = new HashMap<>();
        editMessageMap = new HashMap<>();
        userRecyclerActiveMap = new HashMap<>();
        scrollNumMap = new HashMap<>();
        photoAndVideoMap = new HashMap<>();
        filePositionMap = new HashMap<>();
        downMsgCountMap = new HashMap<>();
        insideChatMap = new HashMap<>();
        otherUidList = new ArrayList<>();
        userModelList = new ArrayList<>();
        chatModelList = new ArrayList<>();
        adapterMap = new HashMap<>();
        recyclerMap = new HashMap<>();
        readDatabase = 0;  // 0 is read, 1 is no_read
        loopOnceMap = new HashMap<>();
        handlerInternet = new Handler();
        handlerTyping = new Handler();
        allUsersFromRoom = new ArrayList<>();
        playersInGameList = new ArrayList<>();

        // pins
        pinNextPublic = 1;
        pinNextPrivate = 1;
        pinScrollPrivate = 0;
        pinScrollPublic = 0;

        //  forward
        forwardChatUserId = new ArrayList<>();
        selectedUserNames = new ArrayList<>();
        selectedPlayerMList = new ArrayList<>();
        newPlayerMList = new ArrayList<>();
        selectCount = 0;
        loadMsg = true;

        getOnBackPressedDispatcher().addCallback(this, callback);

        // video and audio calls
        handlerOnAnotherCall = new Handler();

        // passing life to interface
        CallCenterActivity.callListenerNext = this;
        CallPickUpCenter.callListenerNext = this;
        NotificationHelper.listener = this;
        NotificationHelper.homeContext = this;
        NotificationHelper.homeActivity = this;
        WhotOptionActivity.TriggerInterface.triggerOnForward = this;
        RedirectHome.triggerOnForward = this;
        ViewImageActivity.triggerOnForward = this;


        goBackToCall = getString(R.string.returnToCall);
        callRepository = CallRepository.getInstance();

        // XXX  =================   get intent when file is share from other app    ================
        sharingPhotoActivated = getIntent().getBooleanExtra("isSharing", false);
        photoModelList = (List<MessageModel>) getIntent().getSerializableExtra("photoModel");   // get all the photos model and add it to chatList

        hideKeyboard();

        // Create an executor
        executor = ContextCompat.getMainExecutor(this);

        callUtils = new CallUtils(this);

        resetLoginSharePref = this.getSharedPreferences(K.RESET_LOGIN, Context.MODE_PRIVATE);
        boolean isResetMood = user != null && resetLoginSharePref.getBoolean(user.getUid(), false);

        deviceFirstLoginRef = this.getSharedPreferences(K.DEVICEFIRSTLOGIN, Context.MODE_PRIVATE);
        boolean isFirstTimeLogin = deviceFirstLoginRef.getBoolean(K.FIRSTTIME, true);

        if(user == null){

            if(isFirstTimeLogin){  // this is user first time login
                startActivity(new Intent(MainActivity.this, PhoneLoginActivity.class));
            } else {    // the phone already has a previous login
                startActivity(new Intent(MainActivity.this, EmailOrPhoneLoginActivity.class));
            }
            finish();

        } else if ( (user.getEmail() != null && user.getEmail().isEmpty()) || user.getEmail() == null)
        {
            // redirect to setup account
            startActivity(new Intent(MainActivity.this, SetUpProfileActivity.class));
            finish();

//FirebaseAuth auth = FirebaseAuth.getInstance();
//auth.signOut();

        } else if ((user.getPhoneNumber() != null && user.getPhoneNumber().isEmpty()) || user.getPhoneNumber() == null)
        {
            IdTokenUtil.generateToken(token -> {
                System.out.println("what is token " + token);
            }, this);

            startActivity(new Intent(MainActivity.this, LinkNumberActivity.class));
            Toast.makeText(this, getString(R.string.linkPhoneNumber), Toast.LENGTH_SHORT).show();
            finish();

        } else if (isResetMood)
        {
            startActivity(new Intent(MainActivity.this, ResetAccountActivity.class));
            finish();
        } else{

            deviceFirstLoginRef.edit().putBoolean(K.FIRSTTIME, false).apply();

            ReplyReceiver.chatListener = this;

            IdTokenUtil.generateToken(token -> {
                System.out.println("what is token " + token);
            }, this);

            myId = user.getUid();

            lastPositionPreference = this.getSharedPreferences(K.SCROLLPOSITION, Context.MODE_PRIVATE);
            gameSharePref = getSharedPreferences(K.GameIds, Context.MODE_PRIVATE);

            // store new username if another new user login
            myUserNamePreferences = this.getSharedPreferences(K.MYUSERNAME, Context.MODE_PRIVATE);
            getMyUserName = myUserNamePreferences.getString(K.USERNAME, null);
            refUsers.child(myId).child("general").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    String fetchUserName = snapshot.child("userName").getValue().toString();

                    if(getMyUserName == null || (getMyUserName != null && !fetchUserName.equals(getMyUserName)) ){
                        myUserNamePreferences.edit().putString(K.USERNAME, fetchUserName).apply();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            incomingGameObserver();

            // store failed chat in local sharePreference
            offlineChat = this.getSharedPreferences(K.OFFLINECHAT, Context.MODE_PRIVATE);
            // store each photo with their user uid
            documentIdShareRef = getSharedPreferences(K.PHOTO_OTHERUID, Context.MODE_PRIVATE);
            voiceNoteIdShareRef = getSharedPreferences(K.VOICENOTE_UID, Context.MODE_PRIVATE);
            if(user != null) myProfileShareRef = getSharedPreferences(user.getUid(), Context.MODE_PRIVATE);

            // for other user details
            contactNameShareRef = getSharedPreferences(K.CONTACTNAME, Context.MODE_PRIVATE);
            otherUserFcmTokenRef = getSharedPreferences(K.FCMTOKEN, Context.MODE_PRIVATE);
            otherUserHintRef = getSharedPreferences(K.OTHERUSERHINT, Context.MODE_PRIVATE);

            // Register the NetworkChangeReceiver to receive network connectivity changes
            new Handler().postDelayed(()-> {
                networkChangeReceiver = new NetworkChangeReceiver(this);
                registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

                // manually call and check for the network
                internetCheckRunnable = () -> {
                    networkChangeReceiver.onReceive(MainActivity.this,
                            new Intent(ConnectivityManager.CONNECTIVITY_ACTION));

                    // Repeat the network check everything 3 sce till network is okay
                    handlerInternet.postDelayed(internetCheckRunnable, 3000);
                };
            }, 2000);


            handlerInternet.post(internetCheckRunnable);

            // check internet every 15sec only while typing
            runnableTyping = () -> {
                if(networkListener.equals("yes")) {
                    handlerInternet.post(internetCheckRunnable);
                }
                handlerTyping.postDelayed(runnableTyping, 15_000);
            };

            ViewPagerMainAdapter adapterV = new ViewPagerMainAdapter(getSupportFragmentManager(), getLifecycle());

            viewPager2General.setAdapter(adapterV);

            TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayoutGeneral, viewPager2General, true, true,
                    (tab, position) -> {

                        switch (position){
                            case 0:
                                tab.setText(getString(R.string.players));
                                break;
                            case 1:
                                tab.setText(getString(R.string.chats));
                                break;
                            case 2:
                                tab.setText(getString(R.string.league));
                                break;
                            case 3:
                                tab.setText(getString(R.string.tour) + " 🏆");
                                break;
                        }
                    });
            tabLayoutMediator.attach();

            tabListener();

            // set my online presence to be true
            refUsers.child(myId).child("general").child("presence").setValue(1);

            returnToCallLayout.setOnClickListener(v -> {
                makeCall(null);
            });


            //  =============   chats containers    onClicks

            onEdit = view -> {
                view.animate().scaleX(1.1f).scaleY(1.1f).setDuration(30)
                        .withEndAction(() ->
                        {
                            if(firstTopChatViews == null) setFirstTopChatViews();     // onEdit()

                            // share
                            if (onShare){

                                Photo_Video_Utils.shareImageUsingContentUri(this, modelChatsOption, null, null);

                                firstTopChatViews.setVisibility(View.VISIBLE);
                                chatOptionView.setVisibility(View.GONE);

                            } else {
                                onEditOrReplyMessage_(modelChatsOption,"edit", "editing...",
                                        android.R.drawable.ic_menu_edit);
                            }

                            // Reset the scale
                            view.setScaleX(1.0f);
                            view.setScaleY(1.0f);

                            generalBackground.setVisibility(View.GONE);
                            if(moreOptionViews != null) moreOptionViews.setVisibility(View.GONE);
                            if(topEmojiView != null) AnimUtils.fadeOutGone(topEmojiView, 500);
                            if(onChatClickView != null) onChatClickView.setVisibility(View.GONE);
                        }).start();

                clearAllHighlights();

            };

            onPin = view -> {
                view.animate().scaleX(1.1f).scaleY(1.1f).setDuration(40)
                        .withEndAction(() -> {

                            onPinData(modelChatsOption.getIdKey(), modelChatsOption.getMessage(), ServerValue.TIMESTAMP);

                            // Reset the scale
                            view.setScaleX(1.0f);
                            view.setScaleY(1.0f);

                            pinIconsVisibility(otherUserUid);
                            generalBackground.setVisibility(View.GONE);
                            moreOptionViews.setVisibility(View.GONE);
                            if(onChatClickView != null) onChatClickView.setVisibility(View.GONE);

                        }).start();
            };

            onCopy = view -> {

                view.animate().scaleX(1.1f).scaleY(1.1f).setDuration(50)
                        .withEndAction(() -> {

                            copyChats();

                            // Reset the scale
                            view.setScaleX(1.0f);
                            view.setScaleY(1.0f);

                            pinIconsVisibility(otherUserUid);
                            generalBackground.setVisibility(View.GONE);
                            moreOptionViews.setVisibility(View.GONE);
                            if(topEmojiView != null) AnimUtils.fadeOutGone(topEmojiView, 500);
                            if(onChatClickView != null) onChatClickView.setVisibility(View.GONE);

                        }).start();

            };

            forwardChat = view -> {

                view.animate().scaleX(1.1f).scaleY(1.1f).setDuration(50)
                        .withEndAction(() ->
                        {
                            setForwardChat();

                            // Reset the scale
                            view.setScaleX(1.0f);
                            view.setScaleY(1.0f);

                            generalBackground.setVisibility(View.GONE);
                            moreOptionViews.setVisibility(View.GONE);
                            if(topEmojiView != null) AnimUtils.fadeOutGone(topEmojiView, 500);
                            if(onChatClickView != null) onChatClickView.setVisibility(View.GONE);

                        }).start();

                // call runnable to check for network
                handlerTyping.post(runnableTyping);

            };

            onDelete = view -> {

                view.animate().scaleX(1.1f).scaleY(1.1f).setDuration(50)
                        .withEndAction(() -> {

                            onDeleteMessage();

                            // Reset the scale
                            view.setScaleX(1.0f);
                            view.setScaleY(1.0f);

                            pinIconsVisibility(otherUserUid);
                            generalBackground.setVisibility(View.GONE);
                            moreOptionViews.setVisibility(View.GONE);
                            if(topEmojiView != null) AnimUtils.fadeOutGone(topEmojiView, 500);
                            if(onChatClickView != null) onChatClickView.setVisibility(View.GONE);

                        }).start();

            };

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
            replyOrEditCancel_IV.setOnClickListener(view ->{
                if(replyFrom == null) editTextMessage.setText("");
                clearInputFields(true);
            });

            // ============ scroll to previous position of reply message
            scrollPositionIV.setOnClickListener(view -> {

                view.animate().scaleX(1.2f).scaleY(1.2f).withEndAction(()->
                {
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
                    view.setScaleX(1.0f);
                    view.setScaleY(1.0f);
                }).start();

            });

            activityDocumentLauncher();
            initialiseMultipleAudioPicker();

            // open set files(documents) -> camera - photo - documents
            file_IV.setOnClickListener(view -> {
                setFileOptionViews();
                fileOptionViews.setVisibility(View.VISIBLE);
                AnimUtils.slideInFromBottom(fileContainerAnim, 100);

            });

            // send message
            View.OnClickListener sendChat = view -> {

                MessageAdapter adapter = adapterMap.get(otherUserUid);
                assert adapter != null;
                if (adapter.getItemCount() == 0){
                    ChatUtils.addEmptyChatCard(otherUserUid, Objects.requireNonNull(adapterMap.get(otherUserUid)));     // mine sending
                }

                String message = editTextMessage.getText().toString().trim();
                editTextMessage.setText(null);

                new Handler().postDelayed(()->
                {
                    if (!message.isEmpty()) {
                        //  0 is text, 1 is voice note, 2 is photo, 3 is document, 4 is audio (mp3), 5 is video
                        if (containsOnlyEmojis(message)) {
                            // send as emoji text to increase the size
                            sendMessage(null, message, 0, null, null, otherUserUid, true);

                        } else {// Send as normal text
                            sendMessage(message, null, 0, null, null, otherUserUid, true);
                        }

                    }

                    // clear notification bar
                    NotificationHelper.clearMessagesForUser(otherUserUid, this);

                }, 300);

                endTyping();
//                clearInputFields();
            };
            sendMessageButton.setOnClickListener(sendChat);
            typeMsgContainer.setOnClickListener(sendChat);

            gameMe_IV.setOnClickListener(view -> {
                view.animate().scaleX(1.2f).scaleY(1.2f).withEndAction(()->
                {
                    targetUserOnGame();

                    new Handler().postDelayed(()-> {
                        view.setScaleX(1f);
                        view.setScaleY(1f);
                    }, 300);
                });
            });

            camera_IV.setOnClickListener(view -> {
                // call runnable to check for network
                handlerTyping.post(runnableTyping);

                view.animate().scaleX(1.2f).scaleY(1.2f).setDuration(30).withEndAction(()->
                {
                    selectedUserNames.clear();
                    forwardChatUserId.clear();
                    chatModelList.clear();
                    isSendingFile = true;
                    selectedUserNames.add(otherUserName);
                    forwardChatUserId.add(otherUserUid);
                    startActivity(new Intent(this, CameraActivity.class));

                    new Handler().postDelayed(()-> {
                        view.setScaleX(1f);
                        view.setScaleY(1f);
                    }, 500);
                });
            });



            //  ==================      side bar menu option     ===========================


            sideBarMenuOpen.setOnClickListener(view -> {    // open the side bar menu option
                if(sideBarView == null) activateSideBarViews(); // onMenu click
                AnimUtils.slideInFromRight(sideBarView, 120);
            });

            // open menu option via logo too
//            imageViewLogo.setOnClickListener(view -> {
//                if(sideBarView == null){
//                    activateSideBarViews();
//                }
//                sideBarView.setVisibility(View.VISIBLE);
//            });


            // =========    wallet finger print    ==============

            openWallet = v ->
            {
                if(walletVerifyView == null ) setWalletVerifyViews();
                // check if user is new -- not created pin yet
                Intent intent = new Intent(this, CreatePinActivity.class);
//                startActivity(intent);

                // move on to fingerprint if pin match (maybe it request pin only after 10min delay after first open)
                walletVerifyView.setVisibility(View.VISIBLE);
                pinOptionContainer.setVisibility(View.GONE);
                showFingerPrint();

            };

            viewWalletIcon.setOnClickListener(openWallet);


            devicePermission(); // fetch contactList and request for all permission

//            new Thread(this::readContactFromFile).start();
            FetchContacts.contactListFile = FetchContacts.readContactFromFile(this);

            new Handler().postDelayed(()->
            {
                AnimUtils.animateView(setHomeLastViews());
                setUserDetails();
                setFirstTopChatViews();     // onCreate()
                setChatOptionView();

                if(sideBarView == null) {   //  activate long press chat options
                    new Handler().postDelayed(this::activateSideBarViews, 3000);
                }

            }, 5000);

            if( (!deviceOnNightMode && nightMood) || (deviceOnNightMode && !nightMood)) {
                new Handler().postDelayed(this::activateAllViews, 30_000);
//                System.out.println("what is done");
            }

            fiveSecondsDelay(); // for network check too
            swipeReply();
            new Handler().postDelayed(()-> deleteUnusedPhotoFromSharePrefsAndAppMemory(this), 5000);

            // call the method last so as to give room for all user list to finish load
            if(sharingPhotoActivated){
                setForwardChat();
                chatModelList.addAll(photoModelList);
            }

            allUsersFromRoom = UserChatUtils.getAllUsersFromRoom(this);

            checkIfGameHasStarted();

        }


        adjustRecyclerViewToPhoneScreen();

        testingApi();

        // Handle the intent if the activity is created for the first time
        handleIntent(getIntent());

//        Picasso.get().load("content://media/external/images/media/1000124641").into(imageViewLogo);

//        imageViewLogo.setImageURI(Uri.parse("content://media/external/images/media/1000124641"));
// /external/images/media/1000123946
        //  content://media/external/images/media/1000124641


        // lazy loading
//        activateAudioOrVideoOptionView();
//        setChatOptionView()
//        activateWalletVerifyViews()

    }       //      ============== create

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Update the intent
        handleIntent(intent); // Handle the new intent
    }

    private void handleIntent(Intent intent)
    {
        // Check if the activity was started with the notification intent
        if (intent != null && intent.hasExtra("otherUid")) {
            String otherUid = intent.getStringExtra("otherUid");
            String title = intent.getStringExtra("title");
//            String body = intent.getStringExtra("body");

            if (otherUid != null && recyclerMap.get(otherUid) != null) {
                chatBodyVisibility(title, null, MainActivity.getMyUserName, otherUid, this, recyclerMap.get(otherUid));
                getLastSeenAndOnline(otherUid, this);
                msgBackgroundActivities(otherUid);
                callAllMethods(otherUid, this, this, true);
            }

        } else {
            assert intent != null;
            if (intent.hasExtra("gameMode")) {

                if(intent.getBooleanExtra("refreshList", false)) {
                    gameMode = null;
                    stakeAmount = "0";
                    hostNote = null;
                    totalStake = "0";
                    numberOfPlayers = "0";
                }

                gameMode = intent.getStringExtra("gameMode");
                stakeAmount = intent.getStringExtra("stakeAmount");
                hostNote = intent.getStringExtra("hostNote");

//                System.out.println("what is gameMode: " + intent.getStringExtra("gameMode"));
            }
        }

    }

    private void testingApi(){

        WalletListener walletListener = K.retrofit.create(WalletListener.class);

        walletListener.test().enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                Toast.makeText(MainActivity.this, "Api test is : " + response.body(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Long> call, Throwable throwable) {
                Toast.makeText(MainActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void tabListener()
    {
        // Set up the page change callback
        viewPager2General.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                if(onUserLongPressView != null && onUserLongPressView.getVisibility() == View.VISIBLE)
                {
                    cancelUserDeleteOption();
                }

            }
        });
    }

    // =============    arrange views

    private void activateSideBarViews(){

        if(sideBarView == null)
        {
            sideBarMenuContainer = findViewById(R.id.sideBarMenuContainer);

            if (sideBarMenuContainer != null) {
                sideBarView = sideBarMenuContainer.inflate();
                // Now find views within sideBarView
                cardViewWallet = sideBarView.findViewById(R.id.cardViewWallet);
                sideBarMenuClose = sideBarView.findViewById(R.id.imageViewMenuClose);
                tapImage_TV = sideBarView.findViewById(R.id.tapImage_TV);
                imageViewUserPhoto = sideBarView.findViewById(R.id.imageViewUserPhoto);
                textViewDisplayName = sideBarView.findViewById(R.id.textViewDisplayName2);
                textViewUserName = sideBarView.findViewById(R.id.textViewUserName2);
                cardViewSettings = sideBarView.findViewById(R.id.cardViewSettings);
                dashboard = sideBarView.findViewById(R.id.cardViewDashboard);
                premium = sideBarView.findViewById(R.id.cardViewPremium);
                advertise = sideBarView.findViewById(R.id.cardAdvert);
                invite = sideBarView.findViewById(R.id.cardViewInvite2);
                support = sideBarView.findViewById(R.id.cardViewCustomerCare);
                phoneNumber_TV = sideBarView.findViewById(R.id.phoneNumber_TV);
                copyUserNameIV = sideBarView.findViewById(R.id.copyUserNameIV);
                copyNumberIV = sideBarView.findViewById(R.id.copyNumberIV);
                hint_TV = sideBarView.findViewById(R.id.hint_TV);
                darkMoodSwitch = sideBarView.findViewById(R.id.switch1);
                textLightAndDay = sideBarView.findViewById(R.id.lightAndDark_TV);
                progressBarMood = sideBarView.findViewById(R.id.progressBarMood);

            if(nightMood){
                darkMoodSwitch.setChecked(true);
                textLightAndDay.setText("Light");
            } else {
                textLightAndDay.setText("Dark");
            };

            darkMoodSwitch.setOnClickListener(view ->
            {
                progressBarMood.setVisibility(View.VISIBLE);
                darkMoodSwitch.setVisibility(View.INVISIBLE);
                textLightAndDay.setText(getString(R.string.initialising));

                new Handler().postDelayed(()->
                {
                    if(nightMood){
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        moodPreferences.edit().putBoolean("MoodStatus", false).apply();
                    } else{
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        moodPreferences.edit().putBoolean("MoodStatus", true).apply();
                    }
                    recreate();

                }, 10);
            });

                // close the side bar menu option
                sideBarMenuClose.setOnClickListener(view -> {
                    sideBarMenuContainer.setVisibility(View.GONE);
                });

                sideBarView.setOnClickListener(view -> {
                    sideBarMenuContainer.setVisibility(View.GONE);
                });

                // wallet
                cardViewWallet.setOnClickListener(openWallet);

                View.OnClickListener viewImage = view -> {
                    Intent i = new Intent(this, ZoomImage.class);
                    i.putExtra("otherName", "My Profile Photo");
                    i.putExtra("imageLink", imageLink);
                    i.putExtra("from", "profilePix");

                    startActivity(i);
                };
                imageViewUserPhoto.setOnClickListener(viewImage); // view my profile photo @sideBar menu
                tapImage_TV.setOnClickListener(viewImage);

                // dashboard side bar
                dashboard.setOnClickListener(view ->
                {
                    view.animate().scaleX(1.1f).scaleY(1.1f).setDuration(10).withEndAction(() ->
                            startActivity(new Intent(MainActivity.this, DashboardActivity.class))).start();

                    new Handler().postDelayed(() -> {
                        sideBarMenuContainer.setVisibility(View.GONE);
                        view.setScaleX(1.0f);
                        view.setScaleY(1.0f);
                    }, 1000);
                });


                // settings
                cardViewSettings.setOnClickListener(v ->
                {
                    v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(10).withEndAction(() ->
                    {
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));

                        new Handler().postDelayed(() -> {
                            sideBarMenuContainer.setVisibility(View.GONE);
                            v.setScaleX(1.0f);
                            v.setScaleY(1.0f);
                        }, 1000);

                    }).start();

                });

                premium.setOnClickListener(v ->
                {
                    v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(10).withEndAction(() ->
                    {
                        startActivity(new Intent(MainActivity.this, PremiumActivity.class));

                    }).start();

                    new Handler().postDelayed(() -> {
                        sideBarMenuContainer.setVisibility(View.GONE);
                        v.setScaleX(1.0f);
                        v.setScaleY(1.0f);
                    }, 1000);
                });

                advertise.setOnClickListener(v ->
                {
                    v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(10).withEndAction(() ->
                    {
                        Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
                    });

                    new Handler().postDelayed(() -> {
//                    sideBarMenuContainer.setVisibility(View.GONE);
                        v.setScaleX(1.0f);
                        v.setScaleY(1.0f);
                    }, 1000);
                });


                invite.setOnClickListener(v ->
                {
                    v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(20).withEndAction(() ->
                    {
                        Photo_Video_Utils.shareDrawableImage(this, R.drawable.logo_new_name, getString(R.string.appInvite) );
                    });

                    new Handler().postDelayed(() -> {
//                    sideBarMenuContainer.setVisibility(View.GONE);
                        v.setScaleX(1.0f);
                        v.setScaleY(1.0f);
                    }, 1000);
                });

                support.setOnClickListener(v ->
                {
                    v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(10).withEndAction(() ->
                    {
                        Intent intent = new Intent(MainActivity.this, SupportActivity.class);
                        intent.putExtra("reason", "from home activity - state reason from chat");

                        startActivity(intent);
                    });

                    new Handler().postDelayed(() -> {
                        sideBarMenuContainer.setVisibility(View.GONE);
                        v.setScaleX(1.0f);
                        v.setScaleY(1.0f);
                    }, 1000);
                });


                copyUserNameIV.setOnClickListener(v -> PhoneUtils.copyText(this, textViewUserName));

                copyNumberIV.setOnClickListener(v -> PhoneUtils.copyText(this, phoneNumber_TV));

                getProfileSharePref();

            }

        }
    }

    private void activateIncomingCallView()
    {
        if(incomingCallView == null)
        {
            ViewStub incomingCallLayout = findViewById(R.id.incomingCallLayout);

            incomingCallView = incomingCallLayout.inflate();

            whoIsCallingTV = incomingCallView.findViewById(R.id.incomingNameTV);
            answerCall_IV = incomingCallView.findViewById(R.id.annswerCall_IV);
            rejectCall_IV = incomingCallView.findViewById(R.id.rejectCall_IV);


            rejectCall_IV.setOnClickListener(v -> {
                rejectCall();
            });

            answerCall_IV.setOnClickListener(v->{

                if(appPermissionCheck.isCameraOk(this) ){
                    if(appPermissionCheck.isRecordingOk(this)){
                        answerCall();
                    } else{
                        appPermissionCheck.requestRecordingForCall(this);
                    }
                } else {
                    appPermissionCheck.requestCameraForCall(this);
                }

            });

            // open the call in view call center
            incomingCallView.setOnClickListener(v -> {
                if(callModel != null){
                    Intent intent = new Intent(this, CallPickUpCenter.class);
                    intent.putExtra("otherUid", callModel.getSenderUid());
                    intent.putExtra("myId", myId);
                    intent.putExtra("otherName", callModel.getSenderName());
                    intent.putExtra("myUsername", myUserName);
                    intent.putExtra("answerCall", true);
                    intent.putExtra("callType", callType_);
                    startActivity(intent);
                }
            });

        }
    }

    private void setAddPlayerConfirmView()
    {
        if(addPlayerConfirmView == null) {
            ViewStub addPlayerConfirmVS = findViewById(R.id.addPlayerConfirmVS);
            addPlayerConfirmView = addPlayerConfirmVS.inflate();

            TextView yesTV = addPlayerConfirmView.findViewById(R.id.yesTV);
            TextView noTV = addPlayerConfirmView.findViewById(R.id.noTV);
            titleNoticeTV = addPlayerConfirmView.findViewById(R.id.sureNoticeTV);

            noTV.setOnClickListener(v -> {
                cancelAddNewPlayer();
                ChatsFragment.newInstance().notifyVisibleUser();
            });

            yesTV.setOnClickListener(v -> {

                addNewPlayer = true;
                doneSelectingPlayers = true;

                // add new player details to existing list
                selectedPlayerMList.addAll(newPlayerMList);
                numberOfPlayers = String.valueOf(forwardChatUserId.size());
//                System.out.println("what is amount: " + stakeAmount + " number: " + numberOfPlayers);
                totalStake = String.valueOf( (Double.parseDouble(stakeAmount) * Integer.parseInt(numberOfPlayers)) ) ;

                newPlayerUid = newPlayerMList.get(0).getPlayerUid();
                sendMessage(getString(R.string.whotGame), getString(R.string.connect), 7, null, null, newPlayerUid, false); // audio or video call


                cancelForwardSettings(this);

            });

        }
    }

    private void cancelAddNewPlayer(){
        if(newPlayerMList != null && newPlayerMList.size() > 0){
            String undoPlayerId = newPlayerMList.get(0).getPlayerUid();
            forwardChatUserId.removeIf(name -> name.equals(undoPlayerId));

            newPlayerMList.clear();
        }
        if(addPlayerConfirmView != null) addPlayerConfirmView.setVisibility(View.GONE);
    }

    @Override
    public void openAddPlayerLayout(String playerName) {
        setAddPlayerConfirmView();
        addPlayerConfirmView.setVisibility(View.VISIBLE);
        String addPlayerName = getString(R.string.add) + " " + playerName + " " + getString(R.string.toGame);
        titleNoticeTV.setText(addPlayerName);
    }

    private void setIncomingGameView()
    {
        if(incomingGameView == null)
        {
            ViewStub incomingGameViewStub = findViewById(R.id.incomingGameLayout);
            incomingGameView = incomingGameViewStub.inflate();

            TextView acceptTV = incomingGameView.findViewById(R.id.acceptGameTV);
            ImageView muteIV = incomingGameView.findViewById(R.id.muteIV);
            TextView rejectTV = incomingGameView.findViewById(R.id.rejectGameTV);
            ProgressBar progressBarJ = incomingGameView.findViewById(R.id.progressBarJ);
            expectedPlayerNumTV = incomingGameView.findViewById(R.id.expectedPlayerNumTV);
            hostByTV = incomingGameView.findViewById(R.id.hostGameTV);
            gameModeTV = incomingGameView.findViewById(R.id.modeTV);
            gameTypeTV = incomingGameView.findViewById(R.id.gameTypeTV);
            stakeAmountTV = incomingGameView.findViewById(R.id.stakeAmountTV);
            rewardPoolTV = incomingGameView.findViewById(R.id.rewardTV);
            hostNoteTV = incomingGameView.findViewById(R.id.hostNoteTV);

            ImageView minimizeIV = incomingGameView.findViewById(R.id.minimizeIV);
            minimizedContainer = incomingGameView.findViewById(R.id.minimizedContainer);
            signalCardView = incomingGameView.findViewById(R.id.signalCardView);
            minimizedTV = incomingGameView.findViewById(R.id.minimizedTV);

            minimizeIV.setOnClickListener(v -> {
                AnimUtils.slideOutToBottom(signalCardView, 200);
                minimizedTV.setText(getString(R.string.awaitingGame));
                new Handler().postDelayed(()->{
                    AnimUtils.slideInFromBottom(minimizedContainer, 300);
                }, 200);

            });

            minimizedContainer.setOnClickListener(v -> {

                if(isOnGameNow) goToOngoingGameBoard();
                else if(onAwaitActivity) goToExistingAwaitPlayer();
                else AnimUtils.slideInFromTop(signalCardView, 200);

                minimizedContainer.setVisibility(View.GONE);
            });

            acceptTV.setOnClickListener(v -> {

                if(gameMode.equals("stake")){
                    AnimUtils.slideOutToBottom(incomingGameView, 200);
                    AnimUtils.slideInFromTop(joinGameViews, 300);

                } else {
                    selectedPlayerMList.clear();

                    progressBarJ.setVisibility(View.VISIBLE);
                    acceptTV.setVisibility(View.INVISIBLE);

                    if(hostUid != null) verifyBalanceAndJoin(progressBarJ, acceptTV);
                }

                stopRingTone();
                getGameBalance(null);

            });

            muteIV.setOnClickListener(v -> {
                v.animate().scaleX(1.3f).scaleY(1.3f).withEndAction(() ->
                {
                    stopRingTone();
                    v.setScaleY(1.0f);
                    v.setScaleX(1.0f);
                });
            });

            rejectTV.setOnClickListener(v ->
            {
                v.animate().scaleX(1.3f).scaleY(1.3f).setDuration(100).withEndAction(() ->
                {
                    incomingGameView.setVisibility(View.GONE);
                    stopRingTone();
                    onGameNow = false;
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                });

                GameUtils.rejectGameOrAddNewPlayer(this, hostUid, null, null, new GameUtils.RejectGameInterface() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(MainActivity.this, getString(R.string.gameRejected), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure() {
                        Toast.makeText(MainActivity.this, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show();
                    }
                });

                progressBarJ.setVisibility(View.GONE);
                acceptTV.setVisibility(View.VISIBLE);
            });

        }
    }

    private void goToExistingAwaitPlayer(){
        if(hostRelaunchApp && hostReopen) {
            Intent intent = new Intent(MainActivity.this, AwaitPlayersActivity.class);
            intent.putExtra("mode", gameMode);
            intent.putExtra("hostName", ProfileUtils.getMyDisplayOrUsername());
            intent.putExtra("hostUid", hostUid);
            intent.putExtra("gameID", gameID);
            startActivity(intent);
        } else {
            Intent mainActivityIntent = new Intent(this, AwaitPlayersActivity.class);
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(mainActivityIntent);
            incomingGameView.setVisibility(View.GONE);
        }
        onAwaitActivity = false;
        hostReopen = false;
        if(selectGameView != null) selectGameView.setVisibility(View.GONE);
    }

    private void goToOngoingGameBoard(){
        if(iRelaunchApp) {
            Intent goToWhotGameIntent;
            if (playersInGameList.size() > 2) {
                goToWhotGameIntent = new Intent(this, WhotLandscapeActivity.class);
            } else {
                goToWhotGameIntent = new Intent(this, WhotGameActivity.class);
            }

            String gameId = gameSharePref.getString(K.ongoingGameId, null);
            String hostId = gameSharePref.getString(gameHostUid, null);
            // Put the list as a Parcelable extra
            goToWhotGameIntent.putParcelableArrayListExtra("playerDetailList", new ArrayList<>(playersInGameList));
            goToWhotGameIntent.putExtra("gameId", gameId);
            goToWhotGameIntent.putExtra("hostId", hostId);
            startActivity(goToWhotGameIntent);

        } else {
            Intent gameActivityIntent = null;
            if(whichGameActivity_.equals("whotLandscape")) {
                gameActivityIntent = new Intent(this, WhotLandscapeActivity.class);
            } else if (whichGameActivity_.equals("whotPortrait")) {
                gameActivityIntent = new Intent(this, WhotGameActivity.class);
            }
            gameActivityIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(gameActivityIntent);
        }
//        isOnGameNow = false;
        iRelaunchApp = false;
    }

    private void setJoinGameViews()
    {
        if(joinGameViews == null)
        {
            ViewStub joinGameViewStub = findViewById(R.id.joinGameLayout);
            joinGameViews = joinGameViewStub.inflate();

            TextView join = joinGameViews.findViewById(R.id.acceptGameTV);
            gameBalTV = joinGameViews.findViewById(R.id.gameBalTV);
            TextView topUpTV = joinGameViews.findViewById(R.id.topUpTV);
            TextView cancelJoinTV = joinGameViews.findViewById(R.id.cancelJoinTV);
            notice_TV = joinGameViews.findViewById(R.id.notice_TV);
            ProgressBar progressBarJ = joinGameViews.findViewById(R.id.progressBarJ);

            topUpTV.setOnClickListener(v -> {
                v.animate().scaleX(1.3f).scaleY(1.3f).withEndAction(() ->
                {
                    Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
                    v.setScaleY(1.0f);
                    v.setScaleX(1.0f);
                });
            });

            join.setOnClickListener(v -> {

                selectedPlayerMList.clear();

                progressBarJ.setVisibility(View.VISIBLE);
                join.setVisibility(View.INVISIBLE);
                
                if(hostUid != null) verifyBalanceAndJoin(progressBarJ, join);

            });

            cancelJoinTV.setOnClickListener(v -> {

                AnimUtils.slideOutToBottom(joinGameViews, 200);
                AnimUtils.slideInFromTop(incomingGameView, 400);
                progressBarJ.setVisibility(View.GONE);
                join.setVisibility(View.VISIBLE);
//                v.animate().scaleX(1.3f).scaleY(1.3f).withEndAction(() ->
//                {
//                    Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
//                    v.setScaleY(1.0f);
//                    v.setScaleX(1.0f);
//                });
            });



        }

    }
    
    private void verifyBalanceAndJoin(View progressBarJ, View joinTV)
    {
        refGameAlert.child(myId).child(hostUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    SignalPlayerM signalPlayerM = snapshot.getValue(SignalPlayerM.class);

                    // Retrieve the players map
                    DataSnapshot playersSnapshot = snapshot.child("players");

                    for (DataSnapshot playerSnapshot : playersSnapshot.getChildren()) {
                        AwaitPlayerM player = playerSnapshot.getValue(AwaitPlayerM.class);
                        if (player != null) selectedPlayerMList.add(player);
                    }

                    GameAPI gameAPI = K.retrofit.create(GameAPI.class);

                    IdTokenUtil.generateToken(token->{

                        TwoValueM twoValueM = new TwoValueM(token, hostUid);

                        gameAPI.join(twoValueM).enqueue(new Callback<ResultApiM>() {
                            @Override
                            public void onResponse(Call<ResultApiM> call, Response<ResultApiM> response) {

                                if(response.isSuccessful()) {

                                    if(response.body().getResult().equals("success"))
                                    {
                                        Intent intent = new Intent(MainActivity.this, AwaitPlayersActivity.class);
                                        intent.putExtra("mode", signalPlayerM.getGameMode());
                                        intent.putExtra("hostName", signalPlayerM.getSenderName());
                                        intent.putExtra("hostUid", signalPlayerM.getFromUid());
                                        intent.putExtra("gameID", signalPlayerM.getGameID());
                                        startActivity(intent);

                                        gameSharePref.edit().putString(K.ongoingGameId, signalPlayerM.getGameID()).apply();
                                        gameSharePref.edit().putString(gameHostUid, signalPlayerM.getFromUid()).apply();

                                        new Handler().postDelayed(()->{
                                            progressBarJ.setVisibility(View.GONE);
                                            joinTV.setVisibility(View.VISIBLE);
                                            incomingGameView.setVisibility(View.GONE);
                                            joinGameViews.setVisibility(View.GONE);

                                        }, 1000);

                                    } else if(response.body().getResult().equals("insufficient funds"))
                                    {
                                        Toast.makeText(MainActivity.this, getString(R.string.insufficientBal), Toast.LENGTH_SHORT).show();
                                        progressBarJ.setVisibility(View.GONE);
                                        joinTV.setVisibility(View.VISIBLE);
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<ResultApiM> call, Throwable throwable) {
                                progressBarJ.setVisibility(View.GONE);
                                joinTV.setVisibility(View.VISIBLE);
                                Toast.makeText(MainActivity.this, ""+getString(R.string.errorOccur), Toast.LENGTH_LONG).show();
                                System.out.println("what is wallet error occur MainActivity L1700: " + throwable.getMessage());

                            }
                        });

                    }, MainActivity.this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBarJ.setVisibility(View.GONE);
                joinTV.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, ""+ getString(R.string.noInternetConnection), Toast.LENGTH_SHORT).show();
            }

        });
        
    }

    private void activateAudioOrVideoOptionView()
    {
        if(audioOrVideoView == null)
        {
            audioOrVideoOptionConatiner = findViewById(R.id.videoOrAudioOptionLayout);

            audioOrVideoView = audioOrVideoOptionConatiner.inflate();

            TextView audioCallButton = audioOrVideoView.findViewById(R.id.audioCallOption);
            TextView videoCallButton = audioOrVideoView.findViewById(R.id.videoCallOption);

            audioOrVideoView.setOnClickListener(v -> audioOrVideoView.setVisibility(View.GONE));

            audioCallButton.setOnClickListener(v -> {
                v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(30).withEndAction(() ->
                {
                    makeCall("audio");
                    makeCall = true;
                    audioOrVideoView.setVisibility(View.GONE);
                    // Reset the scale
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);

                }).start();
            });

            videoCallButton.setOnClickListener(v -> {
                v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(50).withEndAction(() ->
                {
                    makeCall("video");
                    makeCall = true;
                    audioOrVideoView.setVisibility(View.GONE);

                    // Reset the scale
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);

                }).start();
            });

        }

    }

    private void setCheckNetworkView()
    {
        if(checkNetworkView == null)
        {
            constrNetConnect = findViewById(R.id.networkCheckLayout);

            checkNetworkView = constrNetConnect.inflate();
        }
    }

    private void setWalletVerifyViews()
    {
        if(walletVerifyView == null)
        {
            //  ============    wallet pin verify id  =======================
            walletVerifyLayout = findViewById(R.id.walletVerifyLayout);
            
            walletVerifyView = walletVerifyLayout.inflate();

            pinContainerHome = walletVerifyView.findViewById(R.id.pinContainerHome);
            cancelPinOption_IV = walletVerifyView.findViewById(R.id.cancelPinOption_IV);
            or_TV = walletVerifyView.findViewById(R.id.or_TV);
            openPinBox_TV = walletVerifyView.findViewById(R.id.openPinBox_TV);
            pinOptionContainer = walletVerifyView.findViewById(R.id.pinOptionContainer);
            fingerprintIcon = walletVerifyView.findViewById(R.id.fingerprintIcon);
            verifyViaTV = walletVerifyView.findViewById(R.id.verifyViaTV);
            openWalletButton = walletVerifyView.findViewById(R.id.openWalletButton);
            forgetPin_Button = walletVerifyView.findViewById(R.id.forgetPin_Button);
            enterAckPin_ET = walletVerifyView.findViewById(R.id.enterAckPin_ET);

            cancelPinOption_IV.setOnClickListener(v -> walletVerifyView.setVisibility(View.GONE));

            openPinBox_TV.setOnClickListener(v -> verifyWithPin());
            fingerprintIcon.setOnClickListener(v -> showFingerPrint());

            openWalletButton.setOnClickListener(v -> {
                if(enterAckPin_ET.length() == 4){
                    loadVerify();
                    openWalletMethod();     // pin
                } else {
                    Toast.makeText(this, getString(R.string.incorrectPin), Toast.LENGTH_SHORT).show();
                }
            });

            forgetPin_Button.setOnClickListener(v -> {
                Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
            });

        }
    }

    public void setChatOptionView()     //  ===== Long Press chat option settings
    {
//        if(chatOptionView == null)
//        {
            ViewStub chatOptionViewStub = findViewById(R.id.chatOptions);
            if(chatOptionViewStub != null)
            {
                chatOptionView = chatOptionViewStub.inflate();
                chatOptionView.setVisibility(View.GONE);

                editChatOption_IV = chatOptionView.findViewById(R.id.edit_IV);
                replyChatOption_IV = chatOptionView.findViewById(R.id.reply_IV);
                forwardChatOption_IV = chatOptionView.findViewById(R.id.forward_IV);
                copyChatOption_IV = chatOptionView.findViewById(R.id.copyText_IV);
                deleteChatOption_IV = chatOptionView.findViewById(R.id.delete_IV);
                chatSelected_TV = chatOptionView.findViewById(R.id.count_TV);
                cancelChatOption_IV = chatOptionView.findViewById(R.id.cancel_IV);
                moreOption_IV = chatOptionView.findViewById(R.id.moreOptionIV);

                setMoreOptionViews();

                //  =====   onClicks

                cancelChatOption_IV.setOnClickListener(view -> {
                    view.animate().scaleX(1.2f).scaleY(1.2f).setDuration(50)
                            .withEndAction(() -> {

                                getOnBackPressedDispatcher().onBackPressed();

                                // Reset the scale
                                view.setScaleX(1.0f);
                                view.setScaleY(1.0f);

                            }).start();
                });

                // reply button on long press
                replyChatOption_IV.setOnClickListener(view -> {
                    view.animate().scaleX(1.3f).scaleY(1.3f).setDuration(50).withEndAction(() ->
                    {

                        onEditOrReplyMessage_(modelChatsOption, "reply", "replying...",
                                R.drawable.reply);

                        // Reset the scale
                        view.setScaleX(1.0f);
                        view.setScaleY(1.0f);

                        pinIconsVisibility(otherUserUid);
                        generalBackground.setVisibility(View.GONE);
                        moreOptionViews.setVisibility(View.GONE);
                        AnimUtils.fadeOutGone(topEmojiView, 500);

                    }).start();

                });

                editChatOption_IV.setOnClickListener(onEdit);

                //  forward chat
                forwardChatOption_IV.setOnClickListener(forwardChat);

                // delete chat
                deleteChatOption_IV.setOnClickListener(onDelete);

                // copy chat
                copyChatOption_IV.setOnClickListener(onCopy);

                // open more option
                moreOption_IV.setOnClickListener(view ->
                {
                    generalBackground.setVisibility(View.VISIBLE);

                    view.animate().scaleX(1.2f).scaleY(1.2f).setDuration(20).withEndAction(()->
                    {
                        if(modelChatsOption.getType() != 0){    //  0 is text, 1 is voice note, 2 is photo, 3 is document, 4 is audio (mp3), 5 is video
                            if(modelChatsOption.getType() ==  2 || modelChatsOption.getType() ==  5) {
                                saveToGalleryTV.setVisibility(View.VISIBLE);
                            } else saveToGalleryTV.setVisibility(View.GONE);

                        } else {
                            saveToGalleryTV.setVisibility(View.GONE);
                        }

                        // open or close more option
                        if(moreOptionViews.getVisibility() == View.GONE){
                            // Start the animation to make it visible
                            animateVisibility(null, moreOptionViews);
                        } else {
                            generalBackground.setVisibility(View.GONE);
                            moreOptionViews.setVisibility(View.GONE);
                        }

                        new Handler().postDelayed(()-> {
                            view.setScaleX(1f);
                            view.setScaleY(1f);
                        }, 100);
                        AnimUtils.fadeOutGone(topEmojiView, 500);

                    });
                });
            }

//        }
    }

    private void setMoreOptionViews()
    {
        if(moreOptionViews == null)
        {
            moreOptionViewStub = findViewById(R.id.moreOptionViewStub);

            moreOptionViews = moreOptionViewStub.inflate();
            moreOptionViews.setVisibility(View.GONE);

            pinTV = moreOptionViews.findViewById(R.id.pinChat_TV);
            TextView editTV = moreOptionViews.findViewById(R.id.editChatTV);
            saveToGalleryTV = moreOptionViews.findViewById(R.id.saveToGalleryTV);
            reportTV = moreOptionViews.findViewById(R.id.report_TV);

            //  ====    onClicks

            editTV.setOnClickListener(onEdit);

            //  pin
            pinTV.setOnClickListener(onPin);

            // save photo or file to gallery
            saveToGalleryTV.setOnClickListener(view -> {
                Toast.makeText(this, "Work in progress", Toast.LENGTH_SHORT).show();
            });

            // save photo or file to gallery
            reportTV.setOnClickListener(view -> {
                Toast.makeText(this, "Report chat in progress", Toast.LENGTH_SHORT).show();
            });

            // hide the more option container
            generalBackground.setOnClickListener(v -> {
                generalBackground.setVisibility(View.GONE);
                if(moreOptionViews != null) moreOptionViews.setVisibility(View.GONE);
            });
        }
    }

    private void setDeleteUserOrClearChatViews()    // delete user or clear chats
    {
        //  delete user from chat list ids
        if(deleteUserOrClearChatViews == null)
        {
            ViewStub deleteUserOrClearChatContainer = findViewById(R.id.deleteUserOrClearChatContainer);
            deleteUserOrClearChatViews = deleteUserOrClearChatContainer.inflate();

            deleteUserForMe_TV = deleteUserOrClearChatViews.findViewById(R.id.deleteForMe_TV);
            deleteUserForAll_TV = deleteUserOrClearChatViews.findViewById(R.id.deleteForEveryone_TV);
            ImageView cancelUserDelete_IV = deleteUserOrClearChatViews.findViewById(R.id.cancelDelete_IV);
            otherUserName_TV = deleteUserOrClearChatViews.findViewById(R.id.otherUserName_TV);

            //  ===========  delete user from ChatList  And Clear Chat History onClicks =========

            deleteUserOrClearChatViews.setOnClickListener(view -> cancelUserDeleteOption());
            //  delete user container   -- close the container when click and cancel button click
            cancelUserDelete_IV.setOnClickListener(view -> cancelUserDeleteOption());

            // delete user for only me
            deleteUserForMe_TV.setOnClickListener(view -> {

                view.animate().scaleX(1.2f).scaleY(1.2f).withEndAction(() ->
                {
                    for (int i = 0; i < userModelList.size(); i++)
                    {
                        String otherUid_Del = userModelList.get(i).getOtherUid();

                        if(!clearOnlyChatHistory)   // delete user from chat list
                        {
                            refLastDetails.child(myId).child(otherUid_Del).removeValue();
                            refChecks.child(myId).child(otherUid_Del).removeValue();

                            // delete user from adapter list
                            UserChatUtils.findUserAndDelete(PlayersFragment.adapter, otherUid_Del);
                            if(ChatsFragment.adapter != null) UserChatUtils.findUserAndDelete(ChatsFragment.adapter, otherUid_Del);

                            // delete user from ROOM
                            chatViewModel.deleteUserById(otherUid_Del);

                        }  else      // clear only chat from outside
                        {
                            refLastDetails.child(myId).child(otherUid_Del).child("message").setValue(K.DELETE_ICON +" ...");

                            MessageAdapter adapter = adapterMap.get(otherUid_Del);
                            assert adapter != null;

                            // delete last chat from ROOM - outside UI
                            MessageModel model = adapter.getModelList().get(adapter.getModelList().size()-1);   // get the last msg on the chat list
                            chatViewModel.editOutsideChat(otherUid_Del, K.DELETE_ICON + "  ...",
                                    null, model.getIdKey());

                            // update outside user UI chatList model
                            if(ChatsFragment.adapter != null) UserChatUtils.findUserAndDeleteChat(ChatsFragment.adapter, otherUid_Del, model.getIdKey());
                            UserChatUtils.findUserAndDeleteChat(PlayersFragment.adapter, otherUid_Del, model.getIdKey());

                            Toast.makeText(this, getString(R.string.chatClearForMe), Toast.LENGTH_SHORT).show();
                        }

                        refPublicPinChat.child(myId).child(otherUid_Del).removeValue();
                        refPrivatePinChat.child(myId).child(otherUid_Del).removeValue();

                        refMsgFast.child(myId).child(otherUid_Del).removeValue();

                        // clear chats from ROOM
                        chatViewModel.deleteChatByUserId(otherUid_Del, myId);
                        // delete from adapter chat list
                        try{
                            if(adapterMap.get(otherUid_Del) != null) adapterMap.get(otherUid_Del).clearChats();
                        } catch (Exception e){
                            System.out.println("what is error Main L2195: " + e.getMessage());
                        }

                        if(photoAndVideoMap.get(otherUid_Del) != null) photoAndVideoMap.get(otherUid_Del).clear();

                        if(i == userModelList.size()-1){
                            if(clearOnlyChatHistory) {
                                if(adapterMap.get(otherUserUid) != null) adapterMap.get(otherUserUid).notifyDataSetChanged();
                            }
                            clearOnlyChatHistory = false;
                            cancelUserDeleteOption();
                        }

                    }
                    cancelUserDeleteOption();
                    // Reset the scale
                    view.setScaleX(1.0f);
                    view.setScaleY(1.0f);
                }).start();

            });

            // delete user or clear chat for everyone
            deleteUserForAll_TV.setOnClickListener(view -> {

                view.animate().scaleX(1.2f).scaleY(1.2f).withEndAction(() ->
                {
                    for (int i = 0; i < userModelList.size(); i++)
                    {
                        String otherUid_Del = userModelList.get(i).getOtherUid();

                        if(!clearOnlyChatHistory)   // delete user from chat list
                        {
                            refLastDetails.child(myId).child(otherUid_Del).removeValue();
                            refChecks.child(myId).child(otherUid_Del).removeValue();

                            refLastDetails.child(otherUid_Del).child(myId).removeValue();
                            refChecks.child(otherUid_Del).child(myId).removeValue();

                            // delete user from adapter list
                            UserChatUtils.findUserAndDelete(PlayersFragment.adapter, otherUid_Del);
                            if(ChatsFragment.adapter != null) UserChatUtils.findUserAndDelete(ChatsFragment.adapter, otherUid_Del);

                            // delete user from ROOM
                            chatViewModel.deleteUserById(otherUid_Del);

                            refDeleteUser.child(myId).child(otherUid_Del).setValue("clear");

                        } else      // clear only chat from outside
                        {
                            refLastDetails.child(myId).child(otherUid_Del)
                                    .child("message").setValue(K.DELETE_ICON + " ...");
                            refLastDetails.child(otherUid_Del).child(myId)
                                    .child("message").setValue( K.DELETE_ICON + " ...");

                            // delete last chat from ROOM - outside UI
                            MessageAdapter adapter = adapterMap.get(otherUid_Del);
                            assert adapter != null;
                            MessageModel model = adapter.getModelList().get(adapter.getModelList().size()-1);   // get the last msg on the chat list
                            chatViewModel.editOutsideChat(otherUid_Del, K.DELETE_ICON + "  ...",
                                    null, model.getIdKey());

                            // update outside user UI chatList model
                            if(ChatsFragment.adapter != null) UserChatUtils.findUserAndDeleteChat(ChatsFragment.adapter, otherUid_Del, model.getIdKey());
                            UserChatUtils.findUserAndDeleteChat(PlayersFragment.adapter, otherUid_Del, model.getIdKey());

                            // help to remove the last chat on outside UI
                            String lastChatId = adapter.getModelList().get(adapter.getModelList().size() -1).getIdKey();
                            deleteMap.put("idKey", lastChatId);
                            refDeleteMsg.child(otherUserUid).child(myId).child(lastChatId).setValue(deleteMap);

                            Toast.makeText(this, getString(R.string.chatClearForEveryone), Toast.LENGTH_SHORT).show();
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

                        // clear chats from ROOM
                        chatViewModel.deleteChatByUserId(otherUid_Del, myId);

                        if(adapterMap.get(otherUid_Del) != null) adapterMap.get(otherUid_Del).clearChats(); // delete chats
                        if(photoAndVideoMap.get(otherUid_Del) != null) photoAndVideoMap.get(otherUid_Del).clear();

                        if(i == userModelList.size()-1){
                            if(clearOnlyChatHistory) {
                                if(adapterMap.get(otherUid_Del) != null) adapterMap.get(otherUserUid).notifyDataSetChanged();
                            }
                            clearOnlyChatHistory = false;
                            cancelUserDeleteOption();
                        }

                    }

                    // Reset the scale
                    view.setScaleX(1.0f);
                    view.setScaleY(1.0f);
                }).start();


            });

        }
    }

    private void setChatMenuViews()
    {
        if(chatMenuViews == null)
        {
            //  Chat Box Menu ids
            ViewStub chatMenuProfile = findViewById(R.id.chatMenuViewStub);
            chatMenuViews = chatMenuProfile.inflate();

            scrollViewMenu = chatMenuViews.findViewById(R.id.scrollViewMenu);
            TextView viewProfileTV = chatMenuViews.findViewById(R.id.viewProfileTV);
            TextView joinTour_TV = chatMenuViews.findViewById(R.id.joinTour_TV);
            TextView muteTV = chatMenuViews.findViewById(R.id.muteIV);
            TextView search_TV = chatMenuViews.findViewById(R.id.search_TV);
            TextView clearPin_TV = chatMenuViews.findViewById(R.id.clearPin_TV);
            TextView clearChat_TV = chatMenuViews.findViewById(R.id.clearChat_TV);
            TextView disappearChat_TV = chatMenuViews.findViewById(R.id.disappearChat_TV);
            TextView blockAndReport_TV = chatMenuViews.findViewById(R.id.blockAndReport_TV);
            ImageView closeMenu = chatMenuViews.findViewById(R.id.imageViewCancel9);

            //  ===  onClicks

            // close user menu
            View.OnClickListener close = v -> {
                scrollViewMenu.setVisibility(View.GONE);
                chatMenuViews.setVisibility(View.GONE);
            };
            closeMenu.setOnClickListener(close);
            chatMenuViews.setOnClickListener(close);

            //  clear user chats
            clearChat_TV.setOnClickListener(view -> {

                if (adapterMap.get(otherUserUid) != null && adapterMap.get(otherUserUid).getItemCount() > 0)
                {
                    setDeleteUserOrClearChatViews();

                    chatMenuViews.setVisibility(View.GONE);   // hide profile option
                    scrollViewMenu.setVisibility(View.GONE);
                    otherUserName_TV.setText(R.string.clear_history );
                    deleteUserForMe_TV.setText(R.string.clear_for_me);
                    deleteUserForAll_TV.setText(R.string.clear_for_everyone);
                    deleteUserOrClearChatViews.setVisibility(View.VISIBLE);

                    clearOnlyChatHistory = true;

                    userModelList.clear();
                    UserOnChatUI_Model userModel = new UserOnChatUI_Model(otherUserUid, null, null,
                            null, null, 0, 0, 0, null,0);
                    userModelList.add(userModel);

                } else Toast.makeText(this, getString(R.string.chatEmpty), Toast.LENGTH_SHORT).show();

            });

            viewProfileTV.setOnClickListener(v -> {
                Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
            });

            muteTV.setOnClickListener(v -> {
                Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
            });

            search_TV.setOnClickListener(v -> {
                Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
            });

            clearPin_TV.setOnClickListener(v -> {
                Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
            });

            disappearChat_TV.setOnClickListener(v -> {
                Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
            });

            blockAndReport_TV.setOnClickListener(v -> {
                Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
            });

        }
    }


    private void setFirstTopChatViews()     // firstTopView  and  setPinChatViews
    {
        if (firstTopChatViews == null)
        {
            ViewStub firstTopUserDetailsContainer = findViewById(R.id.firstTopContainer);
            firstTopChatViews = firstTopUserDetailsContainer.inflate();

            textViewMsgTyping = firstTopChatViews.findViewById(R.id.isTyping_TV);
            circleImageLogo = firstTopChatViews.findViewById(R.id.circleImageLogo9);
            textViewOtherUser = firstTopChatViews.findViewById(R.id.userName_TV);
            arrowBack = firstTopChatViews.findViewById(R.id.backArrow_IV);
            openChatMenu_IV = firstTopChatViews.findViewById(R.id.chatMenu_IV);
            textViewLastSeen = firstTopChatViews.findViewById(R.id.onlineStatus_TV);
            callButton = firstTopChatViews.findViewById(R.id.callMeet_IV);
            bioHint_TV = firstTopChatViews.findViewById(R.id.bioHint_TV);

            //  ========    onClicks

            arrowBack.setOnClickListener(view -> {
                hideKeyboard();
                insideChat = false;      // back button

                clearEmojiReactSetting();
                getOnBackPressedDispatcher().onBackPressed();
            });

            openChatMenu_IV.setOnClickListener(view -> {
                setChatMenuViews();
                chatMenuViews.setVisibility(View.VISIBLE);
                AnimUtils.slideInFromRight(scrollViewMenu, 100);
            });

            callButton.setOnClickListener(view -> {
                view.animate().scaleX(1.2f).scaleY(1.2f).setDuration(50).withEndAction(() ->
                {
                    if(appPermissionCheck.isCameraOk(this) ){
                        if(appPermissionCheck.isRecordingOk(this)){

                            activateAudioOrVideoOptionView();
                            audioOrVideoView.setVisibility(View.VISIBLE);

                        } else{
                            appPermissionCheck.requestRecordingForCall(this);
                        }
                    } else {
                        appPermissionCheck.requestCameraForCall(this);
                    }

                    // Reset the scale
                    view.setScaleX(1.0f);
                    view.setScaleY(1.0f);

                }).start();

            });

            if(onForward) firstTopChatViews.setVisibility(View.INVISIBLE);

            //  ==========      set pin views

            pinMsgConst = firstTopChatViews.findViewById(R.id.pinMsgConst);
            pinIconsContainer = firstTopChatViews.findViewById(R.id.openPinMsg_Constr);
            pinMsgBox_Constr = firstTopChatViews.findViewById(R.id.pinMsgBox);
            hidePinMsg_IV = firstTopChatViews.findViewById(R.id.view_IV);
            pinPrivateIcon_IV = firstTopChatViews.findViewById(R.id.pinsPrivate_IV);
            pinPublicIcon_IV = firstTopChatViews.findViewById(R.id.pinsPublic_IV);
            pinCount_TV = firstTopChatViews.findViewById(R.id.pinCount_TV);
            totalPinPrivate_TV = firstTopChatViews.findViewById(R.id.totalPinMsgPrivate_TV);
            totalPinPublic_TV = firstTopChatViews.findViewById(R.id.totalPinMsgPublic_TV);
            pinMsg_TV = firstTopChatViews.findViewById(R.id.pinMsg_TV);
            arrowDown = firstTopChatViews.findViewById(R.id.downArrow_IV);
            arrowUp = firstTopChatViews.findViewById(R.id.upArrow_IV);
            pinLockPrivate_IV = firstTopChatViews.findViewById(R.id.private_IV);
            pinLockPublic_IV = firstTopChatViews.findViewById(R.id.public_IV);
            line = firstTopChatViews.findViewById(R.id.line_);
            newPinIndicator_TV = firstTopChatViews.findViewById(R.id.newPinIndicator_TV);
            pinClose_IV = firstTopChatViews.findViewById(R.id.pinClose_IV);
            pinByTV = firstTopChatViews.findViewById(R.id.pinByWho_TV);
            closePinBox_TV = firstTopChatViews.findViewById(R.id.closePinBox_TV);


            View.OnClickListener closePinBox = view -> {  // personalise later
                pinIconsContainer.setVisibility(View.VISIBLE);
                pinMsgConst.setClickable(false);    //  allow item on the background clickable
                pinMsgBox_Constr.setVisibility(View.INVISIBLE);

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
            closePinBox_TV.setOnClickListener(closePinBox);

            // open private pin message box
            View.OnClickListener openPrivatePinMsg = view -> { // personalise later
                pinIconsContainer.setVisibility(View.GONE);
                pinMsgBox_Constr.setVisibility(View.VISIBLE);
                pinStatus = PRIVATE;      // indicate to show private pins
//                pinChatViews.setClickable(true);   //  stop item on the background clickable
                hidePinMsg_IV.setImageResource(R.drawable.lock);  // indicate private icon

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
                pinMsgConst.setClickable(true); // stop item on the background clickable

                // show current pin message and total pin number
                int pinNum = pinPublicChatMap.get(otherUserUid).size();
                pinCount_TV.setText("(" + pinNextPublic + "/" + (pinNum) + ")");
                int currentPinNumber = pinNum - pinNextPublic;
                String getChat, getPinBy, getPinName;
                try{
                    PinMessageModel pinModel = pinPublicChatMap.get(otherUserUid).get(currentPinNumber);
                    getChat = pinModel.getMessage();
                    getPinBy = pinModel.getPinByWho();
                    getPinName = contactNameShareRef.getString(pinModel.getPinByUID(), getPinBy);
                } catch (Exception e){
                    PinMessageModel pinModel = pinPublicChatMap.get(otherUserUid).get(pinNum-1);
                    getChat = pinModel.getMessage();
                    getPinBy = pinModel.getPinByWho();
                    getPinName = contactNameShareRef.getString(pinModel.getPinByUID(), getPinBy);
                }
                // display pin chat and pinByWho on the UI
                pinMsg_TV.setTypeface(null);
                pinMsg_TV.setText(getChat);

//                String pinBy = getString(R.string.pinBy) + " " + getPinBy;
                pinByTV.setText(getPinName);
                newPinIndicator_TV.setVisibility(View.GONE);

                // make pinByWho visible and close pin box option
                pinByTV.setVisibility(View.VISIBLE);
                pinClose_IV.setVisibility(View.VISIBLE);

            };
            pinPublicIcon_IV.setOnClickListener(openPublicPinMsg);
            totalPinPublic_TV.setOnClickListener(openPublicPinMsg);

            // arrow up, scroll to upper previous pins
            View.OnClickListener arrowUpClick = view -> {    // go to next upper pin message
                arrowUp.animate().scaleX(1.3f).scaleY(1.3f).setDuration(30).withEndAction(()->
                {
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
                        String getChat, getPinByWho, getPinName;
                        try{
                            PinMessageModel pinModel = pinPublicChatMap.get(otherUserUid).get(reduceNumber);
                            getChat = pinModel.getMessage();
                            getPinByWho = pinModel.getPinByWho();
                            getPinName = contactNameShareRef.getString(pinModel.getPinByUID(), getPinByWho);
                        } catch (Exception e ){
                            PinMessageModel pinModel = pinPublicChatMap.get(otherUserUid).get(pinNum - 1);
                            getChat = pinModel.getMessage();
                            getPinByWho = pinModel.getPinByWho();
                            getPinName = contactNameShareRef.getString(pinModel.getPinByUID(), getPinByWho);
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
//                        String pinBy = getString(R.string.pinBy) + " " + getPinByWho;
                            pinByTV.setText(getPinName);
                        } else{
                            pinNextPublic -= 1;
//                    Toast.makeText(this, "No more pin message!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    arrowUp.setScaleX(1.0f);
                    arrowUp.setScaleY(1.0f);
                }).start();

                new Handler().postDelayed(MainActivity::clearAllHighlights, 5_000);

            };
            arrowUp.setOnClickListener(arrowUpClick);
            pinMsgBox_Constr.setOnClickListener(arrowUpClick);

            // arrow down, scroll to recent pin messages
            arrowDown.setOnClickListener(view -> {    // go to next down pin message
                view.animate().scaleX(1.3f).scaleY(1.3f).setDuration(30).withEndAction(()->
                {
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
                        String getChat, getPinByWho, getPinName;
                        try{
                            PinMessageModel pinModel = pinPublicChatMap.get(otherUserUid).get(increaseNumber);
                            getChat = pinModel.getMessage();
                            getPinByWho = pinModel.getPinByWho();
                            getPinName = contactNameShareRef.getString(pinModel.getPinByUID(), getPinByWho);
                        }catch (Exception e){
                            PinMessageModel pinModel = pinPublicChatMap.get(otherUserUid).get(pinNum - 1);
                            getChat = pinModel.getMessage();
                            getPinByWho = pinModel.getPinByWho();
                            getPinName = contactNameShareRef.getString(pinModel.getPinByUID(), getPinByWho);
                        }

                        // only scroll when pin is between 0 to pinNum
                        if (scrollPosition < pinNum ) {
                            scrollToPinMessage(scrollPosition); // call method to scroll to message
                        } else {
                            pinScrollPublic += 1;
                            Toast.makeText(this, getString(R.string.scrollUpForPin), Toast.LENGTH_SHORT).show();
                        }

                        // only update UI when pin is between 0 to pinNum
                        if (increaseNumber < pinNum){
                            pinCount_TV.setText("(" + pinNextPublic + "/" + (pinNum) + ")");
                            pinMsg_TV.setText(getChat);
//                        String pinBy = getString(R.string.pinBy) + " " + getPinByWho;
                            pinByTV.setText(getPinName);
                        } else {
                            pinNextPublic += 1; // to enable it stop decreasing
                        }
                    }

                    view.setScaleX(1.0f);
                    view.setScaleY(1.0f);
                }).start();

                new Handler().postDelayed(MainActivity::clearAllHighlights, 5_000);
            });

            pinIconsVisibility(otherUserUid);
        }

    }

    private void setOnForwardTopView()
    {
        if(onForwardTopView == null)
        {
            ViewStub forwardTopContainer = findViewById(R.id.forwardConstraint);
            onForwardTopView = forwardTopContainer.inflate();

            progressBarForward = onForwardTopView.findViewById(R.id.progressBarForward);
            ImageView cancelForward_IV = onForwardTopView.findViewById(R.id.forwardCancel_IV);
            ImageView searchUserForward_IV = onForwardTopView.findViewById(R.id.forwardSearchIV);
            titleTV_ = onForwardTopView.findViewById(R.id.titleTV_);

            //  onClicks

            cancelForward_IV.setOnClickListener(view -> cancelForwardSettings(this));

            searchUserForward_IV.setOnClickListener(v -> {
                Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
            });

        }
    }

    private void setMoreOptionHomeView()
    {
        if(moreOptionHomeView == null)
        {
            moreOptionHomeLayout = findViewById(R.id.moreHomeContainer);
            moreOptionHomeView = moreOptionHomeLayout.inflate();

            moreHomeCont2 = moreOptionHomeView.findViewById(R.id.moreHomeCont2);
            TextView hostPlayerClick = moreOptionHomeView.findViewById(R.id.hostPlayerClick);
            TextView createLeagueClick = moreOptionHomeView.findViewById(R.id.createLeagueClick);
            TextView createCommunityClick = moreOptionHomeView.findViewById(R.id.createCommunityClick);
            TextView inviteFriendsClick = moreOptionHomeView.findViewById(R.id.inviteFriendsClick);
            TextView goPremiumClick = moreOptionHomeView.findViewById(R.id.goPremiumClick);

            //  ======  onClicks

            moreOptionHomeView.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

            hostPlayerClick.setOnClickListener(v ->
            {
                v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(20).withEndAction(()->
                {
                    Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
//setIncomingGameView();
//incomingGameView.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(()-> {
                        v.setScaleX(1f);
                        v.setScaleY(1f);
                        getOnBackPressedDispatcher().onBackPressed();
                    }, 300);
                });
            });

            createLeagueClick.setOnClickListener(v ->
            {
                Intent intent = new Intent(this, CreateLeagueActivity.class);
                OpenActivityUtil.openColorHighlight(v, this, intent);

                new Handler().postDelayed(()-> getOnBackPressedDispatcher().onBackPressed(), 300);
            });

            createCommunityClick.setOnClickListener(v ->
            {
                v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(20).withEndAction(()->
                {
                    Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(()-> {
                        v.setScaleX(1f);
                        v.setScaleY(1f);
                        getOnBackPressedDispatcher().onBackPressed();
                    }, 300);
                });
            });

            inviteFriendsClick.setOnClickListener(v ->
            {
                v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(20).withEndAction(()->
                {
                    Photo_Video_Utils.shareDrawableImage(this, R.drawable.logo_new_name, getString(R.string.appInvite) );

                    new Handler().postDelayed(()-> {
                        v.setScaleX(1f);
                        v.setScaleY(1f);
                        getOnBackPressedDispatcher().onBackPressed();
                    }, 500);
                });
            });

            goPremiumClick.setOnClickListener(v ->
            {
                Intent intent = new Intent(this, PremiumActivity.class);
                OpenActivityUtil.openColorHighlight(v, this, intent);

                new Handler().postDelayed(()-> getOnBackPressedDispatcher().onBackPressed(), 300);

            });


        }
    }

    private View setHomeLastViews()
    {
        if(homeLastViews == null)
        {
            ViewStub homeLastLayout = findViewById(R.id.constraintLastLayer);
            homeLastViews = homeLastLayout.inflate();
            ImageView p2pHome_IV = homeLastViews.findViewById(R.id.p2pHome_IV);
            ImageView homeOpenMore = homeLastViews.findViewById(R.id.imageViewMore);
            ImageView homePage = homeLastViews.findViewById(R.id.homePage_IV);
            ImageView liveWatch_IV = homeLastViews.findViewById(R.id.liveWatch_IV);
            ImageView games_IV = homeLastViews.findViewById(R.id.games_IV);

            // Forward chat ids
            forwardDownContainer = homeLastViews.findViewById(R.id.forwardLastConst);
            totalUser_TV = homeLastViews.findViewById(R.id.userSelectedCount_TV);
            circleForwardSend = homeLastViews.findViewById(R.id.circleForwardSend);
            //  =========   onClicks

            p2pHome_IV.setOnClickListener(v -> {
                if(onUserLongPressView != null && onUserLongPressView.getVisibility() == View.VISIBLE)
                {
                    cancelUserDeleteOption();

                } else {
                    v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(10).withEndAction(() ->
                    {
                        Intent intent = new Intent(this, P2pExchangeActivity.class);
                        startActivity(intent);

                        // Reset the scale
                        new Handler().postDelayed(()-> {
                            v.setScaleX(1.0f);
                            v.setScaleY(1.0f);
                        }, 200);

                    }).start();
                }
            });

            homeOpenMore.setOnClickListener(v ->
            {
                if(onUserLongPressView != null && onUserLongPressView.getVisibility() == View.VISIBLE)
                {
                    cancelUserDeleteOption();
                } else {
                    setMoreOptionHomeView();
                    moreOptionHomeView.setVisibility(View.VISIBLE);
                    v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(20).withEndAction(()->
                    {
                        // Start the animation to make it visible
                        AnimUtils.slideInFromBottom(moreHomeCont2, 100);

                        new Handler().postDelayed(()-> {
                            v.setScaleX(1f);
                            v.setScaleY(1f);
                        }, 300);
                    });
                }
            });

            homePage.setOnClickListener(v -> {
                viewPager2General.setCurrentItem(0, true);
            });

            liveWatch_IV.setOnClickListener(v ->
            {
                if(onUserLongPressView != null && onUserLongPressView.getVisibility() == View.VISIBLE)
                {
                    cancelUserDeleteOption();
                } else {
                    v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(20).withEndAction(()->
                    {
                        Intent intent = new Intent(this, LiveGameActivity.class);
                        startActivity(intent);

                        new Handler().postDelayed(()-> {
                            v.setScaleX(1f);
                            v.setScaleY(1f);
                        }, 300);
                    });
                }
            });

            games_IV.setOnClickListener(v ->
            {
                if(onUserLongPressView != null && onUserLongPressView.getVisibility() == View.VISIBLE)
                {
                    cancelUserDeleteOption();
                } else {
                    if(onGameNow || isOnGameNow){
                        Toast.makeText(this, getString(R.string.finishIncomingGame), Toast.LENGTH_SHORT).show();
                    } else {
                        setSelectGameView();
                        addAnimToGameButton();
                        if(selectGameView != null) selectGameView.setVisibility(View.VISIBLE);
                    }
                }
            });

            // send forward message
            circleForwardSend.setOnClickListener(view ->
            {
                progressBarForward.setVisibility(View.VISIBLE);

                if(MainActivity.sharingPhotoActivated) // open send_image activity and prepare the photo
                {
                    sharing = true;
                    startActivity(new Intent(this, SendImageOrVideoActivity.class));

                } else if (onSelectPlayer)
                {
                    doneSelectingPlayers = true;
                    if(circleForwardSend != null) circleForwardSend.setVisibility(View.INVISIBLE);

                    PhoneUtils.hasInternetConnectivity(new PhoneUtils.CheckInternet() {
                        @Override
                        public void networkIsTrue() {
                            cancelForwardSettings(MainActivity.this);
                            moveToAwaitPlayers(gameMode, stakeAmount);  // multiple player
                            if(circleForwardSend != null) circleForwardSend.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void networkIsFalse() {
                            if(progressBarForward != null) progressBarForward.setVisibility(View.GONE);
                            if(circleForwardSend != null) circleForwardSend.setVisibility(View.VISIBLE);
                            Toast.makeText(MainActivity.this, getString(R.string.noInternetConnection), Toast.LENGTH_SHORT).show();
                        }
                    });

                } else if (onForward)   // send forward or share chat
                {
                    view.animate().scaleX(1.2f).scaleY(1.2f)
                            .setDuration(50).withEndAction(() -> {

                                sendSharedChat(this);

                                // Reset the scale
                                view.setScaleX(1.0f);
                                view.setScaleY(1.0f);

                            }).start();
                }

                new Handler().postDelayed(()-> progressBarForward.setVisibility(View.GONE), 5000);

            });

        }

        return homeLastViews;
    }

    private void moveToAwaitPlayers(String gameMode_, String stakeAmount_ ) // host is starting a new fresh game
    {
        // add my details
        String mySafeImageLink = imageLink != null ? imageLink : "null";
        String myName = ProfileUtils.getMyDisplayOrUsername();
        selectedPlayerMList.add(new AwaitPlayerM(mySafeImageLink, myName, myId, "hostAdmin", false));

        gameID = GameUtils.gameId();    // generating gameId for the first time
        hostUid = myId;
        gameMode = gameMode_;

        numberOfPlayers = String.valueOf(selectedPlayerMList.size());
        totalStake = String.valueOf( (Double.parseDouble(stakeAmount_) * Integer.parseInt(numberOfPlayers)) );

        forwardChatUserId.forEach(uid-> {
            sendMessage(getString(R.string.whotGame), getString(R.string.connect), 7, null, null, uid, false); // audio or video call
        });

        if (circleForwardSend != null) circleForwardSend.setVisibility(View.INVISIBLE);

        Intent intent = new Intent(this, AwaitPlayersActivity.class);
        intent.putExtra("mode", gameMode);
        intent.putExtra("hostName", myName);
        intent.putExtra("hostUid", hostUid);
        intent.putExtra("gameID", gameID);
        startActivity(intent);

        gameSharePref.edit().putString(K.ongoingGameId, gameID).apply();
        gameSharePref.edit().putString(gameHostUid, hostUid).apply();

    }

    private void setSelectGameView()
    {
        if(selectGameView == null)
        {
            ViewStub selectGameViewStub = findViewById(R.id.selectGameViewStub);
            selectGameView = selectGameViewStub.inflate();

            closePageIV = selectGameView.findViewById(R.id.closePageIV);
            whotButton = selectGameView.findViewById(R.id.whotButton);
            chessButton = selectGameView.findViewById(R.id.chessButton);
            pokerButton = selectGameView.findViewById(R.id.pokerButton);
            scrabbleButton = selectGameView.findViewById(R.id.scrabbleButton);
            riddleButton = selectGameView.findViewById(R.id.riddleButton);
            diceButton = selectGameView.findViewById(R.id.diceButton);


            whotButton.setOnClickListener(v -> {
                v.animate().scaleX(1.1f).scaleY(1.1f).withEndAction(()->
                {
                    Intent intent = new Intent(this, WhotOptionActivity.class);
                    startActivity(intent);

                    v.setScaleX(1f);
                    v.setScaleY(1f);
                    new Handler().postDelayed(()-> selectGameView.setVisibility(View.GONE), 500);

                    if( (!deviceOnNightMode && nightMood) || (deviceOnNightMode && !nightMood)) activateAllViews();
                });
            });

            chessButton.setOnClickListener(v -> {
                v.animate().scaleX(1.1f).scaleY(1.1f).withEndAction(()->
                {
                    Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
                    if(targetPlayer) {
                        //...
                    } else {

                    }
                    if( (!deviceOnNightMode && nightMood) || (deviceOnNightMode && !nightMood)) activateAllViews();

                    v.setScaleX(1f);
                    v.setScaleY(1f);
                });
            });

            pokerButton.setOnClickListener(v -> {
                v.animate().scaleX(1.1f).scaleY(1.1f).withEndAction(()->
                {
                    if(targetPlayer) {
                        //...
                    } else {

                    }
                    Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();

                    if( (!deviceOnNightMode && nightMood) || (deviceOnNightMode && !nightMood)) activateAllViews();

                    v.setScaleX(1f);
                    v.setScaleY(1f);
                });
            });

            scrabbleButton.setOnClickListener(v -> {
                v.animate().scaleX(1.1f).scaleY(1.1f).withEndAction(()->
                {
                    if(targetPlayer) {
                        //...
                    } else {

                    }
                    Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();

                    if( (!deviceOnNightMode && nightMood) || (deviceOnNightMode && !nightMood)) activateAllViews();

                    v.setScaleX(1f);
                    v.setScaleY(1f);
                });
            });

            riddleButton.setOnClickListener(v -> {
                v.animate().scaleX(1.1f).scaleY(1.1f).withEndAction(()->
                {
                    Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();

                    if( (!deviceOnNightMode && nightMood) || (deviceOnNightMode && !nightMood)) activateAllViews();

                    v.setScaleX(1f);
                    v.setScaleY(1f);
                });
            });

            diceButton.setOnClickListener(v -> {
                v.animate().scaleX(1.1f).scaleY(1.1f).withEndAction(()->
                {
                    Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();

                    if( (!deviceOnNightMode && nightMood) || (deviceOnNightMode && !nightMood)) activateAllViews();

                    v.setScaleX(1f);
                    v.setScaleY(1f);
                });
            });


            closePageIV.setOnClickListener(v -> {
                getOnBackPressedDispatcher().onBackPressed();
            });

        }

    }

    private void targetUserOnGame()
    {
        if( !AwaitPlayersActivity.PlayersUid.playerUidList.contains(otherUserUid))
        {
            if(!onGameNow && !isOnGameNow) {
                selectedPlayerMList.clear();

                String safeImageLink = imageUri != null ? imageUri : "null"; // Provide a default value or handle null
                AwaitPlayerM awaitPlayerM = new AwaitPlayerM(safeImageLink, otherUserName, otherUserUid, "signal", false);
                if( !selectedPlayerMList.contains(awaitPlayerM) ) selectedPlayerMList.add(awaitPlayerM);

                setSelectGameView();
                addAnimToGameButton();
                if(selectGameView != null) selectGameView.setVisibility(View.VISIBLE);
                targetPlayer = true;

                if(!forwardChatUserId.contains(otherUserUid)) forwardChatUserId.add(otherUserUid);
                if(!selectedUserNames.contains(otherUserName)) selectedUserNames.add(otherUserName);  // Add username to the List

            } else {
                Toast.makeText(this, getString(R.string.onGoingGameAlready), Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(this, getString(R.string.playerAlreadyInGame), Toast.LENGTH_SHORT).show();
        }

    }

    private void addAnimToGameButton()
    {
        whotButton.setVisibility(View.GONE);
        chessButton.setVisibility(View.GONE);
        pokerButton.setVisibility(View.GONE);
        scrabbleButton.setVisibility(View.GONE);
        riddleButton.setVisibility(View.GONE);
        diceButton.setVisibility(View.GONE);

        AnimUtils.slideInFromRight(whotButton, 400);
        AnimUtils.slideInFromLeft(chessButton, 400);
        AnimUtils.slideInFromRight(pokerButton, 400);
        AnimUtils.slideInFromLeft(scrabbleButton, 400);
        AnimUtils.slideInFromRight(riddleButton, 400);
        AnimUtils.slideInFromLeft(diceButton, 400);
    }

    private void setFileOptionViews()
    {
        if(fileOptionViews == null)
        {
            fileAttachOptionContainer = findViewById(R.id.FileAttachOptionContainer);
            fileOptionViews = fileAttachOptionContainer.inflate();

            fileContainerAnim = fileOptionViews.findViewById(R.id.fileContainerAnim);
            ImageView galleryIV = fileOptionViews.findViewById(R.id.galleryIV);
            ImageView documentIV = fileOptionViews.findViewById(R.id.documentIV);
            ImageView audioIV = fileOptionViews.findViewById(R.id.audioIV);
            ImageView contactIV = fileOptionViews.findViewById(R.id.contactIV);
            ImageView gameIV = fileOptionViews.findViewById(R.id.gameIV);

//  ================     Send Documents and camera onClick Settings      ===================

            fileOptionViews.setOnClickListener(v -> {
                fileOptionViews.setVisibility(View.GONE);
                fileContainerAnim.setVisibility(View.INVISIBLE);
            });

            // use for document
            documentIV.setOnClickListener(view -> {
                view.animate().scaleX(1.2f).scaleY(1.2f).setDuration(30).withEndAction(()->
                {
                    forwardChatUserId.clear();
                    selectedUserNames.clear();
                    forwardChatUserId.add(otherUserUid);
                    selectedUserNames.add(otherUserName);
                    pickDocumentLauncher.launch(ACCEPTED_MIME_TYPES);

                    fileOptionViews.setVisibility(View.GONE);
                    fileContainerAnim.setVisibility(View.INVISIBLE);

                    new Handler().postDelayed(()-> {
                        view.setScaleX(1f);
                        view.setScaleY(1f);
                    }, 300);
                });
            });

            galleryIV.setOnClickListener(view -> {
                view.animate().scaleX(1.2f).scaleY(1.2f).setDuration(30).withEndAction(()->
                {
                    forwardChatUserId.clear();
                    selectedUserNames.clear();
                    chatModelList.clear();
                    cancelChatOption();
                    isSendingFile = true;
                    sharingPhotoActivated = false;
                    selectedUserNames.add(otherUserName);
                    forwardChatUserId.add(otherUserUid);

                    Intent i = new Intent(this, SendImageOrVideoActivity.class);
                    startActivity(i);

                    fileOptionViews.setVisibility(View.GONE);
                    fileContainerAnim.setVisibility(View.INVISIBLE);

                    // call runnable to check for network
                    handlerTyping.post(runnableTyping);
                    new Handler().postDelayed(()-> {
                        view.setScaleX(1f);
                        view.setScaleY(1f);
                    }, 200);
                });
            });

            audioIV.setOnClickListener(view -> {
                view.animate().scaleX(1.2f).scaleY(1.2f).setDuration(30).withEndAction(()->
                {
                    forwardChatUserId.add(otherUserUid);
                    selectedUserNames.add(otherUserName);
                    launchMultipleAudioPicker();

                    fileOptionViews.setVisibility(View.GONE);
                    fileContainerAnim.setVisibility(View.INVISIBLE);

                    new Handler().postDelayed(()-> {
                        view.setScaleX(1f);
                        view.setScaleY(1f);
                    }, 500);
                });
            });

            gameIV.setOnClickListener(v -> {
                v.animate().scaleX(1.1f).scaleY(1.1f).withEndAction(()->
                {
                    targetUserOnGame();

                    new Handler().postDelayed(()-> {
                        fileOptionViews.setVisibility(View.GONE);
                        slideOutToBottom(fileContainerAnim, 100);
                        v.setScaleX(1f);
                        v.setScaleY(1f);
                    }, 300);
                });

            });

            contactIV.setOnClickListener(v -> {
                v.animate().scaleX(1.2f).scaleY(1.2f).withEndAction(()->
                {
                    Toast.makeText(this, "Work in progress", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(()-> {
                        v.setScaleX(1f);
                        v.setScaleY(1f);
                    }, 500);
                });
            });

        }
    }

    public void setPinForWhoViews()
    {
        if(pinForWhoViews == null)
        {
            ViewStub pinOptionBox = findViewById(R.id.pinOptionBoxConstr);
            pinForWhoViews = pinOptionBox.inflate();
            pinMineTV = pinForWhoViews.findViewById(R.id.textViewPinMine);
            pinEveryoneTV = pinForWhoViews.findViewById(R.id.textViewPinEveryone);
            cancelPinOption = pinForWhoViews.findViewById(R.id.imageViewCancelPin);

            //  ========== pin message for only me -- private
            pinMineTV.setOnClickListener(view -> {
                pinAndUnpinChatPrivately();
                clearAllHighlights();
                pinIconsVisibility(otherUserUid);
            });

            // pin message for everyone
            pinEveryoneTV.setOnClickListener(view -> {
                pinAndUnpinChatForEveryone();   // call pin/unpin method
                clearAllHighlights();
                pinIconsVisibility(otherUserUid);
            });

            //  close pin option box
            View.OnClickListener closePinOption = view -> {
                pinForWhoViews.setVisibility(View.GONE);
                cancelChatOption();
            };
            cancelPinOption.setOnClickListener(closePinOption); // close when the cancel pin is click
            pinForWhoViews.setOnClickListener(closePinOption);    // close when the background is click

        }
    }


    private void setDeleteForWhoView()
    {
        if(deleteForWhoView == null)
        {
            ViewStub constraintDelBody = findViewById(R.id.constDelBody);
            deleteForWhoView = constraintDelBody.inflate();

            TextView deleteChatForOnlyMe_TV = deleteForWhoView.findViewById(R.id.deleteChatForOnlyMe_TV);
            deleteChatForOnlyOther_TV = deleteForWhoView.findViewById(R.id.deleteChatForOnlyOther_TV);
            TextView deleteChatsForEveryone_TV = deleteForWhoView.findViewById(R.id.deleteChatsForEveryone_TV);
            ImageView imageViewCancelDel = deleteForWhoView.findViewById(R.id.imageViewCancelDel);

            //  ==================    delete message onClicks     ============================

            deleteForWhoView.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

            deleteChatForOnlyMe_TV.setOnClickListener(view -> {
                int size = chatModelList.size();
                for (int i = 0; i < size; i++){
                    MessageModel chatModel = chatModelList.get(i);

                    // check if photo uri is not null and delete if it exist
                    deleteFileFromPhoneStorage(chatModel); // delete mine

                    // delete from my local list
                    MessageAdapter adapter = adapterMap.get(otherUserUid);
                    adapter.deleteMessage(chatModel.getIdKey());

                    // update outside user UI chatList model
                    if(ChatsFragment.adapter != null) UserChatUtils.findUserAndDeleteChat(ChatsFragment.adapter, otherUserUid, chatModel.getIdKey());
                    UserChatUtils.findUserAndDeleteChat(PlayersFragment.adapter, otherUserUid, chatModel.getIdKey());

                    // delete the ROOM outside UI
                    chatViewModel.editOutsideChat( otherUserUid, K.DELETE_ICON + " ...",
                            null, chatModel.getIdKey());

                    // delete inside chat from ROOM
                    chatViewModel.deleteChat(chatModel);

//                refMessages.child(myUserName).child(otherUserName).child(idKey).getRef().removeValue();
                    refMsgFast.child(myId).child(otherUserUid).child(chatModel.getIdKey()).getRef().removeValue();
                    refEditMsg.child(myId).child(otherUserUid).child(chatModel.getIdKey()).getRef().removeValue();

                    deleteForWhoView.setVisibility(View.GONE);

                    if(i == size-1){
                        cancelChatOption();
                        Toast.makeText(MainActivity.this, getString(R.string.deleteForMe), Toast.LENGTH_SHORT).show();

                        new Thread(()-> getEachUserAllPhotoAndVideos(otherUserUid, adapterMap.get(otherUserUid).getModelList())).start();
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

                    if(i == size-1){    // no need to call adapter since you are only deleting for other user.
                        cancelChatOption();
                        deleteForWhoView.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, getString(R.string.deleteForOthers) +" "+otherUserName+".", Toast.LENGTH_SHORT).show();
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
                    assert adapter != null;
                    adapter.deleteMessage(chatModel.getIdKey());

                    // save to delete database to loop through the other user local list and delete if idKey is found
                    deleteMap.put("idKey", chatModel.getIdKey());
                    refDeleteMsg.child(otherUserUid).child(myId).child(chatModel.getIdKey()).setValue(deleteMap);


                    try{
                        // delete chat from ROOM - outside UI
                        chatViewModel.editOutsideChat(otherUserUid, K.DELETE_ICON + "  ...",
                                null, chatModel.getIdKey());

                        // update outside user UI chatList model
                        if(ChatsFragment.adapter != null) UserChatUtils.findUserAndDeleteChat(ChatsFragment.adapter, otherUserUid, chatModel.getIdKey());
                        UserChatUtils.findUserAndDeleteChat(PlayersFragment.adapter, otherUserUid, chatModel.getIdKey());

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
                        deleteForWhoView.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, getString(R.string.deleteForEveryone), Toast.LENGTH_SHORT).show();

                        new Thread(()-> getEachUserAllPhotoAndVideos(otherUserUid, adapterMap.get(otherUserUid).getModelList())).start();
                    }
                }

            });

            // Close delete message option
            constraintDelBody.setOnClickListener(view -> cancelChatDeleteOption());
            imageViewCancelDel.setOnClickListener(view -> cancelChatDeleteOption());

        }
    }

    private void setTopEmojiView() {
        if (topEmojiView == null) {
            ViewStub topEmoji = findViewById(R.id.topEmojiViewStub);
            topEmojiView = topEmoji.inflate();

            TableLayout tableLayout = topEmojiView.findViewById(R.id.tableLayout);

            // Iterate through each row
            for (int i = 0; i < tableLayout.getChildCount(); i++) {
                View row = tableLayout.getChildAt(i);
                if (row instanceof TableRow) {
                    TableRow tableRow = (TableRow) row;

                    setTableRowOnClick(tableRow);
                }
            }
        }
    }

    private void setOnChatClickView()
    {
        if(onChatClickView == null)
        {
            ViewStub onChatClickVS = findViewById(R.id.onChatClickVS);

            onChatClickView = onChatClickVS.inflate();

            ConstraintLayout replyLayout = onChatClickView.findViewById(R.id.replyLayout);
            animateView = onChatClickView.findViewById(R.id.animateView);
            editLayout = onChatClickView.findViewById(R.id.editLayout);
            ConstraintLayout pinLayout = onChatClickView.findViewById(R.id.pinLayout);
            ConstraintLayout copyLayout = onChatClickView.findViewById(R.id.copyLayout);
            ConstraintLayout forwardLayout = onChatClickView.findViewById(R.id.forwardLayout);
            ConstraintLayout deleteLayout = onChatClickView.findViewById(R.id.deleteLayout);
            TableRow tableRow = onChatClickView.findViewById(R.id.tableRow);

            editOrShareTV = onChatClickView.findViewById(R.id.editOrShareTV);
            editOrShareIcon = onChatClickView.findViewById(R.id.editOrShareIcon);

            pinChatTV = onChatClickView.findViewById(R.id.pinChatTV);
            pinIcon = onChatClickView.findViewById(R.id.pinChatIV);


            //  ======    onClicks

            onChatClickView.setOnClickListener(v -> {
                onChatClickView.setVisibility(View.GONE);
            });

            replyLayout.setOnClickListener(v -> {
                onEditOrReplyMessage_(modelChatsOption, "reply", "replying...", R.drawable.reply);
            });

            editLayout.setOnClickListener(onEdit);

            pinLayout.setOnClickListener(onPin);

            copyLayout.setOnClickListener(onCopy);

            forwardLayout.setOnClickListener(forwardChat);

            deleteLayout.setOnClickListener(onDelete);

            setTableRowOnClick(tableRow);

        }

    }

    private void setTableRowOnClick(TableRow tableRow)
    {
        // Iterate through each cell in the row
        for (int j = 0; j < tableRow.getChildCount(); j++) {
            View cell = tableRow.getChildAt(j);
            if (cell instanceof TextView)
            {
                TextView textView = (TextView) cell;

                textView.setOnClickListener(v ->    // onClick
                {
                    String text = textView.getText().toString();
                    String getChatID = modelChatsOption.getIdKey();

                    if(text.equals(getString(R.string.more_)))
                    {
                        onEmojiReact(getChatID);

                        pinIconsVisibility(otherUserUid);
                        generalBackground.setVisibility(View.GONE);
                        moreOptionViews.setVisibility(View.GONE);
                        if(topEmojiView != null) AnimUtils.fadeOutGone(topEmojiView, 500);
                        if(onChatClickView != null) onChatClickView.setVisibility(View.GONE);

                    } else {
                        MessageAdapter messageAdapter = adapterMap.get(otherUserUid);
                        // add emoji to local list and ROOM
                        String chat = messageAdapter.addEmojiReact(text, getChatID, otherUserUid);  // add emoji to inside chat

                        String myDisplayName = myProfileShareRef.getString(K.PROFILE_DISNAME, "@"+myUserName);
                        if(myDisplayName.length() > 15) myDisplayName = myDisplayName.substring(0, 14);

                        // send a signal to add emoji for other user also
                        Map<String, Object> emojiMap = new HashMap<>();
                        emojiMap.put("emojiReact", text);
                        emojiMap.put("chatID", getChatID);
                        emojiMap.put("reactByWho", myDisplayName);

                        refEmojiReact.child(otherUserUid).child(myId).push().setValue(emojiMap);

                        if(ChatsFragment.adapter != null)   // update outside user chat list
                        {
                            UserOnChatUI_Model outsideUserModel = ChatsFragment.adapter.findUserModelByUid(otherUserUid);
                            List<UserOnChatUI_Model> userList = ChatsFragment.adapter.userModelList;

                            updateOutsideChatWithReact(getString(R.string.you), userList, outsideUserModel, chat, text, false, true, otherUserUid);
                        }

                        if(PlayersFragment.adapter != null)     // update outside chat on playerFragment
                        {
                            UserOnChatUI_Model outsideUserModel = PlayersFragment.adapter.findUserModelByUid(otherUserUid);
                            List<UserOnChatUI_Model> userList = PlayersFragment.adapter.userModelList;

                            updateOutsideChatWithReact(getString(R.string.you), userList, outsideUserModel, chat, text, true, false, otherUserUid);
                        }

                        getOnBackPressedDispatcher().onBackPressed();
                    }

                });
            }
        }
    }

    private void setOnUserLongPressView()
    {
        if(onUserLongPressView == null)
        {
            ViewStub userLongPressVS = findViewById(R.id.userLongPressVS);
            onUserLongPressView = userLongPressVS.inflate();

            ImageView cancel_IV = onUserLongPressView.findViewById(R.id.cancel_IV);
            count_TV = onUserLongPressView.findViewById(R.id.count_TV);
            ImageView playGame_IV = onUserLongPressView.findViewById(R.id.playGame_IV);
            pinUser_IV = onUserLongPressView.findViewById(R.id.pinUser_IV);
            ImageView muteUserIV = onUserLongPressView.findViewById(R.id.muteUserIV);
            deleteUser_IV = onUserLongPressView.findViewById(R.id.deleteUser_IV);
            ImageView moreOptionIV = onUserLongPressView.findViewById(R.id.moreOptionIV);

            deleteUser_IV.setOnClickListener(v -> {
                setDeleteUserOrClearChatViews();
                deleteUserForMe_TV.setText(R.string.delete_for_me);
                deleteUserForAll_TV.setText(R.string.delete_for_everyone);
                deleteUserOrClearChatViews.setVisibility(View.VISIBLE);
            });

            moreOptionIV.setOnClickListener(v ->
            {
                if(onUserMoreLongPressView.getVisibility() == View.GONE)
                {
                    onUserMoreLongPressView.setVisibility(View.VISIBLE);
                    AnimUtils.slideInFromRight(userLongPressMoreSub, 80);
                } else {
                    onUserMoreLongPressView.setVisibility(View.GONE);
                    userLongPressMoreSub.setVisibility(View.GONE);
                }

                if(userModelList.size() > 1) viewProfile_TV.setVisibility(View.GONE);
                else viewProfile_TV.setVisibility(View.VISIBLE);

            });

            cancel_IV.setOnClickListener(v -> cancelUserDeleteOption());

            playGame_IV.setOnClickListener(v -> {
                PlayersFragment.adapter.userModelList.clear();
                PlayersFragment.adapter.notifyDataSetChanged();
            });
        }
    }

    private void setOnUserMoreLongPressView()
    {
        if(onUserMoreLongPressView == null)
        {
            ViewStub userMoreLongPressVS = findViewById(R.id.userLongPressMoreVS);
            onUserMoreLongPressView = userMoreLongPressVS.inflate();

            userLongPressMoreSub = onUserMoreLongPressView.findViewById(R.id.userLongPressMore);
            TextView playGameIV = onUserMoreLongPressView.findViewById(R.id.playGameIV);
            viewProfile_TV = onUserMoreLongPressView.findViewById(R.id.viewProfile_TV);
            TextView markAsRead = onUserMoreLongPressView.findViewById(R.id.markAsRead);
            TextView blockAndReport = onUserMoreLongPressView.findViewById(R.id.report_TV);

            onUserMoreLongPressView.setOnClickListener(v -> onUserMoreLongPressView.setVisibility(View.GONE));

        }
    }

    private void activateAllViews() {
        activateIncomingCallView();
        activateAudioOrVideoOptionView();
        setCheckNetworkView();
        setWalletVerifyViews();
        setChatOptionView();
        setMoreOptionViews();
        setDeleteUserOrClearChatViews();
        setChatMenuViews();
//        setFirstTopChatViews();
        setOnForwardTopView();
        setMoreOptionHomeView();
//        setHomeLastViews();
//        setSelectGameView();
        setFileOptionViews();
        setPinForWhoViews();
        setDeleteForWhoView();
        setTopEmojiView();
        setOnChatClickView();
        setOnUserLongPressView();
        setOnUserMoreLongPressView();
    }

    //  --------------- methods && interface --------------------


    @Override
    public void onLongPressUser(UserOnChatUI_Model userModel)
    {
        onUserLongPress = true;

        setOnUserLongPressView();
        setOnUserMoreLongPressView();
        setDeleteUserOrClearChatViews();

        onUserLongPressView.setVisibility(View.VISIBLE);
        deleteUser_IV.setVisibility(View.VISIBLE);
        pinUser_IV.setVisibility(View.VISIBLE);

        if(userModelList.contains(userModel)) userModelList.remove(userModel);
        else userModelList.add(userModel);

        int listSize = userModelList.size();

        if(listSize == 0) {
            cancelUserDeleteOption();
        }else if(listSize == 1){
            String name = getOtherUserName(userModelList.get(0), null);
            otherUserName_TV.setText(name);
        } else {
            otherUserName_TV.setText(getString(R.string.deleteNow));

            if(listSize > 4) {
                deleteUser_IV.setVisibility(View.GONE);
                pinUser_IV.setVisibility(View.GONE);
            }
        }

        count_TV.setText(""+listSize);

    }

    private String getOtherUserName(UserOnChatUI_Model userModel, MessageModel chatModel)
    {
        String otherDisplayName__ = userModel != null ? userModel.getOtherDisplayName() : chatModel.getSenderName();
        String otherUserName__ = userModel != null ? userModel.getOtherUserName() : chatModel.getSenderName();
        String otherId = userModel != null ? userModel.getOtherUid() : chatModel.getFromUid();

        if(otherId.equals(myId)) return getString(R.string.you);

        // return the contact name or displayed name of other user
        return contactNameShareRef.getString(otherId, otherDisplayName__ != null ? otherDisplayName__ : "@"+otherUserName__);

    }

    private String getOthername(String otherId, String otherDisplayName__, String otherUserName__)
    {
        // return the contact name or displayed name of other user
        return contactNameShareRef.getString(otherId, otherDisplayName__ != null ? otherDisplayName__ : "@"+otherUserName__);

    }

    @Override
    public void callAllMethods(String otherUid, Context context, Activity activity, boolean onNotification)
    {
        getMyUserTyping(otherUid);
        tellUserAmTyping_AddUser();
        if(!onNotification) getChatReadRequest(otherUid);

        emoji_IV.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);

//        if(pinChatViews != null) pinIconsVisibility(otherUid);
        if(firstTopChatViews != null) pinIconsVisibility(otherUid);

        popup = EmojiPopup.Builder.fromRootView( recyclerContainer).build(editTextMessage);

        insideChatMap.put(otherUid, true);

        fileNamePath = getRecordFilePath(context);     // regenerate filePathName

        if(networkOk)  // reload failed chat if network is okay
            new Handler().postDelayed(() -> reloadFailedMessagesWhenNetworkIsOk(), 500);

        // initialize voice note
        voiceNote(context, activity);

        // clear all notification bar
        new Thread(()-> NotificationHelper.clearAllMessagesAndNotifications(context));
    }


    @Override
    public void openChatClickOption(boolean open, MessageModel messageModel, int totalSize, int chatPosition_)
    {

        setOnChatClickView();

        if(open)
        {
            if(onChatClickView != null)
            {
                onChatClickView.setVisibility(View.VISIBLE);
                editLayout.setVisibility(View.VISIBLE);
                AnimUtils.slideInFromBottom(animateView, 100);

                togglePinAndShareIconMain(messageModel, totalSize, chatPosition_, myId);

                if(!messageModel.getFromUid().equals(myId)) editLayout.setVisibility(View.GONE);
            }

        } else {
            onChatClickView.setVisibility(View.GONE);
        }

        setPinLayout(messageModel);

        chatModelList.clear();
        chatModelList.add(messageModel);
    }

    private void togglePinAndShareIconMain(MessageModel model, int totalSize, int chatPosition, String myId)
    {
        int positionCheck = totalSize - chatPosition;    // e.g 1000 - 960 => 40
        if(positionCheck < 100)
        {
            editOrShareIcon.setImageResource(R.drawable.baseline_mode_edit_24);
            editOrShareTV.setText(getString(R.string.edit));
            if(model.getFromUid().equals(myId)) editLayout.setVisibility(View.VISIBLE);

        } else {
            editLayout.setVisibility(View.GONE);
        }

        if (model.getType() != 0 || (!model.getFromUid().equals(myId) && model.getType() != 0) )
        {   // set image and document => type 0 is for just text-chat, type 1 is voice_note, type 2 is photo, type 3 is document, type 4 is audio (mp3)
            editOrShareIcon.setImageResource(R.drawable.baseline_share_24);
            editOrShareTV.setText(getString(R.string.share));

            onShare = true;

            editLayout.setVisibility(View.VISIBLE);

        } else {
            onShare = false;
        }

        modelChatsOption = model;  // assign the last model
    }

    private void setPinLayout(MessageModel messageModel)
    {
        setPinForWhoViews();

        boolean isPrivatePin = false;
        boolean isPublicPin = false;
        for (PinMessageModel pinMes : pinPrivateChatMap.get(otherUserUid))
        {
            if (pinMes.getMsgId().equals(messageModel.getIdKey())) {
                isPrivatePin = true;
                break;
            }
        }
        for (PinMessageModel pinChatEveryone :
                pinPublicChatMap.get(otherUserUid)) {

            if (pinChatEveryone.getMsgId().equals(messageModel.getIdKey())) {
                isPublicPin = true;
                break;
            }
        }

        if(isPrivatePin && isPublicPin){
            pinIcon.setImageResource(R.drawable.unpin);
            pinChatTV.setText(getString(R.string.unpin));
            pinTV.setText(getString(R.string.unpin));

            pinMineTV.setText(getString(R.string.unpinForMe));
            pinEveryoneTV.setText(getString(R.string.unpinForEveryone));

        }else {
            if(isPrivatePin)
            {
                pinChatTV.setText(getString(R.string.unpin_));
                pinTV.setText(getString(R.string.unpin_));

                MainActivity.pinMineTV.setText(getString(R.string.unpinForMe));
                MainActivity.pinEveryoneTV.setText(getString(R.string.pinForEveryone));

            } else if (isPublicPin)
            {
                pinChatTV.setText(getString(R.string.unpin_));
                pinTV.setText(getString(R.string.unpin_));

                pinEveryoneTV.setText(getString(R.string.unpinForEveryone));
                pinMineTV.setText(getString(R.string.pinForMe));

            } else
            {
                pinChatTV.setText(getString(R.string.pinChat));
                pinTV.setText(getString(R.string.pinChat));

                MainActivity.pinMineTV.setText(getString(R.string.pinForMe));
                MainActivity.pinEveryoneTV.setText(getString(R.string.pinForEveryone));
            }
            pinIcon.setImageResource(R.drawable.baseline_push_pin_24);
        }
    }

    @Override
    public void setDelAndPinForWho(boolean makeEmojiVisible, boolean makeTopViewVisible)    // when item is onLongPress
    {
        setTopEmojiView();  // visible emoji layout
        if(makeEmojiVisible) AnimUtils.fadeInVisible(topEmojiView, 300);
        else AnimUtils.fadeOutGone(topEmojiView, 500);

        setPinForWhoViews();
        setDeleteForWhoView();

        firstTopChatViews.setVisibility(View.VISIBLE);
        chatOptionView.setVisibility(View.VISIBLE);

        if(!makeTopViewVisible){
            firstTopChatViews.setVisibility(View.GONE);
        }

        if(!makeEmojiVisible && makeTopViewVisible){
            chatOptionView.setVisibility(View.GONE);
        }

    }

    @Override
    public void chatBodyVisibility(String otherName, String imageUrl, String myUsername, String uID, Context mContext_,
                                  RecyclerView recyclerChat)
    {
        editTextMessage.setFocusableInTouchMode(true);
        editTextMessage.requestFocus();

        recyclerViewChatVisibility(uID);    // make only the active recyclerView to be visible

        try {
            setFirstTopChatViews(); // chatBodyVisibility()
            if(firstTopChatViews.getVisibility() != View.VISIBLE) firstTopChatViews.setVisibility(View.VISIBLE);
        } catch (Exception e){
            System.out.println("what is error MainAct L3255: " + e.getMessage());
        }

//        AnimUtils.slideInFromRight(constraintMsgBody, 200);
        constraintMsgBody.setVisibility(View.VISIBLE);  // make chat box visible
        topMainContainer.setVisibility(View.INVISIBLE);
        tabLayoutGeneral.setVisibility(View.INVISIBLE);

        // make progressBar visible at first
        if(userRecyclerActiveMap.get(uID) == null) progressBarLoadChats.setVisibility(View.VISIBLE);
//        if(userRecyclerActiveMap.get(uID) != null)  populateRecyclerWithAdapter(myUsername, uID, mContext_, recyclerChat);

        new Handler().postDelayed(()->
        {
//            if(userRecyclerActiveMap.get(uID) == null) populateRecyclerWithAdapter(myUsername, uID, mContext_, recyclerChat);
            populateRecyclerWithAdapter(myUsername, uID, mContext_, recyclerChat);

            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerMap.get(uID).getLayoutManager();
            assert layoutManager != null;
            downMsgCount = (layoutManager.getItemCount() - 1) - layoutManager.findLastVisibleItemPosition();
            String numb = ""+ downMsgCount;
            scrollCountTV.setText(numb);           // set down msg count
            downMsgCountMap.put(uID, downMsgCount);     // set it for "sending message method"

            displayOrHideCountArrowDown(downMsgCount, true);  //  check count and display/hide the scroll arrow


            //        Add an OnScrollListener to the RecyclerView
            recyclerMap.get(uID).addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy)
                {
                    super.onScrolled(recyclerView, dx, dy);

                    // keep saving the recycler position while scrolling
                    scrollNum = layoutManager.findLastVisibleItemPosition();
                    scrollNumMap.put(uID, scrollNum);

                    // keep updating the number of down messages
                    downMsgCount = (layoutManager.getItemCount() - 1) - scrollNum;
                    scrollCountTV.setText(""+downMsgCount);

                    //  store the downMsgCount in a map for each user, to enable me add to the number on sendMessage() when I send new message
                    downMsgCountMap.put(uID, downMsgCount);

                    displayOrHideCountArrowDown(downMsgCount, false);

                    if(topEmojiView != null) AnimUtils.fadeOutGone(topEmojiView, 500);
                }
            });


            // Attach the ItemTouchHelper to your RecyclerView
            if(recyclerMap.get(uID) != null) itemTouchSwipe.attachToRecyclerView(recyclerMap.get(uID));


        }, 100);

        textViewOtherUser.setText(otherName);   // display their userName on top of their page

        String otherUserHint = otherUserHintRef.getString(uID, mContext_.getString(R.string.hint2));
        bioHint_TV.setText(otherUserHint);
        AnimUtils.animateView(bioHint_TV);

        //  get user image
        if (imageUrl == null || imageUrl.equals("null")) {
            circleImageLogo.setImageResource(R.drawable.person_round_white);
        }
        else Picasso.get().load(imageUrl).into(circleImageLogo);

        otherUserName = otherName;
        myUserName = myUsername;
        imageUri = imageUrl;
        otherUserUid = uID;

    }

    private void populateRecyclerWithAdapter(String myUserName_, String uID, Context context, RecyclerView recyclerChat)
    {
        if(userRecyclerActiveMap.get(uID) == null) {

            if(recyclerMap.get(uID) == null)
            {
                sendRecyclerView(recyclerChat, uID);
                getMessage(myUserName_, uID, context, true);
            } else
            {
                recyclerMap.get(uID).setAdapter(adapterMap.get(uID));   // set the adapter to the recyclerView
                new Handler().postDelayed(()-> progressBarLoadChats.setVisibility(View.GONE), 100);
            }


//                loadOtherUserChatsToRecycler();

        } else
            progressBarLoadChats.setVisibility(View.GONE);

        if(adapterMap.get(uID) != null){
            // check recycler position before scrolling
            int position = lastPositionPreference.getInt(uID, adapterMap.get(uID).getItemCount() - 1);
            int scrollCheck = adapterMap.get(uID).getItemCount() - position;

            if(scrollCheck > 0 && scrollCheck < 5)   // scroll to last position
            {
                recyclerMap.get(uID).scrollToPosition(adapterMap.get(uID).getItemCount() - 1);
            } else if(userRecyclerActiveMap.get(uID) == null)
            {
                recyclerMap.get(uID).scrollToPosition(position - 10);
            }

            scrollNum = position;
            scrollNumMap.put(uID, position);

            userRecyclerActiveMap.put(uID, true);

            System.out.println("Total adapter (MainAct L2500) " + adapterMap.get(uID).getItemCount());
        }

        if(chatOptionView == null) {   //  activate long press chat options
            new Handler().postDelayed(this::setChatOptionView, 3000);
        }

    }

    private void displayOrHideCountArrowDown(int downMsgCount, boolean yes)
    {
        if(downMsgCount < 3){
            scrollCountTV.setVisibility(View.GONE);
            scrollPositionIV.setVisibility(View.GONE);
            receiveIndicator.setVisibility(View.GONE);
            sendIndicator.setVisibility(View.GONE);
        } else {
            if(yes) {
                AnimUtils.animateView(scrollCountTV);
                AnimUtils.animateView(scrollPositionIV);
            } else {
                scrollCountTV.setVisibility(View.VISIBLE);
                scrollPositionIV.setVisibility(View.VISIBLE);
            }

        }
    }

    private void loadOtherUserChatsToRecycler(){
        // chichi - ucdySn50eeRx1iquBilJtYL86e92
        // bella    -   Qfkxb3jrJEUM6rS8el2VUBQc6WA3
//        boss GT   -   AZkfy6uZunMfMUxR7HIol4rPZBq2
        //  noble - s4QQf6riOiRcwC9HMeA3S8TgL8y1
//        recyclerViewChatVisibility("ucdySn50eeRx1iquBilJtYL86e92");    // make only the active recyclerView to be visible
//        recyclerViewChatVisibility("Qfkxb3jrJEUM6rS8el2VUBQc6WA3");    // make only the active recyclerView to be visible

        if(userRecyclerActiveMap.get("ucdySn50eeRx1iquBilJtYL86e92") == null){    //// chichi

            new Handler().postDelayed(()->{

                recyclerMap.get("ucdySn50eeRx1iquBilJtYL86e92").setAdapter(adapterMap.get("ucdySn50eeRx1iquBilJtYL86e92"));   // set the adapter to the recyclerView
                userRecyclerActiveMap.put("ucdySn50eeRx1iquBilJtYL86e92", true);

                int position = lastPositionPreference.getInt("ucdySn50eeRx1iquBilJtYL86e92", adapterMap.get("ucdySn50eeRx1iquBilJtYL86e92").getItemCount() - 1);
                recyclerMap.get("ucdySn50eeRx1iquBilJtYL86e92").scrollToPosition(position-6 + 1);

            },5000);
        }

        if(userRecyclerActiveMap.get("Qfkxb3jrJEUM6rS8el2VUBQc6WA3") == null){    //// bella

            new Handler().postDelayed(()->{

                recyclerMap.get("Qfkxb3jrJEUM6rS8el2VUBQc6WA3").setAdapter(adapterMap.get("Qfkxb3jrJEUM6rS8el2VUBQc6WA3"));   // set the adapter to the recyclerView
                userRecyclerActiveMap.put("Qfkxb3jrJEUM6rS8el2VUBQc6WA3", true);

                int position = lastPositionPreference.getInt("Qfkxb3jrJEUM6rS8el2VUBQc6WA3", adapterMap.get("Qfkxb3jrJEUM6rS8el2VUBQc6WA3").getItemCount() - 1);
                recyclerMap.get("Qfkxb3jrJEUM6rS8el2VUBQc6WA3").scrollToPosition(position-6 + 1);

            },5000);
        }
    }

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
    public void getMessage(String myUsername, String otherUID, Context mContext_, boolean activateRecycler)
    {
        myUserName = myUsername;

        // only alert me if I am not on another call    -- work on this later
        incomingCallObserver(otherUID);

//        new Handler().postDelayed(()-> {      // delay for 3 secs to allow user chatList to load users first

            try {
                retrieveMessages(myUsername, otherUID, mContext_, activateRecycler);
            } catch (Exception e){
                Toast.makeText(this, "error occur retriever M3610", Toast.LENGTH_SHORT).show();
                System.out.println("Error MainActivity L3490: " + e.getMessage());
            }

            // get pinMessage once
            getPinChats(otherUID);
            getDeletePinId(otherUID);
            getEmojiReact(otherUID);
            getReadChatResponse(otherUID);

//        }, 3000);

    }


    // get last seen and set inbox status to be true
    @Override
    public void getLastSeenAndOnline(String otherUid, Context context)
    {
        AnimUtils.animateView(textViewLastSeen);

        lastSeenValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                try{
                    long onlineValue = snapshot.child("presence").exists() ?
                            (long) snapshot.child("presence").getValue() : -1;
                    if(onlineValue != -1)
                    {
                        if (onlineValue == 1)
                        {
                            String online = getString(R.string.online);
                            textViewLastSeen.setText(online);
                        } else {
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

                    } else{
                        textViewLastSeen.setText(context.getString(R.string.unavailable));
                    }
                } catch (Exception e){
                    String appName = context.getString(R.string.app_name);
                    textViewLastSeen.setText(appName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        refUsers.child(otherUid).child("general").addValueEventListener(lastSeenValueListener);
    }

    // This method will be called by NetworkChangeReceiver whenever network status changes   // reload message loadStatus
    @Override
    public void onNetworkStatusChanged(boolean isConnected)
    {
        setCheckNetworkView();
        if (isConnected) {
            // Network is connected, perform actions accordingly
            if(checkNetworkView != null) checkNetworkView.setVisibility(View.GONE);
            networkListener = "yes";
            chatDeliveryStatus = 700024;
            networkOk = true;
            // remove runnable when network is okay to prevent continuous data usage
            handlerInternet.removeCallbacks(internetCheckRunnable);

            reloadFailedMessagesWhenNetworkIsOk();  // reload message loadStatus

        } else {
            // Network is disconnected, handle this case as well
            if(checkNetworkView != null) checkNetworkView.setVisibility(View.VISIBLE);
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
    public void msgBackgroundActivities(String otherUid) {

        K.executors.execute(() ->
        {
            newChatNumberPosition = -1;
            // set my status to be true in case I receive msg, it will be tick as seen
            Map <String, Object> statusAndMSgCount = new HashMap<>();
            statusAndMSgCount.put("status", true);
            statusAndMSgCount.put("unreadMsg", 0);

            refChecks.child(myId).child(otherUid).updateChildren(statusAndMSgCount);

            // set responds to pend always      ------- will change later to check condition if user is still an active call
            refChecks.child(myId).child(otherUid).child("vCallResp").setValue("pending");

            insideChat = true;     // opening chat

            try{
                if (adapterMap.get(otherUid) != null && recyclerMap.get(otherUid) != null){
                    newChatNumberPosition = UserChatUtils.getNewChatNumberPosition(recyclerMap
                            .get(otherUid), otherUid, adapterMap.get(otherUid).getModelList());
                }
            } catch (Exception e) {
                System.out.println("what is error MainAct 3670: " + e.getMessage());
            }

            // just in case newChatDoesExist, but there's count on outside UI
            if(loopOnceMap.get(otherUid) == null && adapterMap.get(otherUid) != null)
            {
                UserChatUtils.checkIfNewCountExist(adapterMap.get(otherUid).getModelList(), otherUid,
                        false, null);
//                System.out.println("what is calling");
            }


        });

    }


    public void onDeleteMessage() {

        clearEmojiReactSetting();
        hideKeyboard();
        setDeleteForWhoView();
        deleteChatForOnlyOther_TV.setVisibility(View.VISIBLE);

        // user1 should not be unable to delete user2 msg
        for (MessageModel model : chatModelList){
            if(!model.getFromUid().equals(myId)){
                deleteChatForOnlyOther_TV.setVisibility(View.GONE);
                break;
            }
        }

        deleteForWhoView.setVisibility(View.VISIBLE);

        chatOptionView.setVisibility(View.GONE);
        firstTopChatViews.setVisibility(View.VISIBLE);

    }

    private void onEditOrReplyMessage_(MessageModel messageModel, String editOrReply, String status, int icon){
        clearEmojiReactSetting();
        isChatKeyboardON = true;

        // pop up keyboard
        editTextMessage.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(editTextMessage, InputMethodManager.SHOW_IMPLICIT);

        String chat = UserChatUtils.setChatText(messageModel.getType(), messageModel.getMessage(),
                messageModel.getEmojiOnly(), messageModel.getVnDuration(), this);


        // General settings
        textViewReplyOrEdit.setText(chat);
        editOrReplyIV.setImageResource(icon);               //  show edit or reply icon at left view
        cardViewReplyOrEdit.setVisibility(View.VISIBLE);    // make the container of the text visible
        replyVisible.setVisibility(View.VISIBLE);
        replyVisible.setText(status);                       // indicating its on edit or reply mood
        nameReply.setVisibility(View.VISIBLE);
        listener = editOrReply;
        replyFrom =null;

        // this id will enable user to click a reply msg and scroll there
        // Edit -- it will replace the message with the id
        idKey = messageModel.getIdKey();
        editChatModel = messageModel;

        // hide chat option
        if(chatOptionView != null) chatOptionView.setVisibility(View.GONE);
        if(onChatClickView != null) onChatClickView.setVisibility(View.GONE);
        firstTopChatViews.setVisibility(View.VISIBLE);

        // edit settings
        if(editOrReply.equals("edit")){
            nameReply.setText("");
            editTextMessage.setText(chat);  // set the edit message on the text field
            editTextMessage.setSelection(chat.length()); // Set focus to the end of the text
        }

        // reply setting
        if(editOrReply.equals("reply")){

            replyText = chat;
            replyFrom = messageModel.getFromUid() + K.JOIN + messageModel.getSenderName();
            String from = getOtherUserName(null, messageModel);
            nameReply.setText(from);

//            if (fromWho.equals(myUserName)) {   // change fromWho from display name to username later
//                replyFrom = "From You.";
//            }
//            else {
//                // edit later to username and display name
//                replyFrom = fromWho ;
//                nameReply.setText(replyFrom);
//            }
        }
    }

    public void setForwardChat()
    {
        onForward = true;

        // return back to the first position
        if(viewPager2General != null){
            if(viewPager2General.getCurrentItem() != 1) {
                viewPager2General.setCurrentItem(1, true);
            }
        }

        // Disable user from swiping
        viewPager2General.setUserInputEnabled(false);

        clearEmojiReactSetting();
        hideKeyboard();

        setOnForwardTopView();
        setHomeLastViews();

        titleTV_.setText(R.string.forward);
        if(onSelectPlayer && titleTV_ != null) titleTV_.setText(R.string.selectPlayer);
        if(onSelectNewPlayer && titleTV_ != null) titleTV_.setText(R.string.addPlayer);

        if(pinMsgConst != null) pinMsgConst.setVisibility(View.GONE);
        if(firstTopChatViews != null) firstTopChatViews.setVisibility(View.INVISIBLE);
        if(constraintMsgBody != null) constraintMsgBody.setVisibility(View.INVISIBLE);
        topMainContainer.setVisibility(View.INVISIBLE);

        tabLayoutGeneral.setVisibility(View.GONE);
        forwardDownContainer.setVisibility(View.VISIBLE);
        onForwardTopView.setVisibility(View.VISIBLE);
        onForwardTopView.setClickable(true);
        if(onSelectNewPlayer){
            circleForwardSend.setVisibility(View.INVISIBLE);
            totalUser_TV.setText(null);
        } else {
            forwardChatUserId.clear();
            selectedUserNames.clear();
            circleForwardSend.setVisibility(View.VISIBLE);
            totalUser_TV.setText("0 selected");
        }
        forwardDownContainer.setClickable(true);

        if(chatOptionView != null) chatOptionView.setVisibility(View.GONE);
        progressBarForward.setVisibility(View.GONE);

        if(ChatsFragment.adapter != null){
            ChatsFragment.openContactList.setVisibility(View.INVISIBLE);
            ChatsFragment.newInstance().notifyVisibleUser();
        }

        if(sharingPhotoActivated || onSelectPlayer){
            circleForwardSend.setImageResource(R.drawable.baseline_arrow_forward_24);
        } else {
            circleForwardSend.setImageResource(R.drawable.baseline_send_24);
        }

//        selectedPlayerMList.clear();

    }

    private void onPinData(String msgId_, String message_, Object timeStamp_)
    {

        clearEmojiReactSetting();
        hideKeyboard();

        msgId = msgId_;
        message = message_;
        timeStamp = timeStamp_;
        pinByWho = myProfileShareRef.getString(K.PROFILE_DISNAME, myUserName);
        // show pin option
        setPinForWhoViews();
        pinForWhoViews.setVisibility(View.VISIBLE);

        // hide chat option
        chatOptionView.setVisibility(View.GONE);
        firstTopChatViews.setVisibility(View.VISIBLE);

    }

    private void onEmojiReact(String chatID_) {

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


        chatID = chatID_;

//        isKeyboardVisible = false;
        clearHighLight = true;
        typeMsgContainer.setVisibility(View.GONE);
        et_emoji.setText("");

        // hide chat option
        chatOptionView.setVisibility(View.GONE);
        firstTopChatViews.setVisibility(View.VISIBLE);

    }



    //  ------------    methods     ---------------

//    private void addEmptyChatCard(String otherID, MessageAdapter adapter){
//        String chatKey = refMsgFast.child(myId).push().getKey();  // create an id for each message
//
//        // save to local list for fast update
//        MessageModel messageModel = new MessageModel(null, null, myId, null,
//                System.currentTimeMillis(), chatKey, null, null,
//                null, 0, 10, null, null, false, false,
//                null, null, null, null, null, null);
//
//        messageModel.setMyUid(myId);
//
//        // add chat to local list
//        adapter.addMyMessageDB(messageModel);  // add empty card
//
//        // save to local ROOM database
//        chatViewModel.insertChat(otherID, messageModel);
//
//    }

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
    private Map<String, Object> forwardChatsMap(MessageModel chatModel)
    {
        Map<String, Object> forwardMessageMap = new HashMap<>();
        String myDisplayName = myProfileShareRef.getString(K.PROFILE_DISNAME, myUserName);

        forwardMessageMap.put("senderName", myDisplayName);
        forwardMessageMap.put("fromUid", myId);
        forwardMessageMap.put("type", chatModel.getType());
        forwardMessageMap.put("idKey", chatModel.getIdKey());
        forwardMessageMap.put("message", chatModel.getMessage());
        forwardMessageMap.put("emojiOnly", chatModel.getEmojiOnly());
        forwardMessageMap.put("voiceNote", chatModel.getVoiceNote());
        forwardMessageMap.put("msgStatus", 700024);
        forwardMessageMap.put( "timeSent", ServerValue.TIMESTAMP);
        forwardMessageMap.put("newChatNumberID", chatModel.getNewChatNumberID());
        forwardMessageMap.put("chatIsPin", false);
        forwardMessageMap.put("photoUriPath", chatModel.getPhotoUriPath());
        if(chatModel.getFromUid().equals(myId)){
            forwardMessageMap.put("chatIsForward", false);
        } else {
            forwardMessageMap.put("chatIsForward", true);
        }

        if(chatModel.getType() == 2 || chatModel.getType() == 4){
            forwardMessageMap.put("replyFrom", replyFrom);
            forwardMessageMap.put("replyID", idKey);
            forwardMessageMap.put("replyMsg", replyText);
        }

        return forwardMessageMap;
    }

    @Override
    public void sendMessage(final String text, final String emojiOnly, final int type, final String vnPath_,
                            final String durationOrSizeVN, final String otherId, final boolean addReply)
    {
        if(listener.equals("edit")){ // check if it's on edit mode
            sendEditChat(text, emojiOnly, otherId);
        } else {

            String newChatNumberKey = refMsgFast.child(myId).child(otherId).push().getKey();  // create an id for each message
            String myDisplayName = myProfileShareRef.getString(K.PROFILE_DISNAME, "@"+getMyUserName);

            String chatKey = refMsgFast.child(myId).child(otherId).push().getKey();  // create an id for each message

            // check if duration is more than 1mb, then split and send only duration to my model local chat and send the rest to firebase
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
                voiceNoteIdShareRef.edit().putString(chatKey, otherId + K.JOIN + "yes").apply();
                delivery = 700033;
            }

            // save to local list for fast update
            MessageModel messageModel = new MessageModel(text, myDisplayName, myId, addReply ? replyFrom : null,
                    System.currentTimeMillis(), chatKey, null, newChatNumberKey, addReply ? replyText : null,
                    delivery, type, null, idKey, false, false, null,
                    emojiOnly, vnPath_, durationVN, null, null);

            messageModel.setMyUid(myId);
            // add chat to local list
            MessageAdapter adapter = adapterMap.get(otherId);

            if(adapter != null)
            {
                if(getLastTimeChat.get(otherId) != null){   // check if it's the first chat of the day
                    ChatUtils.notifyFirstChatOfANewDay(getLastTimeChat.get(otherId), messageModel.getTimeSent(),  // onSendChat
                            newChatNumberKey, adapter, otherId, this);
                }

                adapter.addMyMessageDB(messageModel);  // add new chat
                if(scrollNumMap != null && scrollNumMap.get(otherId) != null)   // scroll to new position only if scrollCheck int is < 20
                {
                    int scrollCheck = adapter.getItemCount() - (int) scrollNumMap.get(otherId);
                    int lastPosition = adapterMap.get(otherId).getItemCount()-1;

                    if(scrollCheck < 6){    // scroll to last position on new message update.
                        recyclerMap.get(otherId).scrollToPosition(lastPosition);
                    } // else don't scroll...

                    // add one to the down message number
                    int increaseScroll = (int) downMsgCountMap.get(otherId) + 1;
                    scrollCountTV.setText(""+increaseScroll);
                    downMsgCountMap.put(otherId, increaseScroll); // save new position
                }
            }


            // show indicator that msg is sent
            if(scrollPositionIV.getVisibility() == View.VISIBLE){
                sendIndicator.setVisibility(View.VISIBLE);
                receiveIndicator.setVisibility(View.GONE);
            }

            // save the chatKey to network and reload when network is okay
            if(chatDeliveryStatus == 700033 && !isSendingFile && !isSendingVoiceNote){
                offlineChat.edit().putString(chatKey, otherId).apply();
            }

            executorSendDB.execute(() ->     // send only text chat to other user -- not photo nor file yet
            {
            // find position and move it to top as recent chat... also update the outside chat details
                if(ChatsFragment.adapter != null) {
                    UserChatUtils.findUserPositionByUID(ChatsFragment.adapter.userModelList, otherId, messageModel, 0, this);
                } else {    //  proceed with only chats on player tab
                    UserChatUtils.findUserPositionByUID(PlayersFragment.adapter.userModelList, otherId, messageModel, 0, this);
                }

                // remove the typingRunnable for checking network
                handlerTyping.removeCallbacks(runnableTyping);
                networkTypingOk = true;

                // save to local ROOM database
                chatViewModel.insertChat(otherId, messageModel);

                String text_1 = text;
                String emoji_1 = emojiOnly;

                if(messageModel.getType() == 6) {   // sending to other user database
                    emoji_1 = getString(R.string.missed);
                    callChatModel = messageModel;
                } else if (messageModel.getType() == 7) {
                    emoji_1 = getString(R.string.incomingGame);
                    gameModel = messageModel;
                }
                final String emoji = emoji_1;

                if(messageModel.getType() == K.type_pin)     // @maro pin a chat
                {
                    String displayName = myProfileShareRef.getString(K.PROFILE_DISNAME, "@"+myUserName);
                    String pinNoty = emojiOnly.equals(getString(R.string.pin)) ? getString(R.string.pinAChat) : getString(R.string.unPinAChat);
                    text_1 = displayName + " " + pinNoty;
                    messageModel.setReplyID(msgId);
                    idKey = msgId;
                }
                final String text_ = text_1;


                if(!isSendingVoiceNote){    // send only text chat, don't sent voice note here
                    sendToDataBase(messageModel, text_, emoji, chatDeliveryStatus, durationOrSizeVN, otherId, addReply, this);
                }

                clearInputFields(addReply);     // sendMessage
                isSendingVoiceNote = false; // deactivate voice note boolean not to block sending text

            });

        }

    }


    private void sendToDataBase(MessageModel messageModel, String text, String emojiOnly, int chatDeliveryStatus,
                               String durationOrSizeVN, String otherUid, boolean addReply, Context context)
    {

        Map<String, Object> getInsideChatMap = ChatUtils.setMessageMap(messageModel, text, emojiOnly, chatDeliveryStatus, durationOrSizeVN);
        if(addReply) {
            getInsideChatMap.put("replyFrom", replyFrom);
            getInsideChatMap.put("replyID", idKey);
            getInsideChatMap.put("replyMsg", replyText);
        }
        // send the chat to other user
        refMsgFast.child(otherUid).child(myId).child(messageModel.getIdKey()).setValue(getInsideChatMap);


        // save last msg for outside chat display
        Map<String, Object> getOutsideChatMap = ChatUtils.setOutsideChatMap(text, emojiOnly, messageModel, chatDeliveryStatus, durationOrSizeVN, context);

        refLastDetails.child(myId).child(otherUid).updateChildren(getOutsideChatMap);
        refLastDetails.child(otherUid).child(myId).updateChildren(getOutsideChatMap);

        if(messageModel.getType() == K.type_game)
        {
            getOutsideChatMap.put("gameMode", gameMode);
            getOutsideChatMap.put("stakeAmount", stakeAmount);
            getOutsideChatMap.put("hostNote", hostNote);
            getOutsideChatMap.put("numberOfPlayers", numberOfPlayers);
            getOutsideChatMap.put("totalStake", totalStake);
            getOutsideChatMap.put("gameID", gameID);
            getOutsideChatMap.put("players", GameUtils.getEachPlayerMap(selectedPlayerMList));

            IdTokenUtil.generateToken(token -> {

                GameAPI gameAPI = K.retrofit.create(GameAPI.class);

                GameSignalM signalM = new GameSignalM(token, otherUid, getOutsideChatMap);

                gameAPI.signal(signalM).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if(response.isSuccessful()){

                            if(addNewPlayer) alertOtherPlayerOfNewAddedPlayer();

                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable throwable) {
                        Toast.makeText(MainActivity.this, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show();
                    }
                });

            }, context);

        }

        //  send chatKey to other User to read  -- customise later to check user OnRead settings
        refOnReadRequest.child(otherUid).child(myId).push().setValue(messageModel.getIdKey());


        // notify other user
        String fcmToken = otherUserFcmTokenRef.getString(otherUid, null);
        ChatUtils.sentChatNotification( otherUid, getInsideChatMap, fcmToken );

//        refChecks.child(otherUid).child(myId).child("contactName").addListenerForSingleValueEvent(new ValueEventListener()
//        {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if(snapshot.exists()){
//                    String myContactName = snapshot.getValue().toString();
//                    sentChatNotification( otherUid, myContactName,  getInsideChatMap);
//                } else {
//                    sentChatNotification( otherUid, null,  getInsideChatMap);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                sentChatNotification( otherUid, null,  getInsideChatMap);
//            }
//        });

    }

    private void alertOtherPlayerOfNewAddedPlayer()
    {
        Map<String, Object> newPlayerMap = new HashMap<>();
        newPlayerMap.put(newPlayerUid, GameUtils.newPlayerMap(newPlayerMList));

        GameUtils.rejectGameOrAddNewPlayer(this, hostUid, newPlayerUid, newPlayerMap, new GameUtils.RejectGameInterface()
        {
            @Override
            public void onSuccess() {
                newPlayerMList.clear();
                addNewPlayer = false;
                System.out.println("successfully invite new players");
//                                        Toast.makeText(MainActivity.this, getString(R.string.invite_friends), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure() {
                Toast.makeText(MainActivity.this, getString(R.string.errorOccur), Toast.LENGTH_LONG).show();
            }
        });
    }

    // send forward chat or new photo, video or doc files
    public void sendSharedChat(Context context){

        // loop through each user
        int userSize = forwardChatUserId.size();
        for (int i = 0; i < userSize; i++ ) {
            String otherUid = forwardChatUserId.get(i);

            MessageAdapter adapter = adapterMap.get(otherUid);
            assert adapter != null;
            if (adapter.getItemCount() == 0){
                ChatUtils.addEmptyChatCard(otherUid, Objects.requireNonNull(adapterMap.get(otherUid)));     // mine sending onForward
            }

            // loop through each selected chat
            int chatSize = chatModelList.size();
            for (int j = 0; j < chatSize; j++){
                MessageModel chatModel = chatModelList.get(j);

                // get the local list variables
                int forwardType = chatModel.getType();
                String forwardChat = chatModel.getMessage();
                String forwardChatEmojiOnly = chatModel.getEmojiOnly();
                String vnPathFile = chatModel.getVoiceNote();
                String audioOrVN_Duration = chatModel.getVnDuration();
                String forwardPhotoPath = chatModel.getPhotoUriPath();
                String photoOriginal = chatModel.getPhotoUriOriginal(); // also for document
                String imageSize_ = chatModel.getImageSize();
                String replyFrom_ = null;
                String replyId = null;
                String replyText_ = null;
                boolean forwardIcon;
                if(chatModel.getFromUid() != null){
                    forwardIcon = chatModel.getFromUid().equals(myId) ? false : true;
                } else forwardIcon = false;

                if(chatModel.getType() != 3){ // don't apply to document which is 3.
                    replyFrom_ = replyFrom;
                    replyId = idKey;
                    replyText_ = replyText;
                }
                String newChatNumberId = refMsgFast.child(myId).child(otherUid).push().getKey();  // create an id for each message

                String chatId = refMsgFast.child(myId).child(otherUid).push().getKey();  // create an id for each message

                // set image and document => type 0 is for just text-chat, type 1 is voice_note, type 2 is photo, type 3 is document, type 4 is audio (mp3)
                int delivery = chatDeliveryStatus;
                if(forwardPhotoPath != null || forwardType == 3)
                {
                    // save otherId to a photo key, so it doesn't send to another user
                    documentIdShareRef.edit().putString(chatId, otherUid + K.JOIN + "yes").apply();
                    delivery = 700033;
                    if(imageSize_ == null){
                        Uri uriOnPhone = photoOriginal.startsWith("/storage/") ? Uri.fromFile(new File(photoOriginal))
                                : Uri.parse(photoOriginal);
                        imageSize_ = FileUtils.getFileSize(uriOnPhone, this);
                    }

                } else if (forwardType == 4 || forwardType == 1)
                {
                    delivery = 700033;  // so as to enable sending
                    voiceNoteIdShareRef.edit().putString(chatId, otherUid + K.JOIN + "yes").apply();
                }

                String myDisplayName = myProfileShareRef.getString(K.PROFILE_DISNAME, myUserName);

                // save to local list for fast update
                MessageModel messageModel = new MessageModel(forwardChat, myDisplayName, myId, replyFrom_,
                        System.currentTimeMillis(), chatId, null, newChatNumberId, replyText_, delivery,
                        forwardType, imageSize_, replyId, false, forwardIcon, null,
                        forwardChatEmojiOnly, vnPathFile, audioOrVN_Duration, forwardPhotoPath, photoOriginal);

                messageModel.setMyUid(myId);

                adapter.addNewMessageDB(messageModel);  // onForward

                // scroll to new position only if scrollCheck int is < 20
                int scrollCheck;
                try{
                    scrollCheck = adapter.getItemCount() - (int) scrollNumMap.get(otherUid);
                } catch (Exception e) {
                    scrollCheck = adapter.getItemCount();
                }
                int lastPosition = adapterMap.get(otherUid).getItemCount()-1;
                if(scrollCheck < 6){    // scroll to last position on new message update.
                    recyclerMap.get(otherUid).scrollToPosition(lastPosition);
                }   // else don't scroll.

                // save the chatKey to network and reload when network is okay  -- edit later
//                if(chatDeliveryStatus == 700033){
//                    offlineChat.edit().putString(chatId, otherUid).apply();
//                }

                // save to ROOM database
                chatViewModel.insertChat(otherUid, messageModel);

                // send to database only if it's text
                if(forwardType == 0)
                {
                    refMsgFast.child(otherUid).child(myId).child(chatId).setValue( forwardChatsMap(chatModel) );
                    // save last msg for outside chat display
                    refLastDetails.child(myId).child(otherUid).setValue(
                            ChatUtils.setOutsideChatMap(chatModel.getMessage(), chatModel.getEmojiOnly(),
                                    chatModel, 700024, audioOrVN_Duration, context)
                    );
                    refLastDetails.child(otherUid).child(myId).setValue(
                            ChatUtils.setOutsideChatMap(chatModel.getMessage(), chatModel.getEmojiOnly(),
                                    chatModel, 700024, audioOrVN_Duration, context)
                    );
                }

                // add new photo or video to all photo and video views list
                if(photoAndVideoMap.get(otherUid) == null) getEachUserAllPhotoAndVideos(otherUid, adapter.getModelList());
                else MessageAdapter.addNewPhotoOrVideoToViewLists(otherUid, messageModel);

                // find position and move it to top as recent chat... also update the outside chat and ROOM DB
                if(ChatsFragment.adapter != null) {
                    UserChatUtils.findUserPositionByUID(ChatsFragment.adapter.userModelList, otherUid, messageModel, 0, this);
                } else {    //  proceed with only chats on player tab
                    UserChatUtils.findUserPositionByUID(PlayersFragment.adapter.userModelList, otherUid, messageModel, 0, this);
                }

                //  send chatKey to other User to read  -- customise later to check user OnRead settings
                refOnReadRequest.child(otherUid).child(myId).push().setValue(chatId);

                // cancel when all is done
                if(i == userSize-1 && j == chatSize-1 ){
                    cancelForwardSettings(context);
                    clearInputFields(false);
                    Toast.makeText(context, context.getString(R.string.sending), Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    public static void updateCallOrGameChat(int type, String response)
    {
        if(callChatModel != null)
        {
            callChatModel.setEmojiOnly(response);

            //  update inside chat adapter
            adapterMap.get(currentUserUidOnCall).updateCallOrGameChat(callChatModel);

            // save to ROOM inside chat
            chatViewModel.updateChat(callChatModel);

            //  update outside chat adapter      //  update ROOM outside database too
            if(ChatsFragment.adapter != null) ChatsFragment.adapter.updateCallOrGameUI(type, currentUserUidOnCall, myId, response, true, false);
            if(PlayersFragment.adapter != null) PlayersFragment.adapter.updateCallOrGameUI(type, currentUserUidOnCall, myId, response, false, true);

        }

    }

    public static void missCallOrGameMethod(int type, String response, String otherId)
    {
        if(missCallModel != null)
        {
            missCallModel.setEmojiOnly(response);

            //  update inside chat adapter
            adapterMap.get(otherId).updateCallOrGameChat(missCallModel);

            // save to ROOM inside chat
            chatViewModel.updateChat(missCallModel);

            //  update outside chat adapter      //  update ROOM outside database too
            if(ChatsFragment.adapter != null) ChatsFragment.adapter.updateCallOrGameUI(type, otherId, myId, response, true, false);
            if(PlayersFragment.adapter != null) PlayersFragment.adapter.updateCallOrGameUI(type, otherId, myId, response, false, true);

        }

    }
    private void sendEditChat(String text, String emojiOnly, String otherId){
        editMessageMap.put("from", myUserName);
        editMessageMap.put("fromUid", myId);
        editMessageMap.put("message", text);
        editMessageMap.put("emojiOnly", emojiOnly);
        editMessageMap.put("edit", "edited");
        editMessageMap.put( "timeSent", ServerValue.TIMESTAMP);

        // save to edit message to update other user chat
        refEditMsg.child(otherId).child(myId).child(idKey).setValue(editMessageMap);

        editChatModel.setMessage(text);
        editChatModel.setEmojiOnly(emojiOnly);
        editChatModel.setTimeSent(System.currentTimeMillis());
        editChatModel.setEdit("edited");

        //  send the messageModel to the adapter to update it
        adapterMap.get(otherId).updateMessage(editChatModel);

        //  save to ROOM database
        chatViewModel.updateChat(editChatModel);

        // update user chatList model if it same last chat
        UserChatUtils.findUserAndEditChat(PlayersFragment.adapter, otherId, idKey, text, emojiOnly);
        if(ChatsFragment.adapter != null) UserChatUtils.findUserAndEditChat(ChatsFragment.adapter,
                otherId, idKey, text, emojiOnly);

        // update the ROOM outside UI
        chatViewModel.editOutsideChat(otherId, K.EDIT_ICON + text,
                emojiOnly, idKey);

        // remove the typingRunnable for checking network
        handlerTyping.removeCallbacks(runnableTyping);
        networkTypingOk = true;

        // hide chat Option menu if visible
        chatOptionView.setVisibility(View.GONE);

        clearInputFields(true);

    }

    // record voiceNote
    private void voiceNote (Context context, Activity activity){

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

                if(appPermissionCheck.isRecordingOk(context)){
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
                    appPermissionCheck.requestRecording(activity);
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

                    // Calculate the estimated file size (kb or mb)
                    long durationMillis = SystemClock.elapsedRealtime() - startTimeMillis;
                    long fileSizeBytes = ( (audioBitrate / 8) * durationMillis / 1000 ) / 10; // Convert duration to seconds

                    // Convert file size to kilobytes (KB) and megabytes (MB)
                    int fileSizeKB = (int) fileSizeBytes / 1024; // Size in kilobytes
                    int fileSizeMB = fileSizeKB / 1024; // Size in megabytes

                    String formattedDuration = formatDuration(mediaPlayer.getDuration());   // don't auto download for other user if size is greater than 500kb
                    String sizeOrDuration = fileSizeKB < 500.0 ? formattedDuration : Math.round(fileSizeMB) + " MB ~ " + formattedDuration;

                    mediaPlayer.release();  // Release the MediaPlayer resources after getting the duration

                    // send voice note to local list and room only -- adapter will do the sending to other user
                    sendMessage(null, null, 1, fileNamePath, sizeOrDuration, otherUserUid, true);     // voice note

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
        File file = new File(getVoiceNoteFolder(mContext), "voice_note_" + System.currentTimeMillis() + ".opus");
        return file.getPath();
    }


    public void retrieveMessages(String myUsername, String otherUID, Context mContext, boolean activateRecycler)
    {

        List<MessageModel> modelListAllMsg = new ArrayList<>();     // send empty List to the adapter

        // initialise adapter
        MessageAdapter adapter = new MessageAdapter(modelListAllMsg, myUsername, otherUID, mContext,
                recyclerMap.get(otherUID));

//        getLastScrollPosition(otherUID);    // retrieve the last previous scroll position

        K.executors.execute(() -> {

            if(chatViewModel != null){
                // fetch chats from ROOM
                if(chatViewModel.getEachUserChat_(otherUID, myId) != null){
                    List<MessageModel> getEachUserChats = chatViewModel.getEachUserChat_(otherUID, myId);
                    adapter.setModelList(getEachUserChats);

                    adapter.setFragmentListener((FragmentListener) mContext);
                    adapterMap.put(otherUID, adapter); // save each user adapter

                    getEachUserAllPhotoAndVideos(otherUID, getEachUserChats);
                }

                runOnUiThread(() -> {

                    if(activateRecycler) {  // for new user not the list previously
                        recyclerMap.get(otherUID).setAdapter(adapter);
                        progressBarLoadChats.setVisibility(View.GONE);
                    }

                    //  delay for like 2 sec to fetch all old data first
                    new Handler().postDelayed( () ->
                    {
                        // add new message directly to local List and interact with few msg in refMsgFast database
                        newMessageInteraction(adapter, otherUID, mContext);

                        // edit message
                        getEditMessage(adapter, otherUID);

                        // delete local list with idKey
                        getDeleteMsgId(adapter, otherUID);

                        checkClearChatsDB(otherUID);

                    }, 3000);

                });
            }

        });


//        getAllMessages(userName, otherName, modelListAllMsg, msgListNotRead, adapter, otherUID);

        adapter.setFragmentListener((FragmentListener) mContext);
        adapterMap.put(otherUID, adapter); // save each user adapter


//        recyclerMap.get("ucdySn50eeRx1iquBilJtYL86e92").setAdapter(adapterMap.get("ucdySn50eeRx1iquBilJtYL86e92"));   // set chichi the adapter to the recyclerView
//        Toast.makeText(mContext, "adapter " + adapterMap.get("ucdySn50eeRx1iquBilJtYL86e92").getItemCount(), Toast.LENGTH_SHORT).show();
    }

    public static void getEachUserAllPhotoAndVideos(String otherUID, List<MessageModel> modelList){

        int count = 0;
        // Create a list to add the photo
        List<MessageModel> photoAndVideoModelList = new ArrayList<>();

        // loop through the list to select which one contain the download image
        for (int i = 0; i < modelList.size(); i++) {

            MessageModel model = modelList.get(i);

            // check if the model is photo or video file
            if(model.getType() == 2 || model.getType() == 5)
            {
                // check if I have downloaded other user photo
                if(!model.getFromUid().equals(myId) && !model.getPhotoUriOriginal().startsWith("media/video")
                        && !model.getPhotoUriOriginal().startsWith("media/photos"))
                {
                    photoAndVideoModelList.add(model);
                    filePositionMap.put(model.getIdKey(), count++);

                } else if (model.getFromUid().equals(myId)) {
                    // add my image -- from my from storage
                    photoAndVideoModelList.add(model);
                    filePositionMap.put(model.getIdKey(), count++);

                }
            }

            if (i == (modelList.size() - 1) && !photoAndVideoModelList.isEmpty())
            {
                photoAndVideoMap.put(otherUID, photoAndVideoModelList);
            }

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

        firstTopChatViews.setVisibility(View.VISIBLE);

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
                        ChatUtils.scrollToPreviousPosition(otherUID, (int) scrollNumMap.get(otherUID));   // get All message
                    } catch (Exception e){
                        ChatUtils.scrollToPreviousPosition(otherUID, adapter.getItemCount() - 1); // get All message...
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    
    // add new message to local List and interact with few msg in refMsgFast database for delivery and read status
    private void newMessageInteraction(MessageAdapter adapter, String otherId, Context context)
    {
        if(adapter == null || otherId == null || context == null) return;

        refMsgFast.child(myId).child(otherId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                K.executors.execute(() ->
                {
                    int itemCount = (int) snapshot.getChildrenCount();
                    int processedItems = 0;

                    for (DataSnapshot snapshot1 : snapshot.getChildren())
                    {
                        MessageModel messageModel = snapshot1.getValue(MessageModel.class);

                        // If there's new message from otherUser, add here
                        assert messageModel != null;
                        if(messageModel.getFromUid() != null && messageModel.getIdKey() != null)
                        {
                            ChatUtils.getChatFromOtherUser(messageModel, otherId, adapter, context, "retrieve");
                        }

                        // delete after delivery the chat
                        processedItems++;
                        if (processedItems == itemCount) refMsgFast.child(myId).child(otherId).removeValue();

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
                                    UserChatUtils.findUserAndEditChat(PlayersFragment.adapter, otherUid,
                                            editID, editMessageModel.getMessage(), editMessageModel.getEmojiOnly());
                                    if(ChatsFragment.adapter != null) {
                                        UserChatUtils.findUserAndEditChat(PlayersFragment.adapter,otherUid, editID,
                                                editMessageModel.getMessage(), editMessageModel.getEmojiOnly());
                                    }

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
                    String deleteChatID = snapshotDelete.child("idKey").exists() ?
                            snapshotDelete.child("idKey").getValue().toString() : null;
                    if(deleteChatID != null)
                    {
                        String deleteIdKey = snapshotDelete.getKey();
                        // loop through the local list and search for same idKey
                        for (int i = adapter.getModelList().size() - 1; i >= 0; i--) {
                            String listMessageID = adapter.getModelList().get(i).getIdKey();

                            if(deleteIdKey.equals(listMessageID)){
                                MessageModel chatModel = adapter.getModelList().get(i);

                                // delete from ROOM database
                                chatViewModel.deleteChat(chatModel);
                                // delete photo from my phone storage if photo exist
                                if(!chatModel.getFromUid().equals(myId)) {  // other user is not permitted to delete my media except the one they sent me.
                                    deleteFileFromPhoneStorage(chatModel); // get id delete other user
                                }
                                // delete from local list if id key matches
                                adapter.getModelList().remove(i);
                                adapter.notifyItemRangeChanged(i, adapter.getModelList().size(), new Object());
                                // delete idKey from database if id key matches
                                refDeleteMsg.child(myId).child(otherUid).child(deleteIdKey).removeValue();
                                break;
                            }
                        }
                    }

                    // check outside message if it's same message that was deleted and delete for both user
                    refLastDetails.child(myId).child(otherUid).addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshotLast)
                        {
                            try {
                                //  check if the random id is same
                                String outSideChatID = snapshotLast.child("idKey").exists() ?
                                        snapshotLast.child("idKey").getValue().toString() : null;
                                if(outSideChatID != null)
                                {
                                    if(deleteChatID.equals(outSideChatID)){
                                        //   the message
                                        refLastDetails.child(myId).child(otherUid)
                                                .child("message").setValue("...");
                                        refLastDetails.child(otherUid).child(myId)
                                                .child("message").setValue("...");

                                        // delete the ROOM outside UI
                                        chatViewModel.editOutsideChat(otherUid,
                                                K.DELETE_ICON + " ...",
                                                null, outSideChatID);

                                        // update user chatList model
                                        if(ChatsFragment.adapter != null) UserChatUtils.findUserAndDeleteChat(ChatsFragment.adapter, otherUid, deleteChatID);
                                        UserChatUtils.findUserAndDeleteChat(PlayersFragment.adapter, otherUid, deleteChatID);

                                    }
                                }

                            }catch (Exception e){
                                System.out.println("what is error MainA L5150: " + e.getMessage());
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

    // check if other user has chats or deleted me from his chat list
    public void checkClearChatsDB(String otherUid)
    {
        refClearSign.child(otherUid).child(myId).addValueEventListener(new ValueEventListener() // check if user delete all chats
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                K.executors.execute(() -> {
                    if (snapshot.getValue() != null && snapshot.getValue().toString().equals("clear")) {
                        if (adapterMap.get(otherUid) != null) {
                            // clear local list -- adapter
                            adapterMap.get(otherUid).clearChats();
                            runOnUiThread(() -> adapterMap.get(otherUid).notifyDataSetChanged());
                        }
                        //delete the sign from firebase DB
                        refClearSign.child(otherUid).child(myId).removeValue();
                        // delete all chat from ROOM
                        chatViewModel.deleteChatByUserId(otherUid, myId);

                        if(ChatsFragment.adapter != null)   // reset num of new chat to 0
                            ChatsFragment.adapter.findUserModelByUidAndResetNewChatNum(otherUid, K.fromChatFragment, false);
                        if(PlayersFragment.adapter != null)
                            PlayersFragment.adapter.findUserModelByUidAndResetNewChatNum(otherUid, K.fromPlayerFragment, false);

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        // check if other user has deleted me from his chat list
        refDeleteUser.child(otherUid).child(myId).addValueEventListener(new ValueEventListener()    // check if user delete me from user list
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                K.executors.execute(()-> {
                    if(snapshot.getValue() != null && snapshot.getValue().toString().equals("clear"))
                    {
                        if(adapterMap.get(otherUid) != null )
                        {
                            // clear chats from local list -- adapter
                            adapterMap.get(otherUid).clearChats();
                            // delete user from adapter list
                            UserChatUtils.findUserAndDelete(PlayersFragment.adapter, otherUid);
                            if(ChatsFragment.adapter != null) UserChatUtils.findUserAndDelete(ChatsFragment.adapter, otherUid);
                        }
                        //delete the sign from firebase DB
                        refDeleteUser.child(otherUid).child(myId).removeValue();
                        // delete user from ROOM
                        chatViewModel.deleteUserById(otherUid);

//                            if(!clearOnlyChatHistory) { // reactivate valueListener to get accurate result
//                                PlayersFragment.newInstance().removeValueListener();
//                                if (ChatsFragment.adapter != null) PlayersFragment.newInstance().removeValueListener();
//
//                                PlayersFragment.newInstance().activateValueListener();
//                                if (ChatsFragment.adapter != null) PlayersFragment.newInstance().activateValueListener();
//                            }
                    }
                });

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
            File searchPhotoOriginal = new File(chatModel.getPhotoUriOriginal());
            if(chatModel.getPhotoUriOriginal().startsWith("file:/")){
                try {
                    searchPhotoOriginal = new File(new URI(chatModel.getPhotoUriOriginal()));
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
            if(searchPhotoOriginal.exists()) {
                searchPhotoOriginal.delete();
            }

            //  delete from firebase storage
            if(chatModel.getPhotoUriOriginal().startsWith("media/voice_note")){
                FirebaseStorage.getInstance().getReference(chatModel.getPhotoUriOriginal()).delete();
            }
            
        }
        // delete voice note from phone storage
        if(chatModel.getVoiceNote() != null){
            File searchVoiceNotePath = new File(chatModel.getVoiceNote());
            if(chatModel.getVoiceNote().startsWith("file:/")){
                try {
                    searchVoiceNotePath = new File(new URI(chatModel.getVoiceNote()));
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
            if(searchVoiceNotePath.exists()) {
                searchVoiceNotePath.delete();
            }
            //  delete from firebase storage if not yet deleted
            if(chatModel.getVoiceNote().startsWith("media/voice_note")){
                FirebaseStorage.getInstance().getReference(chatModel.getVoiceNote()).delete();
            }
        }

    }

    // check if other user send me chat id to read
    private void getChatReadRequest(String otherId) {
        chatReadListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                K.executors.execute(() ->
                {
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
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors
            }
        };

        // Add the ValueEventListener to the database reference
        refOnReadRequest.child(myId).child(otherId).addValueEventListener(chatReadListener);
    }

    // check if other user has read my chat
    private void getReadChatResponse(String otherId){
        refChatIsRead.child(myId).child(otherId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                K.executors.execute(() ->
                {
                    int totalItem = (int) snapshot.getChildrenCount();
                    int processedItems = 0;

                    for (DataSnapshot snapshot1 : snapshot.getChildren())
                    {
                        if(snapshot1.exists()){
                            String getChatId = snapshot1.getValue().toString();

                            if(adapterMap.get(otherId) != null){

                                MessageAdapter adapter = adapterMap.get(otherId);
                                // find the position of the chat
                                int chatPosition = adapter.findMessagePositionById(getChatId);

                                if(chatPosition != -1)  // check if chat exist
                                {
                                    // get the current delivery status of the chat
                                    int currentStatus = adapter.getModelList().get(chatPosition).getMsgStatus();
                                    if(currentStatus != 700016){ // 700016 means chat is read

                                        adapter.getModelList().get(chatPosition).setMsgStatus(700016);

                                        // update the inside chat
                                        runOnUiThread(()-> adapter.notifyItemChanged(chatPosition, new Object()));

                                        // update delivery status for outSide chat
                                        PlayersFragment.adapter.updateDeliveryToRead(otherId, K.fromPlayerFragment);

                                        if(ChatsFragment.adapter != null)
                                            ChatsFragment.adapter.updateDeliveryToRead(otherId, K.fromChatFragment);

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

                        // delete when done
                        processedItems++;
                        if(processedItems == totalItem) {
                            refChatIsRead.child(myId).child(otherId).removeValue();
                        }
                    }

                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addClickableEmoji() {

        if(otherUserUid == null) return;;

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
                    if (containsEmoji)
                    {
                        // add emoji to local list and ROOM
                        String chat = messageAdapter.addEmojiReact(getEmoji, chatID, otherUserUid);

                        String myDisplayName = myProfileShareRef.getString(K.PROFILE_DISNAME, "@"+myUserName);
                        if(myDisplayName.length() > 15) myDisplayName = myDisplayName.substring(0, 14);

                        // send a signal to add emoji for other user also
                        Map<String, Object> emojiMap = new HashMap<>();
                        emojiMap.put("emojiReact", getEmoji);
                        emojiMap.put("chatID", chatID);
                        emojiMap.put("reactByWho", myDisplayName);
                        refEmojiReact.child(otherUserUid).child(myId).push().setValue(emojiMap);

                        if(ChatsFragment.adapter != null)   // update outside user chat list
                        {
                            UserOnChatUI_Model outsideUserModel = ChatsFragment.adapter.findUserModelByUid(otherUserUid);
                            List<UserOnChatUI_Model> userList = ChatsFragment.adapter.userModelList;

                            updateOutsideChatWithReact(getString(R.string.you), userList, outsideUserModel, chat, getEmoji, false, true, otherUserUid);
                        }

                        if(PlayersFragment.adapter != null)     // update outside chat on playerFragment
                        {
                            UserOnChatUI_Model outsideUserModel = PlayersFragment.adapter.findUserModelByUid(otherUserUid);
                            List<UserOnChatUI_Model> userList = PlayersFragment.adapter.userModelList;

                            updateOutsideChatWithReact(getString(R.string.you), userList, outsideUserModel, chat, getEmoji, true, false, otherUserUid);
                        }
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
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                K.executors.execute(() ->
                {
                    int itemCount = (int) snapshot.getChildrenCount();
                    int processedItems = 0;

                    for (DataSnapshot snapshot1 : snapshot.getChildren())
                    {
                        if(snapshot1.child("emojiReact").getValue() != null && snapshot1.child("chatID").getValue() != null &&
                                snapshot1.child("reactByWho").getValue() != null )
                        {
                            String getEmoji = snapshot1.child("emojiReact").getValue().toString();
                            String getChatId = snapshot1.child("chatID").getValue().toString();
                            String reactByWho = snapshot1.child("reactByWho").getValue().toString();

                            String getOtherContactname = contactNameShareRef.getString(otherId, reactByWho);

                            String chat = adapterMap.get(otherId).addEmojiReact(getEmoji, getChatId, otherId);   // update inside chat

                            if(ChatsFragment.adapter != null)   // update outside user chat list
                            {
                                UserOnChatUI_Model outsideUserModel = ChatsFragment.adapter.findUserModelByUid(otherId);
                                List<UserOnChatUI_Model> userList = ChatsFragment.adapter.userModelList;

                                updateOutsideChatWithReact(getOtherContactname, userList, outsideUserModel, chat, getEmoji, false, true, otherId);
                            }

                            if(PlayersFragment.adapter != null)     // update outside chat on playerFragment
                            {
                                UserOnChatUI_Model outsideUserModel = PlayersFragment.adapter.findUserModelByUid(otherId);
                                List<UserOnChatUI_Model> userList = PlayersFragment.adapter.userModelList;

                                updateOutsideChatWithReact(getOtherContactname, userList, outsideUserModel, chat, getEmoji, true, false, otherId);
                            }

                        }

                        // delete from database once added
                        processedItems++;
                        if(processedItems == itemCount) refEmojiReact.child(myId).child(otherId).removeValue();

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }

    private void updateOutsideChatWithReact(String from, List<UserOnChatUI_Model> userList, UserOnChatUI_Model outsideUserModel,
                                            String chat, String getEmoji, boolean onPlayer, boolean onChat, String otherId)
    {
        int userPosition = userList.indexOf(outsideUserModel);

        if(chat != null && userPosition >= 0)    // update outside chat with the new emoji react
        {
            String emojiAndChat = from + ": " + getEmoji + " " + getString(R.string.to_) + "  '" + chat + "'";
            userList.get(userPosition).setMessage(emojiAndChat);    // set the emoji chat

            runOnUiThread(()-> {
                if(onChat){
                    ChatsFragment.newInstance().notifyItemChanged(userPosition);     // notify the adapter of the item changes
                }
            });

            runOnUiThread(()-> {
                if(onPlayer){
                    PlayersFragment.newInstance().notifyItemChanged(userPosition);     // notify the adapter of the item changes
                }
            });

            MainActivity.chatViewModel.updateUser(userList.get(userPosition));     // update room db

            refLastDetails.child(user.getUid()).child(otherId).child("message").setValue(emojiAndChat);

        }
    }

    //      ================      pin methods           ========================

    private void pinIconsVisibility(String otherId){
        int totalPinsPrivate = 0;
        int totalPinsPublic = 0;
        // make private pin details invisible if no pin chat yet
        if(pinPrivateChatMap.get(otherId) != null){
            totalPinsPrivate = pinPrivateChatMap.get(otherId).size();
            if(totalPinsPrivate > 0){  // make pin icon visible
                pinPrivateIcon_IV.setVisibility(View.VISIBLE);
                pinLockPrivate_IV.setVisibility(View.VISIBLE);
                totalPinPrivate_TV.setVisibility(View.VISIBLE);
                totalPinPrivate_TV.setText(""+ totalPinsPrivate);
            }else {    // make pin icon invisible
                pinPrivateIcon_IV.setVisibility(View.GONE);
                pinLockPrivate_IV.setVisibility(View.GONE);
                totalPinPrivate_TV.setVisibility(View.GONE);
            }
        } else {    // make invisible if null
//            pinChatViews.setVisibility(View.INVISIBLE);
            pinMsgConst.setVisibility(View.INVISIBLE);
        }

        // make public pin details invisible if no pin chat yet
        if(pinPublicChatMap.get(otherId) != null){
            totalPinsPublic = pinPublicChatMap.get(otherId).size();
            if(totalPinsPublic > 0){  // make pin icon visible
                pinPublicIcon_IV.setVisibility(View.VISIBLE);
                pinLockPublic_IV.setVisibility(View.VISIBLE);
                totalPinPublic_TV.setVisibility(View.VISIBLE);
                totalPinPublic_TV.setText(""+ totalPinsPublic);

            } else {    // make pin icon invisible
                pinPublicIcon_IV.setVisibility(View.GONE);
                pinLockPublic_IV.setVisibility(View.GONE);
                totalPinPublic_TV.setVisibility(View.GONE);
            }
        } else {
//            pinChatViews.setVisibility(View.INVISIBLE);
            pinMsgConst.setVisibility(View.INVISIBLE);
        }

        if(totalPinsPublic > 0 || totalPinsPrivate > 0) {
            pinIconsContainer.setVisibility(View.VISIBLE);
            AnimUtils.fadeInVisible(pinMsgConst, 300);
            pinMsgConst.setClickable(false);    //  allow item on the background clickable
            if(totalPinsPublic > 0 && totalPinsPrivate > 0) line.setVisibility(View.VISIBLE);
        }

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

                K.executors.execute(()->
                {
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
                                    messageAdapter.getModelList().get(pinPosition).setChatIsPin(true);
                                    if (fiveSecondsWait) {
                                        // delay 5sec to allow pin chat to the list first
                                        runOnUiThread(()-> messageAdapter.notifyItemChanged(pinPosition, new Object()));
                                    }
                                }
                            }
                        }
                    }
                });

                if(firstTopChatViews != null){
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
                        if(pinPosition != -1){
                            messageAdapter.getModelList().get(pinPosition).setChatIsPin(false);
                            messageAdapter.notifyItemChanged(pinPosition, new Object());
                        }
                        // update the icon on the model chat list UI

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
        pinDetails.put("pinByUID", myId);

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
        // check if message has already been pin or not in private pin
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
                adapterMap.get(otherUserUid).pinIconDisplay(msgId,false);
            }

            Toast.makeText(this, "Chat unpin!", Toast.LENGTH_SHORT).show();

            // send to database
            sendMessage(getString(R.string.youUnpinPublicly), getString(R.string.unpin), K.type_pin,    // unpin publicly
                    null, null, otherUserUid, false);

        } else {
            // Add the new pin message to the local map
            PinMessageModel newPin = new PinMessageModel(msgId, message, timeStamp, pinByWho, myId);
            pinPublicChatMap.get(otherUserUid).add(newPin);

            // Add the new pin message to firebase database
            refPublicPinChat.child(myId).child(otherUserUid).child(msgId)
                    .setValue(pinDetails);
            refPublicPinChat.child(otherUserUid).child(myId).child(msgId)
                    .setValue(pinDetails);

            //  show pin icon 📌 on the chat UI, and update ROOM and adapter list
            adapterMap.get(otherUserUid).pinIconDisplay(msgId,true);

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

            // send to database
            sendMessage(getString(R.string.youPinPublicly), getString(R.string.pin), K.type_pin,    // pin publicly
                    null, null, otherUserUid, false );
        }

        // close the pin box option
        pinForWhoViews.setVisibility(View.GONE);
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
        pinDetails.put("pinByUID", myId);

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
                adapterMap.get(otherUserUid).pinIconDisplay(msgId, false);
            }

            Toast.makeText(this, "Chat unpin!", Toast.LENGTH_SHORT).show();

        } else {
            // Add the new pin message to the local map
            PinMessageModel newPin = new PinMessageModel(msgId, message, timeStamp, pinByWho, myId);
            pinPrivateChatMap.get(otherUserUid).add(newPin);

            // Add the new pin message to firebase database
            refPrivatePinChat.child(myId).child(otherUserUid).child(msgId)
                    .setValue(pinDetails);

            //  show and update pin icon on the chat UI, also update ROOM
            adapterMap.get(otherUserUid).pinIconDisplay(msgId, true);

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
        pinForWhoViews.setVisibility(View.GONE);
        pinStatus = "null";      // indicate to show public pins

    }
    private void scrollToPinMessage(int i){
        pinMsg_TV.setTypeface(null);    // remove italic style if any

        // get the msg id you want to scroll to
        String findMsgId = pinStatus.equals(PRIVATE) ? pinPrivateChatMap.get(otherUserUid).get(i).
                getMsgId() : pinPublicChatMap.get(otherUserUid).get(i).getMsgId();

        // get the position of the message
        int position = adapterMap.get(otherUserUid).findMessagePositionById(findMsgId);

        if(position != RecyclerView.NO_POSITION){
            if(scrollNum > position) recyclerMap.get(otherUserUid).scrollToPosition(position - 3);   // under correct
            else recyclerMap.get(otherUserUid).scrollToPosition(position + 3);

            // highlight the message found
            adapterMap.get(otherUserUid).highlightItem(position);

        } else {
            Toast.makeText(this, getString(R.string.chatNotFound), Toast.LENGTH_SHORT).show();
            chatNotFoundID = findMsgId;
        }
    }

    public static void openInChat(String chatIdKey, Context context) {
        // get the position of the message
        int position = Objects.requireNonNull(adapterMap.get(otherUserUid)).findMessagePositionById(chatIdKey);

        if(position != RecyclerView.NO_POSITION){
            if(scrollNum > position) Objects.requireNonNull(recyclerMap.get(otherUserUid)).scrollToPosition(position - 3);   // under correct
            else if (Objects.requireNonNull(adapterMap.get(otherUserUid)).getItemCount() - position < 4) {
                Objects.requireNonNull(recyclerMap.get(otherUserUid)).scrollToPosition(Objects.requireNonNull(adapterMap.get(otherUserUid)).getItemCount() - 1);
            } else Objects.requireNonNull(recyclerMap.get(otherUserUid)).scrollToPosition(position + 3);

            // highlight the message found
            Objects.requireNonNull(adapterMap.get(otherUserUid)).highlightItem(position);

            new Handler().postDelayed(MainActivity::clearAllHighlights, 5_000);

        } else {
            Toast.makeText(context, context.getString(R.string.chatNotFound), Toast.LENGTH_SHORT).show();
//            chatNotFoundID = findMsgId;
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

                if(modelChats.getType() != 8 && modelChats.getType() != 10)
                {
                    onEditOrReplyMessage_(modelChats, "reply", "replying...", R.drawable.reply);

                    // notify it to reset the adapter in case onSwipe was called
                    adapterMap.get(otherUserUid).notifyDataSetChanged();
                    // close chatOption is visible
                    cancelChatOption();
                    if(topEmojiView != null) AnimUtils.fadeOutGone(topEmojiView, 500);

                }
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

                MessageModel modelChats = adapterMap.get(otherUserUid).getModelList()
                        .get(viewHolder.getAdapterPosition());

                if(modelChats.getType() != 8 && modelChats.getType() != 10){
                    // Check if the item is beyond the quarter of the screen and Return 0 to disable both dragging and swiping
                    if (viewHolder.itemView.getX() >= quarterScreen || viewHolder.itemView.getX() <= -quarterScreen) {

                        onEditOrReplyMessage_(modelChats, "reply", "replying...", R.drawable.reply);

                        // close chatOption if visible
                        cancelChatOption();
                        if(topEmojiView != null) AnimUtils.fadeOutGone(topEmojiView, 500);

                        return 0;
                    } else {
                        // Allow normal movement
                        return super.getMovementFlags(recyclerView, viewHolder);
                    }

                } else {
                    return 0;
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull
            RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                MessageModel modelChats = adapterMap.get(otherUserUid).getModelList()
                        .get(viewHolder.getAdapterPosition());

                if(modelChats.getType() != 8 && modelChats.getType() != 10) {

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
            }

        });

    }


    // call - alert me when other user is calling me
    public void observeIncomingLatestEvent(String otherUid, NewEventCallBack callBack) {
        refCalls.child(myId).child(otherUid).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            String data = Objects.requireNonNull(snapshot.getValue()).toString();
                            CallModel callModel = gson.fromJson(data, CallModel.class);
                            callBack.onNewEventReceived(callModel);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }


    //      ================      wallet methods           ========================
    private void openWalletMethod(){

        PhoneUtils.hideKeyboard(this, enterAckPin_ET);

        Intent intent = new Intent(this, WalletActivity.class);
        startActivity(intent);

        walletVerifyView.setVisibility(View.GONE);

        if(sideBarView != null) sideBarMenuContainer.setVisibility(View.GONE);

//        v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(10).withEndAction(() ->
//        {

        // Reset the scale
//            new Handler().postDelayed( ()-> {
//                v.setScaleX(1.0f);
//                v.setScaleY(1.0f);
//            }, 50);
//        }).start();
    }

    private void showFingerPrint(){
        if(appPermissionCheck.isBiometricOk(this)){

            enterAckPin_ET.setText(null);
            // Create a BiometricPrompt instance
            biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback()
            {
                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    if(errorCode == 13){

                        verifyWithPin();

                    } else {
                        pinOptionContainer.setVisibility(View.VISIBLE);
                        pinContainerHome.setVisibility(View.GONE);
                        openWalletButton.setVisibility(View.GONE);
                        forgetPin_Button.setVisibility(View.GONE);

                        fingerprintIcon.setVisibility(View.VISIBLE);
                        verifyViaTV.setVisibility(View.VISIBLE);
                        or_TV.setVisibility(View.VISIBLE);
                        openPinBox_TV.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);

                    loadVerify();

                    openWalletMethod(); // fingerprint

                    Toast.makeText(MainActivity.this, getString(R.string.verifying), Toast.LENGTH_SHORT).show();
                    // Handle authentication success
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    Toast.makeText(MainActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    // verify with PIN directly
                    verifyWithPin();
                }
            });

            // Create a BiometricPrompt.PromptInfo instance
            promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Fingerprint Authentication")
                    .setSubtitle("Click 'Verify by PIN' if fingerprint is dirty or wet")
                    .setNegativeButtonText("Or\n Verify by PIN")
                    .build();

            // Show the fingerprint authentication dialog
            biometricPrompt.authenticate(promptInfo);

        } else {
            appPermissionCheck.requestBiometric(this);
        }

    }

    private void verifyWithPin(){
        pinOptionContainer.setVisibility(View.VISIBLE);
        pinContainerHome.setVisibility(View.VISIBLE);
        openWalletButton.setVisibility(View.VISIBLE);
        forgetPin_Button.setVisibility(View.VISIBLE);

        fingerprintIcon.setVisibility(View.GONE);
        verifyViaTV.setVisibility(View.GONE);
        or_TV.setVisibility(View.GONE);
        openPinBox_TV.setVisibility(View.GONE);
    }

    private void loadVerify(){
        generalBackground.setVisibility(View.VISIBLE);
        verifyLoadTV.setVisibility(View.VISIBLE);
        verifyProgressBar.setVisibility(View.VISIBLE);

        new Handler().postDelayed(()-> {
            generalBackground.setVisibility(View.GONE);
            verifyLoadTV.setVisibility(View.GONE);
            verifyProgressBar.setVisibility(View.GONE);
        }, 2000);
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
        editTextMessage.addTextChangedListener(new TextWatcher() {
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

                    onTyping();

                } else {
                    if(listener.equals("edit")){
                        onTyping();
                    } else {
                        endTyping();
                    }
                }

                isSendingFile = false;
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void onTyping()
    {
        if(camera_IV.getVisibility() == View.VISIBLE) AnimUtils.slideOutToRight(camera_IV, 100);
        if(gameMe_IV.getVisibility() == View.VISIBLE) AnimUtils.slideOutToRight(gameMe_IV, 100);

        sendMessageButton.setVisibility(View.VISIBLE);
        recordButton.setVisibility(View.INVISIBLE);
        refChecks.child(otherUserUid).child(myId).child("typing").setValue(1);
    }

    private void endTyping()
    {
        if(camera_IV.getVisibility() != View.VISIBLE ) AnimUtils.slideInFromRight(camera_IV, 100);
        if(gameMe_IV.getVisibility() != View.VISIBLE) AnimUtils.slideInFromRight(gameMe_IV, 100);

        sendMessageButton.setVisibility(View.INVISIBLE);
        recordButton.setVisibility(View.VISIBLE);
        refChecks.child(otherUserUid).child(myId).child("typing").setValue(0);
    }

    // show when other user is typing
    public void getMyUserTyping(String otherUid)
    {
        typingValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    long typing = snapshot.child("typing").exists() ?
                            (long) snapshot.child("typing").getValue() : -1;

                    if(typing == 0){
                        textViewMsgTyping.setText("");

                    } else if (typing == 1){
                        AnimUtils.fadeInVisible(textViewMsgTyping, 300);
                        textViewMsgTyping.setText("typing...");
                    }

                } catch (Exception e){
                    if(otherUserUid != null)refChecks.child(myId).child(otherUserUid).child("typing").setValue(0);
                    System.out.println("what is error MainAct: " + e.getMessage());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        refChecks.child(myId).child(otherUid).addValueEventListener(typingValueListener);

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

                if(adapterMap.get(otherUID_) != null && chatKey != null){

                    MessageAdapter adapter = adapterMap.get(otherUID_);
                    // find the position of the failed chat
                    assert adapter != null;
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
                            PlayersFragment.adapter.updateDeliveryStatus(otherUID_, K.fromPlayerFragment);
                            if(ChatsFragment.adapter != null) ChatsFragment.adapter.updateDeliveryStatus(otherUID_, K.fromChatFragment);

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


    //      ================      Gallery methods (photo and video)          ========================

//    private void launchPhotoAndVideoPicker() {
//        Intent intent = new Intent(Intent.ACTION_PICK);
//        intent.setType("*/*"); // Specify all MIME types
//        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"}); // Specify MIME types for photos and videos
//        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Allow multiple selection
//        pickPhotoAndVideoLauncher.launch(intent);
//    }

//    private void initialiseMultipleVideoAndPhotoPicker(){
//        // Initialize the activity result launcher
//        pickPhotoAndVideoLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
//                        // Handle the selected files
//                        // Example: Get the URIs of selected files
//                        System.out.println("what is uri " + result.getResultCode() + " data " + result.getData());
//
//                        Intent intent = result.getData();
//                        if (intent.getData() != null) {
//                            // Single file selected
//                            // Example: Uri uri = data.getData();
//                            System.out.println("what is uri single " + intent.getData());
//                            videoView.setVideoURI(intent.getData());
//                            videoView.setVisibility(View.VISIBLE);
//                            videoView.start();
//                        } else if (intent.getClipData() != null) {
//                            // Multiple files selected
//                            ClipData clipData = intent.getClipData();
//                            for (int i = 0; i < clipData.getItemCount(); i++) {
//
//                                Uri uri = clipData.getItemAt(i).getUri();
//                                videoView.setVideoPath(uri.toString());
//                                videoView.setVisibility(View.VISIBLE);
//                                videoView.start();
//                            }
//
////  content://media/picker/0/com.android.providers.media.photopicker/media/1000147624
////  content://media/external/video/media/1000070184
//                        }
//                    }
//                });
//
//    }

    //      ================      audio methods           ========================
    private void launchMultipleAudioPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*"); // Specify the MIME type to audio
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Allow multiple selection
        pickMultipleAudioLauncher.launch(intent);
    }

    // ActivityResultLauncher for multiple audio picker
    private void initialiseMultipleAudioPicker() {
        pickMultipleAudioLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), 
                result -> {

            if(result.getResultCode() == RESULT_OK && result.getData() != null) {
                Intent intent = result.getData();

                if (intent.getClipData() != null) {     //  handle multiple Uri
                    ClipData clipData = intent.getClipData();
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        Uri uri = clipData.getItemAt(i).getUri();   //  content://com.android.providers.downloads.documents/document/msf%3A1000125619
                        String convertUri = FileUtils.saveFileFromContentUriToAppStorage(uri, this);
    // convertUri ->  /storage/emulated/0/Android/data/com.pixel.chatapp/files/Media/Audio/Topper_Pheelz-JELO-feat-Young-Jonn.mp3

                        String fileName = getFileName(uri, this);

                        // Convert file size to kilobytes (KB) and megabytes (MB)
                        int fileSizeKB = (int) getAudioFileSize(uri) / 1024; // Size in kilobytes
                        int fileSizeMB = fileSizeKB / 1024; // Size in megabytes

                        String formattedDuration = formatDuration((int) getAudioDuration(uri));   // don't auto download for other user if size is greater than 500kb
                        String sizeOrDuration = fileSizeKB < 500.0 ? formattedDuration : formattedDuration +  " * Audio " + fileSizeMB + " MB";

                        MessageModel messageModel = new MessageModel(null, null, user.getUid(), null,
                                System.currentTimeMillis(), null, null, null, null,
                                700033, 4, null, null, false, false,
                                null, fileName, convertUri, sizeOrDuration, null, null);

                        chatModelList.add(messageModel);

                        if(i == clipData.getItemCount()-1){  // send the audio
                            sendSharedChat(MainActivity.this);
                        }

                    }

                } else {        // handle single uri
                    Uri uri = intent.getData();
//  content://com.android.providers.downloads.documents/document/msf%3A1000125619

                    String convertUri = FileUtils.saveFileFromContentUriToAppStorage(uri, this);
//  /storage/emulated/0/Android/data/com.pixel.chatapp/files/Media/Audio/Topper_Pheelz-JELO-feat-Young-Jonn.mp3

                    String fileName =getFileName(uri, this);

                    // Convert file size to kilobytes (KB) and megabytes (MB)
                    int fileSizeKB = (int) getAudioFileSize(uri) / 1024; // Size in kilobytes
                    int fileSizeMB = fileSizeKB / 1024; // Size in megabytes

                    // don't auto download for other user if size is greater than 500kb
                    String formattedDuration = formatDuration((int) getAudioDuration(uri));
                    String sizeOrDuration = fileSizeKB < 500.0 ? formattedDuration : formattedDuration +  " * Audio " + fileSizeMB + " MB";

                    MessageModel messageModel = new MessageModel(null, null, user.getUid(), null,
                            System.currentTimeMillis(), null, null, null, null,
                            700033, 4, null, null, false, false,
                            null, fileName, convertUri, sizeOrDuration, null, null);

                    chatModelList.add(messageModel);
                    sendSharedChat(MainActivity.this);

                }

            } else {
//                Toast.makeText(this, "Her it is null", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private long getAudioDuration(Uri audioUri) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(getApplicationContext(), audioUri);
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long duration = Long.parseLong(durationStr); // Duration in milliseconds
            retriever.release();
            return duration;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private long getAudioFileSize(Uri audioUri) {
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(audioUri, null, null, null, null);
        long size = 0;
        if (cursor != null && cursor.moveToFirst()) {
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            if (sizeIndex != -1) {
                size = cursor.getLong(sizeIndex); // Size in bytes
            }
            cursor.close();
        }
        return size;
    }


    //      ================      document methods           ========================
    private void activityDocumentLauncher(){
        pickDocumentLauncher = registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(),
                new ActivityResultCallback<List<Uri>>() {
                    @Override
                    public void onActivityResult(List<Uri> results) {
                        if (results != null && !results.isEmpty()) {

                            chatModelList.clear();

                            for (int i = 0; i < results.size(); i++) {  // I don't download document here. it's done via the adapter
                               
                                Uri documentUri = results.get(i);   //  content://com.android.providers.downloads.documents/document/msf%3A1000135295
                                String docName = getFileName(documentUri, MainActivity.this);
                                String docSize = FileUtils.getFileSize(documentUri, MainActivity.this);
                                String lowUri = null;
                                String details;

                                if(isPdfFile(documentUri, MainActivity.this))  // for pdf
                                {
                                    lowUri = getThumbnailFromPdfUri(documentUri).toString();
                                    int numberOfPages = getNumberOfPdfPages(documentUri);

                                    details = numberOfPages + " " + getString(R.string.page) + " ~ " + docSize + " ~ pdf";

                                } else if (isMsWordFile(documentUri, MainActivity.this))   // for ms word
                                {
                                    details = docSize + " ~ docx";

                                } else if (isPhotoFile(documentUri, MainActivity.this))    // for photo
                                {
                                    details = docSize + " ~ " + getString(R.string.photo);

                                } else if (isCdrFile(documentUri, MainActivity.this))  // for corel draw
                                {
                                    details = docSize + " ~ cdr";
                                } else if ( isPhotoshopFile(documentUri, MainActivity.this) )  // for photoshop
                                {
                                    details = docSize + " ~ photoshop";
                                } else if(isAudioFile(documentUri, MainActivity.this)) // for audio
                                {
                                    details = docSize + " ~ audio";
                                } else if (isVideoFile(documentUri, MainActivity.this))    // for video
                                {
                                    details = docSize + " ~ video";
                                } else {
                                    details = docSize + " ~ document";
                                }

                                String chatId = refMsgFast.child(user.getUid()).push().getKey();  // create an id for each message
                                String docNameAndDetails = docName + "\n" + details;

                                // replace emojiOnly with docNameAndDetails
                                MessageModel messageModel = new MessageModel(null, myUserName, user.getUid(), null,
                                        System.currentTimeMillis(), chatId, null, null, null, 700033,
                                        3, docSize, null, false, false, null, docNameAndDetails,
                                        null, null, lowUri, documentUri.toString());
                                // type 0 is for just text-chat, type 1 is voice_note, type 2 is photo, type 3 is document, type 4 is audio (mp3)

                                chatModelList.add(messageModel);

                                if(i == results.size()-1){  // send the document
                                    sharingPhotoActivated = true;
                                    isSharingDocument = true;
                                    startActivity(new Intent(MainActivity.this, SendImageOrVideoActivity.class));
//                                    sendSharedChat(MainActivity.this);
                                }

                            }

                        } else {
                            forwardChatUserId.clear();
                            selectedUserNames.clear();
                            chatModelList.clear();
                            sharingPhotoActivated = false;
                            isSharingDocument = false;
                        }
                    }
                });
    }

    private int getNumberOfPdfPages(Uri pdfUri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(pdfUri, "r");
            if (parcelFileDescriptor != null) {
                PdfRenderer renderer = new PdfRenderer(parcelFileDescriptor);
                int pageCount = renderer.getPageCount();
                renderer.close();
                parcelFileDescriptor.close();
                return pageCount;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Uri getThumbnailFromPdfUri(Uri uri) {
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

                Uri newUri = FileUtils.reduceImageSize(thumbnail, null, 500, this);

                return newUri;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    //      ================      call methods           ========================
    private void makeCall(String callType) {
        if(run == 0){
            Intent intent = new Intent(this, CallCenterActivity.class);
            intent.putExtra("otherUid", otherUserUid);
            intent.putExtra("myId", myId);
            intent.putExtra("otherName", otherUserName);
            intent.putExtra("myUsername", myUserName);
            intent.putExtra("answerCall", false);
            intent.putExtra("callType", callType);
            startActivity(intent);

            new Handler().postDelayed(() -> returnToCallLayout.setVisibility(View.VISIBLE), 1000);
            returnToCallWithDuration.setText(getString(R.string.returnToCall));

            currentUserUidOnCall = otherUserUid;

            activeOnCall = 0;

            String headingType = getString(R.string.audio_call);
            if(callType.equals("video")) headingType = getString(R.string.video_call);

            sendMessage(headingType, getString(R.string.connect), 6, null, null, otherUserUid, false); // audio or video call

        } else {
            Intent callIntentActivity = new Intent(this, CallCenterActivity.class);
            callIntentActivity.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(callIntentActivity);

        }
        run = 1;    // to make the call activity to open instead of recreating new instance
    }

    private void rejectCall() {
        busyCall();
    }

    public void stopRingTone(){
        callUtils.stopVibration();
        callUtils.stopRingtone();   // stop the ringtone
    }

    private void answerCall() {
        if(callModel != null){
            Intent intent = new Intent(this, CallCenterActivity.class);
            intent.putExtra("otherUid", callModel.getSenderUid());
            intent.putExtra("myId", myId);
            intent.putExtra("otherName", callModel.getSenderName());
            intent.putExtra("myUsername", myUserName);
            intent.putExtra("answerCall", true);
            intent.putExtra("callType", callType_);

            stopRingTone();

            startActivity(intent);
            new Handler().postDelayed(() -> returnToCallLayout.setVisibility(View.VISIBLE), 1000);
            returnToCallWithDuration.setText(getString(R.string.returnToCall));
        }

        callChatModel = missCallModel;  // interswitch to detect other user miss call
        missCallModel = null;

        incomingCallView.setVisibility(View.GONE);
        run = 1;    // to make the call activity to open instead of recreating new instance
        handlerOnAnotherCall.removeCallbacks(runnableOnAnotherCall);
    }

    // only alert me if I am not on another call    -- work on this later
    private void incomingCallObserver(String otherUID){
        callRepository.subscribeForLatestEvent(otherUID, (data)->{
            if (data.getType().equals(DataModelType.AudioCall) || data.getType().equals(DataModelType.VideoCall)
                    || data.getType().equals(DataModelType.AcceptVideo) || data.getType().equals(DataModelType.RejectVideo))
            {
                activeOnCall+=1;
                runOnUiThread(()->{
                    //  activeOnCall = 1, mean I have received the call signal, 2 means I have indicate to user that it's ringing here
                    if (activeOnCall <= 1 || ( currentUserUidOnCall != null && currentUserUidOnCall.equals(data.getSenderUid())) )
                    {
                        if(activeOnCall <=1)
                        {
                            iHaveACall(data);

                        } else if (currentUserUidOnCall != null) {

                            goBackToCall = getString(R.string.returnToCall);

                            if(data.getType().equals(DataModelType.VideoCall))
                            {
                                if(CallCenterActivity.callType.equals("audio")){
                                    callsListener.getRequestVideoCall(data);
                                    goBackToCall =  data.getSenderName() +" " + getString(R.string.requestForVideoCall);
                                }
                                CallCenterActivity.isOtherUserCameraOn = true;

                            } else if (data.getType().equals(DataModelType.RejectVideo))
                            {
                                callsListener.getRejectVideoCallResp(data);
                                // change mine "type" to "recall" so user can still recall
                                CallModel callModel1 = new CallModel(currentUserUidOnCall, otherUserName,
                                        user.getUid(), myUserName,null, DataModelType.RecallCall, false);
                                refCalls.child(myId).child(currentUserUidOnCall).setValue(gson.toJson(callModel1));

                            } else if (data.getType().equals(DataModelType.AcceptVideo))
                            {
                                // get  the response when other user accept my video call
                                callsListener.acceptVideoCall();

                            }else if (data.getType().equals(DataModelType.AudioCall))
                            {
                                //  notify me when other user off his camera
                                if(!CallCenterActivity.isOnVideo) { // If I'm on audio mood, change to audio mood background
                                    callsListener.myUserOffCamera();
                                }
                                CallCenterActivity.isOtherUserCameraOn = false;
                            }

                        }

                    } else {
                        data.setType(DataModelType.OnAnotherCall);    // indicate to other user that it is ringing
                        refCalls.child(myId).child(data.getSenderUid()).setValue(gson.toJson(data));
                        Toast.makeText(this, data.getSenderName() +" " +
                                getString(R.string.anotherUserIsCalling), Toast.LENGTH_SHORT).show();
                    }

                });
            } else if (data.getType().equals(DataModelType.None))
            {
                runOnUiThread(()->{
                    if(incomingCallView != null) incomingCallView.setVisibility(View.GONE);
                    stopRingTone();
                    handlerVibrate.removeCallbacks(CallCenterActivity.runnableVibrate);
                    handlerOnAnotherCall.removeCallbacks(runnableOnAnotherCall);
                    activeOnCall = 0;
                });

                if(missCallModel != null && missCallModel.getFromUid().equals(data.getSenderUid())){
                    missCallOrGameMethod(K.type_call, getString(R.string.missed), data.getSenderUid()); // update chat UI
                }

                // to enable viewCallerActivity to end or finish when other user end call
                if(callsListener != null) callsListener.myUserEndCall();  // close callCentreActivity

            }

        });

    }

    private void iHaveACall(CallModel callData){
        this.callModel = callData;
        stopRingTone();

        if(incomingCallView == null ) activateIncomingCallView();
        incomingCallView.setVisibility(View.VISIBLE);
        currentUserUidOnCall = callData.getSenderUid();

        String whoIsCalling;
        if(callData.getType().equals(DataModelType.VideoCall)) {
            whoIsCalling = getString(R.string.videoCall) +  "  " + callData.getSenderName();
            callType_ = "video";
        } else {
            whoIsCalling = getString(R.string.audioCall) +  "  " + callData.getSenderName();
            callType_ = "audio";
        }
        whoIsCallingTV.setText(whoIsCalling);
        //vibrate my tone
        callUtils.startContinuousVibration();
        callUtils.playRingtone();

        callData.setIsRinging(true);    // indicate to other user that it is ringing
        callData.setType(DataModelType.Offline);    // indicate to other user that it is ringing
        refCalls.child(myId).child(callData.getSenderUid()).setValue(gson.toJson(callData));

        // make activeCall return back to 0 after 33sec if I didn't pick
        runnableOnAnotherCall = () -> activeOnCall = 0;
        handlerOnAnotherCall.postDelayed(runnableOnAnotherCall, 33_000);

        missCallOrGameMethod(K.type_call, getString(R.string.incomingCall), callData.getSenderUid());
        // make activeCall return back to 0 after 5min if I didn't pick
//        runnableOnAnotherCall2 = () -> activeOnCall = 0;
//        handlerOnAnotherCall2.postDelayed(runnableOnAnotherCall2, 300_000);
    }


    //      ================      game methods           ========================
    private void checkIfGameHasStarted() {

        String gameId = gameSharePref.getString(K.ongoingGameId, null);
        String hostUid_ = gameSharePref.getString(K.gameHostUid, null);
        if (gameId != null && hostUid_ != null) {
            refGameStarts.child(gameId).child(hostUid_).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {

                    if (snapshot.exists()) {

                        AtomicInteger atomicInteger = new AtomicInteger(0);

                        if (snapshot.child("players").child(user.getUid()).exists()) {

                            playersInGameList.clear();
                            long totalPlayers = snapshot.child("players").getChildrenCount();

                            for (DataSnapshot playerSnapshot : snapshot.child("players").getChildren()) {

                                AwaitPlayerM getPlayer = playerSnapshot.getValue(AwaitPlayerM.class);
                                playersInGameList.add(getPlayer);

                                if (atomicInteger.incrementAndGet() == totalPlayers) {

                                    setIncomingGameView();
                                    AnimUtils.slideInFromTop(incomingGameView, 200);
                                    signalCardView.setVisibility(View.GONE);
                                    minimizedContainer.setVisibility(View.VISIBLE);

                                    minimizedTV.setText(getString(R.string.onGoingGame));

                                    onGameNow = true;
                                    isOnGameNow = true;
                                    iRelaunchApp = true;
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Handle the error
                }
            });

        }
    }

    private void incomingGameObserver()     // later -- first check if there is game signal already b4 accepting new one
    {
        refGameAlert.child(myId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){

                    snapshot.getChildren().forEach(user->{

                        if(snapshot.child("gameID").exists()) {   // clear mini error if exist
                            refGameAlert.child(myId).child("gameID").removeValue();

                        } else {

                            refGameAlert.child(myId).child("gameID").removeValue(); // clear mini error if exist
                            SignalPlayerM signalPlayerM = user.getValue(SignalPlayerM.class);

                            if(!user.getKey().equals(myId))     // I am not the host
                            {
                                if(!signalPlayerM.getGameID().equals("null") && !signalPlayerM.getGameID().equals("remove")
                                        && !myGameKey.equals(signalPlayerM.getIdKey()) )
                                {
                                    if(!onGameNow && !isOnGameNow)
                                    {
                                        myGameKey = signalPlayerM.getIdKey();
                                        gameID = signalPlayerM.getGameID();
                                        hostUid = signalPlayerM.getFromUid();
                                        gameMode = signalPlayerM.getGameMode();
                                        onGameNow = true;
                                        stopRingTone();

                                        setLayout();

                                        String getGameMode = getString(R.string.mode_) + " " + getString(R.string.free);
                                        if (gameMode.equals("stake")) {
                                            getGameMode = getString(R.string.mode_) + " " + getString(R.string.stake);
                                            stakeAmountTV.setVisibility(View.VISIBLE);
                                            rewardPoolTV.setVisibility(View.VISIBLE);
                                            hostNoteTV.setVisibility(View.VISIBLE);

                                            // set stakeAmount
                                            String stakeAmount = getString(R.string.entryFee) + " $" +  signalPlayerM.getStakeAmount();
                                            stakeAmountTV.setText(stakeAmount);

                                            // set reward prize for win
                                            String reward = getString(R.string.winnerPrize) + " $" +  signalPlayerM.getTotalStake();
                                            rewardPoolTV.setText(reward);

                                            // set host note if any
                                            String hostRemark = getString(R.string.hostRemark) + " " +  signalPlayerM.getHostNote();
                                            hostNoteTV.setText(hostRemark);

                                        }
                                        // set game mode
                                        gameModeTV.setText(getGameMode);

                                        // set host by name
                                        String name = getString(R.string.hostBy) + " " + contactNameShareRef.getString(hostUid, signalPlayerM.getSenderName());
                                        hostByTV.setText(name);

                                        //set game type
                                        String gameType = getString(R.string.game_) + " " +  signalPlayerM.getMessage();
                                        gameTypeTV.setText(gameType);

                                        //set number of expected player
                                        String numOfPlayer = getString(R.string.expectedPlayer) + " " +  signalPlayerM.getNumberOfPlayers();
                                        expectedPlayerNumTV.setText(numOfPlayer);

                                        //vibrate my tone
                                        callUtils.startContinuousVibration();
                                        callUtils.playRingtone();

                                        getGameBalance(signalPlayerM.getStakeAmount());

                                        new Handler().postDelayed(()-> signalToAwait(hostUid), 2000);

                                        if(signalPlayerM.getGameID().equals("remove")) removeSignal(signalPlayerM.getFromUid());
//                                        String gameStatus = snapshot.child(hostUid).child("players").child(myId).child("signalUpdate").getValue().toString();
//                                    if(gameStatus.equals("reject")) onGameNow = false;
//                                    if(gameStatus.equals("join")) onGameNow = false;

                                    } else {
                                        //  1.  signal me the host game
                                        Toast.makeText(MainActivity.this, getString(R.string.youHaveIncomingGame) + " "
                                                + ProfileUtils.getOtherDisplayOrUsername(signalPlayerM.getFromUid(), signalPlayerM.getSenderName()), Toast.LENGTH_LONG).show();
                                        //  2.  Tell host, I am on a game
                                    }

                                } else if(signalPlayerM.getGameID().equals("null") || signalPlayerM.getGameID().equals("remove"))
                                {
                                    removeSignal(signalPlayerM.getFromUid());
                                }

                            } else // I am the host, in_case I relaunch my app
                            {
                                if("remove".equals(signalPlayerM.getGameID())) removeSignal(signalPlayerM.getFromUid());
                                else {
                                    if(!onGameNow && !hostRelaunchApp && !isOnGameNow){
                                        DataSnapshot playersSnapshot = user.child("players");

                                        for (DataSnapshot playerSnapshot : playersSnapshot.getChildren()) {
                                            AwaitPlayerM player = playerSnapshot.getValue(AwaitPlayerM.class);
                                            if (player != null) selectedPlayerMList.add(player);
                                        }

                                        myGameKey = signalPlayerM.getIdKey();
                                        gameID = signalPlayerM.getGameID();
                                        hostUid = signalPlayerM.getFromUid();
                                        gameMode = signalPlayerM.getGameMode();
                                        stakeAmount = signalPlayerM.getStakeAmount();
                                        hostNote = signalPlayerM.getHostNote();
                                        stopRingTone();

                                        setIncomingGameView();
                                        AnimUtils.slideInFromTop(incomingGameView, 200);
                                        signalCardView.setVisibility(View.GONE);
                                        minimizedContainer.setVisibility(View.VISIBLE);
                                        new Handler().postDelayed(()-> AnimUtils.slideInFromTop(minimizedContainer, 5000), 1000);

                                        onGameNow = true;
                                        onAwaitActivity = true;
                                        hostRelaunchApp = true;
                                        hostReopen = true;
                                    }

                                }
                            }
                        }

                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void setLayout(){
        setIncomingGameView();
        AnimUtils.slideInFromTop(incomingGameView, 200);
        AnimUtils.slideInFromTop(signalCardView, 200);
        minimizedContainer.setVisibility(View.GONE);

        setJoinGameViews();

        stakeAmountTV.setVisibility(View.GONE);
        rewardPoolTV.setVisibility(View.GONE);
        hostNoteTV.setVisibility(View.GONE);
    }

    private void signalToAwait(String hostUid){

        GameAPI gameAPI = K.retrofit.create(GameAPI.class);

        IdTokenUtil.generateToken(token -> {
            TwoValueM twoValueM = new TwoValueM(token, hostUid);
            gameAPI.await(twoValueM).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if(response.isSuccessful()){
                        Toast.makeText(MainActivity.this, getString(R.string.incomingGame), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, @NonNull Throwable throwable) {
                    Toast.makeText(MainActivity.this, getString(R.string.noInternetConnection), Toast.LENGTH_SHORT).show();
                    System.out.println("Checking error: " + throwable.getMessage());
                }
            });
        }, this);
    }

    private void removeSignal(String hostUid){
        stopRingTone();

        setIncomingGameView();
        incomingGameView.setVisibility(View.GONE);

        refGameAlert.child(myId).child(hostUid).removeValue();
        onGameNow = false;
    }

    private void getGameBalance(String stakeAmount){
        WalletUtils.balance(this, new WalletCallBack() {
            @Override
            public void onSuccess(@NonNull AssetsModel assetsModel) {
                String getGameBal = NumberSpacing.formatNumberWithCommas(assetsModel.getGameAsset());
                String gameBal = getString(R.string.gameBal) + " $" + getGameBal;
                if(joinGameViews != null) gameBalTV.setText(gameBal);

                if(stakeAmount != null){
                    String getStakeAmount = NumberSpacing.formatNumberWithCommas(stakeAmount);
                    String deductNotice =  "$" + getStakeAmount + " " + getString(R.string.gameDecision);
                    notice_TV.setText(deductNotice);
                }

            }

            @Override
            public void onFailure() {
                System.out.println("what is wallet error occur MainActivity L7190");
            }
        });
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

        // remove the typingRunnable for checking network
        handlerTyping.removeCallbacks(runnableTyping);

        cancelChatOption(); // close the option chat menu that pop up

        if(appActivity || otherUserUid == null || !insideChat) {
            constraintMsgBody.setVisibility(View.INVISIBLE);
            if(!doneSelectingPlayers) topMainContainer.setVisibility(View.VISIBLE);
//            firstTopChatViews.setVisibility(View.INVISIBLE);
            closePinIcons();    // for sharing photo from other app

            // open the previous Activity it was on before user shared
            if(appActivity){
                Intent mainActivityIntent = new Intent(context, AppLifecycleHandler.currentActivity);
                mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                context.startActivity(mainActivityIntent);
            }
            appActivity = false;
        }

        if(doneSelectingPlayers) {
            new Handler().postDelayed(this::delayCloseForwardView, 3000);
            doneSelectingPlayers = false;
        } else {
            delayCloseForwardView();
        }

        // delete low photo from app storage if user cancel the sending
        if(sharingPhotoActivated) {
            deleteUnusedPhotoFromSharePrefsAndAppMemory(this);  // call method to delete file
            sharingPhotoActivated = false;
            chatModelList.clear();
        } else chatModelList.clear();

        viewPager2General.setUserInputEnabled(true);    // enable the swiping

        sharing = false; // previous Main Activity from replace the currentActivity when user is sharing photo from other app or gallery

        if (onSelectPlayer || onSelectNewPlayer) {

            if(!doneSelectingPlayers) {
                Intent mainActivityIntent = new Intent(this, WhotOptionActivity.class);
                if(onSelectNewPlayer) mainActivityIntent = new Intent(this, AwaitPlayersActivity.class);
                mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(mainActivityIntent);
                onSelectPlayer = false;
                onSelectNewPlayer = false;
            }

            if(addPlayerConfirmView != null) addPlayerConfirmView.setVisibility(View.GONE);

        }

    }

    private void delayCloseForwardView()
    {
        if(firstTopChatViews != null){
            pinMsgConst.setVisibility(View.VISIBLE);
            firstTopChatViews.setVisibility(View.VISIBLE);
        }
        if(!insideChat){
            tabLayoutGeneral.setVisibility(View.VISIBLE);
            topMainContainer.setVisibility(View.VISIBLE);
        } else {
            constraintMsgBody.setVisibility(View.VISIBLE);
        }
        // call forward setting method to remove the checkBox
        if(onForwardTopView != null) onForwardTopView.setVisibility(View.GONE);
        if(forwardDownContainer != null) forwardDownContainer.setVisibility(View.INVISIBLE);
        if(ChatsFragment.adapter != null) {
            ChatsFragment.openContactList.setVisibility(View.VISIBLE);
            ChatsFragment.newInstance().notifyVisibleUser();
        }

    }

    public static void cancelChatOption()
    {
        if(chatOptionView != null) chatOptionView.setVisibility(View.GONE);
        modelChatsOption = null;
        if(!sharingPhotoActivated) chatModelList.clear();
        editChatOption_IV.setVisibility(View.VISIBLE);
        replyChatOption_IV.setVisibility(View.VISIBLE);
        moreOption_IV.setVisibility(View.VISIBLE);
        editChatOption_IV.setImageResource(R.drawable.baseline_mode_edit_24);
        isOnLongClick = 0;

//        if(pinChatViews != null) pinChatViews.setVisibility(View.VISIBLE);

        // clear chat highlight position
        MessageAdapter.chatPositionList.clear();

        onShare = false;
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
//        if(pinChatViews == null) setPinChatViews();     // onClosePin()
        pinMsgBox_Constr.setVisibility(View.GONE);
        pinMsgConst.setVisibility(View.GONE);   // hide pin Msg container
        totalPinPrivate_TV.setText(""); // make pin count null
        totalPinPublic_TV.setText(""); // make pin count null
        pinNextPrivate = 1;  // return pinNumber to default
        pinNextPublic = 1;  // return public pinNumber to default
        pinScrollPublic = 0;
        pinScrollPrivate = 0;
        if(pinForWhoViews != null) pinForWhoViews.setVisibility(View.GONE);  // close the option box
        line.setVisibility(View.GONE);
        pinStatus = "null";
    }

    // cancel delete option for user from chat list
    private void cancelUserDeleteOption()
    {
        userModelList.clear();
        if(ChatsFragment.adapter != null) ChatsFragment.adapter.otherUidLongPressList.clear();
        if(PlayersFragment.adapter != null) PlayersFragment.adapter.otherUidLongPressList.clear();

        otherUserName_TV.setText(null);
        deleteUserOrClearChatViews.setVisibility(View.GONE);
        if(onUserMoreLongPressView != null){
            userLongPressMoreSub.setVisibility(View.GONE);
            onUserMoreLongPressView.setVisibility(View.GONE);
        }
        if(onUserLongPressView != null) onUserLongPressView.setVisibility(View.GONE);
        onUserLongPress = false;

        if(ChatsFragment.adapter != null) ChatsFragment.newInstance().notifyVisibleUser();
        if(PlayersFragment.adapter != null) PlayersFragment.newInstance().notifyVisibleUser();

    }

    private void cancelChatDeleteOption(){
        deleteForWhoView.setVisibility(View.GONE);
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

        layoutParams.bottomMargin = screenHeight;
        // Apply the changes
        constraintLayoutAdjust.setLayoutParams(layoutParams);

        Toast.makeText(this, getString(R.string.app_name), Toast.LENGTH_SHORT).show();

    }

    // clear old photo from app memory
    public static boolean deleteOldUriFromAppMemory(List<String> uriToDelete, Context context){   // the list contain both low and hugh quality uri path
        for (int i = 0; i < uriToDelete.size(); i++) {
            if(uriToDelete.get(i) != null && !uriToDelete.get(i).startsWith("content")){

                Uri uriPhoto = Uri.parse(uriToDelete.get(i));
                File searchPhotoPath = new File(uriPhoto.getPath());

                if(searchPhotoPath.exists()) {
                    searchPhotoPath.delete();

                }

            }
            // notify that there's uri list on sharePreference so as to clear the sharePref when done
            if(i == uriToDelete.size()-1){
                return true;
            }
        }
        return false;
    }

    public static void deleteSingleUriFromAppMemory(String uriToDelete){   // the list contain both low and hugh quality uri path
        Uri uriPhoto = Uri.parse(uriToDelete);
        File searchPhotoPath = new File(uriPhoto.getPath());

        if(searchPhotoPath.exists()) {
            searchPhotoPath.delete();
        }
//        return false;
    }

    public static void deleteUnusedPhotoFromSharePrefsAndAppMemory(Context context){
        //  store each photo cropping or painting uri to enable delete from onCreate when app is onDestroy
        unusedPhotoShareRef = context.getSharedPreferences(K.URI_PREF, Context.MODE_PRIVATE);

        // delete the uri photo from app memory
        String json = unusedPhotoShareRef.getString(K.OLD_URI_LIST, null);
        if(json != null){
            Gson gson = new Gson();
            List<String> uriList = gson.fromJson(json, List.class);

            boolean isDoneDeleting = uriList != null && deleteOldUriFromAppMemory(uriList, context);
            if(isDoneDeleting){
                unusedPhotoShareRef.edit().remove(K.OLD_URI_LIST).apply();
            }
        }
    }

    public void clearInputFields(boolean addReply)
    {
        if(addReply)
        {
            runOnUiThread(()->{
                nameReply.setVisibility(View.GONE);
                replyVisible.setVisibility(View.GONE);
                cardViewReplyOrEdit.setVisibility(View.GONE);
                textViewReplyOrEdit.setText("");
                clearAllHighlights();
            });

            listener = "no";
            replyText = null;
            replyFrom = null;
            idKey = null;
            editChatModel = null;
        }
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
            if(CacheUtils.getCacheSize(this) > 100) {
                Glide.get(getApplicationContext()).clearDiskCache();
            }
        }).start();
    }

    // set user image on settings
    private void setUserDetails(){
        refUsers.child(myId).child("general").addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                imageLink = snapshot.child("image").exists() && !snapshot.child("image").getValue().toString().equals("null")
                        ? snapshot.child("image").getValue().toString() : null;

                String displayName = snapshot.child("displayName").exists() ?
                        snapshot.child("displayName").getValue().toString() : null;

                String userName = snapshot.child("userName").exists() ?
                        snapshot.child("userName").getValue().toString() : null;

                String hint = snapshot.child("hint").exists() && !snapshot.child("userName").getValue().toString().isEmpty()
                        ? snapshot.child("hint").getValue().toString() : getString(R.string.hint2);

                if (sideBarView != null)
                {
                    if(imageLink != null) Picasso.get().load(imageLink).into(imageViewUserPhoto);
                    textViewDisplayName.setText(displayName);
                    phoneNumber_TV.setText(NumberSpacing.formatPhoneNumber(user.getPhoneNumber(), 3, 3));
                    textViewUserName.setText("@"+ userName);
                    hint_TV.setText(hint);
                }

                // update share_preference
                myProfileShareRef.edit().putString(K.PROFILE_USERNAME, userName).apply();
                myProfileShareRef.edit().putString(K.PROFILE_DISNAME, displayName).apply();
                myProfileShareRef.edit().putString(K.PROFILE_HINT, hint).apply();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        generateFCMToken();
    }

    // generate FCM token   - for notification
    public void generateFCMToken()
    {
        // After login is successful, retrieve the current FCM token
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task ->
        {
            if (!task.isSuccessful()) {
                System.out.println("what is failed token generate: " + task.getException());
                return;
            }

            // Get new FCM registration token
            String token = task.getResult();

            // Send token to your server
            refUsers.child(myId).child("general").child("fcmToken").setValue(token);
        });


    }


    private void getProfileSharePref(){

        // update share_preference
        String username = myProfileShareRef.getString(K.PROFILE_USERNAME, "---");
        String displayName = myProfileShareRef.getString(K.PROFILE_DISNAME, "---");
        String hint = myProfileShareRef.getString(K.PROFILE_HINT, getString(R.string.hint2));
//        String email = myProfileShareRef.getString(K.PROFILE_EMAIL, "---");

        textViewDisplayName.setText(displayName);
        textViewUserName.setText("@"+ username);       // display the database userName to the input field
//        setEmailTV.setText(user.getEmail());
        phoneNumber_TV.setText(NumberSpacing.formatPhoneNumber(user.getPhoneNumber(), 3, 3));
        hint_TV.setText(hint);

        if(imageLink != null && sideBarView != null) Picasso.get().load(imageLink).into(imageViewUserPhoto);

    }

    private void devicePermission()
    {
        if(!appPermissionCheck.isContactOk(this))
        {
            appPermissionCheck.requestContact(this);
        }

        if(!appPermissionCheck.isStorageOk(this))
        {
            appPermissionCheck.requestStorage(this);
        } else {
            new Thread(() -> FetchContacts.readContacts(this)).start();   // permission
        }

        if(!appPermissionCheck.isNotificationOk(this))
        {
            appPermissionCheck.requestNotification(this);
        }
    }

    private void removeValueListeners()
    {
        if(typingValueListener != null) {   // remove the typing listener
            refChecks.child(myId).child(otherUserUid).removeEventListener(typingValueListener);
        }

        if (chatReadListener != null) {     // remove the read receipt listener
            refOnReadRequest.child(myId).child(otherUserUid).removeEventListener(chatReadListener);
        }

        if(lastSeenValueListener != null){
            refUsers.child(otherUserUid).child("general").removeEventListener(lastSeenValueListener);
        }
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
            PhoneUtils.hideKeyboard(this, this.getCurrentFocus()); // hide keyboard before sending

            // turn off my online and set my last seen date/time
//            refUsers.child(myId).child("general").child("presence").setValue(ServerValue.TIMESTAMP);

            K.executors.execute(()->{

                if(constraintMsgBody.getVisibility() == View.VISIBLE){
                    try{
                        refChecks.child(otherUserUid).child(myId).child("typing").setValue(0);
//                        int scroll = scrollNum > 10 ? scrollNum: adapterMap.get(otherUserUid).getItemCount() - 1;

                        Map<String, Object> mapUpdate = new HashMap<>();
                        mapUpdate.put("status", false);
                        mapUpdate.put("newMsgCount", 0);
                        refChecks.child(myId).child(otherUserUid).updateChildren(mapUpdate);

                        // save last scroll position to local preference
                        lastPositionPreference.edit().putInt(otherUserUid, scrollNum).apply();     // onPause

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

            PhoneUtils.hideKeyboard(this, this.getCurrentFocus()); // hide keyboard if any

            toggleCallButtonVisibility();

            refUsers.child(user.getUid()).child("general").child("presence").setValue(1);
            //  reverse the emoji initialization back to the emoji button icon

            popup = EmojiPopup.Builder.fromRootView( recyclerContainer).build(editTextMessage);

            if(constraintMsgBody.getVisibility() == View.VISIBLE)
            {
                // delay the focus so it doesn't pop up the keyboard
                new Handler().postDelayed(()-> editTextMessage.requestFocus(), 1000);

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

    private void toggleCallButtonVisibility(){
       if(onPictureMood){
           returnToCallLayout.setVisibility(View.GONE);
           if(firstTopChatViews != null) callButton.setVisibility(View.GONE);
       } else {
           if(firstTopChatViews != null) callButton.setVisibility(View.VISIBLE);
       }
    }

    OnBackPressedCallback callback = new OnBackPressedCallback(true)
    {
        @Override
        public void handleOnBackPressed() {

            if (selectGameView != null && selectGameView.getVisibility() == View.VISIBLE)
            {
                selectGameView.setVisibility(View.GONE);
                targetPlayer = false;
            } else if (constraintMsgBody.getVisibility() == View.VISIBLE)
            {
                boolean isEmojiVisible_ = popup.isShowing();
                if (isEmojiVisible_)
                {
                    popup.dismiss();
                    isEmojiVisible = false;
                    typeMsgContainer.setVisibility(View.VISIBLE);

                } else if (onChatClickView != null && onChatClickView.getVisibility() == View.VISIBLE)
                {
                    onChatClickView.setVisibility(View.GONE);

                } else if (chatMenuViews != null && chatMenuViews.getVisibility() == View.VISIBLE)
                {
                    chatMenuViews.setVisibility(View.GONE); // close profile menu
                    scrollViewMenu.setVisibility(View.GONE);

                } else if (fileOptionViews != null && fileOptionViews.getVisibility() == View.VISIBLE)
                {
                    fileOptionViews.setVisibility(View.GONE);
                    fileContainerAnim.setVisibility(View.GONE);
                } else if (chatOptionView != null && chatOptionView.getVisibility() == View.VISIBLE)
                {
                    cancelChatOption();
                    generalBackground.setVisibility(View.GONE);
                    if(topEmojiView != null) topEmojiView.setVisibility(View.GONE);
                    if(moreOptionViews != null) moreOptionViews.setVisibility(View.GONE);
                    if(firstTopChatViews != null) firstTopChatViews.setVisibility(View.VISIBLE);

                } else if (deleteForWhoView != null && deleteForWhoView.getVisibility() == View.VISIBLE)
                {
                    cancelChatDeleteOption();
                } else if (pinForWhoViews != null && pinForWhoViews.getVisibility() == View.VISIBLE) {
                    pinForWhoViews.setVisibility(View.GONE);
                    cancelChatOption();
                }
                else if (audioOrVideoView != null && audioOrVideoView.getVisibility() == View.VISIBLE)
                {
                    audioOrVideoView.setVisibility(View.GONE);
                }
                else {

                    emoji_IV.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
                    handlerEmoji.removeCallbacks(emojiRunnable);
                    et_emoji.clearFocus();
                    editTextMessage.clearFocus();
                    editTextMessage.setText("");    // store each user unsent typed msg later

                    // General settings
                    constraintMsgBody.setVisibility(View.INVISIBLE);
                    topMainContainer.setVisibility(View.VISIBLE);
                    tabLayoutGeneral.setVisibility(View.VISIBLE);
                    if(topEmojiView != null) topEmojiView.setVisibility(View.GONE);
                    emoji_IV.setImageResource(R.drawable.baseline_add_reaction_24);
                    isEmojiVisible = false;
                    textViewMsgTyping.setText(null);
                    scrollCountTV.setVisibility(View.GONE);
                    scrollPositionIV.setVisibility(View.GONE);
                    receiveIndicator.setVisibility(View.GONE);
                    sendIndicator.setVisibility(View.GONE);
                    textViewLastSeen.setVisibility(View.INVISIBLE);
                    textViewLastSeen.setText(getString(R.string.app_name));   // clear last seen
                    bioHint_TV.setVisibility(View.INVISIBLE);
                    bioHint_TV.setText(null);   // clear last seen

                    clearInputFields(true);     //onBackPress- edit and reply settings cancel
                    closePinIcons();        // hide the pin icons

                    // highlight send message and new receive message indicator
                    receiveIndicator.setVisibility(View.GONE);
                    sendIndicator.setVisibility(View.GONE);

                    // clear chat highlight position
                    MessageAdapter.chatPositionList.clear();

//                    // make previous view clickable if any
//                    if (ChatListAdapter.previousView != null) {
//                        ChatListAdapter.previousView.setClickable(true);
//                        ChatListAdapter.previousView = null;    // return it back to null
//                    }

                    K.executors.execute(() -> {

                        if (otherUserUid != null) {
                            Map<String, Object> mapUpdate = new HashMap<>();
                            mapUpdate.put("status", false);
                            mapUpdate.put("newMsgCount", 0);
                            refChecks.child(myId).child(otherUserUid).updateChildren(mapUpdate);
                            // save last scroll position to local preference
                            lastPositionPreference.edit().putInt(otherUserUid, scrollNum).apply();     // onBackpress
                            System.out.println("M3052 I have saved scroll " + scrollNum + " to uid: " + otherUserUid);

                            // reset new chat count number -- inside and outside
                            try{
                                adapterMap.get(otherUserUid).getChatByPinTypeAndDeleteViaRecycler(recyclerMap.get(otherUserUid), otherUserUid);   // onBackPress
                            } catch (Exception e) {
                                System.out.println("what is error homeA L7320: " + e.getMessage());
                            }

                            insideChatMap.put(otherUserUid, false);

                            removeValueListeners();

                            // clear notification bar
                            NotificationHelper.clearMessagesForUser(otherUserUid, MainActivity.this);

                            // remove the typingRunnable for checking network
                            handlerTyping.removeCallbacks(runnableTyping);
                            networkTypingOk = true;

                            insideChat = false;  // onBackPress
                            idKey = null;
                        }

                    });

                }

            } else if (onForward)
            {
                cancelForwardSettings(MainActivity.this);    // onBackPress
            } else if (walletVerifyView != null && walletVerifyView.getVisibility() == View.VISIBLE)
            {
                walletVerifyView.setVisibility(View.GONE);

            } else if (chatOptionView != null && chatOptionView.getVisibility() == View.VISIBLE)
            {
                cancelChatOption();
                generalBackground.setVisibility(View.GONE);
                moreOptionViews.setVisibility(View.GONE);

            } else if (sideBarView != null && sideBarView.getVisibility() == View.VISIBLE)
            {
                sideBarMenuContainer.setVisibility(View.GONE);
            } else if (moreOptionHomeView != null && moreOptionHomeView.getVisibility() == View.VISIBLE)
            {
                moreOptionHomeView.setVisibility(View.GONE);
                moreHomeCont2.setVisibility(View.GONE);
            } else if (viewPager2General.getCurrentItem() != 0)
            {
                viewPager2General.setCurrentItem(0, true);
            } else if ( (deleteUserOrClearChatViews != null && deleteUserOrClearChatViews.getVisibility() == View.VISIBLE)
                    || (onUserLongPressView != null && onUserLongPressView.getVisibility() == View.VISIBLE) )
            {
                cancelUserDeleteOption();
            } else if (run == 1)
            {
                Intent callIntentActivity = new Intent(MainActivity.this, CallCenterActivity.class);
                callIntentActivity.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(callIntentActivity);

            } else if(close == 0)
            {
                Toast.makeText(MainActivity.this, getString(R.string.pressAgain), Toast.LENGTH_SHORT).show();
                close = 1;
                new Handler().postDelayed( ()-> close = 0, 5_000);

            } else
            {
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
                setEnabled(true);
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == K.RECORDING_REQUEST_CODE && grantResults.length > 0
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

//        } else if (requestCode == K.WRITE_REQUEST_CODE && grantResults.length > 0
//                &&  grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(this, "write is granted", Toast.LENGTH_SHORT).show();

        }else if(requestCode == K.CALL_CAMERA_REQUEST_CODE && grantResults.length > 0
                &&  grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            if(appPermissionCheck.isRecordingOk(this)){
                if(!makeCall){
                    answerCall();
                } else {
                    activateAudioOrVideoOptionView();
                    audioOrVideoView.setVisibility(View.VISIBLE);
                }

            } else {
                appPermissionCheck.requestRecordingForCall(this);
            }

        }
        else if(requestCode == K.CALL_RECORDING_REQUEST_CODE && grantResults.length > 0
                &&  grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            answerCall();
        }
        else if(requestCode == K.CONTACTS_REQUEST_CODE && grantResults.length > 0
                &&  grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            new Thread(() -> FetchContacts.readContacts(this)).start();   // onRequestPermission
        }
        else if (requestCode == K.BIOMETRIC_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            // Permission granted
            Toast.makeText(this, "I have verify!", Toast.LENGTH_SHORT).show();
            showFingerPrint();
        } else {
            if(requestCode == K.RECORDING_REQUEST_CODE){
                Toast.makeText(mainActivityContext, "Go to phone app settings and permit Microphone", Toast.LENGTH_SHORT).show();
            } else if(requestCode == K.CALL_CAMERA_REQUEST_CODE || requestCode == K.CALL_RECORDING_REQUEST_CODE){
                rejectCall();
                Toast.makeText(mainActivityContext, getString(R.string.permissionCall), Toast.LENGTH_SHORT).show();
            }
//            else if (requestCode == K.WRITE_REQUEST_CODE) {
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

    //  ==========  call interface listener
    @Override
    public void endCall() {
//        MainActivity.updateCallOrGameChat(K.type_call, getString(R.string.noResponse)); // update chat UI

        run = 0;   // restart to 0 so it can create new instead
        activeOnCall = 0;
        callButton.setVisibility(View.VISIBLE);
        currentUserUidOnCall = null;
        callModel = null;
        returnToCallLayout.setVisibility(View.GONE);
        returnToCallWithDuration.setText(getString(R.string.returnToCall));
        if(incomingCallView != null) incomingCallView.setVisibility(View.GONE);

        new Handler().postDelayed(()-> {
            activeOnCall = 0;
        }, 2000);
    }

    @Override
    public void callConnected(String duration) {
        String dur = goBackToCall + "   " + duration;

        returnToCallWithDuration.setText(dur);
    }

    @Override
    public void returnToCallLayoutVisibilty() {
        returnToCallLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void isConnecting(String return_duration) {
        if(incomingCallView != null) incomingCallView.setVisibility(View.GONE);
        returnToCallLayout.setVisibility(View.VISIBLE);
        returnToCallWithDuration.setText(getString(R.string.returnToCall));
        activeOnCall = 1;
        goBackToCall = getString(R.string.returnToCall);
        run = 1;    // to make the call activity to open instead of recreating new instance
        handlerOnAnotherCall.removeCallbacks(runnableOnAnotherCall);
    }

    @Override
    public void busyCall() {

        MainActivity.updateCallOrGameChat(K.type_call, getString(R.string.busy)); // update chat UI

        CallModel data = new CallModel(myId, myUserName, otherUserUid, otherUserName, null, DataModelType.Busy, false);
        refCalls.child(myId).child(currentUserUidOnCall).setValue(gson.toJson(data));
        CallModel callModel = new CallModel(myId, myUserName, otherUserUid, otherUserName, null, DataModelType.None, false);
        refCalls.child(currentUserUidOnCall).child(myId).setValue(gson.toJson(callModel));

        stopRingTone();
        endCall();
    }

    @Override
    public void openOnForwardView() {
        setForwardChat();
    }

    @Override
    public void openSelectGameOption() {
        selectGameView.setVisibility(View.VISIBLE);
    }

    @Override       // for target player (just 2 individaul players)
    public void proceedToAwaitActivity(@NonNull String stakeAmount_, @NonNull String gameMode_, @NonNull String hostNote_)
    {
        stakeAmount = stakeAmount_;
        hostNote = hostNote_;

        moveToAwaitPlayers(gameMode_, stakeAmount_);    // one on one player

    }

    @Override
    public void showMinimiseGameAlert(boolean openSignal, boolean gameHasStarted, String whichGameActivity)
    {
        setIncomingGameView();
        if(openSignal){
            AnimUtils.slideInFromTop(incomingGameView, 400);
            signalCardView.setVisibility(View.GONE);
            minimizedTV.setText(getString(R.string.awaitingPlayer));
            minimizedContainer.setVisibility(View.VISIBLE);
            onAwaitActivity = true;

            if(gameHasStarted) {
                minimizedTV.setText(getString(R.string.onGoingGame));
                isOnGameNow = true;
                whichGameActivity_ = whichGameActivity;
            }

        } else {
            incomingGameView.setVisibility(View.GONE);
            onAwaitActivity = false;
            Intent mainActivityIntent = new Intent(this, AwaitPlayersActivity.class);
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(mainActivityIntent);
        }

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

















