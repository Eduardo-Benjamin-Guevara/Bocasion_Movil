package com.proyecto.capstone.activities.cook.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.proyecto.capstone.R;
import com.proyecto.capstone.adapters.OrderAdapter;
import com.proyecto.capstone.models.Order;
import com.proyecto.capstone.utils.NotificationHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreparingOrdersFragment extends Fragment
        implements OrderAdapter.OnOrderActionListener {

    private RecyclerView recyclerView;

    private OrderAdapter adapter;

    private final List<Order> orderList =
            new ArrayList<>();

    private final List<String> orderKeys =
            new ArrayList<>();

    private final Map<String, String> itemIdToNameMap =
            new HashMap<>();

    private String currentCookId;

    private DatabaseReference ordersDbRef;

    private ValueEventListener ordersListener;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view =
                inflater.inflate(
                        R.layout.fragment_preparing_orders,
                        container,
                        false
                );

        FirebaseUser currentUser =
                FirebaseAuth.getInstance()
                        .getCurrentUser();

        if (currentUser == null) {
            return view;
        }

        currentCookId =
                currentUser.getUid();

        recyclerView =
                view.findViewById(
                        R.id.recycler_orders
                );

        recyclerView.setLayoutManager(
                new LinearLayoutManager(
                        getContext()
                )
        );

        adapter =
                new OrderAdapter(
                        orderList,
                        itemIdToNameMap,
                        this,
                        OrderAdapter.ViewType.PREPARING
                );

        recyclerView.setAdapter(adapter);

        loadItems();

        loadOrders();

        return view;
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();

        if (ordersDbRef != null
                && ordersListener != null) {

            ordersDbRef.removeEventListener(
                    ordersListener
            );
        }
    }

    private void loadItems() {

        FirebaseDatabase.getInstance()
                .getReference("items")
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot
                            ) {

                                itemIdToNameMap.clear();

                                for (DataSnapshot s :
                                        snapshot.getChildren()) {

                                    String itemId =
                                            s.getKey();

                                    String itemName =
                                            s.child("name")
                                                    .getValue(
                                                            String.class
                                                    );

                                    if (itemId != null
                                            && itemName != null) {

                                        itemIdToNameMap.put(
                                                itemId,
                                                itemName
                                        );
                                    }
                                }

                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error
                            ) {
                            }
                        });
    }

    private void loadOrders() {

        ordersDbRef =
                FirebaseDatabase.getInstance()
                        .getReference("orders");

        ordersListener =
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot
                    ) {

                        orderList.clear();
                        orderKeys.clear();

                        for (DataSnapshot s :
                                snapshot.getChildren()) {

                            Order order =
                                    s.getValue(
                                            Order.class
                                    );

                            if (order != null
                                    &&
                                    currentCookId.equals(
                                            order.getCookId()
                                    )
                                    &&
                                    (
                                            "accepted".equals(
                                                    order.getStatus()
                                            )
                                                    ||
                                                    "preparing".equals(
                                                            order.getStatus()
                                                    )
                                    )) {

                                orderList.add(order);

                                orderKeys.add(
                                        s.getKey()
                                );
                            }
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error
                    ) {

                        Toast.makeText(
                                getContext(),
                                "Error cargando pedidos",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                };

        ordersDbRef.addValueEventListener(
                ordersListener
        );
    }

    @Override
    public void onAction(Order order) {

        markAsReady(order);
    }

    private void markAsReady(Order order) {

        int index =
                orderList.indexOf(order);

        if (index == -1) return;

        if (!order.isPaymentConfirmed()) {

            NotificationHelper.showNotification(
                    getContext(),
                    "Pago pendiente",
                    "Debes confirmar el pago antes."
            );

            return;
        }

        String orderKey =
                orderKeys.get(index);

        Map<String, Object> updates =
                new HashMap<>();

        updates.put("status", "ready");
        updates.put("waitingReview", true);
        updates.put("reviewed", false);
        updates.put(
                "readyAt",
                System.currentTimeMillis()
        );

        FirebaseDatabase.getInstance()
                .getReference("orders")
                .child(orderKey)
                .updateChildren(updates)

                .addOnSuccessListener(aVoid -> {

                    NotificationHelper.showNotification(
                            getContext(),
                            "Pedido listo",
                            "Esperando calificación del usuario"
                    );
                });
    }
}