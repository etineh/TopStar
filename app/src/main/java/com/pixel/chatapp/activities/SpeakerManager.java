package com.pixel.chatapp.activities;

import android.content.Context;
import android.media.AudioManager;
import android.widget.Toast;

public class SpeakerManager {

    private final Context context;
    private final AudioManager audioManager;

    public SpeakerManager(Context context) {
        this.context = context;
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    // Method to switch audio output to speakerphone
    public void setSpeakerphoneOn(boolean on) {
        if (on) {
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            audioManager.setSpeakerphoneOn(true);
            Toast.makeText(context, "Mic on", Toast.LENGTH_SHORT).show();
        } else {
            audioManager.setSpeakerphoneOn(false);
            audioManager.setMode(AudioManager.MODE_NORMAL);
            Toast.makeText(context, "Mic off", Toast.LENGTH_SHORT).show();

        }
    }

    // Method to toggle between earpiece and speakerphone
    public void toggleSpeakerphone() {
        boolean isSpeakerphoneOn = audioManager.isSpeakerphoneOn();
        setSpeakerphoneOn(!isSpeakerphoneOn);
    }

    // Method to set the volume
    public void setVolume(int volumeLevel) {
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, volumeLevel, 0);
    }
}
