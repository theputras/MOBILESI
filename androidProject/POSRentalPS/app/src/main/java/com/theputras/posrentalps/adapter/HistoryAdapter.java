package com.theputras.posrentalps.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.theputras.posrentalps.R;
import com.theputras.posrentalps.model.TransactionItem;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<TransactionItem> list;

    public HistoryAdapter(List<TransactionItem> list) {
        this.list = list;
    }

    public void updateList(List<TransactionItem> newList) {
        this.list = newList;
        notifyDataSetChanged();
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
        holder.tvName.setText(item.namaPenyewa);

        String consoleName = (item.console != null) ? item.console.getNamaConsole() : "Unknown";
        holder.tvDetails.setText(consoleName + " | TV " + item.nomorTv + " | " + item.durasiJam + " Jam");
        holder.tvPrice.setText("Rp " + item.totalTagihan);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDetails, tvPrice;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRenterName);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}