package com.pixel.chatapp.view_controller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.pixel.chatapp.R;

public class PlayerFragOptionsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_frag_options);


        ConstraintLayout playerFilterLayout = findViewById(R.id.player_filter_layout);
        ImageView arrowBackS = playerFilterLayout.findViewById(R.id.arrowBackS);
        ImageView filterDone = playerFilterLayout.findViewById(R.id.doneClick);
        CheckBox freeModeCheck = playerFilterLayout.findViewById(R.id.freeModeCheck);
        CheckBox stakeModeCheck = playerFilterLayout.findViewById(R.id.stakeModeCheck);
        CheckBox orangeTickPlayerCheck = playerFilterLayout.findViewById(R.id.orangeTickPlayerCheck);
        CheckBox allPlayersCheck = playerFilterLayout.findViewById(R.id.allPlayersCheck);
        CheckBox whotGameCheck = playerFilterLayout.findViewById(R.id.whotGameCheck);
        CheckBox pokerGameCheck = playerFilterLayout.findViewById(R.id.pokerGameCheck);
        CheckBox chessGameCheck = playerFilterLayout.findViewById(R.id.chessGameCheck);
//        CheckBox whotGameCheck = playerFilterLayout.findViewById(R.id.whotGameCheck);

        ConstraintLayout player_sort_layout = findViewById(R.id.player_sort_layout);
        ImageView sortDone = player_sort_layout.findViewById(R.id.sortDone);
        ImageView backPress = player_sort_layout.findViewById(R.id.arrowBackS);
        RadioButton lowAmountSortRadio = player_sort_layout.findViewById(R.id.lowAmountSortRadio);
        RadioButton highAmountSortRadio = player_sort_layout.findViewById(R.id.highAmountSortRadio);
        RadioButton mostRecentSortRadio = player_sort_layout.findViewById(R.id.mostRecentSortRadio);

        String getFrom = getIntent().getStringExtra("from");

        if(getFrom.equals(getString(R.string.sortBy))){
            player_sort_layout.setVisibility(View.VISIBLE);

        } else if (getFrom.equals(getString(R.string.filterBy))) {
            playerFilterLayout.setVisibility(View.VISIBLE);

        }


        filterDone.setOnClickListener(v -> {
            Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
            finish();
        });

        sortDone.setOnClickListener(v -> {
            Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
            finish();
        });

        arrowBackS.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        backPress.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

    }




}






