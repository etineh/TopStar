package com.pixel.chatapp.side_bar_menu.wallet;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.pixel.chatapp.R;
import com.pixel.chatapp.utils.PhoneUtils;

public class TransactionReceiptActivity extends AppCompatActivity {

    // transfer successful page
    private TextView amount_TV, date_TV, fee_TV, receiver_TV, sender_TV, back, goHistory, trxId_TV, trxType_TV;
    private ImageView copyId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_transaction_receipt);

        // transfer successful ids
        amount_TV = findViewById(R.id.amount_TV);
        date_TV = findViewById(R.id.date_TV);
        fee_TV = findViewById(R.id.fee_TV);
        receiver_TV = findViewById(R.id.receiver_TV);
        sender_TV = findViewById(R.id.sender_TV);
        back = findViewById(R.id.back);
        goHistory = findViewById(R.id.historySuccess);
        copyId = findViewById(R.id.copyId);
        trxId_TV = findViewById(R.id.transactionID_TV);
        trxType_TV = findViewById(R.id.transactionType_TV);

        getOnBackPressedDispatcher().addCallback(this, callback);

        String from = getIntent().getStringExtra("from");

        if(from.equals("P2P")){
            goHistory.setText(getString(R.string.giveReview));
        }

        goHistory.setOnClickListener(v -> {
            if(from.equals("P2P")){
                // go to review page
                Toast.makeText(this, "review in progress", Toast.LENGTH_SHORT).show();

            } else {
                // go to history
                Toast.makeText(this, "history in progress", Toast.LENGTH_SHORT).show();
            }
        });

        copyId.setOnClickListener(v -> {
            v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(20).withEndAction(() ->
            {
                PhoneUtils.copyText(this, trxId_TV);

                new Handler().postDelayed( ()-> {
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                }, 500);

            }).start();

            Toast.makeText(this, "copied!", Toast.LENGTH_SHORT).show();
        });

        back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

    }


    //  =======     methods



    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            finish();
        }
    };

}













