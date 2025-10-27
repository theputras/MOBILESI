package com.example.newproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Context;
import android.view.View;
import android.widget.Toast;
// Hapus import yang tidak perlu: RecyclerView, LinearLayoutManager, ItemAdapter, Gson, dll.
import android.os.AsyncTask; // Diperlukan untuk clear cache di background
import android.util.Log; // Untuk logging error cache
import android.webkit.CookieManager; // Untuk hapus cookie WebView

import java.io.File; // Untuk hapus cache
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2; // Import ViewPager2

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout; // Import TabLayout
import com.google.android.material.tabs.TabLayoutMediator; // Import TabLayoutMediator

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // Hapus: ImageButton btnLogout, RecyclerView recyclerView, ItemAdapter itemAdapter;
    private FloatingActionButton fabAddData;
    private RetrofitClient retrofitClient; // Mungkin tidak perlu di sini lagi jika semua di Fragment
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Inisialisasi ViewPager, TabLayout, FAB
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        fabAddData = findViewById(R.id.fabAddData);
        retrofitClient = RetrofitClient.getInstance(); // Mungkin dipindah ke Fragment
        bottomNavigationView = findViewById(R.id.bottomNavigationView); // Inisialisasi BottomNav


        // Setup ViewPager Adapter
        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        // Hubungkan TabLayout dengan ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Item");
                    break;
                case 1:
                    tab.setText("Konsumen");
                    break;
            }
        }).attach();

        // --- Listener untuk BottomNavigationView (DIPERBARUI) ---
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Jika menu Home diklik, pastikan ViewPager menampilkan tab yg sesuai
                // (Biasanya tidak perlu aksi khusus jika sudah di MainActivity)
                // Opsi: Selalu kembali ke tab Item (index 0)
                // viewPager.setCurrentItem(0, false);
                return true; // Item sudah terpilih/ditangani
            } else if (itemId == R.id.nav_account) {
                // Jika menu Account diklik, buka AccountActivity BARU
                Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                // Flag agar tidak menumpuk Activity yg sama jika diklik berulang
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true; // Item sudah terpilih/ditangani
            }
            return false; // Item tidak dikenali
        });

        // --- Listener untuk ViewPager2 (Sederhana, hanya update TabLayout) ---
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Update TabLayout saja
                if (position < tabLayout.getTabCount()) {
                    tabLayout.selectTab(tabLayout.getTabAt(position));
                }
                // Pastikan Home tetap terpilih di BottomNav
                bottomNavigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
            }
        });
        // Pastikan item Home terpilih saat pertama kali buka
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        // Listener FAB (sementara masih di sini, bisa disesuaikan nanti)
        fabAddData.setOnClickListener(v -> {
             Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
             if (currentFragment instanceof ItemListFragment) {
                 ((ItemListFragment) currentFragment).showAddItemDialog();
             } else if (currentFragment instanceof KonsumenListFragment) {
                 ((KonsumenListFragment) currentFragment).showAddKonsumenDialog();
             }
         });

        // Hapus: recyclerView.setLayoutManager(...);
        // Hapus: itemAdapter = new ItemAdapter(...);
        // Hapus: recyclerView.setAdapter(...);
        // Hapus: btnLogout = findViewById(...);
        // Hapus: btnLogout.setOnClickListener(...);
        // Hapus: getItemsFromServer();
    }
