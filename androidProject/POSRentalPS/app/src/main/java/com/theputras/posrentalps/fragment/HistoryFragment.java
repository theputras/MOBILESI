package com.theputras.posrentalps.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.theputras.posrentalps.R;
import com.theputras.posrentalps.StrukActivity;
import com.theputras.posrentalps.adapter.HistoryAdapter;
import com.theputras.posrentalps.api.ApiClient;
import com.theputras.posrentalps.api.ApiService;
import com.theputras.posrentalps.model.ApiResponse;
import com.theputras.posrentalps.model.TransactionItem;
import com.theputras.posrentalps.model.TransactionRequest;
import com.theputras.posrentalps.utils.CartManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerHistory;
    private SwipeRefreshLayout swipeRefresh;
    private HistoryAdapter adapter;
    private LinearLayout layoutEmpty;
    private EditText etSearch;

    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etSearch = view.findViewById(R.id.etSearchHistory);
        recyclerHistory = view.findViewById(R.id.recyclerHistory);
        swipeRefresh = view.findViewById(R.id.swipeRefreshHistory);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);

        recyclerHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        fetchHistory();
        setupSearchListener();

        swipeRefresh.setOnRefreshListener(this::fetchHistory);
    }

    private void setupSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
            }
            @Override
            public void afterTextChanged(Editable s) {
                searchRunnable = () -> {
                    if (adapter != null) adapter.filter(s.toString());
                };
                searchHandler.postDelayed(searchRunnable, 500);
            }
        });
    }

    private void fetchHistory() {
        swipeRefresh.setRefreshing(true);

        ApiClient.getClient(requireContext()).create(ApiService.class).getHistory().enqueue(new Callback<ApiResponse<List<TransactionItem>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<TransactionItem>>> call, Response<ApiResponse<List<TransactionItem>>> response) {
                swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<TransactionItem> data = response.body().data;

                    if (data == null || data.isEmpty()) {
                        recyclerHistory.setVisibility(View.GONE);
                        layoutEmpty.setVisibility(View.VISIBLE);
                    } else {
                        recyclerHistory.setVisibility(View.VISIBLE);
                        layoutEmpty.setVisibility(View.GONE);

                        // Gunakan Adapter dengan Interface Klik
                        adapter = new HistoryAdapter(requireContext(), data, item -> {
                            // Saat item diklik, jalankan fungsi hack ini
                            openSafeStruk(item);
                            Log.d("HistoryFragment", "Item clicked: " + item.idTransaksi);
                        });
                        recyclerHistory.setAdapter(adapter);
                    }
                } else {
                    Toast.makeText(getContext(), "Gagal: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<TransactionItem>>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- INI TRIK SULAPNYA (YANG DIPERBAIKI STRINGNYA) ---
    private void openSafeStruk(TransactionItem item) {
        Intent intent = new Intent(requireContext(), StrukActivity.class);

        // Ambil Data TV & Console
        String nomorTv = item.getDisplayTv(); // Pakai helper yang tadi dibuat
        String namaConsole = item.getDisplayConsole();

        // Kirim Data Lewat Intent
        intent.putExtra("CUSTOMER_NAME", item.namaPenyewa);
        intent.putExtra("TOTAL_TAGIHAN", item.totalTagihan);
        intent.putExtra("TANGGAL", item.tanggalTransaksi);
        intent.putExtra("TRANSACTION_ID", String.valueOf(item.idTransaksi));

        // Kirim Nomor TV & Console untuk ditampilkan di Struk
        // StrukActivity akan merakitnya jadi: "Sewa TV [No] ([Console])"
        intent.putExtra("NOMOR_TV", nomorTv);

        // Kita bisa kirim nama console sebagai info durasi/paket sementara
        // atau biarkan StrukActivity yang format
        // (Di sini saya kirim nama console agar jelas)
        intent.putExtra("NAMA_CONSOLE", namaConsole);

        startActivity(intent);
    }
}