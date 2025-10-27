package com.example.newproject.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newproject.R;
import com.example.newproject.models.Konsumen;

import java.util.List;
import java.util.ArrayList; // Import ArrayList

public class KonsumenAdapter extends RecyclerView.Adapter<KonsumenAdapter.ViewHolder> {

    private List<Konsumen> konsumenList;

    // Tambahkan interface untuk click listener
    public interface OnKonsumenClickListener {
        void onKonsumenClick(Konsumen konsumen);
    }
    private OnKonsumenClickListener listener;
    // Constructor
// Modifikasi constructor atau tambahkan setter untuk listener
    public KonsumenAdapter(List<Konsumen> konsumenList, OnKonsumenClickListener listener) { // Terima listener
        this.konsumenList = new ArrayList<>(konsumenList != null ? konsumenList : new ArrayList<>());
        this.listener = listener; // Simpan listener
    }

    // Inner ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNamaKonsumen; // Hanya nama sekarang

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNamaKonsumen = itemView.findViewById(R.id.textViewNamaKonsumen);
            // TextView lain dihapus
        }

        // Method bind dipindah ke sini agar bisa akses listener
        public void bind(final Konsumen konsumen, final OnKonsumenClickListener listener) {
            textViewNamaKonsumen.setText(konsumen.getNamaKonsumen()); // Sesuaikan getter
            // Set listener di itemView
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onKonsumenClick(konsumen);
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout konsumen_layout yang sudah dipangkas
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.konsumen_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Konsumen konsumen = konsumenList.get(position);
        // Panggil method bind di ViewHolder
        holder.bind(konsumen, listener);
        // Hapus kode set text yang lain
    }

    @Override
    public int getItemCount() {
        // Kembalikan jumlah item dalam list
        return konsumenList != null ? konsumenList.size() : 0;
    }

    // Method untuk update data di adapter (sesuai permintaan sebelumnya)
    public void updateData(List<Konsumen> newKonsumenList) {
        this.konsumenList.clear();
        if (newKonsumenList != null) {
            this.konsumenList.addAll(newKonsumenList);
        }
        notifyDataSetChanged(); // Beritahu RecyclerView untuk refresh tampilan
    }
}