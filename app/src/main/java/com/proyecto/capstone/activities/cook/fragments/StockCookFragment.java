package com.proyecto.capstone.activities.cook.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.proyecto.capstone.R;
import com.proyecto.capstone.adapters.StockAdapter;
import com.proyecto.capstone.models.Item;

import java.util.ArrayList;
import java.util.List;

public class StockCookFragment extends Fragment {

    private RecyclerView recyclerView;

    private StockAdapter adapter;

    private final List<Item> itemList =
            new ArrayList<>();

    private final List<String> itemKeys =
            new ArrayList<>();

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {

        View view =
                inflater.inflate(
                        R.layout.fragment_stock_cook,
                        container,
                        false
                );

        recyclerView =
                view.findViewById(
                        R.id.recycler_stock
                );

        recyclerView.setLayoutManager(
                new GridLayoutManager(
                        getContext(),
                        2
                )
        );

        adapter =
                new StockAdapter(
                        itemList,
                        itemKeys
                );

        recyclerView.setAdapter(adapter);

        loadItems();

        return view;
    }

    private void loadItems() {

        FirebaseDatabase.getInstance()
                .getReference("items")
                .addValueEventListener(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot
                            ) {

                                itemList.clear();
                                itemKeys.clear();

                                for (DataSnapshot s :
                                        snapshot.getChildren()) {

                                    Item item =
                                            s.getValue(
                                                    Item.class
                                            );

                                    if (item != null) {

                                        itemList.add(item);

                                        itemKeys.add(
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
                            }
                        });
    }
}