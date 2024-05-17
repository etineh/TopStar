package com.pixel.chatapp.side_bar_menu.support;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.pixel.chatapp.R;
import com.pixel.chatapp.adapters.SupportUserAdapter;
import com.pixel.chatapp.model.SupportUserM;

import java.util.ArrayList;
import java.util.List;

public class SupportUserActivity extends AppCompatActivity {

    SupportUserAdapter adapter;
    List<SupportUserM> userLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_user);

        ImageView arrowBackS = findViewById(R.id.arrowBackS);
        ImageView search_ = findViewById(R.id.search_);
        RecyclerView recyclerViewUser = findViewById(R.id.recyclerViewUser);
        Button newChat_button = findViewById(R.id.newChat_button);
        ProgressBar progressBar = findViewById(R.id.progressBar6);

        recyclerViewUser.setLayoutManager(new LinearLayoutManager(this));

        userLists = new ArrayList<>();

        addSupportUser();

        adapter = new SupportUserAdapter(userLists, this);

        new Handler().postDelayed(()->
        {
            recyclerViewUser.setAdapter(adapter);

            new Handler().postDelayed(()-> progressBar.setVisibility(View.GONE), 500);

        }, 500);


        newChat_button.setOnClickListener(v -> {
            Toast.makeText(this, "in progress", Toast.LENGTH_SHORT).show();
        });


        search_.setOnClickListener(v -> {
            Toast.makeText(this, "in progress", Toast.LENGTH_SHORT).show();
        });

        arrowBackS.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

    }


    private void addSupportUser(){
        userLists.add(new SupportUserM("id", "Mario","700024", 1705516722058L,
                "Give us a good review", "4"));

        userLists.add(new SupportUserM("id2", "Fejiro","700016", 1705516722058L,
                "it will be available from next week", "2"));

        userLists.add(new SupportUserM("id3", "Princess","700033", 1705516722058L,
                "Thank you for taking out time to reach out to us", "1"));

        userLists.add(new SupportUserM("id3", "Great","700024", 1705516722058L,
                "It's great to be of assist to you. Have a lovely day!", "5"));

    }


}