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
import com.theputras.posrentalps.utils.BluetoothUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StrukActivity extends AppCompatActivity {

    private ActivityStrukBinding binding;
    private ApiService apiService;
    private String transactionId;
    private BluetoothUtil bluetoothUtil;
    private RiwayatTransaksi currentTransaksi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Setup ViewBinding
        binding = ActivityStrukBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        bluetoothUtil = BluetoothUtil.getInstance(this);
        // 1. Cek Koneksi saat Activity dibuat
        checkPrinterConnection();

        // 2. Logic Tombol Print
        binding.btnPrint.setOnClickListener(v -> printStruk());


        apiService = ApiClient.getClient().create(ApiService.class);


        transactionId = getIntent().getStringExtra("TRANSACTION_ID");
        if (transactionId != null) {
            // Ambil ID Transaksi
            loadStrukData(transactionId);
        } else {
            Toast.makeText(this, "ID Transaksi tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
        }



        // Tombol Kembali
//        binding.btnBackHome.setOnClickListener(v -> {
//            Intent intent = new Intent(StrukActivity.this, MainActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
//            finish();
//        });

        binding.btnBackHome.setOnClickListener(v -> finish());

        binding.btnBackHome.setOnClickListener(v -> finish());

        binding.btnPrint.setOnClickListener(v -> printStruk());

//        // Tombol Print (Sementara Toast dulu)
//        binding.btnPrint.setOnClickListener(v -> {
//            Toast.makeText(this, "Fitur Print Bluetooth sedang dikembangkan...", Toast.LENGTH_SHORT).show();
//        });
        checkPrinterConnection();
    }
    private void checkPrinterConnection() {
        // Set awal: Merah & Disable
        setPrinterStatus(false);

        // Coba Auto Connect
        bluetoothUtil.autoConnect(new BluetoothUtil.ConnectionListener() {
            @Override
            public void onConnected() {
                // Kalau sukses: Hijau & Enable
                setPrinterStatus(true);
            }

            @Override
            public void onFailed(String message) {
                setPrinterStatus(false);
                Toast.makeText(StrukActivity.this, "Printer: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setPrinterStatus(boolean isConnected) {
        runOnUiThread(() -> {
            if (isConnected) {
                binding.indicatorStatus.setCardBackgroundColor(Color.parseColor("#4CAF50")); // HIJAU
                binding.btnPrint.setEnabled(true);
                binding.btnPrint.setBackgroundColor(Color.parseColor("#2196F3")); // Biru (Aktif)
                binding.btnPrint.setText("CETAK STRUK");
            } else {
                binding.indicatorStatus.setCardBackgroundColor(Color.parseColor("#F44336")); // MERAH
                binding.btnPrint.setEnabled(false);
                binding.btnPrint.setBackgroundColor(Color.parseColor("#9E9E9E")); // Abu (Mati)
                binding.btnPrint.setText("PRINTER DISCONNECT");
            }
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
        this.currentTransaksi = trx;
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
                tvNama.setText(item.getNamaItem());
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
    private void printStruk() {
        if (currentTransaksi == null) return;

        new Thread(() -> {
            try {
                BluetoothUtil bt = bluetoothUtil;

                // --- FORMAT STRUK ---
                bt.print(BluetoothUtil.RESET);

                // Header
                bt.print(BluetoothUtil.ALIGN_CENTER);
                bt.print(BluetoothUtil.TEXT_BOLD_ON);
                bt.print(BluetoothUtil.TEXT_SIZE_LARGE);
                bt.print("RENTAL PS SANTUY\n".getBytes());
                bt.print(BluetoothUtil.TEXT_SIZE_NORMAL);
                bt.print(BluetoothUtil.TEXT_BOLD_OFF);
                bt.print("Jl. Low Effort No. 1, Surabaya\n".getBytes());
                bt.print("--------------------------------\n".getBytes());

                // Info Transaksi
                bt.print(BluetoothUtil.ALIGN_LEFT);
                bt.print(("Bon " + currentTransaksi.getIdTransaksi() + "\n").getBytes());
                bt.print(("Tgl : " + currentTransaksi.getTanggalTransaksi() + "\n").getBytes());
                bt.print(("Cust: " + currentTransaksi.getNamaPenyewa() + "\n").getBytes());
                bt.print("--------------------------------\n".getBytes());

                // Item
                if (currentTransaksi.getDetails() != null) {
                    for (RiwayatTransaksi.DetailItem item : currentTransaksi.getDetails()) {
                        String nama = item.getNamaItem();
                        // Potong nama kalau kepanjangan
                        if (nama.length() > 22) nama = nama.substring(0, 22);

                        bt.print((nama + "\n").getBytes());

                        // Format: x1   Rp 20.000 (Rata Kanan Manual sederhana)
                        String qtyHarga = formatRupiah(item.getSubtotal());
                        bt.print(BluetoothUtil.ALIGN_RIGHT);
                        bt.print((qtyHarga + "\n").getBytes());
                        bt.print(BluetoothUtil.ALIGN_LEFT);
                    }
                }

                bt.print("--------------------------------\n".getBytes());

                // Total
                bt.print(BluetoothUtil.ALIGN_RIGHT);
                bt.print(BluetoothUtil.TEXT_BOLD_ON);
                bt.print(("TOTAL: " + formatRupiah(currentTransaksi.getTotalTagihan()) + "\n").getBytes());
                bt.print(BluetoothUtil.TEXT_BOLD_OFF);
                bt.print(("TUNAI: " + formatRupiah(currentTransaksi.getUangBayar()) + "\n").getBytes());
                bt.print(("KEMBALI: " + formatRupiah(currentTransaksi.getUangKembalian()) + "\n").getBytes());

                // Footer
                bt.print(BluetoothUtil.ALIGN_CENTER);
                bt.print("\nTerima Kasih\n".getBytes());
                bt.print("Selamat Bermain!\n\n\n".getBytes()); // Feed akhir biar kertas keluar

                runOnUiThread(() -> Toast.makeText(this, "Print Terkirim", Toast.LENGTH_SHORT).show());

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Gagal Print: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                // Kalau gagal print karena koneksi putus, update status UI
                setPrinterStatus(false);
            }
        }).start();
    }
    private String formatRupiah(int number) {
        return "Rp " + String.format("%,d", number).replace(',', '.');
    }
}