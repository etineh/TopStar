package com.pixel.chatapp.view_controller.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pixel.chatapp.R;
import com.pixel.chatapp.view_controller.PlayerFragOptionsActivity;
import com.pixel.chatapp.adapters.ChatListAdapter;
import com.pixel.chatapp.adapters.PlayerAdapter;
import com.pixel.chatapp.interface_listeners.FragmentListener;
import com.pixel.chatapp.dataModel.UserOnChatUI_Model;
import com.pixel.chatapp.utilities.LocalFileUtils;
import com.pixel.chatapp.utilities.PhoneUtils;
import com.pixel.chatapp.view_controller.MainActivity;
import com.pixel.chatapp.dataModel.PlayerModel;

import java.util.ArrayList;
import java.util.List;

public class PlayersFragment extends Fragment {

    public static PlayersFragment newInstance(){
        return new PlayersFragment();
    }

    ConstraintLayout mainBackground;
    RecyclerView recyclerViewPlayer, recyclerViewChat;
    private NestedScrollView nestedScrollView;
    private ConstraintLayout constraintLayout;

    PlayerAdapter playerAdapter, player2;
    List<PlayerModel> playerModelList, playerList;
    ProgressBar progressBar, progressBarRefresh;
    TextView amountTV, sort_TV, filterTV, recentChatTV;
    ImageView filterIV, refreshIV, searchNow_IV, cancelSearchAmountIV;
    CardView searchAmountContainer;
    EditText searchAmountET;
    FirebaseUser user;
    DatabaseReference refUsers;
    List<UserOnChatUI_Model> userList;
    @SuppressLint("StaticFieldLeak")
    public static ChatListAdapter adapter;
    MainActivity mainActivity = new MainActivity();

    ValueEventListener valueEventListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {

        View view = inflater.inflate(R.layout.players_fragment, container, false);

        mainBackground = view.findViewById(R.id.mainBackground);
        recentChatTV = view.findViewById(R.id.recentChatTV);
        recyclerViewChat = view.findViewById(R.id.recyclerChat);
        nestedScrollView = view.findViewById(R.id.nestedScrollView);
        constraintLayout = view.findViewById(R.id.subTopContainer);
//        nestedScrollView.setEnabled(false);

        recyclerViewPlayer = view.findViewById(R.id.recyclerViewPlayer);
        progressBar = view.findViewById(R.id.progressBar8);
        amountTV = view.findViewById(R.id.amountTV);
        sort_TV = view.findViewById(R.id.sort_TV);
        filterTV = view.findViewById(R.id.filterTV);
        filterIV = view.findViewById(R.id.filterIV);
        refreshIV = view.findViewById(R.id.refreshIV);
        progressBarRefresh = view.findViewById(R.id.refreshLoad);
        searchAmountContainer = view.findViewById(R.id.searchAmountContainer);
        searchAmountET = view.findViewById(R.id.searchAmountET);
        searchNow_IV = view.findViewById(R.id.searchNow_IV);
        cancelSearchAmountIV = view.findViewById(R.id.cancelSearchIV);

        setColours();

        user = FirebaseAuth.getInstance().getCurrentUser();
        refUsers = FirebaseDatabase.getInstance().getReference("UsersList").child(user.getUid());

        userList = new ArrayList<>();

        playerList = new ArrayList<>();
        addPlayerList2();


        // Set up the first RecyclerView
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(getContext()));
//        player2 = new PlayerAdapter(getContext(), playerList);
        new Handler().postDelayed(()->{
            List<UserOnChatUI_Model> userList = new ArrayList<>();
            try{
                userList = LocalFileUtils.readUserListFromFile(getContext());
            } catch (Exception e){
                System.out.println("what is error PlayerFragment L120: " + e.getMessage());
            }

            if(userList == null) {
                userListFromDatabase();
            } else {
                adapter = new ChatListAdapter(ChatsFragment.mUsersID, getContext(), getActivity());
                adapter.setFragmentListener((FragmentListener) getActivity());       // // Set MainActivity as the listener
                recyclerViewChat.setAdapter(adapter);
                progressBar.setVisibility(View.GONE);

            }


            new Handler().postDelayed(()-> progressBar.setVisibility(View.GONE), 4000);

        }, 100);

        recyclerViewPlayer.setLayoutManager(new LinearLayoutManager(getContext()));
        playerModelList = new ArrayList<>();
        addPlayerList();
        playerAdapter = new PlayerAdapter(getContext(), playerModelList);
        recyclerViewPlayer.setAdapter(playerAdapter);

//        new Handler().postDelayed(()->{
//            recyclerViewPlayer.setAdapter(playerAdapter);
//            progressBar.setVisibility(View.GONE);
//        }, 100);


        amountTV.setOnClickListener(v -> {
            searchAmountContainer.setVisibility(View.VISIBLE);
            searchNow_IV.setVisibility(View.VISIBLE);
            searchAmountET.requestFocus();
            PhoneUtils.showKeyboard(getContext(), searchAmountET);
        });

        cancelSearchAmountIV.setOnClickListener(v -> {
            cancelSearchAmount();
        });

