package com.theputras.posrentalps.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.theputras.posrentalps.R;
import com.theputras.posrentalps.model.Tv;
import com.theputras.posrentalps.utils.CartManager;

import java.util.ArrayList;
import java.util.List;

public class TvGridAdapter extends RecyclerView.Adapter<TvGridAdapter.ViewHolder> {

    private Context context;
    private List<Tv> tvList;
    private List<Tv> originalList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Tv tv);
    }

    public TvGridAdapter(Context context, List<Tv> tvList, OnItemClickListener listener) {
        this.context = context;
        this.tvList = tvList;
        this.originalList = new ArrayList<>(tvList);
        this.listener = listener;
    }
    public void filter(String text) {
        tvList.clear(); // Kosongkan tampilan

        if (text.isEmpty()) {
            // Kalau search kosong, balikin semua data dari master
            tvList.addAll(originalList);
        } else {
            text = text.toLowerCase();
            for (Tv item : originalList) {
                // Logic Pencarian: Cek Nomor TV atau Nama Console
                boolean matchNomor = item.getNomorTv().toLowerCase().contains(text);

                boolean matchConsole = false;
                if (item.getJenisConsole() != null && item.getJenisConsole().getNamaConsole() != null) {
                    matchConsole = item.getJenisConsole().getNamaConsole().toLowerCase().contains(text);
                }

                if (matchNomor || matchConsole) {
                    tvList.add(item);
                }
            }
        }
        notifyDataSetChanged(); // Refresh UI
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_grid_tv, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tv tv = tvList.get(position);

        // 1. Data TV
        holder.tvNomor.setText(tv.getNomorTv());
        if (tv.getJenisConsole() != null) {
            holder.tvConsole.setText(tv.getJenisConsole().getNamaConsole());
        } else {
            holder.tvConsole.setText("-");
        }

        String status = (tv.getStatus() != null) ? tv.getStatus().toLowerCase() : "available";

        // 2. Cek apakah TV ini ada di Cart?
        boolean isInCart = isTvInCart(tv.getId());

        // Reset Klik dulu (Default mati)
        holder.itemView.setOnClickListener(null);
        holder.itemView.setClickable(false);

        if (status.equals("booked")) {
            // MERAH - Booked
            holder.tvStatus.setText("Booked");
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.red_busy));
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.red_light));
            holder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.red_busy));
            holder.cardView.setStrokeWidth(2);
            holder.itemView.setAlpha(0.7f);
            // Tidak ada listener (mati)

        } else if (status.equals("maintenance") || status.equals("rusak")) {
            // ABU - Rusak
            holder.tvStatus.setText("Rusak");
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.gray_maintenance));
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.gray_light));
            holder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.gray_maintenance));
            holder.cardView.setStrokeWidth(2);
            holder.itemView.setAlpha(0.6f);
            // Tidak ada listener (mati)

        } else if (isInCart) {
            // ORANGE - Sedang Dipilih (Di Keranjang)
            holder.tvStatus.setText("Dipilih");
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.primary_orange));

            // Background agak kuning/orange
            holder.cardView.setCardBackgroundColor(Color.parseColor("#FFF3E0"));
            holder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.primary_orange));
            holder.cardView.setStrokeWidth(4); // Border tebal

            holder.itemView.setAlpha(1.0f);
            holder.itemView.setClickable(true);
            holder.itemView.setOnClickListener(v ->
                    Toast.makeText(context, "Item sudah ada di keranjang!", Toast.LENGTH_SHORT).show()
            );

        } else {
            // HIJAU - Available (Normal)
            holder.tvStatus.setText("Available");
            holder.tvStatus.setTextColor(Color.WHITE); // Teks putih di background hijau
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_avail); // Pastikan background hijau

            holder.cardView.setCardBackgroundColor(Color.WHITE);
            holder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.text_light_grey));
            holder.cardView.setStrokeWidth(0);

            holder.itemView.setAlpha(1.0f);

            // HANYA DISINI YANG BISA DIKLIK UTK SEWA
            holder.itemView.setClickable(true);
            holder.itemView.setOnClickListener(v -> listener.onItemClick(tv));
        }
    }

    @Override
    public int getItemCount() {
        return (tvList != null) ? tvList.size() : 0;
    }

    // --- INI METHOD YANG HILANG ---
    private boolean isTvInCart(int tvId) {
        for (CartManager.CartDisplay item : CartManager.getInstance().getDisplayList()) {
            if (item.request.tvId == tvId) return true;
        }
        return false;
    }
    // ------------------------------

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNomor, tvStatus, tvConsole;
        // Ganti ke CardView biasa karena di XML kamu pakenya CardView biasa, bukan MaterialCardView
        // Kalau pakai MaterialCardView di XML, ganti ini jadi com.google.android.material.card.MaterialCardView
        com.google.android.material.card.MaterialCardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomor   = itemView.findViewById(R.id.tvNomorTv);
            tvStatus  = itemView.findViewById(R.id.tvStatus);
            tvConsole = itemView.findViewById(R.id.tvConsoleType);

            // Pastikan XML item_grid_tv.xml root-nya <com.google.android.material.card.MaterialCardView ... android:id="@+id/cardView">
            cardView  = itemView.findViewById(R.id.cardView);
        }
    }
}