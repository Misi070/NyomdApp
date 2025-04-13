package com.example.nyomdapp.adapters;


public class Document {
    private String fileUrl;
    private int copies;
    private String status;

    public Document() {
        // Firestore requires a no-argument constructor
    }

    public Document(String fileUrl, int copies, String status) {
        this.fileUrl = fileUrl;
        this.copies = copies;
        this.status = status;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public int getCopies() {
        return copies;
    }

    public void setCopies(int copies) {
        this.copies = copies;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
