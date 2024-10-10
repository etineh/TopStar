package com.pixel.chatapp.adapters;

import static com.pixel.chatapp.view_controller.MainActivity.insideChatMap;
import static com.pixel.chatapp.view_controller.MainActivity.newChatNumberPosition;
import static com.pixel.chatapp.view_controller.MainActivity.otherUserFcmTokenRef;
import static com.pixel.chatapp.utilities.FileUtils.compressVideo;
import static com.pixel.chatapp.utilities.FileUtils.createVideoThumbnail;
import static com.pixel.chatapp.utilities.FileUtils.downloadThumbnailFile;
import static com.pixel.chatapp.utilities.FileUtils.formatDuration;
import static com.pixel.chatapp.utilities.FileUtils.isFileLessThan150Kb;
import static com.pixel.chatapp.utilities.FileUtils.parseDuration;
import static com.pixel.chatapp.utilities.FileUtils.replaceSnapPhotoWithCompressFile;
import static com.pixel.chatapp.utilities.FileUtils.saveFileFromContentUriToAppStorage;
import static com.pixel.chatapp.utilities.FolderUtils.getAudioFolder;
import static com.pixel.chatapp.utilities.FolderUtils.getThumbnailFolder;
import static com.pixel.chatapp.utilities.FolderUtils.getVoiceNoteFolder;
import static com.pixel.chatapp.view_controller.MainActivity.chatViewModel;
import static com.pixel.chatapp.view_controller.MainActivity.contactNameShareRef;
import static com.pixel.chatapp.view_controller.MainActivity.filePositionMap;
import static com.pixel.chatapp.view_controller.MainActivity.networkOk;
import static com.pixel.chatapp.view_controller.MainActivity.otherUserUid;
import static com.pixel.chatapp.view_controller.MainActivity.photoAndVideoMap;
import static com.pixel.chatapp.view_controller.MainActivity.recyclerMap;
import static com.pixel.chatapp.view_controller.MainActivity.viewCacheReceive;
import static com.pixel.chatapp.view_controller.MainActivity.viewCacheSend;
import static com.pixel.chatapp.view_controller.MainActivity.viewPhotoReceive;
import static com.pixel.chatapp.view_controller.MainActivity.viewPhotoSend;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
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
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pixel.chatapp.constants.K;
import com.pixel.chatapp.view_controller.fragments.ChatsFragment;
import com.pixel.chatapp.view_controller.fragments.PlayersFragment;
import com.pixel.chatapp.utilities.ChatUtils;
import com.pixel.chatapp.utilities.TimeUtils;
import com.pixel.chatapp.view_controller.photos_video.ViewImageActivity;
import com.pixel.chatapp.utilities.FileUtils;
import com.pixel.chatapp.utilities.FolderUtils;
import com.pixel.chatapp.interface_listeners.FragmentListener;
import com.pixel.chatapp.permission.AppPermission;
import com.pixel.chatapp.R;
import com.pixel.chatapp.view_controller.MainActivity;
import com.pixel.chatapp.dataModel.MessageModel;
import com.pixel.chatapp.utilities.ToggleUtils;
import com.pixel.chatapp.utilities.UserChatUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<MessageModel> modelList;

    // Define a HashSet to store unique message IDs
    public Set<String> messageIdSet = new HashSet<>(); // to avoid duplicate adding chat, store each id chat here

    public static int lastPosition = 0;

    public static List<Integer> chatPositionList = new ArrayList<>();
    public String uId;
    public String myUsername;
    private Map<String, Runnable> sightedRunnableMap = new HashMap<>();
    private Map<String, Handler> handlerNewChatNumMap = new HashMap<>();

    Handler handlerNewChatNum = new Handler();

    public  Context mContext;

    private int status;
    private final int send;
    private final int sendPhoto;
    private final int receive;
    private final int receivePhoto;
    private final int empty;
    private final int callReceive;
    private final int callSend;
    private final int pinChat;
    private final String myId;
    FirebaseUser user;
    Map<String, Integer> dateMonth = new HashMap<>();
    DatabaseReference refCheck, refUsers, refMsgFast, refLastDetails, refOnReadRequest, refPinMessages;
    private StorageReference deletePathRef = FirebaseStorage.getInstance().getReference();
    Handler handler;
    private static final String VOICE_NOTE = "MyPreferences";
    private static final String KEY_LIST = "myList";
    private List<Map<String, Object>> mapList;
    private AppPermission permissions = new AppPermission();

    private FragmentListener fragmentListener;
    public void setFragmentListener(FragmentListener fragmentListener) {
        this.fragmentListener = fragmentListener;
    }

    // I am using "static" so it add up only once and reuse for all chat box
