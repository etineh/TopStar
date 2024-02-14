package com.pixel.chatapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pixel.chatapp.R;
import com.pixel.chatapp.listeners.ImageListener;
import com.pixel.chatapp.model.MessageModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SendImageAdapter extends RecyclerView.Adapter<SendImageAdapter.ImageViewHolder> {

    private Context context;
    private List<MessageModel> modelList;
    private List<View> viewList = new ArrayList<>();
    private boolean firstRun = true;
    private ImageListener imageListener;
    private View lastView;

    // set the listener to pass details to the main activity
    public void setImageListener(ImageListener imageListener) {
        this.imageListener = imageListener;
    }

    // construction
    public SendImageAdapter(Context context, List<MessageModel> modelList){

        this.context = context;
        this.modelList = modelList;

    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photos_card, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        int getPosition = position;
        String imageUri = modelList.get(getPosition).getPhotoUriPath();
        MessageModel chatModel = modelList.get(getPosition);

        if(!viewList.contains(holder.itemView)) viewList.add(holder.itemView);

        // highlight the first photo position
        if(firstRun && position == 0){
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.orange));
            lastView = holder.itemView;
            firstRun = false;
        }

        holder.itemView.setOnClickListener(view -> {

            if(lastView != null ) lastView.setBackgroundColor(0);

            view.setBackgroundColor(context.getResources().getColor(R.color.orange));

            imageListener.getCurrentModelChat(chatModel, getPosition);

            lastView = view;
        });

        // display the photos
        Picasso.get().load(imageUri).into(holder.showImage_IV);

    }

    public void highLightView(int photoPosition){
        try{
            View view = viewList.get(photoPosition);
            if(lastView != null ) lastView.setBackgroundColor(0);

            view.setBackgroundColor(context.getResources().getColor(R.color.orange));

            lastView = view;
        } catch (Exception e){
//            Toast.makeText(context, "Catch photo highlight error", Toast.LENGTH_SHORT).show();
        }

    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        private ImageView showImage_IV;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            showImage_IV = itemView.findViewById(R.id.photoCard_IV);
        }
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }
}
