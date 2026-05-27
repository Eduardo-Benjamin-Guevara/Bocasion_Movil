package com.proyecto.capstone.activities.admin.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage; // Nuevo
import com.google.firebase.storage.StorageReference; // Nuevo
import com.proyecto.capstone.R;
import com.proyecto.capstone.models.Category;
import com.proyecto.capstone.models.Item;
import com.bumptech.glide.Glide;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ManageItemsFragment extends Fragment {

    private EditText nameEdit, descriptionEdit, priceEdit, stockEdit, imageUrlEdit;
    private AutoCompleteTextView categoryIdEdit;
    private Button addButton, selectImageButton;
    private ImageView itemImageView;

    private List<Category> categoryList = new ArrayList<>();
    private Uri imageUri = null;
    private static final int PICK_IMAGE_REQUEST = 1;

    // Referencia a Firebase Storage
    private StorageReference storageReference; // Nuevo

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_items, container, false);

        // Inicializar Firebase Storage
        storageReference = FirebaseStorage.getInstance().getReference("item_images"); // Carpeta en Storage

        nameEdit = view.findViewById(R.id.item_name_edit);
        descriptionEdit = view.findViewById(R.id.item_description_edit);
        priceEdit = view.findViewById(R.id.item_price_edit);
        stockEdit = view.findViewById(R.id.item_stock_edit);
        categoryIdEdit = view.findViewById(R.id.item_category_id_edit);
        imageUrlEdit = view.findViewById(R.id.item_image_url_edit);
        addButton = view.findViewById(R.id.add_item_button);
        selectImageButton = view.findViewById(R.id.select_image_button);
        itemImageView = view.findViewById(R.id.item_image_view);

        loadCategories();

        selectImageButton.setOnClickListener(v -> openFileChooser());
        addButton.setOnClickListener(v -> validateAndAddItem());

        return view;
    }

    private void loadCategories() {
        DatabaseReference dbCategories = FirebaseDatabase.getInstance().getReference("categories");
        dbCategories.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categoryList.clear();
                List<String> categoryNames = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Category category = postSnapshot.getValue(Category.class);
                    if (category != null) {
                        category.setId(postSnapshot.getKey());
                        categoryList.add(category);
                        categoryNames.add(category.getName());
                    }
                }
                if (getContext() != null) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, categoryNames);
                    categoryIdEdit.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error al cargar categorías: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            if (getContext() != null) {
                Glide.with(getContext()).load(imageUri).into(itemImageView);
                // Muestra el nombre del archivo o un indicador de que se seleccionó
                imageUrlEdit.setText(imageUri.getLastPathSegment() != null ? imageUri.getLastPathSegment() : "Imagen Seleccionada");
                imageUrlEdit.setError(null);
            }
        }
    }

    private String getCategoryIdByName(String categoryName) {
        for (Category category : categoryList) {
            if (category.getName().equalsIgnoreCase(categoryName)) {
                return category.getId();
            }
        }
        return null;
    }

    private void validateAndAddItem() {
        String name = nameEdit.getText().toString().trim();
        String description = descriptionEdit.getText().toString().trim();
        String priceStr = priceEdit.getText().toString().trim();
        String stockStr = stockEdit.getText().toString().trim();
        String categoryName = categoryIdEdit.getText().toString().trim();
        String urlString = imageUrlEdit.getText().toString().trim();
        String categoryId = getCategoryIdByName(categoryName);

        if (name.isEmpty() || description.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty() || categoryId == null || categoryId.isEmpty()) {
            // Validación de campos (simplificada)
            if (name.isEmpty()) nameEdit.setError("Nombre requerido");
            if (description.isEmpty()) descriptionEdit.setError("Descripción requerida");
            if (priceStr.isEmpty()) priceEdit.setError("Precio requerido");
            if (stockStr.isEmpty()) stockEdit.setError("Stock requerido");
            if (categoryId == null || categoryId.isEmpty()) categoryIdEdit.setError("Categoría válida requerida");
            return;
        }

        double price = Double.parseDouble(priceStr);
        int stock = Integer.parseInt(stockStr);

        if (imageUri != null) {
            // Si se seleccionó una imagen de la galería, la subimos a Storage
            uploadImageAndSaveItem(name, description, price, stock, categoryId);
        } else if (!urlString.isEmpty()) {
            // Si no hay imagen de galería, usa la URL/ruta de texto (opción remota/manual)
            saveItemToDatabase(name, description, price, stock, categoryId, urlString);
        } else {
            // Si no hay ninguna, se guarda sin imagen
            saveItemToDatabase(name, description, price, stock, categoryId, null);
        }
    }

    private void uploadImageAndSaveItem(String name, String description, double price, int stock, String categoryId) {
        if (imageUri != null) {
            if (getContext() != null) Toast.makeText(getContext(), "Subiendo imagen...", Toast.LENGTH_SHORT).show();

            // Referencia de archivo única en Firebase Storage
            StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "_" + imageUri.getLastPathSegment());

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                // Obtener la URL de descarga y guardar el ítem
                                String imageUrl = uri.toString();
                                saveItemToDatabase(name, description, price, stock, categoryId, imageUrl);
                            })
                            .addOnFailureListener(e -> {
                                if (getContext() != null) Toast.makeText(getContext(), "Error al obtener URL de descarga: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }))
                    .addOnFailureListener(e -> {
                        if (getContext() != null) Toast.makeText(getContext(), "Error al subir la imagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void saveItemToDatabase(String name, String description, double price, int stock, String categoryId, String imageUrl) {
        Item item = new Item(name, description, price, stock, categoryId, true, imageUrl);
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("items").push();
        db.setValue(item)
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Ítem añadido con éxito.", Toast.LENGTH_LONG).show();
                        // Limpiar campos y variables de estado
                        nameEdit.setText("");
                        descriptionEdit.setText("");
                        priceEdit.setText("");
                        stockEdit.setText("");
                        categoryIdEdit.setText("");
                        imageUrlEdit.setText("");
                        imageUri = null;
                        itemImageView.setImageDrawable(null);
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error al añadir ítem a la base de datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}