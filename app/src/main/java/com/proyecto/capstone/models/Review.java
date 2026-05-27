package com.proyecto.capstone.models;

import java.util.Date;

public class Review {

    private String userId;
    private String itemId;
    private String orderCode;
    private int rating;
    private String comment;
    private Date createdAt;

    public Review() {
    }

    public Review(String userId,
                  String itemId,
                  String orderCode,
                  int rating,
                  String comment,
                  Date createdAt) {

        this.userId = userId;
        this.itemId = itemId;
        this.orderCode = orderCode;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public String getItemId() {
        return itemId;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}