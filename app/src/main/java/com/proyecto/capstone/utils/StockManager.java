package com.proyecto.capstone.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.proyecto.capstone.models.Item;

public class StockManager {
    public static void reduceStock(String itemId, int quantity) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("items").child(itemId);
        db.get().addOnSuccessListener(snapshot -> {
            Item item = snapshot.getValue(Item.class);
            if (item != null && item.getStock() >= quantity) {
                item.setStock(item.getStock() - quantity);
                db.setValue(item);
            }
        });
    }
}