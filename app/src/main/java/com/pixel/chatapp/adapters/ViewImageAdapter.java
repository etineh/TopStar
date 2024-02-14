package com.pixel.chatapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.OnScaleChangedListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.pixel.chatapp.R;
import com.pixel.chatapp.listeners.ImageListener;
import com.pixel.chatapp.model.MessageModel;

import java.util.List;

public class ViewImageAdapter extends PagerAdapter {

    private List<MessageModel> messageModelList;
    private Context context;

    ImageListener imageListener;

    // set the listener to pass details to the main activity
    public void setImageListener(ImageListener imageListener) {
        this.imageListener = imageListener;
    }

    public ViewImageAdapter(Context context, List<MessageModel> messageModelList) {
        this.context = context;
        this.messageModelList = messageModelList;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.view_all_photos, container, false);

        String photoUri = messageModelList.get(position).getPhotoUriOriginal();
        // load the photo the photoView via Picasso
        if(!photoUri.startsWith("media/photos")){
            PhotoView photoView = itemView.findViewById(R.id.zoomPhoto_PView);
//            Picasso.get().load(photoUri).into(photoView);
            Glide.with(context).load(photoUri).into(photoView);

//            Glide.with(context).asBitmap().load(photoUri).into(new SimpleTarget<Bitmap>() {
//                @Override
//                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//                    photoView.setImage(ImageSource.bitmap(resource));
//                }
//            });
        }

        container.addView(itemView);
        return itemView;
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.setPrimaryItem(container, position, object);
        if (imageListener != null) {
            imageListener.getCurrentModelChat(messageModelList.get(position), position);
        }
    }

    @Override
    public int getCount() {
        return messageModelList.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public int getItemPosition(Object object) {
        // This ensures that notifyDataSetChanged() works as expected
        return POSITION_NONE;
    }

}
