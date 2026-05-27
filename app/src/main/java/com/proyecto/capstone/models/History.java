package com.proyecto.capstone.models;

import java.util.Date;
import java.util.List;

public class History {
    private Date date;
    private List<String> orderIds;
    private double totalEarnings;

    public History() {}

    public History(Date date, List<String> orderIds, double totalEarnings) {
        this.date = date;
        this.orderIds = orderIds;
        this.totalEarnings = totalEarnings;
    }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public List<String> getOrderIds() { return orderIds; }
    public void setOrderIds(List<String> orderIds) { this.orderIds = orderIds; }

    public double getTotalEarnings() { return totalEarnings; }
    public void setTotalEarnings(double totalEarnings) { this.totalEarnings = totalEarnings; }
}