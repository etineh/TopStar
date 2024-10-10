package com.pixel.chatapp.utilities;

import android.content.Context;

import com.pixel.chatapp.constants.K;

import java.io.File;

public class FolderUtils {

    public static File getPhotoFolder(Context context){
        File appSpecificFolder = new File(context.getExternalFilesDir(null), K.ALL_PHOTOS);
        if (!appSpecificFolder.exists()) {
            appSpecificFolder.mkdirs();
        }
        return appSpecificFolder;
    }

    public static File getFileDir(Context context){
        File appSpecificFolder = context.getExternalFilesDir(null);
        if (appSpecificFolder != null && !appSpecificFolder.exists()) {
            appSpecificFolder.mkdirs();
        }
        return appSpecificFolder;
    }

    public static File getCacheDir(Context context){
        File appSpecificFolder = context.getCacheDir();
        if (appSpecificFolder != null && !appSpecificFolder.exists()) {
            appSpecificFolder.mkdirs();
        }
        return appSpecificFolder;
    }

    public static File getVideoFolder(Context context){
        File appSpecificFolder = new File(context.getExternalFilesDir(null), K.ALL_VIDEOs);
        if (!appSpecificFolder.exists()) {
            appSpecificFolder.mkdirs();
        }
        return appSpecificFolder;
    }


    public static File getThumbnailFolder(Context context){
        File appSpecificFolder = new File(context.getExternalFilesDir(null), K.ALL_THUMBNAIL);
        if (!appSpecificFolder.exists()) {
            appSpecificFolder.mkdirs();
        }
        return appSpecificFolder;
    }

    public static File getDocumentFolder(Context context){
        File appSpecificFolder = new File(context.getExternalFilesDir(null), K.ALL_DOCUMENTS);
        if (!appSpecificFolder.exists()) {
            appSpecificFolder.mkdirs();
        }
        return appSpecificFolder;
    }

    public static File getAudioFolder(Context context){
        File appSpecificFolder = new File(context.getExternalFilesDir(null), K.ALL_AUDIO);
        if (!appSpecificFolder.exists()) {
            appSpecificFolder.mkdirs();
        }
        return appSpecificFolder;
    }

    public static File getVoiceNoteFolder(Context context){
        File appSpecificFolder = new File(context.getExternalFilesDir(null), K.ALL_VOICENOTE);
        if (!appSpecificFolder.exists()) {
            appSpecificFolder.mkdirs();
        }
        return appSpecificFolder;
    }

}
