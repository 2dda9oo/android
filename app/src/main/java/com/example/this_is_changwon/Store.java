package com.example.this_is_changwon;

public class Store {
    private String name;
    private String type;
    private String classify;
    private String details;
    private String address;
    private String lat;
    private String lon;
    private double whoWeight;
    private double whereWeight;
    private double rating;
    private double totalScore;
    private double reviewCount;

    public Store(String name, String type, String classify, String details, String address, String lat, String lon) {
        this.name = name;
        this.type = type;
        this.classify = classify;
        this.details = details;
        this.address = address;
        this.lat = lat;
        this.lon = lon;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getClassify() {
        return classify;
    }

    public String getDetails() {
        return details;
    }

    public String getAddress() {
        return address;
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }

    public double getWhoWeight() {
        return whoWeight;
    }

    public void setWhoWeight(double whoWeight) {
        this.whoWeight = whoWeight;
    }

    public double getWhereWeight() {
        return whereWeight;
    }

    public void setWhereWeight(double whereWeight) {
        this.whereWeight = whereWeight;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public double getReviewCount() {
        return reviewCount;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(double totalScore) {
        this.totalScore = totalScore;
    }

    public void setReviewCount(double reviewCount) {
        this.reviewCount = reviewCount;
    }
}

