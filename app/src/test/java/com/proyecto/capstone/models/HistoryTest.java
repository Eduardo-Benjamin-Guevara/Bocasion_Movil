package com.proyecto.capstone.models;

import org.junit.Test;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static org.junit.Assert.*;

public class HistoryTest {

    @Test
    public void constructorAndGetters_workCorrectly() {
        Date now = new Date();
        List<String> orders = new ArrayList<>();
        orders.add("ORD123");
        orders.add("ORD456");

        History history = new History(now, orders, 450.25);

        assertEquals(now, history.getDate());
        assertEquals(orders, history.getOrderIds());
        assertEquals(450.25, history.getTotalEarnings(), 0.001);
    }

    @Test
    public void setters_workCorrectly() {
        History history = new History();
        Date now = new Date();
        List<String> orders = new ArrayList<>();
        orders.add("ORD789");

        history.setDate(now);
        history.setOrderIds(orders);
        history.setTotalEarnings(120.00);

        assertEquals(now, history.getDate());
        assertEquals(orders, history.getOrderIds());
        assertEquals(120.00, history.getTotalEarnings(), 0.001);
    }
}