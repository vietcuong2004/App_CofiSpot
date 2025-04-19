package com.example.myapplication.Model;

public class Notification {
    private String message;
    private long timestamp;

    // Constructor mặc định (cần cho Firestore)
    public Notification() {}


    public Notification(String message, long timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}