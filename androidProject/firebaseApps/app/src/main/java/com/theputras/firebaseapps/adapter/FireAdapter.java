package com.theputras.firebaseapps.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.theputras.firebaseapps.R;
import com.theputras.firebaseapps.models.FireModel;

import java.util.List;

public class FireAdapter extends RecyclerView.Adapter<FireAdapter.ViewHolder> {

    private Context context;
    private List<FireModel> list;

    // Interface untuk klik listener
    private OnItemClickListener listener;

    public FireAdapter(Context context, List<FireModel> list) {
        this.context = context;
        this.list = list;
    }

    // Method untuk set listener dari Fragment
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FireModel model = list.get(position);

        holder.tvNama.setText(model.getNama());
        holder.tvNim.setText("NIM: " + model.getNim());
        holder.tvProdi.setText("Prodi: " + model.getProdi());

        // Saat item diklik, panggil listener di Fragment
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvNim, tvProdi;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tv_nama);
            tvNim = itemView.findViewById(R.id.tv_nim);
            tvProdi = itemView.findViewById(R.id.tv_prodi);
        }
    }

    // Interface
    public interface OnItemClickListener {
        void onItemClick(FireModel model);
    }
}