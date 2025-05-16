package com.example.nyomdapp.model;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UploadEntry {
    private String fileName;
    private int copies;
    private String status;
    private Timestamp timestamp;
    private String userId;
    private int statusUpdateStep;
    private String size;
    private String documentId;

    // Üres konstruktor Firebase-hez
    public UploadEntry() {}

    public UploadEntry(String fileName, int copies, String status, Date date, String userId, String documentId, String size) {
        this.fileName = fileName;
        this.copies = copies;
        this.status = status;
        this.timestamp = new Timestamp(date);
        this.userId = userId;
        this.statusUpdateStep = 0;
        this.documentId = documentId;
        this.size = size;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Date getDate() {
        return timestamp != null ? timestamp.toDate() : null;
    }

    public void setDate(Date date) {
        this.timestamp = new Timestamp(date);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getStatusUpdateStep() {
        return statusUpdateStep;
    }

    public void setStatusUpdateStep(int statusUpdateStep) {
        this.statusUpdateStep = statusUpdateStep;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    @Override
    public String toString() {
        String formattedDate = "N/A";
        if (timestamp != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MMMM d., HH:mm");
            formattedDate = sdf.format(timestamp.toDate());
        }

        return "Fájl neve: " + fileName +
                "\nPéldányszám: " + copies +
                "\nStátusz: " + status +
                "\nMéret: " + (size != null ? size : "ismeretlen") +
                "\nFeltöltés dátuma: " + formattedDate;
    }
}
