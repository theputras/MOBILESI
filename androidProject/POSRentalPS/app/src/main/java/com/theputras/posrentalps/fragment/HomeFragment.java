package com.theputras.posrentalps.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.theputras.posrentalps.PaymentActivity;
import com.google.android.material.button.MaterialButton;
import java.util.Collections;
import java.util.Comparator;
import com.theputras.posrentalps.R;
import com.theputras.posrentalps.StrukActivity;
import com.theputras.posrentalps.adapter.CartAdapter;
import com.theputras.posrentalps.adapter.PaketAdapter;
import com.theputras.posrentalps.adapter.TvGridAdapter;
import com.theputras.posrentalps.api.ApiClient;
import com.theputras.posrentalps.api.ApiService;
import com.theputras.posrentalps.databinding.FragmentHomeBinding;
import com.theputras.posrentalps.databinding.LayoutCartBottomSheetBinding;
import com.theputras.posrentalps.model.ApiResponse;
import com.theputras.posrentalps.model.PaketSewa;
import com.theputras.posrentalps.model.RiwayatTransaksi;
import com.theputras.posrentalps.model.TransactionRequest;
import com.theputras.posrentalps.model.Tv;
import com.theputras.posrentalps.model.TvResponse;
import com.theputras.posrentalps.utils.CartManager;

