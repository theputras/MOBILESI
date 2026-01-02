package com.theputras.posrentalps.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.theputras.posrentalps.R;
import com.theputras.posrentalps.model.TransactionItem;
import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private Context context;
    private List<TransactionItem> list;
    private List<TransactionItem> originalList; // Master Data (Backup)
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(TransactionItem item);
    }

    // 3. Update Constructor untuk menerima listener
    public HistoryAdapter(Context context, List<TransactionItem> list, OnItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.originalList = new ArrayList<>(list);
        this.listener = listener;
    }
    public HistoryAdapter(Context context, List<TransactionItem> list) {
        this.context = context;
        this.originalList = new ArrayList<>(list);
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }
    public void filter(String text) {
        list.clear(); // Kosongkan tampilan saat ini

        if (text.isEmpty()) {
            // Kalau search kosong, kembalikan semua data
            list.addAll(originalList);
        } else {
            text = text.toLowerCase();
            for (TransactionItem item : originalList) {

                // 1. Cek Nama Penyewa (Akses Langsung Public Field)
                boolean matchNama = false;
                if (item.namaPenyewa != null) {
                    matchNama = item.namaPenyewa.toLowerCase().contains(text);
                }

                // 2. Cek Nomor TV (Akses Langsung Public Field)
                boolean matchIDTransaksi = false;
                if (item.idTransaksi != null) {
                    matchIDTransaksi = item.idTransaksi.toString().contains(text);
                }

                // Jika salah satu cocok, masukkan ke list tampilan
                if (matchIDTransaksi || matchNama) {
                    list.add(item);
                }
            }
        }
        notifyDataSetChanged(); // Update layar
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransactionItem item = list.get(position);

        holder.tvInvoice.setText("No Invoice: " + item.idTransaksi);
        holder.tvDate.setText(item.tanggalTransaksi);
        holder.tvName.setText(item.namaPenyewa);
        holder.tvAmount.setText("Rp " + String.format("%,d", item.totalTagihan));
//        if (item.nomorTv != null) {
//            holder.tvInfoTv.setText("TV " + item.nomorTv);
//        } else {
//            holder.tvInfoTv.setText("TV -");
//        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInvoice, tvDate, tvName, tvAmount, tvInfoTv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Pastikan ID ini sesuai dengan layout item_history.xml kamu
            tvInvoice = itemView.findViewById(R.id.tvInvoiceId);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvName = itemView.findViewById(R.id.tvCustomerName);
            tvAmount = itemView.findViewById(R.id.tvTotalAmount);
//            tvInfoTv = itemView.findViewById(R.id.tvHistoryTvNum);
        }
    }
}