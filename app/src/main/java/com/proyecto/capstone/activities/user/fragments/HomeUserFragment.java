package com.proyecto.capstone.activities.user.fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.proyecto.capstone.R;
import com.proyecto.capstone.models.Order;

import java.util.Calendar;

public class HomeUserFragment extends Fragment {

    private static final String ROLE_NAME =
            "Explora nuestro delicioso menú";

    private static final String ROLE_DESCRIPTION =
            "Realiza tu pedido fácil y rápido y disfruta del mejor sabor.";

    private View statusCircle;

    private TextView statusText;

    private TextView roleNameTextView;

    private TextView roleDescriptionTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view =
                inflater.inflate(
                        R.layout.fragment_home,
                        container,
                        false
                );

        roleNameTextView =
                view.findViewById(
                        R.id.tv_role_name
                );

        roleDescriptionTextView =
                view.findViewById(
                        R.id.tv_role_description
                );

        statusCircle =
                view.findViewById(
                        R.id.status_circle
                );

        statusText =
                view.findViewById(
                        R.id.status_text
                );

        roleNameTextView.setText(
                ROLE_NAME
        );

        roleDescriptionTextView.setText(
                ROLE_DESCRIPTION
        );

        observeCafeStatus();

        return view;
    }

    private void observeCafeStatus() {

        FirebaseDatabase.getInstance()
                .getReference("orders")
                .addValueEventListener(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot
                            ) {

                                int activeOrders = 0;

                                for (DataSnapshot data :
                                        snapshot.getChildren()) {

                                    Order order =
                                            data.getValue(
                                                    Order.class
                                            );

                                    if (order != null) {

                                        String status =
                                                order.getStatus();

                                        if (
                                                "pending".equals(status)
                                                        ||
                                                        "accepted".equals(status)
                                                        ||
                                                        "preparing".equals(status)
                                        ) {

                                            activeOrders++;
                                        }
                                    }
                                }

                                updateCafeStatus(
                                        activeOrders
                                );
                            }

                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error
                            ) {
                            }
                        });
    }

    private void updateCafeStatus(
            int activeOrders
    ) {

        Calendar calendar =
                Calendar.getInstance();

        int hour =
                calendar.get(
                        Calendar.HOUR_OF_DAY
                );

        int minute =
                calendar.get(
                        Calendar.MINUTE
                );

        double currentHour =
                hour + (minute / 60.0);

        if (currentHour < 9
                || currentHour > 21.5) {

            setStatus(
                    Color.GRAY,
                    "Cafetería cerrada"
            );

            return;
        }

        boolean peakHour =
                (
                        currentHour >= 12
                                &&
                                currentHour <= 15
                )
                        ||
                        (
                                currentHour >= 18
                                        &&
                                        currentHour <= 21
                        );

        int greenLimit =
                peakHour ? 3 : 5;

        int yellowLimit =
                peakHour ? 7 : 10;

        if (activeOrders <= greenLimit) {

            setStatus(
                    Color.parseColor("#4CAF50"),
                    "🟢 Poca gente"
            );

            return;
        }

        if (activeOrders <= yellowLimit) {

            setStatus(
                    Color.parseColor("#FFC107"),
                    "🟡 Tráfico medio"
            );

            return;
        }

        setStatus(
                Color.parseColor("#F44336"),
                "🔴 Muy concurrido"
        );
    }

    private void setStatus(
            int color,
            String text
    ) {

        if (getContext() == null) return;

        statusCircle.setBackgroundTintList(
                ColorStateList.valueOf(
                        color
                )
        );

        statusText.setText(text);
    }
}