package com.theputras.posrentalps;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// --- IMPORTS PENTING ---
import com.theputras.posrentalps.databinding.ActivityStrukBinding;
import com.theputras.posrentalps.api.ApiClient;
import com.theputras.posrentalps.api.ApiService;
import com.theputras.posrentalps.model.ApiResponse;
import com.theputras.posrentalps.model.RiwayatTransaksi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StrukActivity extends AppCompatActivity {

    private ActivityStrukBinding binding;
    private ApiService apiService;
    private String transactionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup ViewBinding
        binding = ActivityStrukBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.getClient().create(ApiService.class);

        // Ambil ID Transaksi
        transactionId = getIntent().getStringExtra("TRANSACTION_ID");

        if (transactionId != null) {
            loadStrukData(transactionId);
        } else {
            Toast.makeText(this, "ID Transaksi tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Tombol Kembali
        binding.btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(StrukActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // Tombol Print (Sementara Toast dulu)
        binding.btnPrint.setOnClickListener(v -> {
            Toast.makeText(this, "Fitur Print Bluetooth sedang dikembangkan...", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadStrukData(String id) {
        // Bisa tambah loading state disini kalau mau

        apiService.getTransactionDetail(id).enqueue(new Callback<ApiResponse<RiwayatTransaksi>>() {
            @Override
            public void onResponse(Call<ApiResponse<RiwayatTransaksi>> call, Response<ApiResponse<RiwayatTransaksi>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<RiwayatTransaksi> apiResponse = response.body();
                    if (apiResponse.data != null) {
                        tampilkanData(apiResponse.data);
                    } else {
                        Toast.makeText(StrukActivity.this, "Data tidak ditemukan", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(StrukActivity.this, "Gagal memuat: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<RiwayatTransaksi>> call, Throwable t) {
                Toast.makeText(StrukActivity.this, "Error Koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void tampilkanData(RiwayatTransaksi trx) {
        // 1. Set Header Sesuai ID di XML kamu
        binding.tvStrukDate.setText(trx.getTanggalTransaksi());
        binding.tvStrukName.setText(trx.getNamaPenyewa()); // Bisa ditambah ID Transaksi kalau mau
        binding.tvIdTransaksi.setText("Bon " + trx.getIdTransaksi());
        // 2. Clear Container Item biar gak numpuk
        binding.containerItems.removeAllViews();

        // 3. LOOPING ITEM & Buat View secara Programmatic (Tanpa item_struk_row.xml)
        if (trx.getDetails() != null) {
            for (RiwayatTransaksi.DetailItem item : trx.getDetails()) {

                // Buat Layout Baris Horizontal
                LinearLayout rowLayout = new LinearLayout(this);
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                rowLayout.setPadding(0, 0, 0, 8); // Padding bawah dikit

                // --- TEXTVIEW 1: NAMA BARANG & QTY ---
                TextView tvNama = new TextView(this);
                // Nama + Qty (Misal: Sewa PS5 x1)
                tvNama.setText(item.getNamaItem() + "  x" + item.getQty());
                tvNama.setTextColor(Color.BLACK);
                tvNama.setTextSize(12);
                tvNama.setTypeface(Typeface.MONOSPACE); // Biar kayak struk

                // Layout Params (Weight 1 biar menuhin kiri)
                LinearLayout.LayoutParams paramsNama = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                tvNama.setLayoutParams(paramsNama);

                // --- TEXTVIEW 2: SUBTOTAL ---
                TextView tvHarga = new TextView(this);
                tvHarga.setText(formatRupiah(item.getSubtotal()));
                tvHarga.setTextColor(Color.BLACK);
                tvHarga.setTextSize(12);
                tvHarga.setTypeface(Typeface.MONOSPACE); // Biar kayak struk
                tvHarga.setGravity(Gravity.END); // Rata Kanan

                // Masukkan ke Row
                rowLayout.addView(tvNama);
                rowLayout.addView(tvHarga);

                // Masukkan Row ke Container XML
                binding.containerItems.addView(rowLayout);
            }
        }

        // 4. Set Footer (Totalan) Sesuai ID XML kamu
        binding.tvStrukTotal.setText(formatRupiah(trx.getTotalTagihan()));
        binding.tvStrukCash.setText(formatRupiah(trx.getUangBayar()));
        binding.tvStrukChange.setText(formatRupiah(trx.getUangKembalian()));
    }

    private String formatRupiah(int number) {
        return "Rp " + String.format("%,d", number).replace(',', '.');
    }
}