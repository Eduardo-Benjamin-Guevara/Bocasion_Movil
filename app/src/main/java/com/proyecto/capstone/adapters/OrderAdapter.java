package com.proyecto.capstone.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.proyecto.capstone.R;
import com.proyecto.capstone.models.Order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

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
        void onConfirmPayment(Order order);
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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.userNameText.setText(order.getUserName() != null ? order.getUserName() : "Usuario");
        holder.userEmailText.setText(order.getUserEmail() != null ? order.getUserEmail() : "Sin correo");

        StringBuilder itemsBuilder = new StringBuilder();
        if (order.getItems() != null) {
            for (Order.OrderItem item : order.getItems()) {
                String name = itemIdToNameMap.get(item.getItemId());
                if (name == null) name = "Producto";
                itemsBuilder.append("- ").append(name).append(" (x").append(item.getQuantity()).append(")\n");
            }
        }
        holder.itemsText.setText(itemsBuilder.toString().trim());

        holder.paymentMethodText.setText("Método: " + order.getPaymentMethod());
        holder.paymentStatusText.setText("Pago: " + (order.isPaymentConfirmed() ? "Confirmado" : "Pendiente"));
        holder.statusText.setText("Estado: " + order.getStatus());

        if (order.getScheduledTime() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            holder.scheduleText.setText("Hora de Entrega: " + sdf.format(new Date(order.getScheduledTime())));
            holder.scheduleText.setVisibility(View.VISIBLE);
        } else {
            holder.scheduleText.setVisibility(View.GONE);
        }

        boolean isTarjeta = order.getPaymentMethod() != null && order.getPaymentMethod().toLowerCase().contains("tarjeta");

        if (viewType == ViewType.PENDING) {
            holder.actionButton.setText("Aceptar");
            holder.actionButton.setVisibility(View.VISIBLE);
            holder.actionButton.setOnClickListener(v -> {
                if (listener != null) listener.onAction(order);
            });
            holder.confirmPaymentButton.setVisibility(View.GONE);

        } else if (viewType == ViewType.PREPARING) {
            holder.actionButton.setText("Listo");
            holder.actionButton.setVisibility(View.VISIBLE);
            holder.actionButton.setOnClickListener(v -> {
                if (listener != null) listener.onAction(order);
            });

            if (!order.isPaymentConfirmed() && isTarjeta) {
                holder.confirmPaymentButton.setVisibility(View.VISIBLE);
                holder.confirmPaymentButton.setOnClickListener(v -> {
                    if (listener != null) listener.onConfirmPayment(order);
                });
            } else {
                holder.confirmPaymentButton.setVisibility(View.GONE);
            }

        } else {
            holder.actionButton.setVisibility(View.GONE);
            holder.confirmPaymentButton.setVisibility(View.GONE);

            if (Boolean.TRUE.equals(order.getWaitingReview()) && !Boolean.TRUE.equals(order.getReviewed())) {
                holder.waitingReviewText.setVisibility(View.VISIBLE);
            } else {
                holder.waitingReviewText.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userNameText, userEmailText, itemsText;
        TextView paymentMethodText, paymentStatusText, statusText, scheduleText, waitingReviewText;
        MaterialButton actionButton, confirmPaymentButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameText = itemView.findViewById(R.id.order_user_name);
            userEmailText = itemView.findViewById(R.id.order_user_email);
            itemsText = itemView.findViewById(R.id.order_items_list);
            paymentMethodText = itemView.findViewById(R.id.payment_method_text);
            paymentStatusText = itemView.findViewById(R.id.payment_status_text);
            statusText = itemView.findViewById(R.id.order_status_text);
            scheduleText = itemView.findViewById(R.id.order_schedule_text);
            waitingReviewText = itemView.findViewById(R.id.order_waiting_review_text);
            actionButton = itemView.findViewById(R.id.order_action_button);
            confirmPaymentButton = itemView.findViewById(R.id.btn_confirm_payment);
        }
    }
}
