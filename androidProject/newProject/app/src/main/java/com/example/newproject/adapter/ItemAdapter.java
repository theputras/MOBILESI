package com.example.newproject.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newproject.R;
import com.example.newproject.models.Item;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private List<Item> itemList;
    private final DecimalFormat rupiahNoCent;
    // Tambahkan interface untuk click listener
    public interface OnItemClickListener {
        void onItemClick(Item item);
    }
    private OnItemClickListener listener;
    public ItemAdapter(List<Item> itemList, OnItemClickListener listener) {

        this.itemList = new ArrayList<>(itemList != null ? itemList : new ArrayList<>());
        this.listener = listener;
        // Formatter Rupiah: "Rp 1.200.000" tanpa angka desimal
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("in", "ID"));
        symbols.setGroupingSeparator('.'); // Menambahkan pemisah ribuan dengan titik
        symbols.setDecimalSeparator(','); // Menambahkan pemisah desimal dengan koma
        rupiahNoCent = new DecimalFormat("Rp #,###", symbols);
        rupiahNoCent.setMaximumFractionDigits(0); // Tidak ada angka desimal
        rupiahNoCent.setMinimumFractionDigits(0); // Tidak ada angka desimal
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNama; // Hanya nama sekarang

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNama = itemView.findViewById(R.id.textViewNama);
            // TextView lain dihapus
        }

        // Method bind dipindah ke sini agar bisa akses listener
        public void bind(final Item item, final OnItemClickListener listener) {
            textViewNama.setText(item.getNama_item()); // Sesuaikan getter
            // Set listener di itemView
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClick(item);
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout item_layout yang sudah dipangkas
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = itemList.get(position);
        // Panggil method bind di ViewHolder
        holder.bind(item, listener);
        // Hapus kode set text yang lain
    }

    public void updateData(List<Item> newItems) {
        // Pastikan list internal tidak null (jika pakai ArrayList)
        if (this.itemList == null) {
            this.itemList = new ArrayList<>();
        }

        this.itemList.clear(); // Hapus data lama

        if (newItems != null) {
            this.itemList.addAll(newItems); // Tambahkan semua data baru
        }

        notifyDataSetChanged(); // Beri tahu RecyclerView untuk refresh
        // Catatan: Untuk performa lebih baik di list yang sangat besar,
        // bisa pakai DiffUtil, tapi notifyDataSetChanged() sudah cukup untuk awal.
    }

}
