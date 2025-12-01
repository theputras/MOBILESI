package com.theputras.firebaseapps;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.theputras.firebaseapps.utils.BluetoothHelper;

public class PrintTextActivity extends AppCompatActivity {

    private TextView tvStatus;
    private TextInputEditText etInputText;
    private Button btnPrint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_text);

        // Inisialisasi View
        tvStatus = findViewById(R.id.tv_status_print);
        etInputText = findViewById(R.id.et_input_text);
        btnPrint = findViewById(R.id.btn_print_action);

        updateStatus();

        // Aksi Tombol Print
        btnPrint.setOnClickListener(v -> {
            String textToPrint = etInputText.getText().toString();

            if (textToPrint.isEmpty()) {
                etInputText.setError("Teks tidak boleh kosong!");
                return;
            }

            if (!BluetoothHelper.getInstance().isConnected()) {
                Toast.makeText(this, "Printer belum terkoneksi!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Proses Print
            boolean isSuccess = BluetoothHelper.getInstance().printText(textToPrint);

            if (isSuccess) {
                Toast.makeText(this, "Berhasil dikirim ke printer", Toast.LENGTH_SHORT).show();
                etInputText.setText(""); // Kosongkan input setelah print
            } else {
                Toast.makeText(this, "Gagal print. Cek koneksi.", Toast.LENGTH_SHORT).show();
                updateStatus(); // Update status jaga-jaga kalau koneksi putus
            }
        });
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