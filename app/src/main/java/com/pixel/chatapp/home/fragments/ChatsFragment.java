package com.pixel.chatapp.home.fragments;

import static com.pixel.chatapp.home.MainActivity.contactNameShareRef;
import static com.pixel.chatapp.home.MainActivity.handlerInternet;
import static com.pixel.chatapp.home.MainActivity.myProfileShareRef;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pixel.chatapp.Permission.Permission;
import com.pixel.chatapp.adapters.ChatListAdapter;
import com.pixel.chatapp.constants.AllConstants;
import com.pixel.chatapp.interface_listeners.FragmentListener;
import com.pixel.chatapp.R;
import com.pixel.chatapp.contacts.UsersContactActivity;
import com.pixel.chatapp.home.MainActivity;
import com.pixel.chatapp.model.UserOnChatUI_Model;
import com.pixel.chatapp.roomDatabase.viewModels.UserChatViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment {

    public static ChatsFragment newInstance(){
        return new ChatsFragment();
    }

    public static RecyclerView recyclerView;
    FirebaseUser user;
    DatabaseReference refUsers;

    String myUserName;
    private UserChatViewModel userViewModel;
    ExecutorService executors = Executors.newSingleThreadExecutor();

    static ChatListAdapter adapter;
    public static CircleImageView openContactList;

    private List<UserOnChatUI_Model> chatListID;
    public static List<UserOnChatUI_Model> mUsersID;
    private FragmentListener fragmentListener;

    Permission permissionCheck = new Permission();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {

        View view = inflater.inflate(R.layout.alert_fragment, container, false);

        openContactList = view.findViewById(R.id.openContactList);
        recyclerView = view.findViewById(R.id.recyclerViewChatList);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if(!MainActivity.sharingPhotoActivated){
            openContactList.setVisibility(View.VISIBLE);
        } else {
            openContactList.setVisibility(View.INVISIBLE);
        }

        chatListID = new ArrayList<>();
        mUsersID = new ArrayList<>();

        user = FirebaseAuth.getInstance().getCurrentUser();
//        refChecks = FirebaseDatabase.getInstance().getReference("Checks");

        userViewModel = new ViewModelProvider(requireActivity()).get(UserChatViewModel.class);
        // get users from ROOM database
        executors.execute(() -> {
            if(userViewModel.getAllUsers() != null){
                mUsersID = userViewModel.getAllUsers();

                adapter = new ChatListAdapter(mUsersID, getContext(), getActivity());
                adapter.setFragmentListener((FragmentListener) getActivity());       // // Set MainActivity as the listener

                getActivity().runOnUiThread(() -> {
                    recyclerView.setAdapter(adapter);
                });
            }
        });



        // Go to contact
        openContactList.setOnClickListener(v ->
        {
            if(permissionCheck.isContactOk(getContext()))
            {
                v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(10).withEndAction(() ->
                {
                    Intent intent = new Intent(getContext(), UsersContactActivity.class);
                    startActivity(intent);
                }).start();

                new Handler().postDelayed(() -> {
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                }, 500);

            } else {
                permissionCheck.requestContact(getActivity());
            }

        });


        chatList();

        return view;
    }

    //  ---------------  All   methods     -----------------

    // find user and delete from the ChatList
    public static void findUserPositionByUID(String userUid) {
        if (mUsersID != null) {
            for (int i = mUsersID.size() - 1; i >= 0; i--) {
                if (mUsersID.get(i).getOtherUid().equals(userUid)) {
                    // Remove the item from its old position.
                    mUsersID.remove(i);
                    adapter.notifyItemRemoved(i);
                    // Exit the loop, as we've found the item and moved it.
                    break;
                }
            }
        }
    }

    public static void findUserAndDeleteChat(String userUid, String chatId) {
        if (mUsersID != null) {
            for (int i = mUsersID.size() - 1; i >= 0; i--) {
                // check if it's same user ID
                if (mUsersID.get(i).getOtherUid().equals(userUid)) {
                    // check if it's same message ID
                    if(mUsersID.get(i).getIdKey().equals(chatId)){
                        // Remove the chat from its old position.
                        mUsersID.get(i).setMessage(AllConstants.DELETE_ICON +"  ...");
//                        mUsersID.get(i).setEmojiOnly("...");
                        adapter.notifyItemChanged(i, new Object());
                    }
                    break;
                }
            }
        }
    }

    public static void findUserAndEditChat(String userUid, String chatId, String chat, String emoji) {
        if (mUsersID != null) {
            for (int i = mUsersID.size() - 1; i >= 0; i--) {
                // check if it's same user ID
                if (mUsersID.get(i).getOtherUid().equals(userUid)) {
                    // check if it's same message ID
                    if(mUsersID.get(i).getIdKey().equals(chatId)){
                        if(chat != null){
                            mUsersID.get(i).setMessage(AllConstants.EDIT_ICON + chat);
                        } else {
                            mUsersID.get(i).setMessage(AllConstants.EDIT_ICON + emoji);
                        }

                        adapter.notifyItemChanged(i, new Object());
                    }
                    break;
                }
            }
        }
    }

    public static void notifyUserMoved(int i){
        adapter.notifyItemMoved(i, 0);
    }
    public static void notifyItemChanged(int i){
        adapter.notifyItemChanged(i, new Object());
    }
    private void chatList()
    {

        adapter = new ChatListAdapter(mUsersID, getContext(), getActivity());

        adapter.setFragmentListener((FragmentListener) getActivity());       // // Set MainActivity as the listener
        recyclerView.setAdapter(adapter);

        // Getting all my user chats only;
        refUsers = FirebaseDatabase.getInstance().getReference("UsersList").child(user.getUid());

        refUsers.orderByChild("timeSent").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                new Thread(()->
                {
                    try{
                        for (DataSnapshot snapshot1 : snapshot.getChildren()){
                            UserOnChatUI_Model userModel = snapshot1.getValue(UserOnChatUI_Model.class);
                            String userId = snapshot1.getKey();
                            userModel.setOtherUid(userId);

                            boolean userAlreadyExists = false;
                            for (UserOnChatUI_Model userModelLoop : mUsersID) {
                                if (userModelLoop.getOtherUid().equals(userId)) {
            System.out.println("what is userId: " + userId + " uid: " + userModelLoop.getOtherUid());
                                    userAlreadyExists = true;
                                    break;
                                }
                            }

                            if (!userAlreadyExists) {
                                // If the user doesn't exist, add it to the list
                                mUsersID.add(0, userModel);

                                // add to local database
                                userViewModel.insertUser(userModel);

                                handlerInternet.post(()-> adapter.notifyDataSetChanged());

                            }
                        }
                    } catch (Exception e){
                        handlerInternet.post(()-> Toast.makeText(getContext(), "Error here " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        System.out.println("what is error + CF L245 " + e.getMessage());
                    }

                }).start();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


//        getActivity().runOnUiThread(() -> {
//        });

    }

    //   ------------ Message Methods

    public void updateAdapterData(String data) {
//        if (adapter != null) {
//            adapter.updateData(newData);
//        }
        System.out.println("This is the "+data);
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

    public static void setOpenContactList() {
        openContactList.setVisibility(View.INVISIBLE);
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











