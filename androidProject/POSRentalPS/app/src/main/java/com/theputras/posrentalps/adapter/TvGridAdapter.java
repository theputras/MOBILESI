package com.theputras.posrentalps.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
        // Mencegah error null pointer pada originalList
        this.originalList = new ArrayList<>(tvList != null ? tvList : new ArrayList<>());
        this.listener = listener;
    }

    public void filter(String text) {
        if (tvList == null) return;

        tvList.clear();
        if (text.isEmpty()) {
            tvList.addAll(originalList);
        } else {
            text = text.toLowerCase();
            for (Tv item : originalList) {
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
        notifyDataSetChanged();
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

        // 1. Set Data Text
        holder.tvNomor.setText(tv.getNomorTv());
        if (tv.getJenisConsole() != null && tv.getJenisConsole().getNamaConsole() != null) {
            holder.tvConsole.setText(tv.getJenisConsole().getNamaConsole());
        } else {
            holder.tvConsole.setText("-");
        }

        String status = (tv.getStatus() != null) ? tv.getStatus().toLowerCase() : "available";

        // 2. Cek Cart (Pakai ID TV)
        // INI YANG DIPERBAIKI -> Mengirim ID (int) ke method isTvInCart
        boolean isInCart = isTvInCart(tv.getId());

        // Reset Listener
        holder.itemView.setOnClickListener(null);
        holder.itemView.setClickable(false);

        // 3. Logic Warna Status
        if (status.equals("booked")) {
            // Booked -> Merah
            holder.tvStatus.setText("Booked");
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.red_busy));
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.red_light));
            holder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.red_busy));
            holder.cardView.setStrokeWidth(2);
            holder.itemView.setAlpha(0.7f);

        } else if (status.equals("maintenance") || status.equals("rusak")) {
            // Rusak -> Abu
            holder.tvStatus.setText("Rusak");
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.gray_maintenance));
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.gray_light));
            holder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.gray_maintenance));
            holder.cardView.setStrokeWidth(2);
            holder.itemView.setAlpha(0.6f);

        } else if (isInCart) {
            // Di Keranjang -> Orange
            holder.tvStatus.setText("Dipilih");
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.primary_orange));
            holder.cardView.setCardBackgroundColor(Color.parseColor("#FFF3E0"));
            holder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.primary_orange));
            holder.cardView.setStrokeWidth(4);
            holder.itemView.setAlpha(1.0f);

            holder.itemView.setClickable(true);
            holder.itemView.setOnClickListener(v ->
                    Toast.makeText(context, "Item sudah ada di keranjang!", Toast.LENGTH_SHORT).show()
            );

        } else {
            // Available -> Hijau/Putih
            holder.tvStatus.setText("Available");
            holder.tvStatus.setTextColor(Color.WHITE);
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_avail);
            holder.cardView.setCardBackgroundColor(Color.WHITE);
            holder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.text_light_grey));
            holder.cardView.setStrokeWidth(0);
            holder.itemView.setAlpha(1.0f);

            holder.itemView.setClickable(true);
            holder.itemView.setOnClickListener(v -> listener.onItemClick(tv));
        }
    }

    @Override
    public int getItemCount() {
        return (tvList != null) ? tvList.size() : 0;
    }

    // --- FIX UTAMA DISINI ---
    private boolean isTvInCart(int tvId) { // Parameter berupa int ID
        for (CartManager.CartDisplay item : CartManager.getInstance().getCartItems()) {
            // 1. Pakai .getTv() (karena variabel tv private)
            // 2. Ambil .getId() (karena kita bandingkan angka dengan angka)
            if (item.getTv().getId() == tvId) {
                return true;
            }
        }
        return false;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNomor, tvStatus, tvConsole;
        com.google.android.material.card.MaterialCardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomor   = itemView.findViewById(R.id.tvNomorTv);
            tvStatus  = itemView.findViewById(R.id.tvStatus);
            tvConsole = itemView.findViewById(R.id.tvConsoleType);
            cardView  = itemView.findViewById(R.id.cardView);
        }
    }
}