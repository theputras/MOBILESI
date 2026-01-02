package com.theputras.posrentalps;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.theputras.posrentalps.utils.CartManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StrukActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_struk);

        LinearLayout container = findViewById(R.id.containerItems);
        TextView tvDate = findViewById(R.id.tvStrukDate);
        TextView tvName = findViewById(R.id.tvStrukName);
        TextView tvTotal = findViewById(R.id.tvStrukTotal);
        TextView tvCash = findViewById(R.id.tvStrukCash);
        TextView tvChange = findViewById(R.id.tvStrukChange);
        Button btnBack = findViewById(R.id.btnBackHome);
        Button btnPrint = findViewById(R.id.btnPrint);

        // Ambil Data Intent
        String custName = getIntent().getStringExtra("CUSTOMER_NAME");
        String historyDate = getIntent().getStringExtra("TANGGAL");
        int historyTotal = getIntent().getIntExtra("TOTAL_TAGIHAN", 0); // Penanda History
        String historyTv = getIntent().getStringExtra("NOMOR_TV");
        int historyDurasi = getIntent().getIntExtra("DURASI", 0);
        int cashGiven = getIntent().getIntExtra("CASH_GIVEN", 0);
        String nomorTv = getIntent().getStringExtra("NOMOR_TV");
        String namaConsole = getIntent().getStringExtra("NAMA_CONSOLE");
        // 1. SET NAMA
        tvName.setText(custName != null ? custName : "Guest");

        // 2. LOGIC CERDAS (HISTORY vs TRANSAKSI BARU)
        if (historyTotal > 0) {
            tvDate.setText(historyDate != null ? historyDate : "-");

            // Format Tampilan Item: "TV 02 - PlayStation 5"
            String deskripsi = (nomorTv != null ? nomorTv : "");
            if (namaConsole != null && !namaConsole.isEmpty()) {
                deskripsi += " - " + namaConsole;
            }

            addItemToStruk(container, deskripsi, historyTotal);

            tvTotal.setText(String.format("Rp %,d", historyTotal));
            tvCash.setText(String.format("Rp %,d", historyTotal));
            tvChange.setText("Rp 0");
        } else {
            // === MODE TRANSAKSI BARU ===
            // Pakai Tanggal Sekarang
            String now = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
            tvDate.setText(now);

            int total = 0;
            // Ambil dari CartManager
            if (CartManager.getInstance().getDisplayList() != null) {
                for (CartManager.CartDisplay item : CartManager.getInstance().getDisplayList()) {
                    addItemToStruk(container, item.displayName, item.price);
                    total += item.price;
                }
            }

            // Hitung Kembalian
            if (cashGiven == 0) cashGiven = total;
            int change = cashGiven - total;

            tvTotal.setText(String.format("Rp %,d", total));
            tvCash.setText(String.format("Rp %,d", cashGiven));
            tvChange.setText(String.format("Rp %,d", change));

            // HANYA CLEAR CART KALAU INI TRANSAKSI BARU
            CartManager.getInstance().clear();
        }

        // Tombol Kembali
        btnBack.setOnClickListener(v -> {
            // Kalau dari History, cukup finish() biar balik ke HistoryFragment
            // Kalau Transaksi Baru, balik ke Home
            if (historyTotal > 0) {
                finish();
            } else {
                Intent intent = new Intent(StrukActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        btnPrint.setOnClickListener(v -> Toast.makeText(this, "Mencetak...", Toast.LENGTH_SHORT).show());
    }

    private void addItemToStruk(LinearLayout container, String name, int price) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 4, 0, 4);
        row.setLayoutParams(params);

        TextView tvItem = new TextView(this);
        tvItem.setText(name);
        tvItem.setTextSize(12);
        tvItem.setTextColor(Color.BLACK);
        tvItem.setTypeface(Typeface.MONOSPACE);
        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        tvItem.setLayoutParams(itemParams);

        TextView tvPrice = new TextView(this);
        tvPrice.setText("Rp " + String.format("%,d", price));
        tvPrice.setTextSize(12);
        tvPrice.setTextColor(Color.BLACK);
        tvPrice.setTypeface(Typeface.MONOSPACE);
        tvPrice.setGravity(Gravity.END);

        row.addView(tvItem);
        row.addView(tvPrice);
        container.addView(row);
    }
}