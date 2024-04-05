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
import com.pixel.chatapp.model.P2pExchangeM;
import com.pixel.chatapp.peer2peer.exchange.BuyerInputAmountActivity;
import com.pixel.chatapp.peer2peer.exchange.SellerInputAmountActivity;

import java.util.List;

public class P2pExchangeAdapter extends RecyclerView.Adapter<P2pExchangeAdapter.P2pViewHolder>   {

    List<P2pExchangeM> p2pExchangeMList;
    Context context;

    public P2pExchangeAdapter(List<P2pExchangeM> p2pExchangeMList, Context context) {
        this.p2pExchangeMList = p2pExchangeMList;
        this.context = context;
    }

    public void setP2pExchangeMList(List<P2pExchangeM> p2pExchangeMList) {
        this.p2pExchangeMList = p2pExchangeMList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public P2pExchangeAdapter.P2pViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.p2p_exhange_user_card,
                parent, false);

        return new P2pViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull P2pExchangeAdapter.P2pViewHolder holder, int position) {

        holder.displayName.setText(null);
        holder.userImage_TV.setText(null);
        holder.buyOrSell.setText(null);
//        holder.paySpeed.setText(null);
        holder.totalOrder.setText(null);
        holder.amountRange.setText(null);
        holder.fee.setText(null);
//        holder.paymentOptions.setText(null);


        P2pExchangeM p2pDetails = p2pExchangeMList.get(position);

        String displayName = p2pDetails.getDisplayName();
        String online = p2pDetails.getOnline();
        String paySpeed = p2pDetails.getPaySpeed();
        String totalOrder = p2pDetails.getTotalOrder();

        String amountRange = p2pDetails.getAmountRange();
        String fee = p2pDetails.getFee();

        String paymentOptions = p2pDetails.getPaymentOptions();

        String buyOrSell = p2pDetails.getBuyOrSell();

        p2pDetails.setUserImage_TV( displayName.substring(0, 1).toUpperCase().toString() );
        String userImage_TV = p2pDetails.getUserImage_TV();

        holder.displayName.setText(displayName);
        holder.userImage_TV.setText(userImage_TV);
//        holder.paySpeed.setText(paySpeed);
        holder.buyOrSell.setText(buyOrSell);
        holder.totalOrder.setText(totalOrder);
        holder.amountRange.setText(amountRange);
        holder.fee.setText(fee);
//        holder.paymentOptions.setText(null);

        // set the background dolor for buy/sell
        if(buyOrSell.equals("buy")){
            holder.buyOrSell.setBackgroundResource(R.drawable.round_radius_green);
        } else {
            holder.buyOrSell.setBackgroundResource(R.drawable.round_radius_orange);
        }

        holder.itemView.setOnClickListener( v -> {

            if(buyOrSell.equals("buy")){
                Intent intent = new Intent(context, BuyerInputAmountActivity.class);

                context.startActivity(intent);
            } else {
                Intent intent = new Intent(context, SellerInputAmountActivity.class);

                context.startActivity(intent);
//                Toast.makeText(context, "Work in progress", Toast.LENGTH_SHORT).show();
            }

        });

    }

    @Override
    public int getItemCount() {
        return p2pExchangeMList.size();
    }

    public static class P2pViewHolder extends RecyclerView.ViewHolder {

        TextView displayName, online, paySpeed;
        TextView totalOrder, amountRange, fee;

        TextView paymentOptions, buyOrSell, userImage_TV;

        public P2pViewHolder(@NonNull View itemView) {
            super(itemView);

            displayName = itemView.findViewById(R.id.user_display_name);
            online = itemView.findViewById(R.id.onlinePresence_TV);
            paySpeed = itemView.findViewById(R.id.paySpeed_TV);
            totalOrder = itemView.findViewById(R.id.totalOrderTV);
            amountRange = itemView.findViewById(R.id.rangeAmount_TV);
            fee = itemView.findViewById(R.id.marchantFee_TV);
            buyOrSell = itemView.findViewById(R.id.sellOrBuy_Button);
            paymentOptions = itemView.findViewById(R.id.paymentOptions_TV);
            userImage_TV = itemView.findViewById(R.id.userImage_TV);
//            displayName = itemView.findViewById(R.id.user_display_name);

        }

    }


}











