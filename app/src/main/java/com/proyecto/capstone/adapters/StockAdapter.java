package com.proyecto.capstone.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.FirebaseDatabase;
import com.proyecto.capstone.R;
import com.proyecto.capstone.models.Item;

import java.io.File;
import java.util.List;

public class StockAdapter
        extends RecyclerView.Adapter<StockAdapter.ViewHolder> {

    private final List<Item> itemList;

    private final List<String> itemKeys;

    public StockAdapter(
            List<Item> itemList,
            List<String> itemKeys
    ) {

        this.itemList = itemList;
        this.itemKeys = itemKeys;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater.from(
                        parent.getContext()
                ).inflate(
                        R.layout.item_item,
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

        Item item =
                itemList.get(position);

        String itemKey =
                itemKeys.get(position);

        holder.nameText.setText(
                item.getName()
        );

        holder.priceText.setText(
                String.format(
                        "S/ %.2f",
                        item.getPrice()
                )
        );

        holder.stockText.setText(
                "Stock: "
                        + item.getStock()
        );

        holder.quantityText.setVisibility(
                View.GONE
        );

        holder.addButton.setVisibility(
                View.VISIBLE
        );

        holder.minusButton.setVisibility(
                View.VISIBLE
        );

        holder.addButton.setOnClickListener(v -> {

            int newStock =
                    item.getStock() + 1;

            updateStock(
                    itemKey,
                    newStock
            );

            item.setStock(newStock);

            holder.stockText.setText(
                    "Stock: " + newStock
            );
        });

        holder.minusButton.setOnClickListener(v -> {

            int current =
                    item.getStock();

            if (current <= 0) return;

            int newStock =
                    current - 1;

            updateStock(
                    itemKey,
                    newStock
            );

            item.setStock(newStock);

            holder.stockText.setText(
                    "Stock: " + newStock
            );
        });

        String imageUrl =
                item.getImageUrl();

        if (imageUrl != null
                && !imageUrl.isEmpty()) {

            Object imageSource;

            if (imageUrl.startsWith("http")) {

                imageSource = imageUrl;

            } else {

                imageSource =
                        new File(imageUrl);
            }

            Glide.with(holder.itemView.getContext())
                    .load(imageSource)
                    .placeholder(R.drawable.restaurante)
                    .error(R.drawable.ic_placeholder)
                    .into(holder.itemImage);

        } else {

            holder.itemImage.setImageResource(
                    R.drawable.restaurante
            );
        }
    }

    private void updateStock(
            String itemKey,
            int stock
    ) {

        FirebaseDatabase.getInstance()
                .getReference("items")
                .child(itemKey)
                .child("stock")
                .setValue(stock);
    }

    @Override
    public int getItemCount() {

        return itemList.size();
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView nameText;
        TextView priceText;
        TextView stockText;
        TextView quantityText;

        Button addButton;
        Button minusButton;

        ImageView itemImage;

        public ViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            nameText =
                    itemView.findViewById(
                            R.id.item_name
                    );

            priceText =
                    itemView.findViewById(
                            R.id.item_price
                    );

            stockText =
                    itemView.findViewById(
                            R.id.item_stock_text
                    );

            quantityText =
                    itemView.findViewById(
                            R.id.cart_item_quantity_text
                    );

            addButton =
                    itemView.findViewById(
                            R.id.add_button
                    );

            minusButton =
                    itemView.findViewById(
                            R.id.minus_button
                    );

            itemImage =
                    itemView.findViewById(
                            R.id.item_imagen
                    );
        }
    }
}