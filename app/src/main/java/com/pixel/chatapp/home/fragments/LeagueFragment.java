package com.pixel.chatapp.home.fragments;

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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pixel.chatapp.R;
import com.pixel.chatapp.adapters.LeagueAdapter;
import com.pixel.chatapp.home.MainActivity;
import com.pixel.chatapp.utils.PhoneUtils;
import com.pixel.chatapp.model.LeagueModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LeagueFragment extends Fragment {

    public static LeagueFragment newInstance(){
        return new LeagueFragment();
    }

    ConstraintLayout backgroundConst;
    RecyclerView recyclerViewHost;
    LeagueAdapter leagueAdapter;
    List<LeagueModel> leagueModelList;
    ProgressBar progressBar, progressBarRefresh;
    TextView amountTV, sort_TV, filterTV;
    ImageView filterIV, refreshIV, searchNow_IV, cancelSearchAmountIV;
    CardView searchAmountContainer;
    EditText searchAmountET;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.league_fragment, container, false);

        backgroundConst = view.findViewById(R.id.backgroundConst);

        recyclerViewHost = view.findViewById(R.id.recyclerViewPlayer);
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

        recyclerViewHost.setLayoutManager(new LinearLayoutManager(getContext()));

        leagueModelList = new ArrayList<>();

        setColours();

        addHostList();

        leagueAdapter = new LeagueAdapter(leagueModelList, getContext());

        new Handler().postDelayed(()->{
            recyclerViewHost.setAdapter(leagueAdapter);
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

            Toast.makeText(getContext(), "work in progress", Toast.LENGTH_SHORT).show();

//            Intent intent = new Intent(getContext(), PlayerFragOptionsActivity.class);
//            intent.putExtra("from", getString(R.string.sortBy));
//
//            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(20).withEndAction(()->
//            {
//                startActivity(intent);
//
//                new Handler().postDelayed(()-> {
//                    v.setScaleX(1f);
//                    v.setScaleY(1f);
//                }, 300);
//            });
        });

        refreshIV.setOnClickListener(v -> {
            Toast.makeText(getContext(), "work in progress", Toast.LENGTH_SHORT).show();

        });

        View.OnClickListener filter = v ->
        {
            Toast.makeText(getContext(), "work in progress", Toast.LENGTH_SHORT).show();

//            Intent intent = new Intent(getContext(), PlayerFragOptionsActivity.class);
//            intent.putExtra("from", getString(R.string.filterBy));
//
//            filterIV.animate().scaleX(1.1f).scaleY(1.1f).setDuration(20).withEndAction(()->
//            {
//                startActivity(intent);
//
//                new Handler().postDelayed(()-> {
//                    filterIV.setScaleX(1f);
//                    filterIV.setScaleY(1f);
//                }, 300);
//            });
//
//            cancelSearchAmount();
        };

        filterIV.setOnClickListener(filter);
        filterTV.setOnClickListener(filter);



        return view;
    }

    private void setColours() {

        if(MainActivity.nightMood){
            backgroundConst.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blackApp));
            refreshIV.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.cool_orange)));
            filterIV.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.cool_orange)));
            progressBarRefresh.setIndeterminateTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.cool_orange)));
            progressBar.setIndeterminateTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.cool_orange)));

        } else {
            backgroundConst.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white));
            refreshIV.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.orange)));
            filterIV.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.orange)));
            progressBarRefresh.setIndeterminateTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.orange)));
            progressBar.setIndeterminateTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.orange)));

        }
    }

    private void addHostList() {

        long currentTimeMillis = System.currentTimeMillis();
        long oneHourAgoMillis = currentTimeMillis - (60 * 60 * 1000); // 1 hour * 60 minutes * 60 seconds * 1000 milliseconds
        long twoHourAgoMillis = currentTimeMillis - (120 * 60 * 1000); // 1 hour * 60 minutes * 60 seconds * 1000 milliseconds
        long threeHourAgoMillis = currentTimeMillis - (180 * 60 * 1000); // 1 hour * 60 minutes * 60 seconds * 1000 milliseconds
        long fourHourAgoMillis = currentTimeMillis - (270 * 60 * 1000); // 1 hour * 60 minutes * 60 seconds * 1000 milliseconds
        long fourDayAgo = currentTimeMillis - (270 * 60 * 1000 * 4); // 1 hour * 60 minutes * 60 seconds * 1000 milliseconds

        //1705516722058L

        leagueModelList.add(new LeagueModel("Uniuyo Poker League", "Free", "$4,000", "Poker",
                fourDayAgo, "Anyone", "1000", "topstar.com.ng", "topstar",
                "@uniuyo", "Join the game league and bw a winner"));

        leagueModelList.add(new LeagueModel("Delta Whot League", "$32", "$10,000", "Whot",
                System.currentTimeMillis(), "Anyone", "23500", "topstar.com.ng", "topstar",
                "@deltaState", "Join the game league and bw a winner"));

        leagueModelList.add(new LeagueModel("Prince Chess League", "Free", "$200", "Chess",
                twoHourAgoMillis, "Orange Tick Player", "10", "topstar.com.ng", "topstar",
                "@prince_eno", "Join the game league and bw a winner"));

        leagueModelList.add(new LeagueModel("Supermario Scrabble League", "Free", "$40", "Scrabble",
                fourHourAgoMillis, "Anyone", "12", "topstar.com.ng", "topstar",
                "@supermario", "Join the game league and bw a winner"));

        leagueModelList.add(new LeagueModel("Maroke Poker League", "Free", "$4000", "Poker",
                fourDayAgo, "Anyone", "100", "topstar.com.ng", "topstar",
                "@maroke", "Join the game league and bw a winner"));

        leagueModelList.add(new LeagueModel("Angel Poker League", "Free", "$800", "Poker",
                fourDayAgo, "Anyone", "200", "topstar.com.ng", "topstar",
                "@angel_poto", "Join the game league and bw a winner"));

        leagueModelList.add(new LeagueModel("Solo Poker League", "Free", "$380", "Poker",
                fourDayAgo, "Anyone", "10", "topstar.com.ng", "topstar",
                "@uniuyo", "Join the game league and bw a winner"));

        leagueModelList.add(new LeagueModel("Uniuyo Poker League", "Free", "$4000", "Poker",
                fourDayAgo, "Anyone", "10", "topstar.com.ng", "topstar",
                "@uniuyo", "Join the game league and bw a winner"));

        leagueModelList.add(new LeagueModel("Uniuyo Poker League", "Free", "$4000", "Poker",
                fourDayAgo, "Anyone", "10", "topstar.com.ng", "topstar",
                "@uniuyo", "Join the game league and bw a winner"));

        leagueModelList.add(new LeagueModel("Uniuyo Poker League", "Free", "$4000", "Poker",
                fourDayAgo, "Anyone", "10", "topstar.com.ng", "topstar",
                "@uniuyo", "Join the game league and bw a winner"));

        leagueModelList.add(new LeagueModel("Uniuyo Poker League", "Free", "$4000", "Poker",
                threeHourAgoMillis, "Anyone", "10", "topstar.com.ng", "topstar",
                "@uniuyo", "Join the game league and bw a winner"));

    }


    //  =====   methods
    private void cancelSearchAmount(){
        if(searchAmountContainer.getVisibility() == View.VISIBLE){
            searchAmountContainer.setVisibility(View.GONE);
            searchAmountET.setText(null);
            PhoneUtils.hideKeyboard(getContext(), searchAmountET);
        }
    }

}
