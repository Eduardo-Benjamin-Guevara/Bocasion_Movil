package com.proyecto.capstone.activities.user.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import com.proyecto.capstone.R;
import com.proyecto.capstone.models.Order;
import com.proyecto.capstone.models.Review;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class OrderStatusFragment extends Fragment {

    private TextView statusText;
    private TextView cookText;
    private TextView typeText;
    private TextView priceText;
    private TextView timeText;
    private TextView orderCodeText;
    private TextView messageText;
    private TextView reviewStatusText;
    private TextView navigationText;

    private MaterialButton reviewButton;
    private MaterialButton cancelButton;

    private ImageButton btnPrev;
    private ImageButton btnNext;

    private String userId;

    private String activeOrderKey;

    private DatabaseReference dbRef;

    private ValueEventListener listener;

    private final ArrayList<Order> orders =
            new ArrayList<>();

    private final ArrayList<String> orderKeys =
            new ArrayList<>();

    private int currentIndex = 0;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view =
                inflater.inflate(
                        R.layout.fragment_order_status,
                        container,
                        false
                );

        if (FirebaseAuth.getInstance()
                .getCurrentUser() == null) {

            return view;
        }

        userId =
                FirebaseAuth.getInstance()
                        .getCurrentUser()
                        .getUid();

        statusText =
                view.findViewById(
                        R.id.order_status_text
                );

        cookText =
                view.findViewById(
                        R.id.order_cook_text
                );

        typeText =
                view.findViewById(
                        R.id.order_type_text
                );

        priceText =
                view.findViewById(
                        R.id.order_price_text
                );

        timeText =
                view.findViewById(
                        R.id.order_time_text
                );

        orderCodeText =
                view.findViewById(
                        R.id.order_code_text
                );

        messageText =
                view.findViewById(
                        R.id.order_message_text
                );

        reviewStatusText =
                view.findViewById(
                        R.id.order_review_status_text
                );

        navigationText =
                view.findViewById(
                        R.id.order_navigation_text
                );

        reviewButton =
                view.findViewById(
                        R.id.btn_review_order
                );

        cancelButton =
                view.findViewById(
                        R.id.btn_cancel_order
                );

        btnPrev =
                view.findViewById(
                        R.id.btn_prev_order
                );

        btnNext =
                view.findViewById(
                        R.id.btn_next_order
                );

        reviewButton.setOnClickListener(
                v -> openReview()
        );

        cancelButton.setOnClickListener(
                v -> cancelOrder()
        );

        btnPrev.setOnClickListener(v -> {

            if (orders.isEmpty()) return;

            currentIndex--;

            if (currentIndex < 0) {

                currentIndex =
                        orders.size() - 1;
            }

            activeOrderKey =
                    orderKeys.get(currentIndex);

            show(
                    orders.get(currentIndex)
            );
        });

        btnNext.setOnClickListener(v -> {

            if (orders.isEmpty()) return;

            currentIndex++;

            if (currentIndex >= orders.size()) {

                currentIndex = 0;
            }

            activeOrderKey =
                    orderKeys.get(currentIndex);

            show(
                    orders.get(currentIndex)
            );
        });

        loadOrders();

        return view;
    }

    private void loadOrders() {

        dbRef =
                FirebaseDatabase.getInstance()
                        .getReference("orders");

        listener =
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot
                    ) {

                        orders.clear();
                        orderKeys.clear();

                        for (DataSnapshot s :
                                snapshot.getChildren()) {

                            Order order =
                                    s.getValue(
                                            Order.class
                                    );

                            if (order != null
                                    &&
                                    userId.equals(
                                            order.getUserId()
                                    )
                                    &&
                                    !"completed".equals(
                                            order.getStatus()
                                    )
                                    &&
                                    !"cancelled".equals(
                                            order.getStatus()
                                    )) {

                                orders.add(order);

                                orderKeys.add(
                                        s.getKey()
                                );
                            }
                        }

                        if (orders.isEmpty()) {

                            clear();

                            return;
                        }

                        if (currentIndex >= orders.size()) {

                            currentIndex = 0;
                        }

                        activeOrderKey =
                                orderKeys.get(currentIndex);

                        show(
                                orders.get(currentIndex)
                        );
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error
                    ) {
                    }
                };

        dbRef.addValueEventListener(listener);
    }

    private void show(Order order) {

        if (currentIndex < orderKeys.size()) {

            activeOrderKey =
                    orderKeys.get(currentIndex);
        }

        messageText.setVisibility(
                View.GONE
        );

        navigationText.setText(
                (currentIndex + 1)
                        + " / "
                        + orders.size()
        );

        statusText.setText(
                "Estado: "
                        + order.getStatus()
        );

        typeText.setText(
                "Pago: "
                        + order.getPaymentMethod()
        );

        priceText.setText(
                "Total: S/ "
                        + order.getTotalPrice()
        );

        orderCodeText.setText(
                "Código: "
                        + order.getOrderCode()
        );

        if (order.getCookId() != null) {

            FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(order.getCookId())

                    .addListenerForSingleValueEvent(
                            new ValueEventListener() {

                                @Override
                                public void onDataChange(
                                        @NonNull DataSnapshot snapshot
                                ) {

                                    String cookName =
                                            snapshot.child("name")
                                                    .getValue(
                                                            String.class
                                                    );

                                    if (cookName != null) {

                                        cookText.setText(
                                                "Cocinero: "
                                                        + cookName
                                        );

                                    } else {

                                        cookText.setText(
                                                "Cocinero asignado"
                                        );
                                    }
                                }

                                @Override
                                public void onCancelled(
                                        @NonNull DatabaseError error
                                ) {
                                }
                            });

        } else {

            cookText.setText(
                    "Esperando cocinero..."
            );
        }

        if (order.getScheduledTime() != null) {

            Date date =
                    new Date(
                            order.getScheduledTime()
                    );

            timeText.setText(
                    "Hora programada: "
                            + new SimpleDateFormat(
                            "HH:mm",
                            Locale.getDefault()
                    ).format(date)
            );

        } else {

            timeText.setText(
                    "Pedido inmediato"
            );
        }

        boolean waitingReview =
                Boolean.TRUE.equals(
                        order.getWaitingReview()
                );

        boolean reviewed =
                Boolean.TRUE.equals(
                        order.getReviewed()
                );

        boolean ready =
                "ready".equals(
                        order.getStatus()
                );

        boolean hasCook =
                order.getCookId() != null;

        if (waitingReview
                && ready
                && hasCook
                && !reviewed) {

            reviewStatusText.setVisibility(
                    View.VISIBLE
            );

            reviewButton.setVisibility(
                    View.VISIBLE
            );

        } else {

            reviewStatusText.setVisibility(
                    View.GONE
            );

            reviewButton.setVisibility(
                    View.GONE
            );
        }

        if ("pending".equals(
                order.getStatus()
        )) {

            cancelButton.setVisibility(
                    View.VISIBLE
            );

        } else {

            cancelButton.setVisibility(
                    View.GONE
            );
        }
    }

    private void cancelOrder() {

        if (activeOrderKey == null) return;

        if (orders.isEmpty()) return;

        Order order =
                orders.get(currentIndex);

        if (!"pending".equals(
                order.getStatus()
        )) {

            Toast.makeText(
                    getContext(),
                    "Ya fue aceptado por cocina",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        cancelButton.setEnabled(false);

        for (Order.OrderItem item :
                order.getItems()) {

            FirebaseDatabase.getInstance()
                    .getReference("items")
                    .child(item.getItemId())
                    .child("stock")

                    .runTransaction(
                            new Transaction.Handler() {

                                @NonNull
                                @Override
                                public Transaction.Result doTransaction(
                                        @NonNull MutableData currentData
                                ) {

                                    Integer stock =
                                            currentData.getValue(
                                                    Integer.class
                                            );

                                    if (stock == null) {

                                        stock = 0;
                                    }

                                    stock =
                                            stock
                                                    + item.getQuantity();

                                    currentData.setValue(
                                            stock
                                    );

                                    return Transaction.success(
                                            currentData
                                    );
                                }

                                @Override
                                public void onComplete(
                                        DatabaseError error,
                                        boolean committed,
                                        DataSnapshot currentData
                                ) {
                                }
                            });
        }

        FirebaseDatabase.getInstance()
                .getReference("orders")
                .child(activeOrderKey)
                .child("status")
                .setValue("cancelled")

                .addOnSuccessListener(aVoid -> {

                    Toast.makeText(
                            getContext(),
                            "Pedido cancelado",
                            Toast.LENGTH_LONG
                    ).show();

                    cancelButton.setEnabled(true);
                })

                .addOnFailureListener(e -> {

                    cancelButton.setEnabled(true);
                });
    }

    private void openReview() {

        View dialogView =
                LayoutInflater.from(getContext())
                        .inflate(
                                R.layout.dialog_review,
                                null
                        );

        RatingBar ratingBar =
                dialogView.findViewById(
                        R.id.review_rating_bar
                );

        EditText commentEdit =
                dialogView.findViewById(
                        R.id.review_comment_edit
                );

        new AlertDialog.Builder(getContext())

                .setTitle(
                        "Calificar Pedido"
                )

                .setView(dialogView)

                .setPositiveButton(
                        "Enviar",
                        (dialog, which) -> {

                            int rating =
                                    (int) ratingBar.getRating();

                            String comment =
                                    commentEdit.getText()
                                            .toString()
                                            .trim();

                            saveReview(
                                    rating,
                                    comment
                            );
                        }
                )

                .setNegativeButton(
                        "Cancelar",
                        null
                )

                .show();
    }

    private void saveReview(int rating,
                            String comment) {

        DatabaseReference reviewDb =
                FirebaseDatabase.getInstance()
                        .getReference("reviews");

        Review review =
                new Review(
                        userId,
                        "",
                        activeOrderKey,
                        rating,
                        comment,
                        new Date()
                );

        reviewDb.push()
                .setValue(review);

        Map<String, Object> updates =
                new HashMap<>();

        updates.put(
                "status",
                "completed"
        );

        updates.put(
                "waitingReview",
                false
        );

        updates.put(
                "reviewed",
                true
        );

        FirebaseDatabase.getInstance()
                .getReference("orders")
                .child(activeOrderKey)
                .updateChildren(updates)

                .addOnSuccessListener(aVoid -> {

                    Toast.makeText(
                            getContext(),
                            "Pedido finalizado ⭐",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void clear() {

        activeOrderKey = null;

        statusText.setText(
                "Sin pedidos activos"
        );

        cookText.setText("");

        typeText.setText("");

        priceText.setText("");

        timeText.setText("");

        orderCodeText.setText("");

        navigationText.setText("");

        messageText.setText(
                "No tienes pedidos activos."
        );

        messageText.setVisibility(
                View.VISIBLE
        );

        reviewButton.setVisibility(
                View.GONE
        );

        cancelButton.setVisibility(
                View.GONE
        );

        reviewStatusText.setVisibility(
                View.GONE
        );
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();

        if (dbRef != null
                && listener != null) {

            dbRef.removeEventListener(
                    listener
            );
        }
    }
}