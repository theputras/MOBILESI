package com.example.newproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private final List<Item> itemList;
    private final DecimalFormat rupiahNoCent;

    public ItemAdapter(List<Item> itemList) {
        this.itemList = itemList;

        // Formatter Rupiah: "Rp 1.200.000" tanpa angka desimal
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("in", "ID"));
        symbols.setGroupingSeparator('.'); // Menambahkan pemisah ribuan dengan titik
        symbols.setDecimalSeparator(','); // Menambahkan pemisah desimal dengan koma
        rupiahNoCent = new DecimalFormat("Rp #,###", symbols);
        rupiahNoCent.setMaximumFractionDigits(0); // Tidak ada angka desimal
        rupiahNoCent.setMinimumFractionDigits(0); // Tidak ada angka desimal
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = itemList.get(position);

        // Format harga dengan rupiah
        String hargaBeli = (item.getHargabeli() != 0)
                ? rupiahNoCent.format(item.getHargabeli()) // Format harga beli
                : "-";
        String hargaJual = (item.getHargajual() != 0)
                ? rupiahNoCent.format(item.getHargajual()) // Format harga jual
                : "-";

        // Set the values to TextViews
        holder.textViewNama.setText(item.getNama_item());
        holder.textViewKode.setText("Kode: " + item.getKode_item());
        holder.textViewSatuan.setText("Satuan: " + item.getSatuan());
        holder.textViewHargaBeli.setText("Harga Beli: " + hargaBeli);
        holder.textViewHargaJual.setText("Harga Jual: " + hargaJual);
    }

    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNama, textViewKode, textViewSatuan, textViewHargaBeli, textViewHargaJual;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNama = itemView.findViewById(R.id.textViewNama);
            textViewKode = itemView.findViewById(R.id.textViewKode);
            textViewSatuan = itemView.findViewById(R.id.textViewSatuan);
            textViewHargaBeli = itemView.findViewById(R.id.textViewHargaBeli);
            textViewHargaJual = itemView.findViewById(R.id.textViewHargaJual);
        }
    }

}
