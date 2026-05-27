package com.proyecto.capstone.models;

import org.junit.Test;
import static org.junit.Assert.*;

public class ItemTest {

    @Test
    public void constructorAndGetters_workCorrectly() {
        Item item = new Item("Hamburguesa", "Deliciosa hamburguesa con queso", 8.50, 20, "CAT123", true, "http://image.com/burguer.png");

        assertEquals("Hamburguesa", item.getName());
        assertEquals("Deliciosa hamburguesa con queso", item.getDescription());
        assertEquals(8.50, item.getPrice(), 0.001);
        assertEquals(20, item.getStock());
        assertEquals("CAT123", item.getCategoryId());
        assertTrue(item.isAvailable());
        assertEquals("http://image.com/burguer.png", item.getImageUrl());
    }

    @Test
    public void setters_workCorrectly() {
        Item item = new Item();

        item.setName("Pizza");
        item.setDescription("Pizza Pepperoni");
        item.setPrice(12.00);
        item.setStock(15);
        item.setCategoryId("CAT456");
        item.setAvailable(false);
        item.setImageUrl("http://image.com/pizza.png");

        assertEquals("Pizza", item.getName());
        assertEquals("Pizza Pepperoni", item.getDescription());
        assertEquals(12.00, item.getPrice(), 0.001);
        assertEquals(15, item.getStock());
        assertEquals("CAT456", item.getCategoryId());
        assertFalse(item.isAvailable());
        assertEquals("http://image.com/pizza.png", item.getImageUrl());
    }
}