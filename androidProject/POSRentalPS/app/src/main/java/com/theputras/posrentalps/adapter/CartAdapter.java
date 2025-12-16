package com.theputras.posrentalps.adapter;

import android.annotation.SuppressLint;
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

    private final List<CartManager.CartDisplay> list;
    private final CartListener listener;

    public interface CartListener {
        void onCartUpdated();
    }

    public CartAdapter(List<CartManager.CartDisplay> list, CartListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Pastikan item_cart.xml sudah diupdate (tanpa qty)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        CartManager.CartDisplay item = list.get(position);

        holder.tvName.setText(item.displayName);
        holder.tvPrice.setText("Rp " + item.price);

        // Hanya Logic Hapus
        holder.btnDelete.setOnClickListener(v -> {
            CartManager.getInstance().removeItem(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, list.size());

            // Beritahu Activity untuk update total harga
            if (listener != null) listener.onCartUpdated();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCartItemName);
            tvPrice = itemView.findViewById(R.id.tvCartItemPrice);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}