package com.proyecto.capstone.activities.user.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import com.proyecto.capstone.R;
import com.proyecto.capstone.adapters.CartAdapter;
import com.proyecto.capstone.models.Item;
import com.proyecto.capstone.models.Order;
import com.proyecto.capstone.models.User;
import com.proyecto.capstone.utils.CartManager;
import com.proyecto.capstone.utils.NotificationHelper;
import com.proyecto.capstone.utils.OrderCodeGenerator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CartFragment extends Fragment
        implements CartAdapter.OnCartActionListener {

    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private final CartManager cartManager = CartManager.getInstance();
    private TextView totalText;
    private boolean isProcessingOrder = false;
    private TextView paymentStatusText;
    private MaterialButton checkoutButton;
    private MaterialButton cardPaymentButton;
    private MaterialButton cashPaymentButton;
    private String selectedPaymentMethod = null;
    private String currentUserId;
    private String currentUserName = "Usuario";
    private Long selectedScheduleTime = null;

    private final Map<String, String> itemIdToNameMap = new HashMap<>();
    private final Map<String, Double> itemIdToPriceMap = new HashMap<>();
    private final Map<String, String> itemIdToImageUrlMap = new HashMap<>();
    private final Map<String, Item> itemMap = new HashMap<>();

    private ValueEventListener itemsListener;
    private DatabaseReference itemsDbRef;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return view;
        }

        currentUserId = user.getUid();

        recyclerView = view.findViewById(R.id.recycler_cart);
        totalText = view.findViewById(R.id.cart_total_text);
        paymentStatusText = view.findViewById(R.id.payment_status_text);
        checkoutButton = view.findViewById(R.id.checkout_button);
        cashPaymentButton = view.findViewById(R.id.btn_efectivo);
        cardPaymentButton = view.findViewById(R.id.btn_tarjeta);

        checkoutButton.setEnabled(false);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        adapter = new CartAdapter(
                cartManager.getCartItems(),
                itemIdToNameMap,
                itemIdToPriceMap,
                itemIdToImageUrlMap,
                cartManager,
                this
        );

        recyclerView.setAdapter(adapter);

        loadUserData();
        loadItems();

        cashPaymentButton.setOnClickListener(v -> handleCashPayment());
        cardPaymentButton.setOnClickListener(v -> handleCardPayment());
        checkoutButton.setOnClickListener(v -> attemptPlaceOrder());

        updateTotal();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (itemsDbRef != null && itemsListener != null) {
            itemsDbRef.removeEventListener(itemsListener);
        }
    }

    private void handleCashPayment() {
        selectedPaymentMethod = "Efectivo";
        checkoutButton.setEnabled(true);
        paymentStatusText.setText("Pago en efectivo seleccionado");
    }

    private void handleCardPayment() {
        selectedPaymentMethod = "Tarjeta POS";
        checkoutButton.setEnabled(true);
        paymentStatusText.setText("Pago con tarjeta seleccionado");
    }

    private void selectOrderTime() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_time_picker, null);
        Spinner spinnerHour = dialogView.findViewById(R.id.spinner_hour);
        Spinner spinnerMinute = dialogView.findViewById(R.id.spinner_minute);

        List<String> hours = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hours.add(String.format(Locale.getDefault(), "%02d", i));
        }

        List<String> minutes = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            minutes.add(String.format(Locale.getDefault(), "%02d", i));
        }

        ArrayAdapter<String> hourAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, hours);
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHour.setAdapter(hourAdapter);

        ArrayAdapter<String> minuteAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, minutes);
        minuteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMinute.setAdapter(minuteAdapter);

        Calendar now = Calendar.getInstance();
        spinnerHour.setSelection(now.get(Calendar.HOUR_OF_DAY));
        spinnerMinute.setSelection(now.get(Calendar.MINUTE));

        new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    int selectedHour = Integer.parseInt(spinnerHour.getSelectedItem().toString());
                    int selectedMinute = Integer.parseInt(spinnerMinute.getSelectedItem().toString());

                    Calendar selected = Calendar.getInstance();
                    selected.set(Calendar.HOUR_OF_DAY, selectedHour);
                    selected.set(Calendar.MINUTE, selectedMinute);
                    selected.set(Calendar.SECOND, 0);
                    selected.set(Calendar.MILLISECOND, 0);

                    if (selectedHour > 21 || (selectedHour == 21 && selectedMinute > 15)) {
                        showToast("No se pueden programar pedidos después de las 9:15 PM");
                        checkoutButton.setEnabled(true);
                        return;
                    }

                    Calendar minAllowedTime = Calendar.getInstance();
                    minAllowedTime.add(Calendar.MINUTE, 5);

                    if (selected.before(minAllowedTime)) {
                        new AlertDialog.Builder(getContext())
                                .setTitle("⚠️ ADVERTENCIA DE SEGURIDAD")
                                .setMessage("No se permiten pedidos programados en el pasado ni con menos de 5 minutos de anticipación.\n\nSi esto es una broma, se le recuerda que ya se tiene un registro del estudiante en el sistema y se informará formalmente a la institución sobre este comportamiento.")
                                .setPositiveButton("Entendido", (dialogInt, whichInt) -> {
                                    checkoutButton.setEnabled(true);
                                })
                                .setCancelable(false)
                                .show();
                        return;
                    }

                    selectedScheduleTime = selected.getTimeInMillis();
                    paymentStatusText.setText("Pedido programado para: " + String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute));

                    if (!isProcessingOrder) {
                        placeOrder(currentUserId, currentUserName, selectedPaymentMethod);
                    }
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    checkoutButton.setEnabled(true);
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    private void attemptPlaceOrder() {
        if (isProcessingOrder) {
            return;
        }

        if (cartManager.getCartItems().isEmpty()) {
            showToast("El carrito está vacío");
            return;
        }

        if (selectedPaymentMethod == null) {
            showToast("Selecciona método de pago");
            return;
        }

        checkoutButton.setEnabled(false);
        selectOrderTime();
    }

    private void placeOrder(String userId, String userName, String paymentMethod) {
        if (isProcessingOrder) {
            return;
        }

        isProcessingOrder = true;

        FirebaseDatabase.getInstance().getReference("users").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String realName = "Usuario";
                        if (snapshot.exists() && snapshot.child("name").getValue(String.class) != null) {
                            realName = snapshot.child("name").getValue(String.class);
                        }

                        double total = cartManager.calculateTotal(itemMap);
                        String orderCode = OrderCodeGenerator.generateCode();
                        List<Order.OrderItem> orderItems = new ArrayList<>(cartManager.getCartItems());

                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        String userEmail = (firebaseUser != null) ? firebaseUser.getEmail() : "";

                        Order newOrder = new Order(
                                userId,
                                realName,
                                userEmail,
                                orderItems,
                                total,
                                orderCode,
                                "pending",
                                paymentMethod,
                                false,
                                null,
                                new Date(),
                                null,
                                false,
                                false,
                                selectedScheduleTime
                        );

                        DatabaseReference db = FirebaseDatabase.getInstance().getReference("orders").push();

                        db.setValue(newOrder)
                                .addOnSuccessListener(aVoid -> {
                                    reduceStock(orderItems);

                                    NotificationHelper.showNotification(
                                            getContext(),
                                            "Nuevo Pedido",
                                            "Pedido " + orderCode
                                    );

                                    cartManager.clearCart();
                                    adapter.notifyDataSetChanged();
                                    updateTotal();

                                    selectedPaymentMethod = null;
                                    selectedScheduleTime = null;

                                    paymentStatusText.setText("Pedido realizado correctamente");
                                    checkoutButton.setEnabled(false);
                                    isProcessingOrder = false;

                                    showToast("Pedido programado correctamente");
                                })
                                .addOnFailureListener(e -> {
                                    isProcessingOrder = false;
                                    checkoutButton.setEnabled(true);
                                    showToast("Error al realizar pedido");
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        isProcessingOrder = false;
                        checkoutButton.setEnabled(true);
                        showToast("Error al validar datos del usuario");
                    }
                });
    }

    private void reduceStock(List<Order.OrderItem> items) {
        for (Order.OrderItem orderItem : items) {
            FirebaseDatabase.getInstance()
                    .getReference("items")
                    .child(orderItem.getItemId())
                    .child("stock")
                    .runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                            Integer stock = currentData.getValue(Integer.class);
                            if (stock == null) {
                                return Transaction.success(currentData);
                            }

                            stock = stock - orderItem.getQuantity();
                            if (stock < 0) {
                                stock = 0;
                            }

                            currentData.setValue(stock);
                            return Transaction.success(currentData);
                        }

                        @Override
                        public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                        }
                    });
        }
    }

    private void loadUserData() {
        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            currentUserName = user.getName();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void loadItems() {
        itemsDbRef = FirebaseDatabase.getInstance().getReference("items");
        itemsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemMap.clear();
                itemIdToNameMap.clear();
                itemIdToPriceMap.clear();
                itemIdToImageUrlMap.clear();

                for (DataSnapshot s : snapshot.getChildren()) {
                    Item item = s.getValue(Item.class);
                    if (item != null) {
                        itemMap.put(s.getKey(), item);
                        itemIdToNameMap.put(s.getKey(), item.getName());
                        itemIdToPriceMap.put(s.getKey(), item.getPrice());
                        itemIdToImageUrlMap.put(s.getKey(), item.getImageUrl());
                    }
                }

                adapter.notifyDataSetChanged();
                updateTotal();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        itemsDbRef.addValueEventListener(itemsListener);
    }

    private void updateTotal() {
        totalText.setText("TOTAL: S/ " + String.format(Locale.getDefault(), "%.2f", cartManager.calculateTotal(itemMap)));
    }

    @Override
    public void onQuantityChange(String itemId, int newQuantity) {
        cartManager.updateItemQuantity(itemId, newQuantity, (success, stock) -> {
            adapter.notifyDataSetChanged();
            updateTotal();
        });
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