//    public static List<View> viewCacheSend = new ArrayList<>(); // List to store views for caching
//    public static List<View> viewCacheReceive = new ArrayList<>(); // List to store views for caching
//    public static List<View> viewPhotoSend = new ArrayList<>(); // List to store views for caching
//    public static List<View> viewPhotoReceive = new ArrayList<>(); // List to store views for caching
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

    private final MainActivity mainActivity = new MainActivity();

    public MessageAdapter( List<MessageModel> modelList, String myUsername, String uId,
                          Context mContext, ViewGroup parent) {
        this.modelList = modelList;
        this.myUsername = myUsername;
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
        callSend = 6;
        callReceive = 7;
        pinChat = 8;

        empty = 10;

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
        // Set the new modelList
        this.modelList = modelList;

        // Clear the existing message ID set before adding new message IDs
        messageIdSet.clear();

        // Iterate through the modelList to extract message IDs and add them to the messageIdSet
        new Thread(() -> {
            synchronized (this.modelList) {
                for (MessageModel message : modelList) {
                    messageIdSet.add(message.getIdKey());
                }
            }
        }).start();
    }


    // add new message to list method
    public void addNewMessageDB(MessageModel newMessage) {

        // Check if the message ID is already present in the HashSet
        if (!messageIdSet.contains(newMessage.getIdKey())) {
            // Add the new message ID to the HashSet
            messageIdSet.add(newMessage.getIdKey());

            // Add the new message to the list
            modelList.add(newMessage);
        }

    }

    // add new message to list method
    public void addMyMessageDB(MessageModel newMessage) {

        // Add the new message to the list
        modelList.add(newMessage);

        // Add the new message ID to the HashSet
        messageIdSet.add(newMessage.getIdKey());

    }

    public void updateMessage(MessageModel chatModel)
    {
        int numLoop = modelList.size() > 100 ? modelList.size() - 50 : 0;
        for (int i = modelList.size()-1; i >= numLoop; i--) {
            String all_IDs = modelList.get(i).getIdKey();
            if (chatModel.getIdKey().equals(all_IDs)) { // Use equals() for string comparison
                modelList.remove(i);
                modelList.add(i, chatModel);
                notifyItemChanged(i, new Object());
                break;
            }
        }
    }

    public void updateCallOrGameChat(MessageModel chatModel)
    {
        int numLoop = modelList.size() > 50 ? modelList.size() - 30 : 0;
        for (int i = modelList.size()-1; i >= numLoop; i--) {
            String all_IDs = modelList.get(i).getIdKey();
            if (chatModel.getIdKey().equals(all_IDs)) { // Use equals() for string comparison
                modelList.remove(i);
                modelList.add(i, chatModel);
                notifyItemChanged(i, new Object());
                break;
            }
        }
    }

    public void getChatAndIncrementNewChatNumber(String number)
    {
        for (int i = modelList.size()-1; i >= 0; i--)
        {
            int finalPosition = i;

            int type = modelList.get(finalPosition).getType();

            String edit = modelList.get(finalPosition).getEdit();

            if(type == K.type_pin && edit != null && edit.equals("yes"))
            {
                handler.post(()-> {
                    modelList.get(finalPosition).setNewChatNumberID(number);
                    notifyItemChanged(finalPosition, new Object());
                });

                chatViewModel.updateChat(modelList.get(finalPosition));    // update room

                break;
            }
        }
    }

    public void getChatByPinTypeAndDelete(int i)    // get the position on first sight and delete when user press back
    {
        int type = modelList.get(i).getType();
        String edit = modelList.get(i).getEdit();

        if(type == K.type_pin && edit != null && edit.equals("yes"))
        {
            chatViewModel.deleteChat(modelList.get(i));    // delete from room

            handler.post(()-> {
                modelList.remove(i);    // delete from list
                notifyItemRemoved(i);
                notifyItemRangeChanged(i, modelList.size(), new Object());
//                System.out.println("what is found222");

            });
        }

    }

    public void getChatByPinTypeAndDeleteViaRecycler(RecyclerView recyclerView, String otherId)
    {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;

            int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
            int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();

            // Ensure the indices are within bounds
            if (firstVisibleItemPosition < 0 || lastVisibleItemPosition < 0) {
                return;
            }

            for (int i = lastVisibleItemPosition; i >= firstVisibleItemPosition; i--)
            {
                int finalPosition = i;
                int type = modelList.get(finalPosition).getType();
                String edit = modelList.get(finalPosition).getEdit();
                MessageModel modelChats = modelList.get(finalPosition);

                if (type == K.type_pin && edit != null && edit.equals("yes"))
                {
                    chatViewModel.deleteChat(modelChats); // delete from room

                    K.handler.post(()->{
                        modelList.remove(finalPosition); // delete from list
                        notifyItemRemoved(finalPosition);
                        notifyItemRangeChanged(finalPosition, modelList.size(), new Object());
//                        System.out.println("what is found");
                    });
                    // reset new chat count -- outside outside >> in case I am inside the chat when user send new chat
                    if(ChatsFragment.adapter != null)
                        ChatsFragment.adapter.findUserModelByUidAndResetNewChatNum(otherId, K.fromChatFragment, true);
                    if(PlayersFragment.adapter != null)
                        PlayersFragment.adapter.findUserModelByUidAndResetNewChatNum(otherId, K.fromPlayerFragment, true);

                    // remove the previous runnable to prevent the onBindView calling it again
                    Runnable runnableNewChatNum = sightedRunnableMap.get(modelChats.getIdKey());
                    if(handlerNewChatNum != null && runnableNewChatNum != null) {
                        handlerNewChatNum.removeCallbacks(runnableNewChatNum);
                    }
                    break;

                } else if (i == firstVisibleItemPosition){
                    if(newChatNumberPosition != -1) getChatByPinTypeAndDelete(newChatNumberPosition);
                }
            }
        }
    }

    public void deleteMessage(String id){
        for (int i = modelList.size()-1; i >= 0; i--) {
            String all_IDs = modelList.get(i).getIdKey();
            if (id.equals(all_IDs)) { // Use equals() for string comparison
                modelList.remove(i);
                notifyItemRangeChanged(i, modelList.size(), new Object());
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
            if(viewTypeSelect == send && viewCacheSend.size() < 30){
                View itemView = inflater.inflate(R.layout.view_card, parent, false);
                viewCacheSend.add(itemView);
                System.out.println("Adding to viewSend " + viewCacheSend.size());
            } else if (viewTypeSelect == sendPhoto && viewPhotoSend.size() < 30) {
//                if()
                View itemView = inflater.inflate(R.layout.photo_send_card, parent, false);
                viewPhotoSend.add(itemView);
                System.out.println("Adding to viewPhotoSend " + viewPhotoSend.size());
            } else if (viewTypeSelect == receivePhoto && viewPhotoReceive.size() < 30) {
                View itemView = inflater.inflate(R.layout.photo_receive_card, parent, false);
                viewPhotoReceive.add(itemView);
                System.out.println("Adding to viewPhotoRe " + viewPhotoReceive.size());
            } else if(viewCacheReceive.size() < 30) {
                View itemView = inflater.inflate(R.layout.view_card_receiver, parent, false);
                viewCacheReceive.add(itemView);
                System.out.println("Adding to viewReceive " + viewCacheReceive.size());
            }

            return null;
        }
    }

    public void addLayoutWhileScrolling(int viewTypeSelect)
    {
        new Thread(()->
        {
            if(viewTypeSelect == send && viewCacheSend.size() < 30){
                View itemView = inflater.inflate(R.layout.view_card, parent, false);
                viewCacheSend.add(itemView);
                System.out.println("Adding to viewSend " + viewCacheSend.size());
            } else if (viewTypeSelect == sendPhoto && viewPhotoSend.size() < 30)
            {
//                if()
                View itemView = inflater.inflate(R.layout.photo_send_card, parent, false);
                viewPhotoSend.add(itemView);
                System.out.println("Adding to viewPhotoSend " + viewPhotoSend.size());
            } else if (viewTypeSelect == receivePhoto && viewPhotoReceive.size() < 30)
            {
                View itemView = inflater.inflate(R.layout.photo_receive_card, parent, false);
                viewPhotoReceive.add(itemView);
                System.out.println("Adding to viewPhotoRe " + viewPhotoReceive.size());
            } else if(viewCacheReceive.size() < 30) {
                View itemView = inflater.inflate(R.layout.view_card_receiver, parent, false);
                viewCacheReceive.add(itemView);
                System.out.println("Adding to viewReceive " + viewCacheReceive.size());
            }
        }).start();
    }

    public void addLayoutViewInBackground() {
        Executor executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {

            for (int i = 0; i < 10; i++) {

                if(i == 9) {
                    ((Activity) mContext).runOnUiThread(() -> {
//                        Toast.makeText(mContext, "Test view MsgAdapter L310", Toast.LENGTH_SHORT).show();
                    });
                }

                View itemView;

                if(i % 2 == 0){
                    itemView = inflater.inflate(R.layout.view_card, parent, false);
                    synchronized (viewCacheSend) {
                        viewCacheSend.add(itemView);
                        System.out.println("Added to viewCacheSend:  " + viewCacheSend.size());
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

        new PreloadViewsTask(viewType).execute();
//        addLayoutWhileScrolling(viewType);

        View itemView;
        if (viewType == send && !viewCacheSend.isEmpty())
        {
            // Retrieve and remove a cached send view
            itemView = viewCacheSend.remove(0);
//            new PreloadViewsTask(viewType).execute();

        } else if (viewType == sendPhoto && !viewPhotoSend.isEmpty())
        {

            itemView = viewPhotoSend.remove(0);
//            new PreloadViewsTask(viewType).execute();

        } else if (viewType == receivePhoto && !viewPhotoReceive.isEmpty())
        {

            itemView = viewPhotoReceive.remove(0);
//            new PreloadViewsTask(viewType).execute();

        } else if (viewType == receive && !viewCacheReceive.isEmpty())
        {
            // Retrieve and remove a cached receive view
            itemView = viewCacheReceive.remove(0);
//            new PreloadViewsTask(viewType).execute();

        } else
        {
            // Inflate a new view if the cache is empty or the view type doesn't match
            int layer = R.layout.empty_chat_card;
            if(viewType == send)
            {
                layer = R.layout.view_card;
//                Toast.makeText(mContext, "view sender", Toast.LENGTH_SHORT).show();
            } else if (viewType == sendPhoto)
            {
                layer = R.layout.photo_send_card;
//                Toast.makeText(mContext, "view photo send", Toast.LENGTH_SHORT).show();
            } else if (viewType == receivePhoto)
            {
                layer = R.layout.photo_receive_card;
//                Toast.makeText(mContext, "view photo receive", Toast.LENGTH_SHORT).show();
            } else if (viewType == receive) {
                layer = R.layout.view_card_receiver;
//                Toast.makeText(mContext, "view receiver", Toast.LENGTH_SHORT).show();
            } else if (viewType == callSend) {
                layer = R.layout.call_sender_card;
            } else if (viewType == callReceive) {
                layer = R.layout.call_receiver_card;
            } else if (viewType == pinChat) {
                layer = R.layout.pin_chat_card;
            }

            itemView = inflater.inflate(layer, parent, false);

        }

        return new MessageViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position_) {

        // type 0 - text-chat, type 1 is voice_note, type 2 is photo, type 3 is document,
        // type 4 is audio (mp3), type 5 is video, 6 is call, 7 is game, 8 is pin, 10 is empty card
        setColours(holder);

        int chatPosition = position_;     //   to get the position of each chat
        MessageModel modelChats = modelList.get(chatPosition);    // get the model position of each chat

        // reset all data to default
        if(modelChats.getType() != 10 && modelChats.getType() != 6 && modelChats.getType() != 7 && modelChats.getType() != 8)
        {
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
                holder.emojiOnly_TV.setVisibility(View.GONE);
            }
            holder.timeMsg.setText("");             // time reset

            if(holder.react_TV != null){
                holder.react_TV.setVisibility(View.GONE);
            }       // react emoji reset

        }

        // reset reply box
        if(holder.linearLayoutReplyBox != null){
            holder.linearLayoutReplyBox.setVisibility(View.GONE);
            if(holder.linearLayoutClick != null) holder.linearLayoutClick.setVisibility(View.GONE);
            holder.senderNameTV.setText(null);
            holder.replyChat_TV.setText(null);
        }

        // reset calls and games
        if(modelChats.getType() == K.type_call || modelChats.getType() == K.type_game)
        {
            holder.callOrGameHeading_TV.setText(null);
            holder.response_TV.setText(null);

            // interchange the game icon later
            holder.callOrGame_IV.setImageResource(0);
            holder.timeMsg.setText(null);    // time reset
        }

        // reset pin or number of new chat
        if(modelChats.getType() == K.type_pin) holder.pinAlertTV.setText(null);

        // reset voice note tools player
        if(holder.seekBarProgress != null){
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(holder.constraintMsgContainer);

            // Clear the constraint when the condition is not met
            if(modelChats.getFromUid().equals(myId)) {
                constraintSet.clear(holder.chatContainer.getId(), ConstraintSet.START); // Clear start as well if needed
            } else {
                constraintSet.clear(holder.chatContainer.getId(), ConstraintSet.END);
            }
            constraintSet.applyTo(holder.constraintMsgContainer);

            holder.seekBarProgress.setVisibility(View.GONE);
            holder.circleDownload.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.GONE);
            holder.speedTV.setVisibility(View.INVISIBLE);
            holder.pauseAndPlay_IV.setVisibility(View.GONE);
            holder.seekBarProgress.setProgress(0);
            holder.totalMinute_TV.setText("");
        }

        // reset photo, video and document tools
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
            holder.playVideoIcon_IV.setVisibility(View.GONE);
            holder.videoDuration_TV.setVisibility(View.GONE);
            holder.videoDuration_TV.setText(null);

        }

        holder.itemView.setBackgroundColor(0);  //  reset background highlight

        if (modelChats.getVoiceNote() != null)      // adjust voice note size
        {
            // Add logging to see if this block is executed

            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(holder.constraintMsgContainer);

            // Set new constraints
            if(modelChats.getFromUid().equals(myId)) {
                constraintSet.connect(holder.chatContainer.getId(), ConstraintSet.START, holder.constraintMsgContainer.getId(), ConstraintSet.START); // Ensure start is constrained too
            } else {
                constraintSet.connect(holder.chatContainer.getId(), ConstraintSet.END, holder.constraintMsgContainer.getId(), ConstraintSet.END);
            }

            constraintSet.applyTo(holder.constraintMsgContainer);

        }


        //  ==============    input the real data to their positions after resetting    ============


        if(holder.timeMsg != null) holder.timeMsg.setText( chatTime(modelChats.getTimeSent()) ); // show the time each msg was sent

        if (modelChats.getMessage() != null)
        {
            String message = modelChats.getMessage() + chatPosition;

            if (holder.textViewShowMsg != null) {
                SpannableString spannableString = seperateUsernameFromText(message, modelChats, holder, chatPosition);
                holder.textViewShowMsg.setText(spannableString);
                holder.textViewShowMsg.setMovementMethod(LinkMovementMethod.getInstance());
            }

            if (holder.photoChatTV != null && modelChats.getMessage().length() > 0) {
                SpannableString spannableString = seperateUsernameFromText(message, modelChats, holder, chatPosition);
                holder.photoChatTV.setVisibility(View.VISIBLE);
                holder.photoChatTV.setText(spannableString);
                holder.photoChatTV.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }

        if(modelChats.getEmojiOnly() != null && modelChats.getType() == 0)  // set emoji only
        {
            holder.emojiOnly_TV.setText(modelChats.getEmojiOnly());
            holder.emojiOnly_TV.setVisibility(View.VISIBLE);
        }

        // set edit icon on chat
        if(modelChats.getEdit() != null){
            if(modelChats.getEdit().equals("edited"))
                holder.editNotify.setVisibility(View.VISIBLE);
        }

        // set forward icon on chat
        if(modelChats.getChatIsForward()){
            holder.forwardIcon_IV.setVisibility(View.VISIBLE);
        }

        // set pin icon on chat
        if(modelChats.getChatIsPin()){
            holder.pinIcon_IV.setVisibility(View.VISIBLE);
        }

        //  set emoji reaction
        if(modelChats.getEmoji() != null){
            setEmojiReact(holder, modelChats.getEmoji());
        }

        // set call or game on chat
        if (modelChats.getType() == 6 || modelChats.getType() == 7)
        {
            holder.callOrGameHeading_TV.setText(modelChats.getMessage());
            holder.response_TV.setText(modelChats.getEmojiOnly());

            int callIcon = R.drawable.baseline_call_24;
            if(modelChats.getType() == K.type_game) callIcon = R.drawable.baseline_games_24;

            holder.callOrGame_IV.setImageResource(callIcon);    // interchange the game icon later
        }

        // ----------------- reply msg setting
        if(modelChats.getReplyFrom() != null && modelChats.getReplyMsg() != null && holder.linearLayoutReplyBox != null)
        {
            if(holder.linearLayoutClick != null) holder.linearLayoutClick.setVisibility(View.VISIBLE);
            holder.linearLayoutReplyBox.setVisibility(View.VISIBLE);    // set reply container to visibility
            holder.replyChat_TV.setText(modelChats.getReplyMsg());     //   set the reply text on top msg

            if(modelChats.getReplyFrom().contains(K.JOIN))   //  set the username for reply msg
            {
                if(modelChats.getReplyFrom().contains(myId))
                {
                    holder.senderNameTV.setText(mContext.getString(R.string.you));
                } else {
                    String[] split_uid_name = modelChats.getReplyFrom().split(K.JOIN);
                    String otherName = contactNameShareRef.getString(split_uid_name[0], split_uid_name[1]);
                    holder.senderNameTV.setText(otherName);
                }

            } else {
                holder.senderNameTV.setText(modelChats.getReplyFrom());
            }
        }

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

        // set or auto-send image and document => type 0 is text-chat, type 1 is voice_note, 2 is photo, 3 is document, 4 is audio (mp3), 5 is video
        if(modelChats.getPhotoUriPath() != null || modelChats.getType() == 2 || modelChats.getType() == 3 || modelChats.getType() == 5)
        {

            // activate my auto image sending  ------ change later to auto download other user photo that he sent according to his settings
            if( modelChats.getFromUid().equals(myId) && modelChats.getMsgStatus() == 700033
                    && modelChats.getPhotoUriOriginal() != null ){
                // get the photo uid owner and the sending state
                String photoCheck = MainActivity.documentIdShareRef.getString(modelChats.getIdKey(), "");
                String[] splitX = photoCheck.split(K.JOIN);
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
                            .putString(modelChats.getIdKey(), otherId + K.JOIN + "no").apply();
                }

            }

            // display or download the low quality image or document thumbnail  (if not downloaded yet)
            if (modelChats.getPhotoUriPath() != null){
                Uri imageUri_ = Uri.parse(modelChats.getPhotoUriPath());
                displayLowImageOrDocumentThumbnail(imageUri_, holder, modelChats);

                // download the low quality photo or doc thumbnail auto from firebase (if not downloaded yet)
                K.executors.execute(() -> {
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


        // ----- Voice NoteDocumentation and Audio setting and auto sending
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
                holder.speedTV.setVisibility(View.INVISIBLE);
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
                    holder.circleDownload.setVisibility(View.GONE);
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


        View.OnClickListener optionClickListener = view ->
        {
            textAndOptionsOnclick(modelChats, holder, chatPosition);
        };
        //   show chat selection options via onClick
        holder.constraintMsgContainer.setOnClickListener(optionClickListener);

        //   show chat selection options on top via onLongClick
        View.OnLongClickListener longClick = (view -> {
            MainActivity.isOnLongClick =1;

            if(MainActivity.chatOptionView != null && MainActivity.chatOptionView.getVisibility() != View.VISIBLE){
                // activate long click press and send data to MainActivity
                if(modelChats.getType() != 1 && modelChats.getType() != 2 && modelChats.getType() != 5)
                {
                    activateOnLongClick(modelChats, holder, chatPosition);

                } else {
                    if( (modelChats.getType() == 1 && !modelChats.getVoiceNote().startsWith("media/voice"))
                            || ( modelChats.getPhotoUriOriginal() != null && !modelChats.getPhotoUriOriginal().startsWith("media") ))
                    {
                        activateOnLongClick(modelChats, holder, chatPosition);
                    }  else {
                        Toast.makeText(mContext, mContext.getString(R.string.downloadFirst), Toast.LENGTH_SHORT).show();
                    }
                }

            } else {
                // if onLongPress mood is activated, add or remove chat from list when user click a chat
                if(modelChats.getType() != 1 && modelChats.getType() != 2 && modelChats.getType() != 5)
                {
                    addOrRemoveChatFromList(modelChats, chatPosition);

                } else {
                    if( (modelChats.getType() == 1 && !modelChats.getVoiceNote().startsWith("media/voice"))
                            || ( modelChats.getPhotoUriOriginal() != null && !modelChats.getPhotoUriOriginal().startsWith("media") ))
                    {
                        addOrRemoveChatFromList(modelChats, chatPosition);
                    }  else {
                        Toast.makeText(mContext, mContext.getString(R.string.downloadFirst), Toast.LENGTH_SHORT).show();
                    }
                }
            }

//            new Handler().postDelayed(()-> MainActivity.isOnlongClick = 0, 2000);

            return true;
        });

        if(modelChats.getType() != empty && modelChats.getType() != 6 && modelChats.getType() != 7 && modelChats.getType() != 8)
        {
            holder.linearLayoutReplyBox.setOnLongClickListener(longClick);
            holder.constraintMsgContainer.setOnLongClickListener(longClick);
            if(holder.photoChatTV != null ) holder.photoChatTV.setOnLongClickListener(longClick);
            if(holder.textViewShowMsg != null ) holder.textViewShowMsg.setOnLongClickListener(longClick);
        }

        //  image, document and progressLoad bar onClick settings
        if(holder.showImage != null){

            // for long press
            holder.loadProgressTV.setOnLongClickListener(longClick);
            holder.showImage.setOnLongClickListener(longClick);
            holder.documentContainer.setOnLongClickListener(longClick);

            // for single onClick =>    type 0 is for just text-chat, type 1 is voice_note, type 2 is photo, 3 is document, 4 is audio (mp3), 5 is video
            View.OnClickListener imageOrDocOnClick = view ->
            {
                // check if longPress is activated yet or not
                if(MainActivity.chatOptionView != null && MainActivity.chatOptionView.getVisibility() != View.VISIBLE)
                {
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

                    } else{ // download the photo other user sent to me
                        if(fileDownloadTaskMap.get(modelChats.getIdKey()) != null){
                            if(fileDownloadTaskMap.get(modelChats.getIdKey()).isInProgress() ){
                                fileDownloadTaskMap.get(modelChats.getIdKey()).cancel();
                                fileDownloadTaskMap.remove(modelChats.getIdKey());  // reset the map for that chat id
                                notifyItemChanged(chatPosition);
                                Toast.makeText(mContext, mContext.getString(R.string.dCancel), Toast.LENGTH_SHORT).show();
                            }

                        }else downloadPhotoOrDocSentByOtherUser(modelChats, holder, chatPosition);
                    }

                } else {
                    // if onLongPress mood is activated, add or remove chat from list when user click a chat
                    addOrRemoveChatFromList(modelChats, chatPosition);
                }

            };
            holder.showImage.setOnClickListener(imageOrDocOnClick);
            holder.documentContainer.setOnClickListener(imageOrDocOnClick);

            holder.loadProgressTV.setOnClickListener(view ->
            {
                if(MainActivity.chatOptionView.getVisibility() != View.VISIBLE){

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
                    } else Toast.makeText(mContext, mContext.getString(R.string.resendFile), Toast.LENGTH_SHORT).show();

                } else {
                    // if onLongPress mood is activated, add or remove chat from list when user click a chat
                    addOrRemoveChatFromList(modelChats, chatPosition);
                }

            });
        }

        // voice note play button
        if(holder.pauseAndPlay_IV != null) holder.pauseAndPlay_IV.setOnClickListener(view -> {

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

        if(holder.imageViewOptions != null) holder.imageViewOptions.setOnClickListener(optionClickListener);

        // download / send voice note
        if(holder.circleDownload != null) holder.circleDownload.setOnClickListener(view -> {
            // check if the voice note is from me.
            if(modelChats.getFromUid().equals(myId)){
                // get the photo uid owner and the sending state
                String photoCheck = MainActivity.voiceNoteIdShareRef.getString(modelChats.getIdKey(), "");
                String[] splitX = photoCheck.split(K.JOIN);
                // check if the ref is not empty
                if(splitX.length > 1) {
                    String otherId = splitX[0];
                    String isSending = splitX[1];
                    if(MainActivity.networkOk){
                        if(isSending.equals("no") || holder.circleDownload.getVisibility() == View.VISIBLE){
                            // send voice note
                            sendMyVoiceNote(modelChats, holder, otherId, chatPosition);    // for button click
                        }
                    } else Toast.makeText(mContext, mContext.getString(R.string.noInternetConnection), Toast.LENGTH_SHORT).show();
                }

            } else {
                // download the voice note sent from other user
                if(networkOk){
                    // check if download is already in progress and if voice note duration is less than 5minutes
                    if(fileDownloadTaskMap.get(modelChats.getIdKey()) == null && holder.circleDownload.getVisibility() == View.VISIBLE){

                        downloadOtherUserVoiceNote(modelChats, holder, position_);  // onClick

                    }
                } else Toast.makeText(mContext, mContext.getString(R.string.noInternetConnection), Toast.LENGTH_SHORT).show();

            }

        });


        // single onClick -- scroll and highlight reply message
        View.OnClickListener scrollToReplyChat = view -> {

            if(MainActivity.chatOptionView != null && MainActivity.chatOptionView.getVisibility() != View.VISIBLE)
            {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.transparent_orange));

                new Handler().postDelayed(()-> {
                    String originalMessageId = modelChats.getReplyID();
                    int originalPosition = findMessagePositionById(originalMessageId);

                    // Scroll to the original message's position
                    if (originalPosition != RecyclerView.NO_POSITION) {

                        recyclerMap.get(otherUserUid).scrollToPosition(originalPosition-4);

                        highlightItem(originalPosition);

                        // when the down-arrow button on MainActivity(444) is clicked, it should check  goToLastMessage first
                        MainActivity.goToNum = chatPosition;
                        // goToLastMessage = true; then scroll to the previous message, else scroll down as usual
                        MainActivity.goToLastMessage = true;

                    }

                    lastPosition = originalPosition;    // for deleting highlight

                    // clear highlight
                    new Handler().postDelayed(()-> {
                        // reset the background o=colour
                        holder.itemView.setBackgroundColor(0);
                    }, 100);
                }, 20);


            } else
            {
                if(modelChats.getType() != K.type_pin)   // don't select if it's a pin card
                {
                    // if onLongPress mood is activated, add or remove chat from list when user click a chat
                    if(modelChats.getType() != 1 && modelChats.getType() != 2 && modelChats.getType() != 5)
                    {
                        addOrRemoveChatFromList(modelChats, chatPosition);

                    } else {
                        if( (modelChats.getType() == 1 && !modelChats.getVoiceNote().startsWith("media/voice"))
                                || ( modelChats.getPhotoUriOriginal() != null && !modelChats.getPhotoUriOriginal().startsWith("media") ))
                        {
                            addOrRemoveChatFromList(modelChats, chatPosition);
                        }  else {
                            Toast.makeText(mContext, mContext.getString(R.string.downloadFirst), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

        };
        // set pin alert and onclick
        if (modelChats.getType() != empty && modelChats.getType() != 6 && modelChats.getType() != 7)
        {
            if(modelChats.getType() == K.type_pin)   // set pin chat and scroll to chat onClick
            {
                if( modelChats.getEdit()!= null && modelChats.getEdit().equals("yes")   //  for number of new chat
                        && modelChats.getNewChatNumberID() != null && Integer.parseInt(modelChats.getNewChatNumberID()) > 0)
                {
                    String newChatNo = modelChats.getNewChatNumberID() + " " + mContext.getString(R.string.newMessage);
                    holder.pinAlertTV.setText(newChatNo);

                    if(insideChatMap.get(modelChats.getOtherUid()) != null && insideChatMap.get(modelChats.getOtherUid()))
                    {
                        Runnable runnableNewChatNum = sightedRunnableMap.get(modelChats.getIdKey());

                        // remove the previous runnable if sighted twice (in case user scroll to same position b4 it delete)
                        if(handlerNewChatNum != null && runnableNewChatNum != null) {
                            handlerNewChatNum.removeCallbacks(runnableNewChatNum);
                        }

                        runnableNewChatNum = ()->{  // create new runnable

                            chatViewModel.deleteChat(modelChats);    // delete from room

                            modelList.remove(modelChats);    // delete from list
                            notifyItemRemoved(chatPosition);
                            notifyItemRangeChanged(chatPosition, modelList.size());

                        };

                        assert handlerNewChatNum != null;
                        handlerNewChatNum.postDelayed(runnableNewChatNum, 10_000);

                        sightedRunnableMap.put(modelChats.getIdKey(), runnableNewChatNum);  // save the new runnable

                        // reset new chat count -- outside outside >> in case I am inside the chat when user send new chat
                        if(ChatsFragment.adapter != null)
                            ChatsFragment.adapter.findUserModelByUidAndResetNewChatNum(otherUserUid, K.fromChatFragment, true);
                        if(PlayersFragment.adapter != null)
                            PlayersFragment.adapter.findUserModelByUidAndResetNewChatNum(otherUserUid, K.fromPlayerFragment, true);

                    }

                } else if (modelChats.getEdit()!= null && modelChats.getEdit().equals("newDate"))
                {
                    long getTime = modelChats.getTimeSent();

                    if (TimeUtils.compareDays(getTime) == 0) {
                        holder.pinAlertTV.setText(mContext.getString(R.string.today));
                    } else if (TimeUtils.compareDays(getTime) == 1) {
                        holder.pinAlertTV.setText(mContext.getString(R.string.yesterday));
                    } else {
                        Date d = new Date(getTime);
                        @SuppressLint("SimpleDateFormat") DateFormat dateFormatter = new SimpleDateFormat("MMMM dd, yyyy");
                        String formattedDate = dateFormatter.format(d);

                        holder.pinAlertTV.setText(formattedDate);
                    }

                } else holder.pinAlertTV.setText(modelChats.getMessage());  // for user Pin a Chat

                holder.pinAlertTV.setOnClickListener(scrollToReplyChat);

            } else {    // scroll to highlight position

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
            }
        }

        // check it's on onLongPress and retain chat position highlight
        retainHighlight(chatPosition);

    }         //    =====  onBind


    // ---------------------- methods ---------------------------

    private void setColours(MessageViewHolder holder)
    {
        if(MainActivity.nightMood)
        {
            if(holder.pauseAndPlay_IV != null) holder.pauseAndPlay_IV.setImageTintList(ColorStateList
                    .valueOf(ContextCompat.getColor(mContext, R.color.cool_orange)));
            if(holder.speedTV != null) holder.speedTV.setBackgroundResource(R.drawable.cool_black_circle_round);
            if(holder.emojiOnly_TV != null) holder.emojiOnly_TV.setTextColor(ContextCompat.getColor(mContext, R.color.white));

            if(holder.textViewShowMsg != null) holder.textViewShowMsg.setTextColor(ContextCompat.getColor(mContext, R.color.white));
            if(holder.photoChatTV != null) holder.photoChatTV.setTextColor(ContextCompat.getColor(mContext, R.color.white));
            if(holder.callOrGameHeading_TV != null) holder.callOrGameHeading_TV.setTextColor(ContextCompat.getColor(mContext, R.color.white));

            if(holder.react_TV != null) holder.react_TV.setTextColor(ContextCompat.getColor(mContext, R.color.whitePure));
            if(holder.react_TV != null) holder.react_TV.setBackgroundResource(R.drawable.black_circle_round);

            if(status == receive){
                holder.chatContainer.setBackgroundResource(R.drawable.view_card_receive_dark);
                holder.linearLayoutClick.setBackgroundResource(R.drawable.reply_background_night2);
                holder.linearLayoutReplyBox.setBackgroundResource(R.drawable.reply_background_night2);
                holder.senderNameTV.setTextColor(ContextCompat.getColor(mContext, R.color.cool_orange));
                holder.imageViewOptions.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.cool_orange)));

            } else if (status == send) {
                holder.chatContainer.setBackgroundResource(R.drawable.view_card_send_dark);
                holder.linearLayoutClick.setBackgroundResource(R.drawable.reply_background_night1);
                holder.linearLayoutReplyBox.setBackgroundResource(R.drawable.reply_background_night1);
                holder.senderNameTV.setTextColor(ContextCompat.getColor(mContext, R.color.cool_orange));
                holder.imageViewOptions.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.cool_orange)));

            } else if(status == receivePhoto || status == callReceive){
                holder.chatContainer.setBackgroundResource(R.drawable.view_card_receive_dark);

            } else if(status == sendPhoto || status == callSend){
                holder.chatContainer.setBackgroundResource(R.drawable.view_card_send_dark);

            } else if(status == pinChat) {
                holder.pinAlertTV.setBackgroundColor(ContextCompat.getColor(mContext, R.color.blackApp));
                holder.pinAlertTV.setTextColor(ContextCompat.getColor(mContext, R.color.cool_orange));

            }

        } else
        {
            if(holder.pauseAndPlay_IV != null) holder.pauseAndPlay_IV.setImageTintList(ColorStateList
                    .valueOf(ContextCompat.getColor(mContext, R.color.orange)));
            if(holder.speedTV != null) holder.speedTV.setBackgroundResource(R.drawable.msg_count);

            if(holder.textViewShowMsg != null) holder.textViewShowMsg.setTextColor(ContextCompat.getColor(mContext, R.color.black));
            if(holder.photoChatTV != null) holder.photoChatTV.setTextColor(ContextCompat.getColor(mContext, R.color.black));
            if(holder.callOrGameHeading_TV != null) holder.callOrGameHeading_TV.setTextColor(ContextCompat.getColor(mContext, R.color.black));

            if(holder.emojiOnly_TV != null) holder.emojiOnly_TV.setTextColor(ContextCompat.getColor(mContext, R.color.black));
            if(holder.react_TV != null) holder.react_TV.setTextColor(ContextCompat.getColor(mContext, R.color.black2));
            if(holder.react_TV != null) holder.react_TV.setBackgroundResource(R.drawable.white_circle_bg);

            if(status == receive){
                holder.chatContainer.setBackgroundResource(R.drawable.view_card_receive_day);
                holder.linearLayoutClick.setBackgroundResource(R.drawable.reply_background_day2);
                holder.linearLayoutReplyBox.setBackgroundResource(R.drawable.reply_background_day2);
                holder.senderNameTV.setTextColor(ContextCompat.getColor(mContext, R.color.replyNa));
                holder.imageViewOptions.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.orange)));

            } else if (status == send) {
                holder.chatContainer.setBackgroundResource(R.drawable.view_card_send_day);
                holder.linearLayoutClick.setBackgroundResource(R.drawable.reply_background_day1);
                holder.linearLayoutReplyBox.setBackgroundResource(R.drawable.reply_background_day1);
                holder.senderNameTV.setTextColor(ContextCompat.getColor(mContext, R.color.black2));
                holder.imageViewOptions.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.white)));

            } else if(status == receivePhoto || status == callReceive){
                holder.chatContainer.setBackgroundResource(R.drawable.view_card_receive_day);

            } else if(status == sendPhoto || status == callSend){
                holder.chatContainer.setBackgroundResource(R.drawable.view_card_send_day);

            } else if(status == pinChat) {
                holder.pinAlertTV.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
                holder.pinAlertTV.setTextColor(ContextCompat.getColor(mContext, R.color.orange));

            }

        }

    }

    private SpannableString seperateUsernameFromText(String message, MessageModel modelChats, MessageViewHolder holder, int chatPosition)
    {
        SpannableString spannableString = new SpannableString(message);

        // Regular expression to find all usernames starting with '@'
        Pattern pattern = Pattern.compile("@\\w+");
        Matcher matcher = pattern.matcher(message);

        int lastEnd = 0;

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            // ClickableSpan for username
            ClickableSpan usernameClickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    // Handle username click, e.g., open profile
                    String username = message.substring(start, end);
                    if(MainActivity.isOnLongClick != 1) openUserProfile(username, modelChats, chatPosition, holder);
                    MainActivity.isOnLongClick =0;
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.orange)); // Set username color
                    ds.setUnderlineText(true); // Remove underline if any
                }
            };

            spannableString.setSpan(usernameClickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // ClickableSpan for the text before the username
            if (start > lastEnd) {
                ClickableSpan textClickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {

                        if(MainActivity.isOnLongClick != 1) textAndOptionsOnclick(modelChats, holder, chatPosition);
                        MainActivity.isOnLongClick =0;

                    }

                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        if(MainActivity.nightMood){
                            ds.setColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white)); // Set default text color
                        } else {
                            ds.setColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.black)); // Set default text color
                        }
                        ds.setUnderlineText(false); // Remove underline if any
                    }
                };
                spannableString.setSpan(textClickableSpan, lastEnd, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            lastEnd = end;
        }

        // ClickableSpan for the text after the last username
        if (lastEnd < message.length()) {
            ClickableSpan textClickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {

                    if(MainActivity.isOnLongClick != 1) textAndOptionsOnclick(modelChats, holder, chatPosition);
                    MainActivity.isOnLongClick =0;

                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    if(MainActivity.nightMood){
                        ds.setColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white)); // Set default text color
                    } else {
                        ds.setColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.black)); // Set default text color
                    }
                    ds.setUnderlineText(false); // Remove underline if any
                }
            };
            spannableString.setSpan(textClickableSpan, lastEnd, message.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannableString;

    }

    private void textAndOptionsOnclick(MessageModel modelChats, MessageViewHolder holder, int chatPosition)
    {
        if(modelChats.getType() != empty && modelChats.getType() != 6 && modelChats.getType() != 7 && modelChats.getType() != 8)
        {
            // process onClick if longPress is not yet activated
            if(MainActivity.chatOptionView != null && MainActivity.chatOptionView.getVisibility() != View.VISIBLE)
            {
                fragmentListener.openChatClickOption(true, modelChats, modelList.size(), chatPosition);

            } else
            {
                // if onLongPress mood is activated, add or remove chat from list when user click a chat
                if(modelChats.getType() != 1 && modelChats.getType() != 2 && modelChats.getType() != 5)
                {
                    addOrRemoveChatFromList(modelChats, chatPosition);

                } else {
                    if( (modelChats.getType() == 1 && !modelChats.getVoiceNote().startsWith("media/voice"))
                            || ( modelChats.getPhotoUriOriginal() != null && !modelChats.getPhotoUriOriginal().startsWith("media") ))
                    {
                        addOrRemoveChatFromList(modelChats, chatPosition);
                    }  else {
                        Toast.makeText(mContext, mContext.getString(R.string.downloadFirst), Toast.LENGTH_SHORT).show();
                    }
                }
                MainActivity.isOnLongClick = 0;
            }

        }
    }
    private void openUserProfile(String username, MessageModel modelChats, int chatPosition, MessageViewHolder holder)
    {
        if(MainActivity.chatOptionView != null && MainActivity.chatOptionView.getVisibility() != View.VISIBLE)
        {
            Toast.makeText(mContext, "what is username: " + username, Toast.LENGTH_SHORT).show();

        } else {
            // if onLongPress mood is activated, add or remove chat from list when user click a chat
            if(modelChats.getType() != 1 && modelChats.getType() != 2 && modelChats.getType() != 5)
            {
                addOrRemoveChatFromList(modelChats, chatPosition);

            } else {
                if( (modelChats.getType() == 1 && !modelChats.getVoiceNote().startsWith("media/voice"))
                        || ( modelChats.getPhotoUriOriginal() != null && !modelChats.getPhotoUriOriginal().startsWith("media") ))
                {
                    addOrRemoveChatFromList(modelChats, chatPosition);
                }  else {
                    Toast.makeText(mContext, mContext.getString(R.string.downloadFirst), Toast.LENGTH_SHORT).show();
                }
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

        if( isFileLessThan150Kb(uriOnPhone, mContext) && modelChats.getOtherUid().equals(myId) &&
            !modelChats.getPhotoUriOriginal().startsWith("/storage/")
                && !modelChats.getPhotoUriOriginal().startsWith("file:/"))
        {
            String imageSize = modelChats.getImageSize();
            holder.loadProgressTV.setText(imageSize);

        } else if(!modelChats.getOtherUid().equals(myId)) {
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

    private void activateOnLongClick(MessageModel modelChats, MessageViewHolder holder, int chatPosition)
    {
//        if(modelChats.getMsgStatus() != 700033){

            // send the chats details
            MainActivity.modelChatsOption = modelChats;
            MainActivity.chatPosition = chatPosition;

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
            if(positionCheck < 100 && modelChats.getFromUid().equals(myId))
            {
                MainActivity.editChatOption_IV.setImageResource(R.drawable.baseline_mode_edit_24);
                MainActivity.editChatOption_IV.setVisibility(View.VISIBLE);

            } else {
                MainActivity.editChatOption_IV.setVisibility(View.GONE);
            }

            if (modelChats.getType() != 0 || (!modelChats.getFromUid().equals(myId) && modelChats.getType() != 0) )
            {   // set image and document => type 0 is for just text-chat, type 1 is voice_note, type 2 is photo, type 3 is document, type 4 is audio (mp3)
                MainActivity.editChatOption_IV.setImageResource(R.drawable.baseline_share_24);
                MainActivity.onShare = true;
                MainActivity.editChatOption_IV.setVisibility(View.VISIBLE);
            }

            fragmentListener.setDelAndPinForWho(true, false);

            // display pin icon, is it private or public
            fragmentListener.openChatClickOption(false, modelChats, modelList.size(), chatPosition);

            // Highlight the clicked item
            View itemView = recyclerMap.get(otherUserUid).getLayoutManager().findViewByPosition(chatPosition);
            itemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.transparent_orangeLow));

            // remove previous highlight is any
            if(chatPosition != lastPosition) clearHighlight();

            // activate the highlight checker
            isOnlongPress = true;

//        } else Toast.makeText(mContext, mContext.getString(R.string.isNetwork), Toast.LENGTH_SHORT).show();

    }

    // if onLongPress mood is activated, add or remove chat from list when user click a chat
    private void addOrRemoveChatFromList(MessageModel modelChats, int chatPosition)
    {
        if(MainActivity.chatOptionView != null)
        {
            fragmentListener.setDelAndPinForWho(false, false);

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
                            MainActivity.moreOption_IV.setVisibility(View.GONE);
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

                if(MainActivity.chatModelList.size() == 0 )     // close the chatOption container if list is empty
                {
                    MainActivity.cancelChatOption();
                    fragmentListener.setDelAndPinForWho(false, true);

                } else if (MainActivity.chatModelList.size() == 1)
                {
                    MainActivity.editChatOption_IV.setVisibility(View.GONE);
                    MainActivity.replyChatOption_IV.setVisibility(View.VISIBLE);
                    MainActivity.moreOption_IV.setVisibility(View.VISIBLE);

                    MessageModel model = MainActivity.chatModelList.get(0);
                    int position = findMessagePositionById(model.getIdKey());
                    ToggleUtils.togglePinAndShareIcon(model, modelList.size(), position, myId);

                    // show if chat is pin publicly or privately
                    fragmentListener.openChatClickOption(false, MainActivity.chatModelList.get(0), 0,0);

                } else
                {
                    MainActivity.editChatOption_IV.setVisibility(View.GONE);
                    MainActivity.replyChatOption_IV.setVisibility(View.GONE);
                    MainActivity.moreOption_IV.setVisibility(View.GONE);
                }

                // Remove the background color
                if (currentColor == highlightedColor) itemView.setBackgroundColor(Color.TRANSPARENT);

            }

            // display the total number of chat selected in the List
            String totalChatSelected = MainActivity.chatModelList.size() + "";
            MainActivity.chatSelected_TV.setText(totalChatSelected);
        }
    }

//

    //  =============== sending image or document methods
    private void sendMyPhotoOrDocument(MessageModel modelChats, MessageViewHolder holder, int chatPosition){
        // get the photo uid owner and the sending state
        String[] photoCheck = MainActivity.documentIdShareRef.getString(modelChats.getIdKey(), "")
                .split(K.JOIN);
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
                onSendingSuccessful(modelChats, finalPhoto_location_original_DB, null, otherUid, null);

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
                .putString(modelChats.getIdKey(), otherUid + K.JOIN + "yes").apply();
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

                onSendingSuccessful(modelChats, finalPhoto_location_original_DB, photo_location_low_DB, otherUid, imageLinkToFBStorage);

            }).addOnFailureListener(e -> {
                loadBarVisibility(holder, modelChats);
                Toast.makeText(mContext, "Upload failed_", Toast.LENGTH_SHORT).show();
                MainActivity.documentIdShareRef.edit()
                        .putString(modelChats.getIdKey(), otherUid + K.JOIN + "yes").apply();
            });

        }).addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
            loadBarVisibility(holder, modelChats);
            Toast.makeText(mContext, "Upload failed__", Toast.LENGTH_SHORT).show();
            MainActivity.documentIdShareRef.edit()
                    .putString(modelChats.getIdKey(), otherUid + K.JOIN + "yes").apply();
            // delete the path from the app memory
            MainActivity.deleteSingleUriFromAppMemory(modelChats.getPhotoUriOriginal());
        });

    }

    private void downloadPhotoOrDocSentByOtherUser(MessageModel modelChats, MessageViewHolder holder, int chatPosition)
    {
        String pathToFirebase = modelChats.getPhotoUriOriginal();   //media/documents/AZkfy6uZunMfMUxR7HIol4rPZBq2/1708199920604winnerChatJoinPathsnull
        if(pathToFirebase.contains(K.JOIN)){ // split in case the null was not removed
            String splitPath[] = pathToFirebase.split(K.JOIN);
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

            if(photoAndVideoMap.get(modelChats.getFromUid()) == null){      // add new photo or video to all photo and video views list
                new Thread(()->MainActivity.getEachUserAllPhotoAndVideos(modelChats.getFromUid(), modelList)).start();
            } else addNewPhotoOrVideoToViewLists(modelChats.getFromUid(), modelChats);

        }).addOnFailureListener(exception -> {
            // Handle failed download
            int orangeColor = ContextCompat.getColor(mContext, R.color.orange);
            // Set the indeterminate tint color for the ProgressBar to white
            holder.progressBarLoad.setIndeterminateTintList(ColorStateList.valueOf(orangeColor));
            holder.loadProgressTV.setText(modelChats.getImageSize());
            Toast.makeText(mContext, mContext.getString(R.string.askForResend), Toast.LENGTH_SHORT).show();
        }).addOnProgressListener(snapshot -> {
            // Calculate progress percentage
            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
            trackLoadProgress(progress, holder, modelChats);
        });

    }

    public static void addNewPhotoOrVideoToViewLists(String otherUid, MessageModel modelChats)    // add new photo or video to all photo and video views list
    {
        if(modelChats.getType() == 2 || modelChats.getType() == 5)
        {
            if(!photoAndVideoMap.get(otherUid).contains(modelChats)){
                photoAndVideoMap.get(otherUid).add(modelChats);
                // add the file position
                filePositionMap.put(modelChats.getIdKey(), photoAndVideoMap.get(otherUid).size() - 1);
            }

        }
    }

    private void openPhoto(MessageModel modelChats, MessageViewHolder holder){
        holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.transparent_orange));

        new Handler().postDelayed(()->
        {
            Intent intent = new Intent(mContext, ViewImageActivity.class);
            // Put the modelList as an extra
            intent.putExtra("modelList", new ArrayList<>(photoAndVideoMap.get(modelChats.getOtherUid())));
            intent.putExtra("photoId", (modelChats.getIdKey()));  // scroll to the file position
            intent.putExtra("photoIdPosition", MainActivity.filePositionMap.get(modelChats.getIdKey()));  // scroll to the file position

            // Start the ViewPagerActivity
            mContext.startActivity(intent);

            // clear highlight
            new Handler().postDelayed(MainActivity::clearAllHighlights, 100);

        }, 10);

