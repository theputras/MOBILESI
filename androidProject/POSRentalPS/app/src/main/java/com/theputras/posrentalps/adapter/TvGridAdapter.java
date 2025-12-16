package com.theputras.posrentalps.adapter;

import android.content.Context;
import android.util.Log; // Tambah import Log
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.theputras.posrentalps.R;
import com.theputras.posrentalps.model.Tv;
import java.util.List;

public class TvGridAdapter extends RecyclerView.Adapter<TvGridAdapter.ViewHolder> {

    private Context context;
    private List<Tv> tvList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Tv tv);
    }

    public TvGridAdapter(Context context, List<Tv> tvList, OnItemClickListener listener) {
        this.context = context;
        this.tvList = tvList;
        this.listener = listener;
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

        holder.tvNomor.setText(tv.getNomorTv());

        if (tv.getJenisConsole() != null) {
            holder.tvConsole.setText(tv.getJenisConsole().getNamaConsole());
        } else {
            holder.tvConsole.setText("-");
        }

        // Cek status null safety
        String status = tv.getStatus() != null ? tv.getStatus() : "Unknown";
        holder.tvStatus.setText(status);

        // --- BAGIAN PENTING: CLICK LISTENER ---
        holder.itemView.setOnClickListener(v -> {
            // Log ke System agar kita tau item diklik
            Log.d("ADAPTER_CLICK", "Item diklik: " + tv.getNomorTv() + ", Status: " + status);

            // Panggil listener ke MainActivity
            if (listener != null) {
                listener.onItemClick(tv);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tvList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNomor, tvConsole, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomor = itemView.findViewById(R.id.tvNomor);
            tvConsole = itemView.findViewById(R.id.tvConsoleType);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}