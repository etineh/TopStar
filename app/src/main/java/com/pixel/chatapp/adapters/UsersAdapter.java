package com.pixel.chatapp.adapters;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pixel.chatapp.listeners.FragmentListener;
import com.pixel.chatapp.R;
import com.pixel.chatapp.home.MainActivity;
import com.pixel.chatapp.home.fragments.ChatsListFragment;
import com.pixel.chatapp.model.ContactModel;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    MainActivity mainActivity = new MainActivity();
    List<ContactModel> otherUsersList;
//    List<String> names;
    DatabaseReference refUsers, refClearSign;
    FirebaseUser user;
    ChatsListFragment chatsListFragment = new ChatsListFragment();

    public static Context mContext;

    public static Context getmContext() {
        return mContext;
    }

    public static void setmContext(Context mContext_) {
        mContext = mContext_;
    }

    public interface BackButtonClickListener {
        void onBackButtonClicked();
    }

    private BackButtonClickListener backButtonClickListener;

    public void setBackButtonClickListener(BackButtonClickListener listener) {
        this.backButtonClickListener = listener;
    }

    private FragmentListener listener;

    public void setListener(FragmentListener listener) {
        this.listener = listener;
    }

    //  constructor
    public UsersAdapter(List<ContactModel> otherUsersList, Context mContext) {
        this.otherUsersList = otherUsersList;
//        this.mContext = mContext;

        user = FirebaseAuth.getInstance().getCurrentUser();
        refUsers = FirebaseDatabase.getInstance().getReference("Users");
        refClearSign = FirebaseDatabase.getInstance().getReference("ClearSign");

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
        ContactModel contactModel = otherUsersList.get(pos);

        //  get all other-user details -----------------------
        String otherUid = contactModel.getOtherUid();
        String otherName = contactModel.getUserName();
        String myUserName = contactModel.getMyUserName();
        String imageUrl = contactModel.getImage();
        String hint = contactModel.getBio();

        if (imageUrl == null || imageUrl.equals("null")) {      // display user photo
            holder.imageView.setImageResource(R.drawable.person_round);
        }
        else Picasso.get().load(imageUrl).into(holder.imageView);

        holder.textViewUser.setText(otherName);                 // display all myUserName
//        holder.textViewTime.setText(lastTimeSent);              // display last time
        holder.textViewMsg.setText(hint);                    // display Bio of the user

        // add up user recyclerView that doesn't exist
        listener.sendRecyclerView(holder.recyclerChat, otherUid);

        // check if other user deleted me from his chat list, if yes, then clear all the user chat
//        MainActivity.checkClearChatsDB(otherUid);

        // what happen when the cardView is click
        holder.cardView.setOnClickListener(view -> {

            if (backButtonClickListener != null) {  // close contact list
                backButtonClickListener.onBackButtonClicked();
            }

            listener.chatBodyVisibility(otherName, imageUrl, myUserName, otherUid, getmContext(), holder.recyclerChat);

            listener.getLastSeenAndOnline(otherUid);

            listener.msgBackgroundActivities(otherUid);

            listener.callAllMethods(otherUid, getmContext());

            // activate adapter for user if null
            if(MainActivity.adapterMap.get(otherUid) == null){
                // call getMessage() to add up new user adapter
//                listener.getMessage(myUserName, otherName, otherUid, chatsListFragment.getMainContext());
                listener.getMessage(myUserName, otherUid, getmContext());
            }
            // check if network is okay and remove the network bar constraint
            new Handler().postDelayed(() -> {
                if(MainActivity.networkListener.equals("yes"))
                    MainActivity.constrNetConnect.setVisibility(View.GONE);
            }, 1000);

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
        RecyclerView recyclerChat;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageViewUsers);
            textViewUser = itemView.findViewById(R.id.textViewUser);
            textViewMsg = itemView.findViewById(R.id.textViewMsg);
            textViewMsgCount = itemView.findViewById(R.id.textViewMsgCount);
            imageViewDeliver = itemView.findViewById(R.id.imageViewDelivery);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            cardView = itemView.findViewById(R.id.cardView);

            recyclerChat = itemView.findViewById(R.id.recyclerChat);
            recyclerChat.setLayoutManager(new LinearLayoutManager(itemView.getContext()));

        }

    }
}







