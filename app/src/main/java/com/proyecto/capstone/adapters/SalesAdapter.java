package com.proyecto.capstone.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.proyecto.capstone.R;
import com.proyecto.capstone.models.Sales;
import com.proyecto.capstone.activities.admin.fragments.SalesFragment.SalesCountWithCook;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class SalesAdapter extends RecyclerView.Adapter<SalesAdapter.ViewHolder> {
    private List<Sales> salesList;
    private final OnDetailsClickListener listener;

    public interface OnDetailsClickListener {
        void onDetailsClick(Sales salesItem);
    }

    public SalesAdapter(List<Sales> salesList, OnDetailsClickListener listener) {
        this.salesList = salesList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sales, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Sales sales = salesList.get(position);

        if (sales instanceof SalesCountWithCook) {
            SalesCountWithCook cookSales = (SalesCountWithCook) sales;

            holder.cookIdText.setText("Cocinero: " + cookSales.getCookName());

            // MODIFICACIÓN CLAVE: Mostrar el conteo de pedidos
            holder.totalText.setText(String.valueOf(cookSales.getOrderCount()));
        } else {
            // Fallback
            holder.cookIdText.setText("Cocinero (ID Desconocido)");
            holder.totalText.setText("N/A");
        }

        holder.detailsButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDetailsClick(sales);
            }
        });
    }

    @Override
    public int getItemCount() {
        return salesList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView cookIdText, totalText;
        MaterialButton detailsButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cookIdText = itemView.findViewById(R.id.sales_cook_id);
            totalText = itemView.findViewById(R.id.sales_total);
            detailsButton = itemView.findViewById(R.id.details_button);
        }
    }
}