//        holder.itemView.animate().scaleX(1.1f).scaleY(1.1f).withEndAction(() ->
//        {
//            Intent intent = new Intent(mContext, ViewImageActivity.class);
//            // Put the modelList as an extra
//            intent.putExtra("modelList", new ArrayList<>(MainActivity.photoAndVideoMap.get(modelChats.getId())));
//            intent.putExtra("photoId", (modelChats.getIdKey()));  // scroll to the file position
//            intent.putExtra("photoIdPosition", MainActivity.filePositionMap.get(modelChats.getIdKey()));  // scroll to the file position
//
//            // Start the ViewPagerActivity
//            mContext.startActivity(intent);
//
//            // clear highlight
//            new Handler().postDelayed(()-> {
//                clearAllHighlights();
//                // Reset the scale
//                holder.itemView.setScaleX(1.0f);
//                holder.itemView.setScaleY(1.0f);
//            }, 100);
//        });

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

    private void onSendingSuccessful(MessageModel modelChats, String finalPhoto_location_original_DB,
                                     String lowPhotoOrDocLocationOn_DB, String otherUid, String imageLinkToFBStorage)
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
        if(modelChats.getType() == 2)
        {
            holder.documentContainer.setVisibility(View.GONE);
            holder.showImage.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(imageUri_).into(holder.showImage);

        } else if (modelChats.getType() == 3)
        {
            holder.showImage.setVisibility(View.GONE);
            holder.documentContainer.setVisibility(View.VISIBLE);   // make the document container visible
            holder.documentDetails_TV.setText(modelChats.getEmojiOnly());   // I used document details to replace emojiOnly
            holder.document_IV.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(imageUri_)
//                    .apply(RequestOptions.centerCropTransform())
                    .into(holder.document_IV);  // display the document thumbnail

        } else if (modelChats.getType() == 5)
        {    // it is video
            holder.documentContainer.setVisibility(View.GONE);
            holder.showImage.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(imageUri_).into(holder.showImage);
            // show the playIcon button, size and duration
            holder.playVideoIcon_IV.setVisibility(View.VISIBLE);
            holder.videoDuration_TV.setVisibility(View.VISIBLE);
            holder.videoDuration_TV.setText(modelChats.getVnDuration());

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
                    String[] splitPath = modelChats.getPhotoUriOriginal().split(K.JOIN);
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
//                                String[] splitPath = modelChats.getPhotoUriOriginal().split(K.JOIN);
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
        holder.circleDownload.setVisibility(View.GONE);
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
                otherUid + K.JOIN + "no").apply();
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
        String[] splitX = photoCheck.split(K.JOIN);
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
            holder.circleDownload.setVisibility(View.GONE);
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

        return time.toLowerCase();

