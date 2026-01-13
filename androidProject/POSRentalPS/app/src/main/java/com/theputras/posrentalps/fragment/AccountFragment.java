package com.theputras.posrentalps.fragment;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import android.graphics.Color;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.theputras.posrentalps.LoginActivity;
import com.theputras.posrentalps.R;
import com.theputras.posrentalps.api.ApiClient;
import com.theputras.posrentalps.api.ApiService;
import com.theputras.posrentalps.databinding.FragmentAccountBinding;
import com.theputras.posrentalps.utils.BluetoothUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    private BluetoothUtil bluetoothUtil;

    private SharedPreferences sharedPreferences;
    private ApiService apiService;

    // Launcher Izin Bluetooth
    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean allGranted = true;
                for (Boolean isGranted : result.values()) {
                    if (!isGranted) allGranted = false;
                }
                if (allGranted) {
                    showPrinterDialog();
                } else {
                    Toast.makeText(getContext(), "Izin Bluetooth Diperlukan!", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAccountBinding.inflate(inflater, container, false);
        bluetoothUtil = BluetoothUtil.getInstance(getContext());
        apiService = ApiClient.getClient(requireContext()).create(ApiService.class);
        sharedPreferences = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        updatePrinterStatusUI();
        updatePaperStatusUI();

        binding.btnPrinterSetting.setOnClickListener(v -> checkPermissionAndShowDialog());
        binding.btnPaperSetting.setOnClickListener(v -> showPaperSizeDialog());
        binding.btnLogout.setOnClickListener(v -> logoutProcess());
        return binding.getRoot();
    }

    private void logoutProcess() {
        // Ubah teks tombol biar user tau sedang loading
        binding.btnLogout.setEnabled(false);
        Toast.makeText(getContext(), "Sedang keluar...", Toast.LENGTH_SHORT).show();

        // Panggil API Logout
        apiService.logout().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // Mau sukses (200) atau gagal (401/500) di server,
                // kita tetap harus hapus sesi di HP agar user bisa login ulang.
                doLocalLogout();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Jika koneksi internet mati, paksa logout lokal saja
                Toast.makeText(getContext(), "Gagal terhubung server, memaksa keluar...", Toast.LENGTH_SHORT).show();
                doLocalLogout();
            }
        });
    }

    private void doLocalLogout() {
        if (getContext() == null) return;

        // 1. Hapus Semua Data di SharedPreferences (Token & Nama)
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // 2. Pindah ke LoginActivity
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        // Flag ini PENTING: Untuk menghapus history activity sebelumnya (MainActivity)
        // Jadi kalau user tekan BACK, aplikasi langsung keluar, bukan balik ke menu utama.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // 3. Tutup Fragment/Activity saat ini
        requireActivity().finish();
    }
    private void updatePaperStatusUI() {
        int width = bluetoothUtil.getPrinterWidth();

        if (width == 48) {
            binding.tvPaperStatus.setText("80mm (Besar)");
        } else if (width == 24) {
            binding.tvPaperStatus.setText("48mm (Mini)");
        } else {
            // Default 32 atau angka lain
            binding.tvPaperStatus.setText("58mm (Standar)");
        }
    }
    private void updatePrinterStatusUI() {
        if (bluetoothUtil.getSavedPrinterMac() != null) {
            binding.tvConnectedDevice.setText("Tersimpan");
            binding.tvConnectedDevice.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            binding.tvConnectedDevice.setText("Belum Diset");
            binding.tvConnectedDevice.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private void checkPermissionAndShowDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(new String[]{
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN
                });
                return;
            }
        }
        showPrinterDialog();
    }
    private void showPaperSizeDialog() {
        // Pilihan Menu
        String[] options = {
                "48mm (Mini/Saku)",
                "58mm (Standar/Mobile)",
                "80mm (Printer Kasir Besar)"
        };

        // Cari tahu mana yang lagi aktif sekarang buat set default check
        int currentWidth = bluetoothUtil.getPrinterWidth();
        int checkedItem = 1; // Kalau 48 berarti index 1, selain itu 0
        if (currentWidth == 24) {
            checkedItem = 0; // 48mm
        } else if (currentWidth == 32) {
            checkedItem = 1; // 58mm
        } else if (currentWidth == 48) {
            checkedItem = 2; // 80mm
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("Pilih Ukuran Kertas")
                .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                    // Logic simpan pilihan
                    if (which == 0) {
                        // 48mm -> Set 24 Karakter
                        bluetoothUtil.savePrinterWidth(24);
                    } else if (which == 1) {
                        // 58mm -> Set 32 Karakter (Standar)
                        bluetoothUtil.savePrinterWidth(32);
                    } else {
                        // 80mm -> Set 48 Karakter
                        bluetoothUtil.savePrinterWidth(48);
                    }

                    updatePaperStatusUI(); // Update teks di layar
                    dialog.dismiss();      // Tutup dialog
                    Toast.makeText(getContext(), "Ukuran kertas disimpan!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    // --- GANTI METHOD showPrinterDialog() DENGAN INI ---
    private void showPrinterDialog() {
        Set<BluetoothDevice> pairedDevices = bluetoothUtil.getPairedDevices();

        // 1. Setup Dialog & View Custom
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_printer_selector, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        // Background transparan biar rounded corner CardView kelihatan bagus
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // 2. Binding View
        LinearLayout container = view.findViewById(R.id.containerDevices);
        TextView tvStatus = view.findViewById(R.id.tvCurrentStatus);
        TextView tvMac = view.findViewById(R.id.tvCurrentMac);
        ImageView imgIndicator = view.findViewById(R.id.imgStatusIndicator);
        com.google.android.material.button.MaterialButton btnDisconnect = view.findViewById(R.id.btnDisconnect);
        android.widget.Button btnCancel = view.findViewById(R.id.btnCancel);

        // 3. Cek Status Saat Ini
        String savedMac = bluetoothUtil.getSavedPrinterMac();
        if (savedMac != null) {
            tvStatus.setText("TERSIMPAN & AKTIF");
            tvStatus.setTextColor(Color.parseColor("#2E7D32")); // Hijau
            tvMac.setText(savedMac);
            imgIndicator.setColorFilter(Color.parseColor("#2E7D32"));

            // Tampilkan tombol disconnect
            btnDisconnect.setVisibility(View.VISIBLE);
        } else {
            tvStatus.setText("BELUM ADA PRINTER");
            tvStatus.setTextColor(Color.parseColor("#F44336")); // Merah
            tvMac.setText("-");
            imgIndicator.setColorFilter(Color.parseColor("#F44336"));
            btnDisconnect.setVisibility(View.GONE);
        }

        // 4. Logic Tombol Disconnect
        btnDisconnect.setOnClickListener(v -> {
            // Hapus MAC dari memory
            bluetoothUtil.savePrinterMac(null);

            // Putus koneksi socket
            bluetoothUtil.closeConnection();

            Toast.makeText(getContext(), "Printer Dilupakan / Disconnect", Toast.LENGTH_SHORT).show();
            updatePrinterStatusUI(); // Update teks di halaman AccountFragment
            dialog.dismiss();
        });

        // 5. Populate List Device (Manual Inflate biar fleksibel)
        if (pairedDevices != null && !pairedDevices.isEmpty()) {
            for (BluetoothDevice device : pairedDevices) {
                View itemView = getLayoutInflater().inflate(R.layout.item_bluetooth_device, null);

                TextView tvName = itemView.findViewById(R.id.tvDeviceName);
                TextView tvDevMac = itemView.findViewById(R.id.tvDeviceMac);

                try {
                    String devName = device.getName();
                    String devAddress = device.getAddress();

                    tvName.setText(devName != null ? devName : "Unknown Device");
                    tvDevMac.setText(devAddress);

                    // Aksi saat Device diklik
                    itemView.setOnClickListener(v -> {
                        bluetoothUtil.savePrinterMac(devAddress);
                        updatePrinterStatusUI();

                        Toast.makeText(getContext(), "Menyimpan " + devName + "...", Toast.LENGTH_SHORT).show();

                        // Coba test connect
                        bluetoothUtil.connect(devAddress, new BluetoothUtil.ConnectionListener() {
                            @Override
                            public void onConnected() {
                                Toast.makeText(getContext(), "Berhasil Terhubung!", Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onFailed(String message) {
                                Toast.makeText(getContext(), "Gagal: " + message, Toast.LENGTH_SHORT).show();
                            }
                        });

                        dialog.dismiss();
                    });

                    container.addView(itemView);

                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }
        } else {
            // Kalau kosong
            TextView emptyView = new TextView(getContext());
            emptyView.setText("Tidak ada perangkat Bluetooth yang dipairing.\nSilakan pairing lewat Pengaturan HP.");
            emptyView.setPadding(0, 32, 0, 32);
            emptyView.setGravity(android.view.Gravity.CENTER);
            container.addView(emptyView);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}