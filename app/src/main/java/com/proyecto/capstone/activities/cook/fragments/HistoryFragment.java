package com.proyecto.capstone.activities.cook.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.proyecto.capstone.R;
import com.proyecto.capstone.adapters.OrderAdapter;
import com.proyecto.capstone.models.Order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;

    private TextView emptyText;

    private OrderAdapter adapter;

    private final List<Order> orderList =
            new ArrayList<>();

    private final Map<String, String> itemIdToNameMap =
            new HashMap<>();

    private String cookId;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {

        View view =
                inflater.inflate(
                        R.layout.fragment_history,
                        container,
                        false
                );

        if (FirebaseAuth.getInstance()
                .getCurrentUser() == null) {

            return view;
        }

        cookId =
                FirebaseAuth.getInstance()
                        .getCurrentUser()
                        .getUid();

        recyclerView =
                view.findViewById(
                        R.id.recycler_history
                );

        emptyText =
                view.findViewById(
                        R.id.empty_history_text
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
                        null,
                        OrderAdapter.ViewType.HISTORY
                );

        recyclerView.setAdapter(adapter);

        loadItems();

        loadHistory();

        return view;
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

                                if (isAdded()) {

                                    adapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error
                            ) {
                            }
                        });
    }

    private void loadHistory() {

        FirebaseDatabase.getInstance()
                .getReference("orders")

                .addValueEventListener(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot
                            ) {

                                orderList.clear();

                                for (DataSnapshot s :
                                        snapshot.getChildren()) {

                                    Order order =
                                            s.getValue(
                                                    Order.class
                                            );

                                    if (order != null
                                            &&
                                            cookId.equals(
                                                    order.getCookId()
                                            )
                                            &&
                                            (
                                                    "completed".equals(
                                                            order.getStatus()
                                                    )
                                                            ||
                                                            "cancelled".equals(
                                                                    order.getStatus()
                                                            )
                                            )) {

                                        orderList.add(order);
                                    }
                                }

                                if (!isAdded()) return;

                                adapter.notifyDataSetChanged();

                                if (orderList.isEmpty()) {

                                    emptyText.setVisibility(
                                            View.VISIBLE
                                    );

                                    recyclerView.setVisibility(
                                            View.GONE
                                    );

                                } else {

                                    emptyText.setVisibility(
                                            View.GONE
                                    );

                                    recyclerView.setVisibility(
                                            View.VISIBLE
                                    );
                                }
                            }

                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error
                            ) {
                            }
                        });
    }
}