package com.pixel.chatapp.activities;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.pixel.chatapp.R;
import com.pixel.chatapp.adapters.LiveGameAdapter;
import com.pixel.chatapp.all_utils.PhoneUtils;
import com.pixel.chatapp.model.LiveGameM;

import java.util.ArrayList;
import java.util.List;

public class LiveGameActivity extends AppCompatActivity {

    CardView searchTitleContainer;
    RecyclerView recyclerViewPlayer;
    EditText searchTitleET;
    private List<LiveGameM> gameMList;
    LiveGameAdapter gameAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);

        ImageView arrowBackS = findViewById(R.id.arrowBackS);
        ImageView refreshIV = findViewById(R.id.refreshIV);
        ImageView searchIV = findViewById(R.id.searchIV);
        searchTitleContainer = findViewById(R.id.searchTitleContainer);
        ImageView searchNow_IV = findViewById(R.id.searchNow_IV);
        searchTitleET = findViewById(R.id.searchTitleET);
        ImageView cancelSearchIV = findViewById(R.id.cancelSearchIV);
        recyclerViewPlayer = findViewById(R.id.recyclerViewLive);
        ProgressBar progressLive = findViewById(R.id.progressLive);
        ProgressBar refreshLoad = findViewById(R.id.refreshLoad);

        recyclerViewPlayer.setLayoutManager(new LinearLayoutManager(this));

        gameMList = new ArrayList<>();

        setLiveGameList();

        gameAdapter = new LiveGameAdapter(gameMList, this);

        new Handler().postDelayed(()->{
            recyclerViewPlayer.setAdapter(gameAdapter);
            progressLive.setVisibility(View.GONE);
        }, 100);

        searchIV.setOnClickListener(v -> {
            searchTitleContainer.setVisibility(View.VISIBLE);
            PhoneUtils.showKeyboard(this, searchTitleET);
            searchTitleET.requestFocus();
        });

        searchNow_IV.setOnClickListener(v -> {
            refreshIV.setVisibility(View.INVISIBLE);
            refreshLoad.setVisibility(View.VISIBLE);
            searchTitleContainer.setVisibility(View.GONE);
            PhoneUtils.hideKeyboard(this, searchTitleET);
            searchTitleET.setText(null);

            new Handler().postDelayed(()-> {
                refreshLoad.setVisibility(View.GONE);
                refreshIV.setVisibility(View.VISIBLE);
            }, 2_000);
        });

        cancelSearchIV.setOnClickListener(v -> {
            searchTitleContainer.setVisibility(View.GONE);
            searchTitleET.setText(null);
            PhoneUtils.hideKeyboard(this, searchTitleET);

        });

        refreshIV.setOnClickListener(v -> {
            Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
        });


        arrowBackS.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        getOnBackPressedDispatcher().addCallback(callback);
    }




    private void setLiveGameList()
    {
        LiveGameM liveGameM1 = new LiveGameM("230", "Prince Whot Tournament", "Whot", "68",
                "@ndifrekeSunday\n vs \n@maro_great", "", "", "$60", "@princeMaro");
        gameMList.add(liveGameM1);

        LiveGameM liveGameM2 = new LiveGameM("13k", "Delta Poker Tournament", "Poker", "1225",
                "@fejiroBaby \nvs \nflavour_gamer", "", "", "$1,000", "@deltaState");
        gameMList.add(liveGameM2);

        LiveGameM liveGameM3 = new LiveGameM("860", "Uniuyo Chess Tournament", "Chess", "102",
                "@ndifrekeSunday \nvs \n@maro_great", "", "", "$600", "@uniuyo");
        gameMList.add(liveGameM3);

        LiveGameM liveGameM4 = new LiveGameM("20", "Victor Poker Tournament", "Poker", "67",
                "@kissMe \nvs \n@pokerDeGreat", "", "", "$20", "@champ_channel");
        gameMList.add(liveGameM4);

        LiveGameM liveGameM5 = new LiveGameM("50", "Lagos Scrabble Tournament", "Scrabble", "67",
                "@Kelvin \nvs \n@OmoroKen", "", "", "$50", "@lagosState");
        gameMList.add(liveGameM5);

        LiveGameM liveGameM6 = new LiveGameM("2k", "Prince Whot Tournament", "Whot", "67",
                "@ndifrekeSunday \nvs \n@maro_great", "", "", "$1000", "@princeMaro");
        gameMList.add(liveGameM6);

        LiveGameM liveGameM7 = new LiveGameM("23k", "Prince Poker Tournament", "Poker", "67",
                "@ndifrekeSunday \nvs \n@maro_great", "", "", "$60", "@princeMaro");
        gameMList.add(liveGameM7);

        LiveGameM liveGameM8 = new LiveGameM("4k", "Prince Poker Tournament", "Poker", "20",
                "@ndifrekeSunday vs @maro_great", "", "", "$20", "@princeMaro");
        gameMList.add(liveGameM8);

        LiveGameM liveGameM9 = new LiveGameM("230", "Prince Whot Tournament", "Whot", "10",
                "@ndifrekeSunday vs @maro_great", "", "", "$80", "@princeMaro");
        gameMList.add(liveGameM9);

        LiveGameM liveGameM10 = new LiveGameM("130", "Prince Whot Tournament", "Whot", "22",
                "@ndifrekeSunday vs @maro_great", "", "", "$87", "@princeMaro");
        gameMList.add(liveGameM10);

    }


    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if(searchTitleContainer.getVisibility() == View.VISIBLE)
            {
                searchTitleContainer.setVisibility(View.GONE);
                searchTitleET.setText(null);
                PhoneUtils.hideKeyboard(LiveGameActivity.this, searchTitleET);
            } else {
                finish();
            }
        }
    };

}









