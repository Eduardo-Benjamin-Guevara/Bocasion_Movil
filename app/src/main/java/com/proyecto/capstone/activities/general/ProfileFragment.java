package com.proyecto.capstone.activities.general;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.proyecto.capstone.R;
import com.proyecto.capstone.activities.LoginActivity;
import com.proyecto.capstone.models.User;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private EditText emailEdit, nameEdit, currentPassEdit, newPassEdit;
    private Button logoutButton, saveButton;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private FirebaseUser firebaseUser;
    private String currentUid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("users");
        firebaseUser = mAuth.getCurrentUser();

        emailEdit = view.findViewById(R.id.profile_email_edit);
        nameEdit = view.findViewById(R.id.profile_username_edit);
        currentPassEdit = view.findViewById(R.id.profile_current_password);
        newPassEdit = view.findViewById(R.id.profile_new_password);

        logoutButton = view.findViewById(R.id.profile_logout_button);
        saveButton = view.findViewById(R.id.profile_save_button);

        emailEdit.setEnabled(false);

        if (firebaseUser != null) {
            currentUid = firebaseUser.getUid();
            loadUserData(currentUid);
        } else {
            Toast.makeText(getContext(), "Error: Usuario no autenticado.", Toast.LENGTH_SHORT).show();
            performLogout();
        }

        logoutButton.setOnClickListener(v -> performLogout());

        saveButton.setOnClickListener(v -> saveChanges());

        return view;
    }

    private void loadUserData(String uid) {
        dbRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    emailEdit.setText(user.getEmail());
                    nameEdit.setText(user.getName());
                } else {
                    Toast.makeText(getContext(), "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error al cargar datos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveChanges() {
        String name = nameEdit.getText().toString().trim();
        String currentPass = currentPassEdit.getText().toString().trim();
        String newPass = newPassEdit.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(getContext(), "El nombre no puede estar vacío.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Actualizar nombre primero
        updateName(name);

        // Si no quiere cambiar contraseña → FIN
        if (TextUtils.isEmpty(currentPass) && TextUtils.isEmpty(newPass)) {
            Toast.makeText(getContext(), "Datos guardados.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Si quiere cambiar contraseña → validar
        if (currentPass.isEmpty() || newPass.isEmpty()) {
            Toast.makeText(getContext(), "Para cambiar contraseña, llena ambos campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPass.length() < 6) {
            Toast.makeText(getContext(), "La nueva contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show();
            return;
        }

        updatePassword(currentPass, newPass);
    }

    private void updateName(String name) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);

        dbRef.child(currentUid).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {})
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al actualizar nombre: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
        
    private void updatePassword(String currentPass, String newPass) {
        AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), currentPass);

        firebaseUser.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {

                    firebaseUser.updatePassword(newPass)
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(getContext(), "Contraseña actualizada exitosamente.", Toast.LENGTH_SHORT).show();
                                currentPassEdit.setText("");
                                newPassEdit.setText("");
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Error al actualizar contraseña: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );

                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "La contraseña actual es incorrecta.", Toast.LENGTH_LONG).show()
                );
    }



    private void performLogout() {
        mAuth.signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) getActivity().finish();
    }
}
