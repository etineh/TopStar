package com.pixel.chatapp.utilities;

import android.content.Context;

import java.io.File;

public class CacheUtils {

    public static long getCacheSize(Context context) {
        long cacheSize = 0;
        File cacheDir = context.getCacheDir();
        if (cacheDir.isDirectory()) {
            File[] cacheFiles = cacheDir.listFiles();
            if (cacheFiles != null) {
                for (File file : cacheFiles) {
                    cacheSize += file.length();
                }
            }
        }
        return cacheSize / 1024 / 1024; // in MB
    }

    public static void clearCache(Context context) {
        File cacheDir = context.getCacheDir();
        if (cacheDir != null && cacheDir.isDirectory()) {
            File[] cacheFiles = cacheDir.listFiles();
            if (cacheFiles != null) {
                for (File file : cacheFiles) {
                    file.delete();
                }
            }
        }
    }


}
