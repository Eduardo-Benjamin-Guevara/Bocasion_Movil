package com.proyecto.capstone.models;
import org.junit.Test;
import static org.junit.Assert.*;

public class CategoryTest {
    @Test
    public void constructorAndGetters_workCorrectly() {
        Category c = new Category("1", "Bebidas", "Bebidas frías y calientes");
        assertEquals("1", c.getId());
        assertEquals("Bebidas", c.getName());
        assertEquals("Bebidas frías y calientes", c.getDescription());
    }

    @Test
    public void setters_workCorrectly() {
        Category c = new Category();
        c.setId("10");
        c.setName("Postres");
        c.setDescription("Dulces y helados");
        assertEquals("10", c.getId());
        assertEquals("Postres", c.getName());
        assertEquals("Dulces y helados", c.getDescription());
    }

}
