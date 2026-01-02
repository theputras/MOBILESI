package com.theputras.posrentalps;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.theputras.posrentalps.adapter.CartAdapter;
import com.theputras.posrentalps.api.ApiClient;
import com.theputras.posrentalps.api.ApiService;
import com.theputras.posrentalps.model.ApiResponse;
import com.theputras.posrentalps.model.TransactionItem;
import com.theputras.posrentalps.model.TransactionRequest;
import com.theputras.posrentalps.utils.CartManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    private TextView tvTotal;
    private EditText etCustName, etUangBayar;
    private TextInputLayout inputLayoutUang;
    private RadioGroup radioGroupPayment;
    private RadioButton rbTunai, rbQris;
    private MaterialButton btnConfirm;
    private ProgressBar loading;

    // Variabel untuk melacak progress looping request
    private int processedCount = 0;
    private int totalRequestQueue = 0;
    private boolean hasError = false;
    private CartAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Binding Views
        RecyclerView recycler = findViewById(R.id.recyclerCartItems);
        tvTotal = findViewById(R.id.tvTotalBill);
        etCustName = findViewById(R.id.etCustomerName);
        etUangBayar = findViewById(R.id.etUangBayar);
        inputLayoutUang = findViewById(R.id.inputLayoutUang);
        radioGroupPayment = findViewById(R.id.radioGroupPayment);
        rbTunai = findViewById(R.id.rbTunai);
        rbQris = findViewById(R.id.rbQris);
        btnConfirm = findViewById(R.id.btnProcessPayment);
        loading = findViewById(R.id.progressBar);

        // Setup RecyclerView
        recycler.setLayoutManager(new LinearLayoutManager(this));

        // Pass listener "this::updateTotalUI" atau lambda
        adapter = new CartAdapter(CartManager.getInstance().getDisplayList(), new CartAdapter.CartListener() {
            @Override
            public void onDeleteClick(int position) {
                // 1. Hapus data dari manager
                CartManager.getInstance().removeItem(position);

                // 2. Beritahu adapter ada yang hilang
                adapter.notifyItemRemoved(position);

                // 3. HITUNG ULANG TOTAL DI SINI (Pengganti onCartUpdated)
                refreshTotal();

                // Cek kalau kosong, tutup activity
                if (CartManager.getInstance().getDisplayList().isEmpty()) {
                    finish();
                }
            }
        });

        recycler.setAdapter(adapter);

        // Panggil pertama kali
        refreshTotal();

        // Tampilkan Total
        int total = CartManager.getInstance().getTotalPrice();
        tvTotal.setText("Rp " + total);

        // Logic RadioButton (Sembunyikan Input Uang jika QRIS)
        radioGroupPayment.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbQris) {
                inputLayoutUang.setVisibility(View.GONE);
                etUangBayar.setText(String.valueOf(total)); // Otomatis lunas
            } else {
                inputLayoutUang.setVisibility(View.VISIBLE);
                etUangBayar.setText("");
            }
        });

        btnConfirm.setOnClickListener(v -> processCheckout(total));
    }

    private void refreshTotal() {
        int total = CartManager.getInstance().getTotalPrice();
        tvTotal.setText("Rp " + total);

        // Update juga nilai default QRIS kalau sedang terpilih
        if (rbQris.isChecked()) {
            etUangBayar.setText(String.valueOf(total));
        }

        // Kalau keranjang jadi kosong, bisa tutup activity (opsional)
        if (CartManager.getInstance().getDisplayList().isEmpty()) {
            Toast.makeText(this, "Keranjang kosong", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    private void processCheckout(int grandTotal) {
        String name = etCustName.getText().toString().trim();
        String uangStr = etUangBayar.getText().toString().trim();

        // 1. Validasi Input
        if (name.isEmpty()) {
            etCustName.setError("Nama wajib diisi");
            return;
        }

        int tempUangBayar = 0; // Ganti nama variabel lokal sementara
        if (rbTunai.isChecked()) {
            if (uangStr.isEmpty()) {
                etUangBayar.setError("Masukkan nominal uang");
                return;
            }
            tempUangBayar = Integer.parseInt(uangStr);
            if (tempUangBayar < grandTotal) {
                etUangBayar.setError("Uang kurang!");
                return;
            }
        } else {
            // Jika QRIS, anggap uang bayar pas sesuai tagihan
            tempUangBayar = grandTotal;
        }

        // --- SOLUSI ERROR ADA DISINI ---
        // Buat variabel FINAL agar bisa dibaca di dalam Callback
        final int finalUangBayar = tempUangBayar;
        // -------------------------------

        // Mulai Proses ke Server
        loading.setVisibility(View.VISIBLE);
        btnConfirm.setEnabled(false);
        hasError = false;
        processedCount = 0;

        // 1. Queue sekarang berdasarkan jumlah item unik (setiap item harus beda TV)
        totalRequestQueue = CartManager.getInstance().getDisplayList().size();

        ApiService api = ApiClient.getClient().create(ApiService.class);

        // 2. Looping kirim data per Item (Bukan per Qty)
        for (CartManager.CartDisplay item : CartManager.getInstance().getDisplayList()) {

            // Pastikan request object membawa data terbaru
            item.request.namaPenyewa = name;
            item.request.metodePembayaran = rbQris.isChecked() ? "QRIS" : "TUNAI";
            item.request.uangBayar = item.price; // Harga per paket TV tersebut
            android.util.Log.d("PAYMENT_DEBUG", "Mengirim TV ID: " + item.request.tvId);
            // Kirim request satu per satu untuk setiap TV yang dipilih
            api.saveTransaction(item.request).enqueue(new Callback<ApiResponse<TransactionItem>>() {
                @Override
                public void onResponse(Call<ApiResponse<TransactionItem>> call, Response<ApiResponse<TransactionItem>> response) {
                    if (!response.isSuccessful()) {
                        hasError = true;
                        // Ambil pesan error asli dari Laravel (Contoh: "TV tidak tersedia")
                        try {
                            String errorMsg = response.errorBody().string();
                            android.util.Log.e("API_ERROR", errorMsg);
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                    checkComplete(name, finalUangBayar);
                }

                @Override
                public void onFailure(Call<ApiResponse<TransactionItem>> call, Throwable t) {
                    hasError = true;
                    checkComplete(name, finalUangBayar);
                }
            });
        }
    }

    // Method sinkronisasi untuk cek apakah semua request sudah selesai
    private synchronized void checkComplete(String customerName, int totalCashGiven) {
        processedCount++;

        // Jika semua request sudah diproses (sukses/gagal)
        if (processedCount >= totalRequestQueue) {
            loading.setVisibility(View.GONE);

            if (!hasError) {
                // Sukses
                Intent intent = new Intent(PaymentActivity.this, StrukActivity.class);
                intent.putExtra("CUSTOMER_NAME", customerName);
                intent.putExtra("CASH_GIVEN", totalCashGiven); // Kirim uang total yang dikasih user
                startActivity(intent);
                finish();
            } else {
                btnConfirm.setEnabled(true);
                Toast.makeText(this, "Ada kesalahan saat memproses transaksi", Toast.LENGTH_LONG).show();
            }
        }
    }
}