package com.example.newproject;

// Import yang diperlukan
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
import android.widget.Toast; // Contoh
import android.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment; // Pastikan extends Fragment
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

// Sesuaikan path ini jika adapter/model ada di sub-package
import com.example.newproject.adapter.KonsumenAdapter;
import com.example.newproject.models.Konsumen;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.newproject.DetailActivity;

public class KonsumenListFragment extends Fragment implements KonsumenAdapter.OnKonsumenClickListener {

    // --- Variabel ---
    private RecyclerView recyclerViewKonsumen;
    private KonsumenAdapter konsumenAdapter;
    private RetrofitClient retrofitClient; // Instance Retrofit
    private static final String TAG = "KonsumenListFragment";
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private static final int DETAIL_REQUEST_CODE = 102;

    // ... di dalam class KonsumenListFragment ...

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode); // Log untuk debug

        // Cek apakah hasilnya dari DetailActivity dan hasilnya OK
        if (requestCode == DETAIL_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            Log.i(TAG, "Received RESULT_OK from DetailActivity. Refreshing konsumen list...");
            // Panggil method get datanya di sini
            getKonsumenFromServer();
        }
    }
    // Constructor kosong (wajib ada untuk Fragment)
    public KonsumenListFragment() {}

    // --- Lifecycle Methods ---
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout untuk Fragment ini
        View view = inflater.inflate(R.layout.fragment_konsumen_list, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inisialisasi SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayoutKonsumens); // Atau swipeRefreshLayoutKonsumen


        // Inisialisasi RetrofitClient
        retrofitClient = RetrofitClient.getInstance();

        // Inisialisasi RecyclerView
        recyclerViewKonsumen = view.findViewById(R.id.recyclerViewKonsumen); // Pastikan ID ini ada di fragment_konsumen_list.xml
        recyclerViewKonsumen.setLayoutManager(new LinearLayoutManager(getContext()));
        progressBar = view.findViewById(R.id.progressBarKonsumen);

        // Inisialisasi Adapter dengan list kosong dulu
        konsumenAdapter = new KonsumenAdapter(new ArrayList<>(), this);
        recyclerViewKonsumen.setAdapter(konsumenAdapter);
        // Setup Listener untuk aksi refresh
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Panggil method fetch data saat di-refresh
                Log.d(TAG, "Pull to refresh triggered.");
                getKonsumenFromServer(); // Atau getKonsumenFromServer()
                // Jangan tampilkan ProgressBar utama saat refresh, cukup indicator SwipeRefreshLayout
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (recyclerViewKonsumen != null) recyclerViewKonsumen.setVisibility(View.VISIBLE); // Pastikan RV terlihat
            }
        });

        // Panggil method untuk mengambil data dari server
        getKonsumenFromServer();
    }

    // --- Implementasi method dari OnKonsumenClickListener ---
    @Override
    public void onKonsumenClick(Konsumen konsumen) {
        Log.d(TAG, "Konsumen clicked: " + konsumen.getNamaKonsumen());
        Intent intent = new Intent(getActivity(), DetailActivity.class);

        // Cara 1: Kirim ID
        intent.putExtra("DATA_TYPE", "konsumen"); // Tandai tipe data
        intent.putExtra("KONSUMEN_ID", konsumen.getId()); // Kirim ID konsumen

        // Cara 2: Kirim objek (jika Konsumen Serializable/Parcelable)
        // intent.putExtra("DATA_TYPE", "konsumen");
        // intent.putExtra("KONSUMEN_OBJECT", konsumen);

        // Cara 3: Kirim JSON
        // String konsumenJson = new Gson().toJson(konsumen);
        // intent.putExtra("DATA_TYPE", "konsumen");
        // intent.putExtra("KONSUMEN_JSON", konsumenJson);

//        startActivity(intent);
        startActivityForResult(intent, DETAIL_REQUEST_CODE);
    }

    // Bisa tambahkan onResume() jika ingin refresh data saat kembali ke fragment
     @Override
     public void onResume() {
         super.onResume();
         getKonsumenFromServer(); // Refresh data
     }

    // --- Method untuk ambil data ---
    private void getKonsumenFromServer() {
        Context context = getContext();
        if (context == null) {
            Log.e(TAG, "Context is null in getKonsumenFromServer");
            return; // Keluar jika context null
        }

        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("access_token", null);

        if (token == null) {
            Log.e(TAG, "Token tidak ditemukan");
            // Opsional: Tampilkan pesan error ke user
             Toast.makeText(context, "Sesi habis, silakan login ulang", Toast.LENGTH_SHORT).show();
            // Mungkin redirect ke login
            return;
        }
        // Tampilkan loading, sembunyikan RecyclerView SEBELUM panggil API
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (recyclerViewKonsumen != null) recyclerViewKonsumen.setVisibility(View.GONE);
        // Jika sedang refresh, swipeRefreshLayout.isRefreshing() akan true
        if (!swipeRefreshLayout.isRefreshing()) {
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
            if (recyclerViewKonsumen != null) recyclerViewKonsumen.setVisibility(View.GONE);
        } else {
            // Jika sedang refresh, pastikan RV terlihat
            if (recyclerViewKonsumen != null) recyclerViewKonsumen.setVisibility(View.VISIBLE);
            if (progressBar != null) progressBar.setVisibility(View.GONE);
        }
        ApiService api = retrofitClient.getRetrofitInstance().create(ApiService.class);

        api.getKonsumen("Bearer " + token, "application/json").enqueue(new Callback<List<Konsumen>>() {
            @Override
            public void onResponse(Call<List<Konsumen>> call, Response<List<Konsumen>> response) {
                // Cek fragment masih ada sebelum proses response
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
                if (recyclerViewKonsumen != null) recyclerViewKonsumen.setVisibility(View.VISIBLE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Konsumen> konsumenList = response.body();
                    Log.d(TAG, "Data Konsumen diterima: " + new Gson().toJson(konsumenList));

                    // Update data di adapter
                    if (konsumenAdapter != null) {
                        konsumenAdapter.updateData(konsumenList);
                    } else {
                        Log.e(TAG, "konsumenAdapter is null saat onResponse");
                    }

                } else {
                    String errorBody = "";
                    try { if (response.errorBody() != null) errorBody = response.errorBody().string(); } catch (Exception e) {}
                    Log.e(TAG, "Gagal mendapatkan data konsumen. Code: " + response.code() + ", Body: " + errorBody);
                    // Opsional: Tampilkan pesan error ke user
                     Toast.makeText(context, "Gagal memuat data konsumen: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Konsumen>> call, Throwable t) {
                // Cek fragment masih ada sebelum proses failure
                if (!isAdded()) return;

                // Sembunyikan loading, tampilkan RecyclerView (meskipun gagal, biar nggak kosong)
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (recyclerViewKonsumen != null) recyclerViewKonsumen.setVisibility(View.VISIBLE);
                Log.e(TAG, "Koneksi gagal saat get konsumen", t);
                // Opsional: Tampilkan pesan error ke user
                 Toast.makeText(context, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Method tampilkan dialog tambah konsumen
// --- Di dalam KonsumenListFragment.java ---

    public void showAddKonsumenDialog() {
        Context context = getContext();
        if (context == null) {
            Log.w(TAG, "Context is null, cannot show add konsumen dialog.");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        // Gunakan layout gabungan
        View dialogView = inflater.inflate(R.layout.dialog_add_data, null);
        builder.setView(dialogView);
        builder.setTitle("Tambah Konsumen Baru"); // Set judul

        // --- Ambil referensi SEMUA field (Item & Konsumen) ---
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
        layoutKeterangan.setVisibility(View.VISIBLE); // Keterangan ditampilkan (sesuai layout)

        // Ambil EditText khusus Konsumen (untuk ambil data nanti)
        EditText etKodeKonsumen = dialogView.findViewById(R.id.etKodeKonsumen);
        EditText etNamaKonsumen = dialogView.findViewById(R.id.etNamaKonsumen);
        EditText etTelp = dialogView.findViewById(R.id.etTelp);
        EditText etAlamat = dialogView.findViewById(R.id.etAlamat);
        EditText etPerusahaan = dialogView.findViewById(R.id.etPerusahaan);
        EditText etKeterangan = dialogView.findViewById(R.id.etKeterangan);

        builder.setPositiveButton("Tambah", (dialog, which) -> {
            String kode = etKodeKonsumen.getText().toString().trim();
            String nama = etNamaKonsumen.getText().toString().trim();
            String telp = etTelp.getText().toString().trim();
            String alamat = etAlamat.getText().toString().trim();
            String perusahaan = etPerusahaan.getText().toString().trim();
            String keterangan = etKeterangan.getText().toString().trim();

            // Validasi input konsumen (sesuaikan mana yang wajib)
            if (kode.isEmpty() || nama.isEmpty() || telp.isEmpty() || alamat.isEmpty() || perusahaan.isEmpty()) {
                Toast.makeText(context, "Kode, Nama, Telp, Alamat, dan Perusahaan wajib diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            // Panggil fungsi tambah konsumen
            addKonsumenToServer(kode, nama, telp, alamat, perusahaan, keterangan);
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
    // --- Letakkan method ini di dalam KonsumenListFragment.java ---

    private void addKonsumenToServer(String kode, String nama, String telp, String alamat, String perusahaan, String keterangan) {
        Context context = getContext();
        if (context == null) {
            Log.e(TAG, "Context is null in addKonsumenToServer");
            return; // Keluar jika context null
        }

        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("access_token", null);
        if (token == null) {
            Toast.makeText(context, "Token tidak ditemukan, silakan login ulang", Toast.LENGTH_SHORT).show();
            // Mungkin redirect ke login
            return;
        }

        // Pastikan retrofitClient sudah diinisialisasi (misal di onViewCreated)
        if (retrofitClient == null) {
            Toast.makeText(context, "Kesalahan: Klien service belum siap", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "retrofitClient is null in addKonsumenToServer");
            return;
        }

        ApiService api = retrofitClient.getRetrofitInstance().create(ApiService.class);

        // Buat objek Konsumen baru dari data dialog
        // Pastikan constructor di Konsumen.java menerima parameter ini
        Konsumen newKonsumen = new Konsumen(kode, nama, telp, alamat, perusahaan, keterangan);

        // Panggil method addKonsumen dari ApiService
        api.addKonsumen("Bearer " + token, "application/json", newKonsumen).enqueue(new Callback<Konsumen>() { // Sesuaikan Tipe <Konsumen> jika API mengembalikan data lain
            @Override
            public void onResponse(Call<Konsumen> call, Response<Konsumen> response) {
                // Cek fragment masih ada
                if (!isAdded() || context == null) return;

                if (response.isSuccessful()) {
                    // Berhasil menambahkan
                    Toast.makeText(context, "Konsumen berhasil ditambah", Toast.LENGTH_SHORT).show();

                    // Refresh daftar konsumen untuk menampilkan data baru
                    getKonsumenFromServer();

                    // Dialog akan tertutup otomatis karena ini dipanggil dari PositiveButton

                } else {
                    // Gagal menambahkan
                    String errorBody = "";
                    try { if (response.errorBody() != null) errorBody = response.errorBody().string(); } catch (Exception e) {}
                    Log.e(TAG, "Gagal menambah konsumen. Kode: " + response.code() + " Body: " + errorBody);
                    Toast.makeText(context, "Gagal menambah konsumen (Code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Konsumen> call, Throwable t) {
                // Cek fragment masih ada
                if (!isAdded() || context == null) return;

                // Gagal koneksi
                Log.e(TAG, "Koneksi gagal saat add konsumen", t);
                Toast.makeText(context, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}