package com.theputras.firebaseapps;


import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.theputras.firebaseapps.utils.BluetoothHelper;
import com.theputras.firebaseapps.DeviceListActivity;

public class MainActivity extends AppCompatActivity {

    // ID channel notifikasi (dipakai juga di Fragment)
    public static final String CHANNEL_ID = "mahasiswa_channel";

    private static final int REQ_NOTIF_PERMISSION = 100;
    private TextView tvStatus;
    private Button btnMenuConnect, btnMenuPrint, btnMenuData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Bikin notification channel
        createNotificationChannel();

        // 2. Minta izin notifikasi (khusus Android 13+ / API 33 ke atas)
        requestNotificationPermissionIfNeeded();

        // Inisialisasi View
        tvStatus = findViewById(R.id.tv_status_main);
        btnMenuConnect = findViewById(R.id.btn_menu_connect);
        btnMenuPrint = findViewById(R.id.btn_menu_print);
        btnMenuData = findViewById(R.id.btn_menu_data);

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

        // 3. Tambahkan Aksi Tombol Cek Data
        btnMenuData.setOnClickListener(v -> {
            // Ini akan membuka List Mahasiswa
            Intent intent = new Intent(MainActivity.this, DataActivity.class);
            startActivity(intent);
        });
    }

    // Fungsi untuk bikin Notification Channel (wajib Android 8+)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = "Update Mahasiswa";
            String description = "Notifikasi perubahan data mahasiswa";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }
    // Fungsi untuk cek status koneksi dari BluetoothHelper
    private void updateStatus() {
        if (com.theputras.firebaseapps.utils.BluetoothHelper.getInstance().isConnected()) {
            String deviceName = com.theputras.firebaseapps.utils.BluetoothHelper.getInstance().getConnectedDeviceName();
            tvStatus.setText("Status: Terkoneksi ke " + deviceName);
            tvStatus.setTextColor(Color.parseColor("#006400")); // Hijau Tua
        } else {
            tvStatus.setText("Status: Belum Terkoneksi");
            tvStatus.setTextColor(Color.RED);
        }
    }

    // Fungsi untuk minta izin notif di Android 13+
    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
            // Cek apakah sudah diizinkan
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                // Kalau belum → minta izin ke user
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQ_NOTIF_PERMISSION
                );
            }
        }
    }
}