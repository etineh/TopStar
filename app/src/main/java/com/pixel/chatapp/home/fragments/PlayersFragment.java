package com.pixel.chatapp.home.fragments;

import android.content.Intent;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pixel.chatapp.R;
import com.pixel.chatapp.activities.PlayerFragOptionsActivity;
import com.pixel.chatapp.adapters.PlayerAdapter;
import com.pixel.chatapp.utils.PhoneUtils;
import com.pixel.chatapp.home.MainActivity;
import com.pixel.chatapp.model.PlayerModel;

import java.util.ArrayList;
import java.util.List;

public class PlayersFragment extends Fragment {

    public static PlayersFragment newInstance(){
        return new PlayersFragment();
    }

    RecyclerView recyclerViewPlayer;
    PlayerAdapter playerAdapter;
    List<PlayerModel> playerModelList;
    ProgressBar progressBar, progressBarRefresh;
    TextView amountTV, sort_TV, filterTV;
    ImageView filterIV, refreshIV, searchNow_IV, cancelSearchAmountIV;
    CardView searchAmountContainer;
    EditText searchAmountET;

    MainActivity mainActivity = new MainActivity();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {

        View view = inflater.inflate(R.layout.players_fragment, container, false);

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

        recyclerViewPlayer.setLayoutManager(new LinearLayoutManager(getContext()));

        playerModelList = new ArrayList<>();

        addPlayerList();

        playerAdapter = new PlayerAdapter(getContext(), playerModelList);

        new Handler().postDelayed(()->{
            recyclerViewPlayer.setAdapter(playerAdapter);
            progressBar.setVisibility(View.GONE);
        }, 100);


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
                System.currentTimeMillis(), "", null, 1, getString(R.string.global));
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

    private void cancelSearchAmount(){
        if(searchAmountContainer.getVisibility() == View.VISIBLE){
            searchAmountContainer.setVisibility(View.GONE);
            searchAmountET.setText(null);
            PhoneUtils.hideKeyboard(getContext(), searchAmountET);
        }
    }

}













