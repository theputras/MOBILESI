package com.example.newproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem; // Untuk tombol back
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar; // Import ProgressBar
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull; // Import NonNull
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Ganti ke androidx

// Import model (Pastikan path benar)
//import com.example.newproject.Item; // Misal tidak pakai sub-package
//import com.example.newproject.Konsumen; // Misal tidak pakai sub-package
// Jika pakai sub-package:
 import com.example.newproject.models.Item;
 import com.example.newproject.models.Konsumen;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.text.DecimalFormat; // Untuk format harga
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";

    // View dari desc_data.xml
    private TextView detailNama, detailKode;
    private LinearLayout sectionItemDetails, sectionKonsumenDetails;
    // Item fields
    private TextView detailSatuan, detailHargaBeli, detailHargaJual;
    // Konsumen fields
    private TextView detailTelp, detailAlamat, detailPerusahaan, detailKeterangan;
    private Button buttonDelete, buttonUpdate;
    private ProgressBar progressBarDetail; // Tambahkan ProgressBar

    private String dataType;
    private int dataId;
    private Item currentItem; // Simpan objek item saat ini
    private Konsumen currentKonsumen; // Simpan objek konsumen saat ini

    private DecimalFormat rupiahNoCent;
    private RetrofitClient retrofitClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        retrofitClient = RetrofitClient.getInstance();
        setupToolbar();
        setupRupiahFormatter();
        findViews(); // Cari semua view

        Intent intent = getIntent();
        dataType = intent.getStringExtra("DATA_TYPE");
        if (dataType == null) {
            handleErrorAndFinish("Tipe data tidak ditemukan.");
            return;
        }

        if (dataType.equals("item")) {
            dataId = intent.getIntExtra("ITEM_ID", -1);
            if (dataId != -1) {
                fetchItemDetails(dataId);
            } else {
                handleErrorAndFinish("ID Item tidak valid.");
            }
        } else if (dataType.equals("konsumen")) {
            dataId = intent.getIntExtra("KONSUMEN_ID", -1);
            if (dataId != -1) {
                fetchKonsumenDetails(dataId);
            } else {
                handleErrorAndFinish("ID Konsumen tidak valid.");
            }
        } else {
            handleErrorAndFinish("Tipe data tidak dikenal: " + dataType);
        }

        setupButtons();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.detailToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detail"); // Akan diupdate nanti
        }
    }

    private void setupRupiahFormatter() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("in", "ID"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        rupiahNoCent = new DecimalFormat("Rp #,###", symbols);
        rupiahNoCent.setMaximumFractionDigits(0);
        rupiahNoCent.setMinimumFractionDigits(0);
    }

    private void findViews() {
        View includedView = findViewById(R.id.includedLayout);
        detailNama = includedView.findViewById(R.id.detailNama);
        detailKode = includedView.findViewById(R.id.detailKode);
        sectionItemDetails = includedView.findViewById(R.id.sectionItemDetails);
        sectionKonsumenDetails = includedView.findViewById(R.id.sectionKonsumenDetails);
        detailSatuan = includedView.findViewById(R.id.detailSatuan);
        detailHargaBeli = includedView.findViewById(R.id.detailHargaBeli);
        detailHargaJual = includedView.findViewById(R.id.detailHargaJual);
        detailTelp = includedView.findViewById(R.id.detailTelp);
        detailAlamat = includedView.findViewById(R.id.detailAlamat);
        detailPerusahaan = includedView.findViewById(R.id.detailPerusahaan);
        detailKeterangan = includedView.findViewById(R.id.detailKeterangan);
        buttonDelete = includedView.findViewById(R.id.buttonDelete);
        buttonUpdate = includedView.findViewById(R.id.buttonUpdate);
        // Tambahkan ProgressBar (Asumsi ID progressBarDetail ada di activity_detail.xml atau desc_data.xml)
        // Jika di desc_data.xml: progressBarDetail = includedView.findViewById(R.id.progressBarDetail);
        // Jika di activity_detail.xml: progressBarDetail = findViewById(R.id.progressBarDetail);
        // Contoh jika di activity_detail.xml:
        // progressBarDetail = findViewById(R.id.progressBarDetail); // Pastikan ID ini ada!
    }

    // --- Method Fetch Item Detail (Implementasi Lengkap) ---
    private void fetchItemDetails(int itemId) {
        showLoading(true); // Tampilkan loading
        Log.d(TAG, "Fetching item details for ID: " + itemId);

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("access_token", null);
        if (token == null) {
            handleUnauthorized(); // Token tidak ada, anggap unauthorized
            return;
        }

        if (retrofitClient == null) {
            retrofitClient = RetrofitClient.getInstance();
            if(retrofitClient == null){
                showLoading(false);
                handleError("Klien service error.");
                return;
            }
        }
        ApiService api = retrofitClient.getRetrofitInstance().create(ApiService.class);

        api.getItemById("Bearer " + token, "application/json", itemId).enqueue(new Callback<List<Item>>() {
            @Override
            public void onResponse(@NonNull Call<List<Item>> call, @NonNull Response<List<Item>> response) {
                showLoading(false);

                if (response.code() == 401) {
                    handleUnauthorized();
                    return;
                }

                // Cek sukses dan body ada
                if (response.isSuccessful() && response.body() != null) {
                    // Cek APAKAH body (list) KOSONG
                    if (!response.body().isEmpty()) {
                        // Jika TIDAK KOSONG, ambil data pertama
                        currentItem = response.body().get(0);
                        displayItemDetails(currentItem);
                    } else {
                        // Jika KOSONG (API return []), anggap tidak ditemukan
                        handleError("Item tidak ditemukan (ID: " + itemId + ")");
                        detailNama.setText("Item tidak ditemukan"); // Beri feedback
                    }
                } else {
                    // Jika GAGAL (bukan 200) atau body null
                    handleError("Gagal memuat detail Item: " + response.code());
                    if(response.code() == 404) detailNama.setText("Item tidak ditemukan");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Item>> call, @NonNull Throwable t) {
                showLoading(false); // Sembunyikan loading
                handleError("Koneksi gagal: " + t.getMessage());
                detailNama.setText("Gagal memuat data"); // Beri feedback di UI
            }
        });

        // HAPUS PANGGILAN PLACEHOLDER
//         Item placeholderItem = new Item(itemId, "BRG"+itemId, "Nama Barang "+itemId, "Pcs", 10000, 15000);
//         displayItemDetails(placeholderItem);
    }

    // --- Method Fetch Konsumen Detail (Implementasi Lengkap) ---
    private void fetchKonsumenDetails(int konsumenId) {
        showLoading(true); // Tampilkan loading
        Log.d(TAG, "Fetching konsumen details for ID: " + konsumenId);

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("access_token", null);
        if (token == null) {
            handleUnauthorized();
            return;
        }

        if (retrofitClient == null) {
            retrofitClient = RetrofitClient.getInstance();
            if(retrofitClient == null){
                showLoading(false);
                handleError("Klien service error.");
                return;
            }
        }
        ApiService api = retrofitClient.getRetrofitInstance().create(ApiService.class);

        api.getKonsumenById("Bearer " + token, "application/json", konsumenId).enqueue(new Callback<List<Konsumen>>() {
            @Override
            public void onResponse(@NonNull Call<List<Konsumen>> call, @NonNull Response<List<Konsumen>> response) {
                showLoading(false);

                if (response.code() == 401) {
                    handleUnauthorized();
                    return;
                }

                // Cek sukses dan body ada
                if (response.isSuccessful() && response.body() != null) {
                    // Cek APAKAH body (list) KOSONG
                    if (!response.body().isEmpty()) {
                        // Jika TIDAK KOSONG, ambil data pertama
                        currentKonsumen = response.body().get(0);
                        displayKonsumenDetails(currentKonsumen);
                    } else {
                        // Jika KOSONG (API return []), anggap tidak ditemukan
                        handleError("Konsumen tidak ditemukan (ID: " + konsumenId + ")");
                        detailNama.setText("Konsumen tidak ditemukan"); // Beri feedback
                    }
                } else {
                    // Jika GAGAL (bukan 200) atau body null
                    handleError("Gagal memuat detail konsumen: " + response.code());
                    if(response.code() == 404) detailNama.setText("Konsumen tidak ditemukan");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Konsumen>> call, @NonNull Throwable t) {
                showLoading(false); // Sembunyikan loading
                handleError("Koneksi gagal: " + t.getMessage());
                detailNama.setText("Gagal memuat data");
            }
        });

        // HAPUS PANGGILAN PLACEHOLDER
//         Konsumen placeholderKonsumen = new Konsumen(konsumenId, "KSM"+konsumenId, "Nama Konsumen "+konsumenId, "081"+konsumenId, "Alamat "+konsumenId, "PT. "+konsumenId, "Ket "+konsumenId);
//         displayKonsumenDetails(placeholderKonsumen);
    }

    // --- Method untuk Menampilkan Data ---
    private void displayItemDetails(Item item) {
        if (item == null) return; // Tambah pengecekan null
        getSupportActionBar().setTitle("Detail Item");
        detailNama.setText(item.getNama_item()); // Sesuaikan getter
        detailKode.setText("Kode: " + item.getKode_item()); // Sesuaikan getter
        detailSatuan.setText("Satuan: " + item.getSatuan());
        detailHargaBeli.setText("Harga Beli: " + rupiahNoCent.format(item.getHargabeli()));
        detailHargaJual.setText("Harga Jual: " + rupiahNoCent.format(item.getHargajual()));
        sectionItemDetails.setVisibility(View.VISIBLE);
        sectionKonsumenDetails.setVisibility(View.GONE);
    }

    private void displayKonsumenDetails(Konsumen konsumen) {
        if (konsumen == null) return; // Tambah pengecekan null
        getSupportActionBar().setTitle("Detail Konsumen");
        detailNama.setText(konsumen.getNamaKonsumen());
        detailKode.setText("Kode: " + konsumen.getKodeKonsumen()); // Sesuaikan getter
        detailTelp.setText("Telepon: " + (konsumen.getTelp() != null ? konsumen.getTelp() : "-"));
        detailAlamat.setText("Alamat: " + (konsumen.getAlamat() != null ? konsumen.getAlamat() : "-"));
        detailPerusahaan.setText("Perusahaan: " + (konsumen.getPerusahaan() != null ? konsumen.getPerusahaan() : "-"));
        detailKeterangan.setText("Keterangan: " + (konsumen.getKeterangan() != null ? konsumen.getKeterangan() : "-"));
        sectionItemDetails.setVisibility(View.GONE);
        sectionKonsumenDetails.setVisibility(View.VISIBLE);
    }

    private void setupButtons() {
        buttonDelete.setOnClickListener(v -> showDeleteConfirmation());

        buttonUpdate.setOnClickListener(v -> {
            Log.d(TAG, "Update button clicked for type: " + dataType + " ID: " + dataId);
            if (dataType.equals("item") && currentItem != null) {
                showUpdateItemDialog(currentItem); // <--- Memanggil dialog update Item
            } else if (dataType.equals("konsumen") && currentKonsumen != null) {
                showUpdateKonsumenDialog(currentKonsumen); // <--- Memanggil dialog update Konsumen
            } else {
                Toast.makeText(this, "Data belum dimuat sepenuhnya.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Hapus")
                .setMessage("Anda yakin ingin menghapus data '" + detailNama.getText() + "'?") // Tampilkan nama data
                .setPositiveButton("Hapus", (dialog, which) -> {
                    if (dataType.equals("item")) {
                        deleteItem(dataId);
                    } else if (dataType.equals("konsumen")) {
                        deleteKonsumen(dataId);
                    }
                })
                .setNegativeButton("Batal", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // --- Method Delete Item (Sudah ada dari sebelumnya) ---
    private void deleteItem(int itemId) {
        // ... (Kode deleteItem dari jawaban sebelumnya sudah benar) ...
        Log.d(TAG, "Attempting to delete item with ID: " + itemId);
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("access_token", null);
        if (token == null) { /* Handle token null */ return; }
        if (retrofitClient == null) { /* Handle retrofit null */ return; }
        ApiService api = retrofitClient.getRetrofitInstance().create(ApiService.class);
        // showLoading(true); // Opsional
        api.deleteItem("Bearer " + token, "application/json", itemId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                // showLoading(false); // Opsional
                if (response.code() == 401) { handleUnauthorized(); return; }
                if (response.isSuccessful()) {
                    Toast.makeText(DetailActivity.this, "Item berhasil dihapus", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK); // Beri tahu list ada perubahan
                    finish();
                } else {
                    String errorMsg = "Gagal menghapus item"; /* ... baca error body ... */
                    handleError(errorMsg);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                // showLoading(false); // Opsional
                handleError("Koneksi gagal saat menghapus: " + t.getMessage());
            }
        });
    }

    // --- Method Delete Konsumen (Implementasi) ---
    private void deleteKonsumen(int konsumenId) {
        Log.d(TAG, "Attempting to delete konsumen with ID: " + konsumenId);
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("access_token", null);
        if (token == null) { /* Handle token null */ return; }
        if (retrofitClient == null) { /* Handle retrofit null */ return; }
        ApiService api = retrofitClient.getRetrofitInstance().create(ApiService.class);
        // showLoading(true); // Opsional
        api.deleteKonsumen("Bearer " + token, "application/json", konsumenId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                // showLoading(false); // Opsional
                if (response.code() == 401) { handleUnauthorized(); return; }
                if (response.isSuccessful()) {
                    Toast.makeText(DetailActivity.this, "Konsumen berhasil dihapus", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK); // Beri tahu list ada perubahan
                    finish();
                } else {
                    String errorMsg = "Gagal menghapus konsumen"; /* ... baca error body ... */
                    handleError(errorMsg);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                // showLoading(false); // Opsional
                handleError("Koneksi gagal saat menghapus: " + t.getMessage());
            }
        });
    }

    // --- Method untuk Update Data (Buka Dialog) ---

    // Method tampilkan dialog UPDATE item

    private void showUpdateItemDialog(Item itemToUpdate) {
        if (itemToUpdate == null) return;
        Context context = this;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_data, null);
        builder.setView(dialogView);
        builder.setTitle("Update Item");

        // --- Ambil referensi SEMUA TextInputLayout ---
        TextInputLayout layoutKodeItem = dialogView.findViewById(R.id.layoutKodeItem);
        TextInputLayout layoutNamaItem = dialogView.findViewById(R.id.layoutNamaItem);
        TextInputLayout layoutSatuan = dialogView.findViewById(R.id.layoutSatuan);
        TextInputLayout layoutHargaBeli = dialogView.findViewById(R.id.layoutHargaBeli);
        TextInputLayout layoutHargaJual = dialogView.findViewById(R.id.layoutHargaJual);

        TextInputLayout layoutKodeKonsumen = dialogView.findViewById(R.id.layoutKodeKonsumen);
        TextInputLayout layoutNamaKonsumen = dialogView.findViewById(R.id.layoutNamaKonsumen);
        TextInputLayout layoutTelp = dialogView.findViewById(R.id.layoutTelp);
        TextInputLayout layoutAlamat = dialogView.findViewById(R.id.layoutAlamat);
        TextInputLayout layoutPerusahaan = dialogView.findViewById(R.id.layoutPerusahaan);
        TextInputLayout layoutKeterangan = dialogView.findViewById(R.id.layoutKeterangan);

        // --- Atur Visibilitas: Tampilkan Item, Sembunyikan Konsumen ---
        layoutKodeItem.setVisibility(View.VISIBLE);
        layoutNamaItem.setVisibility(View.VISIBLE);
        layoutSatuan.setVisibility(View.VISIBLE);
        layoutHargaBeli.setVisibility(View.VISIBLE);
        layoutHargaJual.setVisibility(View.VISIBLE);

        layoutKodeKonsumen.setVisibility(View.GONE);
        layoutNamaKonsumen.setVisibility(View.GONE);
        layoutTelp.setVisibility(View.GONE);
        layoutAlamat.setVisibility(View.GONE);
        layoutPerusahaan.setVisibility(View.GONE);
        layoutKeterangan.setVisibility(View.GONE);

        // Ambil EditText Item
        EditText etKode = dialogView.findViewById(R.id.etKodeItem);
        EditText etNama = dialogView.findViewById(R.id.etNamaItem);
        EditText etSatuan = dialogView.findViewById(R.id.etSatuan);
        EditText etBeli = dialogView.findViewById(R.id.etHargaBeli);
        EditText etJual = dialogView.findViewById(R.id.etHargaJual);

        // Isi EditText dengan data itemToUpdate
        etKode.setText(itemToUpdate.getKode_item());
        etNama.setText(itemToUpdate.getNama_item());
        etSatuan.setText(itemToUpdate.getSatuan());
        etBeli.setText(String.valueOf(itemToUpdate.getHargabeli()));
        etJual.setText(String.valueOf(itemToUpdate.getHargajual()));

        builder.setPositiveButton("Update", (dialog, which) -> {
            // ... (Kode validasi dan panggil updateItemToServer tetap sama) ...
            String kode = etKode.getText().toString().trim();
            String nama = etNama.getText().toString().trim();
            String satuan = etSatuan.getText().toString().trim();
            String beliStr = etBeli.getText().toString().trim();
            String jualStr = etJual.getText().toString().trim();

            if (kode.isEmpty() || nama.isEmpty() || satuan.isEmpty() || beliStr.isEmpty() || jualStr.isEmpty()) {
                Toast.makeText(context, "Kode, Nama, Satuan, Harga Beli, dan Harga Jual wajib diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int hargabeli = Integer.parseInt(beliStr);
                int hargajual = Integer.parseInt(jualStr);
                Item updatedItem = new Item(kode, nama, satuan, hargabeli, hargajual);
                updateItemToServer(itemToUpdate.getId(), updatedItem);
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Harga Beli/Jual harus angka", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
    // Method tampilkan dialog UPDATE konsumen

    private void showUpdateKonsumenDialog(Konsumen konsumenToUpdate) {
        if (konsumenToUpdate == null) return;
        Context context = this;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_data, null);
        builder.setView(dialogView);
        builder.setTitle("Update Konsumen");

        // --- Ambil referensi SEMUA TextInputLayout ---
        TextInputLayout layoutKodeItem = dialogView.findViewById(R.id.layoutKodeItem);
        TextInputLayout layoutNamaItem = dialogView.findViewById(R.id.layoutNamaItem);
        TextInputLayout layoutSatuan = dialogView.findViewById(R.id.layoutSatuan);
        TextInputLayout layoutHargaBeli = dialogView.findViewById(R.id.layoutHargaBeli);
        TextInputLayout layoutHargaJual = dialogView.findViewById(R.id.layoutHargaJual);

        TextInputLayout layoutKodeKonsumen = dialogView.findViewById(R.id.layoutKodeKonsumen);
        TextInputLayout layoutNamaKonsumen = dialogView.findViewById(R.id.layoutNamaKonsumen);
        TextInputLayout layoutTelp = dialogView.findViewById(R.id.layoutTelp);
        TextInputLayout layoutAlamat = dialogView.findViewById(R.id.layoutAlamat);
        TextInputLayout layoutPerusahaan = dialogView.findViewById(R.id.layoutPerusahaan);
        TextInputLayout layoutKeterangan = dialogView.findViewById(R.id.layoutKeterangan);

        // --- Atur Visibilitas: Sembunyikan Item, Tampilkan Konsumen ---
        layoutKodeItem.setVisibility(View.GONE);
        layoutNamaItem.setVisibility(View.GONE);
        layoutSatuan.setVisibility(View.GONE);
        layoutHargaBeli.setVisibility(View.GONE);
        layoutHargaJual.setVisibility(View.GONE);

        layoutKodeKonsumen.setVisibility(View.VISIBLE);
        layoutNamaKonsumen.setVisibility(View.VISIBLE);
        layoutTelp.setVisibility(View.VISIBLE);
        layoutAlamat.setVisibility(View.VISIBLE);
        layoutPerusahaan.setVisibility(View.VISIBLE);
        layoutKeterangan.setVisibility(View.VISIBLE); // Keterangan ditampilkan

        // Ambil EditText Konsumen
        EditText etKodeKonsumen = dialogView.findViewById(R.id.etKodeKonsumen);
        EditText etNamaKonsumen = dialogView.findViewById(R.id.etNamaKonsumen);
        EditText etTelp = dialogView.findViewById(R.id.etTelp);
        EditText etAlamat = dialogView.findViewById(R.id.etAlamat);
        EditText etPerusahaan = dialogView.findViewById(R.id.etPerusahaan);
        EditText etKeterangan = dialogView.findViewById(R.id.etKeterangan);

        // Isi EditText dengan data konsumenToUpdate
        etKodeKonsumen.setText(konsumenToUpdate.getKodeKonsumen());
        etNamaKonsumen.setText(konsumenToUpdate.getNamaKonsumen());
        etTelp.setText(konsumenToUpdate.getTelp());
        etAlamat.setText(konsumenToUpdate.getAlamat());
        etPerusahaan.setText(konsumenToUpdate.getPerusahaan());
        etKeterangan.setText(konsumenToUpdate.getKeterangan());

        builder.setPositiveButton("Update", (dialog, which) -> {
            // ... (Kode validasi dan panggil updateKonsumenToServer tetap sama) ...
            String kode = etKodeKonsumen.getText().toString().trim();
            String nama = etNamaKonsumen.getText().toString().trim();
            String telp = etTelp.getText().toString().trim();
            String alamat = etAlamat.getText().toString().trim();
            String perusahaan = etPerusahaan.getText().toString().trim();
            String keterangan = etKeterangan.getText().toString().trim();

            if (kode.isEmpty() || nama.isEmpty() || telp.isEmpty() || alamat.isEmpty() || perusahaan.isEmpty()) {
                Toast.makeText(context, "Kode, Nama, Telp, Alamat, dan Perusahaan wajib diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            Konsumen updatedKonsumen = new Konsumen(kode, nama, telp, alamat, perusahaan, keterangan);
            updateKonsumenToServer(konsumenToUpdate.getId(), updatedKonsumen);
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
    // --- Method untuk Kirim Update ke Server ---
    private void updateItemToServer(int itemId, Item updatedItem) {
        Log.d(TAG, "Updating item ID: " + itemId);
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("access_token", null);
        if (token == null) { handleUnauthorized(); return; }
        if (retrofitClient == null) { /* Handle retrofit null */ return; }

        ApiService api = retrofitClient.getRetrofitInstance().create(ApiService.class);
        // showLoading(true); // Opsional

        api.updateItem("Bearer " + token, "application/json", itemId, updatedItem).enqueue(new Callback<Item>() { // Kembalian mungkin Item
            @Override
            public void onResponse(@NonNull Call<Item> call, @NonNull Response<Item> response) {
                // showLoading(false); // Opsional
                if (response.code() == 401) { handleUnauthorized(); return; }

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(DetailActivity.this, "Item berhasil diupdate", Toast.LENGTH_SHORT).show();
                    // Update tampilan detail dengan data baru dari response
                    currentItem = response.body();
                    displayItemDetails(currentItem);
                    setResult(RESULT_OK); // Beri tahu list ada perubahan
                    // Jangan finish(), biarkan user tetap di detail
                } else {
                    String errorMsg = "Gagal mengupdate item"; /* ... baca error body ... */
                    handleError(errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Item> call, @NonNull Throwable t) {
                // showLoading(false); // Opsional
                handleError("Koneksi gagal saat update: " + t.getMessage());
            }
        });
    }

    private void updateKonsumenToServer(int konsumenId, Konsumen updatedKonsumen) {
        Log.d(TAG, "Updating konsumen ID: " + konsumenId);
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("access_token", null);
        if (token == null) { handleUnauthorized(); return; }
        if (retrofitClient == null) { /* Handle retrofit null */ return; }

        ApiService api = retrofitClient.getRetrofitInstance().create(ApiService.class);
        // showLoading(true); // Opsional

        api.updateKonsumen("Bearer " + token, "application/json", konsumenId, updatedKonsumen).enqueue(new Callback<Konsumen>() {
            @Override
            public void onResponse(@NonNull Call<Konsumen> call, @NonNull Response<Konsumen> response) {
                // showLoading(false); // Opsional
                if (response.code() == 401) { handleUnauthorized(); return; }

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(DetailActivity.this, "Konsumen berhasil diupdate", Toast.LENGTH_SHORT).show();
                    // Update tampilan detail
                    currentKonsumen = response.body();
                    displayKonsumenDetails(currentKonsumen);
                    setResult(RESULT_OK); // Beri tahu list ada perubahan
                } else {
                    String errorMsg = "Gagal mengupdate konsumen"; /* ... baca error body ... */
                    handleError(errorMsg);
                }
            }
            @Override
            public void onFailure(@NonNull Call<Konsumen> call, @NonNull Throwable t) {
                // showLoading(false); // Opsional
                handleError("Koneksi gagal saat update: " + t.getMessage());
            }
        });
    }

    // --- Helper Methods ---
    private void showLoading(boolean show) {
        if (progressBarDetail != null) {
            progressBarDetail.setVisibility(show ? View.VISIBLE : View.GONE);
            // Sembunyikan/tampilkan konten utama saat loading
            View includedView = findViewById(R.id.includedLayout);
            if (includedView != null) {
                includedView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        } else {
            Log.w(TAG, "ProgressBar is null, cannot show loading state.");
        }
    }

    private void handleUnauthorized() {
        Toast.makeText(this, "Sesi berakhir, silakan login ulang.", Toast.LENGTH_LONG).show();
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        prefs.edit().remove("access_token").apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void handleError(String message) {
        Log.e(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        // Pertimbangkan finish() jika error terjadi saat fetch awal
    }
    private void handleErrorAndFinish(String message) {
        handleError(message);
        finish();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}