package com.pixel.chatapp.chats;

import static com.pixel.chatapp.all_utils.FileUtils.compressVideo;
import static com.pixel.chatapp.all_utils.FileUtils.createVideoThumbnail;
import static com.pixel.chatapp.all_utils.FileUtils.downloadThumbnailFile;
import static com.pixel.chatapp.all_utils.FileUtils.formatDuration;
import static com.pixel.chatapp.all_utils.FileUtils.isFileLessThan150Kb;
import static com.pixel.chatapp.all_utils.FileUtils.parseDuration;
import static com.pixel.chatapp.all_utils.FileUtils.replaceSnapPhotoWithCompressFile;
import static com.pixel.chatapp.all_utils.FileUtils.saveFileFromContentUriToAppStorage;
import static com.pixel.chatapp.all_utils.FolderUtils.getAudioFolder;
import static com.pixel.chatapp.all_utils.FolderUtils.getThumbnailFolder;
import static com.pixel.chatapp.all_utils.FolderUtils.getVoiceNoteFolder;
import static com.pixel.chatapp.home.MainActivity.chatViewModel;
import static com.pixel.chatapp.home.MainActivity.clearAllHighlights;
import static com.pixel.chatapp.home.MainActivity.firstTopUserDetailsContainer;
import static com.pixel.chatapp.home.MainActivity.networkOk;
import static com.pixel.chatapp.home.MainActivity.otherUserUid;
import static com.pixel.chatapp.home.MainActivity.recyclerMap;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pixel.chatapp.photos.ViewImageActivity;
import com.pixel.chatapp.adapters.ChatListAdapter;
import com.pixel.chatapp.all_utils.FileUtils;
import com.pixel.chatapp.all_utils.FolderUtils;
import com.pixel.chatapp.interface_listeners.FragmentListener;
import com.pixel.chatapp.Permission.Permission;
import com.pixel.chatapp.R;
import com.pixel.chatapp.constants.AllConstants;
import com.pixel.chatapp.home.MainActivity;
import com.pixel.chatapp.model.MessageModel;
import com.pixel.chatapp.model.PinMessageModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    public  List<MessageModel> modelList;

    // Define a HashSet to store unique message IDs
    private Set<String> messageIdSet = new HashSet<>(); // to avoid duplicate adding chat, store each id chat here

    public static int lastPosition = 0;
    public static List<Integer> chatPositionList = new ArrayList<>();
    public String uId;
    public String userName;
    private String otherName;
    public  Context mContext;

    private int status;
    private final int send;
    private final int sendPhoto;
    private final int receive;
    private final int receivePhoto;
    private final String myId;
    FirebaseUser user;
    Map<String, Integer> dateMonth = new HashMap<>();
    DatabaseReference refCheck, refUsers, refMsgFast, refLastDetails, refOnReadRequest, refPinMessages;
    private StorageReference deletePathRef = FirebaseStorage.getInstance().getReference();
    private MessageViewHolder lastOpenViewHolder = null;
    Handler handler;
    private static final String VOICE_NOTE = "MyPreferences";
    private static final String KEY_LIST = "myList";
    private List<Map<String, Object>> mapList;
    private Permission permissions = new Permission();

    private FragmentListener fragmentListener;
    public void setFragmentListener(FragmentListener fragmentListener) {
        this.fragmentListener = fragmentListener;
    }

    // I am using "static" so it add up only once and reuse for all chat box
    public static List<View> viewCacheSend = new ArrayList<>(); // List to store views for caching
    public static List<View> viewCacheReceive = new ArrayList<>(); // List to store views for caching
    public static List<View> viewPhotoSend = new ArrayList<>(); // List to store views for caching
    public static List<View> viewPhotoReceive = new ArrayList<>(); // List to store views for caching
    private LayoutInflater inflater;
    private ViewGroup parent;

    // voice note global declares
    private Runnable runnable;
    private Handler handler1;
    private String lastIdKeyChat;
    MessageViewHolder lastHolder_;
    private final Map<String, Integer> mapLastDuration = new HashMap<>();
    private final Map<String, MediaPlayer> mediaPlayerMap = new HashMap<>();
    private final Map<String, Integer> progressMap = new HashMap<>(); // hide download icon


    // =========    send image
    Map<String, UploadTask> uploadTaskMap = new HashMap<>();
    Map<String, FileDownloadTask> fileDownloadTaskMap = new HashMap<>();

    public static boolean isOnlongPress = false; // the will enable the background highlight checker for onLongPress

    public MessageAdapter( List<MessageModel> modelList, String userName, String uId,
                          Context mContext, ViewGroup parent) {
        this.modelList = modelList;
        this.userName = userName;
        this.uId = uId;
        this.mContext = mContext;
        this.parent = parent;
//        this.otherName = otherName;
        handler = new Handler(Looper.getMainLooper());

        handler1 = new Handler();

        send = 1;
        receive = 2;
        sendPhoto = 3;
        receivePhoto = 4;

        status = send;

        user = FirebaseAuth.getInstance().getCurrentUser();
        refCheck = FirebaseDatabase.getInstance().getReference("Checks");
        refUsers = FirebaseDatabase.getInstance().getReference("Users");
        refMsgFast = FirebaseDatabase.getInstance().getReference("MsgFast");
        refLastDetails = FirebaseDatabase.getInstance().getReference("UsersList");
        refOnReadRequest = FirebaseDatabase.getInstance().getReference("OnReadRequest");
//        refPinMessages = FirebaseDatabase.getInstance().getReference("PinMessages");

        myId = user.getUid();
        this.inflater = LayoutInflater.from(mContext);

    }


    public List<MessageModel> getModelList() {
        return modelList;
    }

    public void setModelList(List<MessageModel> modelList) {
        // Clear the existing message ID set before adding new message IDs
        messageIdSet.clear();

        // Iterate through the modelList to extract message IDs and add them to the messageIdSet
        for (MessageModel message : modelList) {
            messageIdSet.add(message.getIdKey());
        }

        // Set the new modelList
        this.modelList = modelList;

    }

    // add new message to list method
    public void addNewMessageDB(MessageModel newMessage) {

        // Check if the message ID is already present in the HashSet
        if (!messageIdSet.contains(newMessage.getIdKey())) {
            // Add the new message ID to the HashSet
            messageIdSet.add(newMessage.getIdKey());

            // Add the new message to the list
            modelList.add(newMessage);
        } else {
            // Display a toast message to indicate that the chat message already exists
            Toast.makeText(mContext, "Chat message already exists", Toast.LENGTH_SHORT).show();
        }

//        if(!modelList.contains(newMessage)) {
//            modelList.add(newMessage);
//        } else {
//            Toast.makeText(mContext, "Chat can't duplicate", Toast.LENGTH_SHORT).show();
//        }
    }

    public void updateMessage(MessageModel chatModel, MessageViewHolder replyHolder) {

        int numLoop = modelList.size() > 100 ? modelList.size() - 50 : 0;
        for (int i = modelList.size()-1; i >= numLoop; i--) {
            String all_IDs = modelList.get(i).getIdKey();
            if (chatModel.getIdKey().equals(all_IDs)) { // Use equals() for string comparison
                modelList.remove(i);
                modelList.add(i, chatModel);
                if(chatModel.getEmojiOnly() == null){
                    replyHolder.emojiOnly_TV.setVisibility(View.GONE);
                }
                notifyItemChanged(i, new Object());
                break;
            }
        }
    }

    public void deleteMessage(String id){
        for (int i = modelList.size()-1; i >= 0; i--) {
            String all_IDs = modelList.get(i).getIdKey();
            if (id.equals(all_IDs)) { // Use equals() for string comparison
                modelList.remove(i);
                break;
            }
        }
    }

    //  delete all chat with user
    public void clearChats() {
        modelList.clear();
    }

    // replace the view that was removed as user scroll
    private class PreloadViewsTask extends AsyncTask<Void, Void, Void> {
        private final int viewTypeSelect;
        public PreloadViewsTask(int viewTypeSelect) {
            this.viewTypeSelect = viewTypeSelect;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            // Preload views in the background and add them to the cache
            if(viewTypeSelect == send){
                View itemView = inflater.inflate(R.layout.view_card, parent, false);
                viewCacheSend.add(itemView);
                System.out.println("Adding to viewSend " + viewCacheSend.size());
            } else if (viewTypeSelect == sendPhoto) {
                View itemView = inflater.inflate(R.layout.photo_send_card, parent, false);
                viewPhotoSend.add(itemView);
                System.out.println("Adding to viewPhotoSend " + viewPhotoSend.size());
            } else if (viewTypeSelect == receivePhoto) {
                View itemView = inflater.inflate(R.layout.photo_receive_card, parent, false);
                viewPhotoReceive.add(itemView);
                System.out.println("Adding to viewPhotoRe " + viewPhotoReceive.size());
            } else {
                View itemView = inflater.inflate(R.layout.view_card_receiver, parent, false);
                viewCacheReceive.add(itemView);
                System.out.println("Adding to viewReceive " + viewCacheReceive.size());
            }

            return null;
        }
    }

    // add 10 views on first load
    public class PreloadTenViewsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            // Preload views in the background and add them to the cache
            for (int i = 0; i < 40; i++) {

                View itemView;

                if(i % 2 == 0){
                    itemView = inflater.inflate(R.layout.view_card, parent, false);
                    synchronized (viewCacheSend) {
                        viewCacheSend.add(itemView);
                        System.out.println("first loading Send: " + viewCacheSend.size());
                    }
                } else if (i % 3 == 0) {
                    itemView = inflater.inflate(R.layout.photo_send_card, parent, false);
                    synchronized (viewPhotoSend) {
                        viewPhotoSend.add(itemView);
                        System.out.println("first loading Send: " + viewPhotoSend.size());
                    }
                } else{
                    itemView = inflater.inflate(R.layout.view_card_receiver, parent, false);
                    synchronized (viewCacheSend) {
                        viewCacheReceive.add(itemView);
                        System.out.println("first loading Receive: " + viewCacheReceive.size());
                    }
                }

            }
            return null;
        }

    }

    // called at CA212
    public void addLayoutViewEverySec() {
        Executor executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {

            for (int i = 0; i < 4; i++) {

                View itemView;

                if(i % 2 == 0){
                    itemView = inflater.inflate(R.layout.view_card, parent, false);
                    synchronized (viewCacheSend) {
                        viewCacheSend.add(itemView);
                        System.out.println("Added to viewCacheSend: " + viewCacheSend.size());
                    }
                } else if (i % 3 == 0) {
                    itemView = inflater.inflate(R.layout.photo_send_card, parent, false);
                    synchronized (viewPhotoSend) {
                        viewPhotoSend.add(itemView);
                        System.out.println("Added to viewPhotoLayer: " + viewPhotoSend.size());
                    }
                } else if (i % 4 == 0) {
                    itemView = inflater.inflate(R.layout.photo_receive_card, parent, false);
                    synchronized (viewPhotoReceive) {
                        viewPhotoReceive.add(itemView);
                        System.out.println("Added to viewPhotoRe:  " + viewPhotoReceive.size());
                    }
                } else{
                    itemView = inflater.inflate(R.layout.view_card_receiver, parent, false);
                    synchronized (viewCacheSend) {
                        viewCacheReceive.add(itemView);
                        System.out.println("Added to viewCacheReceive: " + viewCacheReceive.size());
                    }
                }

            }

        });
    }

    // add first 15 on first load
    public void addLayoutViewInBackground() {
        Executor executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {

            for (int i = 0; i < 60; i++) {

                if(i == 58) {
                    if (!MainActivity.isLoadViewRunnableRunning && viewCacheSend.size() < 50) {
                        MainActivity.handlerLoadViewLayout.post(MainActivity.loadViewRunnable);
                    }
                    ((Activity) mContext).runOnUiThread(() -> {
                        Toast.makeText(mContext, "view adding done MA340", Toast.LENGTH_SHORT).show();
                    });
                }

                View itemView;

                if(i % 2 == 0){
                    itemView = inflater.inflate(R.layout.view_card, parent, false);
                    synchronized (viewCacheSend) {
                        viewCacheSend.add(itemView);
                        System.out.println("Added to viewCacheSend:  " + viewCacheSend.size());
                    }
                } else if (i % 3 == 0) {
                    itemView = inflater.inflate(R.layout.photo_send_card, parent, false);
                    synchronized (viewPhotoSend) {
                        viewPhotoSend.add(itemView);
                        System.out.println("Added to viewPhotoSend:  " + viewPhotoSend.size());
                    }
                }else if (i % 4 == 0) {
                    itemView = inflater.inflate(R.layout.photo_receive_card, parent, false);
                    synchronized (viewPhotoReceive) {
                        viewPhotoReceive.add(itemView);
                        System.out.println("Added to viewPhotoRe:  " + viewPhotoReceive.size());
                    }
                } else{
                    itemView = inflater.inflate(R.layout.view_card_receiver, parent, false);
                    synchronized (viewCacheSend) {
                        viewCacheReceive.add(itemView);
                        System.out.println("Added to viewCacheReceive: " + viewCacheReceive.size());
                    }
                }

            }

        });
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_msg, parent, false);

        View itemView;
        if (viewType == send && !viewCacheSend.isEmpty())
        {
            // Retrieve and remove a cached send view
            itemView = viewCacheSend.remove(0);
            new PreloadViewsTask(viewType).execute();   // add a new view
//            Toast.makeText(mContext, "adding view 1", Toast.LENGTH_SHORT).show();

        } else if (viewType == sendPhoto && !viewPhotoSend.isEmpty())
        {

            itemView = viewPhotoSend.remove(0);
            new PreloadViewsTask(viewType).execute();

        } else if (viewType == receivePhoto && !viewPhotoReceive.isEmpty())
        {

            itemView = viewPhotoReceive.remove(0);
            new PreloadViewsTask(viewType).execute();

        } else if (viewType == receive && !viewCacheReceive.isEmpty())
        {
            // Retrieve and remove a cached receive view
            itemView = viewCacheReceive.remove(0);
            new PreloadViewsTask(viewType).execute();   // add a new view

        } else
        {
            // Inflate a new view if the cache is empty or the view type doesn't match
            int layer;
            if(viewType == send){
                layer = R.layout.view_card;
                Toast.makeText(mContext, "view sender", Toast.LENGTH_SHORT).show();
            } else if (viewType == sendPhoto) {
                layer = R.layout.photo_send_card;
                Toast.makeText(mContext, "view photo send", Toast.LENGTH_SHORT).show();
            } else if (viewType == receivePhoto) {
                layer = R.layout.photo_receive_card;
                Toast.makeText(mContext, "view photo receive", Toast.LENGTH_SHORT).show();
            }else {
                layer = R.layout.view_card_receiver;
                Toast.makeText(mContext, "view receiver", Toast.LENGTH_SHORT).show();
            }

            itemView = inflater.inflate(layer, parent, false);

        }
        return new MessageViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position_) {

        int chatPosition = position_;     //   to get the position of each chat
        MessageModel modelChats = modelList.get(chatPosition);    // get the model position of each chat

        // reset all data to default
        holder.pinIcon_IV.setVisibility(View.GONE);         // pin icon reset
        holder.forwardIcon_IV.setVisibility(View.GONE);     // forward icon reset
        if(holder.editNotify != null){
            holder.editNotify.setVisibility(View.GONE);     // edit icon reset
        }       // edit icon
        if(holder.seenMsg != null ) holder.seenMsg.setImageResource(0);   // seen icon
        if(holder.textViewShowMsg != null) {
            holder.textViewShowMsg.setText("");
            // Reset width and height properties of textTV
            holder.textViewShowMsg.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
            holder.textViewShowMsg.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }   //  reset chat textView and size
        if(holder.emojiOnly_TV != null){        // reset emoji only
            holder.emojiOnly_TV.setText("");
        }
        holder.timeMsg.setText("");             // time reset
        if(holder.constraintChatTop != null){
            closeMyOwnOption(modelList.get(position_), holder);
//            holder.constraintChatTop.setVisibility(View.GONE);
        }   // top chat option reset
        if(holder.react_TV != null){
            holder.react_TV.setVisibility(View.GONE);
        }       // react emoji reset
        // reset voice note tools player
        if(holder.seekBarProgress != null){
            holder.seekBarProgress.setVisibility(View.GONE);
            holder.circleDownload.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.GONE);
            holder.speedTV.setVisibility(View.GONE);
            holder.pauseAndPlay_IV.setVisibility(View.GONE);
            holder.seekBarProgress.setProgress(0);
            holder.totalMinute_TV.setText("");
        }
        // reset image and document tools
        if(holder.loadProgressTV != null){
            holder.loadProgressTV.setVisibility(View.GONE); // hide image loading progress bar
            holder.progressBarLoad.setVisibility(View.GONE); // hide image loading progress bar
            holder.photoChatTV.setVisibility(View.GONE);
            holder.documentContainer.setVisibility(View.GONE);
            holder.document_IV.setVisibility(View.GONE);
            holder.documentDetails_TV.setText("");
            holder.loadProgressTV.setText("1 %");
            holder.showImage.setVisibility(View.VISIBLE);
            holder.showImage.setImageURI(null);
            int orangeColor = ContextCompat.getColor(mContext, R.color.white);
            holder.progressBarLoad.setIndeterminateTintList(ColorStateList.valueOf(orangeColor));

        }
        // reset photo chat textView

        holder.itemView.setBackgroundColor(0);  //  reset background highlight


        //  ==============    input the real data to their positions after resetting    ============


        holder.timeMsg.setText( chatTime(modelChats.getTimeSent()) ); // show the time each msg was sent

        if(modelChats.getMessage() != null){
            if(holder.textViewShowMsg != null)
                holder.textViewShowMsg.setText(modelChats.getMessage() + chatPosition);
            // display text for photo
            if(holder.photoChatTV != null && modelChats.getMessage().length() > 0 ) {
                holder.photoChatTV.setVisibility(View.VISIBLE);
                holder.photoChatTV.setText(modelChats.getMessage());
            }
        }   //  Show messages

        if(modelChats.getEmojiOnly() != null && modelChats.getType() == 0){
            holder.emojiOnly_TV.setText(modelChats.getEmojiOnly());
            holder.emojiOnly_TV.setVisibility(View.VISIBLE);
        }

        // set edit icon on chat
        if(modelChats.getEdit() != null){
            if(modelChats.getEdit().equals("edited"))
                holder.editNotify.setVisibility(View.VISIBLE);
        }

        // set forward icon on chat
        if(modelChats.getIsChatForward() != null && modelChats.getIsChatForward()){
            holder.forwardIcon_IV.setVisibility(View.VISIBLE);
        }

        // set pin icon on chat
        if(modelChats.getIsChatPin() != null && modelChats.getIsChatPin()){
            holder.pinIcon_IV.setVisibility(View.VISIBLE);
        }

        //  set emoji reaction
        if(modelChats.getEmoji() != null){
            setEmojiReact(holder, modelChats.getEmoji());
        }


        // ----------------- reply msg setting
        int intValue = (int) modelChats.getVisibility();
        if(holder.linearLayoutClick != null) holder.linearLayoutClick.setVisibility(intValue);
        holder.linearLayoutReplyBox.setVisibility(intValue);    // set reply container to visibility
        holder.senderNameTV.setText(modelChats.getReplyFrom());  //  set the username for reply msg
        holder.replyChat_TV.setText(modelChats.getReplyMsg());     //   set the reply text on top msg

        // set unsent and sent msg... delivery and seen settings-- msg status tick
        int intMsg = modelChats.getMsgStatus();
        int numMsg = R.drawable.baseline_grade_24;

        // 700024 --- tick one msg  // 700016 -- send msg   // 700033 -- load
        if(intMsg == 700033){   // load
            numMsg = R.drawable.message_load;
        } else if (intMsg == 700024) {  // read
            numMsg = R.drawable.message_tick_one;
        }

        // set seen chat
        if(holder.seenMsg != null ) {
            holder.seenMsg.setImageResource(numMsg);     // set msg status tick
        }


        // set load image progress bar
        if(holder.loadProgressTV != null && modelChats.getPhotoUriOriginal() != null){
            if(!modelChats.getFromUid().equals(myId) &&
                    ( modelChats.getPhotoUriOriginal().startsWith("media/photo") || modelChats.getPhotoUriOriginal().startsWith("media/document") )
                    || modelChats.getMsgStatus() == 700033){

                if(uploadTaskMap.get(modelChats.getIdKey()) != null)
                {
                    uploadTaskMap.get(modelChats.getIdKey()).addOnProgressListener(taskSnapshot ->{
                        holder.progressBarLoad.setVisibility(View.VISIBLE);
                        holder.loadProgressTV.setVisibility(View.VISIBLE);
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        trackLoadProgress(progress, holder, modelChats);
                    });

                } else if(fileDownloadTaskMap.get(modelChats.getIdKey()) != null)
                {
                    fileDownloadTaskMap.get(modelChats.getIdKey()).addOnProgressListener(taskSnapshot -> {
                        holder.progressBarLoad.setVisibility(View.VISIBLE);
                        holder.loadProgressTV.setVisibility(View.VISIBLE);
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        trackLoadProgress(progress, holder, modelChats);
                    });
                }else {
                    loadBarVisibility(holder, modelChats);
                }

            }
        }

        // set image and document => type 0 is for just text-chat, type 1 is voice_note, type 2 is photo, type 3 is document, type 4 is audio (mp3)
        if(modelChats.getPhotoUriPath() != null || modelChats.getType() == 3){

            // activate my auto image sending  ------ change later to auto download other user photo that he sent according to his settings
            if( modelChats.getFromUid().equals(myId) && modelChats.getMsgStatus() == 700033
                    && modelChats.getPhotoUriOriginal() != null ){
                // get the photo uid owner and the sending state
                String photoCheck = MainActivity.documentIdShareRef.getString(modelChats.getIdKey(), "");
                String[] splitX = photoCheck.split(AllConstants.JOIN);
                // check if the ref is not empty
                if(splitX.length > 1){
                    String otherId = splitX[0];
                    String isSending = splitX[1];
                    if(isSending.equals("yes")){
                        if(networkOk){
                            int orangeColor = ContextCompat.getColor(mContext, R.color.white);
                            holder.progressBarLoad.setIndeterminateTintList(ColorStateList.valueOf(orangeColor));
                            holder.loadProgressTV.setText("2 %");
                            loadMyPhotoOrDocument(modelChats, holder, chatPosition, otherId);
                        } else {
                            // add to sharePreference to load later
                        }
                    }
                    // it's loading now, don't repeat.
                    MainActivity.documentIdShareRef.edit()
                            .putString(modelChats.getIdKey(), otherId + AllConstants.JOIN + "no").apply();
                }

            }

            // display or download the low quality image or document thumbnail  (if not downloaded yet)
            if (modelChats.getPhotoUriPath() != null){
                Uri imageUri_ = Uri.parse(modelChats.getPhotoUriPath());
                displayLowImageOrDocumentThumbnail(imageUri_, holder, modelChats);

                // download the low quality photo or doc thumbnail auto from firebase (if not downloaded yet)
                AllConstants.executors.execute(() -> {
                    downloadLowImageFrom_FB_Storage(modelChats, imageUri_);
                });

            } else {
                holder.showImage.setVisibility(View.GONE);
                holder.documentContainer.setVisibility(View.VISIBLE);
                holder.documentDetails_TV.setText(modelChats.getEmojiOnly());   // I used document details to replace emojiOnly
                if(holder.loadProgressTV.getVisibility() == View.VISIBLE){
                    holder.document_IV.setVisibility(View.VISIBLE);
                } else {
                    holder.document_IV.setVisibility(View.GONE);
                }
            }

//            Picasso.get().load(imageUri_).into(new Target() {
//                @Override
//                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                    // download the low thump image from firebase on first arrival if image is from other user
//                    downloadLowImageFrom_FB_Storage(modelChats, bitmap);
//                    holder.showImage.setImageBitmap(bitmap);
//
//                }
//
//                @Override
//                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
//                    holder.showImage.setImageResource(R.color.transparent_orangeLow);
////                    Toast.makeText(mContext, "error loading image MA580 " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//
//                @Override
//                public void onPrepareLoad(Drawable placeHolderDrawable) {
//                    holder.showImage.setImageResource(R.color.transparent_orangeLow);
//                }
//            });

            // content://media/external/images/media/1000124641
//            holder.showImage.setImageURI(Uri.parse("/external/images/media/1000123946"));
//    working ->  holder.showImage.setImageURI(Uri.parse("content://media/external/images/media/1000124641"));
        }


        // ----- Voice Note and Audio setting
        int visible = (int) modelChats.getType();   //  1 is visible, 4 is invisible, 8 is Gone
        if( holder.seekBarProgress != null && (visible == 1 || visible == 4) ){
            // display the view setting
            holder.seekBarProgress.setVisibility(View.VISIBLE);
            holder.totalMinute_TV.setVisibility(View.VISIBLE);
            holder.totalMinute_TV.setText(modelChats.getVnDuration()); // set the time duration
            holder.pauseAndPlay_IV.setVisibility(View.VISIBLE);
            holder.speedTV.setVisibility(View.VISIBLE);

            // check if my voice note sending is downloading and display the progress
            if(modelChats.getMsgStatus() == 700033 && modelChats.getFromUid().equals(myId)){
                holder.circleDownload.setVisibility(View.VISIBLE);
                holder.speedTV.setVisibility(View.GONE);
                // if voice note is already downloading, show the progress it has reached
                displaySendingProgress(modelChats, holder);
                // auto send voice note -- get the voice note uid owner and the sending state
                activateSendingVN(modelChats, holder, chatPosition);
            }

            // download other user voice note automatic
            if(modelChats.getVoiceNote().startsWith("media/voice_note")
                    || modelChats.getVoiceNote().startsWith("media/audio") && !modelChats.getFromUid().equals(myId))
            {
                holder.pauseAndPlay_IV.setVisibility(View.INVISIBLE);
                holder.circleDownload.setVisibility(View.VISIBLE);
                if(fileDownloadTaskMap.get(modelChats.getIdKey()) != null){
                    holder.circleDownload.setVisibility(View.INVISIBLE);
                }
                // check for network first
                if(networkOk){
                    // check if download is already in progress and if voice note duration is less than 1MB
                    if(fileDownloadTaskMap.get(modelChats.getIdKey()) == null && holder.circleDownload.getVisibility() == View.VISIBLE
                            && !modelChats.getVnDuration().contains("~") && !modelChats.getVnDuration().contains("Audio") ){

                        downloadOtherUserVoiceNote(modelChats, holder, position_);  // settings

                    } else {
                        // show downloading progress since it's already downloading
                        if(fileDownloadTaskMap.get(modelChats.getIdKey()) != null) {
                            fileDownloadTaskMap.get(modelChats.getIdKey()).addOnProgressListener(taskProgress ->{
                                double progress = (100.0 * taskProgress.getBytesTransferred()) / taskProgress.getTotalByteCount();
                                trackLoadProgress(progress, holder, modelChats);    // voice note - this works when it 75% plus
                                if(progressMap.get(modelChats.getIdKey()) != null){
                                    int runnableProgress = progressMap.get(modelChats.getIdKey());
                                    holder.progressBar.setProgress(runnableProgress);
                                }
                            });
                        }
                    }
                }
            }

            //update the progress seekBar when user scroll back to voice note that's still playing
            showTheVoiceNotePlaying(modelChats, holder);

        }


        // --------------------------   Settings end   ----------------------------------------------------------------------


        View.OnClickListener optionClickListener = view -> {
            // process onClick if longPress is not yet activated
            if(MainActivity.chatOptionsConstraints.getVisibility() != View.VISIBLE){

                // Close the previously open chat options
                closePreviousChatOption(modelChats, holder);

                // make option menu visible if it's gone
                makeChatOptionVisible(modelChats, holder, chatPosition);

            } else {
                // if onLongPress mood is activated, add or remove chat from list when user click a chat
                addOrRemoveChatFromList(modelChats, chatPosition);
            }
        };
        //   show chat selection options via onClick
        holder.constraintMsgContainer.setOnClickListener(optionClickListener);
        if(holder.textViewShowMsg != null )
            holder.textViewShowMsg.setOnClickListener(optionClickListener);

        //   show chat selection options on top via onLongClick
        View.OnLongClickListener longClick = (view -> {

            if(MainActivity.chatOptionsConstraints.getVisibility() != View.VISIBLE){
                // activate long click press and send data to MainActivity
                activateOnLongClick(modelChats, holder, chatPosition);
            } else {
                // if onLongPress mood is activated, add or remove chat from list when user click a chat
                addOrRemoveChatFromList(modelChats, chatPosition);
            }

            return true;
        });
        holder.linearLayoutReplyBox.setOnLongClickListener(longClick);
        holder.constraintMsgContainer.setOnLongClickListener(longClick);
        if(holder.photoChatTV != null ) holder.photoChatTV.setOnLongClickListener(longClick);
        if(holder.textViewShowMsg != null ) holder.textViewShowMsg.setOnLongClickListener(longClick);

        //  image, document and progressLoad bar onClick settings
        if(holder.showImage != null){

            // for long press
            holder.loadProgressTV.setOnLongClickListener(longClick);
            holder.showImage.setOnLongClickListener(longClick);
            holder.documentContainer.setOnLongClickListener(longClick);

            // for single onClick =>    type 0 is for just text-chat, type 1 is voice_note, type 2 is photo, type 3 is document, type 4 is audio (mp3)
            View.OnClickListener imageOrDocOnClick = view -> {
                // check if longPress is activated yet or not
                if(MainActivity.chatOptionsConstraints.getVisibility() != View.VISIBLE
                ){
                    // open image if I was the one that sent them photo
                    if(modelChats.getFromUid().equals(myId) ||
                            (!modelChats.getFromUid().equals(myId) && holder.progressBarLoad.getVisibility() == View.GONE) )
                    {
                        if((modelChats.getType() == 2 || modelChats.getType() == 5) // it is photo or video
                                && !modelChats.getPhotoUriOriginal().startsWith("media/photo"))
                        {
                            openPhoto(modelChats, holder);  // swipe to view all photo
                        } else if (modelChats.getType() == 3 /* || modelChats.getMsgStatus() != 700033 */)
                        {
                            // open document
                            try {
                                FileUtils.openDocumentFromUrl(mContext, modelChats.getPhotoUriOriginal());
                            } catch (URISyntaxException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            Toast.makeText(mContext, mContext.getString(R.string.notSentYet), Toast.LENGTH_SHORT).show();
                        }
                        // Close the previously open chat options
                        closePreviousChatOption(modelChats, holder);

                    } else{ // download the photo other user sent to me
                        if(fileDownloadTaskMap.get(modelChats.getIdKey()) != null){
                            if(fileDownloadTaskMap.get(modelChats.getIdKey()).isInProgress() ){
                                fileDownloadTaskMap.get(modelChats.getIdKey()).cancel();
                                fileDownloadTaskMap.remove(modelChats.getIdKey());  // reset the map for that chat id
                                notifyItemChanged(chatPosition);
                                Toast.makeText(mContext, mContext.getString(R.string.dCancel), Toast.LENGTH_SHORT).show();
                            }

                        }else
                            downloadPhotoOrDocSentByOtherUser(modelChats, holder, chatPosition);
                    }

                } else {
                    // if onLongPress mood is activated, add or remove chat from list when user click a chat
                    addOrRemoveChatFromList(modelChats, chatPosition);
                }

            };
            holder.showImage.setOnClickListener(imageOrDocOnClick);
            holder.documentContainer.setOnClickListener(imageOrDocOnClick);

            holder.loadProgressTV.setOnClickListener(view -> {
//System.out.println("what is uri " + modelChats.getPhotoUriOriginal());
                if(MainActivity.chatOptionsConstraints.getVisibility() != View.VISIBLE){

                    if(modelChats.getPhotoUriOriginal() != null){
                        if(modelChats.getFromUid().equals(myId)){
                            if(networkOk)
                                sendMyPhotoOrDocument(modelChats, holder, chatPosition);
                            else Toast.makeText(mContext, mContext.getString(R.string.isNetwork), Toast.LENGTH_SHORT).show();
                        }  else{ // download the photo other user sent to me
                            if(fileDownloadTaskMap.get(modelChats.getIdKey()) != null){
                                if(fileDownloadTaskMap.get(modelChats.getIdKey()).isInProgress() ){
                                    fileDownloadTaskMap.get(modelChats.getIdKey()).cancel();
                                    fileDownloadTaskMap.remove(modelChats.getIdKey());
                                    notifyItemChanged(chatPosition);
                                    Toast.makeText(mContext, mContext.getString(R.string.dCancel), Toast.LENGTH_SHORT).show();
                                }

                            }else
                                downloadPhotoOrDocSentByOtherUser(modelChats, holder, chatPosition);
                        }
                    } else Toast.makeText(mContext, "Resend file boss", Toast.LENGTH_SHORT).show();

                } else {
                    // if onLongPress mood is activated, add or remove chat from list when user click a chat
                    addOrRemoveChatFromList(modelChats, chatPosition);
                }

            });
        }

        //  top chats option onClick settings
        if(holder.imageViewReply != null ){ // chat reply icon

            // voice note play button
            holder.pauseAndPlay_IV.setOnClickListener(view -> {

                if(lastIdKeyChat != null){

                    if(lastIdKeyChat.equals(modelChats.getIdKey())){
//                        mediaPlayerMap.put(modelChats.getIdKey(), new MediaPlayer());
                        playOrPauseVoiceNote(holder, modelChats);
                    } else {

                        MediaPlayer mediaPlayer = mediaPlayerMap.get(lastIdKeyChat);
                        if(mediaPlayer.isPlaying()){
                            // pause previous voice note if playing
                            mediaPlayer.pause();
                            lastHolder_.pauseAndPlay_IV.setImageResource(R.drawable.baseline_play_arrow_24);
                            mapLastDuration.put(lastIdKeyChat, mediaPlayer.getCurrentPosition());
                            handler1.removeCallbacks(runnable);

                            // change back to the total time duration
                            String timeLabel = modelChats.getVnDuration().contains("Audio") ? formatDuration(mediaPlayer.getDuration()) +  " ~ Audio"
                                    : formatDuration(mediaPlayer.getDuration());
                            lastHolder_.totalMinute_TV.setText(timeLabel);

                            // stop media player and create new instance
                            mediaPlayer.release();
                            mediaPlayerMap.put(lastIdKeyChat, new MediaPlayer());

                            // play the new selected voice note
                            mediaPlayerMap.put(modelChats.getIdKey(), new MediaPlayer());
                            playOrPauseVoiceNote(holder, modelChats);

                        } else {
                            mediaPlayerMap.put(modelChats.getIdKey(), new MediaPlayer());
                            playOrPauseVoiceNote(holder, modelChats);
                        }

                        lastHolder_ = holder;
                        lastIdKeyChat = modelChats.getIdKey();

                    }

                } else {    // media is playing for the first time.
                    mediaPlayerMap.put(modelChats.getIdKey(), new MediaPlayer());
                    playOrPauseVoiceNote(holder, modelChats);
                    lastIdKeyChat = modelChats.getIdKey();
                    lastHolder_ = holder;
                }

                seekBarListener(modelChats, holder);

            });

            // forward chat option
            holder.imageViewForward.setOnClickListener(view -> {

                // clear list and save to chat list
                MainActivity.chatModelList.clear();
                MainActivity.chatModelList.add(modelChats);

                fragmentListener.onForwardChat();

                holder.constraintChatTop.setVisibility(View.GONE);  // close option menu

                // reverse arrow
                closeMyOwnOption(modelChats, holder);
            });

            // reply option
            holder.imageViewReply.setOnClickListener(view -> {
                replyChat(modelChats, holder);
                closeMyOwnOption(modelChats, holder);
            });

            // edit option
            holder.imageViewEdit.setOnClickListener(view -> {

                int deliveryStatus = modelChats.getMsgStatus();
                int positionCheck = modelList.size() - chatPosition;    // 1000 - 960 => 40

                if(deliveryStatus == 700033){
                    Toast.makeText(mContext, "Check your network connection", Toast.LENGTH_SHORT).show();
                } else if (positionCheck > 100)
                {
                    Toast.makeText(mContext, "Edit recent message", Toast.LENGTH_SHORT).show();
                } else if(modelChats.getType() == 1 || modelChats.getType() == 4){
                    Toast.makeText(mContext, "This chat can't be edited for now!", Toast.LENGTH_SHORT).show();
                }else {
                    // send data to MainActivity via interface listener
                    fragmentListener.onEditOrReplyMessage(modelChats,"edit", "editing...",
                            android.R.drawable.ic_menu_edit, View.GONE, holder);
                }
                // reverse arrow
                closeMyOwnOption(modelChats, holder);

            });

            // delete option
            holder.imageViewDel.setOnClickListener(view -> {

                if(modelChats.getMsgStatus() == 700033){
                    Toast.makeText(mContext, "Check your network connection", Toast.LENGTH_SHORT).show();
                } else {

                    holder.constraintChatTop.setVisibility(View.GONE);

                    // for saving chatModel to chat list
                    MainActivity.chatModelList.clear();
                    MainActivity.chatModelList.add(modelChats);

                    fragmentListener.onDeleteMessage();  // call method on MainActivity(L700)

                    // reverse arrow
                    closeMyOwnOption(modelChats, holder);
                }
            });

            // emoji react onClick option
            holder.imageViewReact.setOnClickListener(view -> {

                // Highlight the clicked item
                View itemView = recyclerMap.get(otherUserUid).getLayoutManager().findViewByPosition(chatPosition);
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.transparent_orangeLow));

                // remove previous highlight is any
                if(chatPosition != lastPosition) {
                    View itemView2 = recyclerMap.get(otherUserUid).getLayoutManager().findViewByPosition(lastPosition);
                    if(itemView2 != null){
                        itemView2.setBackgroundColor(0);
                    }
                }

                //re-assign last chat position for deleting highlight
                lastPosition = chatPosition;

                try{
                    fragmentListener.onEmojiReact(holder, modelChats.getIdKey());
                }catch (Exception e){
                    System.out.println("Urgent error at MA320" + e.getMessage());
                };

                // reverse arrow
                closeMyOwnOption(modelChats, holder);

            });

            // copy option
            holder.imageViewCopy.setOnClickListener(view -> {

                String selectedText = modelChats.getMessage();
                ClipboardManager clipboard =  (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", selectedText);

                if (clipboard == null || clip == null) return;
                clipboard.setPrimaryClip(clip);

                Toast.makeText(mContext, "Copied!", Toast.LENGTH_SHORT).show();
                // for paste code
//                ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
//                try {
//                    CharSequence text = clipboard.getPrimaryClip().getItemAt(0).getText();
//                } catch (Exception e) {
//                    return;
//                }
                // reverse arrow
                closeMyOwnOption(modelChats, holder);
                holder.constraintChatTop.setVisibility(View.GONE);

            });

            // pin options -- (for me or everyone)
            holder.imageViewPin.setOnClickListener(view -> {

                // send pin chat data to MainActivity
                fragmentListener.onPinData(modelChats.getIdKey(), modelChats.getMessage(),
                        ServerValue.TIMESTAMP, userName, holder);

                closeMyOwnOption(modelChats, holder);
            });

            holder.imageViewOptions.setOnClickListener(optionClickListener);

            // download / send voice note
            holder.circleDownload.setOnClickListener(view -> {
                // check if the voice note is from me.
                if(modelChats.getFromUid().equals(myId)){
                    // get the photo uid owner and the sending state
                    String photoCheck = MainActivity.voiceNoteIdShareRef.getString(modelChats.getIdKey(), "");
                    String[] splitX = photoCheck.split(AllConstants.JOIN);
                    // check if the ref is not empty
                    if(splitX.length > 1) {
                        String otherId = splitX[0];
                        String isSending = splitX[1];
                        if(MainActivity.networkOk){
                            if(isSending.equals("no") || holder.circleDownload.getVisibility() == View.VISIBLE){
                                // send voice note
                                sendMyVoiceNote(modelChats, holder, otherId, chatPosition);    // for button click
                            }
                        } else Toast.makeText(mContext, "No internet connection 😔", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    // download the voice note sent from other user
                    if(networkOk){
                        // check if download is already in progress and if voice note duration is less than 5minutes
                        if(fileDownloadTaskMap.get(modelChats.getIdKey()) == null && holder.circleDownload.getVisibility() == View.VISIBLE){

                            downloadOtherUserVoiceNote(modelChats, holder, position_);  // onClick

                        }
                    } else Toast.makeText(mContext, "No internet connection 😔", Toast.LENGTH_SHORT).show();

                }

            });

        }

        // single onClick -- scroll and highlight reply message
        View.OnClickListener scrollToReplyChat = view -> {

            if(MainActivity.chatOptionsConstraints.getVisibility() != View.VISIBLE) {

                holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.transparent_orange));

                holder.itemView.animate().scaleX(1.2f).scaleY(1.2f).setDuration(10).withEndAction(() ->
                {
                    String originalMessageId = modelChats.getReplyID();
                    int originalPosition = findMessagePositionById(originalMessageId);

                    // Scroll to the original message's position
                    if (originalPosition != RecyclerView.NO_POSITION) {

                        recyclerMap.get(otherUserUid).scrollToPosition(originalPosition-2);

                        highlightItem(originalPosition);

                        // when the down-arrow button on MainActivity(444) is clicked, it should check  goToLastMessage first
                        MainActivity.goToNum = chatPosition;
                        // goToLastMessage = true; then scroll to the previous message, else scroll down as usual
                        MainActivity.goToLastMessage = true;

                    }

                    lastPosition = originalPosition;    // for deleting highlight

                    // clear highlight
                    new Handler().postDelayed(()-> {
                        // Reset the scale
                        holder.itemView.setScaleX(1.0f);
                        holder.itemView.setScaleY(1.0f);
                        // reset the background o=colour
                        holder.itemView.setBackgroundColor(0);
                    }, 100);

                }).start();

            } else {
                // if onLongPress mood is activated, add or remove chat from list when user click a chat
                addOrRemoveChatFromList(modelChats, chatPosition);
            }

        };
        holder.linearLayoutReplyBox.setOnClickListener(scrollToReplyChat);
        holder.senderNameTV.setOnClickListener(scrollToReplyChat);
        holder.replyChat_TV.setOnClickListener(scrollToReplyChat);
        if(holder.linearLayoutClick != null ) { // reply background fill-up onClick
            holder.linearLayoutClick.setOnClickListener(scrollToReplyChat);
            holder.linearLayoutClick.setOnLongClickListener(longClick);
        }
        // onLongClick for reply box highlights
        holder.linearLayoutReplyBox.setOnLongClickListener(longClick);
        holder.senderNameTV.setOnLongClickListener(longClick);
        holder.replyChat_TV.setOnLongClickListener(longClick);

        // check it's on onLongPress and retain chat position highlight
        retainHighlight(chatPosition);

    }


    // ---------------------- methods ---------------------------

    private void closePreviousChatOption(MessageModel modelChats, MessageViewHolder holder){
        // Close the previously open chat options
        if (lastOpenViewHolder != null && lastOpenViewHolder != holder) {
            // check if the lastHolder has the chatTop Constraint
            if(lastOpenViewHolder.constraintChatTop != null){
                lastOpenViewHolder.constraintChatTop.setVisibility(View.GONE);

                // reverse the image resource to it's original imageView
                if(modelChats.getFromUid().equals(myId)){
                    lastOpenViewHolder.imageViewOptions.setImageResource(R.drawable.arrow_left);
                } else{
                    lastOpenViewHolder.imageViewOptions.setImageResource(R.drawable.arrow_right_);
                }
            }
        }
    }

    private void closeMyOwnOption(MessageModel modelChats, MessageViewHolder holder){
        if(holder.constraintChatTop != null){
            holder.constraintChatTop.setVisibility(View.GONE);
            // reverse the image resource to it's original imageView
            if(modelChats.getFromUid().equals(myId)){
                holder.imageViewOptions.setImageResource(R.drawable.arrow_left);
            } else{
                holder.imageViewOptions.setImageResource(R.drawable.arrow_right_);
            }
        }
    }
    // make option menu visible if it's gone
    private void makeChatOptionVisible(MessageModel modelChats, MessageViewHolder holder, int chatPosition){
        // make option menu visible if it's gone
        if(holder.constraintChatTop != null){
            if(holder.constraintChatTop.getVisibility() == View.GONE){

                holder.constraintChatTop.setVisibility(View.VISIBLE);

                // indicate sign that msg can't be edited
                if(modelList.size() - chatPosition > 100){
                    int fadedOrangeColor = ContextCompat.getColor(mContext, R.color.transparent_orange);
                    holder.imageViewEdit.setColorFilter(fadedOrangeColor);
                }

                holder.imageViewOptions.setImageResource(R.drawable.baseline_cancel_24);

                // change the pin icon to unpin/view
                pinStatusIcon(holder, modelChats);

            } else{ // hide if it's visible and return arrow image
                // close mine chatTop Constraint
                closeMyOwnOption( modelChats, holder );
            }
        }

        // Update the last open ViewHolder
        lastOpenViewHolder = holder;
    }

    // check if the chat is pin privately or publicly
    private void pinStatusIcon(MessageViewHolder holder, MessageModel modelChats){
        boolean isPrivatePin = false;
        boolean isPublicPin = false;
        for (PinMessageModel pinMes :
                MainActivity.pinPrivateChatMap.get(otherUserUid)) {

            if (pinMes.getMsgId().equals(modelChats.getIdKey())) {
                isPrivatePin = true;
                break;
            }
        }
        for (PinMessageModel pinChatEveryone :
                MainActivity.pinPublicChatMap.get(otherUserUid)) {

            if (pinChatEveryone.getMsgId().equals(modelChats.getIdKey())) {
                isPublicPin = true;
                break;
            }
        }

        if(isPrivatePin && isPublicPin){
            if(holder != null){
                holder.pinALL_IV.setVisibility(View.VISIBLE);
                holder.pinALL_IV.setImageResource(R.drawable.baseline_disabled_visible_view_24);
            }
            MainActivity.pinTV.setText(mContext.getString(R.string.unpin));

            MainActivity.pinMineTV.setText("Unpin for me");
            MainActivity.pinEveryoneTV.setText("Unpin for everyone");

        }else {
            if(isPrivatePin){
                if(holder != null) {
                    holder.pinALL_IV.setVisibility(View.VISIBLE);
                    holder.pinALL_IV.setImageResource(R.drawable.lock);
                }
                MainActivity.pinTV.setText(mContext.getString(R.string.unpin));

                MainActivity.pinMineTV.setText("Unpin for me");
                MainActivity.pinEveryoneTV.setText("Pin for everyone");
            } else if (isPublicPin) {
                if(holder != null){
                    holder.pinALL_IV.setVisibility(View.VISIBLE);
                    holder.pinALL_IV.setImageResource(R.drawable.baseline_public_24);
                }
                MainActivity.pinTV.setText(mContext.getString(R.string.unpin));

                MainActivity.pinEveryoneTV.setText("Unpin for everyone");
                MainActivity.pinMineTV.setText("Pin for me only");
            } else {
                if(holder != null){
                    holder.pinALL_IV.setVisibility(View.GONE);
                }
                MainActivity.pinTV.setText(mContext.getString(R.string.pinChat));

                MainActivity.pinMineTV.setText("Pin for me only");
                MainActivity.pinEveryoneTV.setText("Pin for everyone");
            }
        }
    }

    //  return to previous state of progressLoad Bar when file fails to send
    private void loadBarVisibility(MessageViewHolder holder, MessageModel modelChats){
        holder.loadProgressTV.setVisibility(View.VISIBLE);
        holder.progressBarLoad.setVisibility(View.VISIBLE);
        // save the original quality image to firebase
        Uri uriOnPhone = modelChats.getPhotoUriOriginal().startsWith("/storage/") ? Uri.fromFile(new File(modelChats.getPhotoUriOriginal()))
                : Uri.parse(modelChats.getPhotoUriOriginal());  // change from /storage to file://

        if( isFileLessThan150Kb(uriOnPhone, mContext) && modelChats.getId().equals(myId) &&
            !modelChats.getPhotoUriOriginal().startsWith("/storage/")
                && !modelChats.getPhotoUriOriginal().startsWith("file:/"))
        {
            String imageSize = modelChats.getImageSize();
            holder.loadProgressTV.setText(imageSize);

        } else if(!modelChats.getId().equals(myId)) {
            String imageSize = modelChats.getImageSize();
            holder.loadProgressTV.setText(imageSize);

        }else {
            String imageSize = FileUtils.getEstimatePhotoSize(uriOnPhone,mContext);
            holder.loadProgressTV.setText(imageSize);
        }

        // Set the indeterminate tint color for the ProgressBar to white
        int orangeColor = ContextCompat.getColor(mContext, R.color.orange);
        holder.progressBarLoad.setIndeterminateTintList(ColorStateList.valueOf(orangeColor));

    }

    //  =============== on Long Click methods

    private void activateOnLongClick(MessageModel modelChats, MessageViewHolder holder, int chatPosition){
//        if(modelChats.getMsgStatus() != 700033){
            // send the chats details
            MainActivity.modelChatsOption = modelChats;
            MainActivity.chatPosition = chatPosition;
            MainActivity.chatHolder = holder;

            // clear all list first
            MainActivity.chatModelList.clear();
            chatPositionList.clear();

            // clear previous highlight
            MainActivity.clearAllHighlights();

            // add chat to list
            MainActivity.chatModelList.add(modelChats);
            // add position to list to enable correct item background display
            chatPositionList.add(chatPosition); // highlight item on the list

            MainActivity.chatSelected_TV.setText("1");

            int positionCheck = modelList.size() - chatPosition;    // 1000 - 960 => 40

            MainActivity.onShare = false;
            if(positionCheck < 100){
                MainActivity.editChatOption_IV.setImageResource(R.drawable.baseline_mode_edit_24);
                MainActivity.editChatOption_IV.setVisibility(View.VISIBLE);
            }
            if (modelChats.getType() != 0 || !modelChats.getFromUid().equals(myId) )
            {   // set image and document => type 0 is for just text-chat, type 1 is voice_note, type 2 is photo, type 3 is document, type 4 is audio (mp3)
                MainActivity.editChatOption_IV.setImageResource(R.drawable.baseline_share_24);
                MainActivity.onShare = true;

            }

            // display pin icon, is it private or public
            pinStatusIcon(null, modelChats);

            firstTopUserDetailsContainer.setVisibility(View.GONE);
            // make the menu option visible
            MainActivity.chatOptionsConstraints.setVisibility(View.VISIBLE);

            MainActivity.pinMsgContainer.setVisibility(View.GONE);
            // Close the previously open chat options
            closePreviousChatOption(modelChats, holder);
            // close mine chatTop Constraint
            closeMyOwnOption( modelChats, holder );

            // Highlight the clicked item
            View itemView = recyclerMap.get(otherUserUid).getLayoutManager().findViewByPosition(chatPosition);
            itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.transparent_orangeLow));

            // remove previous highlight is any
            if(chatPosition != lastPosition) clearHighlight();

            // activate the highlight checker
            isOnlongPress = true;

