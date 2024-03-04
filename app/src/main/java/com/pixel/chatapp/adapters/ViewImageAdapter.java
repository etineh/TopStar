package com.pixel.chatapp.adapters;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.pixel.chatapp.R;
import com.pixel.chatapp.SendImageActivity;
import com.pixel.chatapp.all_utils.FileUtils;
import com.pixel.chatapp.all_utils.OtherMethods;
import com.pixel.chatapp.listeners.ImageListener;
import com.pixel.chatapp.model.MessageModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        MessageModel model = messageModelList.get(position);

        PhotoView photoView = itemView.findViewById(R.id.zoomPhoto_PView);
        ConstraintLayout fileDetailsContainer = itemView.findViewById(R.id.fileDetailsContainer);
        TextView fileName_TV = itemView.findViewById(R.id.fileName_TV);
        ImageView fileIcon_IV = itemView.findViewById(R.id.fileIcon_IV);
        VideoView videoView = itemView.findViewById(R.id.videoView);
        ImageView playButton_IV = itemView.findViewById(R.id.playIconView_IV);
        ConstraintLayout videoSeekBarContainer = itemView.findViewById(R.id.videoSeekBarContainer);
        SeekBar seekBar = itemView.findViewById(R.id.seekBarProgress);
        TextView durationTV = itemView.findViewById(R.id.duration_TV);
        ImageView playV_IV = itemView.findViewById(R.id.playV_IV);
        TextView fileSize_TV = itemView.findViewById(R.id.fileSize_TV);
        FrameLayout frameLayout = itemView.findViewById(R.id.frameLayout);
        Map<String, VideoView> videoViewMap = new HashMap<>();

        // reset
        fileDetailsContainer.setVisibility(View.GONE);
        frameLayout.setVisibility(View.GONE);
        playButton_IV.setVisibility(View.GONE);
        videoSeekBarContainer.setVisibility(View.GONE);


        // load the photo the photoView via Picasso
        if(model.getPhotoUriOriginal() != null && model.getType() == 2){    // it is photo
            String photoUri = messageModelList.get(position).getPhotoUriOriginal();

            if(!photoUri.startsWith("media/photos")){
//            Picasso.get().load(photoUri).into(photoView);
                Glide.with(context).load(photoUri).into(photoView);
            }

        } else if (model.getType() == 5)
        {   // it is video
            String videoUri = model.getPhotoUriOriginal();
            if (!videoUri.startsWith("media/photos")) {
                Glide.with(context).load(videoUri).into(photoView);
                playButton_IV.setVisibility(View.VISIBLE);
                videoSeekBarContainer.setVisibility(View.VISIBLE);

                durationTV.setText(model.getVnDuration());  // show the duration
                fileSize_TV.setText(model.getImageSize());

            } else {
                Toast.makeText(context, "Error occur at SendImageAdapter L95", Toast.LENGTH_SHORT).show();
            }

        } else {    // it is audio or document
            photoView.setImageResource(0);
            fileDetailsContainer.setVisibility(View.VISIBLE);
            fileName_TV.setText(model.getEmojiOnly());   // I saved the file name at emojiOnly
            fileIcon_IV.setImageResource(R.drawable.baseline_document_scanner_24);

            if(model.getType() == 4) {   // it is audio
                fileIcon_IV.setImageResource(R.drawable.baseline_audio_file_24);
            }
        }

        // -------------     onClicks
        View.OnClickListener playAndPause = v -> {
            frameLayout.setVisibility(View.VISIBLE);

            if(videoViewMap.get(model.getIdKey()) != null){
                VideoView getVideoView = videoViewMap.get(model.getIdKey());
                if(getVideoView.isPlaying()){
                    getVideoView.pause();
                    durationTV.setText(model.getVnDuration());  // show the duration
                    playV_IV.setImageResource(R.drawable.baseline_play_arrow_24);

                } else {
                    getVideoView.start();
                    // start duration counting
                    getSeekBarProgress(getVideoView, seekBar, durationTV);
                    playV_IV.setImageResource(R.drawable.baseline_pause_24);
                    
                    fadeInAndOut(videoSeekBarContainer);
                    
                }

            } else {
//                System.out.println("what is pa " + model.getPhotoUriOriginal());

                videoView.setVideoURI(Uri.parse(model.getPhotoUriOriginal()));
                videoView.start();
                getSeekBarProgress(videoView, seekBar, durationTV);
                // save the videoView player
                videoViewMap.put(model.getIdKey(), videoView);
                // activate the seekBarListener for each new videoView just added to the map
                seekBarOnTouchListener(seekBar, videoView);

                playButton_IV.setVisibility(View.GONE);
                playV_IV.setImageResource(R.drawable.baseline_pause_24);

                fadeInAndOut(videoSeekBarContainer);

            }

        };

        playButton_IV.setOnClickListener(playAndPause);
        playV_IV.setOnClickListener(playAndPause);

        frameLayout.setOnClickListener(v -> {
            fadeInAndOut(videoSeekBarContainer);
        });

        photoView.setOnClickListener(v -> {
//            SendImageActivity.recyclerPhoto.setVisibility(View.VISIBLE);
            OtherMethods.fadeInFastRecyclerview(SendImageActivity.recyclerPhoto);
        });


        container.addView(itemView);
        return itemView;
    }

    private void fadeInAndOut(ConstraintLayout videoSeekBarContainer){
        if(videoSeekBarContainer.getVisibility() == View.VISIBLE){
            OtherMethods.fadeOutSeekBar(videoSeekBarContainer);
            OtherMethods.fadeOutRecyclerview(SendImageActivity.recyclerPhoto);
        } else {
            OtherMethods.fadeInSeekBar(videoSeekBarContainer);
            OtherMethods.fadeInRecyclerview(SendImageActivity.recyclerPhoto);
        }
    }

    private void seekBarOnTouchListener(SeekBar seekBar, VideoView videoView_){
        // Seekbar Change Listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    videoView_.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Pause media playback while user is dragging seekbar
                videoView_.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Resume media playback when user releases seekbar
                videoView_.start();
            }
        });
    }

    //  my methods
    private void getSeekBarProgress(VideoView videoView, SeekBar seekBar, TextView durationTV) {
        // Update Seekbar Progress
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (videoView.isPlaying()) {
                    int currentPosition = videoView.getCurrentPosition();
                    int duration = videoView.getDuration();

                    // Update SeekBar progress
                    seekBar.setMax(duration);
                    seekBar.setProgress(currentPosition);

                    // Update TextView with current position
                    durationTV.setText(FileUtils.formatDuration(currentPosition));
                }

                // Schedule the next update
                handler.postDelayed(this, 1000); // Update every second
            }
        }, 1000);
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
