package com.theputras.posrentalps.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.theputras.posrentalps.R;
import com.theputras.posrentalps.utils.CartManager;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private List<CartManager.CartDisplay> cartItems;
    private CartListener listener;

    // Interface buat ngabarin ke HomeFragment kalau ada perubahan (biar Total Harga update)
    public interface CartListener {
        void onCartUpdated();
    }

    public CartAdapter(List<CartManager.CartDisplay> cartItems, CartListener listener) {
        this.cartItems = cartItems;
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
        CartManager.CartDisplay item = cartItems.get(position);
        Context context = holder.itemView.getContext();

        holder.tvName.setText(item.getDisplayName());
        holder.tvPrice.setText("Rp " + String.format("%,d", item.getPrice()).replace(',', '.'));

        // --- LOGIC TOMBOL HAPUS ---
        holder.btnDelete.setOnClickListener(v -> {
            // --- TAMBAHAN: ALERT DIALOG KONFIRMASI ---
            new AlertDialog.Builder(context)
                    .setTitle("Hapus Item")
                    .setMessage("Yakin ingin menghapus " + item.getDisplayName() + "?")
                    .setPositiveButton("Hapus", (dialog, which) -> {

                        // Eksekusi Hapus saat pilih "YA"
                        int currentPos = holder.getAdapterPosition();
                        if (currentPos != RecyclerView.NO_POSITION) {
                            CartManager.getInstance().removeItem(currentPos);
                            notifyItemRemoved(currentPos);
                            notifyItemRangeChanged(currentPos, cartItems.size());
                            listener.onCartUpdated();
                        }
                    })
                    .setNegativeButton("Batal", null) // Tutup dialog kalau pilih "Batal"
                    .show();
            // -----------------------------------------
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice;
        ImageButton btnDelete; // Pastikan ini ada

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCartItemName);
            tvPrice = itemView.findViewById(R.id.tvCartItemPrice);

            // Binding ID dari XML kamu
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}