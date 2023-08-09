package com.pixel.chatapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.opengl.Visibility;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.pixel.chatapp.FragmentListener;
import com.pixel.chatapp.NetworkChangeReceiver;
import com.pixel.chatapp.R;
import com.pixel.chatapp.chats.MessageActivity;
import com.pixel.chatapp.chats.MessageAdapter;
import com.pixel.chatapp.chats.MessageModel;
import com.pixel.chatapp.home.MainActivity;
import com.pixel.chatapp.home.fragments.ChatsListFragment;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.reflect.jvm.internal.impl.descriptors.ClassOrPackageFragmentDescriptor;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> implements MainActivity.ChatVisibilityListener {

    private static List<String> otherUsersId;
    private static Context mContext;
    private static String userName;
    DatabaseReference referenceUsers, refUsersLast, referenceCheck;
    FirebaseUser user;
    Map<String, Object> offlinePresenceAndStatus;
    Map<String, Integer> dateMonth, dateNum;
    boolean loadMsg = true;
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

        user = FirebaseAuth.getInstance().getCurrentUser();
        referenceCheck = FirebaseDatabase.getInstance().getReference("Checks");
        referenceUsers = FirebaseDatabase.getInstance().getReference("Users");
        refUsersLast = FirebaseDatabase.getInstance().getReference("UsersList");

//        refMsg = FirebaseDatabase.getInstance().getReference("Messages");

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
        String myUsersId = otherUsersId.get(pos);

        new Thread(() -> {
            // set user "typing" to be false when I disconnect
            referenceCheck.child(myUsersId).child(user.getUid())
                    .child("typing").onDisconnect().setValue(0);

            // set my online presence
//        referenceCheck.child(user.getUid()).child(myUsersId).child("presence").setValue(1);
            referenceUsers.child(user.getUid()).child("presence").setValue(1);

            // set my online presence off when I'm disconnected
            referenceUsers.child(user.getUid()).child("presence").onDisconnect().setValue(ServerValue.TIMESTAMP);

            // set offline details automatic
            offlinePresenceAndStatus.put("presence", ServerValue.TIMESTAMP);
            offlinePresenceAndStatus.put("status", false);
            referenceCheck.child(user.getUid()).child(myUsersId).onDisconnect()
                    .updateChildren(offlinePresenceAndStatus);

        }).start();


        //  ----------- call methods    ---------------------

        // get lastMessage, and Date/Time sent, and set delivery msg to visibility
        getLastMsg_TimeSent_MsgDeliveryVisible(holder, myUsersId);

        unreadMsgNumber(holder, myUsersId);     // get number of unread message count

        getTypingState(holder, myUsersId);      // show when other user is typing


//  get all other-user name and photo  and onClick to chat room-----------------------
//        referenceUsers.keepSynced(true);
        referenceUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // Will later change it to Display Names
                String otherName = snapshot.child(myUsersId).child("userName")
                        .getValue().toString();

                holder.textViewUser.setText(otherName);     //set users display name

                // set users image
                String imageUrl = snapshot.child(myUsersId).child("image").getValue().toString();
                if (imageUrl.equals("null")) {
                    holder.imageView.setImageResource(R.drawable.person_round);
                }
                else Picasso.get().load(imageUrl).into(holder.imageView);


                // send all recyclerView to mainActivty just once and call the getMessage to load message to it
                if(loadMsg){
                    new CountDownTimer(1500, 750){
                        @Override
                        public void onTick(long l) {
                            try{
                                listener.sendRecyclerView(holder.recyclerChat, otherName, myUsersId);
                                listener.getMessage(userName, otherName, myUsersId, mContext);
                            } catch (Exception e){
                                System.out.println("Error (ChatListAdapter L187)"+ e.getMessage());
                            }
                        }

                        @Override
                        public void onFinish() {
                            loadMsg = false;    // stop the getVIew method from loading at every instance
                            listener.firstCallLoadPage(otherName);   // call the method to load the all message
                        }
                    }.start();

                }

                // -------- send adapter to MainActivity
                holder.constraintLast.setOnClickListener(view -> {

                        listener.msgBodyVisibility(otherName, imageUrl, userName, myUsersId);
                    try {


                        listener.getLastSeenAndOnline(myUsersId);

                        listener.msgBackgroundActivities(myUsersId);

                        listener.callAllMethods(otherName, userName, myUsersId);

                        holder.textViewMsgCount.setVisibility(View.INVISIBLE);

                    } catch (Exception e){
                        Toast.makeText(mContext, "Send your first message here...!", Toast.LENGTH_SHORT).show();
                        System.out.println("Error occur (ChatListAdapter L218)" + e.getMessage());
                    }

                });

                //   get the number of new message I have to give my recycle position scrolling
                referenceCheck.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot1) {
                        if(!snapshot1.child(myUsersId).child("newMsgCount").exists()){
                            referenceCheck.child(user.getUid()).child(myUsersId).child("newMsgCount").setValue(0);
                        } else {
                            long numScroll = (long) snapshot1.child(myUsersId).child("newMsgCount").getValue();
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
//                                    intent.putExtra("Uid", myUsersId);
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
        holder.imageViewMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.constraintTop.getVisibility() == View.GONE){
                    holder.constraintTop.setVisibility(View.VISIBLE);
                } else {
                    holder.constraintTop.setVisibility(View.GONE);
                }
            }
        });

        //  close option menu
        holder.imageViewCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.constraintTop.setVisibility(View.GONE);
            }
        });

    }

    //      --------- methods -----------

    // get lastMessage, and Date/Time sent, and set delivery msg status
    private void getLastMsg_TimeSent_MsgDeliveryVisible(ChatViewHolder holder, String myUsersId){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
        refUsersLast.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String lastMsg = snapshot.child(myUsersId).child("message").getValue().toString();
                long lastTime = (long)  snapshot.child(myUsersId).child("timeSent").getValue();
                String lastSender = snapshot.child(myUsersId).child("from").getValue().toString();

                // set message delivery visibility
                if(lastSender.equals(userName)){
                    holder.imageViewDeliver.setVisibility(View.VISIBLE);
                } else {
                    holder.imageViewDeliver.setVisibility(View.INVISIBLE);
                }

                //  set the delivery status
                try{
                    long statusNum = (long) snapshot.child(myUsersId).child("msgStatus").getValue();

                    int numMsg = R.drawable.message_load;

                    if(statusNum == 700024){   // delivery
                        numMsg = R.drawable.message_tick_one;
                    } else if (statusNum == 700016) {  // read
                        numMsg = R.drawable.baseline_grade_24;
                    }
                    holder.imageViewDeliver.setImageResource(numMsg);

                }catch (Exception e){
                    refUsersLast.child(user.getUid()).child(myUsersId).child("msgStatus").setValue(700024);
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
//            }
//        }).start();

    }

    // get number of unread message count
    private void unreadMsgNumber(ChatViewHolder holder, String myUsersId){

//        referenceCheck.keepSynced(true);
        referenceCheck.child(user.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        try{
                            long unreadMsg = (long) snapshot.child(myUsersId).child("unreadMsg").getValue();
                            if(unreadMsg > 0) {
                                holder.textViewMsgCount.setVisibility(View.VISIBLE);
                                holder.textViewMsgCount.setText(""+unreadMsg);
                            } else{
                                holder.textViewMsgCount.setVisibility(View.INVISIBLE);
                            }
                        } catch (Exception e){
                            referenceCheck.child(user.getUid()).child(myUsersId)
                                    .child("unreadMsg").setValue(0);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    // get user typing state
    private void getTypingState(ChatViewHolder holder, String myUsersId){
//// Bug ("typing" reflecting on previous position) -- solved by starting ref with user.getUid() and add the rest child to onDataChange
        referenceCheck.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                long typing = (long) snapshot.child(myUsersId).child("typing").getValue();

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

    @Override
    public void constraintChatVisibility(int position, int isVisible) {
//       getView()
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder{

        private CircleImageView imageView;
        private ImageView imageViewDeliver, imageViewMenu;
        private ImageView imageViewPin, imageViewMute, imageViewMove, imageViewDel, imageViewCancel;
        private ConstraintLayout constraintTop, constraintLast;
        private TextView textViewUser, textViewMsg, textViewMsgCount, textViewTime, textViewTyping;
        private TextView textViewDay;
        private CardView cardView;
        RecyclerView recyclerChat;
        int listCount;

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
            imageViewDel = itemView.findViewById(R.id.imageViewDel);
            imageViewMute = itemView.findViewById(R.id.imageViewMute);
            imageViewMove = itemView.findViewById(R.id.imageViewMove);
            imageViewCancel = itemView.findViewById(R.id.imageViewCancel2);
            imageViewMenu = itemView.findViewById(R.id.imageViewUserMenu);
            constraintTop = itemView.findViewById(R.id.constraintTop);
            constraintLast = itemView.findViewById(R.id.constrainLast);
//
            recyclerChat = itemView.findViewById(R.id.recyclerChat);
            recyclerChat.setLayoutManager(new LinearLayoutManager(mContext));

        }
//        public void setRecyclerView(RecyclerView recyclerView) {
//            this.recyclerChat = recyclerView;
//        }
    }

}