// --- Tambahkan method ini di dalam MainActivity.java ---


    @Override
    protected void onResume() {
        super.onResume();
        // Panggil method helper untuk update BottomNav
        updateBottomNavSelection();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Panggil method helper juga saat activity dibawa ke depan via intent baru
        updateBottomNavSelection();
        // (Jika ada data lain yang dikirim via intent, proses di sini)
    }

    // Method helper baru untuk memastikan Home terpilih
    private void updateBottomNavSelection() {
        if (bottomNavigationView != null) {
            // Cek dulu apakah item Home sudah terpilih atau belum
            // Ini mencegah re-selection yang mungkin mengganggu
            if (bottomNavigationView.getSelectedItemId() != R.id.nav_home) {
                Log.d(TAG, "Setting nav_home as selected in BottomNav");
                // Coba pakai setSelectedItemId, ini lebih kuat daripada setChecked
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
            }
        }
    }


    // Hapus: onResume() yang memanggil getItemsFromServer()

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu dari file main_menu.xml
        getMenuInflater().inflate(R.menu.header_display, menu);
        return true; // Kembalikan true agar menu ditampilkan
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle klik item menu (misal: logout)
        if (item.getItemId() == R.id.action_logout) { // Pastikan ID item logout benar
            doLogout(); // Panggil fungsi logout kamu
            return true; // Kembalikan true karena event sudah ditangani
        }
        return super.onOptionsItemSelected(item); // Biarkan sistem handle item lain
    }

    // Hapus method: showAddItemDialog(), addItemToServer(), getItemsFromServer()
    // (Semua method ini harus dipindahkan ke ItemListFragment)

    // Method doLogout() tetap di MainActivity
    public void doLogout() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("access_token", null);

        // 1. Hapus WebView Cookies (Jalankan di UI thread)
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookies(null); // Menghapus semua cookie
        cookieManager.flush(); // Menyimpan perubahan

        // 2. Hapus Cache Aplikasi (Jalankan di background thread)
        //    Gunakan AsyncTask atau thread lain agar tidak memblokir UI thread
        new ClearCacheTask(this).execute(); // Panggil AsyncTask

        // 3. Hapus Token dari SharedPreferences (Ini sudah ada)
        prefs.edit().remove("access_token").apply();

        // 4. Proses Panggil API Logout (Jika API logoutnya penting)
        //    (Kode ini bisa ditaruh sebelum atau sesudah hapus token/cache,
        //     tergantung apakah API butuh token untuk logout)
        if (token != null && retrofitClient != null) {
            ApiService apiService = retrofitClient.getRetrofitInstance().create(ApiService.class);
            Call<LogoutResponse> call = apiService.logout("Bearer " + token);

            call.enqueue(new Callback<LogoutResponse>() {
                @Override
                public void onResponse(Call<LogoutResponse> call, Response<LogoutResponse> response) {
                    if (response.isSuccessful()) {
                        Log.i(TAG, "API logout successful.");
                        // Lanjutkan ke langkah 5 setelah API berhasil (atau gagal tapi tetap mau lanjut)
                        navigateToLogin();
                    } else {
                        Log.w(TAG, "API logout failed. Code: " + response.code());
                        // Tetap lanjutkan logout di sisi client meskipun API gagal
                        navigateToLogin();
                    }
                }

                @Override
                public void onFailure(Call<LogoutResponse> call, Throwable t) {
                    Log.e(TAG, "API logout error: " + t.getMessage());
                    // Tetap lanjutkan logout di sisi client meskipun API gagal
                    navigateToLogin();
                }
            });
        } else {
            // Jika token null atau retrofitClient null, langsung ke login
            navigateToLogin();
        }
    }

    // Method helper untuk pindah ke LoginActivity
    private void navigateToLogin() {
        // Pastikan dijalankan di UI thread jika dipanggil dari background callback
        runOnUiThread(() -> {
            Toast.makeText(MainActivity.this, "Logout sukses", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish(); // Tutup MainActivity
        });
    }

    // AsyncTask untuk menghapus cache di background
    public static class ClearCacheTask extends AsyncTask<Void, Void, Boolean> {
        private final Context context;

        ClearCacheTask(Context context) {
            this.context = context.getApplicationContext(); // Gunakan application context
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                File cacheDir = context.getCacheDir();
                return deleteDir(cacheDir);
            } catch (Exception e) {
                Log.e("ClearCacheTask", "Gagal menghapus cache", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Log.i("ClearCacheTask", "Cache aplikasi berhasil dihapus.");
            } else {
                Log.w("ClearCacheTask", "Gagal menghapus sebagian atau seluruh cache.");
            }
        }

        // Fungsi rekursif untuk menghapus direktori dan isinya
        private boolean deleteDir(File dir) {
            if (dir != null && dir.isDirectory()) {
                String[] children = dir.list();
                if (children != null) {
                    for (String child : children) {
                        boolean success = deleteDir(new File(dir, child));
                        if (!success) {
                            return false; // Hentikan jika salah satu file/sub-dir gagal dihapus
                        }
                    }
                }
                return dir.delete(); // Hapus direktori itu sendiri setelah isinya kosong
            } else if (dir != null && dir.isFile()) {
                return dir.delete(); // Hapus file
            } else {
                return false;
            }
        }
    }
}