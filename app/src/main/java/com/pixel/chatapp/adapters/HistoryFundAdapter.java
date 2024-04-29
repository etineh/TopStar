package com.pixel.chatapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pixel.chatapp.R;
import com.pixel.chatapp.all_utils.OpenActivityUtil;
import com.pixel.chatapp.model.WalletHistory;
import com.pixel.chatapp.side_bar_menu.wallet.TransactionReceiptActivity;

import java.util.Date;
import java.util.List;

public class HistoryFundAdapter extends RecyclerView.Adapter<HistoryFundAdapter.HistoryFundViewHolder>{

    private Context context;
    private List<WalletHistory> walletHistoryList;


    public HistoryFundAdapter(Context context, List<WalletHistory> walletHistoryList) {
        this.context = context;
        this.walletHistoryList = walletHistoryList;
    }

    @NonNull
    @Override
    public HistoryFundAdapter.HistoryFundViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fund_history_card,
                parent, false);

        return new HistoryFundAdapter.HistoryFundViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryFundAdapter.HistoryFundViewHolder holder, int position) {

        // reset
        holder.transactionInfo_TV.setText(null);
        holder.amount1_TV.setText(null);
        holder.transactionDate_TV.setText(null);
        holder.transactionStatus_TV.setText(null);

        // declare variable
        WalletHistory walletHistory = walletHistoryList.get(position);
        String otherUid = walletHistory.getOtherUid();
        String otherUsername = walletHistory.getOtherUsername();
        String otherDisplayName = walletHistory.getOtherDisplayName();
        String transactionInfo = walletHistory.getTransactionInfo();
        String amount = walletHistory.getAmount();
        String transactionStatus = walletHistory.getTransactionStatus();
        String transactionID = walletHistory.getTransactionID();
        String transactionType = walletHistory.getTransactionType();
        Date date = walletHistory.getDate();
        long previousAmount = walletHistory.getPreviousAmount();
        long current_Amount = walletHistory.getCurrent_Amount();


        // assign the var
        holder.transactionInfo_TV.setText(transactionInfo);
        holder.amount1_TV.setText(amount);
        holder.transactionDate_TV.setText(date.toString());
        holder.transactionStatus_TV.setText(transactionStatus);


        // onClick -- view transact details
        holder.itemView.setOnClickListener(v ->
        {
            Intent intent = new Intent(context, TransactionReceiptActivity.class);
            intent.putExtra("from", "asset");
            context.startActivity(intent);
        });



    }

    @Override
    public int getItemCount() {
        return walletHistoryList.size();
    }

    public class HistoryFundViewHolder extends RecyclerView.ViewHolder {

        private TextView transactionInfo_TV, amount1_TV, transactionDate_TV, transactionStatus_TV;

        public HistoryFundViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionInfo_TV = itemView.findViewById(R.id.transactionInfo_TV);
            amount1_TV = itemView.findViewById(R.id.amount1_TV);
            transactionDate_TV = itemView.findViewById(R.id.transactionDate_TV);
            transactionStatus_TV = itemView.findViewById(R.id.transactionStatus_TV);

        }
    }
}
