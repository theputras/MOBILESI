package com.example.newproject;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddItemActivity extends AppCompatActivity {

    private TextInputEditText etKodeItem, etNamaItem, etSatuan, etHargaBeli, etHargaJual;
    private Button btnSimpan;
    private static final String TAG = "AddItemActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        etKodeItem = findViewById(R.id.etKodeItem);
        etNamaItem = findViewById(R.id.etNamaItem);
        etSatuan = findViewById(R.id.etSatuan);
        etHargaBeli = findViewById(R.id.etHargaBeli);
        etHargaJual = findViewById(R.id.etHargaJual);
        btnSimpan = findViewById(R.id.btnSimpan);

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpanData();
            }
        });
    }

    private void simpanData() {
        String kodeItem = etKodeItem.getText().toString().trim();
        String namaItem = etNamaItem.getText().toString().trim();
        String satuan = etSatuan.getText().toString().trim();
        String hargaBeliStr = etHargaBeli.getText().toString().trim();
        String hargaJualStr = etHargaJual.getText().toString().trim();

        if (kodeItem.isEmpty() || namaItem.isEmpty() || satuan.isEmpty() || hargaBeliStr.isEmpty() || hargaJualStr.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        int hargaBeli = Integer.parseInt(hargaBeliStr);
        int hargaJual = Integer.parseInt(hargaJualStr);

        Item newItem = new Item(0, kodeItem, namaItem, satuan, hargaBeli, hargaJual);
        addItemToServer(newItem);
    }

    private void addItemToServer(Item item) {
        ApiService apiService = RetrofitClient.getInstance().getRetrofitInstance().create(ApiService.class);
        apiService.addItem(item).enqueue(new Callback<Item>() {
            @Override
            public void onResponse(Call<Item> call, Response<Item> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddItemActivity.this, "Item berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                    finish(); // Tutup activity ini dan kembali ke MainActivity
                } else {
                    Log.e(TAG, "Gagal menambah item. Kode: " + response.code());
                    Toast.makeText(AddItemActivity.this, "Gagal menambah item", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Item> call, Throwable t) {
                Log.e(TAG, "Koneksi gagal", t);
                Toast.makeText(AddItemActivity.this, "Koneksi gagal", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
