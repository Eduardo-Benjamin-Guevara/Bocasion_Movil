package com.proyecto.capstone.activities.admin.fragments;

import android.os.Bundle;
import android.text.TextUtils; // Importar TextUtils
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText; // Para el campo de contraseña/nombre
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.proyecto.capstone.R;
import com.proyecto.capstone.adapters.UserAdapter;
import com.proyecto.capstone.models.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ManageUsersFragment extends Fragment implements UserAdapter.OnUserClickListener {
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList = new ArrayList<>();
    private List<String> userKeys = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_users, container, false);
        recyclerView = view.findViewById(R.id.recycler_users);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserAdapter(userList, this);
        recyclerView.setAdapter(adapter);

        loadUsers();

        return view;
    }

    private void loadUsers() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("users");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear();
                userKeys.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        userList.add(user);
                        userKeys.add(snapshot.getKey());
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error al cargar usuarios.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onUserClick(User user) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // Usar el nuevo XML de diálogo
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_user, null);

        EditText editName = view.findViewById(R.id.edit_user_name);
        EditText editPassword = view.findViewById(R.id.edit_user_password);
        Spinner roleSpinner = view.findViewById(R.id.edit_role_spinner); // ID ajustado al nuevo XML

        // 1. Pre-llenar el nombre actual
        editName.setText(user.getName());

        // 2. Configurar el Spinner de Roles
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.user_roles_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        // 3. Seleccionar el rol actual
        int currentRoleIndex = adapter.getPosition(user.getRole());
        roleSpinner.setSelection(currentRoleIndex);

        builder.setView(view);
        builder.setTitle("Editar Usuario: " + user.getEmail()); // Mostrar email, que es inmutable

        builder.setPositiveButton("Guardar", (dialog, id) -> {
            String newName = editName.getText().toString().trim();
            String newRole = roleSpinner.getSelectedItem().toString();
            String newPassword = editPassword.getText().toString(); // No trim() la contraseña

            // Solo actualiza si el nombre no está vacío
            if (!TextUtils.isEmpty(newName)) {
                updateUserData(user, newName, newRole, newPassword);
            } else {
                Toast.makeText(getContext(), "El nombre no puede estar vacío.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    // Renombrado de updateUserRole a updateUserData para reflejar que actualiza más campos
    private void updateUserData(User user, String newName, String newRole, String newPassword) {
        // Aseguramos que el fragmento siga adjunto antes de proceder
        if (getContext() == null) return;

        int index = userList.indexOf(user);
        if (index != -1) {
            String userKey = userKeys.get(index);
            DatabaseReference db = FirebaseDatabase.getInstance().getReference("users").child(userKey);

            Map<String, Object> updates = new HashMap<>();
            updates.put("name", newName);
            updates.put("role", newRole);

            // Solo si se proporciona una nueva contraseña (campo no vacío)
            if (!TextUtils.isEmpty(newPassword)) {
                updates.put("password", newPassword); // Asumiendo que guardas la contraseña en Firebase
            }

            db.updateChildren(updates)
                    .addOnSuccessListener(aVoid -> {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Datos de " + user.getName() + " actualizados.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error al actualizar datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            // Se agregó la comprobación de nulidad para evitar el crasheo
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error: Usuario no encontrado.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}