//        } else Toast.makeText(mContext, mContext.getString(R.string.isNetwork), Toast.LENGTH_SHORT).show();

    }

    // if onLongPress mood is activated, add or remove chat from list when user click a chat
    private void addOrRemoveChatFromList(MessageModel modelChats, int chatPosition){
        // Highlight the clicked item
        View itemView = recyclerMap.get(otherUserUid).getLayoutManager().findViewByPosition(chatPosition);
        // Check the current background color
        int currentColor = ((ColorDrawable) itemView.getBackground()).getColor();
        // Define the highlighted color
        int highlightedColor = ContextCompat.getColor(itemView.getContext(), R.color.transparent_orangeLow);

        MainActivity.editChatOption_IV.setImageResource(R.drawable.baseline_mode_edit_24);

        // Add or remove item after checking if the item is not already in the list
        if (!MainActivity.chatModelList.contains(modelChats)) {
            // only add chat to list when it less than 10 chats on the list
            if(MainActivity.chatModelList.size() < 10){

                if(modelChats.getMsgStatus() != 700033){
                    MainActivity.chatModelList.add(modelChats);

                    // make icon invisible
                    if(MainActivity.chatModelList.size() > 1){
                        MainActivity.editChatOption_IV.setVisibility(View.GONE);
                        MainActivity.replyChatOption_IV.setVisibility(View.GONE);
                        MainActivity.moreOption_IV.setVisibility(View.INVISIBLE);
                    }

                    // add position to list to help retain the background color when user scroll
                    chatPositionList.add(chatPosition);

                    // Set the background color to the highlighted color
                    if (currentColor != highlightedColor)
                        itemView.setBackgroundColor(highlightedColor);

                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.isNetwork), Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(mContext, mContext.getString(R.string.subscribe), Toast.LENGTH_SHORT).show();
            }

        } else {
            // remove chat from list if chat already exist
            MainActivity.chatModelList.remove(modelChats);

            if(MainActivity.chatModelList.size() == 0 ){
                // close the chatOption container if list is empty
                MainActivity.cancelChatOption();
            } else if (MainActivity.chatModelList.size() == 1){
                MainActivity.editChatOption_IV.setVisibility(View.VISIBLE);
                MainActivity.replyChatOption_IV.setVisibility(View.VISIBLE);
                MainActivity.moreOption_IV.setVisibility(View.VISIBLE);
                // show if chat is pin publicly or privately
                pinStatusIcon(null, MainActivity.chatModelList.get(0));
            } else {
                MainActivity.editChatOption_IV.setVisibility(View.GONE);
                MainActivity.replyChatOption_IV.setVisibility(View.GONE);
                MainActivity.moreOption_IV.setVisibility(View.INVISIBLE);
            }

            // Remove the background color
            if (currentColor == highlightedColor)
                itemView.setBackgroundColor(Color.TRANSPARENT);

        }

        // toggle edit icon
        for(MessageModel model : MainActivity.chatModelList){
            if ( (model.getType() != 0) || !model.getFromUid().equals(myId))
            {   // set image and document => type 0 is for just text-chat, type 1 is voice_note, type 2 is photo, type 3 is document, type 4 is audio (mp3)
                MainActivity.editChatOption_IV.setImageResource(R.drawable.baseline_share_24);
                MainActivity.onShare = true;
            } else {
                MainActivity.onShare = false;
            }

            MainActivity.modelChatsOption = model;  // assign the last model
        }

        // display the total number of chat selected in the List
        String totalChatSelected = MainActivity.chatModelList.size() + "";
        MainActivity.chatSelected_TV.setText(totalChatSelected);

    }


    //  =============== sending image or document methods
    private void sendMyPhotoOrDocument(MessageModel modelChats, MessageViewHolder holder, int chatPosition){
        // get the photo uid owner and the sending state
        String[] photoCheck = MainActivity.documentIdShareRef.getString(modelChats.getIdKey(), "")
                .split(AllConstants.JOIN);
        if(photoCheck.length > 1){
            String otherId = photoCheck[0];
            String isSending = photoCheck[1];
            if (isSending.equals("no")) {   // it's not on auto sending again
                // deactivate the sending
                if(uploadTaskMap.get(modelChats.getIdKey()) != null) {
                    uploadTaskMap.get(modelChats.getIdKey()).cancel(); // cancel the loading process to firebase
                    uploadTaskMap.remove(modelChats.getIdKey());
                    Toast.makeText(mContext, mContext.getString(R.string.dCancel), Toast.LENGTH_SHORT).show();
                    notifyItemChanged(chatPosition, new Object());
                } else {    // sendPhoto or Document in case map is empty and on "hold"
                    loadMyPhotoOrDocument(modelChats, holder, chatPosition, otherId);
                }
            }

        } else Toast.makeText(mContext, mContext.getString(R.string.resend), Toast.LENGTH_SHORT).show();

    }

    private void loadMyPhotoOrDocument(MessageModel modelChats, MessageViewHolder holder, int chatPosition, String otherUid)
    {
        // Close the previously open chat options
        closePreviousChatOption(modelChats, holder);

        // create the path where it should save to firebase storage
        String photo_location_original_DB = "media/photos/" + user.getUid() + "/" + System.currentTimeMillis();
        if(modelChats.getType() == 3) {
            photo_location_original_DB = "media/documents/" + user.getUid() + "/" + System.currentTimeMillis();
        } else if (modelChats.getType() == 4) {
            photo_location_original_DB = "media/audio/" + user.getUid() + "/" + System.currentTimeMillis();
        }

        //  create the path - storage preference
        StorageReference originalPhotoRef = FirebaseStorage.getInstance()
                .getReference(photo_location_original_DB);

        // save the original quality image to firebase
        Uri uriOnPhone = modelChats.getPhotoUriOriginal().startsWith("/storage/") ? Uri.fromFile(new File(modelChats.getPhotoUriOriginal()))
                : Uri.parse(modelChats.getPhotoUriOriginal());  // change from /storage to file://

        // get the path the original final was saved in firebase to enable other user delete the file from fireStore after downloading it
        String finalPhoto_location_original_DB = photo_location_original_DB;

        if(modelChats.getType() == 5) {     //  it is video
            System.out.println("what is path " + modelChats.getPhotoUriOriginal());

            if( (!modelChats.getPhotoUriOriginal().startsWith("file:/") && !modelChats.getPhotoUriOriginal().startsWith("/storage"))
                    && modelChats.getEmojiOnly().equals("record")) {    // user is sending a recording video
                // compress video and send
                compressVideo(uriOnPhone, mContext, compressedVideoPath -> {
                    // Create a Uri object from the compressed video file path
                    Uri compressedUri = Uri.fromFile(new File(compressedVideoPath));

                    modelChats.setPhotoUriOriginal(compressedUri.toString());
                    // update inside chat ROOM DB about the new compress video uri
                    chatViewModel.updatePhotoUriPath(modelChats.getIdKey(), otherUid,
                            modelChats.getPhotoUriPath(), modelChats.getPhotoUriOriginal(), null, 700024);

                    UploadTask uploadTaskFile_ = originalPhotoRef.putFile(compressedUri);  // send video to firebase storage

                    // save to map for each photo key
                    uploadTaskMap.put(modelChats.getIdKey(), uploadTaskFile_);

                    // track the original photo or doc upload progress
                    uploadTaskFile_.addOnSuccessListener(taskSnapshot -> {

                        updateUI_OnSuccess(chatPosition, modelChats, holder);

                        Bitmap bitmapThumbnail = createVideoThumbnail(mContext, uriOnPhone);
                        Uri getThumbnailUri = FileUtils.getThumbnailUri(bitmapThumbnail, mContext);
                        // Upload the thumbnail to Firebase Storage
                        uploadThumbnailToFirebase(getThumbnailUri, modelChats, chatPosition, holder,
                                finalPhoto_location_original_DB, otherUid);

                    }).addOnFailureListener(exception -> {
                        // Handle unsuccessful uploads
                        onSendFailed(holder, modelChats, otherUid);
                    }).addOnProgressListener(taskSnapshot -> {
                        // Calculate progress percentage
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        trackLoadProgress(progress, holder, modelChats);
                    });

                });

            } else {    // user is forwarding chats, or sending from phone gallery (imbibe a solution later to compress video from gallery)
                UploadTask uploadTaskFile_ = originalPhotoRef.putFile(uriOnPhone);  // send photo

                // save to map for each photo key
                uploadTaskMap.put(modelChats.getIdKey(), uploadTaskFile_);

                // track the original photo or doc upload progress
                uploadTaskFile_.addOnSuccessListener(taskSnapshot -> {

                    updateUI_OnSuccess(chatPosition, modelChats, holder);

                    if (modelChats.getPhotoUriOriginal().startsWith("file:/")
                            || modelChats.getPhotoUriOriginal().startsWith("/storage")){ // already in app memory
                        uploadThumbnailToFirebase(Uri.parse(modelChats.getPhotoUriPath()), modelChats,
                                chatPosition, holder, finalPhoto_location_original_DB, otherUid);

                    } else {    // new video from gallery
                        String savePath = saveFileFromContentUriToAppStorage(uriOnPhone, mContext);

                        modelChats.setPhotoUriOriginal(savePath);
                        // update inside chat ROOM DB about the new compress video uri
                        chatViewModel.updatePhotoUriPath(modelChats.getIdKey(), otherUid,
                                modelChats.getPhotoUriPath(), savePath, null, 700024);

                        Bitmap bitmapThumbnail = createVideoThumbnail(mContext, uriOnPhone);
                        Uri getThumbnailUri = FileUtils.getThumbnailUri(bitmapThumbnail, mContext);
                        // Upload the thumbnail to Firebase Storage and send chat to other user
                        uploadThumbnailToFirebase(getThumbnailUri, modelChats, chatPosition, holder,
                                finalPhoto_location_original_DB, otherUid);
                    }


                }).addOnFailureListener(exception -> {
                    // Handle unsuccessful uploads
                    onSendFailed(holder, modelChats, otherUid);
                }).addOnProgressListener(taskSnapshot -> {
                    // Calculate progress percentage
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    trackLoadProgress(progress, holder, modelChats);
                });
            }

        } else if (modelChats.getType() == 2) { // it is photo

            if(isFileLessThan150Kb(uriOnPhone, mContext)    //
                    || modelChats.getPhotoUriOriginal().startsWith("file:/")
                    || modelChats.getPhotoUriOriginal().startsWith("/storage")) {

//                System.out.println("what is lin: " + modelChats.getPhotoUriOriginal());
                UploadTask uploadTaskFile_ = originalPhotoRef.putFile(uriOnPhone);  // send photo

                // save to map for each photo key
                uploadTaskMap.put(modelChats.getIdKey(), uploadTaskFile_);

                // track the original photo or doc upload progress
                uploadTaskFile_.addOnSuccessListener(taskSnapshot -> {  // user is forwarding photo. So continue using the file path

                    updateUI_OnSuccess(chatPosition, modelChats, holder);

                    if( modelChats.getPhotoUriOriginal().startsWith("file:/")   // continue to use the app storage file
                            || modelChats.getPhotoUriOriginal().startsWith("/storage"))
                    {
                        uploadThumbnailToFirebase(Uri.parse(modelChats.getPhotoUriPath()), modelChats,
                                chatPosition, holder, finalPhoto_location_original_DB, otherUid);

                    } else
                    {   // just generate thumbnail and sent
                        Glide.with(mContext)
                                .asBitmap()
                                .load(uriOnPhone)
                                .into(new SimpleTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                        // Extract a thumbnail
                                        Bitmap thumbnail = ThumbnailUtils.extractThumbnail(resource, 400, 400);

                                        // Get a URI for the thumbnail
                                        Uri thumbnailUri = FileUtils.getThumbnailUri(thumbnail, mContext);

                                        // Upload the thumbnail to Firebase Storage
                                        uploadThumbnailToFirebase(thumbnailUri, modelChats, chatPosition,
                                                holder, finalPhoto_location_original_DB, otherUid);
                                    }

                                });
                    }


                }).addOnFailureListener(exception -> {
                    // Handle unsuccessful uploads
                    onSendFailed(holder, modelChats, otherUid);
                }).addOnProgressListener(taskSnapshot -> {
                    // Calculate progress percentage
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    trackLoadProgress(progress, holder, modelChats);
                });

            } else
            {    // compress file
                // I use Glide, so as to correction photo angle degree in case it's from irregular camera
                Glide.with(mContext)
                        .asBitmap()
                        .load(uriOnPhone)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                                // Compress the Bitmap to reduce its size (adjust the quality as needed)
                                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                resource.compress(Bitmap.CompressFormat.JPEG, 50, outputStream); // 50 is the compression quality (0-100)
                                byte[] compressedBytes = outputStream.toByteArray();

                                UploadTask uploadTaskFile_ = originalPhotoRef.putBytes(compressedBytes);  // send photo to fireStore

                                // save to map for each photo key
                                uploadTaskMap.put(modelChats.getIdKey(), uploadTaskFile_);

                                // track the original photo or doc upload progress
                                uploadTaskFile_.addOnSuccessListener(taskSnapshot -> {
                                    //  get the size of the new compressed image
                                    int fileSizeKB = compressedBytes.length / 1024; // Size in kilobytes
                                    int fileSizeMB = fileSizeKB / 1024; // Size in megabytes
                                    String sizeString = fileSizeKB < 1000.0 ? Math.round(fileSizeKB) + " kB" : Math.round(fileSizeMB) + " MB";

                                    // update the local chat list
                                    modelChats.setImageSize(sizeString);    // set the image size
                                    updateUI_OnSuccess(chatPosition, modelChats, holder);
                                    replaceSnapPhotoWithCompressFile(modelChats, compressedBytes);

                                    // generate thumbnail and sent
                                    Glide.with(mContext)
                                            .asBitmap()
                                            .load(uriOnPhone)
                                            .into(new SimpleTarget<Bitmap>() {
                                                @Override
                                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                    // Extract a thumbnail
                                                    Bitmap thumbnail = ThumbnailUtils.extractThumbnail(resource, 400, 400);

                                                    // Get a URI for the thumbnail
                                                    Uri thumbnailUri = FileUtils.getThumbnailUri(thumbnail, mContext);

                                                    // Upload the thumbnail to Firebase Storage
                                                    uploadThumbnailToFirebase(thumbnailUri, modelChats, chatPosition, holder, finalPhoto_location_original_DB, otherUid);
                                                }

                                            });

                                }).addOnFailureListener(exception -> {
                                    // Handle unsuccessful uploads
                                    onSendFailed(holder, modelChats, otherUid);
                                }).addOnProgressListener(taskSnapshot -> {
                                    // Calculate progress percentage
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                    trackLoadProgress(progress, holder, modelChats);
                                });

                            }

                        });

            }

        } else if (modelChats.getType() == 3) { // it document

            UploadTask uploadTaskFile_ = originalPhotoRef.putFile(uriOnPhone);  // send file as it is. DOnt reduce size nor compress

            // save to map for each photo key
            uploadTaskMap.put(modelChats.getIdKey(), uploadTaskFile_);

            // track the original photo or doc upload progress
            uploadTaskFile_.addOnSuccessListener(taskSnapshot -> {

                updateUI_OnSuccess(chatPosition, modelChats, holder);

                // sending docx, cdr, psd, apk etc (files that doesn't have thumbnail)
                if(modelChats.getPhotoUriOriginal().startsWith("content://com.android")){   // save the file to enable default app to open file
                    String newUri = saveFileFromContentUriToAppStorage(Uri.parse(modelChats.getPhotoUriOriginal()), mContext);
                    modelChats.setPhotoUriOriginal(newUri);
                }
                onSendingSuccessful(modelChats, chatPosition, holder, finalPhoto_location_original_DB,
                        null, otherUid, null);

            }).addOnFailureListener(exception -> {
                // Handle unsuccessful uploads
                onSendFailed(holder, modelChats, otherUid);
            }).addOnProgressListener(taskSnapshot -> {
                // Calculate progress percentage
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                trackLoadProgress(progress, holder, modelChats);
            });
        }

    }

    private void updateUI_OnSuccess(int chatPosition, MessageModel modelChats, MessageViewHolder holder){
        modelChats.setMsgStatus(700024);
        // hide the progressBar
        holder.progressBarLoad.setVisibility(View.GONE);
        notifyItemChanged(chatPosition, new Object());
    }

    private void onSendFailed(MessageViewHolder holder, MessageModel modelChats, String otherUid){
        Toast.makeText(mContext, mContext.getString(R.string.corrupt), Toast.LENGTH_SHORT).show();
        loadBarVisibility(holder, modelChats);
        MainActivity.documentIdShareRef.edit()
                .putString(modelChats.getIdKey(), otherUid + AllConstants.JOIN + "yes").apply();
        // delete the path from the app memory
        MainActivity.deleteSingleUriFromAppMemory(modelChats.getPhotoUriOriginal());
//        System.out.println("what is error " + exception.getMessage());
    }

    private void uploadThumbnailToFirebase(Uri thumbnailUri, MessageModel modelChats, int chatPosition, MessageViewHolder holder,
                                           String finalPhoto_location_original_DB, String otherUid) {
        // update the UI first
        modelChats.setMsgStatus(700024);
        holder.progressBarLoad.setVisibility(View.GONE);    // hide the progressBar
        notifyItemChanged(chatPosition, new Object());

        //  create the path for the low image - storage preference
        String photo_location_low_DB = "media/thumbnail/" + user.getUid() + "/" + System.currentTimeMillis();

        // link the path to the firebase storage instance
        StorageReference LowPhotoRef = FirebaseStorage.getInstance()
                .getReference(photo_location_low_DB);

        // upload the low quality image to firebase
        UploadTask uploadTaskLow = LowPhotoRef.putFile(thumbnailUri);   // send thumbnail to firebase

        // track upload progress for low quality image or doc thumbnail
        uploadTaskLow.addOnSuccessListener(taskSnapshot1 -> {
            // get the low quality image uri path link and send to other user
            LowPhotoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // send the low image uri link to enable other user picasso to auto download it quickly on first arrival
                String imageLinkToFBStorage = uri.toString();

                if(modelChats.getType() == 3 && modelChats.getPhotoUriOriginal()    // 2 is photo, 3 is doc, 4 is audio, 5 is video
                        .startsWith("content://com.android"))
                {   // save the file to enable default app to open file
                    String newUri = saveFileFromContentUriToAppStorage(Uri.parse(modelChats.getPhotoUriOriginal()), mContext);
                    modelChats.setPhotoUriOriginal(newUri);
                }

                modelChats.setPhotoUriPath(thumbnailUri.toString());

                onSendingSuccessful(modelChats, chatPosition, holder, finalPhoto_location_original_DB,
                        photo_location_low_DB, otherUid, imageLinkToFBStorage);

            }).addOnFailureListener(e -> {
                loadBarVisibility(holder, modelChats);
                Toast.makeText(mContext, "Upload failed_", Toast.LENGTH_SHORT).show();
                MainActivity.documentIdShareRef.edit()
                        .putString(modelChats.getIdKey(), otherUid + AllConstants.JOIN + "yes").apply();
            });

        }).addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
            loadBarVisibility(holder, modelChats);
            Toast.makeText(mContext, "Upload failed__", Toast.LENGTH_SHORT).show();
            MainActivity.documentIdShareRef.edit()
                    .putString(modelChats.getIdKey(), otherUid + AllConstants.JOIN + "yes").apply();
            // delete the path from the app memory
            MainActivity.deleteSingleUriFromAppMemory(modelChats.getPhotoUriOriginal());
        });

    }

    private void downloadPhotoOrDocSentByOtherUser(MessageModel modelChats, MessageViewHolder holder, int chatPosition)
    {
        String pathToFirebase = modelChats.getPhotoUriOriginal();   //media/documents/AZkfy6uZunMfMUxR7HIol4rPZBq2/1708199920604winnerChatJoinPathsnull
        if(pathToFirebase.contains(AllConstants.JOIN)){ // split in case the null was not removed
            String splitPath[] = pathToFirebase.split(AllConstants.JOIN);
            pathToFirebase = splitPath[0];
        }
        // get the path of the original image on the firebase storage
        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference(pathToFirebase);

        // create the path where you want to download the image on phone storage
        File saveImageToPhoneUri = new File(FolderUtils.getPhotoFolder(mContext), mContext.getString(R.string.app_name) + System.currentTimeMillis() + ".jpg");
        String uuid = String.valueOf(System.currentTimeMillis()).substring(8);

        if(modelChats.getType() == 3){  // it is document
            String[] splitDetails = modelChats.getEmojiOnly().split("\n");
            String fileName = splitDetails[0];

            saveImageToPhoneUri = new File(FolderUtils.getDocumentFolder(mContext), mContext.getString(R.string.app_name) + "_" + uuid + fileName );

        } else if (modelChats.getType() == 5) {
            saveImageToPhoneUri = new File(FolderUtils.getVideoFolder(mContext), mContext.getString(R.string.app_name) + "_" + uuid + "_.mp4" );
        }

        Uri savePath = Uri.fromFile(saveImageToPhoneUri);   //   File savePath = saveImageToPhoneUri;

        // download the image from database into the part
        FileDownloadTask downloadTask = storageRef.getFile(savePath);
        // save the downloadTask to track and update the progress listener while scrolling
        fileDownloadTaskMap.put(modelChats.getIdKey(), downloadTask);

        // get the original path from modelChats.PhotoUriOriginal -- for deleting on firebase storage
        String originalImageUriToFirebase = pathToFirebase;

        downloadTask.addOnSuccessListener(taskSnapshot -> {

            // update the UI
            holder.progressBarLoad.setVisibility(View.GONE);
            // update chatList
            modelChats.setPhotoUriOriginal(savePath.toString());
//            modelChats.setImageSize(null);
            notifyItemChanged(chatPosition);

            // delete from Firebase Storage
            deletePathRef.child(originalImageUriToFirebase).delete();

            // update ROOM
            chatViewModel.updatePhotoUriPath(modelChats.getIdKey(),
                    modelChats.getFromUid(), modelChats.getPhotoUriPath(), savePath.toString(),
                    modelChats.getImageSize(), 0);

            fileDownloadTaskMap.remove( modelChats.getIdKey()); // remove from when done.

        }).addOnFailureListener(exception -> {
            // Handle failed download
            int orangeColor = ContextCompat.getColor(mContext, R.color.orange);
            // Set the indeterminate tint color for the ProgressBar to white
            holder.progressBarLoad.setIndeterminateTintList(ColorStateList.valueOf(orangeColor));
            holder.loadProgressTV.setText(modelChats.getImageSize());
            Toast.makeText(mContext, "Photo not found. Ask user to resend!", Toast.LENGTH_SHORT).show();
        }).addOnProgressListener(snapshot -> {
            // Calculate progress percentage
            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
            trackLoadProgress(progress, holder, modelChats);
        });

    }

    private void openPhoto(MessageModel modelChats, MessageViewHolder holder){
        holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.transparent_orange));

        holder.itemView.animate().scaleX(1.2f).scaleY(1.2f).setDuration(10).withEndAction(() ->
        {
            // Create a list to add the photo
            List<MessageModel> photoModel = new ArrayList<>();
            // loop through the list to select which one contain the download image
            for (int i = 0; i < modelList.size(); i++) {
                MessageModel model = modelList.get(i);
                // check if the model photo is not null first
                if(model.getPhotoUriPath() != null){
                    if(!model.getFromUid().equals(myId) // check if I have downloaded other user photo
                            && !model.getPhotoUriOriginal().startsWith("media/photos"))
                    {
                        // add other user images if I have downloaded it
                        photoModel.add(model);
                    } else if (model.getFromUid().equals(myId)) {
                        // add my image -- from my from storage
                        photoModel.add(model);
                    }
                }
                // Wait until it loop through the last model before opening the ViewImage
                if (i == (modelList.size() - 1) && !photoModel.isEmpty()) {
                    Intent intent = new Intent(mContext, ViewImageActivity.class);
                    // Put the modelList as an extra
                    intent.putExtra("modelList", new ArrayList<>(photoModel));
                    intent.putExtra("photoId", modelChats.getIdKey());

                    // Start the ViewPagerActivity
                    mContext.startActivity(intent);


                    // clear highlight
                    new Handler().postDelayed(()-> {
                        clearAllHighlights();
                        // Reset the scale
                        holder.itemView.setScaleX(1.0f);
                        holder.itemView.setScaleY(1.0f);
                    }, 100);
                }
            }

        });

    }


    // save small image (thumbnail) to phone on photo first appearance from other other user
    public static File saveThumbnailFromOtherUser(Bitmap bitmap, int quality, Context mContext){

        String fileName = mContext.getString(R.string.app_name) + "_" + System.currentTimeMillis() + ".jpg";
        // create the path where you want to save the image on phone storage
        File saveImageToPhoneUri = new File(getThumbnailFolder(mContext), fileName);

        // download the blur thump image to phone
        OutputStream outputStream = null;
        try {
            // activate the path to the phone storage
            outputStream = new FileOutputStream(saveImageToPhoneUri);
            // save the image to the phone
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);

            // to display the photo on Gallery

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//            MediaStore.Images.Media.insertImage(mContext.getContentResolver(), saveImageToPhoneUri.getAbsolutePath(), fileName, null);

        return saveImageToPhoneUri;
    }

    private void onSendingSuccessful(MessageModel modelChats, int chatPosition, MessageViewHolder holder,
                                     String finalPhoto_location_original_DB, String lowPhotoOrDocLocationOn_DB,
                                     String otherUid, String imageLinkToFBStorage)
    {
        // get the image size and send to user before it turns null for me
        String getImageSize = modelChats.getImageSize();

        Map imageMap = sendMap(modelChats, finalPhoto_location_original_DB,
                lowPhotoOrDocLocationOn_DB, getImageSize, imageLinkToFBStorage, null);

        // send image to database and update ROOM DB
        sendToDatabaseAndRoom(modelChats, imageMap, otherUid);  // send my photo

        // delete key from Share preference, chat is already successfully sent
        MainActivity.documentIdShareRef.edit().remove(modelChats.getIdKey()).apply();
        uploadTaskMap.remove(modelChats.getIdKey());    // remove the upLoad monitor from map

    }

    // just override the file path with the correct photo
    private void correctPhotoAngleResolution(File getInitialFilepath, MessageModel modelChats){
        Glide.with(mContext)
                .asBitmap()
                .load(modelChats.getPhotoUriOriginal())
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                        OutputStream outputStream;
                        try {
                            outputStream = new FileOutputStream(getInitialFilepath);
                            // save the image to the phone
                            resource.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }

                });
    }
    private void displayLowImageOrDocumentThumbnail(Uri imageUri_, MessageViewHolder holder, MessageModel modelChats){
        if(modelChats.getType() == 2){
            holder.documentContainer.setVisibility(View.GONE);
            holder.showImage.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(imageUri_).into(holder.showImage);

        } else if (modelChats.getType() == 3) {
            holder.showImage.setVisibility(View.GONE);
            holder.documentContainer.setVisibility(View.VISIBLE);   // make the document container visible
            holder.documentDetails_TV.setText(modelChats.getEmojiOnly());   // I used document details to replace emojiOnly
            holder.document_IV.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(imageUri_)
//                    .apply(RequestOptions.centerCropTransform())
                    .into(holder.document_IV);  // display the document thumbnail

        } else if (modelChats.getType() == 5) {    // it is video
            holder.documentContainer.setVisibility(View.GONE);
            holder.showImage.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(imageUri_).into(holder.showImage);
            // show the playIcon button
        }

    }

    private void downloadLowImageFrom_FB_Storage( MessageModel modelChats, Uri imageUri_){
        if(!modelChats.getFromUid().equals(myId)){
            // check if the low quality photo uri path is internet link
            if(modelChats.getPhotoUriPath().toLowerCase().startsWith("http://")
                    || modelChats.getPhotoUriPath().toLowerCase().startsWith("https://"))
            {
                try {
                    // split the original and low image uri location on firebase storage
                    String[] splitPath = modelChats.getPhotoUriOriginal().split(AllConstants.JOIN);
                    String originalUri = splitPath[0];
                    String lowUri = splitPath[1];  //  for deleting from firebase storage

                    String phoneImagePath = downloadThumbnailFile(modelChats.getPhotoUriPath(), mContext);

                    // update room and chat list from uri to phone storage path
                    modelChats.setPhotoUriPath(phoneImagePath);
                    modelChats.setPhotoUriOriginal(originalUri);
                    chatViewModel.updatePhotoUriPath(modelChats.getIdKey(),
                            otherUserUid, phoneImagePath, originalUri,
                            modelChats.getImageSize(), 0);

                    // delete the blur image from firebase storage database -- ignore the originalUri till user download it
                    deletePathRef.child(lowUri).delete();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

//                Glide.with(mContext)
//                        .asBitmap()
//                        .load(imageUri_)
//                        .into(new SimpleTarget<Bitmap>() {
//                            @Override
//                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//
//                                // split the original and low image uri location on firebase storage
//                                String[] splitPath = modelChats.getPhotoUriOriginal().split(AllConstants.JOIN);
//                                String originalUri = splitPath[0];
//                                String lowUri = splitPath[1];  //  for deleting from firebase storage
//
//                                // save to phone storage if it's internet uri
//                                File downloadImageToPhone = saveThumbnailFromOtherUser(resource, 100, mContext);
//                                String phoneImagePath = Uri.fromFile( downloadImageToPhone ).toString();
//
//                                // update room and chat list from uri to phone storage path
//                                modelChats.setPhotoUriPath(phoneImagePath);
//                                modelChats.setPhotoUriOriginal(originalUri);
//                                chatViewModel.updatePhotoUriPath(modelChats.getIdKey(),
//                                        otherUserUid, phoneImagePath, originalUri,
//                                        modelChats.getImageSize(), 0);
//
//                                // delete the blur image from firebase storage database -- ignore the originalUri till user download it
//                                deletePathRef.child(lowUri).delete();
//
//                            }
//                        });

            }
        }
    }

    //  =============== voice note methods

    private void playOrPauseVoiceNote(MessageViewHolder holder, MessageModel modelChats){
        MediaPlayer mediaPlayer = mediaPlayerMap.get(modelChats.getIdKey());
        if(mediaPlayer.isPlaying()){
            // pause voice note if playing
            mediaPlayer.pause();
            holder.pauseAndPlay_IV.setImageResource(R.drawable.baseline_play_arrow_24);
            // save the current seekBar position (duration)
            mapLastDuration.put(modelChats.getIdKey(), mediaPlayer.getCurrentPosition());
            handler1.removeCallbacks(runnable);
            // change back to the total time duration
            String timeLabel = modelChats.getVnDuration().contains("Audio") ? formatDuration(mediaPlayer.getDuration()) +  " ~ Audio"
                    : formatDuration(mediaPlayer.getDuration());
            holder.totalMinute_TV.setText(timeLabel);
            // stop and recreate media player instance
            mediaPlayer.release();
            mediaPlayerMap.put(lastIdKeyChat, new MediaPlayer());
            mediaPlayerMap.put(modelChats.getIdKey(), new MediaPlayer());
//            Toast.makeText(mContext, "Done here", Toast.LENGTH_SHORT).show();
        } else {

            holder.pauseAndPlay_IV.setImageResource(R.drawable.baseline_pause_24);
            try {
                // sent the path to the media player to source for the voice note
                if(modelChats.getVoiceNote().equals("content://media")) {
                    Uri uri = Uri.parse(modelChats.getVoiceNote());
                    mediaPlayer.setDataSource(mContext, uri);
                } else {
                    mediaPlayer.setDataSource(modelChats.getVoiceNote()); // path is file:/ or /storage  -> save to my app memory
                }
                mediaPlayer.prepare();

                // check if vn has been played before and get the last saved seekProgress position
                if(mapLastDuration.get(modelChats.getIdKey()) != null){
                    int lastPosition = mapLastDuration.get(modelChats.getIdKey());
                    mediaPlayer.seekTo(lastPosition);
                }

                mediaPlayer.start();

                // call runnable to keep the seekBar progress intact
                runnableSeekBarProgress(holder, modelChats, mediaPlayer);   // play/pause method

            } catch (Exception e) {
                holder.pauseAndPlay_IV.setImageResource(R.drawable.baseline_play_arrow_24);
                mediaPlayer.release(); // Release the MediaPlayer in case of an exception
                mediaPlayerMap.put(modelChats.getIdKey(), new MediaPlayer());
                Toast.makeText(mContext, mContext.getString(R.string.notFound), Toast.LENGTH_SHORT).show();
                System.out.println("what is get error " + e.getMessage());

            }
        }
        // Reset the MediaPlayer when playback is complete
        voiceNoteHasFinishedPlaying(holder, modelChats);
    }

    // keep updating the seekBar as the voice note plays
    private void runnableSeekBarProgress( MessageViewHolder holder, MessageModel modelChats, MediaPlayer mediaPlayer1){
        runnable = () -> {
            if (mediaPlayer1 != null) {

                int currentPosition = mediaPlayer1.getCurrentPosition();
                int maxDuration = mediaPlayer1.getDuration();
                holder.seekBarProgress.setMax(maxDuration);
                holder.seekBarProgress.setProgress(currentPosition);

                // set the time progress as it's playing
                String timeLabel = modelChats.getVnDuration().contains("Audio") ? formatDuration(currentPosition) + " ~ Audio"
                        : formatDuration(currentPosition);
                holder.totalMinute_TV.setText(timeLabel);

                // save currentPosition as the lastPosition
                mapLastDuration.put(modelChats.getIdKey(), currentPosition);
                handler1.postDelayed(runnable, 100);
            }
        };

        handler1.post(runnable);

    }

    // release the voice note media player when finished playing
    private void voiceNoteHasFinishedPlaying(MessageViewHolder holder, MessageModel modelChats){
        MediaPlayer mediaPlayer = mediaPlayerMap.get(modelChats.getIdKey());
        mediaPlayer.setOnCompletionListener(mp -> {
            holder.pauseAndPlay_IV.setImageResource(R.drawable.baseline_play_arrow_24);
            lastHolder_.pauseAndPlay_IV.setImageResource(R.drawable.baseline_play_arrow_24);
            handler1.removeCallbacks(runnable);
            // reset the lastDuration to 0
            mapLastDuration.put(modelChats.getIdKey(), 0);
            // reset media player instance
            mediaPlayerMap.get(modelChats.getIdKey()).release();
            mediaPlayerMap.put(modelChats.getIdKey(), new MediaPlayer());
        });
    }

    // listen to the new selected seekbar position and continue playing the vn from there.
    private void seekBarListener(MessageModel modelChats, MessageViewHolder holder){
        holder.seekBarProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    // Seek to the specified position when the user drags the seek bar
                    try {
                        MediaPlayer mediaPlayer = mediaPlayerMap.get(modelChats.getIdKey());
                        // it it current id
                        if(lastIdKeyChat.equals(modelChats.getIdKey())){
                            if(mediaPlayer.isPlaying()) {
                                // continue playing from the new progress selected
                                mediaPlayer.seekTo(progress);
                            } else {
                                // save the new duration
                                mapLastDuration.put(modelChats.getIdKey(), progress);
                            }

                            // set the time progress as it's playing
                            String timeLabel = modelChats.getVnDuration().contains("Audio") ? formatDuration(progress) + " ~ Audio"
                                    : formatDuration(progress);
                            holder.totalMinute_TV.setText(timeLabel);

                        } else{ // lastId is not the current id

                            // pause last id voice note if playing
                            if(mediaPlayerMap.get(lastIdKeyChat) != null ){
                                if(mediaPlayerMap.get(lastIdKeyChat).isPlaying()){
                                    lastHolder_.pauseAndPlay_IV.setImageResource(R.drawable.baseline_play_arrow_24);
                                    // save the duration
                                    mapLastDuration.put(lastIdKeyChat, mediaPlayerMap.get(lastIdKeyChat).getCurrentPosition());
                                    handler1.removeCallbacks(runnable);
                                    // release and create new instance
                                    mediaPlayerMap.get(lastIdKeyChat).release();
                                    mediaPlayerMap.put(lastIdKeyChat, new MediaPlayer());
                                }
                            }

                            // save the new duration
                            mapLastDuration.put(modelChats.getIdKey(), progress);

                            // set the time progress to the new selected progress textView
                            String timeLabel = modelChats.getVnDuration().contains("Audio") ? formatDuration(progress) + " ~ Audio"
                                    : formatDuration(progress);
                            holder.totalMinute_TV.setText(timeLabel);

                            lastHolder_ = holder;
                            lastIdKeyChat = modelChats.getIdKey();

                        }

                    } catch (Exception e) {
                        Toast.makeText(mContext, "Error occur voice note", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

    }

    private void sendMyVoiceNote(MessageModel modelChats, MessageViewHolder holder,
                                 String otherUid, int chatPosition)
    {
        holder.circleDownload.setVisibility(View.INVISIBLE);
        holder.progressBar.setVisibility(View.VISIBLE);     // change later
        holder.progressBar.incrementProgressBy(5);     // change later

        // auto runnable progress
        autoRunnableProgress(modelChats, holder); // on click

        // send voice note
        String VN_OR_AUDIO_PATH_TO_FB_STORAGE = "media/voice_note/" + user.getUid() +  "/" + System.currentTimeMillis();
        if(modelChats.getVnDuration().contains("Audio")){
            VN_OR_AUDIO_PATH_TO_FB_STORAGE = "media/audio/" + user.getUid() +  "/" + System.currentTimeMillis();
        }

        final String VN_PATH_TO_FB_STORAGE = VN_OR_AUDIO_PATH_TO_FB_STORAGE; // get the final path to firebase
        //  create the path - storage preference
        StorageReference voiceNoteRef = FirebaseStorage.getInstance()
                .getReference(VN_PATH_TO_FB_STORAGE);

        //  get the uri file from phone path
        Uri audioFile = Uri.fromFile(new File(modelChats.getVoiceNote()));
        // /storage/emulated/0/Android/data/com.pixel.chatapp/files/Media/Audio/Topper_Pheelz-JELO-feat-Young-Jonn.mp3
        if(modelChats.getVoiceNote().equals("content://media")) audioFile = Uri.parse(modelChats.getVoiceNote());

        // save to firebase storage
        UploadTask uploadVoiceNote = voiceNoteRef.putFile(audioFile);
        // save in map, the uploadTask to each chatId
        uploadTaskMap.put(modelChats.getIdKey(), uploadVoiceNote);
        // track the success of the upload
        uploadVoiceNote.addOnSuccessListener(taskSnapshot -> {
            // prepare the map
            Map imageMap = sendMap(modelChats, null, null,
                    null, null, VN_PATH_TO_FB_STORAGE);

            // send voice note to database and update ROOM DB
            sendToDatabaseAndRoom(modelChats, imageMap, otherUid);  // audio or voice note

            // update the delivery status
            modelChats.setMsgStatus(700024);
//            holder.circleDownload.setImageResource(R.drawable.logo);
            holder.progressBar.setVisibility(View.GONE);
            notifyItemChanged(chatPosition, new Object());
            // delete id from share preference
            MainActivity.voiceNoteIdShareRef.edit().remove(modelChats.getIdKey()).apply();
            uploadTaskMap.remove(modelChats.getIdKey());    // remove the upLoad monitor from map
            holder.progressBar.setVisibility(View.GONE);

        }).addOnFailureListener(e -> {
            holder.circleDownload.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.GONE);
            Toast.makeText(mContext, mContext.getString(R.string.notFound), Toast.LENGTH_SHORT).show();
            uploadTaskMap.remove(modelChats.getIdKey());    // remove the upLoad monitor from map
            progressMap.remove(modelChats.getIdKey());
            notifyItemChanged(chatPosition, new Object());
            System.out.println("what is voice note fail " + e.getMessage());
// An unknown error occurred, please check the HTTP result code and inner exception for server response.
        }).addOnProgressListener(taskSnapshot -> {
            // Calculate progress percentage
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            trackLoadProgress(progress, holder, modelChats);    // voice note
        });

        // set as "no" to disable auto loading when scrolled to position
        MainActivity.voiceNoteIdShareRef.edit().putString(modelChats.getIdKey(),
                otherUid + AllConstants.JOIN + "no").apply();
    }

    private void downloadOtherUserVoiceNote(MessageModel modelChats, MessageViewHolder holder, int chatPosition)
    {
        holder.progressBar.setVisibility(View.VISIBLE);
        // load the local runnable progress. This helps when the byte is small because fileTask will stay 0.00 to 100% at once
        autoRunnableProgress(modelChats, holder); // settings

        //  create the path - storage preference
        StorageReference voiceNoteRef = FirebaseStorage.getInstance()   // modelChats.getVoiceNote = media/audio/Edcjn9AaPGNwDGjptCDqZIhwNOk1/1708686076728
                .getReference(modelChats.getVoiceNote());

        // create the path to download the voice note to
        File voiceNoteOrAudioPath;
        String uuid = String.valueOf(System.currentTimeMillis()).substring(8);
        if ( modelChats.getVoiceNote().contains("audio") ) {
            voiceNoteOrAudioPath = new File(getAudioFolder(mContext),
                    mContext.getString(R.string.app_name) + "_" + uuid + modelChats.getEmojiOnly() );
        } else {
            voiceNoteOrAudioPath = new File(getVoiceNoteFolder(mContext),
                    "voice_note_" + mContext.getString(R.string.app_name) + "_" + uuid + ".opus");
        }

        FileDownloadTask fileDownloadTask = voiceNoteRef.getFile(voiceNoteOrAudioPath);  // download the file to the path

        // save to map to track the progress in case user scroll back to the position
        fileDownloadTaskMap.put(modelChats.getIdKey(), fileDownloadTask);

        fileDownloadTask.addOnSuccessListener(taskSnapshot ->
        {
            // get the voice note path from modelChats.getVoiceNote -- for deleting on firebase storage
            String voiceNoteUriToFirebase = modelChats.getVoiceNote();

            // update the UI
            holder.circleDownload.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.GONE);
            holder.pauseAndPlay_IV.setVisibility(View.VISIBLE);
            // update chatList
            modelChats.setVoiceNote(voiceNoteOrAudioPath.getPath());
            String voiceNoteDur = modelChats.getVnDuration();
            // if vn dur contain "~" it means it is more than 1 mb -- delete the file size and save only the duration
            if(modelChats.getVnDuration().contains("~")){
                String[] splitDur = modelChats.getVnDuration().split("~");
                voiceNoteDur = splitDur[1];
                modelChats.setVnDuration(voiceNoteDur);
            }
            notifyItemChanged(chatPosition, new Object());

            // delete from Firebase Storage
            deletePathRef.child(voiceNoteUriToFirebase).delete();

            // update ROOM
            chatViewModel.updateVoiceNotePath(modelChats.getFromUid(), modelChats.getIdKey(),
                    voiceNoteOrAudioPath.getPath(), voiceNoteDur);

            fileDownloadTaskMap.remove( modelChats.getIdKey()); // remove from map when done.

        }).addOnFailureListener(exception ->
        {
            // Handle any errors that may occur during the download
            holder.circleDownload.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.GONE);
            fileDownloadTaskMap.remove( modelChats.getIdKey()); // remove from map when done.
            progressMap.remove(modelChats.getIdKey());
            notifyItemChanged(chatPosition, new Object());

        }).addOnProgressListener(taskSnapshot -> {
            // Calculate and track the download progress
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            trackLoadProgress(progress, holder, modelChats);    // voice note - this works when it 75% plus
        });
    }

    //  This helps when the byte is small because fileTask will move 0.00 to 100% at once
    private void autoRunnableProgress(MessageModel modelChats, MessageViewHolder holder){
        // Create a Handler
        Handler handler = new Handler();
        Runnable updateProgressRunnable = new Runnable() {
            @Override
            public void run() {
                // Increment progress by the rate
                int rate = modelChats.getType() == 1 ? 3 : 1;
                int currentProgress = holder.progressBar.getProgress();
                holder.progressBar.setProgress(currentProgress + rate);

                // Check if progress is 90 or more, and remove callbacks to stop updates
                if (currentProgress >= 90) {
                    handler.removeCallbacks(this);
                } else {
                    // Schedule the next update after 100 milliseconds (or 1000 milliseconds for audio type)
                    long delayMillis = modelChats.getType() == 1 ? 100 : 1000;
                    handler.postDelayed(this, delayMillis);
                }
            }
        };

        handler.postDelayed(updateProgressRunnable, 100);

    }

    private void showTheVoiceNotePlaying(MessageModel modelChats, MessageViewHolder holder){
        if(mediaPlayerMap.get(modelChats.getIdKey()) != null){
            MediaPlayer currentMediaPlayer = mediaPlayerMap.get(modelChats.getIdKey());
            if(currentMediaPlayer.isPlaying()){
                // get the last saved seekProgress position
                if(mapLastDuration.get(modelChats.getIdKey()) != null){
                    int lastPosition = mapLastDuration.get(modelChats.getIdKey());
                    currentMediaPlayer.seekTo(lastPosition);
                }
                holder.pauseAndPlay_IV.setImageResource(R.drawable.baseline_pause_24);
                // call runnable to keep the seekBar progress intact
                runnableSeekBarProgress(holder, modelChats, currentMediaPlayer);   // voice note setting
                voiceNoteHasFinishedPlaying(holder, modelChats);    // voice note setting

            } else {    // send the last seekBar progress and time
                holder.totalMinute_TV.setText(modelChats.getVnDuration());
                String dur = modelChats.getVnDuration();
                if(modelChats.getVnDuration().contains("Audio")){
                    String sp[] = modelChats.getVnDuration().split("Audio");
                    dur = sp[0];
                }
                holder.seekBarProgress.setMax(parseDuration(dur)); // done
                if(mapLastDuration.get(modelChats.getIdKey()) != null){
                    holder.seekBarProgress.setProgress( mapLastDuration.get(modelChats.getIdKey()) );
                }
            }

        } else {
            holder.totalMinute_TV.setText(modelChats.getVnDuration());
        }

    }

    private void activateSendingVN(MessageModel modelChats, MessageViewHolder holder, int chatPosition){
        String photoCheck = MainActivity.voiceNoteIdShareRef.getString(modelChats.getIdKey(), "");
        String[] splitX = photoCheck.split(AllConstants.JOIN);
        // check if the ref is not empty
        if(splitX.length > 1) {
            String otherId = splitX[0];
            String isSending = splitX[1];
            // check if it hasn't started downloading already
            if(isSending.equals("yes")){
                if(MainActivity.networkOk){

                    // send voice note
                    sendMyVoiceNote(modelChats, holder, otherId, chatPosition);    // for settings

                } else {
                    holder.circleDownload.setVisibility(View.VISIBLE);
                    holder.progressBar.setVisibility(View.GONE);
                }
            }
        }
    }

    // if voice note is already downloading, show the progress it has reached
    private void displaySendingProgress(MessageModel modelChats, MessageViewHolder holder){
        holder.totalMinute_TV.setText(modelChats.getVnDuration());
        // load the last progress
        if(progressMap.get(modelChats.getIdKey()) != null){
            holder.circleDownload.setVisibility(View.INVISIBLE);
            int lastProgress = progressMap.get(modelChats.getIdKey());
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.progressBar.setProgress(lastProgress);
            // activate the progress listener and continue to update the progress
            if(uploadTaskMap.get(modelChats.getIdKey()) != null){
                uploadTaskMap.get(modelChats.getIdKey()).addOnProgressListener(snapshot -> {
                    // Calculate progress percentage
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    trackLoadProgress(progress, holder, modelChats);    // voice note
                });
            }
        }
    }



    //  =============== General methods

    private String chatTime(long timeDate){
        Date d = new Date(timeDate); //complete Data -- Mon 2023 -03 - 06 12.32pm
        DateFormat formatter = new SimpleDateFormat("h:mm a");
        String time = formatter.format(d);
        String previousDateString = String.valueOf(d);
        int dateLast = Integer.parseInt(previousDateString.substring(8, 10));   // 1 - 30 days

        // months
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

        int lastMonth = dateMonth.get(previousDateString.substring(4,7));
//        String lastYear = previousDateString.substring(32, 34);  // year

        String joinTimeAndDate = time.toLowerCase() + " | " + dateLast +"/"+ lastMonth;

        return joinTimeAndDate;
    }

    // arrange my map for voice_note or photo
    private Map sendMap(MessageModel modelChats, String originalUriPath, String lowImagePath,
                        String imageSize, String imageLinkToFBStorage, String vnPathToStorage)
    {
        Map<String, Object> messageMap = new HashMap<>();

        messageMap.put("from", modelChats.getFrom());
        messageMap.put("fromUid", myId);
        messageMap.put("type", modelChats.getType());            // 8 is for text while 1 is for voice note
        messageMap.put("idKey", modelChats.getIdKey());
        messageMap.put("message", modelChats.getMessage());
        messageMap.put("emojiOnly", modelChats.getEmojiOnly());
        messageMap.put("voiceNote", vnPathToStorage);
        messageMap.put("vnDuration", modelChats.getVnDuration());
        messageMap.put("msgStatus", 0);
        messageMap.put("timeSent", ServerValue.TIMESTAMP);
        messageMap.put("replyFrom", modelChats.getReplyFrom());
        messageMap.put("visibility", modelChats.getVisibility());
        messageMap.put("replyID", modelChats.getReplyID());
        messageMap.put("replyMsg", modelChats.getReplyMsg());
        messageMap.put("isChatPin", modelChats.getIsChatPin());
        messageMap.put("isChatForward", modelChats.getIsChatForward());
        messageMap.put("photoUriPath", imageLinkToFBStorage);
        if(modelChats.getType() == 4 || modelChats.getType() == 1) { // it's audio | voice note
            messageMap.put("photoUriOriginal", null);
        } else {
            messageMap.put("photoUriOriginal", originalUriPath + AllConstants.JOIN + lowImagePath);
        }
        messageMap.put("imageSize", imageSize);

        return messageMap;

    }

    // send my image or voice note to other user database
    private void sendToDatabaseAndRoom(MessageModel modelChats, Map sendMap, String otherUid)
    {
        // send the chat to other user via firebase database
        refMsgFast.child(otherUid).child(myId).child(modelChats.getIdKey()).setValue( sendMap );

        // save last msg for outside chat display
        refLastDetails.child(myId).child(otherUid).setValue( sendMap );
        refLastDetails.child(otherUid).child(myId).setValue( sendMap );

        //  send chatKey to other User to read  -- customise later to check user OnRead settings
        refOnReadRequest.child(otherUid).child(myId).push().setValue(modelChats.getIdKey());

        // update delivery status for outSide chat
        ChatListAdapter.getInstance().updateDeliveryStatus(otherUid);
        // update delivery status ROOM for outside chat
        chatViewModel.updateOutsideDelivery(otherUid, 700024);

        // update inside chat ROOM DB
        chatViewModel.updatePhotoUriPath(modelChats.getIdKey(), otherUid,
                modelChats.getPhotoUriPath(), modelChats.getPhotoUriOriginal(), null, 700024);

        // increase the count number
        MainActivity.checkAndSaveCounts_SendMsg(otherUid);
//System.out.println("what is video pth" + modelChats.getPhotoUriOriginal());
    }

    // track the downloading progress for voice_note or photo
    private void trackLoadProgress(double progress, MessageViewHolder holder, MessageModel modelChats)
    {
        // for image or document
        if(holder.progressBarLoad != null){     // for image or document

            if(progressMap.get(modelChats.getIdKey()) == null) runnableProgressBar(modelChats, holder); // call the runnable method

            int whiteColor = ContextCompat.getColor(mContext, R.color.white);
            // Set the indeterminate tint color for the ProgressBar to white
            holder.progressBarLoad.setIndeterminateTintList(ColorStateList.valueOf(whiteColor));
            // Update progress
            String progress__ = Math.round(progress) + " %";
            holder.loadProgressTV.setText(progress__);
            if(Math.round(progress) >= 60) {
                holder.loadProgressTV.setText(progress__);
                progressMap.put(modelChats.getIdKey(), (int) progress);
            }
            if(progress == 100.0){
                new Handler().postDelayed(() -> holder.loadProgressTV.setVisibility(View.GONE), 300);
            }

            //  for voice note
        } else if (holder.progressBar != null) {    // it is voice note

            if(progress >= 75.0) {
                holder.progressBar.setProgress((int) progress);
                progressMap.put(modelChats.getIdKey(), (int) progress);
            } else if(progress == 100.0){
                holder.progressBar.setVisibility(View.GONE);
                holder.speedTV.setVisibility(View.VISIBLE);
                progressMap.remove(modelChats.getIdKey());
            }
        }

    }

    // for image and document and video
    private void runnableProgressBar(MessageModel modelChats, MessageViewHolder holder){
        Handler handler = new Handler();
        Runnable updateProgressRunnable = new Runnable() {
            @Override
            public void run() {
                if(modelChats.getType() == 2){
                    // Increment progress by the rate
                    int rate = 2;
                    int currentProgress = progressMap.getOrDefault(modelChats.getIdKey(), 3); // Get current progress from the map
                    currentProgress += rate; // Increase progress by the rate
                    holder.loadProgressTV.setText(currentProgress + " %");
                    progressMap.put(modelChats.getIdKey(), currentProgress); // Update progress in the map

                    if (currentProgress >= 70) {
                        handler.removeCallbacks(this);
                    } else {
                        // Schedule the next update after 100 milliseconds (or 1000 milliseconds for document type)
                        handler.postDelayed(this, 200);
                    }
                }
            }
        };

        handler.postDelayed(updateProgressRunnable, 100);

    }

    public void setEmojiReact(MessageViewHolder holder, String emoji){
        holder.react_TV.setVisibility(View.VISIBLE);
         // add the total emoji reaction
        if(emoji.length() > 2){
            String totalReactAndEmoji = (emoji.length()/2) + " " + emoji;
            holder.react_TV.setText(totalReactAndEmoji);
        } else {
            holder.react_TV.setText(emoji);
        }

    }

    // add my own emoji reaction
    public void addEmojiReact(MessageViewHolder holder, String emoji, String chatID, String otherId)
    {
        int chatPosition = findMessagePositionById(chatID);
        // concat previous emoji to the new one
        String addEmoji = modelList.get(chatPosition).getEmoji() != null ?
                modelList.get(chatPosition).getEmoji().concat(emoji): emoji;

        // update local list
        setEmojiReact(holder, addEmoji);
        modelList.get(chatPosition).setEmoji(addEmoji);

        // add to ROOM database
        chatViewModel.updateChatEmoji(otherId, chatID, addEmoji);

    }

    // get the other user emoji reaction and add
    public void emojiReactSignal(String emoji, String chatID, String otherId){
        int chatPosition = findMessagePositionById(chatID) != -1 ? findMessagePositionById(chatID) : -1;
        // check if the chatPosition exist
        if(chatPosition != -1){
            // concat previous emoji to the new one
            String addEmoji = modelList.get(chatPosition).getEmoji() != null ?
                    modelList.get(chatPosition).getEmoji().concat(emoji): emoji;

            modelList.get(chatPosition).setEmoji(addEmoji);
            notifyItemChanged(chatPosition, new Object());

            // add to ROOM database
            chatViewModel.updateChatEmoji(otherId, chatID, addEmoji);
        }

    }

    public void pinIconDisplay(MessageViewHolder holder_, String messageId, boolean status){
        holder_.pinIcon_IV.setVisibility(View.VISIBLE);
        updatePinIcon(messageId, status);   //  ROOM DB
    }
    public void pinIconHide(MessageViewHolder holder_, String messageId, boolean status){
        holder_.pinIcon_IV.setVisibility(View.GONE);
        updatePinIcon(messageId, status);   //  ROOM DB
    }

    public void updatePinIcon(String messageId, boolean status) {
        AllConstants.executors.execute(() -> {
            if(modelList != null){
                for (int i = modelList.size()-1; i >= 0; i--) {
                    if (modelList.get(i).getIdKey().equals(messageId)) {

                        modelList.get(i).setChatPin(status);
                        // update icon on local database
                        chatViewModel.updateChat(modelList.get(i));
                    }
                }
            }
        });

    }

    public int findMessagePositionById(String messageId) {

        if(modelList != null){
            for (int i = modelList.size()-1; i >= 0; i--) {
                if (modelList.get(i).getIdKey().equals(messageId)) {
                    return i;
                }
            }
        }
        return RecyclerView.NO_POSITION;

    }

    public void highlightItem(int position) {
        // Highlight the clicked item
        new Handler().postDelayed(() ->{
            // delay for 500 milliSec to display the view first
            RecyclerView.ViewHolder viewHolder = recyclerMap.get(otherUserUid).findViewHolderForAdapterPosition(position);

            if (viewHolder != null) {
                View itemView = viewHolder.itemView;
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.transparent_orangeLow));
            }

        }, 500);
    }

    public void clearHighlight() {
        // remove last chat highlight if any
        new Handler().postDelayed(() ->{
            // delay for 500 milliSec to display the view first
            RecyclerView.ViewHolder viewHolder = recyclerMap.get(otherUserUid).findViewHolderForAdapterPosition(lastPosition);

            if (viewHolder != null) {
                View itemView = viewHolder.itemView;
                itemView.setBackgroundColor(0);
            }

        }, 500);
    }

    private void retainHighlight(int chatPosition){
        if(isOnlongPress){
            if(chatPositionList.contains(chatPosition)) {
                // Highlight the clicked item
                new Handler().postDelayed(() ->{
                    // delay for 100 milliSec to display the view first
                    RecyclerView.ViewHolder viewHolder = recyclerMap.get(otherUserUid).findViewHolderForAdapterPosition(chatPosition);

                    if (viewHolder != null) {
                        View itemView = viewHolder.itemView;
                        itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.transparent_orangeLow));
                    }

                }, 100);
            }
        }
    }

    public void replyChat(MessageModel messageModel, MessageAdapter.MessageViewHolder holder){
        // call method in MainActivity and set up the details
        fragmentListener.onEditOrReplyMessage(messageModel, "reply",
                "replying...", R.drawable.reply,1, holder);
    }

    private void newMsgNumber(MessageViewHolder holder, int pos){
        refCheck.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(!snapshot.child(uId).child("newMsgCount").exists()){
                    refCheck.child(user.getUid()).child(uId).child("newMsgCount").setValue(0);
                }
                else
                {
                    long newMsgNumber = (long) snapshot.child(uId).child("newMsgCount").getValue();
//                            Log.i("Check", "the num "+snapshot.child(uId));

                    if(newMsgNumber == 0) {
//                        holder.constraintNewMsg.setVisibility(View.GONE);
                    }
                    else {
                        if(pos > (modelList.size() - (newMsgNumber+1)) && pos < (modelList.size() - (newMsgNumber-1))){
//                            holder.constraintNewMsg.setVisibility(View.VISIBLE);
                            holder.textViewNewMsg.setText(newMsgNumber +" new messages");
                        }
                        else{
//                            holder.constraintNewMsg.setVisibility(View.GONE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        List<MessageModel> list = modelList;
        if(list != null){
            return list.size();
        } else {
            return 0;
        }
//        return modelList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        private TextView textViewShowMsg, textViewNewMsg, emojiOnly_TV, photoChatTV;
        private ImageView seenMsg, editNotify, pinALL_IV, pinIcon_IV, forwardIcon_IV;
        private ImageView imageViewReply, imageViewEdit, imageViewPin, imageViewForward;
        private ImageView imageViewReact, imageViewCopy, imageViewDel, imageViewOptions;
        private ConstraintLayout constraintChatTop, constraintMsgContainer, chatContainer;
        private LinearLayout linearLayoutReplyBox, linearLayoutClick;
        private TextView react_TV;
        private TextView replyChat_TV, senderNameTV, otherInfo;
        private TextView timeMsg;
//        private CardView cardViewChatBox;

        // voice note
        private SeekBar seekBarProgress;
        private ImageView pauseAndPlay_IV;
        private TextView totalMinute_TV;
        private CircleImageView circleDownload;
        private ProgressBar progressBar, progressBarLoad;
        private TextView speedTV;

        // photo display
        public ImageView showImage;
        TextView loadProgressTV;

        // document display
        private ConstraintLayout documentContainer;
        private ImageView document_IV;
        private TextView documentDetails_TV;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            if(status == send)
            {
                timeMsg = itemView.findViewById(R.id.textViewChatTime);
                seenMsg = itemView.findViewById(R.id.imageViewSeen);
                textViewShowMsg = itemView.findViewById(R.id.senderChat_TV);
                emojiOnly_TV = itemView.findViewById(R.id.textViewSendOnlyEmoji);
//                chatContainer = itemView.findViewById(R.id.chatContainerS);

                pinALL_IV = itemView.findViewById(R.id.pinALL_S_IV);
                pinIcon_IV = itemView.findViewById(R.id.pinSender_IV);
                forwardIcon_IV = itemView.findViewById(R.id.forwardS_IV);

                // top layer options
                imageViewReply = itemView.findViewById(R.id.imageViewReplyMsg);
                imageViewEdit = itemView.findViewById(R.id.imageViewEdit);
                imageViewPin = itemView.findViewById(R.id.imageViewPinMsg);
                imageViewForward = itemView.findViewById(R.id.imageViewForward);
                imageViewReact = itemView.findViewById(R.id.iVReact);
                imageViewCopy = itemView.findViewById(R.id.imageViewCopyText);
                imageViewDel = itemView.findViewById(R.id.imageViewDel2);

                editNotify = itemView.findViewById(R.id.editedSender_IV);
                textViewNewMsg = itemView.findViewById(R.id.textViewNewMsg);
//                chatContainer = itemView.findViewById(R.id.chatContainerS);
                constraintChatTop = itemView.findViewById(R.id.constraintChatTop);
                constraintMsgContainer = itemView.findViewById(R.id.constraintBodySend);

                // reply
                linearLayoutReplyBox = itemView.findViewById(R.id.linearLayoutReplyBox);
                linearLayoutClick = itemView.findViewById(R.id.linearClickReply1);
                replyChat_TV = itemView.findViewById(R.id.textViewReply);
                senderNameTV = itemView.findViewById(R.id.senderNameTV);

                imageViewOptions = itemView.findViewById(R.id.imageViewOptions);
                react_TV = itemView.findViewById(R.id.reactSender_TV);
                otherInfo = itemView.findViewById(R.id.otherInfo_TV1);

                // voice note
                seekBarProgress = itemView.findViewById(R.id.seekBarMusicProgress);
                circleDownload = itemView.findViewById(R.id.cirleDownload);
                progressBar = itemView.findViewById(R.id.progressBarS);
                pauseAndPlay_IV = itemView.findViewById(R.id.pauseAndPlay_IV);
                totalMinute_TV = itemView.findViewById(R.id.minuteTV);
                speedTV = itemView.findViewById(R.id.speedTV_S);

            } else if (status == sendPhoto || status == receivePhoto)
            {

                constraintMsgContainer = itemView.findViewById(R.id.senderLayerContainer);
                // photo and progress bar
                showImage = itemView.findViewById(R.id.photoCardSender);
                loadProgressTV = itemView.findViewById(R.id.loadPhotoProgressTV);
                progressBarLoad = itemView.findViewById(R.id.progressBarLoad1);

                // general
                timeMsg = itemView.findViewById(R.id.photoChatTimeTV);
                photoChatTV = itemView.findViewById(R.id.photoChatSender_TV);
                seenMsg = itemView.findViewById(R.id.photoSeen_IV);
                pinIcon_IV = itemView.findViewById(R.id.pinPhotoSender_IV);
                forwardIcon_IV = itemView.findViewById(R.id.forwardPhotoSender_IV);
                react_TV = itemView.findViewById(R.id.reactSender_TV);

                // reply box
                linearLayoutReplyBox = itemView.findViewById(R.id.linearLayoutReplyBox_PS);
                replyChat_TV = itemView.findViewById(R.id.textViewReply_PS);
                senderNameTV = itemView.findViewById(R.id.senderNameTV_PS);

                // document
                documentContainer = itemView.findViewById(R.id.documentContainer);
                document_IV = itemView.findViewById(R.id.document_IV);
                documentDetails_TV = itemView.findViewById(R.id.documentDetails_TV);

            }  else {
                timeMsg = itemView.findViewById(R.id.textViewChatTime2);
                textViewShowMsg = itemView.findViewById(R.id.receiverChat_TV);
                emojiOnly_TV = itemView.findViewById(R.id.textViewReceivedOnlyEmoji);
//                chatContainer = itemView.findViewById(R.id.chatContainerR);

                pinALL_IV = itemView.findViewById(R.id.pinALL_R_IV);
                pinIcon_IV = itemView.findViewById(R.id.pinReceiver_IV);
                forwardIcon_IV = itemView.findViewById(R.id.forwardR_IV);

                imageViewReply = itemView.findViewById(R.id.imageViewReplyMsg2);
                imageViewEdit = itemView.findViewById(R.id.imageEdit);
                imageViewPin = itemView.findViewById(R.id.imageViewReceivePinMsg);
                imageViewForward = itemView.findViewById(R.id.imageViewForward2);
                imageViewReact = itemView.findViewById(R.id.imageViewReact2);
                imageViewCopy = itemView.findViewById(R.id.imageViewReceiveCopyText);
                imageViewDel = itemView.findViewById(R.id.imageViewReceiveDel);
                editNotify = itemView.findViewById(R.id.editedReceiver_IV);
                textViewNewMsg = itemView.findViewById(R.id.textViewNewMsg2);
//                chatContainer = itemView.findViewById(R.id.chatContainerR);
                constraintChatTop = itemView.findViewById(R.id.constraintReceiveTop);
                constraintMsgContainer = itemView.findViewById(R.id.constraintBodyReceive);
                imageViewOptions = itemView.findViewById(R.id.imageViewOptions2);

                // reply
                linearLayoutReplyBox = itemView.findViewById(R.id.linearLayoutReplyBox2);
                linearLayoutClick = itemView.findViewById(R.id.linearClickReply2);
                replyChat_TV = itemView.findViewById(R.id.textViewReply2);
                senderNameTV = itemView.findViewById(R.id.senderName2);

                react_TV = itemView.findViewById(R.id.reactReceiver_TV);
                otherInfo = itemView.findViewById(R.id.otherInfo_TV2);

                // voice note
                seekBarProgress = itemView.findViewById(R.id.seekBarMusicProgress2);
                circleDownload = itemView.findViewById(R.id.circleDownload2);
                progressBar = itemView.findViewById(R.id.progressBarR);
                pauseAndPlay_IV = itemView.findViewById(R.id.pauseAndPlay_IV2);
                totalMinute_TV = itemView.findViewById(R.id.minuteTV2);
                speedTV = itemView.findViewById(R.id.speedTV_R);

            }
        }

        // Getter method to access your specific view
//        public View getYourSpecificView() {
//            return seekBarProgress;
//        }
        
    }

    @Override
    public int getItemViewType(int position) {

        // type 0 is for just text-chat, type 1 is voice_note, type 2 is photo, type 3 is document, type 4 is audio (mp3)

        MessageModel chat = modelList.get(position);
        // check if the chat is from me via my uid
        if(chat.getFromUid().equals(myId))
        {
            if(chat.getPhotoUriPath() != null || chat.getType() == 2 || chat.getType() == 3)
            {
                status = sendPhoto;
                return sendPhoto;
            } else
            {
                status = send;
                return send;
            }

        } else
        {    //  chat is from other user
            if(chat.getPhotoUriPath() != null || chat.getType() == 2 || chat.getType() == 3)
            {
                status = receivePhoto;
                return receivePhoto;
            } else
            {
                status = receive;
                return receive;
            }
        }
    }

}


