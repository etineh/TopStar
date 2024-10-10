package com.pixel.chatapp.dataModel;


import java.util.List;

public class FileItem {
    private List<String> fileList;
    private long fileSize;

    public FileItem(List<String> fileList, long fileSize) {
        this.fileList = fileList;
        this.fileSize = fileSize;
    }

    public List<String> getFileList() {
        return fileList;
    }

    public long getFileSize() {
        return fileSize;
    }
}
