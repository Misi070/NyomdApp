package com.example.nyomdapp;

public class UploadItem {
    private String fileName;
    private int copyCount;
    private String status;

    // Üres konstruktor kell Firestore-hoz
    public UploadItem() {}

    public UploadItem(String fileName, int copyCount, String status) {
        this.fileName = fileName;
        this.copyCount = copyCount;
        this.status = status;
    }

    public String getFileName() {
        return fileName;
    }

    public int getCopyCount() {
        return copyCount;
    }

    public String getStatus() {
        return status;
    }
}
