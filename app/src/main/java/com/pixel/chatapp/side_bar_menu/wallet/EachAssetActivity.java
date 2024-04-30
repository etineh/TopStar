package com.pixel.chatapp.side_bar_menu.wallet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pixel.chatapp.R;
import com.pixel.chatapp.adapters.HistoryFundAdapter;
import com.pixel.chatapp.model.WalletHistory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EachAssetActivity extends AppCompatActivity {

    private LinearLayout convertLayout;
    private ImageView arrowClose;
    private TextView eachAssetTotalAmount, setEachAssetName_TV, historyRecord_TV;
    private RecyclerView recyclerAssetHistory;
    private List<WalletHistory> historyList;
    private ProgressBar progressBarH;

//    private

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_each_asset);

        convertLayout = findViewById(R.id.convertContainer);
        arrowClose = findViewById(R.id.arrowClose);
        eachAssetTotalAmount = findViewById(R.id.eachAssetTotalAmount);
        setEachAssetName_TV = findViewById(R.id.setEachAssetName_TV);
        progressBarH = findViewById(R.id.progressBarH);
        historyRecord_TV = findViewById(R.id.historyRecord_TV);

        recyclerAssetHistory = findViewById(R.id.recyclerAssetHistory);
        recyclerAssetHistory.setLayoutManager(new LinearLayoutManager(this));
        historyList = new ArrayList<>();

        // each wallet amount
        String getAssetType = getIntent().getStringExtra("assetType");
        if(getAssetType.equals("gameAsset"))
        {
            setEachAssetName_TV.setText(getString(R.string.totalGameAsset));
            eachAssetTotalAmount.setText("N 5,000");
            historyRecord_TV.setText(getString(R.string.gameRewardHistory));

        } else if (getAssetType.equals("localAsset")) 
        {
            setEachAssetName_TV.setText(getString(R.string.totalLocalAsset));
            eachAssetTotalAmount.setText("N 25,000");
            historyRecord_TV.setText(getString(R.string.localAssetHistory));

        } else if (getAssetType.equals("USDTAsset")) 
        {
            setEachAssetName_TV.setText(getString(R.string.totalUSDTAsset));
            eachAssetTotalAmount.setText("$ 20");
            historyRecord_TV.setText(getString(R.string.USDTHistory));

        } else if (getAssetType.equals("BonusAsset")) 
        {
            setEachAssetName_TV.setText(getString(R.string.totalBonusAsset));
            eachAssetTotalAmount.setText("0.00");
            historyRecord_TV.setText(getString(R.string.bonusRewardHistory));

        } else if (getAssetType.equals("MerchantAsset"))
        {
            setEachAssetName_TV.setText(getString(R.string.totalMerchantAsset));
            eachAssetTotalAmount.setText("N 220,000.00");
            historyRecord_TV.setText(getString(R.string.merchantRewardHistory));
        }

        addHistory();

        new Handler().postDelayed( ()-> {

            HistoryFundAdapter historyFundAdapter = new HistoryFundAdapter(this, historyList);
            recyclerAssetHistory.setAdapter(historyFundAdapter);

            new Handler().postDelayed( ()-> progressBarH.setVisibility(View.GONE), 1000);

        }, 500);



        //  ========    open convert activity
        convertLayout.setOnClickListener(v -> {
            // open convert activity
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(20).withEndAction(() ->
            {

                Intent intent = new Intent(this, ConvertFundActivity.class);
                startActivity(intent);

                new Handler().postDelayed( ()-> {
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                }, 200);

            }).start();

        });


        arrowClose.setOnClickListener(v -> onBackPressed());

    }

    private void addHistory(){
        historyList.add(new WalletHistory("", "@ndifreke" ,
                "Ndifreke Sunday", getString(R.string.winPoker)
                + " @ndifreke", new Date(), "+3000","received"));

        historyList.add(new WalletHistory("", "@frank" ,
                "Frank Umoro", getString(R.string.loseWhot)
                + " Frank", new Date(), "-200","sent"));

        historyList.add(new WalletHistory("", "@maro" ,
                " Maro Princess", "Received from Local Asset",
                new Date(), "+600","convert"));

        historyList.add(new WalletHistory("", "@ndifreke" ,
                "Ndifreke Sunday", getString(R.string.winPoker)
                + " @ndifreke", new Date(), "+3000","received"));

        historyList.add(new WalletHistory("", "@frank" ,
                "Frank Umoro", getString(R.string.loseWhot)
                + " Frank", new Date(), "-200","sent"));

        historyList.add(new WalletHistory("", "@maro" ,
                " Maro Princess", "Received from Local Asset",
                new Date(), "+600","convert"));
        historyList.add(new WalletHistory("", "@ndifreke" ,
                "Ndifreke Sunday", getString(R.string.winPoker)
                + " @ndifreke", new Date(), "+3000","received"));

        historyList.add(new WalletHistory("", "@frank" ,
                "Frank Umoro", getString(R.string.loseWhot)
                + " Frank", new Date(), "-200","sent"));

        historyList.add(new WalletHistory("", "@maro" ,
                " Maro Princess", "Received from Local Asset",
                new Date(), "+600","convert"));
        historyList.add(new WalletHistory("", "@ndifreke" ,
                "Ndifreke Sunday", getString(R.string.winPoker)
                + " @ndifreke", new Date(), "+3000","received"));

        historyList.add(new WalletHistory("", "@frank" ,
                "Frank Umoro", getString(R.string.loseWhot)
                + " Frank", new Date(), "-200","sent"));

        historyList.add(new WalletHistory("", "@maro" ,
                " Maro Princess", "Received from Local Asset",
                new Date(), "+600","convert"));
        historyList.add(new WalletHistory("", "@ndifreke" ,
                "Ndifreke Sunday", getString(R.string.winPoker)
                + " @ndifreke", new Date(), "+3000","received"));

        historyList.add(new WalletHistory("", "@frank" ,
                "Frank Umoro", getString(R.string.loseWhot)
                + " Frank", new Date(), "-200","sent"));

        historyList.add(new WalletHistory("", "@maro" ,
                " Maro Princess", "Received from Local Asset",
                new Date(), "+600","convert"));
        historyList.add(new WalletHistory("", "@ndifreke" ,
                "Ndifreke Sunday", getString(R.string.winPoker)
                + " @ndifreke", new Date(), "+3000","received"));

        historyList.add(new WalletHistory("", "@frank" ,
                "Frank Umoro", getString(R.string.loseWhot)
                + " Frank", new Date(), "-200","sent"));

        historyList.add(new WalletHistory("", "@maro" ,
                " Maro Princess", "Received from Local Asset",
                new Date(), "+600","convert"));
        historyList.add(new WalletHistory("", "@ndifreke" ,
                "Ndifreke Sunday", getString(R.string.winPoker)
                + " @ndifreke", new Date(), "+3000","received"));

        historyList.add(new WalletHistory("", "@frank" ,
                "Frank Umoro", getString(R.string.loseWhot)
                + " Frank", new Date(), "-200","sent"));

        historyList.add(new WalletHistory("", "@maro" ,
                " Maro Princess", "Received from Local Asset",
                new Date(), "+600","convert"));
        historyList.add(new WalletHistory("", "@ndifreke" ,
                "Ndifreke Sunday", getString(R.string.winPoker)
                + " @ndifreke", new Date(), "+3000","received"));

        historyList.add(new WalletHistory("", "@frank" ,
                "Frank Umoro", getString(R.string.loseWhot)
                + " Frank", new Date(), "-200","sent"));

        historyList.add(new WalletHistory("", "@maro" ,
                " Maro Princess", "Received from Local Asset",
                new Date(), "+600","convert"));
        historyList.add(new WalletHistory("", "@ndifreke" ,
                "Ndifreke Sunday", getString(R.string.winPoker)
                + " @ndifreke", new Date(), "+3000","received"));

        historyList.add(new WalletHistory("", "@frank" ,
                "Frank Umoro", getString(R.string.loseWhot)
                + " Frank", new Date(), "-200","sent"));

        historyList.add(new WalletHistory("", "@maro" ,
                " Maro Princess", "Received from Local Asset",
                new Date(), "+600","convert"));
        historyList.add(new WalletHistory("", "@ndifreke" ,
                "Ndifreke Sunday", getString(R.string.winPoker)
                + " @ndifreke", new Date(), "+3000","received"));

        historyList.add(new WalletHistory("", "@frank" ,
                "Frank Umoro", getString(R.string.loseWhot)
                + " Frank", new Date(), "-200","sent"));

        historyList.add(new WalletHistory("", "@maro" ,
                " Maro Princess", "Received from Local Asset",
                new Date(), "+600","convert"));
        historyList.add(new WalletHistory("", "@ndifreke" ,
                "Ndifreke Sunday", getString(R.string.winPoker)
                + " @ndifreke", new Date(), "+3000","received"));

        historyList.add(new WalletHistory("", "@frank" ,
                "Frank Umoro", getString(R.string.loseWhot)
                + " Frank", new Date(), "-200","sent"));

        historyList.add(new WalletHistory("", "@maro" ,
                " Maro Princess", "Received from Local Asset",
                new Date(), "+600","convert"));
        historyList.add(new WalletHistory("", "@ndifreke" ,
                "Ndifreke Sunday", getString(R.string.winPoker)
                + " @ndifreke", new Date(), "+3000","received"));

        historyList.add(new WalletHistory("", "@frank" ,
                "Frank Umoro", getString(R.string.loseWhot)
                + " Frank", new Date(), "-200","sent"));

        historyList.add(new WalletHistory("", "@maro" ,
                " Maro Princess", "Received from Local Asset",
                new Date(), "+600","convert"));
        historyList.add(new WalletHistory("", "@ndifreke" ,
                "Ndifreke Sunday", getString(R.string.winPoker)
                + " @ndifreke", new Date(), "+3000","received"));

        historyList.add(new WalletHistory("", "@frank" ,
                "Frank Umoro", getString(R.string.loseWhot)
                + " Frank", new Date(), "-200","sent"));

        historyList.add(new WalletHistory("", "@maro" ,
                " Maro Princess", "Received from Local Asset",
                new Date(), "+600","convert"));
        historyList.add(new WalletHistory("", "@ndifreke" ,
                "Ndifreke Sunday", getString(R.string.winPoker)
                + " @ndifreke", new Date(), "+3000","received"));

        historyList.add(new WalletHistory("", "@frank" ,
                "Frank Umoro", getString(R.string.loseWhot)
                + " Frank", new Date(), "-200","sent"));

        historyList.add(new WalletHistory("", "@maro" ,
                " Maro Princess", "Received from Local Asset",
                new Date(), "+600","convert"));
        historyList.add(new WalletHistory("", "@ndifreke" ,
                "Ndifreke Sunday", getString(R.string.winPoker)
                + " @ndifreke", new Date(), "+3000","received"));

        historyList.add(new WalletHistory("", "@frank" ,
                "Frank Umoro", getString(R.string.loseWhot)
                + " Frank", new Date(), "-200","sent"));

        historyList.add(new WalletHistory("", "@maro" ,
                " Maro Princess", "Received from Local Asset",
                new Date(), "+600","convert"));
        historyList.add(new WalletHistory("", "@ndifreke" ,
                "Ndifreke Sunday", getString(R.string.winPoker)
                + " @ndifreke", new Date(), "+3000","received"));

        historyList.add(new WalletHistory("", "@frank" ,
                "Frank Umoro", getString(R.string.loseWhot)
                + " Frank", new Date(), "-200","sent"));

        historyList.add(new WalletHistory("", "@maro" ,
                " Maro Princess", "Received from Local Asset",
                new Date(), "+600","convert"));
        historyList.add(new WalletHistory("", "@ndifreke" ,
                "Ndifreke Sunday", getString(R.string.winPoker)
                + " @ndifreke", new Date(), "+3000","received"));

        historyList.add(new WalletHistory("", "@frank" ,
                "Frank Umoro", getString(R.string.loseWhot)
                + " Frank", new Date(), "-200","sent"));

        historyList.add(new WalletHistory("", "@maro" ,
                " Maro Princess", "Received from Local Asset",
                new Date(), "+600","convert"));
        historyList.add(new WalletHistory("", "@ndifreke" ,
                "Ndifreke Sunday", getString(R.string.winPoker)
                + " @ndifreke", new Date(), "+3000","received"));

        historyList.add(new WalletHistory("", "@frank" ,
                "Frank Umoro", getString(R.string.loseWhot)
                + " Frank", new Date(), "-200","sent"));

        historyList.add(new WalletHistory("", "@maro" ,
                " Maro Princess", "Received from Local Asset",
                new Date(), "+600","convert"));

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}



















