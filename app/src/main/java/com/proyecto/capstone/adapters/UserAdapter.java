package com.proyecto.capstone.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // NUEVO
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.proyecto.capstone.R;
import com.proyecto.capstone.models.User;
import com.google.android.material.button.MaterialButton; // Asegurar la importación

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private List<User> userList;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UserAdapter(List<User> userList) {
        this.userList = userList;
        this.listener = null;
    }

    public UserAdapter(List<User> userList, OnUserClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.nameText.setText(user.getName());
        holder.emailText.setText(user.getEmail());
        holder.roleText.setText("Rol: " + user.getRole()); // Añadir el prefijo "Rol: " si es necesario

        if (listener != null) {
            holder.editButton.setVisibility(View.VISIBLE);
            holder.editButton.setOnClickListener(v -> listener.onUserClick(user));
        } else {
            holder.editButton.setVisibility(View.GONE);
        }

        // El ícono de usuario (holder.userIcon) se configura en el XML,
        // no se requiere lógica adicional aquí a menos que cambies el ícono dinámicamente.
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, emailText, roleText;
        MaterialButton editButton; // Cambiado de Button a MaterialButton (si usas la importación de arriba)
        ImageView userIcon; // NUEVO: Para el icono de usuario

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userIcon = itemView.findViewById(R.id.user_icon); // NUEVO
            nameText = itemView.findViewById(R.id.user_name);
            emailText = itemView.findViewById(R.id.user_email);
            roleText = itemView.findViewById(R.id.user_role);
            editButton = itemView.findViewById(R.id.edit_button);
        }
    }
}