        searchNow_IV.setOnClickListener(v -> {
            progressBarRefresh.setVisibility(View.VISIBLE);
            refreshIV.setVisibility(View.INVISIBLE);
            cancelSearchAmount();

            new Handler().postDelayed(()-> {
                refreshIV.setVisibility(View.VISIBLE);
                progressBarRefresh.setVisibility(View.GONE);
            }, 2_000);
        });

        sort_TV.setOnClickListener(v -> {

            Intent intent = new Intent(getContext(), PlayerFragOptionsActivity.class);
            intent.putExtra("from", getString(R.string.sortBy));

            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(20).withEndAction(()->
            {
                startActivity(intent);

                new Handler().postDelayed(()-> {
                    v.setScaleX(1f);
                    v.setScaleY(1f);
                }, 300);
            });
        });

        refreshIV.setOnClickListener(v -> {
            Toast.makeText(getContext(), "work in progress", Toast.LENGTH_SHORT).show();

        });

        View.OnClickListener filter = v ->
        {
            Intent intent = new Intent(getContext(), PlayerFragOptionsActivity.class);
            intent.putExtra("from", getString(R.string.filterBy));

            filterIV.animate().scaleX(1.1f).scaleY(1.1f).setDuration(20).withEndAction(()->
            {
                startActivity(intent);

                new Handler().postDelayed(()-> {
                    filterIV.setScaleX(1f);
                    filterIV.setScaleY(1f);
                }, 300);
            });

            cancelSearchAmount();
        };

        filterIV.setOnClickListener(filter);
        filterTV.setOnClickListener(filter);


