package com.pixel.chatapp.side_bar_menu.dashboard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.pixel.chatapp.R;
import com.pixel.chatapp.adapters.GameRankAdapter;
import com.pixel.chatapp.adapters.UpcomingHostAdapter;
import com.pixel.chatapp.all_utils.OpenActivityUtil;
import com.pixel.chatapp.model.GameRankM;
import com.pixel.chatapp.model.UpcomingHostM;
import com.pixel.chatapp.side_bar_menu.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    ImageView setting_IV, arrowBackP;
    RecyclerView recyclerMatch, recyclerViewMyHost, recyclerViewRank;

    UpcomingHostAdapter otherHostAdapter, myHostAdapter;
    GameRankAdapter rankAdapter;

    List<UpcomingHostM> otherHostList, myHostMList;
    List<GameRankM> gameRankMList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        setting_IV = findViewById(R.id.setting_IV);
        arrowBackP = findViewById(R.id.arrowBackP);
        recyclerMatch = findViewById(R.id.recyclerMatch);
        recyclerViewMyHost = findViewById(R.id.recyclerViewMyHost);
        recyclerViewRank = findViewById(R.id.recyclerViewRank);

        recyclerMatch.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMyHost.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRank.setLayoutManager(new GridLayoutManager(this, 2));

        otherHostList = new ArrayList<>();
        myHostMList = new ArrayList<>();
        gameRankMList = new ArrayList<>();

        otherHostList.add(new UpcomingHostM("logoPath", "Uniuyo Chess Tournament", "Starts: 24/02/2025"));
        otherHostList.add(new UpcomingHostM("logoPath", "Edo Community WHOT Game", "Starts: 20/02/2025"));
        otherHostList.add(new UpcomingHostM("logoPath", "Prince Ike Poker Game", "Starts: 13/03/2025"));

        otherHostAdapter = new UpcomingHostAdapter(this, otherHostList);
        recyclerMatch.setAdapter(otherHostAdapter);


        myHostMList.add(new UpcomingHostM("logoPath", "Ndifreke Poker League", "Starts: 04/03/2025"));
        myHostMList.add(new UpcomingHostM("logoPath", "Free Chess Game", "Starts: 22/04/2025"));
        myHostMList.add(new UpcomingHostM("logoPath", "Logic Scrabble Game", "Starts: 29/04/2025"));

        myHostAdapter = new UpcomingHostAdapter(this, myHostMList);
        recyclerViewMyHost.setAdapter(myHostAdapter);


        gameRankMList.add(new GameRankM(getString(R.string.poker), getString(R.string.totalPlay)+" 60x",
                getString(R.string.win)+" 50", getString(R.string.loss)+" 10", getString(R.string.worldRank) +"\n62nd / 540,210"));
        gameRankMList.add(new GameRankM(getString(R.string.whot), getString(R.string.totalPlay)+" 100x",
                getString(R.string.win)+" 40", getString(R.string.loss)+" 60", getString(R.string.worldRank) +"\n262nd / 540,210"));
        gameRankMList.add(new GameRankM(getString(R.string.chess), getString(R.string.totalPlay)+" 80x",
                getString(R.string.win)+" 60", getString(R.string.loss)+" 20", getString(R.string.worldRank) +"\n101st / 540,210"));
        gameRankMList.add(new GameRankM(getString(R.string.scrabble), getString(R.string.totalPlay)+" 52x",
                getString(R.string.win)+" 45", getString(R.string.loss)+" 7", getString(R.string.worldRank) +"\n12th / 540,210"));

        rankAdapter = new GameRankAdapter(this, gameRankMList);
        recyclerViewRank.setAdapter(rankAdapter);



        arrowBackP.setOnClickListener(v -> {
            onBackPressed();
        });

        setting_IV.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            OpenActivityUtil.openColorHighlight(v, this, intent);
//            finish();
        });

    }










}












