package com.pixel.chatapp.photos;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.pixel.chatapp.R;
import com.pixel.chatapp.all_utils.FileUtils;
import com.pixel.chatapp.side_bar_menu.settings.StorageRecyclerActivity;

import java.io.File;
import java.io.IOException;

public class PlayFileActivity extends AppCompatActivity {


    ImageView cancel_I, del_IV, playV_IV, playIconView_IV;
    VideoView videoView;
    ConstraintLayout videoSeekBarContainer;
    LinearLayout videoContainer;
    SeekBar seekBarProgress;
    TextView duration_TV;
    String getFile, type;
    Uri uri;
    String durationFormat;
    MediaPlayer mediaPlayer;
    Handler handler;
    Runnable runnable;
    boolean play;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_file);


        cancel_I = findViewById(R.id.cancel_I);
        del_IV = findViewById(R.id.del_IV);
        playV_IV = findViewById(R.id.playV_IV);
        videoView = findViewById(R.id.videoView);
        videoSeekBarContainer = findViewById(R.id.videoSeekBarContainer);
        seekBarProgress = findViewById(R.id.seekBarProgress);
        duration_TV = findViewById(R.id.duration_TV);
        playIconView_IV = findViewById(R.id.playIconView_IV);
        videoContainer = findViewById(R.id.videoContainer);

        getFile = getIntent().getStringExtra("file");
        type = getIntent().getStringExtra("type");

        if (type.equals("video"))
        {
            videoContainer.setVisibility(View.VISIBLE);

            uri = Uri.fromFile(new File(getFile));
            durationFormat = FileUtils.getVideoDuration(uri, this);
            videoView.setVideoURI(uri);
            duration_TV.setText(durationFormat);  // show the duration

        } else if(type.equals("audio"))
        {
            videoContainer.setVisibility(View.GONE);

            mediaPlayer = new MediaPlayer();

            try {
                mediaPlayer.setDataSource(getFile);
                mediaPlayer.prepare();

                int duration = mediaPlayer.getDuration();
                durationFormat = FileUtils.formatDuration(duration);

                duration_TV.setText(durationFormat);  // show the duration

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            playIconView_IV.setImageResource(R.drawable.baseline_mic_24);
        }

        seekBarOnTouchListener();

        playAndPauseMedia();

        // play and pause click
        View.OnClickListener playVideo = v -> playAndPauseMedia();
        playV_IV.setOnClickListener(playVideo);
        playIconView_IV.setOnClickListener(playVideo);


        cancel_I.setOnClickListener(v ->
        {
            if(type.equals("audio")) mediaPlayer.stop();
            if(type.equals("video")) videoView.pause();

            if(handler != null) handler.removeCallbacks(runnable);

            getOnBackPressedDispatcher().onBackPressed();
            finish();
        });

        del_IV.setOnClickListener(v -> {
            StorageRecyclerActivity.fileList.add( new File(getFile) );
            deleteListener.onDelete();
            Toast.makeText(this, getString(R.string.deleteSuccessful), Toast.LENGTH_SHORT).show();
            finish();
        });

        getOnBackPressedDispatcher().addCallback(callback);

    }

    //  ===========     methods     ===============

    private void playAndPauseMedia(){
        if(type.equals("video"))
        {
            if(playIconView_IV.getVisibility() == View.VISIBLE) playIconView_IV.setVisibility(View.GONE);
            if(videoView.isPlaying()){
                videoView.pause();
                duration_TV.setText(durationFormat);  // show the duration
                playV_IV.setImageResource(R.drawable.baseline_play_arrow_24);

            } else {
                videoView.start();
                // start duration counting
                playV_IV.setImageResource(R.drawable.baseline_pause_24);
                if (!play) getSeekBarProgress();
            }

        } else if (type.equals("audio"))
        {
            if(mediaPlayer.isPlaying()){
                mediaPlayer.pause();
                playV_IV.setImageResource(R.drawable.baseline_play_arrow_24);
                duration_TV.setText(durationFormat);  // show the duration

            } else {
                mediaPlayer.start();
                if (!play) getSeekBarProgress();
                playV_IV.setImageResource(R.drawable.baseline_pause_24);
            }
        }

        play = true;
    }

    private void seekBarOnTouchListener(){
        // Seekbar Change Listener
        seekBarProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if(type.equals("video")){
                        videoView.seekTo(progress);
                    } else{
                        mediaPlayer.seekTo(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Pause media playback while user is dragging seekbar
                videoView.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Resume media playback when user releases seekbar
                if(type.equals("video")){
                    videoView.start();
                } else{
                    mediaPlayer.start();
                }
            }
        });
    }

    private void getSeekBarProgress() {
        // Update Seekbar Progress
        handler = new Handler();

        runnable = new Runnable() {
            @Override
            public void run() {
                if (type.equals("video")) {
                    if(videoView.isPlaying())
                    {
                        int currentPosition = videoView.getCurrentPosition();
                        int duration = videoView.getDuration();

                        // Update SeekBar progress
                        seekBarProgress.setMax(duration);
                        seekBarProgress.setProgress(currentPosition);

                        // Update TextView with current position
                        duration_TV.setText(FileUtils.formatDuration(currentPosition));
                    }

                } else if (type.equals("audio"))
                {
                    if(mediaPlayer.isPlaying())
                    {
                        int currentPosition = mediaPlayer.getCurrentPosition();
                        int duration = mediaPlayer.getDuration();

                        // Update SeekBar progress
                        seekBarProgress.setMax(duration);
                        seekBarProgress.setProgress(currentPosition);

                        // Update TextView with current position
                        duration_TV.setText(FileUtils.formatDuration(currentPosition));
                    }
                }
                Toast.makeText(PlayFileActivity.this, "Making", Toast.LENGTH_SHORT).show();
                // Schedule the next update
                handler.postDelayed(this, 1000); // Update every second
            }
        };

        handler.postDelayed(runnable, 1000);
    }


    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if(mediaPlayer != null) mediaPlayer.stop();
            finish();
        }
    };


    public static DeleteListener deleteListener;
    public interface DeleteListener {
        void onDelete();
    }

}











