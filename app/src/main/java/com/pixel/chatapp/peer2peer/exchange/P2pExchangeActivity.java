package com.pixel.chatapp.peer2peer.exchange;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pixel.chatapp.R;
import com.pixel.chatapp.adapters.P2pExchangeAdapter;
import com.pixel.chatapp.model.P2pExchangeM;

import java.util.ArrayList;
import java.util.List;

public class P2pExchangeActivity extends AppCompatActivity {


    TextView buy_Button, sell_Button;
    TextView pending_order_TV;
    ImageView filter_IV, closeP2p_Button, searchButton, liveSupport_Button;
    EditText seachAmount_ET;
    ProgressBar progressBarP;

    List<P2pExchangeM> p2pExchangeMList;
    RecyclerView recyclerViewP2p;
    P2pExchangeAdapter p2pExchangeAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p2p_exchange);

        buy_Button = findViewById(R.id.buy_Button);
        sell_Button = findViewById(R.id.sell_Button);
        pending_order_TV = findViewById(R.id.pending_order_TV);
        filter_IV = findViewById(R.id.filter_IV);
        closeP2p_Button = findViewById(R.id.closeP2p_Button);
        seachAmount_ET = findViewById(R.id.seachAmount_ET);
        searchButton = findViewById(R.id.searchButton);
        liveSupport_Button = findViewById(R.id.liveSupport_Button);
        recyclerViewP2p = findViewById(R.id.recyclerViewP2p);
        progressBarP = findViewById(R.id.progressBarP);

        recyclerViewP2p.setLayoutManager(new LinearLayoutManager(this));

        p2pExchangeMList = new ArrayList<>();

        buy_Button.setBackgroundResource(R.drawable.round_radius_green);
        sell_Button.setBackgroundColor(0);

        buy_Button.setOnClickListener(v -> {
            p2pExchangeMList.clear();
            p2pExchangeAdapter.notifyDataSetChanged();

            buy_Button.setBackgroundResource(R.drawable.round_radius_green);
            sell_Button.setBackgroundColor(0);

            progressBarP.setVisibility(View.VISIBLE);
            // call api to fetch all buyer merchant

            p2pExchangeAdapter.setP2pExchangeMList(exchangeList("buy"));
            new Handler().postDelayed(() -> progressBarP.setVisibility(View.GONE), 500);

        });

        sell_Button.setOnClickListener(v -> {
            p2pExchangeMList.clear();
            p2pExchangeAdapter.notifyDataSetChanged();
            buy_Button.setBackgroundColor(0);
            sell_Button.setBackgroundResource(R.drawable.round_radius_orange);

            progressBarP.setVisibility(View.VISIBLE);
            // call api to fetch all seller merchant

            p2pExchangeAdapter.setP2pExchangeMList(exchangeList("sell"));
            new Handler().postDelayed(() -> progressBarP.setVisibility(View.GONE), 500);

        });

        // feed data
        exchangeList("buy");

        new Handler().postDelayed( () -> {

            p2pExchangeAdapter = new P2pExchangeAdapter(p2pExchangeMList, this);
            recyclerViewP2p.setAdapter(p2pExchangeAdapter);

            new Handler().postDelayed(() -> progressBarP.setVisibility(View.GONE), 500);

        }, 100);

        closeP2p_Button.setOnClickListener( v -> {
            onBackPressed();
        });

    }

    //  =======     methods      ==========
    private List<P2pExchangeM> exchangeList(String buyOrSell){

        p2pExchangeMList.add(new P2pExchangeM("Frank", 150 +" " + getString(R.string.order), "10,000 - 250,000", 25 + "NGN", buyOrSell));
        p2pExchangeMList.add(new P2pExchangeM("SoloKing", 40 +" "+ getString(R.string.order), "4,000 - 50,000", 40 + "NGN", buyOrSell));
        p2pExchangeMList.add(new P2pExchangeM("SuperPay", 250 +" "+ getString(R.string.order), "50,000 - 1,950,000", 50 + "NGN", buyOrSell));
        p2pExchangeMList.add(new P2pExchangeM("onpay", 430 +" "+ getString(R.string.order), "2,000 - 20,000", 30 + "NGN", buyOrSell));
        p2pExchangeMList.add(new P2pExchangeM("fastPay", 640 +" "+ getString(R.string.order), "40,000 - 250,000", 40 + "NGN", buyOrSell));
        p2pExchangeMList.add(new P2pExchangeM("track", 200 +" "+ getString(R.string.order), "100,000 - 500,000", 20 + "NGN", buyOrSell));
        p2pExchangeMList.add(new P2pExchangeM("obaro", 350 +" "+ getString(R.string.order), "25,000 - 250,000", 50 + "NGN", buyOrSell));
        p2pExchangeMList.add(new P2pExchangeM("Soe_Sol", 610 +" "+ getString(R.string.order), "1,000 - 20,000", 10 + "NGN", buyOrSell));
        p2pExchangeMList.add(new P2pExchangeM("exchange", 3340 +" "+ getString(R.string.order), "50,000 - 100,000", 40 + "NGN", buyOrSell));
        p2pExchangeMList.add(new P2pExchangeM("MuchMore", 3250 +" "+ getString(R.string.order), "5,000 - 450,000", 50 + "NGN", buyOrSell));
        p2pExchangeMList.add(new P2pExchangeM("QuickPay", 550 +" "+ getString(R.string.order), "4,000 - 150,000", 50 + "NGN", buyOrSell));

        return p2pExchangeMList;

    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


}