package com.proyecto.capstone.utils;

import java.util.Random;

public class OrderCodeGenerator {
    public static String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}