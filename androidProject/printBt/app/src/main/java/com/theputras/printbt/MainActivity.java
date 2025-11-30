package com.theputras.printbt;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.theputras.printbt.utils.BluetoothHelper;

public class MainActivity extends AppCompatActivity {

    private TextView tvStatus;
    private Button btnMenuConnect, btnMenuPrint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi View
        tvStatus = findViewById(R.id.tv_status_main);
        btnMenuConnect = findViewById(R.id.btn_menu_connect);
        btnMenuPrint = findViewById(R.id.btn_menu_print);

        // Aksi Tombol Koneksi
        btnMenuConnect.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DeviceListActivity.class);
            startActivity(intent);
        });

        // Aksi Tombol Print Text
        btnMenuPrint.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PrintTextActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    // Fungsi untuk cek status koneksi dari BluetoothHelper
    private void updateStatus() {
        if (BluetoothHelper.getInstance().isConnected()) {
            String deviceName = BluetoothHelper.getInstance().getConnectedDeviceName();
            tvStatus.setText("Status: Terkoneksi ke " + deviceName);
            tvStatus.setTextColor(Color.parseColor("#006400")); // Hijau Tua
        } else {
            tvStatus.setText("Status: Belum Terkoneksi");
            tvStatus.setTextColor(Color.RED);
        }
    }
}