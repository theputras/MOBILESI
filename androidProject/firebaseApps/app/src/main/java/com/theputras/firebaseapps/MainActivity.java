package com.theputras.firebaseapps;


import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    // ID channel notifikasi (dipakai juga di Fragment)
    public static final String CHANNEL_ID = "mahasiswa_channel";

    private static final int REQ_NOTIF_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Bikin notification channel
        createNotificationChannel();

        // 2. Minta izin notifikasi (khusus Android 13+ / API 33 ke atas)
        requestNotificationPermissionIfNeeded();
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