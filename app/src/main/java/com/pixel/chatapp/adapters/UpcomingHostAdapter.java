package com.pixel.chatapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pixel.chatapp.R;
import com.pixel.chatapp.dataModel.UpcomingHostM;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpcomingHostAdapter extends RecyclerView.Adapter<UpcomingHostAdapter.HostViewHolder> {

    private Context context;
    private final List<UpcomingHostM> hostMList;

    public UpcomingHostAdapter(Context context, List<UpcomingHostM> hostMList) {
        this.context = context;
        this.hostMList = hostMList;
    }


    @NonNull
    @Override
    public HostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.upcoming_match_card, parent, false);
        return new UpcomingHostAdapter.HostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HostViewHolder holder, int position) {

//        holder.gameLogo_IV.setImageResource(0);
        holder.gameTitle_TV.setText(null);
        holder.startDateTV.setText(null);


        String gameTitle = hostMList.get(position).getGameTitle();
        String startDateTV = hostMList.get(position).getStartDate();

        holder.gameTitle_TV.setText(gameTitle);
        holder.startDateTV.setText(startDateTV);

    }

    @Override
    public int getItemCount() {
        return hostMList.size();
    }

    public class HostViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView gameLogo_IV;
        private TextView gameTitle_TV, startDateTV;

        public HostViewHolder(@NonNull View itemView) {
            super(itemView);
            gameLogo_IV = itemView.findViewById(R.id.gameLogo_IV);
            gameTitle_TV = itemView.findViewById(R.id.gameTitle_TV);
            startDateTV = itemView.findViewById(R.id.startDateTV);

        }
    }

}
