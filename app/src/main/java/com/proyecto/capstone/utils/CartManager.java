package com.proyecto.capstone.utils;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.proyecto.capstone.models.Item;
import com.proyecto.capstone.models.Order;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CartManager {

    private static CartManager instance;

    private final List<Order.OrderItem> cartItems =
            new ArrayList<>();

    public interface OnTotalCalculatedListener {

        void onTotalCalculated(double total);

        void onError(String error);
    }

    public interface OnStockCheckListener {

        void onQuantityUpdated(
                boolean success,
                int actualQuantityOrMaxStock
        );
    }

    private CartManager() {
    }

    public static CartManager getInstance() {

        if (instance == null) {

            instance = new CartManager();
        }

        return instance;
    }

    public List<Order.OrderItem> getCartItems() {

        return cartItems;
    }

    public int getQuantity(String itemId) {

        for (Order.OrderItem item : cartItems) {

            if (item.getItemId().equals(itemId)) {

                return item.getQuantity();
            }
        }

        return 0;
    }

    public void clearCart() {

        cartItems.clear();
    }

    public void updateItemQuantity(String itemId,
                                   int newQuantity,
                                   OnStockCheckListener listener) {

        if (newQuantity < 0) {

            if (listener != null) {

                listener.onQuantityUpdated(
                        false,
                        0
                );
            }

            return;
        }

        if (newQuantity == 0) {

            removeItem(itemId);

            if (listener != null) {

                listener.onQuantityUpdated(
                        true,
                        0
                );
            }

            return;
        }

        DatabaseReference itemRef =
                FirebaseDatabase.getInstance()
                        .getReference("items")
                        .child(itemId);

        itemRef.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot
                    ) {

                        Item item =
                                snapshot.getValue(
                                        Item.class
                                );

                        if (item != null
                                && item.isAvailable()) {

                            int maxStock =
                                    item.getStock();

                            int finalQuantity =
                                    Math.min(
                                            newQuantity,
                                            maxStock
                                    );

                            if (finalQuantity > 0) {

                                Order.OrderItem existingItem =
                                        null;

                                for (Order.OrderItem cartItem :
                                        cartItems) {

                                    if (cartItem.getItemId()
                                            .equals(itemId)) {

                                        existingItem =
                                                cartItem;

                                        break;
                                    }
                                }

                                if (existingItem != null) {

                                    existingItem.setQuantity(
                                            finalQuantity
                                    );

                                } else {

                                    cartItems.add(
                                            new Order.OrderItem(
                                                    itemId,
                                                    finalQuantity
                                            )
                                    );
                                }

                                if (listener != null) {

                                    listener.onQuantityUpdated(
                                            true,
                                            finalQuantity
                                    );
                                }

                            } else {

                                if (listener != null) {

                                    listener.onQuantityUpdated(
                                            false,
                                            item.getStock()
                                    );
                                }
                            }

                        } else {

                            if (listener != null) {

                                listener.onQuantityUpdated(
                                        false,
                                        0
                                );
                            }
                        }
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error
                    ) {

                        if (listener != null) {

                            listener.onQuantityUpdated(
                                    false,
                                    0
                            );
                        }
                    }
                });
    }

    public void removeItem(String itemId) {

        Iterator<Order.OrderItem> iterator =
                cartItems.iterator();

        while (iterator.hasNext()) {

            Order.OrderItem item =
                    iterator.next();

            if (item.getItemId().equals(itemId)) {

                iterator.remove();
            }
        }
    }

    public double calculateTotal(
            Map<String, Item> itemMap
    ) {

        double total = 0.0;

        for (Order.OrderItem orderItem :
                cartItems) {

            Item item =
                    itemMap.get(
                            orderItem.getItemId()
                    );

            if (item != null) {

                total +=
                        item.getPrice()
                                * orderItem.getQuantity();
            }
        }

        return total;
    }
}