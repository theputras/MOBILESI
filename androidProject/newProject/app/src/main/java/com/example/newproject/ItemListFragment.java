package com.example.newproject;

// Import yang diperlukan
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

// Sesuaikan path jika adapter/model ada di sub-package
import com.example.newproject.adapter.ItemAdapter;
import com.example.newproject.models.Item;
// Jika di subpackage: import com.example.newproject.adapter.ItemAdapter;
// Jika di subpackage: import com.example.newproject.models.Item;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson; // Opsional untuk logging

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.newproject.DetailActivity;

public class ItemListFragment extends Fragment implements ItemAdapter.OnItemClickListener {

    // --- Variabel ---
    private RecyclerView recyclerViewItems; // Ganti nama variabel
    private ItemAdapter itemAdapter;
    private RetrofitClient retrofitClient; // Instance Retrofit
    private static final String TAG = "ItemListFragment"; // TAG Fragment
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private static final int DETAIL_REQUEST_CODE = 101;

    // ... di dalam class ItemListFragment ...

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode); // Log untuk debug

        // Cek apakah hasilnya dari DetailActivity dan hasilnya OK (berhasil delete/update)
        if (requestCode == DETAIL_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            Log.i(TAG, "Received RESULT_OK from DetailActivity. Refreshing item list...");
            // Panggil method get datanya di sini
            getItemsFromServer();
        }
    }

    // Constructor kosong (wajib ada)
    public ItemListFragment() {}

    // --- Lifecycle Methods ---
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inisialisasi RetrofitClient
        retrofitClient = RetrofitClient.getInstance();
        // Inisialisasi SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayoutItems);
        // Setup Listener untuk aksi refresh
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Panggil method fetch data saat di-refresh
                Log.d(TAG, "Pull to refresh triggered.");
                getItemsFromServer(); // Atau getKonsumenFromServer()
                // Jangan tampilkan ProgressBar utama saat refresh, cukup indicator SwipeRefreshLayout
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (recyclerViewItems != null) recyclerViewItems.setVisibility(View.VISIBLE); // Pastikan RV terlihat
            }
        });
        // Inisialisasi RecyclerView
        recyclerViewItems = view.findViewById(R.id.recyclerViewItems); // Pastikan ID ini ada di fragment_item_list.xml
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(getContext()));
        progressBar = view.findViewById(R.id.progressBarItems);
        // Inisialisasi Adapter dengan list kosong
        itemAdapter = new ItemAdapter(new ArrayList<>(), this);
        recyclerViewItems.setAdapter(itemAdapter);

        // Ambil data item dari server
        getItemsFromServer();

    }
    // --- Implementasi method dari OnItemClickListener ---
    @Override
    public void onItemClick(Item item) {
        Log.d(TAG, "Item clicked: " + item.getNama_item());
        Intent intent = new Intent(getActivity(), DetailActivity.class);

        // Cara 1: Kirim ID saja (lebih ringan)
        // DetailActivity nanti fetch ulang data dari server pakai ID
        intent.putExtra("DATA_TYPE", "item"); // Tandai tipe data
        intent.putExtra("ITEM_ID", item.getId()); // Kirim ID item

        // Cara 2: Kirim seluruh objek (jika objek Item Serializable/Parcelable)
        // intent.putExtra("DATA_TYPE", "item");
        // intent.putExtra("ITEM_OBJECT", item); // Pastikan Item implement Serializable/Parcelable

        // Cara 3: Kirim objek sebagai JSON String (pakai Gson)
        // String itemJson = new Gson().toJson(item);
        // intent.putExtra("DATA_TYPE", "item");
        // intent.putExtra("ITEM_JSON", itemJson);

//        startActivity(intent);
        startActivityForResult(intent, DETAIL_REQUEST_CODE);
    }


    // --- Pindahkan Method dari MainActivity ke sini ---

    // Method ambil data Item
    private void getItemsFromServer() {
        Context context = getContext();
        if (context == null) {
            Log.e(TAG, "Context is null in getItemsFromServer");
            return;
        }
        // ... (cek context, token, dll)
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("access_token", null);
        if (token == null) {
            Log.e(TAG, "Token tidak ditemukan");
            // Handle token null (misal, kembali ke login)
            return;
        }
        // Jika sedang refresh, swipeRefreshLayout.isRefreshing() akan true
        if (!swipeRefreshLayout.isRefreshing()) {
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
            if (recyclerViewItems != null) recyclerViewItems.setVisibility(View.GONE);
        } else {
            // Jika sedang refresh, pastikan RV terlihat
            if (recyclerViewItems != null) recyclerViewItems.setVisibility(View.VISIBLE);
            if (progressBar != null) progressBar.setVisibility(View.GONE);
        }
        // Tampilkan loading, sembunyikan RecyclerView SEBELUM panggil API
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (recyclerViewItems != null) recyclerViewItems.setVisibility(View.GONE);
        ApiService api = retrofitClient.getRetrofitInstance().create(ApiService.class);
        api.getItems("Bearer " + token, "application/json").enqueue(new Callback<List<Item>>() {
            @Override
            public void onResponse(Call<List<Item>> call, Response<List<Item>> response) {
                 // Cek fragment masih ada
                if (!isAdded()) return;
                // Pengecekkan kode 401
                if (response.code() == 401) {
                    // Token tidak valid atau expired
                    Log.w(TAG, "getItems response code 401 (Unauthorized). Logging out.");
                    Toast.makeText(context, "Sesi berakhir, silakan login ulang.", Toast.LENGTH_LONG).show();
                    // Panggil doLogout() dari Activity induk
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).doLogout();
                    }
                    // Hentikan proses lebih lanjut di onResponse ini
                    return;
                }
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                // Sembunyikan loading, tampilkan RecyclerView SETELAH response diterima
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (recyclerViewItems != null) recyclerViewItems.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    List<Item> items = response.body();
                    Log.d(TAG, "Data Items diterima: " + new Gson().toJson(items)); // Log JSON
                    if (itemAdapter != null) {
                        itemAdapter.updateData(items); // Update adapter
                    } else {
                        Log.e(TAG,"itemAdapter is null saat onResponse");
                    }
                } else {
                    Log.e(TAG, "Gagal mendapatkan data item. Code: " + response.code());
                    // Handle error (misal, tampilkan Toast)
                    // Toast.makeText(context, "Gagal memuat item: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Item>> call, Throwable t) {
                 // Cek fragment masih ada
                if (!isAdded()) return;

                // Sembunyikan loading, tampilkan RecyclerView (meskipun gagal, biar nggak kosong)
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (recyclerViewItems != null) recyclerViewItems.setVisibility(View.VISIBLE);
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                Log.e(TAG, "Koneksi gagal saat get item", t);
                // Handle failure (misal, tampilkan Toast)
                 Toast.makeText(context, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method tampilkan dialog tambah item
// --- Di dalam ItemListFragment.java ---

    public void showAddItemDialog() {
        Context context = getContext();
        if (context == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        // Gunakan layout gabungan
        View dialogView = inflater.inflate(R.layout.dialog_add_data, null);
        builder.setView(dialogView);
        builder.setTitle("Tambah Item Baru"); // Set judul

        // --- Ambil referensi SEMUA field (Item & Konsumen) ---
        // Gunakan TextInputLayout untuk menyembunyikan/menampilkan
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

        // Ambil EditText khusus Item (untuk ambil data nanti)
        EditText etKode = dialogView.findViewById(R.id.etKodeItem);
        EditText etNama = dialogView.findViewById(R.id.etNamaItem);
        EditText etSatuan = dialogView.findViewById(R.id.etSatuan);
        EditText etBeli = dialogView.findViewById(R.id.etHargaBeli);
        EditText etJual = dialogView.findViewById(R.id.etHargaJual);

        builder.setPositiveButton("Tambah", (dialog, which) -> {
            String kode = etKode.getText().toString().trim();
            String nama = etNama.getText().toString().trim();
            String satuan = etSatuan.getText().toString().trim();
            String beliStr = etBeli.getText().toString().trim();
            String jualStr = etJual.getText().toString().trim();

            if (kode.isEmpty() || nama.isEmpty() || satuan.isEmpty() || beliStr.isEmpty() || jualStr.isEmpty()) {
                Toast.makeText(context, "Semua kolom item wajib diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                long hargabeli = Long.parseLong(beliStr);
                long hargajual = Long.parseLong(jualStr);
                addItemToServer(kode, nama, satuan, hargabeli, hargajual); // Panggil fungsi tambah item
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Harga Beli/Jual harus angka", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
    // Method kirim data item baru ke server
    private void addItemToServer(String kode, String nama, String satuan, long beli, long jual) {
        Context context = getContext();
        if (context == null) return;

        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("access_token", null);
        if (token == null) {
            Toast.makeText(context, "Token tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService api = retrofitClient.getRetrofitInstance().create(ApiService.class);
        Item newItem = new Item(kode, nama, satuan, beli, jual); // Pastikan constructor Item sesuai

        api.addItem("Bearer " + token, "application/json", newItem).enqueue(new Callback<Item>() {
            @Override
            public void onResponse(Call<Item> call, Response<Item> response) {
                if (!isAdded() || context == null) return; // Cek fragment

                if (response.isSuccessful()) {
                    Toast.makeText(context, "Item berhasil ditambah", Toast.LENGTH_SHORT).show();
                    getItemsFromServer(); // Refresh list setelah berhasil tambah
                } else {
                    String err = "";
                    try { if (response.errorBody() != null) err = response.errorBody().string(); } catch (Exception ignored) {}
                    Log.e(TAG, "Gagal menambah item. Kode: " + response.code() + " body: " + err);
                    Toast.makeText(context, "Gagal menambah item (Code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Item> call, Throwable t) {
                if (!isAdded() || context == null) return; // Cek fragment
                Log.e(TAG, "Koneksi gagal saat add item", t);
                Toast.makeText(context, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}