import android.widget.RadioGroup;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ApiService apiService;
    private TvGridAdapter adapter;
    private List<Tv> tvList = new ArrayList<>();
    private List<Tv> filteredList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiClient.getClient().create(ApiService.class);

        setupRecyclerView();
        setupSearch();
        setupSwipeRefresh();
        setupCartButton();

        loadTvData();
        updateCartUI();
    }

    private void setupRecyclerView() {
        binding.recyclerTvGrid.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new TvGridAdapter(requireContext(), filteredList, this::showPaketDialog);
        binding.recyclerTvGrid.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.etSearchTv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTv(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadTvData);
    }

    private void setupCartButton() {
        binding.fabCart.setOnClickListener(v -> showCartBottomSheet());
    }

    private void loadTvData() {
        binding.swipeRefreshLayout.setRefreshing(true);
        apiService.getAvailableTvs().enqueue(new Callback<TvResponse>() {
            @Override
            public void onResponse(Call<TvResponse> call, Response<TvResponse> response) {
                binding.swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    TvResponse tvResponse = response.body();
                    tvList.clear();
                    if(tvResponse.getData() != null) {
                        tvList.addAll(tvResponse.getData());
                    }
                    filterTv(binding.etSearchTv.getText().toString());
                } else {
                    Toast.makeText(getContext(), "Gagal memuat data TV", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TvResponse> call, Throwable t) {
                binding.swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterTv(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(tvList);
        } else {
            for (Tv tv : tvList) {
                if (tv.getNomorTv().toLowerCase().contains(query.toLowerCase()) ||
                        (tv.getJenisConsole() != null && tv.getJenisConsole().getNamaConsole().toLowerCase().contains(query.toLowerCase()))) {
                    filteredList.add(tv);
                }
            }
        }
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void showPaketDialog(Tv tv) {
        if (!"available".equalsIgnoreCase(tv.getStatus())) {
            Toast.makeText(getContext(), "TV ini sedang dipakai / maintenance", Toast.LENGTH_SHORT).show();
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.layout_pilih_paket, null);
        dialog.setContentView(dialogView);

        RecyclerView recyclerPaket = dialogView.findViewById(R.id.recyclerPaket);
        recyclerPaket.setLayoutManager(new GridLayoutManager(getContext(), 2));

        apiService.getPakets().enqueue(new Callback<List<PaketSewa>>() {
            @Override
            public void onResponse(Call<List<PaketSewa>> call, Response<List<PaketSewa>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    // 1. Definisikan variabel 'allPakets' dari response server
                    List<PaketSewa> allPakets = response.body();
                    List<PaketSewa> filteredPakets = new ArrayList<>();

                    // 2. Ambil ID Console dari TV (Pakai akses langsung field .idConsole)
                    int targetConsoleId = 0;
                    if (tv.getJenisConsole() != null) {
                        // FIX: Langsung panggil .idConsole (karena di model public dan gak ada getter)
                        targetConsoleId = tv.getJenisConsole().idConsole;
                    }

                    // 3. Filter Paket
                    for (PaketSewa p : allPakets) {
                        // Masukkan paket yang ID Console-nya SAMA
                        if (p.idConsole == targetConsoleId) {
                            filteredPakets.add(p);
                        }
                    }

                    // 4. Sorting (Urutkan dari durasi terpendek)
                    Collections.sort(filteredPakets, new Comparator<PaketSewa>() {
                        @Override
                        public int compare(PaketSewa p1, PaketSewa p2) {
                            return Integer.compare(p1.durasiMenit, p2.durasiMenit);
                        }
                    });

                    // 5. Masukkan list yang SUDAH DI-FILTER ke Adapter
                    PaketAdapter paketAdapter = new PaketAdapter(filteredPakets, selectedPaket -> {
                        addToCart(tv, selectedPaket);
                        dialog.dismiss();
                    });
                    recyclerPaket.setAdapter(paketAdapter);

                } else {
                    Toast.makeText(getContext(), "Gagal memuat paket", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PaketSewa>> call, Throwable t) {
                Toast.makeText(getContext(), "Error koneksi paket", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    // --- FIX BUG 1: UPDATE UI SETELAH ADD CART ---
    private void addToCart(Tv tv, PaketSewa paket) {
        boolean isExist = false;

        for (CartManager.CartDisplay item : CartManager.getInstance().getCartItems()) {
            if (item.getTv().getId() == tv.getId()) {
                isExist = true;
                break;
            }
        }

        if (isExist) {
            Toast.makeText(getContext(), "TV ini sudah ada di keranjang!", Toast.LENGTH_SHORT).show();
        } else {
            CartManager.getInstance().addItem(tv, paket);
            Toast.makeText(getContext(), "Berhasil masuk keranjang", Toast.LENGTH_SHORT).show();

            updateCartUI(); // Update tombol keranjang

            // TAMBAHAN PENTING: Refresh Adapter TV biar warnanya jadi 'Dipilih' (Orange)
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void showCartBottomSheet() {
        // 1. Setup Dialog & View
        BottomSheetDialog cartDialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        // Kita pakai cara manual inflate aja biar cocok sama kodingan bawahmu
        View view = getLayoutInflater().inflate(R.layout.layout_cart_bottom_sheet, null);
        cartDialog.setContentView(view);

        // 2. Binding Views (Pakai 'view' yang sudah di-set ke dialog)
        RecyclerView recyclerCart = view.findViewById(R.id.recyclerCartItems);
        TextView tvTotal = view.findViewById(R.id.tvTotalCartPrice);
        com.google.android.material.textfield.TextInputEditText etNama = view.findViewById(R.id.etNamaPenyewa);
        com.google.android.material.textfield.TextInputEditText etCash = view.findViewById(R.id.etCash);
        RadioGroup rgPayment = view.findViewById(R.id.rgPaymentMethod);
        com.google.android.material.button.MaterialButton btnCheckout = view.findViewById(R.id.btnCheckout);

        recyclerCart.setLayoutManager(new LinearLayoutManager(getContext()));

        // 3. Cek Kosong
        if (CartManager.getInstance().getCartItems().isEmpty()) {
            Toast.makeText(getContext(), "Keranjang Kosong", Toast.LENGTH_SHORT).show();
            cartDialog.dismiss();
            return;
        }

        // 4. Setup Adapter & Listener Update Harga
        CartAdapter cartAdapter = new CartAdapter(CartManager.getInstance().getCartItems(), new CartAdapter.CartListener() {
            @Override
            public void onCartUpdated() {
                int newTotal = CartManager.getInstance().getTotalPrice();
                tvTotal.setText("Rp " + String.format("%,d", newTotal).replace(',', '.'));
                updateCartUI(); // Update badge/total di fragment utama

                // Update warna Grid TV di halaman utama (jika ada variabel adapter tv)
                if (adapter != null) adapter.notifyDataSetChanged();

                // Kalau habis dihapus jadi kosong, tutup dialog
                if (CartManager.getInstance().getCartItems().isEmpty()) {
                    cartDialog.dismiss();
                }
            }
        });
        recyclerCart.setAdapter(cartAdapter);

        // Set Total Awal
        int total = CartManager.getInstance().getTotalPrice();
        tvTotal.setText("Rp " + String.format("%,d", total).replace(',', '.'));

        // 5. Logic QRIS Bypass
        rgPayment.setOnCheckedChangeListener((group, checkedId) -> {
            int currentTotal = CartManager.getInstance().getTotalPrice();
            if (checkedId == R.id.rbQris) {
                etCash.setText(String.valueOf(currentTotal));
                etCash.setEnabled(false);
            } else {
                etCash.setText("");
                etCash.setEnabled(true);
                etCash.requestFocus();
            }
        });

        // 6. Tombol Checkout
        btnCheckout.setOnClickListener(v -> {
            String nama = etNama.getText().toString().trim();
            String cashStr = etCash.getText().toString().trim();

            if (TextUtils.isEmpty(nama)) {
                etNama.setError("Wajib diisi");
                return;
            }
            if (TextUtils.isEmpty(cashStr)) {
                etCash.setError("Isi nominal");
                return;
            }

            int uangBayar;
            try {
                uangBayar = Integer.parseInt(cashStr);
            } catch (NumberFormatException e) {
                etCash.setError("Format salah");
                return;
            }

            int totalTagihan = CartManager.getInstance().getTotalPrice();

            if (uangBayar < totalTagihan) {
                Toast.makeText(getContext(), "Uang kurang!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Siapkan Request
            String metode = (rgPayment.getCheckedRadioButtonId() == R.id.rbQris) ? "QRIS" : "TUNAI";
            List<TransactionRequest.ItemDetail> itemsToSend = new ArrayList<>();

            for (CartManager.CartDisplay item : CartManager.getInstance().getCartItems()) {
                if (item.getTv() != null && item.getPaket() != null) {
                    itemsToSend.add(new TransactionRequest.ItemDetail(
                            item.getTv().getId(),
                            item.getPaket().idPaket
                    ));
                }
            }

            TransactionRequest request = new TransactionRequest(nama, uangBayar, metode, itemsToSend);

            // Loading State
            btnCheckout.setEnabled(false);
            btnCheckout.setText("MEMPROSES...");

            // API CALL
            apiService.saveTransaction(request).enqueue(new Callback<ApiResponse<RiwayatTransaksi>>() {
                @Override
                public void onResponse(Call<ApiResponse<RiwayatTransaksi>> call, Response<ApiResponse<RiwayatTransaksi>> response) {
                    btnCheckout.setText("BAYAR SEKARANG");
                    btnCheckout.setEnabled(true);

                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().success) {
                            Toast.makeText(getContext(), "Transaksi Berhasil!", Toast.LENGTH_LONG).show();

                            CartManager.getInstance().clearCart();
                            updateCartUI();
                            if (adapter != null) adapter.notifyDataSetChanged();

                            cartDialog.dismiss();

                            // Pindah ke Struk (Pass ID Transaksi)
                            if (response.body().data != null) {
                                Intent intent = new Intent(getContext(), StrukActivity.class);
                                intent.putExtra("TRANSACTION_ID", response.body().data.getIdTransaksi());
                                startActivity(intent);
                            }
                        } else {
                            Toast.makeText(getContext(), response.body().message, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Gagal: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<RiwayatTransaksi>> call, Throwable t) {
                    btnCheckout.setText("BAYAR SEKARANG");
                    btnCheckout.setEnabled(true);
                    Toast.makeText(getContext(), "Error Koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // 7. Logic Full Screen (Swipe Up)
        cartDialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);

            if (bottomSheet != null) {
                // Set tinggi bottom sheet jadi setinggi layar HP
                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                layoutParams.height = getResources().getDisplayMetrics().heightPixels;
                bottomSheet.setLayoutParams(layoutParams);

                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);

                // Awal muncul: Collapsed (setengah/sesuai peekHeight default)
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                // Opsional: Atur tinggi intial biar enak dilihat (misal 60% layar)
                 behavior.setPeekHeight(getResources().getDisplayMetrics().heightPixels / 2);

                behavior.setSkipCollapsed(true);
            }
        });

        cartDialog.show();
    }
    private void updateCartUI() {
        int count = CartManager.getInstance().getCartItems().size();

        if (count > 0) {
            binding.fabCart.setVisibility(View.VISIBLE);
            binding.tvCartCount.setVisibility(View.VISIBLE);
            binding.tvCartCount.setText(String.valueOf(count));
        } else {
            binding.fabCart.setVisibility(View.GONE);
            binding.tvCartCount.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCartUI();
        loadTvData();
    }
}