        return view;

    }


    //  =========   methods
    private void setColours()
    {
        if(MainActivity.nightMood)
        {
            mainBackground.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blackApp));
            sort_TV.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            amountTV.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            progressBar.setIndeterminateTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.cool_orange)));
            recentChatTV.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            refreshIV.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.cool_orange)));
            filterIV.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.cool_orange)));
        } else {
            mainBackground.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white));
            sort_TV.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            amountTV.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            progressBar.setIndeterminateTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.orange)));
            recentChatTV.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            refreshIV.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.orange)));
            filterIV.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.orange)));
        }

    }

    public void notifyUserMoved(int i){
        if(adapter != null) {
            adapter.notifyItemMoved(i, 0);
            adapter.notifyItemRangeChanged(i, adapter.userModelList.size(), new Object());
        }
    }

    public void notifyItemChanged(int i){
        if(adapter != null) adapter.notifyItemChanged(i, new Object());
    }

    public void notifyItemInserted(int i){
        if(adapter != null) adapter.notifyItemInserted(i);
    }

    public void notifyDataFullSet(){
        adapter.notifyDataSetChanged();
    }

    public void notifyVisibleUser()
    {
        adapter.notifyItemRangeChanged(0, adapter.userModelList.size(), new Object());
//        for (int i = 0; i < adapter.userModelList.size(); i++) {
//            adapter.notifyItemChanged(i, new Object());
//        }
    }

    private void userListFromDatabase()
    {
        adapter = new ChatListAdapter(userList, getContext(), getActivity());
        adapter.setFragmentListener((FragmentListener) getActivity());       // // Set MainActivity as the listener
        recyclerViewChat.setAdapter(adapter);

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                new Thread(()->
                {
                    try{
//                        int itemCount = (int) snapshot.getChildrenCount();
//                        int processedItems = 0;

                        for (DataSnapshot snapshot1 : snapshot.getChildren())
                        {
                            UserOnChatUI_Model userModel = snapshot1.getValue(UserOnChatUI_Model.class);
                            String otherUid = snapshot1.getKey();
                            assert userModel != null; assert otherUid != null;
                            userModel.setOtherUid(otherUid);
                            userModel.setMyUid(user.getUid());

                            boolean userAlreadyExists = false;
                            for (UserOnChatUI_Model userModelLoop : userList) {
                                if (userModelLoop.getOtherUid().equals(otherUid) && userModelLoop.getMyUid().equals(user.getUid())) {
                                    userAlreadyExists = true;
                                    break;
                                }
                            }
                                // If the user doesn't exist, add it to the list
                            if (!userAlreadyExists && userModel.getMessage() != null && userList.size() < 4)
                            {
                                userList.add(0, userModel);
                                getActivity().runOnUiThread(() -> {
                                    adapter.notifyDataSetChanged();
                                    progressBar.setVisibility(View.GONE);
                                });

                            }
                        }
                    } catch (Exception e){
//                        handlerInternet.post(()-> Toast.makeText(getContext(), "Error CFragment L270 " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        System.out.println("what is error in PlayerFragment L300 " + e.getMessage());
                    }

                }).start();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("what is error ChatFragment L300 " + error.getMessage());

            }
        };

        refUsers.orderByChild("timeSent").limitToLast(3).addValueEventListener(valueEventListener);

    }

    private void addPlayerList(){

        long currentTimeMillis = System.currentTimeMillis();
        long oneHourAgoMillis = currentTimeMillis - (60 * 60 * 1000); // 1 hour * 60 minutes * 60 seconds * 1000 milliseconds
        long twoHourAgoMillis = currentTimeMillis - (120 * 60 * 1000); // 1 hour * 60 minutes * 60 seconds * 1000 milliseconds
        long threeHourAgoMillis = currentTimeMillis - (180 * 60 * 1000); // 1 hour * 60 minutes * 60 seconds * 1000 milliseconds
        long fourHourAgoMillis = currentTimeMillis - (270 * 60 * 1000); // 1 hour * 60 minutes * 60 seconds * 1000 milliseconds

        //1705516722058L
        PlayerModel model = new PlayerModel("Ochuko", "chess, Whot", "Free", "$30",
                System.currentTimeMillis(), "", null, 1, getString(R.string.yourContacts));
//        model.setFromWhere(getString(R.string.yourContacts));
        playerModelList.add(model);

        playerModelList.add(new PlayerModel("Prince Mafo", "chess, Whot", "Mode: Free", "Amount: $30 - $100",
                System.currentTimeMillis(), "", null, 2, null));

        playerModelList.add(new PlayerModel("Ochuko Gamer", " Whot", "Mode: Free or Stake", "Amount: $50 - $80",
                1705516722058L, "", null, 2, null));

        playerModelList.add(new PlayerModel("Course Mate", "Scrabble, Whot", "Mode: Stake", "Amount: $10 - $70",
                oneHourAgoMillis, "", null, 2, null));

        playerModelList.add(new PlayerModel("Mario Friend", "Poker, Chess, Whot", "Mode: Free", "Amount: $2 - $5",
                threeHourAgoMillis, "", null, 2, null));

        playerModelList.add(new PlayerModel("Fejiro Poto", "chess, Scrabble", "Free or Stake", "Amount: $5 - $7",
                twoHourAgoMillis, "", null, 2, null));

        playerModelList.add(new PlayerModel("Praise Umoro", "Poker, Whot", "Mode: Stake", " Amount: $3 - $24.5",
                System.currentTimeMillis(), "", null, 2, null));

        //      =============== global

        PlayerModel model2 = new PlayerModel("Ochuko", "chess, Whot", "Mode: Free", "Amount: $30",
                System.currentTimeMillis(), "", null, 1, getString(R.string.globalPlayer));
        playerModelList.add(model2);

        playerModelList.add(new PlayerModel("Kin Caros", "chess, poker", "Mode: Stake", "$30",
                fourHourAgoMillis, "", null, 2, null));

        playerModelList.add(new PlayerModel("Ochuko De Player", "chess, Whot", "Free or Stake", "$10",
                System.currentTimeMillis(), "", null, 2, null));

        playerModelList.add(new PlayerModel("Sandra Baby", "Poker, Whot", "Mode: Stake", "$0.3",
                1705516722058L, "", null, 2, null));

        playerModelList.add(new PlayerModel("Julius Okope", "Scrabble, Whot", "Mode: Stake", "$30.4",
                fourHourAgoMillis, "", null, 2, null));

        playerModelList.add(new PlayerModel("Omo Kin Brain", "Poker, Whot", "Free", "$300",
                System.currentTimeMillis(), "", null, 2, null));

        playerModelList.add(new PlayerModel("King Leo", "Scrabble, Whot", "Free", "$100",
                threeHourAgoMillis, "", null, 2, null));

        playerModelList.add(new PlayerModel("Moses Fran", "Whot, Poker", "Free", "$60.5",
               oneHourAgoMillis, "", null, 2, null));

        playerModelList.add(new PlayerModel("Mary Job", "Poker, Whot", "Free", "$10",
                twoHourAgoMillis, "", null, 2, null));

        playerModelList.add(new PlayerModel("Meri Mark", "chess, Poker", "Free", "$30 - $90",
                1705516722058L, "", null, 2, null));

        playerModelList.add(new PlayerModel("Angela Angel", "Chess, Whot", "Free or Stake", "$10 - $80",
                System.currentTimeMillis(), "", null, 2, null));

        playerModelList.add(new PlayerModel("Kim Pius", "Whot, Chess, Ludo", "Stake", "$20 - $40",
                fourHourAgoMillis, "", null, 2, null));

    }

    private void addPlayerList2(){

        playerList.add(new PlayerModel("Prince Mafo", "chess, Whot", "Mode: Free", "Amount: $30 - $100",
                System.currentTimeMillis(), "", null, 2, null));

        playerList.add(new PlayerModel("Ochuko Gamer", " Whot", "Mode: Free or Stake", "Amount: $50 - $80",
                1705516722058L, "", null, 2, null));

        playerList.add(new PlayerModel("Course Mate", "Scrabble, Whot", "Mode: Stake", "Amount: $10 - $70",
                1705516722058L, "", null, 2, null));


    }

    private void cancelSearchAmount(){
        if(searchAmountContainer.getVisibility() == View.VISIBLE){
            searchAmountContainer.setVisibility(View.GONE);
            searchAmountET.setText(null);
            PhoneUtils.hideKeyboard(getContext(), searchAmountET);
        }
    }

}













