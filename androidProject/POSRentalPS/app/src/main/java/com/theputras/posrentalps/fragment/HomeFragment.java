package com.theputras.posrentalps.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.os.Handler;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.theputras.posrentalps.R;
import com.theputras.posrentalps.StrukActivity;
import com.theputras.posrentalps.adapter.CartAdapter;
import com.theputras.posrentalps.adapter.PaketAdapter;
import com.theputras.posrentalps.adapter.TvGridAdapter;
import com.theputras.posrentalps.api.ApiClient;
import com.theputras.posrentalps.api.ApiService;
import com.theputras.posrentalps.model.ApiResponse;
import com.theputras.posrentalps.model.PaketSewa;
import com.theputras.posrentalps.model.TransactionItem;
import com.theputras.posrentalps.model.TransactionRequest;
import com.theputras.posrentalps.model.Tv;
import com.theputras.posrentalps.model.TvResponse;
import com.theputras.posrentalps.utils.CartManager;
import java.util.Collections; // Tambahkan ini buat sorting
import java.util.Comparator;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private EditText etSearch;
    private RecyclerView recyclerTv;
    private TvGridAdapter adapter;
    private List<PaketSewa> allPakets = new ArrayList<>();
    private ExtendedFloatingActionButton fabCart;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView badge;
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerTv = view.findViewById(R.id.recyclerTvGrid);
        fabCart = view.findViewById(R.id.fabCart);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        badge = view.findViewById(R.id.tvCartCount);

        recyclerTv.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        etSearch = view.findViewById(R.id.etSearchTv);
        setupSearchListener();


        fetchMasterData();

        swipeRefreshLayout.setOnRefreshListener(this::fetchTvs);

        // LOGIC BARU: BUKA BOTTOM SHEET KERANJANG
        fabCart.setOnClickListener(v -> {
            if (!CartManager.getInstance().getDisplayList().isEmpty()) {
                showCartBottomSheet();
            } else {
                Toast.makeText(requireContext(), "Keranjang Kosong", Toast.LENGTH_SHORT).show();
            }
        });

        updateCartUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCartUI();
        fetchTvs();
    }

    private void updateCartUI() {
        if (fabCart == null) return;

        int count = CartManager.getInstance().getDisplayList().size();

        // Hapus variable 'total' kalau tidak dipakai di text tombol
        // int total = CartManager.getInstance().getTotalPrice();

        if (count > 0) {
            // 1. UBAH JADI BULAT (Hapus setText & ganti extend jadi shrink)
            if (fabCart.isExtended()) {
                fabCart.shrink(); // <-- INI YANG BIKIN JADI BULAT
            }

            // 2. TAMPILKAN TOMBOL
            if (fabCart.getVisibility() != View.VISIBLE) {
                fabCart.setVisibility(View.VISIBLE);
                fabCart.setAlpha(1f);
                fabCart.setScaleX(1f);
                fabCart.setScaleY(1f);
            }

            // 3. UPDATE BADGE MERAH
            if (badge != null) {
                badge.setVisibility(View.VISIBLE);
                badge.setText(String.valueOf(count));
            }
        } else {
            // SEMBUNYIKAN
            fabCart.setVisibility(View.GONE);
            if (badge != null) {
                badge.setVisibility(View.GONE);
            }
        }
    }

    private void fetchMasterData() {
        ApiClient.getClient().create(ApiService.class).getPakets().enqueue(new Callback<List<PaketSewa>>() {
            @Override
            public void onResponse(Call<List<PaketSewa>> call, Response<List<PaketSewa>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allPakets = response.body();
                    fetchTvs();
                }
            }
            @Override public void onFailure(Call<List<PaketSewa>> c, Throwable t) {}
        });
    }

    private void fetchTvs() {
        if (swipeRefreshLayout != null && !swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }

        ApiClient.getClient().create(ApiService.class).getAvailableTvs().enqueue(new Callback<TvResponse>() {
            @Override
            public void onResponse(Call<TvResponse> call, Response<TvResponse> response) {
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    // Update Adapter dengan data TV terbaru
                    adapter = new TvGridAdapter(requireContext(), response.body().getData(), tv -> {
                        // Logic Click hanya akan jalan jika status TV Available (diatur di Adapter)
                        showPaketDialog(tv);
                    });
                    recyclerTv.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<TvResponse> c, Throwable t) {
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(requireContext(), "Koneksi Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPaketDialog(Tv tv) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.layout_pilih_paket, null);

        TextView title = view.findViewById(R.id.tvTitleSheet);
        RecyclerView recyclerPaket = view.findViewById(R.id.recyclerPaket);

        String consoleTv = (tv.getJenisConsole() != null) ? tv.getJenisConsole().getNamaConsole() : "";
        title.setText("Sewa " + consoleTv);

        // 1. FILTER DAFTAR PAKET
        List<PaketSewa> filtered = new ArrayList<>(allPakets);

        // 2. SORTING: URUTKAN DARI HARGA TERMURAH (Solusi "Menit Ngaco")
        Collections.sort(filtered, new Comparator<PaketSewa>() {
            @Override
            public int compare(PaketSewa p1, PaketSewa p2) {
                return Integer.compare(p1.harga, p2.harga);
            }
        });

        // 3. GRID 2 KOLOM (Solusi "Bikin Row2")
        recyclerPaket.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        PaketAdapter paketAdapter = new PaketAdapter(filtered, selectedPaket -> {
            boolean isExist = false;
            for (CartManager.CartDisplay item : CartManager.getInstance().getDisplayList()) {
                if (item.request.tvId == tv.getId()) {
                    isExist = true;
                    break;
                }
            }

            if (isExist) {
                Toast.makeText(requireContext(), "TV ini sudah ada di keranjang!", Toast.LENGTH_SHORT).show();
            } else {
                TransactionRequest req = new TransactionRequest("Guest", tv.getId(), selectedPaket.idPaket, selectedPaket.harga);
                CartManager.getInstance().addItem(req, tv.getNomorTv() + " - " + selectedPaket.namaPaket, selectedPaket.harga, 1);

                updateCartUI();
                Toast.makeText(requireContext(), "Berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                dialog.dismiss();

                if (adapter != null) adapter.notifyDataSetChanged();
            }
        });

        recyclerPaket.setAdapter(paketAdapter);
        dialog.setContentView(view);

        setupFullHeight(dialog);
        dialog.show();
    }

    // --- FITUR BARU: BOTTOM SHEET KERANJANG & BAYAR ---
    private void showCartBottomSheet() {
        BottomSheetDialog cartDialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.layout_cart_bottom_sheet, null);

        RecyclerView recyclerCart = view.findViewById(R.id.recyclerCart);
        TextView tvTotal = view.findViewById(R.id.tvTotalCart);

        // VIEW BARU
        TextInputEditText etNama = view.findViewById(R.id.etNamaPenyewa);
        RadioGroup rgPayment = view.findViewById(R.id.rgPaymentMethod);
        TextInputEditText etCash = view.findViewById(R.id.etCash);
        View btnPay = view.findViewById(R.id.btnProcessPay);

        recyclerCart.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Update Total
        int totalHarga = CartManager.getInstance().getTotalPrice();
        tvTotal.setText("Rp " + String.format("%,d", totalHarga));

        // Logic Radio Button (Optional: Sembunyikan input cash kalau QRIS)
        rgPayment.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbQris) {
                etCash.setText(String.valueOf(totalHarga)); // Auto isi pas
                etCash.setEnabled(false); // Disable edit
            } else {
                etCash.setText("");
                etCash.setEnabled(true);
                etCash.requestFocus();
            }
        });

        CartAdapter cartAdapter = new CartAdapter(CartManager.getInstance().getDisplayList(), position -> {
            CartManager.getInstance().removeItem(position);
            int newTotal = CartManager.getInstance().getTotalPrice();
            tvTotal.setText("Rp " + String.format("%,d", newTotal));
            updateCartUI();
            if (adapter != null) adapter.notifyDataSetChanged();
            if (CartManager.getInstance().getDisplayList().isEmpty()) {
                cartDialog.dismiss();
            }
        });
        recyclerCart.setAdapter(cartAdapter);

        btnPay.setOnClickListener(v -> {
            // 1. Validasi Nama
            String nama = etNama.getText().toString().trim();
            if (nama.isEmpty()) {
                etNama.setError("Wajib diisi!");
                return;
            }

            // 2. Validasi Pembayaran
            String cashStr = etCash.getText().toString();
            int grandTotal = CartManager.getInstance().getTotalPrice();
            int cash = cashStr.isEmpty() ? 0 : Integer.parseInt(cashStr);

            // Cek Metode
            String metode = (rgPayment.getCheckedRadioButtonId() == R.id.rbQris) ? "QRIS" : "TUNAI";

            // Validasi uang (Hanya kalau Tunai)
            if (metode.equals("TUNAI") && cash < grandTotal) {
                etCash.setError("Uang kurang!");
                return;
            }

            // Kalau QRIS, anggap uang pas jika kosong
            if (metode.equals("QRIS")) cash = grandTotal;

            // Proses
            processTransaction(nama, cash, metode, cartDialog);
        });

        cartDialog.setContentView(view);
        setupFullHeight(cartDialog);
        cartDialog.show();
    }

    private void setupFullHeight(BottomSheetDialog bottomSheetDialog) {
        bottomSheetDialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();

                // Set tinggi jadi Full Layar (Match Parent)
                if (layoutParams != null) {
                    layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
                    bottomSheet.setLayoutParams(layoutParams);
                }

                // Paksa status Expanded
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true); // Biar gak bisa di-collapse setengah
            }
        });
    }

    private void setupSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Setiap kali user ngetik, HAPUS rencana pencarian sebelumnya
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Buat rencana pencarian baru
                searchRunnable = new Runnable() {
                    @Override
                    public void run() {
                        // Kodingan ini baru jalan kalau user diem selama 500ms
                        if (adapter != null) {
                            adapter.filter(s.toString());
                        }
                    }
                };

                // JALANKAN SETELAH 500ms (Setengah Detik)
                // Ini yang bikin searchnya "tv01" langsung, bukan "t"-"v"-"0"-"1"
                searchHandler.postDelayed(searchRunnable, 500);
            }
        });
    }

    private void processTransaction(String namaPenyewa, int cashGiven, BottomSheetDialog dialog) {
        List<CartManager.CartDisplay> items = new ArrayList<>(CartManager.getInstance().getDisplayList());
        if (items.isEmpty()) return;

        // Total item yang harus diproses
        final int totalItems = items.size();
        final int[] processedCount = {0}; // Counter sukses

        // Loop semua item di keranjang
        for (CartManager.CartDisplay item : items) {

            // Update nama & uang di request masing-masing item
            item.request.namaPenyewa = namaPenyewa;
            // Tips: Uang bayar bisa dibagi rata atau diset full di item pertama,
            // tapi paling aman diset sesuai harga item biar lunas per item.
            // Atau kalau API support 'uang_bayar' global, kirim cashGiven.
            // Disini kita asumsi backend hitung kembalian per transaksi.
            item.request.uangBayar = item.price;

            ApiClient.getClient().create(ApiService.class).saveTransaction(item.request).enqueue(new Callback<ApiResponse<TransactionItem>>() {
                @Override
                public void onResponse(Call<ApiResponse<TransactionItem>> call, Response<ApiResponse<TransactionItem>> response) {
                    // Tambah counter setiap selesai request
                    processedCount[0]++;

                    // Cek jika semua item sudah diproses
                    if (processedCount[0] == totalItems) {
                        finishAllTransactions(namaPenyewa, cashGiven, dialog);
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<TransactionItem>> c, Throwable t) {
                    processedCount[0]++;
                    Toast.makeText(requireContext(), "Gagal 1 Item: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                    // Tetap lanjut finish jika ini item terakhir
                    if (processedCount[0] == totalItems) {
                        finishAllTransactions(namaPenyewa, cashGiven, dialog);
                    }
                }
            });
        }
    }

    private void finishAllTransactions(String namaPenyewa, int cashGiven, BottomSheetDialog dialog) {
        // Pindah ke Struk Activity setelah SEMUA request selesai
        Intent intent = new Intent(requireContext(), StrukActivity.class);
        intent.putExtra("CASH_GIVEN", cashGiven);
        intent.putExtra("CUSTOMER_NAME", namaPenyewa);
        startActivity(intent);
        dialog.dismiss();
    }
}