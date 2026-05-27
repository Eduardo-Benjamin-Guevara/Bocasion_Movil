package com.proyecto.capstone.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.proyecto.capstone.R;
import com.proyecto.capstone.activities.admin.AdminActivity;
import com.proyecto.capstone.activities.cook.CookActivity;
import com.proyecto.capstone.activities.user.UserActivity;
import com.proyecto.capstone.models.User;
import com.proyecto.capstone.utils.NotificationHelper;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference db;
    private EditText emailEdit, passwordEdit;
    private TextView registerLink;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        NotificationHelper.createNotificationChannel(this);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();

        emailEdit = findViewById(R.id.login_email_edit);
        passwordEdit = findViewById(R.id.login_password_edit);
        Button loginButton = findViewById(R.id.login_button);
        registerLink = findViewById(R.id.register_button);

        loginButton.setOnClickListener(v -> attemptLogin());
        registerLink.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        String email = emailEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, completa ambos campos", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            if (user.isEmailVerified()) {
                                checkUserRoleAndRedirect(user.getUid());
                            } else {
                                Toast.makeText(LoginActivity.this, "Por favor, verifica tu correo electrónico para iniciar sesión.", Toast.LENGTH_LONG).show();
                                mAuth.signOut();
                            }
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Fallo de autenticación: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserRoleAndRedirect(String uid) {
        db.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null && user.getRole() != null) {
                    redirectBasedOnRole(user.getRole());
                } else {
                    Toast.makeText(LoginActivity.this, "Error al obtener rol", Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(LoginActivity.this, "Error de conexión", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void redirectBasedOnRole(String role) {

        if ("usuario".equals(role)) {

            java.util.Calendar calendar =
                    java.util.Calendar.getInstance();

            int hour =
                    calendar.get(
                            java.util.Calendar.HOUR_OF_DAY
                    );

            int minute =
                    calendar.get(
                            java.util.Calendar.MINUTE
                    );

            int currentMinutes =
                    (hour * 60) + minute;

            int closeTime =
                    (21 * 60) + 40;

            int openTime =
                    (8 * 60) + 30;

            boolean cafeteriaClosed =
                    currentMinutes >= closeTime
                            || currentMinutes < openTime;

            if (cafeteriaClosed) {

                showClosedDialog();

                return;
            }
        }

        Intent intent;

        if ("admin".equals(role)) {

            intent =
                    new Intent(
                            this,
                            AdminActivity.class
                    );

        } else if ("cocinero".equals(role)) {

            intent =
                    new Intent(
                            this,
                            CookActivity.class
                    );

        } else {

            intent =
                    new Intent(
                            this,
                            UserActivity.class
                    );
        }

        startActivity(intent);

        finish();
    }

    private void showClosedDialog() {

        android.app.AlertDialog.Builder builder =
                new android.app.AlertDialog.Builder(
                        this
                );

        android.view.View view =
                getLayoutInflater().inflate(
                        R.layout.dialog_cafeteria_cerrada,
                        null
                );

        builder.setView(view);

        builder.setPositiveButton(
                "Aceptar",
                (dialog, which) -> {

                    FirebaseAuth.getInstance()
                            .signOut();

                    dialog.dismiss();
                }
        );

        builder.setCancelable(false);

        builder.show();
    }
}