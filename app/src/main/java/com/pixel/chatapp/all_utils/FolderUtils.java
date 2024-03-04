package com.pixel.chatapp.all_utils;

import android.content.Context;

import com.pixel.chatapp.constants.AllConstants;

import java.io.File;

public class FolderUtils {

    public static File getPhotoFolder(Context context){
        File appSpecificFolder = new File(context.getExternalFilesDir(null), AllConstants.ALL_PHOTOS);
        if (!appSpecificFolder.exists()) {
            appSpecificFolder.mkdirs();
        }
        return appSpecificFolder;
    }

    public static File getVideoFolder(Context context){
        File appSpecificFolder = new File(context.getExternalFilesDir(null), AllConstants.ALL_VIDEOs);
        if (!appSpecificFolder.exists()) {
            appSpecificFolder.mkdirs();
        }
        return appSpecificFolder;
    }


    public static File getThumbnailFolder(Context context){
        File appSpecificFolder = new File(context.getExternalFilesDir(null), AllConstants.ALL_THUMBNAIL);
        if (!appSpecificFolder.exists()) {
            appSpecificFolder.mkdirs();
        }
        return appSpecificFolder;
    }

    public static File getDocumentFolder(Context context){
        File appSpecificFolder = new File(context.getExternalFilesDir(null), AllConstants.ALL_DOCUMENTS);
        if (!appSpecificFolder.exists()) {
            appSpecificFolder.mkdirs();
        }
        return appSpecificFolder;
    }

    public static File getAudioFolder(Context context){
        File appSpecificFolder = new File(context.getExternalFilesDir(null), AllConstants.ALL_AUDIO);
        if (!appSpecificFolder.exists()) {
            appSpecificFolder.mkdirs();
        }
        return appSpecificFolder;
    }

    public static File getVoiceNoteFolder(Context context){
        File appSpecificFolder = new File(context.getExternalFilesDir(null), AllConstants.ALL_VOICENOTE);
        if (!appSpecificFolder.exists()) {
            appSpecificFolder.mkdirs();
        }
        return appSpecificFolder;
    }

}
