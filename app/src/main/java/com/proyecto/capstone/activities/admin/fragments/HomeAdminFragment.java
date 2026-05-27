package com.proyecto.capstone.activities.admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.proyecto.capstone.R; // Asegúrate de que este R.java esté disponible

public class HomeAdminFragment extends Fragment {

    private static final String ROLE_NAME = "Panel de Administrador";
    private static final String ROLE_DESCRIPTION = "Tu plataforma central para una gestión eficiente y completa del restaurante.";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflar el diseño del fragmento de inicio
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Configurar los textos específicos del rol
        TextView roleNameTextView = view.findViewById(R.id.tv_role_name);
        TextView roleDescriptionTextView = view.findViewById(R.id.tv_role_description);

        if (roleNameTextView != null) {
            roleNameTextView.setText(ROLE_NAME);
        }
        if (roleDescriptionTextView != null) {
            roleDescriptionTextView.setText(ROLE_DESCRIPTION);
        }

        return view;
    }
}