package com.example.myapplication.Model;

import java.util.List;

public class Cafe {
    private String id;
    private String name;
    private String address; // Có thể giữ lại nếu cần dùng ở nơi khác
    private String locationText; // Thêm field mới để lưu địa chỉ từ Firestore
    private String description;
    private Double ratingStar;
    private String activity;
    private String image1;
    private double lat;
    private double lng;
    private List<Review> reviews;

    // Constructor rỗng (yêu cầu bởi Firestore)
    public Cafe() {}

    // Getters và Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLocationText() {
        return locationText;
    }

    public void setLocationText(String locationText) {
        this.locationText = locationText;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getRatingStar() {
        return ratingStar;
    }

    public void setRatingStar(Double ratingStar) {
        this.ratingStar = ratingStar;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getImage1() {
        return image1;
    }

    public void setImage1(String image1) {
        this.image1 = image1;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public List<Review> getComments() {
        return reviews;
    }

    public void setComments(List<Review> reviews) {
        this.reviews = reviews;
    }
}