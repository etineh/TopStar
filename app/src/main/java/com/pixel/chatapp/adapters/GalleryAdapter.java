package com.pixel.chatapp.adapters;

import static com.pixel.chatapp.home.MainActivity.chatModelList;
import static com.pixel.chatapp.home.MainActivity.user;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pixel.chatapp.R;
import com.pixel.chatapp.photos.CameraActivity;
import com.pixel.chatapp.utils.FileUtils;
import com.pixel.chatapp.model.MessageModel;

import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
    private Context mContext;
    private List<Uri> mImageUris;
    private DatabaseReference refMsgFast;
    private CameraActivity cameraActivity;

    public GalleryAdapter(Context context, CameraActivity activity, List<Uri> imageUris) {
        mContext = context;
        mImageUris = imageUris;
        this.cameraActivity = activity;
        refMsgFast = FirebaseDatabase.getInstance().getReference("MsgFast");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.photos_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Uri imageUri = mImageUris.get(position);

        // reset
        holder.imageView.setBackgroundColor(0); // remove highlight
        holder.itemView.setBackgroundColor(0);
        holder.morePhoto_TV.setVisibility(View.GONE);
        holder.playIcon_IV.setVisibility(View.GONE);
        holder.deleteIcon_IV.setVisibility(View.GONE);


        // initialise the previous state of the item _> highlight
        if(imageUri != null){
            if(!imageUri.toString().startsWith("file:/") && FileUtils.isVideoFile(imageUri, mContext)){
                holder.playIcon_IV.setVisibility(View.VISIBLE);
            }

            for (int i = 0; i < chatModelList.size(); i++) {
                MessageModel chats = chatModelList.get(i);
                if( chats.getPhotoUriOriginal().equals( imageUri.toString() ) ) {
                    // recall highlight and previous state
                    holder.itemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.orange));
                    holder.deleteIcon_IV.setVisibility(View.VISIBLE);
                }
            }
        } else{
            holder.imageView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.black2));
            holder.morePhoto_TV.setVisibility(View.VISIBLE);
            holder.playIcon_IV.setVisibility(View.GONE);
            holder.deleteIcon_IV.setVisibility(View.GONE);
        }

        Glide.with(mContext).load(imageUri).into(holder.imageView); // Use Glide library to load image


        holder.itemView.setOnClickListener(v -> selectPhotos(imageUri, v, holder));

        holder.itemView.setOnLongClickListener(v -> {
            selectPhotos(imageUri, v, holder);
            return true;
        });

    }

    //  ==========      methods
    private void selectPhotos(Uri imageUri, View v, ViewHolder holder)
    {
        if(imageUri != null) {
            int orangeColor = ContextCompat.getColor(mContext, R.color.orange);

            if( (holder.itemView.getBackground()) == null || ( (ColorDrawable) holder.itemView.getBackground() ).getColor() != orangeColor) {

                v.setBackgroundColor(ContextCompat.getColor(mContext, R.color.orange));

//content://media/external/images/media/1000147354 for photo picked from gallery  or file:// for the photo snapped
                String newChatNumId = refMsgFast.child(user.getUid()).push().getKey();  // create an id for each message
                String chatId = refMsgFast.child(user.getUid()).push().getKey();  // create an id for each message
                String size = FileUtils.getFileSize(imageUri, mContext);   // get the size of the image
                String fileName = "🌃 " + FileUtils.getFileName(imageUri, mContext);
                int type = 2;
                String videoDuration = null;
                if( FileUtils.isVideoFile(imageUri, mContext) ){   //  content://media/external/video/media/1000142346
                    type = 5;
                    videoDuration = FileUtils.getVideoDuration(imageUri, mContext);
                    fileName = "🎥 " + FileUtils.getFileName(imageUri, mContext);
                    size = FileUtils.getEstimateVideoSize(imageUri,mContext);
                }
                //  0 is text, 1 is voice note, 2 is photo, 3 is document, 4 is audio (mp3), 5 is video
                MessageModel messageModel = new MessageModel(null, null, user.getUid(), null,
                        System.currentTimeMillis(), chatId, null, newChatNumId,
                        null, 700033, type, size, null, false, false,
                        null, fileName, null, videoDuration, imageUri.toString(), imageUri.toString());

                if ( !chatModelList.contains(messageModel) ) chatModelList.add(messageModel);

                // in case user select image from gallery that is already added to the chatList
                CameraActivity.checkIfUriExist.add(imageUri);

                CameraActivity.photoSelected_TV.setText(chatModelList.size()+"");
                CameraActivity.sendALLPhoto_TV.setVisibility(View.VISIBLE);
                CameraActivity.photoSelected_TV.setVisibility(View.VISIBLE);
                holder.deleteIcon_IV.setVisibility(View.VISIBLE);

            } else {

                for (int i = 0; i < chatModelList.size(); i++) {
                    MessageModel chats = chatModelList.get(i);
//                    boolean isContain = false;
                    if( chats.getPhotoUriOriginal().equals( imageUri.toString() ) ) {
//                        isContain = true;
                        chatModelList.remove(i);
                        v.setBackgroundColor(0);
                        holder.deleteIcon_IV.setVisibility(View.GONE);
                        // in case user select image from gallery that is already added to the chatList
                        CameraActivity.checkIfUriExist.remove(imageUri);

                        if(chatModelList.size() == 0) {
                            CameraActivity.sendALLPhoto_TV.setVisibility(View.GONE);
                            CameraActivity.photoSelected_TV.setVisibility(View.GONE);
                        }
                        CameraActivity.photoSelected_TV.setText(chatModelList.size() + "");
                    }
                }

            }

        } else{
            cameraActivity.selectImageFromGallery(mContext, cameraActivity);
        }
    }

    public void addPhotoUri (Uri uri){
        if ( !mImageUris.contains(uri) ) {
            mImageUris.add(0, uri);
        } else {
            mImageUris.remove(uri); // remove it
            mImageUris.add(0, uri); // add it again so that it will appear on front role
        }
    }

    @Override
    public int getItemCount() {
        return mImageUris.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, deleteIcon_IV, playIcon_IV;
        TextView morePhoto_TV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.photoCard_IV);
            morePhoto_TV = itemView.findViewById(R.id.morePhoto_TV);
            playIcon_IV = itemView.findViewById(R.id.playIcon_IV);
            deleteIcon_IV = itemView.findViewById(R.id.deleteIcon_IV);

        }
    }
}

