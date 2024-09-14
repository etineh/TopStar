package com.pixel.chatapp.adapters;

import static com.pixel.chatapp.home.MainActivity.contactNameShareRef;
import static com.pixel.chatapp.home.MainActivity.forwardChatUserId;
import static com.pixel.chatapp.home.MainActivity.myProfileShareRef;
import static com.pixel.chatapp.home.MainActivity.myUserName;
import static com.pixel.chatapp.home.MainActivity.newPlayerMList;
import static com.pixel.chatapp.home.MainActivity.onForward;
import static com.pixel.chatapp.home.MainActivity.onUserLongPress;
import static com.pixel.chatapp.home.MainActivity.otherUserFcmTokenRef;
import static com.pixel.chatapp.home.MainActivity.otherUserHintRef;
import static com.pixel.chatapp.home.MainActivity.selectedPlayerMList;
import static com.pixel.chatapp.home.MainActivity.selectedUserNames;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.pixel.chatapp.activities.LinearLayoutManagerWrapper;
import com.pixel.chatapp.constants.AllConstants;
import com.pixel.chatapp.home.fragments.ChatsFragment;
import com.pixel.chatapp.home.fragments.PlayersFragment;
import com.pixel.chatapp.interface_listeners.ChatListener;
import com.pixel.chatapp.interface_listeners.FragmentListener;
import com.pixel.chatapp.R;
import com.pixel.chatapp.model.AwaitPlayerM;
import com.pixel.chatapp.photos.ZoomImage;
import com.pixel.chatapp.chats.MessageAdapter;
import com.pixel.chatapp.home.MainActivity;
import com.pixel.chatapp.model.MessageModel;
import com.pixel.chatapp.model.UserOnChatUI_Model;
import com.pixel.chatapp.utils.AnimUtils;
import com.pixel.chatapp.utils.ProfileUtils;
import com.pixel.chatapp.utils.UserChatUtils;
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

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>  {

    public final List<UserOnChatUI_Model> userModelList;

    private final Context mContext;
    private final Activity activity;

    private final DatabaseReference referenceUsers;
    private final DatabaseReference refUsersLast;
    private final DatabaseReference referenceCheck;
    private final DatabaseReference refChatList;
    private final DatabaseReference refChat;
    private final DatabaseReference refMsgFast;
    private final DatabaseReference refPinPublic;
    private final DatabaseReference refPinPrivate;
    FirebaseUser user;
    Map<String, Object> offlinePresenceAndStatus;
    Map<String, Integer> dateMonth, dateNum;
//    Map<String, ValueEventListener> typeValueListenerMap;
    private boolean typeListenerActivated;
    public List<String> otherUidLongPressList = new ArrayList<>();

    private Handler handler = new Handler(Looper.getMainLooper());
    private List<View> viewClickList = new ArrayList<>();
    public View previousView;

    private FragmentListener listener;
    private ChatListener chatListener;

    public void setFragmentListener(FragmentListener listener) {
        this.listener = listener;
    }

    public void setChatListener(ChatListener chatListener) {
        this.chatListener = chatListener;
    }

    // =======  constructor
    public ChatListAdapter(List<UserOnChatUI_Model> userModelList, Context mContext, Activity activity)
    {
        this.userModelList = userModelList;
        this.mContext = mContext;
        this.activity = activity;
//        MainActivity.myHolder_ = new ArrayList<>();

        ContactAdapter.setmContext(mContext);

        user = FirebaseAuth.getInstance().getCurrentUser();
        referenceCheck = FirebaseDatabase.getInstance().getReference("Checks");
        referenceUsers = FirebaseDatabase.getInstance().getReference("Users");
        refUsersLast = FirebaseDatabase.getInstance().getReference("UsersList");
        refChatList = FirebaseDatabase.getInstance().getReference("ChatList");
        refChat = FirebaseDatabase.getInstance().getReference("Messages");
        refPinPrivate = FirebaseDatabase.getInstance().getReference("PinChatPrivate");
        refPinPublic = FirebaseDatabase.getInstance().getReference("PinChatPublic");
        refMsgFast = FirebaseDatabase.getInstance().getReference("MsgFast");

        offlinePresenceAndStatus = new HashMap<>();
        dateMonth = new HashMap<>();
        dateNum = new HashMap<>();
    }


    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.userlist_card, parent, false);

        return new ChatViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {

        setColours(holder);

        // reset data
        holder.textViewMsgCount.setText(null);
        holder.textViewMsgCount.setVisibility(View.INVISIBLE);
        holder.textViewTyping.setText(null);
        holder.imageView.setImageResource(R.drawable.person_round);
        holder.imageViewDeliver.setImageResource(0);
        holder.textViewMsg.setText(null);
        holder.textViewUser.setText("");
//        holder.imageView.setImageResource(0);

        holder.checkBoxContainer.setVisibility(View.GONE);
        holder.checkBoxToWho.setChecked(false);

//        holder.textViewMsg.setTextAppearance(android.R.style.TextAppearance);

        int position_ = position;
        String otherUid = userModelList.get(position_).getOtherUid();
        String emojiOnly = userModelList.get(position_).getEmojiOnly();
        String message = userModelList.get(position_).getMessage();
        String otherUserName = userModelList.get(position_).getOtherUserName();
        String otherDisplayName = userModelList.get(position_).getOtherDisplayName();
        String otherContactName = userModelList.get(position_).getOtherContactName();
        String imageLink = userModelList.get(position_).getImageUrl();
        int type = userModelList.get(position_).getType();
        int newChatNumbers = userModelList.get(position_).getNumberOfNewChat();
        int msgStatus = userModelList.get(position_).getMsgStatus();
//        String imgUrl = "https://firebasestorage.googleapis.com/v0/b/chatapp-9b7ce.appspot.com/o/images%2Fcd89b442-735a-4cae-8da0-2b7f36817e17.jpg?alt=media&token=21b05768-df6e-4be1-b9a2-12d5f944c641";
        long timeSent = userModelList.get(position_).getTimeSent();


        if(MainActivity.onForward || MainActivity.sharingPhotoActivated || MainActivity.onSelectNewPlayer){
            holder.checkBoxContainer.setVisibility(View.VISIBLE);
            if(forwardChatUserId.contains(otherUid)) holder.checkBoxToWho.setChecked(true);
        }

        // set display name
        String otherName__ = contactNameShareRef.getString(otherUid, otherDisplayName != null ? otherDisplayName : "@"+otherUserName);

        holder.textViewUser.setText(otherName__);     //set user name

        if (imageLink != null && !imageLink.isEmpty()) {
            Picasso.get().load(imageLink).into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.person_round);
        }

        // set last message
        if(message != null)
        {
//            AnimUtils.fadeInVisible(holder.textViewMsg, 300);
            holder.textViewMsg.setText(message);

            if(type == AllConstants.type_call || type ==AllConstants.type_game) // add colour if on call or game
            {
                if(message.contains(mContext.getString(R.string.incomingAudioCall)) || message.contains(mContext.getString(R.string.incomingVideoCall))
                        || message.contains(mContext.getString(R.string.ongoingCall)) || message.contains(mContext.getString(R.string.ringing)) )
                {
                    holder.textViewMsg.setTextColor(ContextCompat.getColor(mContext, R.color.orange));
                }
            }
        } else if(emojiOnly != null) {
            holder.textViewMsg.setText(emojiOnly);
        }

        // set the date and time last chat was sent
        timeAndDateSent(timeSent, holder);

        // set msgStatus if last chat was from own user
        int deliveryIcon = R.drawable.message_load;
        if(msgStatus == 700024){   // delivery
            deliveryIcon = R.drawable.message_tick_one;
        } else if (msgStatus == 700016) {  // read
            deliveryIcon = R.drawable.baseline_grade_24;
        } else if(msgStatus == 0) {
            deliveryIcon = 0;
        }
        holder.imageViewDeliver.setImageResource(deliveryIcon);

        // set number of new number
        if(newChatNumbers > 0){
            holder.textViewMsgCount.setVisibility(View.VISIBLE);
            holder.imageViewDeliver.setImageResource(0);
            holder.textViewMsgCount.setText(String.valueOf(newChatNumbers));
        }

        // set checkbox when scrolling, if user is among the onLongPress
        if(otherUidLongPressList.contains(otherUid)){
            holder.checkBoxContainer.setVisibility(View.VISIBLE);
            holder.checkBoxToWho.setChecked(true);
        }


        //  ----------- call methods    ---------------------

        getTypingState(holder, otherUid);      // show when other user is typing

        updateNameAndPhoto(holder, otherUid, position_);

        // send all recyclerView to mainActivity just once and call the getMessage to load message to it
        if(MainActivity.loadMsg){
            try{
                String myUsername_ = myProfileShareRef.getString(AllConstants.PROFILE_USERNAME, "@" + MainActivity.getMyUserName);

                listener.sendRecyclerView(holder.recyclerChat, otherUid);
                listener.getMessage(myUsername_, otherUid, mContext, false);

            } catch (Exception e){
                System.out.println("what is Error (ChatListAdapter L220)"+ e.getMessage());
            }

            if(position == 0){
//                new Handler().postDelayed(()-> {
                    for (Map.Entry<String, MessageAdapter> entry : MainActivity.adapterMap.entrySet()) {
                        String firstKey = entry.getKey();
                        MainActivity.adapterMap.get(firstKey).addLayoutViewInBackground();
                    }
//                }, 2000);
            }


            if(position_ == getItemCount() - 1){
                MainActivity.loadMsg = false;    // stop the getVIew method from loading at every instance
//                listener.firstCallLoadPage(otherUid);   // call the method to load the all message
            }

        }

        // receiving for new user the first time
        if(MainActivity.allUsersFromRoom != null && MainActivity.allUsersFromRoom.contains(userModelList.get(position_)))
        {
            String myUsername_ = myProfileShareRef.getString(AllConstants.PROFILE_USERNAME, "@" + MainActivity.getMyUserName);
            listener.sendRecyclerView(holder.recyclerChat, otherUid);
            listener.getMessage(myUsername_, otherUid, mContext, true);
            System.out.println("checking loadMsg from CHLAdapter L270");

        }


        // -------- open user chat box on MainActivity
        holder.itemView.setOnClickListener(v ->
        {
            String otherDisplayName__ = userModelList.get(position_).getOtherDisplayName();
            String otherUserName__ = userModelList.get(position_).getOtherUserName();
            String imageLink__ = userModelList.get(position_).getImageUrl();
            String otherId = userModelList.get(position_).getOtherUid();

            // get the contact name or displayed name of other user
            String getUserName = contactNameShareRef.getString(otherId, otherDisplayName__ != null ? otherDisplayName__ : "@"+otherUserName__);

            String myUsername_ = myProfileShareRef.getString(AllConstants.PROFILE_USERNAME, "@" + MainActivity.getMyUserName);
            String myDisplayName = myProfileShareRef.getString(AllConstants.PROFILE_DISNAME, null);

            // make previous view clickable if any
//            if(previousView != null && previousView != v) {
//                previousView.setClickable(true);
//                previousView.setBackgroundColor(((ColorDrawable) v.getBackground()).getColor());
//            }

//            MyFirebaseMessagingService firebaseMessagingService = new MyFirebaseMessagingService();
//            firebaseMessagingService.showNotification(mContext, "New chat", "How are you", null);

            if (MainActivity.getLastTimeChat != null)
                MainActivity.getLastTimeChat.put(otherId, userModelList.get(position_).getTimeSent());    // enable add up new time card

            if(!MainActivity.onForward && !MainActivity.onUserLongPress && !MainActivity.onSelectNewPlayer)
            {
                // open the user chats
                try {
                    listener.chatBodyVisibility(getUserName, imageLink__, myUsername_, otherUid, mContext, holder.recyclerChat);

                    listener.getLastSeenAndOnline(otherUid, mContext);

                    listener.msgBackgroundActivities(otherUid);

                    listener.callAllMethods(otherUid, mContext, activity, false);

                } catch (Exception e){

                    MainActivity.readDatabase = 0;
                    listener.sendRecyclerView(holder.recyclerChat, otherUid);
                    listener.getMessage(myUserName, otherUid, mContext, false);

                    listener.chatBodyVisibility(getUserName, imageLink__, myUsername_, otherUid, mContext, holder.recyclerChat);

                    listener.getLastSeenAndOnline(otherUid, mContext);

                    listener.msgBackgroundActivities(otherUid);

                    listener.callAllMethods(otherUid, mContext, activity, false);


                    MainActivity.offMainDatabase();
                    Toast.makeText(mContext, mContext.getString(R.string.app_name), Toast.LENGTH_SHORT).show();
                    System.out.println("What is Error occur - WinnerChat Toast (ChatListAdapter L222)" + e.getMessage());
                }

                holder.checkBoxContainer.setVisibility(View.GONE);  // close forward chat checkbox if visible by bug

                // don't show anim if it contain
//                if(viewClickList.contains(v)){
//                    // open the user chats
//                    try {
//                        listener.chatBodyVisibility(getUserName, imageLink__, myUsername_, otherUid, mContext, holder.recyclerChat);
//
//                        listener.getLastSeenAndOnline(otherUid, mContext);
//
//                        listener.msgBackgroundActivities(otherUid);
//
//                        listener.callAllMethods(otherUid, mContext, activity);
//
//                        holder.textViewMsgCount.setVisibility(View.INVISIBLE);
//
//                    } catch (Exception e){
//
//                        MainActivity.readDatabase = 0;
//                        listener.sendRecyclerView(holder.recyclerChat, otherUid);
//                        listener.getMessage(myUserName, otherUid, mContext, false);
//
//                        listener.chatBodyVisibility(getUserName, imageLink__, myUsername_, otherUid, mContext, holder.recyclerChat);
//
//                        listener.getLastSeenAndOnline(otherUid, mContext);
//
//                        listener.msgBackgroundActivities(otherUid);
//
//                        listener.callAllMethods(otherUid, mContext, activity);
//
//                        holder.textViewMsgCount.setVisibility(View.INVISIBLE);
//
//                        MainActivity.offMainDatabase();
//                        Toast.makeText(mContext, mContext.getString(R.string.app_name), Toast.LENGTH_SHORT).show();
//                        System.out.println("Error occur - WinnerChat Toast (ChatListAdapter L222)" + e.getMessage());
//                    }
//
//                } else
//                {
//                    previousView = v;   // to enable the view clickable via MainActivity @onBackPress
//
//                    int originalColor;  // Default color
//                    if (v.getBackground() instanceof ColorDrawable) {
//                        originalColor = ((ColorDrawable) v.getBackground()).getColor();
//                    } else {
//                        originalColor = 0;
//                    }
//                    v.setBackgroundColor(mContext.getResources().getColor(R.color.transparent_orangeLow));
//                    // set it false so it doesn't click and change colour
//                    v.setClickable(false);
//                    // Apply custom animation (e.g., scale)
//                    v.animate().scaleX(1.1f).scaleY(1.1f)
////                    .setDuration(50)
//                            .withEndAction(() -> {
//
//                                if(!MainActivity.onForward){
//
//                                    try {
//
//                                        listener.chatBodyVisibility(getUserName, imageLink__, myUsername_, otherUid, mContext, holder.recyclerChat);
//
//                                        listener.getLastSeenAndOnline(otherUid, mContext);
//
//                                        listener.msgBackgroundActivities(otherUid);
//
//                                        listener.callAllMethods(otherUid, mContext, activity);
//
//                                        holder.textViewMsgCount.setVisibility(View.INVISIBLE);
//
//                                    } catch (Exception e){
//
//                                        MainActivity.readDatabase = 0;
//                                        listener.sendRecyclerView(holder.recyclerChat, otherUid);
//                                        listener.getMessage(myUsername_, otherUid, mContext, false);
//
//                                        listener.chatBodyVisibility(getUserName, imageLink__, myUsername_, otherUid, mContext, holder.recyclerChat);
//
//                                        listener.getLastSeenAndOnline(otherUid, mContext);
//
//                                        listener.msgBackgroundActivities(otherUid);
//
//                                        listener.callAllMethods(otherUid, mContext, activity);
//
//                                        holder.textViewMsgCount.setVisibility(View.INVISIBLE);
//
//                                        MainActivity.offMainDatabase();
//                                        Toast.makeText(mContext, "WinnerChats...", Toast.LENGTH_SHORT).show();
//                                        System.out.println("Error occur - WinnerChat Toast (ChatListAdapter L222)" + e.getMessage());
//                                    }
//
//                                }
//
//                                v.setBackgroundColor(originalColor);
//
//                                // Reset the scale
//
//                                v.setScaleX(1.0f);
//                                v.setScaleY(1.0f);
//
//                            }).start();
//
//                    viewClickList.add(v);
//                    previousView = v;   // to enable the view clickable via MainActivity @onBackPress
//                }

            } else // check if user is forwarding chat or onUserLongPress
            {
                if(holder.checkBoxContainer.getVisibility() != View.VISIBLE)
                {
                    if(onForward){
                        holder.checkBoxToWho.setChecked(true); //
                        holder.checkBoxContainer.setVisibility(View.VISIBLE);
                        String playerName = otherDisplayName__ != null ? otherDisplayName__ : "@"+otherUserName__;
                        activateForwardCheckBox(holder, otherId, imageLink__, playerName);
                    }

                    if(onUserLongPress) {
                        UserOnChatUI_Model userModel = userModelList.get(position_);
                        activateOnUserLongPress(holder, userModel);
                    }

                }
            }

        });

        //   ===========   onLongPress   ===========================
        holder.itemView.setOnLongClickListener(v ->
        {
            UserOnChatUI_Model userModel = userModelList.get(position_);

            activateOnUserLongPress(holder, userModel);

            return true;
        });


        // view image   ===========   onClicks    ===========================
        holder.imageView.setOnClickListener(view -> {
            Intent i = new Intent(mContext, ZoomImage.class);
            i.putExtra("otherName", otherUserName);
            i.putExtra("imageLink", imageLink);
            i.putExtra("from", "chatList");
            mContext.startActivity(i);
        });


        // forward option   -- checkbox
        holder.checkBoxToWho.setOnClickListener(view ->
        {
            if(onUserLongPress)
            {
                holder.checkBoxToWho.setChecked(false);
                holder.checkBoxContainer.setVisibility(View.GONE);

                UserOnChatUI_Model userModel = userModelList.get(position_);

                otherUidLongPressList.remove(userModel.getOtherUid());

                listener.onLongPressUser(userModel);
            } else if(onForward){
                String playerName = otherDisplayName != null ? otherDisplayName : "@"+otherUserName;
                activateForwardCheckBox(holder, otherUid, imageLink, playerName);
            }
            else {
                holder.checkBoxToWho.setChecked(false);
                holder.checkBoxContainer.setVisibility(View.GONE);
            }

        });

        // forward option   -- checkbox Container
        holder.checkBoxContainer.setOnClickListener(view ->
        {
            if(onForward)
            {
                CheckBox checkBox = holder.checkBoxToWho;
                String otherName = holder.textViewUser.getText().toString();    // get user username
                String playerName = otherDisplayName != null ? otherDisplayName : "@"+otherUserName;

                if(checkBox.isChecked()) {
                    if(MainActivity.onSelectNewPlayer) {
                        checkBox.setChecked(true);
                        Toast.makeText(mContext, mContext.getString(R.string.cantRemovePlayer), Toast.LENGTH_SHORT).show();
                    } else {

                        MainActivity.selectCount--;
                        checkBox.setChecked(false);

                        forwardChatUserId.removeIf(name -> name.equals(otherUid));
                        selectedUserNames.removeIf(name -> name.equals(otherName));
                        selectedPlayerMList.removeIf(name -> name.getPlayerUid().equals(otherUid));  // Add imageUri to the List
                    }

                } else {
                    MainActivity.selectCount++;
                    checkBox.setChecked(true);

                    String safeImageLink = imageLink != null ? imageLink : "null"; // Provide a default value or handle null
                    if(MainActivity.onSelectNewPlayer){
                        newPlayerMList.add(new AwaitPlayerM(safeImageLink, playerName, otherUid, "signal", false));
                        chatListener.openAddPlayerLayout(ProfileUtils.getOtherDisplayOrUsername(otherUid, playerName));
                    } else {
                        selectedPlayerMList.add(new AwaitPlayerM(safeImageLink, playerName, otherUid, "signal", false) );
                    }
                    if( !forwardChatUserId.contains(otherUid)) forwardChatUserId.add(otherUid);  // Add other user id to the List
                    if( !selectedUserNames.contains(otherUid)) selectedUserNames.add(otherName);  // Add username to the List

                }

                if(forwardChatUserId.size() > 0 && !MainActivity.onSelectNewPlayer){           //  make send button invisible
                    String totalUser = MainActivity.selectCount + " " + mContext.getString(R.string.selected);
                    MainActivity.totalUser_TV.setText(totalUser);
                    MainActivity.circleForwardSend.setVisibility(View.VISIBLE);
                } else {
                    MainActivity.circleForwardSend.setVisibility(View.INVISIBLE);
                    MainActivity.totalUser_TV.setText(null);
                }

            }else if(onUserLongPress)
            {
                UserOnChatUI_Model userModel = userModelList.get(position_);

                activateOnUserLongPress(holder, userModel);

            } else {
                holder.checkBoxToWho.setChecked(false);
                holder.checkBoxContainer.setVisibility(View.GONE);
            }

        });

        offlineActivity(otherUid);

    }

    //      --------- methods -----------
    private void setColours(ChatViewHolder holder)
    {
        if(MainActivity.nightMood){
            holder.textViewMsg.setTextColor(ContextCompat.getColor(mContext, R.color.defaultWhite));
            holder.userBackground.setBackgroundResource(R.drawable.user_card_dark);
            holder.checkBoxToWho.setBackgroundColor(ContextCompat.getColor(mContext, R.color.blackApp));
            holder.textViewUser.setTextColor(ContextCompat.getColor(mContext, R.color.white));

        } else {
            holder.textViewMsg.setTextColor(ContextCompat.getColor(mContext, R.color.defaultBlack));
            holder.userBackground.setBackgroundResource(R.drawable.user_card_day);
            holder.checkBoxToWho.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
            holder.textViewUser.setTextColor(ContextCompat.getColor(mContext, R.color.black));

        }

    }

    private void activateOnUserLongPress(ChatViewHolder holder, UserOnChatUI_Model userModel )
    {
        holder.checkBoxContainer.setVisibility(View.VISIBLE);
        CheckBox checkBox = holder.checkBoxToWho;

        if(checkBox.isChecked()){
            checkBox.setChecked(false);
            holder.checkBoxContainer.setVisibility(View.GONE);
            otherUidLongPressList.remove(userModel.getOtherUid());
        } else {
            otherUidLongPressList.add(userModel.getOtherUid());
            checkBox.setChecked(true);
        }
        listener.onLongPressUser(userModel);

//        System.out.println("what is adapter: " +ChatsFragment.adapter.userModelList.size() + " most recent " + PlayersFragment.adapter.userModelList.size());
    }

    private void activateForwardCheckBox(ChatViewHolder holder, String otherUid, String imageLink, String playerName){
        CheckBox checkBox = holder.checkBoxToWho;
        checkBox.setChecked(checkBox.isChecked()); // Toggle the checked state
        String otherName = holder.textViewUser.getText().toString();    // get user username

        if(checkBox.isChecked())
        {
            MainActivity.selectCount++;
            if (!forwardChatUserId.contains(otherUid)) forwardChatUserId.add(otherUid);
            if (!selectedUserNames.contains(otherUid)) selectedUserNames.add(otherName);  // Add username to the List

            String safeImageLink = imageLink != null ? imageLink : "null"; // Provide a default value or handle null
            if(MainActivity.onSelectNewPlayer){
                newPlayerMList.add(new AwaitPlayerM(safeImageLink, playerName, otherUid, "signal", false));
                chatListener.openAddPlayerLayout(ProfileUtils.getOtherDisplayOrUsername(otherUid, playerName));
            } else {
                selectedPlayerMList.add(new AwaitPlayerM(safeImageLink, playerName, otherUid, "signal", false));
            }

        } else {
            if(MainActivity.onSelectNewPlayer) {
                checkBox.setChecked(true);
                Toast.makeText(mContext, mContext.getString(R.string.cantRemovePlayer), Toast.LENGTH_SHORT).show();
            } else {
                MainActivity.selectCount--;
                forwardChatUserId.removeIf(name -> name.equals(otherUid)); // remove user id
                selectedUserNames.removeIf(name -> name.equals(otherName)); // remove user name
                selectedPlayerMList.removeIf(name -> name.getPlayerUid().equals(otherUid));  // Add imageUri to the List
            }

        }


        if(forwardChatUserId.size() > 0 && !MainActivity.onSelectNewPlayer) {           //  make send button invisible
            MainActivity.totalUser_TV.setText("" + MainActivity.selectCount + " selected");
            MainActivity.circleForwardSend.setVisibility(View.VISIBLE);
        } else {
            MainActivity.totalUser_TV.setText(null);
            MainActivity.circleForwardSend.setVisibility(View.INVISIBLE);
        }
    }

    // update failed status to delivery status when network is okay
    public void updateDeliveryStatus(String otherUid, String from){
        AllConstants.executors.execute(()->
        {
            if(userModelList != null){
                for (int i = userModelList.size() - 1; i >= 0; i--)
                {
                    final int position = i;
                    if (userModelList.get(position).getOtherUid().equals(otherUid)) {
                        // Store the item in a temporary variable.
                        UserOnChatUI_Model getUser = userModelList.get(position);
                        // check the delivery status and update
                        if(getUser.getMsgStatus() == 700033){
                            getUser.setMsgStatus(700024);
                            // update the UI
                            AllConstants.handler.post(() ->{
                                if(from.equals(AllConstants.fromChatFragment)) ChatsFragment.newInstance().notifyItemChanged(position);
                                if(from.equals(AllConstants.fromPlayerFragment)) PlayersFragment.newInstance().notifyItemChanged(position);
                            });

                            // update the firebase
                            refUsersLast.child(user.getUid()).child(otherUid).child("msgStatus").setValue(700024);

                        }
                    }
                }
            }

        });
    }

    public void updateDeliveryToRead(String otherUid, String from){
        if(userModelList != null){
            for (int i = userModelList.size() - 1; i >= 0; i--)
            {
                final int position = i;
                if (userModelList.get(position).getOtherUid().equals(otherUid)) {
                    // Store the item in a temporary variable.
                    UserOnChatUI_Model getUser = userModelList.get(position);
                    // check the delivery status and update
                    if(getUser.getMsgStatus() != 700016){
                        // update the list
                        getUser.setMsgStatus(700016);
                        // update the UI
                        AllConstants.handler.post(() ->{
                            if(from.equals(AllConstants.fromChatFragment)) ChatsFragment.newInstance().notifyItemChanged(position);
                            if(from.equals(AllConstants.fromPlayerFragment)) PlayersFragment.newInstance().notifyItemChanged(position);
                        });

                        // update the firebase
                        refUsersLast.child(user.getUid()).child(otherUid)
                                .child("msgStatus").setValue(700016);

                    }
                }
            }
        }
    }

    //  update outside chat adapter for call or game     //  update ROOM outside database too
    public void updateCallOrGameUI(int type, String otherUID, String myUid, String emojiOnly, boolean onChat, boolean onPlayer)
    {
        if(userModelList != null)
        {
            for (int i = userModelList.size() - 1; i >= 0; i--)
            {
                String otherId = userModelList.get(i).getOtherUid();
                String myId = userModelList.get(i).getMyUid();
                if(otherId.equals(otherUID) && myId.equals(myUid))
                {
                    String headingMsg = userModelList.get(i).getMessage();
                    String chat = UserChatUtils.setChatText(type, headingMsg, emojiOnly, null, mContext);

                    userModelList.get(i).setMessage(chat);
                    userModelList.get(i).setEmojiOnly(emojiOnly);

                    // notify the adapter of the item changes - outside chat
                    if(onChat) ChatsFragment.newInstance().notifyItemChanged(i);
                    if(onPlayer) PlayersFragment.newInstance().notifyItemChanged(i);

                    // update outside chat in ROOM
                    MainActivity.chatViewModel.updateUserCallOrGame(otherUID, myId, chat);
                    refUsersLast.child(user.getUid()).child(otherUID).child("message").setValue(chat);

                }
            }
        }
    }

    public UserOnChatUI_Model findUserModelByUid(String otherID)
    {
        if (userModelList != null) {
            for (int i = 0; i < userModelList.size(); i++) {
                if (userModelList.get(i).getOtherUid().equals(otherID))
                {
                    return userModelList.get(i);
                }
            }
        }
        return null; // Return null if no matching user is found

    }

    public void findUserModelByUidAndResetNewChatNum(String otherId, String fromFragment, boolean databaseUpdate)   // reset to zero
    {
        if (userModelList != null) {
            for (int i = 0; i < userModelList.size(); i++) {
                if (userModelList.get(i).getOtherUid().equals(otherId))
                {
                    userModelList.get(i).setNumberOfNewChat(0);  // reset it to 0

                    final int position = i;
                    if(fromFragment.equals(AllConstants.fromChatFragment)) {
                        AllConstants.handler.post(()-> ChatsFragment.newInstance().notifyItemChanged(position) );
                    }
                    if(fromFragment.equals(AllConstants.fromPlayerFragment)) {
                        AllConstants.handler.post(()-> PlayersFragment.newInstance().notifyItemChanged(position) );
                    }

                    MainActivity.chatViewModel.updateUser(userModelList.get(position));     // update room db

                    if(databaseUpdate) refUsersLast.child(user.getUid()).child(otherId).child("numberOfNewChat").setValue(0);

                    break;
                }
            }
        }

    }


    private void timeAndDateSent(long lastTime, ChatViewHolder holder){

        // current date and time
        Timestamp stamp = new Timestamp(System.currentTimeMillis());
        Date date = new Date(stamp.getTime());
        String currentDateString = String.valueOf(date);

        // last user date and time
        Date d = new Date(lastTime);    // convert the timestamp to current time
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

        int curMonth = dateMonth.get(currentDateString.substring(4,7));    // Months
        int lastMonth = dateMonth.get(previousDateString.substring(4,7));

        int curDay = dateNum.get(currentDateString.substring(0,3));         // Mon - Sun
        int lastDay = dateNum.get(previousDateString.substring(0,3));

        String lastDayString = previousDateString.substring(0,3);   // get the day string

        int dateCur = Integer.parseInt(currentDateString.substring(8, 10));    // day 1 - 30
        int dateLast = Integer.parseInt(previousDateString.substring(8, 10));

        if (curMonth - lastMonth == 0)
        {
            if (dateCur - dateLast < 7)
            {
                if(curDay - lastDay == 0)
                {
                    holder.dateTime_TV.setText(time.toLowerCase());

                } else if (curDay - lastDay == 1)
                {
                    holder.dateTime_TV.setText(mContext.getString(R.string.yesterday));

                } else if (curDay - lastDay == 2)
                {
                    holder.dateTime_TV.setText(mContext.getString(R.string.day2ago));
//                    holder.dateTime_TV.setText(time.toLowerCase());

                } else if (curDay - lastDay == 3)
                {
                    holder.dateTime_TV.setText(mContext.getString(R.string.day3ago));
//                    holder.dateTime_TV.setText(time.toLowerCase());

                } else if (curDay - lastDay == 4)
                {
                    holder.dateTime_TV.setText(mContext.getString(R.string.day4ago));
//                    holder.dateTime_TV.setText(time.toLowerCase());

                } else if (curDay - lastDay == 5)
                {
                    holder.dateTime_TV.setText(mContext.getString(R.string.day5ago));
//                    holder.dateTime_TV.setText(time.toLowerCase());

                } else if (curDay - lastDay == 6)
                {
                    holder.dateTime_TV.setText(mContext.getString(R.string.day6ago));
//                    holder.dateTime_TV.setText(time.toLowerCase());
                }

            } else if (dateCur - dateLast >= 7 && dateCur - dateLast < 14)
            {
                holder.dateTime_TV.setText(mContext.getString(R.string.week1ago));

            } else if (dateCur - dateLast >= 14 && dateCur - dateLast < 21)
            {
                holder.dateTime_TV.setText(mContext.getString(R.string.week2ago));

            } else if (dateCur - dateLast >= 21 && dateCur - dateLast < 27)
            {
                holder.dateTime_TV.setText(mContext.getString(R.string.week3ago));

            } else {
                holder.dateTime_TV.setText(mContext.getString(R.string.monthsAgo));
            }

        } else
        {
            String lastD = dateLast +"/"+ lastMonth+"/"+ lastYear;
            holder.dateTime_TV.setText(lastD);
        }

    }


    // get user typing state
    private void getTypingState(ChatViewHolder holder, String otherUid){
//// Bug ("typing" reflecting on previous position) -- solved by starting ref with user.getUid() and add the rest child to onDataChange
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                try {
                    long typing = snapshot.child(otherUid).child("typing").exists() ?
                            (long) snapshot.child(otherUid).child("typing").getValue() : -1;

                    if(typing == 0)
                    {
//                            AnimUtils.fadeInVisible(holder.textViewMsg, 300);
                        holder.textViewTyping.setVisibility(View.GONE);
                        holder.textViewMsg.setVisibility(View.VISIBLE);

                    } else if(typing == 1){
                        AnimUtils.fadeInVisible(holder.textViewTyping, 300);
                        holder.textViewMsg.setVisibility(View.INVISIBLE);
                        holder.textViewTyping.setText(mContext.getString(R.string.typing));
                    }

                } catch (Exception e){
                    System.out.println("what is error ChatListAdap L880 " + e.getMessage());
                }
//
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        referenceCheck.child(user.getUid()).removeEventListener(valueEventListener);
        referenceCheck.child(user.getUid()).addValueEventListener(valueEventListener);

    }

    //  get other user name and photo
    private void updateNameAndPhoto(ChatViewHolder holder, String otherUid, int position)
    {
//        referenceUsers.keepSynced(true);
        referenceUsers.child(otherUid).child("general").addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                AllConstants.executors.execute(()->
                {
                    if(position < userModelList.size())
                    {
                        // Will later change it to Display Names
                        String otherUsername = snapshot.child("userName").getValue().toString();

                        String otherDisplayName = snapshot.child("displayName").exists()
                                && !snapshot.child("displayName").getValue().toString().isEmpty()
                                ? snapshot.child("displayName").getValue().toString() : null;

                        String otherContactName = contactNameShareRef.getString(otherUid, otherDisplayName);

                        String otherFcmToken = !snapshot.child("fcmToken").exists() ? null :
                                snapshot.child("fcmToken").getValue().toString();

                        otherUserFcmTokenRef.edit().putString(otherUid, otherFcmToken).apply();

                        String hint = !snapshot.child("hint").exists() ? mContext.getString(R.string.hint2) :
                                snapshot.child("hint").getValue().toString();

                        otherUserHintRef.edit().putString(otherUid, hint).apply();

                        // set users image
                        String imageUrl = snapshot.child("image").exists()
                                && !snapshot.child("image").getValue().toString().equals("null")
                                && !snapshot.child("image").getValue().toString().isEmpty()
                                ? snapshot.child("image").getValue().toString() : null;

                        AllConstants.handler.post(()->
                        {
                            if(otherContactName != null){
                                holder.textViewUser.setText(otherContactName);     //set users contact name
                            } else {
                                holder.textViewUser.setText("@"+otherUsername);     //set users username name
                            }

                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Picasso.get().load(imageUrl).into(holder.imageView);

                            } else {
                                holder.imageView.setImageResource(R.drawable.person_round);
                            }
                        });


                        userModelList.get(position).setOtherUserName(otherUsername);
                        userModelList.get(position).setOtherDisplayName(otherDisplayName);
                        userModelList.get(position).setImageUrl(imageUrl);

                        MainActivity.chatViewModel.updateOtherNameAndPhoto(otherUid, otherUsername, otherDisplayName, otherContactName, imageUrl);

                    }

//                    // save the contact name to my database
//                    referenceCheck.child(user.getUid()).child(otherUid).child("contactName")
//                            .setValue(otherContactName != null ? otherContactName : otherUsername);

                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void offlineActivity(String otherUid){
        new Thread(() -> {
            // set user "typing" to be false when I disconnect
            referenceCheck.child(otherUid).child(user.getUid())
                    .child("typing").onDisconnect().setValue(0);
            referenceCheck.child(otherUid).child(user.getUid())
                    .child("typing").onDisconnect().setValue(0);
            // set my online presence
//        referenceCheck.child(user.getUid()).child(otherUid).child("presence").setValue(1);
            referenceUsers.child(user.getUid()).child("general").child("presence").setValue(1);

            // set my online presence off when I'm disconnected
            referenceUsers.child(user.getUid()).child("general").child("presence")
                    .onDisconnect().setValue(ServerValue.TIMESTAMP);

            // set offline details automatic
            offlinePresenceAndStatus.put("status", false);
//            offlinePresenceAndStatus.put("presence", ServerValue.TIMESTAMP);
            offlinePresenceAndStatus.put("onChat", false);
            referenceCheck.child(user.getUid()).child(otherUid).onDisconnect()
                    .updateChildren(offlinePresenceAndStatus);

        }).start();

    }
    @Override
    public int getItemCount() {
        return userModelList.size();
    }


    public class ChatViewHolder extends RecyclerView.ViewHolder{

        private final ConstraintLayout userBackground;
        private final CircleImageView imageView;
        private final ImageView imageViewDeliver;
        //        private final ImageView imageViewPin, imageViewPin2, imageViewMute, imageViewUnmute, imageViewMove, imageViewDel, imageViewCancel;
        private final ImageView pinIcon_IV, muteIcon_IV;
        //        private final ConstraintLayout constraintTop, constraintLast;
        private final TextView textViewUser, textViewMsg, textViewMsgCount, dateTime_TV, textViewTyping;
        private final CardView cardView;
        RecyclerView recyclerChat;

        // forward declares
        private final ConstraintLayout checkBoxContainer;
        private final CheckBox checkBoxToWho;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            userBackground = itemView.findViewById(R.id.userBackground);
            imageView = itemView.findViewById(R.id.imageViewUsers);
            textViewUser = itemView.findViewById(R.id.textViewUser);
            textViewMsg = itemView.findViewById(R.id.textViewMsg);
            textViewMsgCount = itemView.findViewById(R.id.textViewMsgCount);
            imageViewDeliver = itemView.findViewById(R.id.imageViewDelivery);
            dateTime_TV = itemView.findViewById(R.id.dateTime_TV);
//            textViewDay = itemView.findViewById(R.id.textViewDay);
            textViewTyping = itemView.findViewById(R.id.textViewTyping);
            cardView = itemView.findViewById(R.id.cardView);
            pinIcon_IV = itemView.findViewById(R.id.pinIcon_IV);
            muteIcon_IV = itemView.findViewById(R.id.muteIcon_IV);
//            imageViewPin2 = itemView.findViewById(R.id.imageViewPin);
//            imageViewDel = itemView.findViewById(R.id.imageViewDel);
//            imageViewMute = itemView.findViewById(R.id.imageViewMute);
//            imageViewMove = itemView.findViewById(R.id.imageViewMove);
//            imageViewCancel = itemView.findViewById(R.id.imageViewCancel2);
//            imageViewMenu = itemView.findViewById(R.id.imageViewUserMenu);
//            constraintTop = itemView.findViewById(R.id.constraintTop);
//            constraintLast = itemView.findViewById(R.id.constrainLast);
            checkBoxToWho = itemView.findViewById(R.id.checkBoxForward);
            checkBoxContainer = itemView.findViewById(R.id.checkBoxContainer);

//
//            recyclerChat.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            recyclerChat = itemView.findViewById(R.id.recyclerChat);

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManagerWrapper(itemView.getContext(), LinearLayoutManager.VERTICAL, false);

            recyclerChat.setLayoutManager(mLayoutManager);

        }
//        public void setRecyclerView(RecyclerView recyclerView) {
//            this.recyclerChat = recyclerView;
//        }
    }

}










