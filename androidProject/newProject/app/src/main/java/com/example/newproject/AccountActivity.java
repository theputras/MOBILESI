package com.example.newproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;

import okhttp3.ResponseBody; // Import ResponseBody
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountActivity extends AppCompatActivity {

    private static final String TAG = "AccountActivity";

    private TextView textViewUserInfo;
    private Button action_logout, buttonLogoutAll;
    private BottomNavigationView bottomNavigationView;
    private RetrofitClient retrofitClient; // Untuk panggil API

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        retrofitClient = RetrofitClient.getInstance();

        // Setup Toolbar (Opsional)
        Toolbar toolbar = findViewById(R.id.accountToolbar);
        setSupportActionBar(toolbar);
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Tidak perlu jika pakai BottomNav

        // Inisialisasi Views
        textViewUserInfo = findViewById(R.id.textViewUserInfo);
        action_logout = findViewById(R.id.action_logout);
        buttonLogoutAll = findViewById(R.id.buttonLogoutAll);
        bottomNavigationView = findViewById(R.id.bottomNavigationViewAccount);

        // TODO: Ambil data user dari SharedPreferences atau API dan tampilkan di textViewUserInfo
        // Contoh: SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        // String userName = prefs.getString("user_name", "User"); // Asumsi nama disimpan
        // textViewUserInfo.setText("Selamat datang, " + userName);
        textViewUserInfo.setText("User"); // Placeholder

        // Setup Listener Tombol
        action_logout.setOnClickListener(v -> {
            // Tampilkan konfirmasi sebelum logout
            new AlertDialog.Builder(this)
                    .setTitle("Konfirmasi Logout")
                    .setMessage("Anda yakin ingin logout dari akun ini?")
                    .setPositiveButton("Logout", (dialog, which) -> doLogout()) // Panggil doLogout jika ya
                    .setNegativeButton("Batal", null)
                    .show();
        });

        buttonLogoutAll.setOnClickListener(v -> {
            // Tampilkan konfirmasi sebelum logout all
            new AlertDialog.Builder(this)
                    .setTitle("Konfirmasi Logout Semua")
                    .setMessage("Anda yakin ingin logout dari semua perangkat?")
                    .setPositiveButton("Logout Semua", (dialog, which) -> logoutAllDevices()) // Panggil logoutAll jika ya
                    .setNegativeButton("Batal", null)
                    .show();
        });

        // Setup Bottom Navigation
        bottomNavigationView.setSelectedItemId(R.id.nav_account); // Tandai Account sebagai aktif
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Kembali ke MainActivity
                Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); // Bawa ke depan jika sudah ada
                startActivity(intent);
                overridePendingTransition(0, 0);
                // finish(); // Opsi: tutup AccountActivity saat kembali ke Home
                return true;
            } else if (itemId == R.id.nav_account) {
                // Sudah di halaman Account, tidak perlu aksi
                return true;
            }
            return false;
        });
    }

    // --- Method Logout Biasa (Mirip MainActivity) ---
    private void doLogout() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("access_token", null);

        // Hapus Cookie & Cache (Bisa dipanggil juga)
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
        new ClearCacheTask(this).execute();

        // Hapus token lokal
        prefs.edit().remove("access_token").apply();
        // Hapus info user lain jika disimpan di prefs
        // prefs.edit().remove("user_name").apply();

        // Panggil API Logout (Opsional, tapi bagus)
        if (token != null && retrofitClient != null) {
            ApiService apiService = retrofitClient.getRetrofitInstance().create(ApiService.class);
            apiService.logout("Bearer " + token).enqueue(new Callback<LogoutResponse>() {
                @Override
                public void onResponse(Call<LogoutResponse> call, Response<LogoutResponse> response) {
                    Log.i(TAG, "API Logout response: " + response.code());
                    navigateToLogin(); // Tetap ke login meskipun API gagal
                }
                @Override
                public void onFailure(Call<LogoutResponse> call, Throwable t) {
                    Log.e(TAG, "API Logout failure: " + t.getMessage());
                    navigateToLogin(); // Tetap ke login
                }
            });
        } else {
            navigateToLogin(); // Langsung ke login jika token tidak ada
        }
    }

    // --- Method Logout Semua Perangkat ---
    private void logoutAllDevices() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("access_token", null);

        // 1. Hapus WebView Cookies (Jalankan di UI thread)
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookies(null); // Menghapus semua cookie
        cookieManager.flush(); // Menyimpan perubahan

        // 2. Hapus Cache Aplikasi (Jalankan di background thread)
        //    Gunakan AsyncTask atau thread lain agar tidak memblokir UI thread
        new MainActivity.ClearCacheTask(this).execute(); // Panggil AsyncTask

        // 3. Hapus Token dari SharedPreferences (Ini sudah ada)
        prefs.edit().remove("access_token").apply();

        if (token == null || retrofitClient == null) {
            Toast.makeText(this, "Tidak bisa logout semua perangkat. Coba login ulang.", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = retrofitClient.getRetrofitInstance().create(ApiService.class);

        // Panggil API logoutAll (Pastikan method ini ada di ApiService.java)
        Call<ResponseBody> call = apiService.logoutAll("Bearer " + token); // Ganti ResponseBody jika perlu

        // Tampilkan loading indicator?
        Toast.makeText(this, "Memproses logout...", Toast.LENGTH_SHORT).show();

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if(response.code() == 401){
                    handleUnauthorized(); // Handle jika token sudah invalid saat panggil API ini
                    return;
                }

                if (response.isSuccessful()) {
                    Log.i(TAG, "API Logout All successful.");
                    // Hapus data lokal setelah API berhasil
                    CookieManager.getInstance().removeAllCookies(null);
                    CookieManager.getInstance().flush();
                    new ClearCacheTask(AccountActivity.this).execute();
                    prefs.edit().remove("access_token").apply();
                    // prefs.edit().remove("user_name").apply(); // Hapus data user lain
                    navigateToLogin(); // Arahkan ke login
                } else {
                    Log.w(TAG, "API Logout All failed. Code: " + response.code());
                    Toast.makeText(AccountActivity.this, "Gagal logout semua perangkat (Code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "API Logout All error: " + t.getMessage());
                Toast.makeText(AccountActivity.this, "Koneksi gagal saat logout semua: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    // --- Helper Methods (Mirip MainActivity) ---
    private void navigateToLogin() {
        runOnUiThread(() -> {
            Toast.makeText(AccountActivity.this, "Logout sukses", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Hapus backstack
            startActivity(intent);
            finish(); // Tutup AccountActivity
        });
    }

    private void handleUnauthorized() { // Perlu method ini juga
        Toast.makeText(this, "Sesi berakhir, silakan login ulang.", Toast.LENGTH_LONG).show();
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        prefs.edit().remove("access_token").apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


    // AsyncTask Clear Cache (Sama seperti MainActivity, bisa dibuat class terpisah)
    private static class ClearCacheTask extends AsyncTask<Void, Void, Boolean> {
        private final Context context;
        ClearCacheTask(Context context) { this.context = context.getApplicationContext(); }
        @Override protected Boolean doInBackground(Void... voids) { /* ... Hapus Cache ... */
            try { File cacheDir = context.getCacheDir(); return deleteDir(cacheDir); }
            catch (Exception e) { Log.e("ClearCacheTask", "Gagal hapus cache", e); return false; }
        }
        @Override protected void onPostExecute(Boolean success) { /* ... Log hasil ... */ }
        private boolean deleteDir(File dir) { /* ... Logika hapus direktori ... */
            if (dir != null && dir.isDirectory()) { String[] children = dir.list(); if (children != null) { for (String child : children) { boolean success = deleteDir(new File(dir, child)); if (!success) { return false; } } } return dir.delete(); }
            else if (dir != null && dir.isFile()) { return dir.delete(); } else { return false; }
        }
    }

    // Handle back button di toolbar (jika ada)
    // @Override
    // public boolean onOptionsItemSelected(MenuItem item) {
    //     if (item.getItemId() == android.R.id.home) {
    //         onBackPressed(); // Kembali ke activity sebelumnya (MainActivity)
    //         return true;
    //     }
    //     return super.onOptionsItemSelected(item);
    // }
}