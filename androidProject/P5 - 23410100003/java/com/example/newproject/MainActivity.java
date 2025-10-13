package com.example.newproject;

import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson; // <-- Tambahkan import ini

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fabAddItem;
    private RetrofitClient retrofitClient;
    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        retrofitClient = RetrofitClient.getInstance();
        fabAddItem = findViewById(R.id.fabAddItem);


        recyclerView = findViewById(R.id.recyclerViewItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Kita inisialisasi adapter dengan list kosong dulu
        itemAdapter = new ItemAdapter(new ArrayList<>());
        recyclerView.setAdapter(itemAdapter);

        fabAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Buka AddItemActivity saat tombol di klik
                Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
                startActivity(intent);
            }
        });

        getItemsFromServer();
    }
    @Override
    protected void onResume() {
        super.onResume();
        getItemsFromServer(); // Panggil data dari server setiap kali activity ini ditampilkan
    }
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemAdapter = new ItemAdapter(new ArrayList<>());
        recyclerView.setAdapter(itemAdapter);
    }
    private void getItemsFromServer() {
        ApiService apiService = retrofitClient.getRetrofitInstance().create(ApiService.class);
        apiService.getItems().enqueue(new Callback<List<Item>>() { // Pastikan ini memanggil getItems()
            @Override
            public void onResponse(Call<List<Item>> call, Response<List<Item>> response) {
                if (response.isSuccessful()) {
                    List<Item> items = response.body();

                    // --- INI PERUBAHANNYA ---
                    // Ubah List<Item> menjadi JSON String menggunakan Gson
                    Gson gson = new Gson();
                    String jsonItems = gson.toJson(items);

                    itemAdapter = new ItemAdapter(items);
                    recyclerView.setAdapter(itemAdapter);
                    // Tampilkan JSON String di log
                    Log.d(TAG, "Data Items: " + jsonItems);

                } else {
                    Log.e(TAG, "Gagal mendapatkan data. Kode: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Item>> call, Throwable t) {
                Log.e(TAG, "Koneksi gagal", t);
            }
        });
    }

    private void addItemToServer(Item item) {
        // ... (kode untuk menambah item tetap sama)
    }
}