package com.theputras.posrentalps.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.theputras.posrentalps.R;
import com.theputras.posrentalps.model.PaketSewa;
import java.util.List;

public class PaketAdapter extends RecyclerView.Adapter<PaketAdapter.ViewHolder> {

    private List<PaketSewa> list;
    private OnPaketClickListener listener;

    public interface OnPaketClickListener {
        void onPaketClick(PaketSewa paket);
    }

    public PaketAdapter(List<PaketSewa> list, OnPaketClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Kita butuh layout item_paket.xml (akan dibuat di bawah)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_paket, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaketSewa paket = list.get(position);

        // ID-nya harus sesuai item_paket.xml di atas
        holder.tvNama.setText(paket.namaPaket);
        holder.tvHarga.setText("Rp " + String.format("%,d", paket.harga));

        holder.itemView.setOnClickListener(v -> listener.onPaketClick(paket));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvHarga;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Pastikan ID ini ada di item_paket.xml
            tvNama = itemView.findViewById(R.id.tvNamaPaket);
            tvHarga = itemView.findViewById(R.id.tvHargaPaket);
        }
    }
}