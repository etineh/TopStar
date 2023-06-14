package com.pixel.chatapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    List<String> otherUsersList;
//    List<String> names;
    Context mContext;
    String userName;
    DatabaseReference fbReference;
    FirebaseUser user;
//    SharedPreferences sharedPreferences;
//    long count;

    public UsersAdapter(List<String> otherUsersList, Context mContext, String userName) {
        this.otherUsersList = otherUsersList;
        this.mContext = mContext;
        this.userName = userName;

        user = FirebaseAuth.getInstance().getCurrentUser();
        fbReference = FirebaseDatabase.getInstance().getReference("Users");
//        sharedPreferences = this.mContext.getSharedPreferences("LastUserMessage", Context.MODE_PRIVATE); // here you can store different data into the Score, like line 42

    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_card, parent, false);

        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {

        int pos = position;

//  get all other-user details -----------------------
        fbReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String otherName = snapshot.child(otherUsersList.get(pos)).child("userName")
                        .getValue().toString();           // fetch out all the userName except mine
//                String lastMsg = snapshot.child(otherUsersList.get(pos)).child(user.getUid())
//                        .child("lastMsg").getValue().toString();

                String imageUrl = snapshot.child(otherUsersList.get(pos)).child("image").getValue().toString();    // fetch out image
                if (imageUrl.equals("null")) holder.imageView.setImageResource(R.drawable.person_round);
                else Picasso.get().load(imageUrl).into(holder.imageView);

                holder.textViewUser.setText(otherName);                 // display all username
//                holder.textViewTime.setText(lastTimeSent);              // display last time
//                holder.textViewMsg.setText(bio);                    // display Bio of the user

                // what happen when the cardView is click
                holder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, MessageActivity.class);
                        // save both userName and otherName and each user UiD to display on the chat box
                        intent.putExtra("otherName", otherName);
                        intent.putExtra("userName", userName);
                        intent.putExtra("Uid", otherUsersList.get(pos));

                        // coming back
                        // set all delivery/seen notification to "in"
//                        fbReference.child("Users").child(user.getUid()).child(otherUsersList.get(pos))  // set seen msg
//                                .child("seen").setValue("in");
//                        fbReference.child("Users").child(user.getUid()).child(otherUsersList.get(pos))  // set seen msg
//                                .child("show").setValue("in");
//
//                        // checking if the other user disable his view or if he has view my message
//                        if(snapshot.child(otherUsersList.get(pos)).child(user.getUid()).child("show").getValue().equals("in")){
//                            fbReference.child("Users").child(user.getUid()).child(otherUsersList.get(pos))  // set seen msg
//                                    .child("realr").setValue("in");
//                        }

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
        return otherUsersList.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder{

        private CircleImageView imageView;
        private ImageView imageViewDeliver;
        private TextView textViewUser, textViewMsg, textViewMsgCount, textViewTime;
        private CardView cardView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageViewUsers);
            textViewUser = itemView.findViewById(R.id.textViewUser);
            textViewMsg = itemView.findViewById(R.id.textViewMsg);
            textViewMsgCount = itemView.findViewById(R.id.textViewMsgCount);
            imageViewDeliver = itemView.findViewById(R.id.imageViewDelivery);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            cardView = itemView.findViewById(R.id.cardView);

        }

    }
}







