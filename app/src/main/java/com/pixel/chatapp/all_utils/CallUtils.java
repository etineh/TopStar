package com.pixel.chatapp.all_utils;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.widget.Toast;

import com.pixel.chatapp.R;

import java.io.IOException;

public class CallUtils {

    Context context;
    Ringtone ringtone;
    private MediaPlayer mediaPlayer;
    private MediaPlayer ringbackPlayer;

    public CallUtils(Context context) {
        this.context = context;
    }

    // Method to play the ringtone
    public void playRingtone() {
        try {
            Uri defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(context, defaultRingtoneUri);
            ringtone.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to stop the ringtone
    public void stopRingtone() {
        try {
            if (ringtone != null && ringtone.isPlaying()) {
                ringtone.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Method to start continuous vibration
    public void startContinuousVibration() {
        try {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null && audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
                // If the phone is not in silent mode, start the custom vibration pattern
                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null) {
                    // Define the custom vibration pattern
                    long[] pattern = {0, // Start immediately
                            3000, // Vibrate for 3 seconds
                            1500}; // Pause for 1 second

                    // Vibrate with the custom pattern
                    vibrator.vibrate(pattern, 0); // -1 for no repeat, 0 to repeat at pattern[0] index
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to stop vibration
    public void stopVibration() {
        try {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.cancel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to start ringing
    public void startRingingIndicator(boolean useSpeakerphone) {
        // Get AudioManager instance
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        // Set the audio mode to MODE_RINGTONE for ringing behavior
        audioManager.setMode(AudioManager.MODE_RINGTONE);

        // Determine the audio stream type based on the call mode
        int streamType = useSpeakerphone ? AudioManager.STREAM_RING : AudioManager.STREAM_VOICE_CALL;

        // Set the audio attributes for the MediaPlayer
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setLegacyStreamType(streamType)
                .build();

        // Create a MediaPlayer instance and set the audio attributes
        ringbackPlayer = new MediaPlayer();
        ringbackPlayer.setAudioAttributes(attributes);

        try {
            // Prepare the MediaPlayer with the ringback tone audio file
            ringbackPlayer.setDataSource(context, Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.ringtone));
            ringbackPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle error
            return;
        }

        // Set looping to true for continuous playback
        ringbackPlayer.setLooping(true);

        // Start playback
        ringbackPlayer.start();
    }




    // Method to stop ringing
    public void stopRingingIndicator() {
        // Stop and release the MediaPlayer
        if (ringbackPlayer != null) {
            ringbackPlayer.stop();
            ringbackPlayer.release();
            ringbackPlayer = null;
        }
    }

}

