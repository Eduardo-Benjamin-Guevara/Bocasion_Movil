package com.proyecto.capstone.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.proyecto.capstone.R;
import com.proyecto.capstone.models.User;
import java.util.Date;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailEdit, usernameEdit, passwordEdit, confirmPasswordEdit;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();

        emailEdit = findViewById(R.id.register_email_edit);
        usernameEdit = findViewById(R.id.register_username_edit);
        passwordEdit = findViewById(R.id.register_password_edit);
        confirmPasswordEdit = findViewById(R.id.register_confirm_password_edit);
        registerButton = findViewById(R.id.register_button);

        registerButton.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String email = emailEdit.getText().toString().trim();
        String username = usernameEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();
        String confirmPassword = confirmPasswordEdit.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToDatabase(user.getUid(), email, username, password);
                            user.sendEmailVerification()
                                    .addOnCompleteListener(verificationTask -> {
                                        if (verificationTask.isSuccessful()) {
                                            Toast.makeText(RegisterActivity.this, "Registro exitoso. Se ha enviado un correo de verificación.", Toast.LENGTH_LONG).show();
                                            mAuth.signOut();
                                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                            finish();
                                        } else {
                                            Toast.makeText(RegisterActivity.this, "Error al enviar verificación: " + verificationTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            user.delete();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registro fallido: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToDatabase(String uid, String email, String username, String password) {
        User user = new User(uid, email, username, "usuario", new Date(), password);

        db.child("users").child(uid).setValue(user)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar datos de usuario en DB: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}