package com.proyecto.capstone.models;

import org.junit.Test;
import static org.junit.Assert.*;

public class SalesTest {

    @Test
    public void constructorAndGetters_workCorrectly() {
        Sales sales = new Sales(1500.75);
        assertEquals(1500.75, sales.getTotalSales(), 0.001);
    }

    @Test
    public void setters_workCorrectly() {
        Sales sales = new Sales();
        sales.setTotalSales(3200.50);
        assertEquals(3200.50, sales.getTotalSales(), 0.001);
    }
}