package com.theputras.posrentalps.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.theputras.posrentalps.R;
import com.theputras.posrentalps.model.Tv;
import java.util.List;

public class TvAdapter extends RecyclerView.Adapter<TvAdapter.TvViewHolder> {

    private Context context;
    private List<Tv> tvList;
    private OnTvClickListener listener;

    // Interface buat handle klik
    public interface OnTvClickListener {
        void onTvClick(Tv tv);
    }

    public TvAdapter(Context context, List<Tv> tvList, OnTvClickListener listener) {
        this.context = context;
        this.tvList = tvList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TvViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tv, parent, false);
        return new TvViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TvViewHolder holder, int position) {
        Tv tv = tvList.get(position);

        holder.txtNomor.setText(tv.getNomorTv());

        // Ambil nama console dari nested object
        if (tv.getJenisConsole() != null) {
            holder.txtConsole.setText(tv.getJenisConsole().getNamaConsole());
        }

        // Klik Item
        holder.itemView.setOnClickListener(v -> listener.onTvClick(tv));
    }

    @Override
    public int getItemCount() {
        return tvList.size();
    }

    public static class TvViewHolder extends RecyclerView.ViewHolder {
        TextView txtNomor, txtConsole;

        public TvViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNomor = itemView.findViewById(R.id.tvNomor);
            txtConsole = itemView.findViewById(R.id.tvConsole);
        }
    }
}