package com.proyecto.capstone.models;

import org.junit.Test;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static org.junit.Assert.*;

public class OrderTest {

    @Test
    public void constructorAndGetters_workCorrectly() {
        List<Order.OrderItem> items = new ArrayList<>();
        items.add(new Order.OrderItem("ITEM1", 2));
        Date now = new Date();

        Order order = new Order(
                "USER123", "Juan Perez", items, 25.50, "ORDER-001",
                "PENDING", "CARD", true, "COOK789", now,
                1620000000L, true, false, 1610000000L
        );

        assertEquals("USER123", order.getUserId());
        assertEquals("Juan Perez", order.getUserName());
        assertEquals(items, order.getItems());
        assertEquals(25.50, order.getTotalPrice(), 0.001);
        assertEquals("ORDER-001", order.getOrderCode());
        assertEquals("PENDING", order.getStatus());
        assertEquals("CARD", order.getPaymentMethod());
        assertTrue(order.isPaymentConfirmed());
        assertEquals("COOK789", order.getCookId());
        assertEquals(now, order.getCreatedAt());
        assertEquals(Long.valueOf(1620000000L), order.getReadyAt());
        assertTrue(order.getWaitingReview());
        assertFalse(order.getReviewed());
        assertEquals(Long.valueOf(1610000000L), order.getScheduledTime());
    }

    @Test
    public void setters_workCorrectly() {
        Order order = new Order();
        List<Order.OrderItem> items = new ArrayList<>();
        Date now = new Date();

        order.setUserId("USER456");
        order.setUserName("Maria Lopez");
        order.setItems(items);
        order.setTotalPrice(40.0);
        order.setOrderCode("ORDER-002");
        order.setStatus("DELIVERED");
        order.setPaymentMethod("CASH");
        order.setPaymentConfirmed(false);
        order.setCookId("COOK123");
        order.setCreatedAt(now);
        order.setReadyAt(1630000000L);
        order.setWaitingReview(false);
        order.setReviewed(true);
        order.setScheduledTime(1640000000L);

        assertEquals("USER456", order.getUserId());
        assertEquals("Maria Lopez", order.getUserName());
        assertEquals(items, order.getItems());
        assertEquals(40.0, order.getTotalPrice(), 0.001);
        assertEquals("ORDER-002", order.getOrderCode());
        assertEquals("DELIVERED", order.getStatus());
        assertEquals("CASH", order.getPaymentMethod());
        assertFalse(order.isPaymentConfirmed());
        assertEquals("COOK123", order.getCookId());
        assertEquals(now, order.getCreatedAt());
        assertEquals(Long.valueOf(1630000000L), order.getReadyAt());
        assertFalse(order.getWaitingReview());
        assertTrue(order.getReviewed());
        assertEquals(Long.valueOf(1640000000L), order.getScheduledTime());
    }

    @Test
    public void orderItem_worksCorrectly() {
        Order.OrderItem orderItem = new Order.OrderItem("ITEM99", 5);
        assertEquals("ITEM99", orderItem.getItemId());
        assertEquals(5, orderItem.getQuantity());

        Order.OrderItem emptyItem = new Order.OrderItem();
        emptyItem.setItemId("ITEM88");
        emptyItem.setQuantity(3);
        assertEquals("ITEM88", emptyItem.getItemId());
        assertEquals(3, emptyItem.getQuantity());
    }
}