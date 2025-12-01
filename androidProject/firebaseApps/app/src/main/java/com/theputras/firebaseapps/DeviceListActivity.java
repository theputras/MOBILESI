package com.theputras.firebaseapps;

import android.Manifest;
import android.app.ProgressDialog; // Tambahan untuk loading dialog
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.theputras.firebaseapps.utils.BluetoothHelper;

import java.util.ArrayList;
import java.util.Set;

public class DeviceListActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> devicesAdapter;
    private ArrayList<BluetoothDevice> deviceList;
    private Button btnScan, btnTestPrint;
    private ListView listDevices;

    // Tambahan: Dialog Loading & Flag untuk mencegah klik ganda
    private ProgressDialog loadingDialog;
    private boolean isConnecting = false;

    private static final int PERMISSION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        // Setup Loading Dialog
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("Sedang menghubungkan...");
        loadingDialog.setCancelable(false); // User gabisa cancel paksa biar gak error logic

        btnScan = findViewById(R.id.btn_scan);
        btnTestPrint = findViewById(R.id.btn_test_print);
        listDevices = findViewById(R.id.list_devices);

        deviceList = new ArrayList<>();
        devicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listDevices.setAdapter(devicesAdapter);

        bluetoothAdapter = BluetoothHelper.getInstance().getAdapter();

        btnScan.setOnClickListener(v -> {
            if (checkPermission()) {
                startDiscovery();
            } else {
                requestBluetoothPermissions();
            }
        });

        // --- UPDATE LOGIC KLIK ITEM ---
        listDevices.setOnItemClickListener((parent, view, position, id) -> {
            // 1. Cek Permission
            if (!checkPermission()) {
                requestBluetoothPermissions();
                return;
            }

            // 2. CEK: Apakah sedang proses konek? Kalau iya, tolak klik baru.
            if (isConnecting) return;

            // 3. Matikan scan biar fokus konek
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }

            BluetoothDevice device = deviceList.get(position);
            connectToDevice(device);
        });

        btnTestPrint.setOnClickListener(v -> {
            BluetoothHelper.getInstance().printText("TEST PRINT BERHASIL!\n\n");
        });

        updateUI();
    }

    private void connectToDevice(BluetoothDevice device) {
        // Tampilkan Loading & Kunci Status
        isConnecting = true;
        loadingDialog.show();

        new Thread(() -> {
            boolean success = BluetoothHelper.getInstance().connect(device);

            runOnUiThread(() -> {
                // Sembunyikan Loading & Buka Kunci
                loadingDialog.dismiss();
                isConnecting = false;

                if (success) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    Toast.makeText(this, "Berhasil Terkoneksi ke " + device.getName(), Toast.LENGTH_SHORT).show();
                    updateUI();
                    finish(); // Opsional: Langsung tutup halaman list kalau sukses biar user balik ke menu utama
                } else {
                    Toast.makeText(this, "Gagal Konek. Pastikan printer nyala.", Toast.LENGTH_LONG).show();
                    updateUI();
                }
            });
        }).start();
    }

    private void updateUI() {
        if (BluetoothHelper.getInstance().isConnected()) {
            btnTestPrint.setVisibility(View.VISIBLE);
        } else {
            btnTestPrint.setVisibility(View.GONE);
        }
    }

    private void startDiscovery() {
        if (!checkPermission()) return;

        devicesAdapter.clear();
        deviceList.clear();

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                devicesAdapter.add(device.getName() + "\n" + device.getAddress() + " [Paired]");
                deviceList.add(device);
            }
        }

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
        Toast.makeText(this, "Scanning...", Toast.LENGTH_SHORT).show();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (checkPermission() && device != null) {
                    boolean isExist = false;
                    for(BluetoothDevice d : deviceList) {
                        if(d.getAddress().equals(device.getAddress())) {
                            isExist = true;
                            break;
                        }
                    }

                    if (!isExist) {
                        String name = device.getName() == null ? "Unknown Device" : device.getName();
                        devicesAdapter.add(name + "\n" + device.getAddress());
                        deviceList.add(device);
                        devicesAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private void requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT
                    },
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Izin diberikan, silakan scan lagi", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Izin Bluetooth ditolak", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
        }
        // Jangan lupa tutup dialog biar gak error window leak
        if(loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}