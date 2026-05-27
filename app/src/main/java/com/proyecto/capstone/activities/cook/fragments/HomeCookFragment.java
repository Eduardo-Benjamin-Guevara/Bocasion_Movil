package com.proyecto.capstone.activities.cook.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.proyecto.capstone.R;

public class HomeCookFragment extends Fragment {

    private static final String ROLE_NAME = "Área de Cocina";
    private static final String ROLE_DESCRIPTION = "Tu plataforma para recibir y gestionar los pedidos que necesitan ser preparados. ¡Manos a la obra!";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

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