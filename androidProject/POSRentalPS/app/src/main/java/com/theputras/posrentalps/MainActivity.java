package com.theputras.posrentalps;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.theputras.posrentalps.adapter.PaketAdapter;
import com.theputras.posrentalps.adapter.TvGridAdapter;
import com.theputras.posrentalps.api.ApiClient;
import com.theputras.posrentalps.api.ApiService;
import com.theputras.posrentalps.model.PaketSewa;
import com.theputras.posrentalps.model.TransactionRequest;
import com.theputras.posrentalps.model.Tv;
import com.theputras.posrentalps.model.TvResponse;
import com.theputras.posrentalps.utils.CartManager;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerTv;
    private TvGridAdapter adapter;
    private List<PaketSewa> allPakets = new ArrayList<>();
    private ExtendedFloatingActionButton fabCart; // Pake Extended biar mirip tombol checkout di gambar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerTv = findViewById(R.id.recyclerTvGrid);
        fabCart = findViewById(R.id.fabCart);

        // Grid 2 Kolom
        recyclerTv.setLayoutManager(new GridLayoutManager(this, 2));

        // Load Data
        fetchMasterData();

        // Tombol Cart di bawah (Floating)
        fabCart.setOnClickListener(v -> {
            if (!CartManager.getInstance().getDisplayList().isEmpty()) {
                startActivity(new Intent(this, PaymentActivity.class));
            } else {
                Toast.makeText(this, "Keranjang Kosong", Toast.LENGTH_SHORT).show();
            }
        });

        updateCartUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartUI();
        fetchTvs(); // Refresh status TV saat kembali ke menu utama
    }

    private void updateCartUI() {
        int count = CartManager.getInstance().getDisplayList().size();
        int total = CartManager.getInstance().getTotalPrice();

        // Debugging: Pastikan log ini muncul di Logcat saat klik "Tambah"
        android.util.Log.d("CART_CHECK", "Item di keranjang: " + count);

        // Ambil referensi badge merah (pastikan ID di XML benar: tvCartCount)
        TextView badge = findViewById(R.id.tvCartCount);

        if (count > 0) {
            // --- BAGIAN INI YANG BIKIN MUNCUL ---
            // 1. Paksa VISIBLE dulu (Jurus Ampuh)
            if (fabCart.getVisibility() != View.VISIBLE) {
                fabCart.setVisibility(View.VISIBLE);
            }

            // 2. Perpanjang tombol jika ciut
            if (!fabCart.isExtended()) {
                fabCart.extend();
            }

            // 3. Set Teks & Tampilkan
            fabCart.setText("Bayar (" + count + ") - Rp " + total);
            fabCart.show();

            // 4. Munculkan Badge Merah
            if (badge != null) {
                badge.setVisibility(View.VISIBLE);
                badge.setText(String.valueOf(count));
            }
        } else {
            fabCart.hide();
            fabCart.setVisibility(View.GONE); // Pastikan hilang kalau kosong
            if (badge != null) badge.setVisibility(View.GONE);
        }
    }

    private void fetchMasterData() {
        // Ambil Paket dulu untuk cache local
        ApiClient.getClient().create(ApiService.class).getPakets().enqueue(new Callback<List<PaketSewa>>() {
            @Override
            public void onResponse(Call<List<PaketSewa>> call, Response<List<PaketSewa>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allPakets = response.body();
                    fetchTvs(); // Setelah paket siap, ambil TV
                }
            }
            @Override public void onFailure(Call<List<PaketSewa>> c, Throwable t) {}
        });
    }

    private void fetchTvs() {
        ApiClient.getClient().create(ApiService.class).getAvailableTvs().enqueue(new Callback<TvResponse>() {
            @Override
            public void onResponse(Call<TvResponse> call, Response<TvResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter = new TvGridAdapter(MainActivity.this, response.body().getData(), tv -> {
                        if (tv.getStatus().equalsIgnoreCase("available")) {
                            showPaketDialog(tv);
                        } else {
                            Toast.makeText(MainActivity.this, "TV sedang dipakai", Toast.LENGTH_SHORT).show();
                        }
                    });
                    recyclerTv.setAdapter(adapter);
                }
            }
            @Override public void onFailure(Call<TvResponse> c, Throwable t) {}
        });
    }

    // --- Bottom Sheet (Mirip Gambar 2 - Menu Item) ---

    private void showPaketDialog(Tv tv) {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.layout_pilih_paket, null);

        TextView title = view.findViewById(R.id.tvTitleSheet);
        RecyclerView recyclerPaket = view.findViewById(R.id.recyclerPaket);

        String consoleTv = (tv.getJenisConsole() != null) ? tv.getJenisConsole().getNamaConsole() : "";
        title.setText("Pilih Paket - " + consoleTv);

        // --- FILTER PAKET (Logic Sementara: Tampilkan Semua biar aman) ---
        List<PaketSewa> filtered = new ArrayList<>();
        for (PaketSewa p : allPakets) {
            // Bisa diperketat nanti pakai p.namaConsole.contains(...)
            filtered.add(p);
        }
        // -------------------------------------------------------------

        recyclerPaket.setLayoutManager(new GridLayoutManager(this, 1));

        // ADAPTER CLICK LISTENER
        PaketAdapter paketAdapter = new PaketAdapter(filtered, selectedPaket -> {

            // 1. Cek dulu apakah TV ini sudah ada di cart? (Mencegah Double Booking)
            boolean isExist = false;
            for (CartManager.CartDisplay item : CartManager.getInstance().getDisplayList()) {
                if (item.request.tvId == tv.getId()) {
                    isExist = true;
                    break;
                }
            }

            if (isExist) {
                Toast.makeText(this, "TV ini sudah ada di keranjang!", Toast.LENGTH_SHORT).show();
            } else {
                // 2. LANGSUNG ADD TO CART (Tanpa Qty Dialog)
                TransactionRequest req = new TransactionRequest(
                        "Guest", // Default nama sementara
                        tv.getId(),
                        selectedPaket.idPaket,
                        selectedPaket.harga
                );

                CartManager.getInstance().addItem(
                        req,
                        "TV " + tv.getNomorTv() + " - " + selectedPaket.namaPaket,
                        selectedPaket.harga,
                        1 // Hardcode Qty = 1
                );

                updateCartUI(); // Update tombol bayar
                Toast.makeText(this, "Berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                dialog.dismiss(); // Tutup popup paket
            }
        });

        recyclerPaket.setAdapter(paketAdapter);
        dialog.setContentView(view);
        dialog.show();
    }

    // ... HAPUS method showQtyDialog karena sudah tidak dipakai ...

    // Saran: Pisahkan logic popup jumlah biar kodingan showPaketDialog ga kepanjangan
    private void showQtyDialog(Tv tv, PaketSewa selectedPaket, BottomSheetDialog parentDialog) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Masukkan Jumlah");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText("1");
        builder.setView(input);

        builder.setPositiveButton("Tambah", (d, w) -> {
            int qty = Integer.parseInt(input.getText().toString());

            // Masukkan keranjang
            TransactionRequest req = new TransactionRequest(
                    "Guest", tv.getId(), selectedPaket.idPaket, selectedPaket.harga
            );

            CartManager.getInstance().addItem(
                    req,
                    "TV " + tv.getNomorTv() + " - " + selectedPaket.namaPaket,
                    selectedPaket.harga,
                    qty
            );

            // UPDATE UI & LOG
            updateCartUI();
            android.util.Log.d("CART_ACTION", "Berhasil tambah item. Total skrg: " + CartManager.getInstance().getDisplayList().size());

            parentDialog.dismiss();
        });
        builder.show();
    }
}