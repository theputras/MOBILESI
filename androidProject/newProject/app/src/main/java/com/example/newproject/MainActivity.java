package com.example.newproject;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    ImageButton btnLogout;
    private FloatingActionButton fabAddItem;
    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private RetrofitClient retrofitClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        retrofitClient = RetrofitClient.getInstance();
        fabAddItem = findViewById(R.id.fabAddItem);
        btnLogout = findViewById(R.id.btnLogout);
        recyclerView = findViewById(R.id.recyclerViewItems);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemAdapter = new ItemAdapter(new ArrayList<>());
        recyclerView.setAdapter(itemAdapter);

        fabAddItem.setOnClickListener(v -> showAddItemDialog());

        btnLogout.setOnClickListener(v -> doLogout());

        getItemsFromServer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getItemsFromServer(); // refresh data setiap balik ke main
    }
    // ===== Tambah Item langsung dari MainActivity =====
    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_item, null);
        builder.setView(dialogView);

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
                Toast.makeText(MainActivity.this, "Semua kolom wajib diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            int hargabeli = Integer.parseInt(beliStr);
            int hargajual = Integer.parseInt(jualStr);
            addItemToServer(kode, nama, satuan, hargabeli, hargajual);
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void addItemToServer(String kode, String nama, String satuan, int beli, int jual) {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("access_token", null);
        if (token == null) {
            Toast.makeText(this, "Token tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService api = retrofitClient.getRetrofitInstance().create(ApiService.class);
        Item newItem = new Item(kode, nama, satuan, beli, jual);

        api.addItem("Bearer " + token, "application/json", newItem).enqueue(new Callback<Item>() {
            @Override
            public void onResponse(Call<Item> call, Response<Item> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Item berhasil ditambah", Toast.LENGTH_SHORT).show();
                    getItemsFromServer();
                } else {
                    String err = "";
                    try { err = response.errorBody() != null ? response.errorBody().string() : ""; } catch (Exception ignored) {}
                    Log.e(TAG, "Gagal menambah item. Kode: " + response.code() + " body: " + err);
                    Toast.makeText(MainActivity.this, "Gagal menambah item (Code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Item> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getItemsFromServer() {

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("access_token", null);
        ApiService api = retrofitClient.getRetrofitInstance().create(ApiService.class);
        api.getItems("Bearer " + token, "application/json").enqueue(new Callback<List<Item>>() {
            @Override
            public void onResponse(Call<List<Item>> call, Response<List<Item>> response) {
                if (response.isSuccessful()) {
                    List<Item> items = response.body();
                    Gson gson = new Gson();
                    String jsonItems = gson.toJson(items);
                    Log.d(TAG, "Data Items: " + jsonItems);

                    itemAdapter = new ItemAdapter(items);
                    recyclerView.setAdapter(itemAdapter);
                } else {
                    Log.e(TAG, "Gagal mendapatkan data. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Item>> call, Throwable t) {
                Log.e(TAG, "Koneksi gagal", t);
            }
        });
    }

    private void doLogout() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("access_token", null);

        if (token == null) {
            Toast.makeText(this, "Token tidak ditemukan", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        ApiService apiService = RetrofitClient.getInstance().getRetrofitInstance().create(ApiService.class);
        Call<LogoutResponse> call = apiService.logout("Bearer " + token);

        call.enqueue(new Callback<LogoutResponse>() {
            @Override
            public void onResponse(Call<LogoutResponse> call, Response<LogoutResponse> response) {
                if (response.isSuccessful()) {
                    prefs.edit().remove("access_token").apply();
                    Toast.makeText(MainActivity.this, "Logout sukses", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Gagal logout. Code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LogoutResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
