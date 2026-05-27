package com.proyecto.capstone.models;

import org.junit.Test;
import java.util.Date;
import static org.junit.Assert.*;

public class UserTest {

    @Test
    public void constructorAndGetters_workCorrectly() {
        Date now = new Date();
        User user = new User("UID123", "test@correo.com", "Carlos V", "ADMIN", now, "securePassword123");

        assertEquals("UID123", user.getUid());
        assertEquals("test@correo.com", user.getEmail());
        assertEquals("Carlos V", user.getName());
        assertEquals("ADMIN", user.getRole());
        assertEquals(now, user.getCreatedAt());
        assertEquals("securePassword123", user.getPassword());
    }

    @Test
    public void setters_workCorrectly() {
        User user = new User();
        Date now = new Date();

        user.setUid("UID456");
        user.setEmail("user@correo.com");
        user.setName("Ana G");
        user.setRole("CUSTOMER");
        user.setCreatedAt(now);
        user.setPassword("anotherPassword");

        assertEquals("UID456", user.getUid());
        assertEquals("user@correo.com", user.getEmail());
        assertEquals("Ana G", user.getName());
        assertEquals("CUSTOMER", user.getRole());
        assertEquals(now, user.getCreatedAt());
        assertEquals("anotherPassword", user.getPassword());
    }
}