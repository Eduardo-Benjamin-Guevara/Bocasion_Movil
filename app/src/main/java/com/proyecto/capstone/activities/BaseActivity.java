package com.proyecto.capstone.activities;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Clase Base para implementar el timeout de sesión de 10 minutos.
 * Todas las actividades principales deben heredar de esta clase.
 */
public abstract class BaseActivity extends AppCompatActivity {

    // 10 minutos = 600,000 milisegundos
    private static final long LOGOUT_TIMEOUT_MS = 10 * 60 * 1000L;
    private final Handler sessionHandler = new Handler(Looper.getMainLooper());
    private final Runnable logoutRunnable = this::performLogout;

    @Override
    protected void onResume() {
        super.onResume();
        // Al regresar a la actividad (o al iniciar), iniciamos el temporizador.
        startLogoutTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Al salir de la actividad, detenemos el temporizador.
        stopLogoutTimer();
    }

    // Se puede implementar un método para refrescar el timer en caso de interacción.
    // Aunque con onResume/onPause ya cumple con "si la app está abierta".
    protected void resetLogoutTimer() {
        startLogoutTimer();
    }

    private void startLogoutTimer() {
        sessionHandler.removeCallbacks(logoutRunnable);
        sessionHandler.postDelayed(logoutRunnable, LOGOUT_TIMEOUT_MS);
    }

    private void stopLogoutTimer() {
        sessionHandler.removeCallbacks(logoutRunnable);
    }

    private void performLogout() {
        // Cierra la sesión de Firebase
        FirebaseAuth.getInstance().signOut();

        // Notifica al usuario
        Toast.makeText(this, "Sesión expirada por inactividad (10 minutos). Inicia sesión de nuevo.", Toast.LENGTH_LONG).show();

        // Redirige a LoginActivity y limpia el stack de actividades
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}