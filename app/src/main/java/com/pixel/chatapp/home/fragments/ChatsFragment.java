package com.pixel.chatapp.home.fragments;

import static com.pixel.chatapp.home.MainActivity.handlerInternet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
import com.pixel.chatapp.utils.AnimUtils;
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
    ProgressBar progressBarLoadChatList;
    FirebaseUser user;
    DatabaseReference refUsers;

    private UserChatViewModel userViewModel;
    ExecutorService executors = Executors.newSingleThreadExecutor();

    @SuppressLint("StaticFieldLeak")
    public static ChatListAdapter adapter;
    public static CircleImageView openContactList;

    private List<UserOnChatUI_Model> chatListID;
    public static List<UserOnChatUI_Model> mUsersID;

    Permission permissionCheck = new Permission();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {

        View view = inflater.inflate(R.layout.chatlist_fragment, container, false);

        openContactList = view.findViewById(R.id.openContactList);
        recyclerView = view.findViewById(R.id.recyclerViewChatList);
        progressBarLoadChatList = view.findViewById(R.id.progressBarLoadChatList);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if(!MainActivity.sharingPhotoActivated && !MainActivity.onForward){
            AnimUtils.animateView(openContactList);
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
            if(userViewModel.getAllUsers(user.getUid()) != null){
                mUsersID = userViewModel.getAllUsers(user.getUid());

                adapter = new ChatListAdapter(mUsersID, getContext(), getActivity());
                adapter.setFragmentListener((FragmentListener) getActivity());       // // Set MainActivity as the listener

                getActivity().runOnUiThread(() -> {
                    recyclerView.setAdapter(adapter);
                    progressBarLoadChatList.setVisibility(View.GONE);
                    chatList(false);
                });

            } else {
                chatList(true);
            }
        });


        // Go to contact
        openContactList.setOnClickListener(v ->
        {
            if(permissionCheck.isContactOk(getContext()))
            {
                v.animate().scaleX(1.1f).scaleY(1.1f).withEndAction(() ->
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


        return view;
    }

    //  ---------------  All   methods     -----------------

    public void notifyDataFullSet(){
        adapter.notifyDataSetChanged();
    }

    public void notifyVisibleUser()
    {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;

            int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
            int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();

            // Ensure the indices are within bounds
            if (firstVisibleItemPosition < 0 || lastVisibleItemPosition < 0) {
//                adapter.notifyDataSetChanged();
                return; // Indicating no valid position found
            }

            for (int i = lastVisibleItemPosition + 2; i >= firstVisibleItemPosition; i--) {

                assert adapter != null;
                if (i < adapter.userModelList.size()) { // Check if index is within bounds

                    adapter.notifyItemChanged(i, new Object());
                }
            }
        }
    }

    public void notifyItemInserted(int i){
        if(adapter != null) adapter.notifyItemInserted(i);
    }

    public void notifyUserMoved(int i){
        if(adapter != null) {
            adapter.notifyItemMoved(i, 0);
            adapter.notifyItemRangeChanged(i, adapter.userModelList.size(), new Object());
        }
    }
    public void notifyItemChanged(int i){
        adapter.notifyItemChanged(i, new Object());
    }

    private void chatList(boolean isRoomDbNull)
    {
        if(isRoomDbNull){
            adapter = new ChatListAdapter(mUsersID, getContext(), getActivity());
            adapter.setFragmentListener((FragmentListener) getActivity());       // // Set MainActivity as the listener
            recyclerView.setAdapter(adapter);
            progressBarLoadChatList.setVisibility(View.GONE);
        }

        // Getting all my user chats only;
        refUsers = FirebaseDatabase.getInstance().getReference("UsersList").child(user.getUid());

        refUsers.orderByChild("timeSent").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                new Thread(()->
                {
                    try{
                        for (DataSnapshot snapshot1 : snapshot.getChildren())
                        {
//                            int itemCount = (int) snapshot.getChildrenCount();
//                            int processedItems = 0;

                            UserOnChatUI_Model userModel = snapshot1.getValue(UserOnChatUI_Model.class);
                            String otherUid = snapshot1.getKey();
                            userModel.setOtherUid(otherUid);
                            userModel.setMyUid(user.getUid());

                            boolean userAlreadyExists = false;
                            for (UserOnChatUI_Model userModelLoop : mUsersID) {
                                if (userModelLoop.getOtherUid().equals(otherUid) && userModelLoop.getMyUid().equals(user.getUid())) {
                                    userAlreadyExists = true;
                                    break;
                                }
                            }

                            // If the user doesn't exist, add it to the list
                            if (!userAlreadyExists && userModel.getMessage() != null)
                            {
                                mUsersID.add(0, userModel);
                                // add to local database
                                userViewModel.insertUser(userModel);

                                getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());

                            }

                        }
                    } catch (Exception e){
//                        handlerInternet.post(()-> Toast.makeText(getContext(), "Error CFragment L270 " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        System.out.println("what is error in CFragment L230 " + e.getMessage());
                    }

                }).start();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("what is error ChatFragment L270 " + error.getMessage());

            }
        });

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
            FragmentListener fragmentListener = (FragmentListener) context;
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











