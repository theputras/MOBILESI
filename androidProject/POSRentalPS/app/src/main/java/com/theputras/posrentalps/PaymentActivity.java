package com.theputras.posrentalps;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

// Imports
import com.theputras.posrentalps.databinding.ActivityPaymentBinding;
import com.theputras.posrentalps.api.ApiClient;
import com.theputras.posrentalps.api.ApiService;
import com.theputras.posrentalps.model.ApiResponse;
import com.theputras.posrentalps.model.RiwayatTransaksi;
import com.theputras.posrentalps.model.TransactionRequest;
import com.theputras.posrentalps.utils.CartManager;
import com.theputras.posrentalps.adapter.CartAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    private ActivityPaymentBinding binding;
    private ApiService apiService;
    private CartAdapter cartAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Pastikan ApiClient logging aktif jika masih error
        apiService = ApiClient.getClient().create(ApiService.class);

        setupUI();
    }

    private void setupUI() {
        // 1. SETUP RECYCLER VIEW
        binding.recyclerCart.setHasFixedSize(true);
        binding.recyclerCart.setLayoutManager(new LinearLayoutManager(this));

        List<CartManager.CartDisplay> items = CartManager.getInstance().getCartItems();
        // Listener null karena di halaman payment mungkin item tidak bisa dihapus (read-only),
        // atau kamu bisa buat listener baru jika ingin fitur hapus di sini.
        cartAdapter = new CartAdapter(items, null);
        binding.recyclerCart.setAdapter(cartAdapter);

        // 2. SETUP TOTAL
        int total = CartManager.getInstance().getTotalPrice();
        binding.tvTotalCart.setText("Rp " + String.format("%,d", total).replace(',', '.'));

        // 3. LOGIC QRIS BYPASS
        binding.rgPaymentMethod.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbQris) {
                binding.etCash.setText(String.valueOf(total));
                binding.etCash.setEnabled(false);
                binding.etCash.setFocusable(false);
            } else {
                binding.etCash.setText("");
                binding.etCash.setEnabled(true);
                binding.etCash.setFocusableInTouchMode(true);
                binding.etCash.requestFocus();
            }
        });

        // 4. TOMBOL BAYAR
        binding.btnProcessPay.setOnClickListener(v -> prosesTransaksi());
    }

    private void prosesTransaksi() {
        String nama = binding.etNamaPenyewa.getText().toString().trim();
        String uangBayarStr = binding.etCash.getText().toString().trim();

        if (TextUtils.isEmpty(nama)) {
            binding.etNamaPenyewa.setError("Wajib diisi");
            binding.etNamaPenyewa.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(uangBayarStr)) {
            binding.etCash.setError("Isi nominal");
            binding.etCash.requestFocus();
            return;
        }

        int uangBayar;
        try {
            uangBayar = Integer.parseInt(uangBayarStr);
        } catch (NumberFormatException e) {
            binding.etCash.setError("Format angka salah");
            return;
        }

        int totalTagihan = CartManager.getInstance().getTotalPrice();

        if (uangBayar < totalTagihan) {
            Toast.makeText(this, "Uang kurang!", Toast.LENGTH_SHORT).show();
            return;
        }

        String metodePembayaran = binding.rbQris.isChecked() ? "QRIS" : "TUNAI";

        List<CartManager.CartDisplay> cartItems = CartManager.getInstance().getCartItems();
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Keranjang kosong!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<TransactionRequest.ItemDetail> itemsToSend = new ArrayList<>();
        for (CartManager.CartDisplay cart : cartItems) {
            // Validasi data null sebelum kirim
            if (cart.getTv() != null && cart.getPaket() != null) {
                itemsToSend.add(new TransactionRequest.ItemDetail(
                        cart.getTv().getId(),
                        cart.getPaket().idPaket
                ));
            }
        }

        TransactionRequest request = new TransactionRequest(
                nama,
                uangBayar,
                metodePembayaran,
                itemsToSend
        );

        setLoading(true);

        apiService.saveTransaction(request).enqueue(new Callback<ApiResponse<RiwayatTransaksi>>() {
            @Override
            public void onResponse(Call<ApiResponse<RiwayatTransaksi>> call, Response<ApiResponse<RiwayatTransaksi>> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<RiwayatTransaksi> apiResponse = response.body();

                    if (apiResponse.success) {
                        Toast.makeText(PaymentActivity.this, "Sukses!", Toast.LENGTH_LONG).show();
                        CartManager.getInstance().clearCart();

                        if (apiResponse.data != null) {
                            Intent intent = new Intent(PaymentActivity.this, StrukActivity.class);
                            // ID ini harus sesuai dengan yang dikirim backend (sekarang id_transaksi)
                            intent.putExtra("TRANSACTION_ID", apiResponse.data.getIdTransaksi());
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(PaymentActivity.this, "Sukses tapi data struk kosong", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // FIX: Handle pesan null
                        String msg = apiResponse.message != null ? apiResponse.message : "Gagal: Pesan tidak diketahui";
                        Toast.makeText(PaymentActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Jika error dari server (400/404/500)
                    String errorMsg = "Error Server: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            // Mencoba membaca error body jika ada
                            // errorMsg += " " + response.errorBody().string(); // Hati-hati ini bisa exception di main thread
                        }
                    } catch (Exception e) {}
                    Toast.makeText(PaymentActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<RiwayatTransaksi>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(PaymentActivity.this, "Koneksi Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            binding.btnProcessPay.setText("MEMPROSES...");
            binding.btnProcessPay.setEnabled(false);
        } else {
            binding.btnProcessPay.setText("BAYAR SEKARANG");
            binding.btnProcessPay.setEnabled(true);
        }
    }
}