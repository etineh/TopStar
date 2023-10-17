package com.pixel.chatapp.adapters;

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
import com.pixel.chatapp.home.MainActivity;
import com.pixel.chatapp.home.fragments.ChatsListFragment;
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

    private ChatViewHolder lastOpenViewHolder = null;
    private static List<String> otherUsersId;
    private static Context mContext;
    private static String userName;
    private DatabaseReference referenceUsers, refUsersLast, referenceCheck, refChatList, refChat,
            refMsgFast, refPinPublic, refPinPrivate;
    FirebaseUser user;
    Map<String, Object> offlinePresenceAndStatus;
    Map<String, Integer> dateMonth, dateNum;

    private Handler handler = new Handler(Looper.getMainLooper());

    private FragmentListener listener;

    public void setFragmentListener(FragmentListener listener) {
        this.listener = listener;
    }

    private static ChatListAdapter instance;

    public ChatListAdapter(List<String> otherUsersId, Context mContext, String userName) {
        this.otherUsersId = otherUsersId;
        this.mContext = mContext;
        this.userName = userName;
        MainActivity.myHolder_ = new ArrayList<>();

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

    public static void checkNum(){
//        System.out.println("What is numner ");
    }
    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {

        int pos = position;
        String otherUid = otherUsersId.get(pos);

        MainActivity.myHolder_.add(holder);  // save user holders in List to MainActivity for forward chat

        new Thread(() -> {
            // set user "typing" to be false when I disconnect
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
            referenceCheck.child(user.getUid()).child(otherUid).onDisconnect()
                    .updateChildren(offlinePresenceAndStatus);

        }).start();


        //  ----------- call methods    ---------------------

        // get lastMessage, and Date/Time sent, and set delivery msg to visibility
        getLastMsg_TimeSent_MsgDeliveryVisible(holder, otherUid);

        unreadMsgNumber(holder, otherUid);     // get number of unread message count

        getTypingState(holder, otherUid);      // show when other user is typing


//  get all other-user name and photo  and onClick to chat room-----------------------
//        referenceUsers.keepSynced(true);
        referenceUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // Will later change it to Display Names
                String otherName = snapshot.child(otherUid).child("userName")
                        .getValue().toString();

                holder.textViewUser.setText(otherName);     //set users display name

                // set users image
                String imageUrl = snapshot.child(otherUid).child("image").getValue().toString();
                if (imageUrl.equals("null")) {
                    holder.imageView.setImageResource(R.drawable.person_round);
                }
                else Picasso.get().load(imageUrl).into(holder.imageView);


                // send all recyclerView to mainActivty just once and call the getMessage to load message to it
                if(MainActivity.loadMsg){
                    new CountDownTimer(1500, 750){
                        @Override
                        public void onTick(long l) {
                            try{
                                listener.sendRecyclerView(holder.recyclerChat, otherName, otherUid);
                                listener.getMessage(userName, otherName, otherUid, mContext);
                            } catch (Exception e){
                                System.out.println("Error (ChatListAdapter L187)"+ e.getMessage());
                            }
                        }

                        @Override
                        public void onFinish() {
                            MainActivity.loadMsg = false;    // stop the getVIew method from loading at every instance
                            listener.firstCallLoadPage(otherName);   // call the method to load the all message
                        }
                    }.start();

                }

                // -------- open user chat box on MainActivity
                holder.constraintLast.setOnClickListener(view -> {

                    if(!MainActivity.onForward){

                        try {

                            listener.chatBodyVisibility(otherName, imageUrl, userName, otherUid, mContext, holder.recyclerChat);

                            listener.getLastSeenAndOnline(otherUid);

                            listener.msgBackgroundActivities(otherUid);

                            listener.callAllMethods(otherName, userName, otherUid);

                            holder.textViewMsgCount.setVisibility(View.INVISIBLE);


                        } catch (Exception e){

                            listener.sendRecyclerView(holder.recyclerChat, otherName, otherUid);
                            listener.getMessage(userName, otherName, otherUid, mContext);

                            listener.chatBodyVisibility(otherName, imageUrl, userName, otherUid, mContext, holder.recyclerChat);

                            listener.getLastSeenAndOnline(otherUid);

                            listener.msgBackgroundActivities(otherUid);

                            listener.callAllMethods(otherName, userName, otherUid);

                            holder.textViewMsgCount.setVisibility(View.INVISIBLE);

                            Toast.makeText(mContext, "WinnerChats...", Toast.LENGTH_SHORT).show();
                            System.out.println("Error occur - WinnerChat Toast (ChatListAdapter L222)" + e.getMessage());
                        }

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

                            // what happen when the cardView is click
//                            holder.imageViewMenu.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View view) {
//
//                                    // send userName, otherName and each user UiD to display on the chat box
//                                    Intent intent = new Intent(mContext, MessageActivity.class);
//                                    intent.putExtra("otherName", otherName);
//                                    intent.putExtra("userName", userName);
//                                    intent.putExtra("Uid", otherUid);
//                                    intent.putExtra("ImageUrl", imageUrl);
//                                    intent.putExtra("recyclerScroll", castNum);
//                                    intent.putExtra("insideChat", "yes");
//                                    intent.putExtra("messageList", (Serializable) holder.modelList2);
//
//                                    mContext.startActivity(intent);
//
//                                    // set my unreadMessage to 0 and hide my count layer
//                                    holder.textViewMsgCount.setVisibility(View.INVISIBLE);
//
//                                    // close option menu if open
//                                    holder.constraintTop.setVisibility(View.GONE);
//                                }
//                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
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

            listener.onUserDelete(otherName, userName, otherUid);

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
                MainActivity.selectedUsernames.add(otherName + " " + otherUid);  // Add username to the List
            } else {
                MainActivity.selectCount--;
                MainActivity.selectedUsernames.removeIf(name -> name.equals(otherName + " " + otherUid)); // remove username
            }

            MainActivity.totalUser_TV.setText("" + MainActivity.selectCount + " selected");

            if(MainActivity.selectedUsernames.size() > 0){           //  make send button invisible
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
                MainActivity.selectedUsernames.removeIf(name -> name.equals(otherName + " " + otherUid));
            } else {
                MainActivity.selectCount++;
                checkBox.setChecked(true);
                MainActivity.selectedUsernames.add(otherName + " " + otherUid);  // Add username to the List
            }
            MainActivity.totalUser_TV.setText("" + MainActivity.selectCount + " selected");

            if(MainActivity.selectedUsernames.size() > 0){           //  make send button invisible
                MainActivity.circleForwardSend.setVisibility(View.VISIBLE);
            } else
                MainActivity.circleForwardSend.setVisibility(View.INVISIBLE);

        });


    }

    //      --------- methods -----------

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










