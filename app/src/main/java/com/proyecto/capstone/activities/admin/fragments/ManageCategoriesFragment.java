package com.proyecto.capstone.activities.admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.proyecto.capstone.R;
import com.proyecto.capstone.models.Category;

public class ManageCategoriesFragment extends Fragment {
    private EditText nameEdit, descriptionEdit;
    private Button addButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_categories, container, false);

        nameEdit = view.findViewById(R.id.category_name_edit);
        descriptionEdit = view.findViewById(R.id.category_description_edit);
        addButton = view.findViewById(R.id.add_category_button);

        addButton.setOnClickListener(v -> addCategory());
        return view;
    }

    private void addCategory() {
        String name = nameEdit.getText().toString().trim();
        String description = descriptionEdit.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(getContext(), "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference db = FirebaseDatabase.getInstance().getReference("categories").push();
        String categoryId = db.getKey();
        Category category = new Category(categoryId, name, description);

        db.setValue(category)
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Categoría " + name + " añadida con éxito.", Toast.LENGTH_SHORT).show();
                        nameEdit.setText("");
                        descriptionEdit.setText("");
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error al añadir categoría: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}