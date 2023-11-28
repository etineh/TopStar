package com.pixel.chatapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.CountDownTimer;
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
import androidx.constraintlayout.widget.ConstraintSet;
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
import com.pixel.chatapp.FragmentListener;
import com.pixel.chatapp.R;
import com.pixel.chatapp.chats.MessageAdapter;
import com.pixel.chatapp.home.MainActivity;
import com.pixel.chatapp.home.fragments.ChatsListFragment;
import com.pixel.chatapp.model.MessageModel;
import com.pixel.chatapp.model.UserOnChatUI_Model;
import com.squareup.picasso.Picasso;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>  {

    private ChatViewHolder lastOpenViewHolder = null;
    private static List<UserOnChatUI_Model> otherUsersId;
//    private static List<String> otherUsersId;

    private static Context mContext;
    private static String userName;
    private String otherName, imageUrl;

    private DatabaseReference referenceUsers, refUsersLast, referenceCheck, refChatList, refChat,
            refMsgFast, refPinPublic, refPinPrivate;
    FirebaseUser user;
    Map<String, Object> offlinePresenceAndStatus;
    Map<String, Integer> dateMonth, dateNum;

    private static Map<String, ChatViewHolder> viewHolderMap = new HashMap<>();

    private Handler handler = new Handler(Looper.getMainLooper());

    private FragmentListener listener;

    public void setFragmentListener(FragmentListener listener) {
        this.listener = listener;
    }

    private static ChatListAdapter instance;

    public ChatListAdapter(List<UserOnChatUI_Model> otherUsersId, Context mContext, String userName) {
        this.otherUsersId = otherUsersId;
        this.mContext = mContext;
        this.userName = userName;
        MainActivity.myHolder_ = new ArrayList<>();

        UsersAdapter.setmContext(mContext);

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

    public static ChatListAdapter getInstance() {
        if (instance == null) {
            instance = new ChatListAdapter(otherUsersId, mContext, userName);
        }
        return instance;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_card, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {

        int position_ = position;
        String otherUid = otherUsersId.get(position_).getId();
        String emojiOnly = otherUsersId.get(position_).getEmojiOnly();
        String message = otherUsersId.get(position_).getMessage();
        int msgStatus = otherUsersId.get(position_).getMsgStatus();
        long timeSent = otherUsersId.get(position_).getTimeSent();

        // save user holders in List to MainActivity for forward chat
        if(!MainActivity.myHolder_.contains(holder)){
            MainActivity.myHolder_.add(holder);
        }
        // save holder in map for outside chat settings
        if(!viewHolderMap.containsKey(otherUid)){
            viewHolderMap.put(otherUid, holder);
        }

        // set last message
        if(message != null){
            holder.textViewMsg.setText(message);
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

        holder.textViewUser.setText(otherUid);     //set users display name

        // send all recyclerView to mainActivty just once and call the getMessage to load message to it
        if(MainActivity.loadMsg){

            try{
                listener.sendRecyclerView(holder.recyclerChat, otherUid);
                listener.getMessage(userName, otherUid, mContext);
            } catch (Exception e){
                System.out.println("Error (ChatListAdapter L187)"+ e.getMessage());
            }


            if(position_ == getItemCount() - 1){
                MainActivity.loadMsg = false;    // stop the getVIew method from loading at every instance
                listener.firstCallLoadPage(otherUid);   // call the method to load the all message

                // add cardView after 2 sec to allow chats to load to List
                new CountDownTimer(2000, 1000){
                    @Override
                    public void onTick(long l) {
                    }

                    @Override
                    public void onFinish() {

                        if(MessageAdapter.viewCacheReceive != null || MessageAdapter.viewCacheSend != null
                                && MessageAdapter.viewCacheReceive.size() < 10 || MessageAdapter.viewCacheSend.size() < 10)
//                        )
                        {
                            for (Map.Entry<String, MessageAdapter> entry : MainActivity.adapterMap.entrySet()) {
                                String firstKey = entry.getKey();
                                MainActivity.adapterMap.get(firstKey).addLayoutViewInBackground();
                                MainActivity.loadViewRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.isLoadViewRunnableRunning = true;

                                        if(MainActivity.adapterMap.get(firstKey) != null){
                                            MainActivity.adapterMap.get(firstKey).addLayoutViewEverySec();
                                        }

                                        MainActivity.handlerLoadViewLayout.postDelayed(this, 1000); // Re-schedule the runnable

                                        if(MessageAdapter.viewCacheSend.size() > 100 && MessageAdapter.viewCacheReceive.size() > 100){
                                            MainActivity.handlerLoadViewLayout.removeCallbacks(MainActivity.loadViewRunnable);
                                            MainActivity.isLoadViewRunnableRunning = false;
                                        }
                                    }
                                };

                                break;
                            }

                        }

                    }
                }.start();

            }

        }

        // -------- open user chat box on MainActivity
        holder.constraintLast.setOnClickListener(view -> {

            if(!MainActivity.onForward){

                try {

                    listener.chatBodyVisibility(otherName, imageUrl, userName, otherUid, mContext, holder.recyclerChat);

                    listener.getLastSeenAndOnline(otherUid);

                    listener.msgBackgroundActivities(otherUid);

                    listener.callAllMethods(otherUid);

                    holder.textViewMsgCount.setVisibility(View.INVISIBLE);

                } catch (Exception e){

                    MainActivity.readDatabase = 0;
                    listener.sendRecyclerView(holder.recyclerChat, otherUid);
                    listener.getMessage(userName, otherUid, mContext);

                    listener.chatBodyVisibility(otherName, imageUrl, userName, otherUid, mContext, holder.recyclerChat);

                    listener.getLastSeenAndOnline(otherUid);

                    listener.msgBackgroundActivities(otherUid);

                    listener.callAllMethods(otherUid);

                    holder.textViewMsgCount.setVisibility(View.INVISIBLE);

                    if (!MainActivity.isLoadViewRunnableRunning) {
                        MainActivity.handlerLoadViewLayout.post(MainActivity.loadViewRunnable);
                    }

                    MainActivity.offMainDatabase();
                    Toast.makeText(mContext, "WinnerChats...", Toast.LENGTH_SHORT).show();
                    System.out.println("Error occur - WinnerChat Toast (ChatListAdapter L222)" + e.getMessage());
                }

            }

        });

        //  ----------- call methods    ---------------------

        // get lastMessage, and Date/Time sent, and set delivery msg to visibility
//        getLastMsg_TimeSent_MsgDeliveryVisible(holder, otherUid);

        unreadMsgNumber(holder, otherUid);     // get number of unread message count

        getTypingState(holder, otherUid);      // show when other user is typing

//  get all other-user name and photo  and onClick to chat room-----------------------
//        referenceUsers.keepSynced(true);
        referenceUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // Will later change it to Display Names
//                String otherName = snapshot.child(otherUid).child("userName")
//                        .getValue().toString();

                // set users image
                imageUrl = snapshot.child(otherUid).child("image").getValue().toString();
                if (imageUrl.equals("null")) {
                    holder.imageView.setImageResource(R.drawable.person_round);
                }
                else Picasso.get().load(imageUrl).into(holder.imageView);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //   get the number of new message I have to give my recycle position scrolling
        referenceCheck.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot1) {
                if(!snapshot1.child(otherUid).child("newMsgCount").exists()){
                    referenceCheck.child(user.getUid()).child(otherUid).child("newMsgCount").setValue(0);
                } else {
                    long numScroll = (long) snapshot1.child(otherUid).child("newMsgCount").getValue();
                    int castNum = (int) numScroll;

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //  open option menu
        holder.imageViewMenu.setOnClickListener(view -> {

            if (lastOpenViewHolder != null && lastOpenViewHolder != holder) {
                lastOpenViewHolder.constraintTop.setVisibility(View.GONE);
            }

            checkConstraintVisibilityAndToggle(holder);

            lastOpenViewHolder = holder;
        });

        //  close option menu
        holder.imageViewCancel.setOnClickListener(view -> holder.constraintTop.setVisibility(View.GONE));


        //  delete user from chat list
        holder.imageViewDel.setOnClickListener(view -> {

            String otherName = holder.textViewUser.getText().toString();    // get user username

            listener.onUserDelete(otherName, otherUid);

            //  return height to 2dp
            holder.constraintTop.setVisibility(View.GONE);
        });


        // forward option   -- checkbox
        holder.checkBoxToWho.setOnClickListener(view -> {
            CheckBox checkBox = holder.checkBoxToWho;
            checkBox.setChecked(checkBox.isChecked()); // Toggle the checked state
            String otherName = holder.textViewUser.getText().toString();    // get user username

            if(checkBox.isChecked()) {
                MainActivity.selectCount++;
                MainActivity.forwardChatUserId.add(otherUid);
            } else {
                MainActivity.selectCount--;
                MainActivity.forwardChatUserId.removeIf(name -> name.equals(otherUid)); // remove user
            }

            MainActivity.totalUser_TV.setText("" + MainActivity.selectCount + " selected");

            if(MainActivity.forwardChatUserId.size() > 0){           //  make send button invisible
                MainActivity.circleForwardSend.setVisibility(View.VISIBLE);
            } else
                MainActivity.circleForwardSend.setVisibility(View.INVISIBLE);

        });

        // forward option   -- checkbox Container
        holder.checkBoxContainer.setOnClickListener(view -> {
            CheckBox checkBox = holder.checkBoxToWho;
            String otherName = holder.textViewUser.getText().toString();    // get user username

            if(checkBox.isChecked()) {
                MainActivity.selectCount--;
                checkBox.setChecked(false);
                MainActivity.forwardChatUserId.removeIf(name -> name.equals(otherUid));
            } else {
                MainActivity.selectCount++;
                checkBox.setChecked(true);
                MainActivity.forwardChatUserId.add(otherUid);  // Add username to the List
            }
            MainActivity.totalUser_TV.setText("" + MainActivity.selectCount + " selected");

            if(MainActivity.forwardChatUserId.size() > 0){           //  make send button invisible
                MainActivity.circleForwardSend.setVisibility(View.VISIBLE);
            } else
                MainActivity.circleForwardSend.setVisibility(View.INVISIBLE);

        });

        offlineActivity(otherUid);

    }

    //      --------- methods -----------
    public void updateDeliveryStatus(String otherUid){
        if(otherUsersId != null){
            for (int i = otherUsersId.size() - 1; i >= 0; i--) {
                if (otherUsersId.get(i).getId().equals(otherUid)) {
                    // Store the item in a temporary variable.
                    UserOnChatUI_Model getUser = otherUsersId.get(i);
                    // check the delivery status and update
                    if(getUser.getMsgStatus() == 700033){
                        getUser.setMsgStatus(700024);
                        // update the UI
                        ChatViewHolder userHolder = viewHolderMap.get(otherUid);
                        userHolder.imageViewDeliver.setImageResource(R.drawable.message_tick_one);

                        // update the firebase
                        refUsersLast.child(user.getUid()).child(otherUid)
                                .child("msgStatus").setValue(700024);

                    }
                }
            }
        }
    }
    public void updateDeliveryToRead(String otherUid){
        if(otherUsersId != null){
            for (int i = otherUsersId.size() - 1; i >= 0; i--) {
                if (otherUsersId.get(i).getId().equals(otherUid)) {
                    // Store the item in a temporary variable.
                    UserOnChatUI_Model getUser = otherUsersId.get(i);
                    // check the delivery status and update
                    if(getUser.getMsgStatus() != 700016){
                        getUser.setMsgStatus(700016);
                        // update the UI
                        ChatViewHolder userHolder = viewHolderMap.get(otherUid);
                        userHolder.imageViewDeliver.setImageResource(R.drawable.read_orange);

                        // update the firebase
                        refUsersLast.child(user.getUid()).child(otherUid)
                                .child("msgStatus").setValue(700016);

                    }
                }
            }
        }
    }

    public void findUserPositionByUID(String userUid, String chat, String emojiOnly, int statusNum, long timeSent) {

        if (otherUsersId != null) {
            for (int i = otherUsersId.size() - 1; i >= 0; i--) {
                if (otherUsersId.get(i).getId().equals(userUid)) {
                    // Store the item in a temporary variable.
                    UserOnChatUI_Model getUser = otherUsersId.get(i);
                    if(chat != null){
                        getUser.setMessage(chat);
                    } else {
                        getUser.setEmojiOnly(emojiOnly);
                    }
                    getUser.setMsgStatus(statusNum);

                    // last user date and time
                    getUser.setTimeSent(timeSent);

                    // Remove the item from its old position.
                    otherUsersId.remove(i);

                    // Insert the item at the first position (position 0) in the list.
                    otherUsersId.add(0, getUser);

//                    ChatsListFragment.notifyItemChanged(i);

                    // Notify the adapter that the user has moved.
                    ChatsListFragment.notifyUserMoved(i);

                    // last user date and time
                    Date d = new Date(timeSent);    // convert the timestamp to current time
                    DateFormat formatter = new SimpleDateFormat("h:mm a");
                    String time = formatter.format(d).toLowerCase();

                    ChatViewHolder userHolder = viewHolderMap.get(userUid);

                    ((Activity) mContext).runOnUiThread(()->{
                        setOutsideTextView(userHolder, chat, emojiOnly, statusNum, time);
                    });

                    // update outside chat in ROOM
                    MainActivity.chatViewModel.updateOutsideChat(userUid, chat, emojiOnly, statusNum, timeSent);

                    // Exit the loop, as we've found the item and moved it.
                    break;
                }
            }
        }
    }

    public void setOutsideTextView(ChatViewHolder holder, String chat, String emoji, int statusNum, String timeSent){

        if(holder != null){

            if(chat != null){
                holder.textViewMsg.setText(chat);
            } else {
                holder.textViewMsg.setText(emoji);
            }


            holder.textViewDay.setText("Today");    // change later
            holder.textViewTime.setText(timeSent);

            // delivery status
            int delivery = R.drawable.message_load;

            if(statusNum == 700024){   // delivery
                delivery = R.drawable.message_tick_one;
            } else if (statusNum == 700016) {  // read
                delivery = R.drawable.baseline_grade_24;
            } else if (statusNum == 0) {  // read
                delivery = 0;
            }

            holder.imageViewDeliver.setImageResource(delivery);

        }

    }

    private void checkConstraintVisibilityAndToggle(ChatViewHolder holder){
        // Now you can access the constraint programmatically
//        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.constraintTop.getLayoutParams();
//        int desiredHeight = (int) mContext.getResources().getDimensionPixelSize(R.dimen.constraint_height);
//        if(params.height == desiredHeight){
//            params.height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
//            holder.constraintTop.setLayoutParams(params);
//        } else{
//            params.height = desiredHeight; // change it back to 2dp
//            holder.constraintTop.setLayoutParams(params);
//        }

        if(holder.constraintTop.getVisibility() == View.GONE){
            holder.constraintTop.setVisibility(View.VISIBLE);
        } else holder.constraintTop.setVisibility(View.GONE);

    }

    public void forwardCheckBoxVisibility(List<ChatViewHolder> holder){

        for (int i = 0; i < holder.size(); i++) {

            if(MainActivity.onForward){
                holder.get(i).checkBoxContainer.setVisibility(View.VISIBLE);

                holder.get(i).imageViewPin2.setVisibility(View.INVISIBLE);
                holder.get(i).imageViewUnmute.setVisibility(View.INVISIBLE);
                holder.get(i).textViewDay.setVisibility(View.INVISIBLE);
                holder.get(i).imageViewMenu.setVisibility(View.INVISIBLE);
                holder.get(i).textViewTime.setVisibility(View.INVISIBLE);
                holder.get(i).imageViewDeliver.setVisibility(View.INVISIBLE);
                holder.get(i).textViewMsgCount.setVisibility(View.INVISIBLE);
                ChatsListFragment.openContactList.setVisibility(View.INVISIBLE);

            } else {
                holder.get(i).checkBoxContainer.setVisibility(View.GONE);
                holder.get(i).checkBoxToWho.setChecked(false); // Toggle the checked state

                holder.get(i).imageViewPin2.setVisibility(View.INVISIBLE);      // check later
                holder.get(i).imageViewUnmute.setVisibility(View.INVISIBLE);    // check later
                holder.get(i).textViewDay.setVisibility(View.VISIBLE);
                holder.get(i).imageViewMenu.setVisibility(View.VISIBLE);
                holder.get(i).textViewTime.setVisibility(View.VISIBLE);
                holder.get(i).imageViewDeliver.setVisibility(View.VISIBLE);
                holder.get(i).textViewMsgCount.setVisibility(View.VISIBLE);
                ChatsListFragment.openContactList.setVisibility(View.VISIBLE);

            }

        }

        //  make send button invisible
        MainActivity.circleForwardSend.setVisibility(View.INVISIBLE);

    }

    // get lastMessage, and Date/Time sent, and set delivery msg status
    private void getLastMsg_TimeSent_MsgDeliveryVisible(ChatViewHolder holder, String otherUid){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
        refUsersLast.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                try{
                    String lastMsg;
                    try{
                        lastMsg = snapshot.child(otherUid).child("message").getValue().toString();
                    } catch (Exception e){
                        lastMsg = snapshot.child(otherUid).child("emojiOnly").getValue().toString();
                    }
                    long lastTime = (long)  snapshot.child(otherUid).child("timeSent").getValue();
                    String lastSender = snapshot.child(otherUid).child("from").getValue().toString();

                    //  set the delivery status
                    try{
                        long statusNum = (long) snapshot.child(otherUid).child("msgStatus").getValue();

                        int numMsg = R.drawable.message_load;

                        if(statusNum == 700024){   // delivery
                            numMsg = R.drawable.message_tick_one;
                        } else if (statusNum == 700016) {  // read
                            numMsg = R.drawable.baseline_grade_24;
                        }
                        holder.imageViewDeliver.setImageResource(numMsg);

                        // set message delivery visibility
                        if(lastSender.equals(userName)){
                            holder.imageViewDeliver.setVisibility(View.VISIBLE);
                        } else {
                            holder.imageViewDeliver.setVisibility(View.INVISIBLE);
                        }

                    }catch (Exception e){
                        refUsersLast.child(user.getUid()).child(otherUid).child("msgStatus").setValue(700024);
                    }

                    // set last message
                    holder.textViewMsg.setText(lastMsg);

                    timeAndDateSent(lastTime, holder);

                } catch (Exception e){

                    System.out.println(otherUid + " Error occur for deleting user CL540" + e.getMessage());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
//            }
//        }).start();

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
                    holder.textViewDay.setText("Today");
                    holder.textViewTime.setText(time.toLowerCase());
                } else if (curDay - lastDay == 1) {
                    holder.textViewDay.setText("Yesterday");
                    holder.textViewTime.setText(time.toLowerCase());
                } else if (curDay - lastDay == 2) {
                    holder.textViewDay.setText("2days ago");
                    holder.textViewTime.setText(time.toLowerCase());
                } else if (curDay - lastDay == 3) {
                    holder.textViewDay.setText("3days ago");
                    holder.textViewTime.setText(time.toLowerCase());
                } else if (curDay - lastDay == 4) {
                    holder.textViewDay.setText("4days ago");
                    holder.textViewTime.setText(time.toLowerCase());
                } else if (curDay - lastDay == 5) {
                    holder.textViewDay.setText("5days ago");
                    holder.textViewTime.setText(time.toLowerCase());
                } else if (curDay - lastDay == 6) {
                    holder.textViewDay.setText("6days ago");
                    holder.textViewTime.setText(time.toLowerCase());
                }
            } else if (dateCur - dateLast >= 7 && dateCur - dateLast < 14) {
                holder.textViewDay.setText(lastDayString);
                holder.textViewTime.setText("1wk ago");
            } else if (dateCur - dateLast >= 14 && dateCur - dateLast < 21) {
                holder.textViewDay.setText(lastDayString);
                holder.textViewTime.setText("2wk ago");
            } else if (dateCur - dateLast >= 21 && dateCur - dateLast < 27) {
                holder.textViewDay.setText(lastDayString);
                holder.textViewTime.setText("3wk ago");
            } else {
                holder.textViewDay.setText(lastDayString);
                holder.textViewTime.setText("month ago");
            }
        } else{
//                    holder.textViewDay.setText(dateLast +" "+ lastMonth);
            holder.textViewTime.setText(dateLast +"/"+ lastMonth+"/"+ lastYear);
        }

    }

    // get number of unread message count
    private void unreadMsgNumber(ChatViewHolder holder, String otherUid){

//        referenceCheck.keepSynced(true);
        referenceCheck.child(user.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        try{
                            long unreadMsg = (long) snapshot.child(otherUid).child("unreadMsg").getValue();
                            if(unreadMsg > 0) {
                                holder.textViewMsgCount.setVisibility(View.VISIBLE);
                                holder.textViewMsgCount.setText(""+unreadMsg);
                            } else{
                                holder.textViewMsgCount.setVisibility(View.INVISIBLE);
                            }
                        } catch (Exception e){
                            referenceCheck.child(user.getUid()).child(otherUid)
                                    .child("unreadMsg").setValue(0);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    // get user typing state
    private void getTypingState(ChatViewHolder holder, String otherUid){
//// Bug ("typing" reflecting on previous position) -- solved by starting ref with user.getUid() and add the rest child to onDataChange
        referenceCheck.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                try {
                    long typing = (long) snapshot.child(otherUid).child("typing").getValue();

                    if(typing == 1){
                        holder.textViewMsg.setVisibility(View.INVISIBLE);
                        holder.textViewTyping.setVisibility(View.VISIBLE);
                        holder.textViewTyping.setText("typing...");
                        holder.textViewTyping.setTypeface(null, Typeface.ITALIC);  // Italic style
                    }
                    else {
                        holder.textViewMsg.setVisibility(View.VISIBLE);
                        holder.textViewTyping.setVisibility(View.GONE);
                    }
                } catch (Exception e){
                    System.out.println(otherUid + " Catch error CL600 " + e.getMessage());
                }
//
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
            referenceUsers.child(user.getUid()).child("presence").setValue(1);

            // set my online presence off when I'm disconnected
            referenceUsers.child(user.getUid()).child("presence").onDisconnect().setValue(ServerValue.TIMESTAMP);

            // set offline details automatic
            offlinePresenceAndStatus.put("presence", ServerValue.TIMESTAMP);
            offlinePresenceAndStatus.put("status", false);
            offlinePresenceAndStatus.put("onChat", false);
            referenceCheck.child(user.getUid()).child(otherUid).onDisconnect()
                    .updateChildren(offlinePresenceAndStatus);

        }).start();

    }
    @Override
    public int getItemCount() {
        return otherUsersId.size();
    }


    public class ChatViewHolder extends RecyclerView.ViewHolder{

        private CircleImageView imageView;
        private ImageView imageViewDeliver, imageViewMenu;
        private ImageView imageViewPin, imageViewPin2, imageViewMute, imageViewUnmute, imageViewMove, imageViewDel, imageViewCancel;
        private ConstraintLayout constraintTop, constraintLast;
        private TextView textViewUser, textViewMsg, textViewMsgCount, textViewTime, textViewTyping;
        private TextView textViewDay;
        private CardView cardView;
        RecyclerView recyclerChat;

        // forward declares
        private ConstraintLayout checkBoxContainer;
        private CheckBox checkBoxToWho;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageViewUsers);
            textViewUser = itemView.findViewById(R.id.textViewUser);
            textViewMsg = itemView.findViewById(R.id.textViewMsg);
            textViewMsgCount = itemView.findViewById(R.id.textViewMsgCount);
            imageViewDeliver = itemView.findViewById(R.id.imageViewDelivery);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewDay = itemView.findViewById(R.id.textViewDay);
            textViewTyping = itemView.findViewById(R.id.textViewTyping);
            cardView = itemView.findViewById(R.id.cardView);
            imageViewPin = itemView.findViewById(R.id.imageViewPin);
            imageViewPin2 = itemView.findViewById(R.id.imageViewPin2);
            imageViewDel = itemView.findViewById(R.id.imageViewDel);
            imageViewMute = itemView.findViewById(R.id.imageViewMute);
            imageViewUnmute = itemView.findViewById(R.id.imageViewUnmute);
            imageViewMove = itemView.findViewById(R.id.imageViewMove);
            imageViewCancel = itemView.findViewById(R.id.imageViewCancel2);
            imageViewMenu = itemView.findViewById(R.id.imageViewUserMenu);
            constraintTop = itemView.findViewById(R.id.constraintTop);
            constraintLast = itemView.findViewById(R.id.constrainLast);
            checkBoxToWho = itemView.findViewById(R.id.checkBoxForward);
            checkBoxContainer = itemView.findViewById(R.id.checkBoxContainer);

//
            recyclerChat = itemView.findViewById(R.id.recyclerChat);
            recyclerChat.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
//            recyclerChat.setHasFixedSize(true);

//            recyclerChat = itemView.findViewById(R.id.recyclerChat);
//
//            // Set the layout manager with desired properties
//            LinearLayoutManager layoutManager = new LinearLayoutManager(itemView.getContext());
////            layoutManager.setReverseLayout(true); // Start from the bottom
////            layoutManager.setStackFromEnd(true);   // Stack items from the bottom
//
//            recyclerChat.setLayoutManager(layoutManager);
        }
//        public void setRecyclerView(RecyclerView recyclerView) {
//            this.recyclerChat = recyclerView;
//        }
    }

}