//        String previousDateString = String.valueOf(d);
//        int dateLast = Integer.parseInt(previousDateString.substring(8, 10));   // 1 - 30 days
//
//        // months
//        dateMonth.put("Jan", 1);
//        dateMonth.put("Feb", 2);
//        dateMonth.put("Mar", 3);
//        dateMonth.put("Apr", 4);
//        dateMonth.put("May", 5);
//        dateMonth.put("Jun", 6);
//        dateMonth.put("Jul", 7);
//        dateMonth.put("Aug", 8);
//        dateMonth.put("Sep", 9);
//        dateMonth.put("Oct", 10);
//        dateMonth.put("Nov", 11);
//        dateMonth.put("Dec", 12);
//
//        int lastMonth = dateMonth.get(previousDateString.substring(4,7));
////        String lastYear = previousDateString.substring(32, 34);  // year
//
//        String joinTimeAndDate = time.toLowerCase() + " | " + dateLast +"/"+ lastMonth;

    }

    // arrange my map for voice_note or photo
    private Map sendMap(MessageModel modelChats, String originalUriPath, String lowImagePath,
                        String imageSize, String imageLinkToFBStorage, String vnPathToStorage)
    {
        Map<String, Object> messageMap = new HashMap<>();
        String myDisplayName = MainActivity.myProfileShareRef.getString(K.PROFILE_DISNAME, modelChats.getSenderName());

        messageMap.put("senderName", myDisplayName);
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
        messageMap.put("newChatNumberID", modelChats.getNewChatNumberID());
        messageMap.put("replyID", modelChats.getReplyID());
        messageMap.put("replyMsg", modelChats.getReplyMsg());
        messageMap.put("chatIsPin", modelChats.getChatIsPin());
        messageMap.put("chatIsForward", modelChats.getChatIsForward());
        messageMap.put("photoUriPath", imageLinkToFBStorage);
        if(modelChats.getType() == 4 || modelChats.getType() == 1) { // it's audio | voice note
            messageMap.put("photoUriOriginal", null);
        } else {
            messageMap.put("photoUriOriginal", originalUriPath + K.JOIN + lowImagePath);
        }
        messageMap.put("imageSize", imageSize);

        return messageMap;

    }

    private Map<String, Object> setOutsideChatMap(MessageModel modelChats)
    {// 700024 --- tick one msg  // 700016 -- seen msg   // 700033 -- load

        String chat = UserChatUtils.setChatText(modelChats.getType(), modelChats.getMessage(), modelChats.getEmojiOnly(), modelChats.getVnDuration(), mContext);

        Map<String, Object> latestChatMap = new HashMap<>();
        // type 0 is for just text-chat, type 1 is voice_note, type 2 is photo, type 3 is document, type 4 is audio (mp3)
        String myDisplayName = MainActivity.myProfileShareRef.getString(K.PROFILE_DISNAME, modelChats.getSenderName());
        latestChatMap.put("fromUid", myId);
        latestChatMap.put("senderName", myDisplayName);
        latestChatMap.put("emojiOnly", modelChats.getEmojiOnly());
        latestChatMap.put("message", chat);
        latestChatMap.put("type", modelChats.getType());
        latestChatMap.put("msgStatus", 0);
        latestChatMap.put( "timeSent", ServerValue.TIMESTAMP);
        latestChatMap.put("idKey", modelChats.getIdKey());


        return latestChatMap;
    }

    // send my image or voice note to other user database
    private void sendToDatabaseAndRoom(MessageModel modelChats, Map sendMap, String otherUid)
    {
        // send the chat to other user via firebase database
        refMsgFast.child(otherUid).child(myId).child(modelChats.getIdKey()).setValue( sendMap );

        // save last msg for outside chat display
        refLastDetails.child(myId).child(otherUid).updateChildren( setOutsideChatMap(modelChats) );
        refLastDetails.child(otherUid).child(myId).updateChildren( setOutsideChatMap(modelChats) );

        //  send chatKey to other User to read  -- customise later to check user OnRead settings
        refOnReadRequest.child(otherUid).child(myId).push().setValue(modelChats.getIdKey());

        // update delivery status for outSide chat
        if(ChatsFragment.adapter != null) ChatsFragment.adapter.updateDeliveryStatus(otherUid, K.fromChatFragment);
        if(PlayersFragment.adapter != null) PlayersFragment.adapter.updateDeliveryStatus(otherUid, K.fromPlayerFragment);

        // update delivery status ROOM for outside chat
        chatViewModel.updateOutsideDelivery(otherUid, 700024);

        // update inside chat ROOM DB
        chatViewModel.updatePhotoUriPath(modelChats.getIdKey(), otherUid,
                modelChats.getPhotoUriPath(), modelChats.getPhotoUriOriginal(), null, 700024);

        // notify other user
        String chat = UserChatUtils.setChatText(modelChats.getType(), modelChats.getMessage(), modelChats.getEmojiOnly(), modelChats.getVnDuration(), mContext);
        String fcmToken = otherUserFcmTokenRef.getString(otherUid, null);
        Map<String, Object> getInsideChatMap = sendMap;
        getInsideChatMap.put("message", chat);

        ChatUtils.sentChatNotification( otherUid, getInsideChatMap, fcmToken );

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


    // add emoji reaction from me or other user
    public String addEmojiReact(String emoji, String chatID, String otherId)
    {
        int chatPosition = findMessagePositionById(chatID) != -1 ? findMessagePositionById(chatID) : -1;
        // check if the chatPosition exist
        if(chatPosition != -1){
            // concat previous emoji to the new one
            String addEmoji = modelList.get(chatPosition).getEmoji() != null ?
                    modelList.get(chatPosition).getEmoji().concat(emoji): emoji;

            K.handler.post(()->{
                modelList.get(chatPosition).setEmoji(addEmoji);
                notifyItemChanged(chatPosition, new Object());
            });

            // add to ROOM database
            chatViewModel.updateChatEmoji(otherId, chatID, addEmoji);

            return modelList.get(chatPosition).getMessage() != null ? modelList.get(chatPosition).getMessage()
                    : modelList.get(chatPosition).getEmojiOnly();
        }

        return null;
    }

    public void pinIconDisplay(String messageId, boolean status){

        K.executors.execute(() -> {
            if(modelList != null){
                for (int i = modelList.size()-1; i >= 0; i--) {
                    if (modelList.get(i).getIdKey().equals(messageId)) {

                        modelList.get(i).setChatIsPin(status);
                        int finalPosition = i;
                        handler1.post(()-> notifyItemChanged(finalPosition, new Object()) );

                        // update icon on local database
                        chatViewModel.updateChat(modelList.get(i));

                        break;
                    }
                }
            }
        });

    }


    public int findMessagePositionById(String messageId)
    {
        if(modelList != null && messageId != null){
            for (int i = modelList.size()-1; i >= 0; i--)
            {
                if (modelList.get(i).getIdKey() != null) {
                    if(modelList.get(i).getIdKey().equals(messageId)) return i;
                } else {
                    chatViewModel.deleteChat(modelList.get(i));
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
        private ImageView imageViewOptions;
        private ConstraintLayout constraintMsgContainer, chatContainer;
        private LinearLayout linearLayoutReplyBox, linearLayoutClick;
        private TextView react_TV;
        private TextView replyChat_TV, senderNameTV;
        private TextView timeMsg;
        private ConstraintLayout emptyConstraint;

        // voice note
        private SeekBar seekBarProgress;
        private ImageView pauseAndPlay_IV;
        private TextView totalMinute_TV;
        private CircleImageView circleDownload;
        private ProgressBar progressBar, progressBarLoad;
        private TextView speedTV;

        // photo display
        public ImageView showImage, playVideoIcon_IV;
        TextView loadProgressTV, videoDuration_TV;

        // document display
        private ConstraintLayout documentContainer;
        private ImageView document_IV;
        private TextView documentDetails_TV;

        // call and game
        private TextView callOrGameHeading_TV;
        private TextView response_TV;
        private ImageView callOrGame_IV;

        // pin
        private TextView pinAlertTV;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            if(status == send)
            {
                timeMsg = itemView.findViewById(R.id.textViewChatTime);
                seenMsg = itemView.findViewById(R.id.imageViewSeen);
                textViewShowMsg = itemView.findViewById(R.id.senderChat_TV);
                emojiOnly_TV = itemView.findViewById(R.id.textViewSendOnlyEmoji);

                pinALL_IV = itemView.findViewById(R.id.pinALL_S_IV);
                pinIcon_IV = itemView.findViewById(R.id.pinSender_IV);
                forwardIcon_IV = itemView.findViewById(R.id.forwardS_IV);

                editNotify = itemView.findViewById(R.id.editedSender_IV);
                textViewNewMsg = itemView.findViewById(R.id.textViewNewMsg);
                chatContainer = itemView.findViewById(R.id.chatContainerS);
                constraintMsgContainer = itemView.findViewById(R.id.constraintBodySend);

                // reply
                linearLayoutReplyBox = itemView.findViewById(R.id.linearLayoutReplyBox);
                linearLayoutClick = itemView.findViewById(R.id.linearClickReply1);
                replyChat_TV = itemView.findViewById(R.id.textViewReply);
                senderNameTV = itemView.findViewById(R.id.senderNameTV);

                imageViewOptions = itemView.findViewById(R.id.imageViewOptions);
                react_TV = itemView.findViewById(R.id.reactSender_TV);

                // voice note
                seekBarProgress = itemView.findViewById(R.id.seekBarMusicProgress);
                circleDownload = itemView.findViewById(R.id.cirleDownload);
                progressBar = itemView.findViewById(R.id.progressBarS);
                pauseAndPlay_IV = itemView.findViewById(R.id.pauseAndPlay_IV);
                totalMinute_TV = itemView.findViewById(R.id.minuteTV);
                speedTV = itemView.findViewById(R.id.speedTV_S);

            } else if (status == sendPhoto || status == receivePhoto)
            {
                chatContainer = itemView.findViewById(R.id.photoContainer);
                constraintMsgContainer = itemView.findViewById(R.id.senderLayerContainer);

                // photo and progress bar
                showImage = itemView.findViewById(R.id.photoCardSender);
                loadProgressTV = itemView.findViewById(R.id.loadPhotoProgressTV);
                progressBarLoad = itemView.findViewById(R.id.progressBarLoad1);

                // video
                playVideoIcon_IV = itemView.findViewById(R.id.playVideoIcon_IV);
                videoDuration_TV = itemView.findViewById(R.id.videoDuration_TV);

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

            }  else if(status == receive)
            {
                timeMsg = itemView.findViewById(R.id.textViewChatTime2);
                textViewShowMsg = itemView.findViewById(R.id.receiverChat_TV);
                emojiOnly_TV = itemView.findViewById(R.id.textViewReceivedOnlyEmoji);
                chatContainer = itemView.findViewById(R.id.chatContainerR);

                pinALL_IV = itemView.findViewById(R.id.pinALL_R_IV);
                pinIcon_IV = itemView.findViewById(R.id.pinReceiver_IV);
                forwardIcon_IV = itemView.findViewById(R.id.forwardR_IV);

                editNotify = itemView.findViewById(R.id.editedReceiver_IV);
                textViewNewMsg = itemView.findViewById(R.id.textViewNewMsg2);
                constraintMsgContainer = itemView.findViewById(R.id.constraintBodyReceive);
                imageViewOptions = itemView.findViewById(R.id.imageViewOptions2);

                // reply
                linearLayoutReplyBox = itemView.findViewById(R.id.linearLayoutReplyBox2);
                linearLayoutClick = itemView.findViewById(R.id.linearClickReply2);
                replyChat_TV = itemView.findViewById(R.id.textViewReply2);
                senderNameTV = itemView.findViewById(R.id.senderName2);

                react_TV = itemView.findViewById(R.id.reactReceiver_TV);

                // voice note
                seekBarProgress = itemView.findViewById(R.id.seekBarMusicProgress2);
                circleDownload = itemView.findViewById(R.id.circleDownload2);
                progressBar = itemView.findViewById(R.id.progressBarR);
                pauseAndPlay_IV = itemView.findViewById(R.id.pauseAndPlay_IV2);
                totalMinute_TV = itemView.findViewById(R.id.minuteTV2);
                speedTV = itemView.findViewById(R.id.speedTV_R);

            } else if(status == empty)
            {
                constraintMsgContainer = itemView.findViewById(R.id.emptyConstraint);
            } else if(status == pinChat)
            {
                constraintMsgContainer = itemView.findViewById(R.id.pinContainer);
                pinAlertTV = itemView.findViewById(R.id.pinAlertTV);

            } else if (status == callSend || status == callReceive)
            {
                constraintMsgContainer = itemView.findViewById(R.id.callOrGameContainer);
                callOrGameHeading_TV = itemView.findViewById(R.id.callOrGameHeading_TV);
                timeMsg = itemView.findViewById(R.id.timeCall_TV);
                response_TV = itemView.findViewById(R.id.response_TV);
                callOrGame_IV = itemView.findViewById(R.id.callOrGame_IV);

                chatContainer = itemView.findViewById(R.id.chatContainer);

            }


        }

        // Getter method to access your specific view
//        public View getYourSpecificView() {
//            return seekBarProgress;
//        }
        
    }

    @Override
    public int getItemViewType(int position) {

        // type 0 - text-chat, type 1 is voice_note, type 2 is photo, type 3 is document,
        // type 4 is audio (mp3), type 5 is video, 6 is call, 7 is game, 8 is pin, 10 is empty card

        MessageModel chat = modelList.get(position);
        // check if the chat is from me via my uid
        if(chat.getFromUid().equals(myId))
        {
            if(chat.getPhotoUriPath() != null || chat.getType() == 2 || chat.getType() == 3)
            {
                status = sendPhoto;
                return sendPhoto;

            } else if(chat.getType() == K.type_empty)
            {
                status = empty;
                return empty;

            } else if(chat.getType() == 6 || chat.getType() == 7)
            {
                status = callSend;
                return callSend;

            } else if(chat.getType() == K.type_pin)
            {
                status = pinChat;
                return pinChat;

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

            } else if(chat.getType() == 10)
            {
                status = callReceive;
                return empty;

            } else if(chat.getType() == 6 || chat.getType() == 7)
            {
                status = callReceive;
                return callReceive;

            } else if(chat.getType() == K.type_pin)
            {
                status = pinChat;
                return pinChat;

            }   else
            {
                status = receive;
                return receive;
            }
        }

    }

}


