package com.proyecto.capstone.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.proyecto.capstone.R;
import com.proyecto.capstone.models.Order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderAdapter
        extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private final List<Order> orderList;

    private final Map<String, String> itemIdToNameMap;

    private final OnOrderActionListener listener;

    private final ViewType viewType;

    public enum ViewType {
        PENDING,
        PREPARING,
        HISTORY
    }

    public interface OnOrderActionListener {
        void onAction(Order order);
    }

    public OrderAdapter(
            List<Order> orderList,
            Map<String, String> itemIdToNameMap,
            OnOrderActionListener listener,
            ViewType viewType
    ) {

        this.orderList = orderList;
        this.itemIdToNameMap = itemIdToNameMap;
        this.listener = listener;
        this.viewType = viewType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(
                                R.layout.item_order,
                                parent,
                                false
                        );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        Order order = orderList.get(position);

        holder.userNameText.setText(
                "Pedido de: " + order.getUserName()
        );

        StringBuilder itemsBuilder =
                new StringBuilder();

        for (Order.OrderItem item :
                order.getItems()) {

            String itemName =
                    itemIdToNameMap.getOrDefault(
                            item.getItemId(),
                            "Producto"
                    );

            itemsBuilder.append(
                            item.getQuantity()
                    )
                    .append(" x ")
                    .append(itemName)
                    .append("\n");
        }

        holder.itemsText.setText(
                itemsBuilder.toString()
        );

        holder.paymentMethodText.setText(
                "Pago: " + order.getPaymentMethod()
        );

        holder.paymentStatusText.setText(
                order.isPaymentConfirmed()
                        ? "Pago confirmado"
                        : "Pago pendiente"
        );

        holder.paymentStatusText.setTextColor(
                order.isPaymentConfirmed()
                        ? Color.parseColor("#4CAF50")
                        : Color.parseColor("#F44336")
        );

        if (order.getScheduledTime() != null) {

            holder.scheduleText.setVisibility(
                    View.VISIBLE
            );

            holder.scheduleText.setText(
                    "Hora programada: "
                            + new SimpleDateFormat(
                            "HH:mm",
                            Locale.getDefault()
                    ).format(
                            new Date(
                                    order.getScheduledTime()
                            )
                    )
            );

        } else {

            holder.scheduleText.setVisibility(
                    View.GONE
            );
        }

        switch (viewType) {

            case PENDING:

                holder.actionButton.setVisibility(
                        View.VISIBLE
                );

                holder.confirmPaymentButton.setVisibility(
                        View.GONE
                );

                holder.waitingReviewText.setVisibility(
                        View.GONE
                );

                holder.actionButton.setText(
                        "Aceptar"
                );

                holder.actionButton.setOnClickListener(
                        v -> listener.onAction(order)
                );

                break;

            case PREPARING:

                holder.actionButton.setVisibility(
                        View.VISIBLE
                );

                holder.actionButton.setText(
                        "Listo"
                );

                holder.confirmPaymentButton.setVisibility(
                        order.isPaymentConfirmed()
                                ? View.GONE
                                : View.VISIBLE
                );

                holder.waitingReviewText.setVisibility(
                        Boolean.TRUE.equals(
                                order.getWaitingReview()
                        )
                                ? View.VISIBLE
                                : View.GONE
                );

                holder.confirmPaymentButton
                        .setOnClickListener(v -> {

                            FirebaseDatabase.getInstance()
                                    .getReference("orders")
                                    .orderByChild("orderCode")
                                    .equalTo(order.getOrderCode())
                                    .addListenerForSingleValueEvent(
                                            new ValueEventListener() {

                                                @Override
                                                public void onDataChange(
                                                        @NonNull DataSnapshot snapshot
                                                ) {

                                                    for (DataSnapshot s :
                                                            snapshot.getChildren()) {

                                                        s.getRef()
                                                                .child("paymentConfirmed")
                                                                .setValue(true);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(
                                                        @NonNull DatabaseError error
                                                ) {
                                                }
                                            });

                            holder.paymentStatusText.setText(
                                    "Pago confirmado"
                            );

                            holder.paymentStatusText.setTextColor(
                                    Color.parseColor("#4CAF50")
                            );

                            holder.confirmPaymentButton.setVisibility(
                                    View.GONE
                            );
                        });

                holder.actionButton.setOnClickListener(
                        v -> listener.onAction(order)
                );

                break;

            case HISTORY:
            default:

                holder.actionButton.setVisibility(
                        View.GONE
                );

                holder.confirmPaymentButton.setVisibility(
                        View.GONE
                );

                holder.waitingReviewText.setVisibility(
                        View.GONE
                );

                break;
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView userNameText;

        TextView itemsText;

        TextView paymentMethodText;

        TextView paymentStatusText;

        TextView scheduleText;

        TextView waitingReviewText;

        MaterialButton actionButton;

        MaterialButton confirmPaymentButton;

        public ViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            userNameText =
                    itemView.findViewById(
                            R.id.order_user_name
                    );

            itemsText =
                    itemView.findViewById(
                            R.id.order_items_list
                    );

            paymentMethodText =
                    itemView.findViewById(
                            R.id.payment_method_text
                    );

            paymentStatusText =
                    itemView.findViewById(
                            R.id.payment_status_text
                    );

            scheduleText =
                    itemView.findViewById(
                            R.id.order_schedule_text
                    );

            waitingReviewText =
                    itemView.findViewById(
                            R.id.order_waiting_review_text
                    );

            actionButton =
                    itemView.findViewById(
                            R.id.order_action_button
                    );

            confirmPaymentButton =
                    itemView.findViewById(
                            R.id.btn_confirm_payment
                    );
        }
    }
}