package com.proyecto.capstone.activities.cook.fragments;

import android.media.MediaPlayer;
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

public class PendingOrdersFragment extends Fragment
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

    private ValueEventListener pendingOrdersListener;

    private int lastOrderCount = 0;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view =
                inflater.inflate(
                        R.layout.fragment_pending_orders,
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
                        OrderAdapter.ViewType.PENDING
                );

        recyclerView.setAdapter(adapter);

        ordersDbRef =
                FirebaseDatabase.getInstance()
                        .getReference("orders");

        loadItems();

        return view;
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {

        super.onViewCreated(
                view,
                savedInstanceState
        );

        loadPendingOrders();
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();

        if (ordersDbRef != null
                && pendingOrdersListener != null) {

            ordersDbRef.removeEventListener(
                    pendingOrdersListener
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

    private void loadPendingOrders() {

        pendingOrdersListener =
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot
                    ) {

                        if (getContext() == null) return;

                        orderList.clear();
                        orderKeys.clear();

                        int currentCount = 0;

                        for (DataSnapshot s :
                                snapshot.getChildren()) {

                            Order order =
                                    s.getValue(
                                            Order.class
                                    );

                            if (order != null) {

                                orderList.add(order);

                                orderKeys.add(
                                        s.getKey()
                                );

                                currentCount++;
                            }
                        }

                        if (currentCount > lastOrderCount) {

                            MediaPlayer mediaPlayer =
                                    MediaPlayer.create(
                                            getContext(),
                                            R.raw.nuevo
                                    );

                            if (mediaPlayer != null) {

                                mediaPlayer.start();

                                mediaPlayer.setOnCompletionListener(
                                        MediaPlayer::release
                                );
                            }
                        }

                        lastOrderCount = currentCount;

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

        ordersDbRef
                .orderByChild("status")
                .equalTo("pending")
                .addValueEventListener(
                        pendingOrdersListener
                );
    }

    @Override
    public void onAction(Order order) {

        acceptOrder(order);
    }

    private void acceptOrder(Order order) {

        if (currentCookId == null
                || getContext() == null) {

            return;
        }

        int index =
                orderList.indexOf(order);

        if (index == -1) return;

        String orderKey =
                orderKeys.get(index);

        Map<String, Object> updates =
                new HashMap<>();

        updates.put("status", "accepted");
        updates.put("cookId", currentCookId);

        FirebaseDatabase.getInstance()
                .getReference("orders")
                .child(orderKey)
                .updateChildren(updates)

                .addOnSuccessListener(aVoid -> {

                    NotificationHelper.showNotification(
                            getContext(),
                            "Pedido aceptado",
                            "Pedido "
                                    + order.getOrderCode()
                                    + " aceptado"
                    );
                });
    }
}