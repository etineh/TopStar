package com.pixel.chatapp.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.pixel.chatapp.constants.Ki;

public class AppPermission {

    public boolean isStorageOk(Context context) {
        String permission;
        if(Build.VERSION.SDK_INT >= 33){    // for SDK 33 or android version 13 above
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {    // for below SDK 33 or below android version 13
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestStorage(Activity activity) {
        String permission;
        if(Build.VERSION.SDK_INT >= 33){    // for SDK 33 or android version 13 above
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {    // for below SDK 33 or below android version 13
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }
        ActivityCompat.requestPermissions(activity, new String[]{permission, Manifest.permission.WRITE_EXTERNAL_STORAGE}, Ki.STORAGE_REQUEST_CODE);
    }

    public boolean isContactOk(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestContact(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_CONTACTS}, Ki.CONTACTS_REQUEST_CODE);
    }

    public boolean isRecordingOk(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestRecording(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, Ki.RECORDING_REQUEST_CODE);
    }

    public boolean isCameraOk(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestCamera(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, Ki.CAMERA_REQUEST_CODE);
    }

    public void requestCameraForCall(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, Ki.CALL_CAMERA_REQUEST_CODE);
    }

    public void requestRecordingForCall(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, Ki.CALL_RECORDING_REQUEST_CODE);
    }

    public boolean isBiometricOk(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.USE_BIOMETRIC) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestBiometric(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.USE_BIOMETRIC}, Ki.BIOMETRIC_REQUEST_CODE);
    }

    public boolean isNotificationOk(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestNotification(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.POST_NOTIFICATIONS}, Ki.NOTIFICATION_REQUEST_CODE);
    }

}



