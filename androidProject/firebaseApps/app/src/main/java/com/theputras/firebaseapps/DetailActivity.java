package com.theputras.firebaseapps;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.theputras.firebaseapps.utils.BluetoothHelper;

public class DetailActivity extends AppCompatActivity {

    private TextView tvProdi, tvNama, tvNim, tvTtl, tvUmur;
    private Button btnPrint;
    private String prodi, nama, nim, ttl, umur;
    private TextView tvStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail); // Kita buat layout ini di langkah 5

        tvProdi = findViewById(R.id.tv_data_prodi);
        tvNama = findViewById(R.id.tv_data_nama);
        tvNim = findViewById(R.id.tv_data_nim);
        tvTtl = findViewById(R.id.tv_data_ttl);
        tvUmur = findViewById(R.id.tv_data_umur);
        btnPrint = findViewById(R.id.btn_data_print);
        tvStatus = findViewById(R.id.tv_status_print);

        if (getIntent() != null) {
            prodi = getIntent().getStringExtra("prodi");
            nama = getIntent().getStringExtra("nama");
            nim = getIntent().getStringExtra("nim");
            ttl = getIntent().getStringExtra("ttl");
            umur = getIntent().getStringExtra("umur");

            tvProdi.setText(prodi);
            tvNama.setText(nama);
            tvNim.setText(nim);
            tvTtl.setText(ttl);
            tvUmur.setText(umur);
        }

        btnPrint.setOnClickListener(v -> printDataMahasiswa());
    }

    private void printDataMahasiswa() {
        BluetoothHelper bt = BluetoothHelper.getInstance();
        if (!bt.isConnected()) {
            Toast.makeText(this, "Printer Belum Konek! Masuk menu koneksi dulu.", Toast.LENGTH_LONG).show();
            return;
        }

        new Thread(() -> {
            bt.resetPrinter();

            // HEADER (CENTER)
            bt.setAlign(1);
            bt.printText("DATA MAHASISWA\n");
            bt.printText("--------------------------------\n");

            // BODY (LEFT)
            bt.setAlign(0);
            bt.printText("NIM  : " + nim + "\n");
            bt.printText("NAMA : " + nama + "\n");
            bt.printText("PRODI: "+ prodi + "\n");
            bt.printText("TTL  : " + ttl + "\n");
            bt.printText("UMUR : " + umur + " Tahun\n");

            // FOOTER (CENTER)
            bt.printText("\n");
            bt.setAlign(1);
            bt.printText("--------------------------------\n");
            bt.printText("Terima Kasih\n\n\n");
        }).start();
    }
    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }
    private void updateStatus() {
        if (BluetoothHelper.getInstance().isConnected()) {
            String deviceName = BluetoothHelper.getInstance().getConnectedDeviceName();
            tvStatus.setText("Siap Mencetak ke: " + deviceName);
            tvStatus.setTextColor(Color.parseColor("#006400")); // Hijau
            btnPrint.setEnabled(true);
        } else {
            tvStatus.setText("Status: Printer Belum Terkoneksi");
            tvStatus.setTextColor(Color.RED);
            btnPrint.setEnabled(false); // Matikan tombol print kalau belum konek
        }
    }
}