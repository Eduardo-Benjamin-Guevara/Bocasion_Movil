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
import com.proyecto.capstone.R;
import com.proyecto.capstone.models.Item;
import com.proyecto.capstone.utils.CartManager;
import java.io.File;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_HEADER = 1;

    private List<Item> itemList;
    private List<String> itemKeys;
    private OnQuantityChangeListener quantityListener;
    private OnItemClickListener itemClickListener;
    private CartManager cartManager;
    private boolean isForMenu;

    public interface OnQuantityChangeListener {
        void onQuantityChange(String itemId, int newQuantity);
    }

    public interface OnItemClickListener {
        void onItemClick(Item item);
    }

    public ItemAdapter(List<Item> itemList, List<String> itemKeys, OnQuantityChangeListener quantityListener, OnItemClickListener itemClickListener, CartManager cartManager, boolean isForMenu) {
        this.itemList = itemList;
        this.itemKeys = itemKeys;
        this.quantityListener = quantityListener;
        this.itemClickListener = itemClickListener;
        this.cartManager = cartManager;
        this.isForMenu = isForMenu;
    }

    @Override
    public int getItemViewType(int position) {
        Item item = itemList.get(position);
        boolean isHeader = item.getName() != null && item.getDescription() == null && item.getPrice() == 0.0;
        return isHeader ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_item, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Item item = itemList.get(position);

        if (holder.getItemViewType() == VIEW_TYPE_HEADER) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.headerText.setText(item.getName());
        } else {
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            String itemId = itemKeys.get(position); // Este puede ser null

            itemHolder.nameText.setText(item.getName());
            itemHolder.priceText.setText(String.format("S/ %.2f", item.getPrice()));

            itemHolder.itemView.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(item);
                }
            });

            if (isForMenu && itemId != null) { // Comprobar itemId antes de configurar listeners de cantidad
                itemHolder.addButton.setVisibility(View.VISIBLE);
                itemHolder.minusButton.setVisibility(View.VISIBLE);

                int currentQuantity = cartManager.getQuantity(itemId);
                itemHolder.quantityText.setText(String.valueOf(currentQuantity));
                itemHolder.quantityText.setVisibility(currentQuantity > 0 ? View.VISIBLE : View.GONE);

                itemHolder.addButton.setOnClickListener(v -> quantityListener.onQuantityChange(itemId, currentQuantity + 1));
                itemHolder.minusButton.setOnClickListener(v -> quantityListener.onQuantityChange(itemId, currentQuantity - 1));

                itemHolder.stockText.setVisibility(View.VISIBLE);

                if (item.getStock() <= 0 || !item.isAvailable()) {
                    itemHolder.addButton.setEnabled(false);
                    itemHolder.addButton.setAlpha(0.5f);
                    itemHolder.minusButton.setEnabled(false);
                    itemHolder.minusButton.setAlpha(0.5f);

                    itemHolder.stockText.setText("AGOTADO");
                    itemHolder.stockText.setBackgroundColor(itemHolder.itemView.getContext().getResources().getColor(R.color.badge_red));
                    itemHolder.stockText.setTextColor(itemHolder.itemView.getContext().getResources().getColor(R.color.white));
                    itemHolder.quantityText.setVisibility(View.GONE);
                } else {
                    itemHolder.addButton.setEnabled(true);
                    itemHolder.addButton.setAlpha(1.0f);
                    itemHolder.minusButton.setEnabled(true);
                    itemHolder.minusButton.setAlpha(1.0f);

                    itemHolder.stockText.setText("Stock: " + item.getStock());
                    itemHolder.stockText.setBackgroundColor(itemHolder.itemView.getContext().getResources().getColor(android.R.color.white));
                    itemHolder.stockText.setTextColor(itemHolder.itemView.getContext().getResources().getColor(R.color.gray_900));
                }

            } else {
                itemHolder.stockText.setVisibility(View.VISIBLE);
                itemHolder.stockText.setText("Stock: " + item.getStock());
                itemHolder.stockText.setBackgroundColor(itemHolder.itemView.getContext().getResources().getColor(android.R.color.white));
                itemHolder.stockText.setTextColor(itemHolder.itemView.getContext().getResources().getColor(R.color.gray_900));

                itemHolder.addButton.setVisibility(View.GONE);
                itemHolder.minusButton.setVisibility(View.GONE);
                itemHolder.quantityText.setVisibility(View.GONE);
            }

            String urlOrPath = item.getImageUrl();
            if (urlOrPath != null && !urlOrPath.isEmpty()) {
                Object imageSource;
                if (urlOrPath.startsWith("http") || urlOrPath.startsWith("https")) {
                    imageSource = urlOrPath;
                } else {
                    imageSource = new File(urlOrPath);
                }

                Glide.with(itemHolder.itemView.getContext())
                        .load(imageSource)
                        .placeholder(R.drawable.restaurante)
                        .error(R.drawable.ic_placeholder)
                        .into(itemHolder.itemImage);

                itemHolder.itemImage.setVisibility(View.VISIBLE);
            } else {
                itemHolder.itemImage.setImageResource(R.drawable.restaurante);
                itemHolder.itemImage.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, priceText, stockText, quantityText;
        Button addButton, minusButton;
        ImageView itemImage;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.item_name);
            priceText = itemView.findViewById(R.id.item_price);
            addButton = itemView.findViewById(R.id.add_button);
            itemImage = itemView.findViewById(R.id.item_imagen);

            stockText = itemView.findViewById(R.id.item_stock_text);
            quantityText = itemView.findViewById(R.id.cart_item_quantity_text);
            minusButton = itemView.findViewById(R.id.minus_button);
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerText;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerText = itemView.findViewById(R.id.category_header_text);
        }
    }
}