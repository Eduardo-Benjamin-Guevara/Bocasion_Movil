package com.proyecto.capstone.models;

public class Sales {
    private double totalSales;

    public Sales() {}

    public Sales(double totalSales) {
        this.totalSales = totalSales;
    }

    public double getTotalSales() { return totalSales; }
    public void setTotalSales(double totalSales) { this.totalSales = totalSales; }
}