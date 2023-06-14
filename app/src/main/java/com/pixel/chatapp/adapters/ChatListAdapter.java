package com.pixel.chatapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pixel.chatapp.R;
import com.pixel.chatapp.chats.MessageActivity;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    List<String> otherUsersId;
    Context mContext;
    String userName;
    DatabaseReference referenceUsers, fReference, referenceCheck;
    FirebaseUser user;

    public ChatListAdapter(List<String> otherUsersId, Context mContext, String userName) {
        this.otherUsersId = otherUsersId;
        this.mContext = mContext;
        this.userName = userName;

        user = FirebaseAuth.getInstance().getCurrentUser();
        referenceCheck = FirebaseDatabase.getInstance().getReference("Checks");
        referenceUsers = FirebaseDatabase.getInstance().getReference("Users");
        fReference = FirebaseDatabase.getInstance().getReference("UsersList");

    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_card, parent, false);

        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {

        int pos = position;

        String myUsersId = otherUsersId.get(pos);

        // get lastMessage and Time sent
        fReference.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String lastMsg = snapshot.child(myUsersId).child("message").getValue().toString();
//                String userName = snapshot.child(myUsersId).child("from").getValue().toString();
                long lastTime = (long) snapshot.child(myUsersId).child("timeSent").getValue();

                // to get the current timestamp
//                Timestamp stamp = new Timestamp(System.currentTimeMillis());
//                Date date = new Date(stamp.getTime());

                // convert the timestamp to current time
                Date d = new Date(lastTime);
                DateFormat formatter = new SimpleDateFormat("h:mm a");
                String time = formatter.format(d);

                holder.textViewMsg.setText(lastMsg);
                holder.textViewTime.setText(time.toLowerCase());
//                holder.textViewUser.setText(userName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        // show number of unread message count
        referenceCheck.keepSynced(true);
        referenceCheck.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!snapshot.child(myUsersId).child("unreadMsg").exists()){
                            referenceCheck.child(user.getUid()).child(myUsersId)
                                    .child("unreadMsg").setValue(1);
                        }
                        else
                        {
                            long unreadMsg = (long) snapshot.child(myUsersId).child("unreadMsg").getValue();
                            if(unreadMsg > 0) {
                                holder.textViewMsgCount.setText(""+unreadMsg);
                                holder.textViewMsgCount.setVisibility(View.VISIBLE);
                            } else{
                                holder.textViewMsgCount.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        // show when user is typing
// Bug ("typing" reflecting on previous position) -- solved by starting ref with user.getUid() and add the rest child to onDataChange
        referenceCheck.child(user.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (!snapshot.child(myUsersId).child("typing").exists()){
                        referenceCheck.child(user.getUid()).child(myUsersId)
                                .child("typing").setValue(0);
                    }
                    else {

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
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });



//  get all other-user name and photo -----------------------
        referenceUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // Will later change it to Display Names
                String otherName = snapshot.child(myUsersId).child("userName")
                        .getValue().toString();

                String imageUrl = snapshot.child(myUsersId).child("image").getValue().toString();    // fetch out image
                if (imageUrl.equals("null")) holder.imageView.setImageResource(R.drawable.person_round);
                else Picasso.get().load(imageUrl).into(holder.imageView);


                // what happen when the cardView is click
                holder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // set my unreadMessage to 0
                        referenceCheck.child(user.getUid()).child(myUsersId)
                                .child("unreadMsg").setValue(0);
                        holder.textViewMsgCount.setVisibility(View.GONE);

                        // send userName, otherName and each user UiD to display on the chat box
                        Intent intent = new Intent(mContext, MessageActivity.class);
                        intent.putExtra("otherName", otherName);
                        intent.putExtra("userName", userName);
                        intent.putExtra("Uid", myUsersId);

                        mContext.startActivity(intent);
                    }
                });
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
        private ImageView imageViewDeliver;
        private TextView textViewUser, textViewMsg, textViewMsgCount, textViewTime, textViewTyping;
        private CardView cardView;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageViewUsers);
            textViewUser = itemView.findViewById(R.id.textViewUser);
            textViewMsg = itemView.findViewById(R.id.textViewMsg);
            textViewMsgCount = itemView.findViewById(R.id.textViewMsgCount);
            imageViewDeliver = itemView.findViewById(R.id.imageViewDelivery);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewTyping = itemView.findViewById(R.id.textViewTyping);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }

}










