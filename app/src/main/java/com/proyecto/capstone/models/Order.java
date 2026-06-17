package com.proyecto.capstone.models;

import java.util.Date;
import java.util.List;

public class Order {

    private String userId;
    private String userName;
    private String userEmail;
    private List<OrderItem> items;
    private Long scheduledTime;
    private double totalPrice;

    private String orderCode;

    private String status;

    private String paymentMethod;

    private boolean paymentConfirmed;

    private String cookId;

    private Date createdAt;

    private Long readyAt;

    private Boolean waitingReview;

    private Boolean reviewed;

    public Order() {
    }

    public Order(String userId,
                 String userName,
                 String userEmail,
                 List<OrderItem> items,
                 double totalPrice,
                 String orderCode,
                 String status,
                 String paymentMethod,
                 boolean paymentConfirmed,
                 String cookId,
                 Date createdAt,
                 Long readyAt,
                 Boolean waitingReview,
                 Boolean reviewed,
                 Long scheduledTime) {

        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.items = items;
        this.totalPrice = totalPrice;
        this.orderCode = orderCode;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.paymentConfirmed = paymentConfirmed;
        this.cookId = cookId;
        this.createdAt = createdAt;
        this.readyAt = readyAt;
        this.waitingReview = waitingReview;
        this.reviewed = reviewed;
        this.scheduledTime = scheduledTime;
    }

    public Long getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(Long scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public boolean isPaymentConfirmed() {
        return paymentConfirmed;
    }

    public void setPaymentConfirmed(boolean paymentConfirmed) {
        this.paymentConfirmed = paymentConfirmed;
    }

    public String getCookId() {
        return cookId;
    }

    public void setCookId(String cookId) {
        this.cookId = cookId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Long getReadyAt() {
        return readyAt;
    }

    public void setReadyAt(Long readyAt) {
        this.readyAt = readyAt;
    }

    public Boolean getWaitingReview() {
        return waitingReview;
    }

    public void setWaitingReview(Boolean waitingReview) {
        this.waitingReview = waitingReview;
    }

    public Boolean getReviewed() {
        return reviewed;
    }

    public void setReviewed(Boolean reviewed) {
        this.reviewed = reviewed;
    }

    public static class OrderItem {

        private String itemId;
        private int quantity;

        public OrderItem() {
        }

        public OrderItem(String itemId, int quantity) {
            this.itemId = itemId;
            this.quantity = quantity;
        }

        public String getItemId() {
            return itemId;
        }

        public void setItemId(String itemId) {
            this.itemId = itemId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}
