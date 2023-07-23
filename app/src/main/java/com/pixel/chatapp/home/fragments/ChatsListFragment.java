package com.pixel.chatapp.home.fragments;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.Context;
import android.content.Intent;
import android.icu.text.RelativeDateTimeFormatter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pixel.chatapp.FragmentListener;
import com.pixel.chatapp.R;
import com.pixel.chatapp.adapters.ChatListAdapter;
import com.pixel.chatapp.contacts.UsersContactActivity;;
import com.pixel.chatapp.model.ChatListModel;

import java.util.ArrayList;
import java.util.List;

public class ChatsListFragment extends Fragment {

    public static ChatsListFragment newInstance(){
        return new ChatsListFragment();
    }

    RecyclerView recyclerView;
    FirebaseUser user;
    DatabaseReference fReference, refChecks;

    String userName;
    ChatListAdapter adapter;
    FloatingActionButton fab;

    private List<ChatListModel> chatListID;
    private List<String> mUsersID;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.chats_list_fragment, container, false);

        fab = view.findViewById(R.id.floatingActionButton);
        recyclerView = view.findViewById(R.id.recyclerViewChatList);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        chatListID = new ArrayList<>();

        user = FirebaseAuth.getInstance().getCurrentUser();
//        refChecks = FirebaseDatabase.getInstance().getReference("Checks");

        // Go to contact
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getContext(), UsersContactActivity.class);
                startActivity(intent);
            }
        });


        // get my userName
        fReference = FirebaseDatabase.getInstance().getReference("Users");
        fReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                userName = snapshot.child(user.getUid()).child("userName").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // add other users id to the list
        fReference = FirebaseDatabase.getInstance().getReference("ChatList").child(user.getUid());

        fReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                chatListID.clear();

                for (DataSnapshot snapshot1 : snapshot.getChildren()){

                    ChatListModel idListModel = snapshot1.getValue(ChatListModel.class);
                    chatListID.add(idListModel);
                }

                // changed the adapter from the chatList method to here and it was faster loading
                adapter = new ChatListAdapter(mUsersID, getContext(), userName);
                recyclerView.setAdapter(adapter);

//                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        chatList();

        return view;
    }

    private void chatList(){

        mUsersID = new ArrayList<>();

        // Getting all recent chats;
        fReference = FirebaseDatabase.getInstance().getReference("UsersList")
                .child(user.getUid());
        fReference.orderByChild("timeSent").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                mUsersID.clear();

                for (DataSnapshot snapshot1 : snapshot.getChildren()){

                    // check and fetch out their id each
                    String user = snapshot1.getKey();
                    for (ChatListModel chatlist : chatListID){

                        if(user.equals(chatlist.getId())){
                            mUsersID.add(0, user);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // when you want to control your mainActivity from your fragment or fetch a method from your mainActivity, use attach to link up
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof FragmentListener) {
            fragmentListener = (FragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement FragmentListener");
        }
    }


//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }

//    @Override
//    public void onResume() {
//        super.onResume();
//    }
}











