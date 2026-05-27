package com.proyecto.capstone.activities.admin.fragments;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.proyecto.capstone.R;
import com.proyecto.capstone.adapters.SalesAdapter;
import com.proyecto.capstone.models.Item;
import com.proyecto.capstone.models.Order;
import com.proyecto.capstone.models.Review;
import com.proyecto.capstone.models.Sales;
import com.proyecto.capstone.models.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class SalesFragment extends Fragment
        implements SalesAdapter.OnDetailsClickListener {

    private RecyclerView recyclerView;
    private SalesAdapter adapter;
    private final List<Sales> salesList = new ArrayList<>();

    private Spinner cookFilterSpinner;
    private final List<User> cookList = new ArrayList<>();
    private final Map<String, String> cookIdToNameMap = new HashMap<>();
    private final List<Order> allCompletedOrders = new ArrayList<>();
    private final Map<String, Item> itemMap = new HashMap<>();

    // Cambiamos el mapa clave para asociarlo mediante el Firebase Order ID único
    private final Map<String, Review> reviewMap = new HashMap<>();
    // Mantenemos también una lista de objetos Order extendidos o mapeados para conservar su clave de nodo de Firebase
    private final Map<Order, String> orderToKeyMap = new HashMap<>();

    private String selectedCookId = null;

    private TextView txtGeneralRating;
    private TextView txtGeneralStatus;
    private TextView txtWeeklyAvg;
    private TextView txtMonthlyAvg;
    private MaterialButton btnAnalytics;

    public static class SalesCountWithCook extends Sales {
        private final String cookId;
        private final String cookName;
        private final int orderCount;
        private final List<Order> completedOrders;

        public SalesCountWithCook(String cookId, String cookName, int orderCount, List<Order> completedOrders) {
            super(0.0);
            this.cookId = cookId;
            this.cookName = cookName;
            this.orderCount = orderCount;
            this.completedOrders = completedOrders;
        }

        public String getCookId() { return cookId; }
        public String getCookName() { return cookName; }
        public int getOrderCount() { return orderCount; }
        public List<Order> getCompletedOrders() { return completedOrders; }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sales, container, false);

        recyclerView = view.findViewById(R.id.recycler_sales);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SalesAdapter(salesList, this);
        recyclerView.setAdapter(adapter);

        cookFilterSpinner = view.findViewById(R.id.cook_filter_spinner);
        txtGeneralRating = view.findViewById(R.id.txt_general_rating);
        txtGeneralStatus = view.findViewById(R.id.txt_general_status);
        txtWeeklyAvg = view.findViewById(R.id.txt_weekly_avg);
        txtMonthlyAvg = view.findViewById(R.id.txt_monthly_avg);
        btnAnalytics = view.findViewById(R.id.btn_analytics);

        recyclerView.setHasFixedSize(true);

        loadCooks();
        loadItems();
        loadReviews(); // Carga las reseñas y encadena secuencialmente la carga de órdenes completadas

        btnAnalytics.setOnClickListener(v -> showAnalyticsDialog());

        cookFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedCookId = null;
                } else {
                    selectedCookId = cookList.get(position - 1).getUid();
                }
                updateSalesCount();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCookId = null;
                updateSalesCount();
            }
        });

        return view;
    }

    private void loadCooks() {
        FirebaseDatabase.getInstance().getReference("users")
                .orderByChild("role")
                .equalTo("cocinero")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!isAdded()) return;
                        cookList.clear();
                        cookIdToNameMap.clear();
                        List<String> names = new ArrayList<>();
                        names.add("Todos los Cocineros");

                        for (DataSnapshot data : snapshot.getChildren()) {
                            User user = data.getValue(User.class);
                            if (user != null) {
                                user.setUid(data.getKey());
                                cookList.add(user);
                                cookIdToNameMap.put(user.getUid(), user.getName());
                                names.add(user.getName());
                            }
                        }

                        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                                requireContext(),
                                android.R.layout.simple_spinner_dropdown_item,
                                names
                        );
                        cookFilterSpinner.setAdapter(spinnerAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loadItems() {
        FirebaseDatabase.getInstance().getReference("items")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        itemMap.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Item item = data.getValue(Item.class);
                            if (item != null) {
                                itemMap.put(data.getKey(), item);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadReviews() {
        FirebaseDatabase.getInstance().getReference("reviews")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        reviewMap.clear();
                        double total = 0;
                        int count = 0;
                        double weeklyTotal = 0;
                        int weeklyCount = 0;
                        double monthlyTotal = 0;
                        int monthlyCount = 0;
                        StringBuilder comments = new StringBuilder();

                        long now = System.currentTimeMillis();
                        long weekMillis = 7L * 24 * 60 * 60 * 1000;
                        long monthMillis = 30L * 24 * 60 * 60 * 1000;

                        for (DataSnapshot data : snapshot.getChildren()) {
                            Review review = data.getValue(Review.class);
                            if (review != null && review.getOrderCode() != null) {
                                // Dado que OrderStatusFragment pasa el ID único de Firebase de la orden en el campo orderCode,
                                // guardamos la reseña bajo ese ID limpio en mayúsculas y sin espacios.
                                String firebaseOrderId = review.getOrderCode().trim();
                                reviewMap.put(firebaseOrderId, review);

                                total += review.getRating();
                                count++;

                                if (review.getComment() != null) {
                                    comments.append(review.getComment().toLowerCase()).append(" ");
                                }

                                Date createdDate = review.getCreatedAt();
                                if (createdDate != null) {
                                    long diff = now - createdDate.getTime();
                                    if (diff <= weekMillis) {
                                        weeklyTotal += review.getRating();
                                        weeklyCount++;
                                    }
                                    if (diff <= monthMillis) {
                                        monthlyTotal += review.getRating();
                                        monthlyCount++;
                                    }
                                }
                            }
                        }

                        double avg = count == 0 ? 0 : total / count;
                        double weeklyAvg = weeklyCount == 0 ? 0 : weeklyTotal / weeklyCount;
                        double monthlyAvg = monthlyCount == 0 ? 0 : monthlyTotal / monthlyCount;

                        if (isAdded()) {
                            txtGeneralRating.setText(String.format(Locale.getDefault(), "⭐ %.1f", avg));
                            txtWeeklyAvg.setText(String.format(Locale.getDefault(), "⭐ %.1f", weeklyAvg));
                            txtMonthlyAvg.setText(String.format(Locale.getDefault(), "⭐ %.1f", monthlyAvg));

                            String analysis;
                            if (avg >= 4.5) analysis = "Excelente servicio y muy alta satisfacción.";
                            else if (avg >= 4) analysis = "Buen desempeño general de la cafetería.";
                            else if (avg >= 3) analysis = "Servicio regular con oportunidades de mejora.";
                            else analysis = "Se detectaron múltiples clientes insatisfechos.";

                            String text = comments.toString();
                            if (text.contains("rico")) analysis += "\n✔ Los clientes destacan la comida.";
                            if (text.contains("rápido")) analysis += "\n✔ Buen tiempo de atención.";
                            if (text.contains("demora")) analysis += "\n⚠ Hay reportes de demora.";
                            if (text.contains("frío")) analysis += "\n⚠ Algunos productos llegan fríos.";

                            txtGeneralStatus.setText(analysis);
                        }

                        // Sincronización asíncrona correcta: cargamos pedidos una vez aseguradas las reseñas
                        loadAllCompletedOrders();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadAllCompletedOrders() {
        FirebaseDatabase.getInstance().getReference("orders")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allCompletedOrders.clear();
                        orderToKeyMap.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Order order = data.getValue(Order.class);
                            if (order != null && order.getCookId() != null && !order.getCookId().isEmpty()
                                    && ("completed".equals(order.getStatus()) || "ready".equals(order.getStatus()))) {
                                allCompletedOrders.add(order);
                                // Conservamos la Key del nodo de Firebase vinculada al objeto Order
                                orderToKeyMap.put(order, data.getKey());
                            }
                        }
                        updateSalesCount();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void updateSalesCount() {
        if (!isAdded()) return;

        Map<String, List<Order>> grouped = allCompletedOrders.stream()
                .collect(Collectors.groupingBy(Order::getCookId));

        List<Sales> temp = new ArrayList<>();
        for (Map.Entry<String, List<Order>> entry : grouped.entrySet()) {
            String cookId = entry.getKey();
            List<Order> orders = entry.getValue();

            if (selectedCookId == null || selectedCookId.equals(cookId)) {
                temp.add(new SalesCountWithCook(
                        cookId,
                        cookIdToNameMap.getOrDefault(cookId, "Desconocido"),
                        orders.size(),
                        orders
                ));
            }
        }

        Collections.sort(temp, (a, b) -> Integer.compare(
                ((SalesCountWithCook) b).getOrderCount(),
                ((SalesCountWithCook) a).getOrderCount()
        ));

        salesList.clear();
        salesList.addAll(temp);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDetailsClick(Sales salesItem) {
        if (!(salesItem instanceof SalesCountWithCook)) return;

        SalesCountWithCook cookSales = (SalesCountWithCook) salesItem;
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_sales_details, null);

        TextView title = dialogView.findViewById(R.id.dialog_title);
        TextView subtitle = dialogView.findViewById(R.id.dialog_subtitle);
        TextView txtDialogAvg = dialogView.findViewById(R.id.txt_dialog_avg);
        TextView txtDialogAnalysis = dialogView.findViewById(R.id.txt_dialog_analysis);
        LinearLayout container = dialogView.findViewById(R.id.container_orders);
        MaterialButton btnClose = dialogView.findViewById(R.id.btn_close);

        title.setText(cookSales.getCookName());
        subtitle.setText(cookSales.getOrderCount() + " pedidos realizados");

        double totalStars = 0;
        int reviewsCount = 0;
        int positive = 0;
        int negative = 0;

        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (Order order : cookSales.getCompletedOrders()) {
            // Inflamos la tarjeta usando el diseño limpio XML item_dialog_order_card.xml
            View cardView = inflater.inflate(R.layout.item_dialog_order_card, container, false);

            TextView txtUser = cardView.findViewById(R.id.card_txt_user);
            TextView txtCode = cardView.findViewById(R.id.card_txt_code);
            TextView txtItems = cardView.findViewById(R.id.card_txt_items);
            TextView txtReview = cardView.findViewById(R.id.card_txt_review);
            TextView txtTotal = cardView.findViewById(R.id.card_txt_total);

            txtUser.setText("👤 " + order.getUserName());
            txtCode.setText("🧾 Pedido: #" + order.getOrderCode());

            StringBuilder itemsBuilder = new StringBuilder();
            if (order.getItems() != null) {
                for (Order.OrderItem item : order.getItems()) {
                    String itemName = itemMap.containsKey(item.getItemId())
                            ? itemMap.get(item.getItemId()).getName()
                            : "Producto";
                    itemsBuilder.append("• ").append(item.getQuantity()).append(" x ").append(itemName).append("\n");
                }
            }
            txtItems.setText(itemsBuilder.toString());

            // SOLUCIÓN CLAVE: Recuperamos el ID de Firebase real con el que se guardó la reseña
            String firebaseOrderKey = orderToKeyMap.get(order);
            Review review = null;
            if (firebaseOrderKey != null) {
                review = reviewMap.get(firebaseOrderKey.trim());
            }

            if (review != null) {
                StringBuilder stars = new StringBuilder();
                for (int i = 0; i < review.getRating(); i++) {
                    stars.append("⭐");
                }

                totalStars += review.getRating();
                reviewsCount++;

                if (review.getRating() >= 4) {
                    positive++;
                } else {
                    negative++;
                }

                String commentText = (review.getComment() != null && !review.getComment().trim().isEmpty())
                        ? review.getComment()
                        : "Sin comentario escrito.";

                txtReview.setText("⭐ CALIFICACIÓN: " + stars + " (" + review.getRating() + "/5)\n💬 " + commentText);
            } else {
                txtReview.setText("⚠️ El cliente aún no ha calificado este pedido.");
            }

            txtTotal.setText(String.format(Locale.getDefault(), "💰 Total: S/ %.2f", order.getTotalPrice()));

            container.addView(cardView);
        }

        double avg = reviewsCount == 0 ? 0 : totalStars / reviewsCount;
        txtDialogAvg.setText(String.format(Locale.getDefault(), "⭐ %.1f", avg));

        String analysis;
        if (avg >= 4.5) analysis = "Excelente desempeño del cocinero.";
        else if (avg >= 4) analysis = "Buen desempeño general.";
        else if (avg >= 3) analysis = "Desempeño regular.";
        else analysis = "Muchos clientes insatisfechos.";

        analysis += "\n\n✔ Positivas: " + positive + "\n⚠ Negativas: " + negative;
        txtDialogAnalysis.setText(analysis);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showAnalyticsDialog() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_review_analytics, null);
        TextView weekly = view.findViewById(R.id.txt_weekly);
        TextView monthly = view.findViewById(R.id.txt_monthly);
        TextView ai = view.findViewById(R.id.txt_ai_analysis);

        int positive = 0;
        int negative = 0;
        StringBuilder comments = new StringBuilder();

        for (Review review : reviewMap.values()) {
            if (review.getRating() >= 4) positive++;
            else negative++;

            if (review.getComment() != null && !review.getComment().trim().isEmpty()) {
                comments.append("• ").append(review.getComment()).append("\n\n");
            }
        }

        weekly.setText("📅 Semana\n\nClientes satisfechos: " + positive);
        monthly.setText("📆 Mes\n\nClientes insatisfechos: " + negative);
        ai.setText("🧠 Comentarios Analizados\n\n" + (comments.length() > 0 ? comments : "No hay comentarios disponibles."));

        new AlertDialog.Builder(requireContext())
                .setView(view)
                .show();
    }
}