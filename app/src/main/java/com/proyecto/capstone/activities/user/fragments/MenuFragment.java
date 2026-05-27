package com.proyecto.capstone.activities.user.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.proyecto.capstone.R;
import com.proyecto.capstone.adapters.ItemAdapter;
import com.proyecto.capstone.models.Category;
import com.proyecto.capstone.models.Item;
import com.proyecto.capstone.utils.CartManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;

public class MenuFragment extends Fragment implements ItemAdapter.OnQuantityChangeListener, ItemAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private List<Item> itemsList = new ArrayList<>();
    private List<String> itemKeysOriginal = new ArrayList<>();
    private List<Item> filteredItemsList = new ArrayList<>();
    private List<String> filteredItemKeys = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();
    private Map<String, String> categoryIdToNameMap = new HashMap<>();
    private Map<String, String> categoryIdToNameMapReverse = new HashMap<>();
    private CartManager cartManager;
    private Spinner categorySpinner;
    private String selectedCategoryId = null;

    private Toast currentToast;

    private DatabaseReference itemsDbRef;
    private ValueEventListener itemsListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_menu, container, false);

        recyclerView = root.findViewById(R.id.recycler_menu);
        categorySpinner = root.findViewById(R.id.category_spinner);
        cartManager = CartManager.getInstance();

        setupRecyclerView();
        loadCategories();
        setupSpinnerListener();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (itemsDbRef != null && itemsListener != null) {
            itemsDbRef.removeEventListener(itemsListener);
        }
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);

        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position < filteredItemsList.size()) {
                    Item item = filteredItemsList.get(position);
                    boolean isHeader = item.getName() != null && item.getDescription() == null && item.getPrice() == 0.0;
                    return isHeader ? layoutManager.getSpanCount() : 1;
                }
                return 1;
            }
        });

        recyclerView.setLayoutManager(layoutManager);
        adapter = new ItemAdapter(filteredItemsList, filteredItemKeys, this, this, cartManager, true);
        recyclerView.setAdapter(adapter);
    }

    private void loadCategories() {
        DatabaseReference dbCategories = FirebaseDatabase.getInstance().getReference("categories");
        dbCategories.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!isAdded()) return;

                categoryList.clear();
                categoryIdToNameMap.clear();
                categoryIdToNameMapReverse.clear();
                List<String> categoryNames = new ArrayList<>();
                categoryNames.add("Todas las categorías");

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Category category = postSnapshot.getValue(Category.class);
                    if (category != null) {
                        String categoryId = postSnapshot.getKey();
                        category.setId(categoryId);
                        categoryList.add(category);
                        categoryIdToNameMap.put(category.getName(), categoryId);
                        categoryIdToNameMapReverse.put(categoryId, category.getName());
                        categoryNames.add(category.getName());
                    }
                }

                Collections.sort(categoryList, Comparator.comparing(Category::getName));
                Collections.sort(categoryNames.subList(1, categoryNames.size()));

                if (getContext() != null) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, categoryNames);
                    categorySpinner.setAdapter(adapter);
                    loadItems();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Error al cargar categorías: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadItems() {
        itemsDbRef = FirebaseDatabase.getInstance().getReference("items");
        itemsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!isAdded()) return;

                itemsList.clear();
                itemKeysOriginal.clear(); // Usar una lista para las keys originales
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Item item = postSnapshot.getValue(Item.class);
                    if (item != null) {
                        itemsList.add(item);
                        itemKeysOriginal.add(postSnapshot.getKey());
                    }
                }
                filterItems();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Error al cargar ítems: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };
        itemsDbRef.addValueEventListener(itemsListener);
    }

    private void setupSpinnerListener() {
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategoryName = (String) parent.getItemAtPosition(position);
                if ("Todas las categorías".equals(selectedCategoryName)) {
                    selectedCategoryId = null;
                } else {
                    selectedCategoryId = categoryIdToNameMap.get(selectedCategoryName);
                }
                filterItems();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategoryId = null;
                filterItems();
            }
        });
    }

    private void filterItems() {
        filteredItemsList.clear();
        filteredItemKeys.clear();

        if (selectedCategoryId == null) {
            Map<String, List<Item>> groupedItems = new HashMap<>();
            Map<String, List<String>> groupedItemKeys = new HashMap<>();

            for (int i = 0; i < itemsList.size(); i++) {
                Item item = itemsList.get(i);
                String itemKey = itemKeysOriginal.get(i);
                String categoryId = item.getCategoryId();

                if (categoryId != null && categoryIdToNameMapReverse.containsKey(categoryId)) {
                    groupedItems.computeIfAbsent(categoryId, k -> new ArrayList<>()).add(item);
                    groupedItemKeys.computeIfAbsent(categoryId, k -> new ArrayList<>()).add(itemKey);
                }
            }

            for (Category category : categoryList) {
                String categoryId = category.getId();
                List<Item> items = groupedItems.get(categoryId);
                List<String> keys = groupedItemKeys.get(categoryId);

                if (items != null && !items.isEmpty()) {
                    Item categoryHeader = new Item();
                    categoryHeader.setName(category.getName());
                    categoryHeader.setPrice(0.0);

                    // Añadir encabezado
                    filteredItemsList.add(categoryHeader);
                    filteredItemKeys.add(null); // ID nulo para el encabezado

                    // Añadir ítems reales
                    for (int i = 0; i < items.size(); i++) {
                        filteredItemsList.add(items.get(i));
                        filteredItemKeys.add(keys.get(i));
                    }
                }
            }

        } else {
            for (int i = 0; i < itemsList.size(); i++) {
                Item item = itemsList.get(i);

                if (selectedCategoryId.equals(item.getCategoryId())) {
                    filteredItemsList.add(item);
                    if (i < itemKeysOriginal.size()) {
                        filteredItemKeys.add(itemKeysOriginal.get(i));
                    } else {
                        filteredItemKeys.add(null);
                    }
                }
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onQuantityChange(String itemId, int newQuantity) {
        if (!isAdded() || cartManager == null || getContext() == null) return;

        // Se añade una comprobación nula para prevenir el error.
        if (itemId == null) {
            return;
        }

        cartManager.updateItemQuantity(itemId, newQuantity, (success, actualQuantityOrMaxStock) -> {
            if (!isAdded() || getContext() == null) return;

            String message;
            int duration = Toast.LENGTH_SHORT;

            if (success) {
                if (newQuantity == 0) {
                    message = "Ítem eliminado del carrito.";
                } else {
                    message = "Cantidad actualizada a " + actualQuantityOrMaxStock;
                }
            } else {
                if (actualQuantityOrMaxStock == 0) {
                    message = "El ítem está agotado.";
                } else {
                    message = "Stock máximo es " + actualQuantityOrMaxStock + ". No se puede añadir más.";
                    duration = Toast.LENGTH_LONG;
                }
            }

            if (currentToast != null) {
                currentToast.cancel();
            }

            currentToast = Toast.makeText(getContext(), message, duration);

            currentToast.show();

            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onItemClick(Item item) {
        if (!isAdded()) return;
        showDescriptionDialog(item);
    }

    private void showDescriptionDialog(Item item) {
        if (getContext() == null || item == null) return;

        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_item_description, null);

        ImageView imageView = view.findViewById(R.id.dialog_item_image);
        TextView nameText = view.findViewById(R.id.dialog_item_name);
        TextView priceText = view.findViewById(R.id.dialog_item_price);
        TextView descriptionText = view.findViewById(R.id.dialog_item_description);

        nameText.setText(item.getName());
        priceText.setText(String.format("S/ %.2f", item.getPrice()));

        String description = item.getDescription();

        if (description != null && !description.isEmpty()) {

            String[] ingredients = description.split(",");

            StringBuilder formatted = new StringBuilder();

            for (String ingredient : ingredients) {
                formatted.append("• ")
                        .append(ingredient.trim())
                        .append("\n");
            }

            descriptionText.setText(formatted.toString());

        } else {
            descriptionText.setText("Sin descripción");
        }

        String urlOrPath = item.getImageUrl();

        if (urlOrPath != null && !urlOrPath.isEmpty()) {

            Object imageSource;

            if (urlOrPath.startsWith("http") || urlOrPath.startsWith("https")) {
                imageSource = urlOrPath;
            } else {
                imageSource = new java.io.File(urlOrPath);
            }

            com.bumptech.glide.Glide.with(requireContext())
                    .load(imageSource)
                    .placeholder(R.drawable.restaurante)
                    .error(R.drawable.ic_placeholder)
                    .into(imageView);

        } else {
            imageView.setImageResource(R.drawable.restaurante);
        }

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(view)
                .create();

        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}