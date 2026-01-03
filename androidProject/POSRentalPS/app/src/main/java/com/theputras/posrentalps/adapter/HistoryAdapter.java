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
    private List<TransactionItem> originalList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(TransactionItem item);
    }

    public HistoryAdapter(Context context, List<TransactionItem> list, OnItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.originalList = new ArrayList<>(list);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransactionItem item = list.get(position);

        holder.tvInvoice.setText(String.valueOf(item.idTransaksi));
        holder.tvDate.setText(item.tanggalTransaksi);
        holder.tvName.setText(item.namaPenyewa);
        holder.tvAmount.setText("Rp " + String.format("%,d", item.totalTagihan));

        // --- AMBIL DATA DARI HELPER (SUDAH AMAN NULL) ---
        String nomorTv = item.getDisplayTv();
        String namaConsole = item.getDisplayConsole();

//        if (!namaConsole.isEmpty()) {
//            holder.tvInfoTv.setText(nomorTv + " (" + namaConsole + ")");
//        } else {
//            holder.tvInfoTv.setText(nomorTv);
//        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void filter(String text) {
        list.clear();
        if (text.isEmpty()) {
            list.addAll(originalList);
        } else {
            text = text.toLowerCase();
            for (TransactionItem item : originalList) {
                boolean matchNama = item.namaPenyewa != null && item.namaPenyewa.toLowerCase().contains(text);

                // Cari berdasarkan Nomor TV (harus akses ke item.tv)
                boolean matchIDTransaksi = false;
                if (item.tv != null && item.idTransaksi != null) {
                    matchIDTransaksi = item.idTransaksi.toString().contains(text);
                }

                if (matchIDTransaksi || matchNama) list.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInvoice, tvDate, tvName, tvAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInvoice = itemView.findViewById(R.id.tvInvoiceId);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvName = itemView.findViewById(R.id.tvCustomerName);
            tvAmount = itemView.findViewById(R.id.tvTotalAmount);
//            tvInfoTv = itemView.findViewById(R.id.tvHistoryTvNum); // Pastikan ID ini ada di XML
        }
    }
}