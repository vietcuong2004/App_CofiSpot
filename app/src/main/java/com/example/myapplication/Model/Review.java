package com.example.myapplication.Model;

import com.google.firebase.Timestamp;
import java.util.List;

public class Review {
    private String userId;
    private String cafeId;
    private float rating;
    private String comment;
    private String activity;
    private String otherActivityDescription;
    private List<String> images;
    private Timestamp timestamp;
    private String username;

    public Review() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCafeId() {
        return cafeId;
    }

    public void setCafeId(String cafeId) {
        this.cafeId = cafeId;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getOtherActivityDescription() {
        return otherActivityDescription;
    }

    public void setOtherActivityDescription(String otherActivityDescription) {
        this.otherActivityDescription = otherActivityDescription;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}