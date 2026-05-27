package com.proyecto.capstone.models;

import org.junit.Test;
import java.util.Date;
import static org.junit.Assert.*;

public class ReviewTest {

    @Test
    public void constructorAndGetters_workCorrectly() {
        Date now = new Date();
        Review review = new Review("USER123", "ITEM456", "ORD-789", 5, "Excelente servicio y comida", now);

        assertEquals("USER123", review.getUserId());
        assertEquals("ITEM456", review.getItemId());
        assertEquals("ORD-789", review.getOrderCode());
        assertEquals(5, review.getRating());
        assertEquals("Excelente servicio y comida", review.getComment());
        assertEquals(now, review.getCreatedAt());
    }

    @Test
    public void emptyConstructor_returnsNullValues() {
        Review review = new Review();
        assertNull(review.getUserId());
        assertNull(review.getItemId());
        assertNull(review.getOrderCode());
        assertEquals(0, review.getRating());
        assertNull(review.getComment());
        assertNull(review.getCreatedAt());
    }
}