package com.proyecto.capstone.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView; // AÑADIDO: Importar ImageView
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.proyecto.capstone.R;
import com.proyecto.capstone.models.Order;
import com.proyecto.capstone.utils.CartManager;
import com.bumptech.glide.Glide; // AÑADIDO: Importar Glide

import java.util.List;
import java.util.Map;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private final List<Order.OrderItem> cartItems;
    private final Map<String, String> itemIdToNameMap;
    // AÑADIDO: Se necesitará un mapa para el precio y la URL o una lista completa de ítems
    private final Map<String, Double> itemIdToPriceMap;
    private final Map<String, String> itemIdToImageUrlMap;
    private final CartManager cartManager;
    private final OnCartActionListener listener;

    public interface OnCartActionListener {
        void onQuantityChange(String itemId, int newQuantity);
    }

    // Constructor modificado para incluir el mapa de precios e imágenes
    public CartAdapter(List<Order.OrderItem> cartItems,
                       Map<String, String> itemIdToNameMap,
                       Map<String, Double> itemIdToPriceMap, // NUEVO: Para obtener el precio
                       Map<String, String> itemIdToImageUrlMap, // NUEVO: Para obtener la URL de la imagen
                       CartManager cartManager,
                       OnCartActionListener listener) {
        this.cartItems = cartItems;
        this.itemIdToNameMap = itemIdToNameMap;
        this.itemIdToPriceMap = itemIdToPriceMap; // Inicialización
        this.itemIdToImageUrlMap = itemIdToImageUrlMap; // Inicialización
        this.cartManager = cartManager;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order.OrderItem cartItem = cartItems.get(position);
        String itemId = cartItem.getItemId();
        int quantity = cartItem.getQuantity();

        String itemName = itemIdToNameMap.getOrDefault(itemId, "Ítem desconocido");
        holder.nameText.setText(itemName);

        double itemPrice = itemIdToPriceMap.getOrDefault(itemId, 0.00);
        holder.priceText.setText(String.format("S/ %.2f", itemPrice));

        holder.quantityText.setText(String.valueOf(quantity));

        // AÑADIDO: Cargar la imagen del producto con Glide
        String imageUrl = itemIdToImageUrlMap.get(itemId);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.restaurante)
                    .error(R.drawable.ic_placeholder)
                    .into(holder.itemImage);
        } else {
            holder.itemImage.setImageResource(R.drawable.restaurante);
        }

        // El resto del código de los botones se mantiene...
        holder.addButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onQuantityChange(itemId, quantity + 1);
            }
        });

        holder.minusButton.setOnClickListener(v -> {
            if (listener != null && quantity > 0) {
                listener.onQuantityChange(itemId, quantity - 1);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, quantityText, priceText; // MODIFICADO: Añadido priceText
        Button addButton, minusButton;
        ImageView itemImage; // AÑADIDO: ImageView

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.cart_item_name);
            quantityText = itemView.findViewById(R.id.cart_item_quantity_text);
            addButton = itemView.findViewById(R.id.cart_add_button);
            minusButton = itemView.findViewById(R.id.cart_minus_button);

            itemImage = itemView.findViewById(R.id.cart_item_image);
            priceText = itemView.findViewById(R.id.cart_item_price);
        }
    }
}