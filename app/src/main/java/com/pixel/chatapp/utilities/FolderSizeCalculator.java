package com.pixel.chatapp.utilities;

import android.content.Context;

import com.pixel.chatapp.dataModel.FileItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FolderSizeCalculator {

    public static String getFormattedSize(long sizeInBytes) {
        if (sizeInBytes < 1024) {
            return sizeInBytes + " bytes";
        } else if (sizeInBytes < 1024 * 1024) {
            return String.format("%.2f KB", (double) sizeInBytes / 1024);
        } else if (sizeInBytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", (double) sizeInBytes / (1024 * 1024));
        } else {
            return String.format("%.2f GB", (double) sizeInBytes / (1024 * 1024 * 1024));
        }
    }


    public static FileItem getItems(File folder) {
        List<String> fileList = new ArrayList<>();
        long length = 0;

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if(file.isFile()){
                        fileList.add(0, file.getPath());

                        length += file.length();
                    } else {
                        length += getItems(file).getFileSize();

                    }
                }
            }
        }
        return new FileItem(fileList, length);
    }

    public static long getRoomDatabaseSize(Context context) {
        // Get the directory path of your Room database
        File databaseDir = context.getDatabasePath("winnerChat_database").getParentFile();
        if (databaseDir == null || !databaseDir.exists()) {
            return 0; // Directory doesn't exist or is empty
        }

        // Iterate through all files in the directory and sum up their sizes
        long totalSize = 0;
        File[] files = databaseDir.listFiles();
        if (files != null) {
            for (File file : files) {
                totalSize += file.length();
            }
        }

        return totalSize;
    }

    public static void fileNameAndSize(File folder, GetResult result) {

        new Thread(()-> result.getItemResult(getItems(folder)) ).start();

    }

    public static String databaseSize(Context context) {
        long databaseSize = getRoomDatabaseSize(context);
        return getFormattedSize(databaseSize);
    }

    public interface GetResult {
        void getItemResult(FileItem files);
    }

}

