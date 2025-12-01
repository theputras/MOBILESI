package com.theputras.firebaseapps.utils;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

// Tambahkan ini untuk menghilangkan tanda merah (Error Permission check di Editor)
@SuppressLint("MissingPermission")
public class BluetoothHelper {

    private static BluetoothHelper instance;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private BluetoothDevice connectedDevice;

    // UUID Standar SPP
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothHelper() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static synchronized BluetoothHelper getInstance() {
        if (instance == null) {
            instance = new BluetoothHelper();
        }
        return instance;
    }

    public BluetoothAdapter getAdapter() {
        return bluetoothAdapter;
    }

    public boolean isConnected() {
        return bluetoothSocket != null && bluetoothSocket.isConnected();
    }

    public String getConnectedDeviceName() {
        if (connectedDevice != null) {
            return connectedDevice.getName();
        }
        return "Belum Terkoneksi";
    }

    // --- PERBAIKAN UTAMA DI SINI ---
    public boolean connect(BluetoothDevice device) {
        // 1. Cek apakah device yang mau dikonek SAMA dengan yang sedang konek sekarang?
        if (connectedDevice != null &&
                connectedDevice.getAddress().equals(device.getAddress()) &&
                isConnected()) {
            Log.d("BluetoothHelper", "Device sudah terkoneksi, tidak perlu reconnect.");
            return true; // Langsung return TRUE, jangan disconnect!
        }

        // 2. Jika beda device, baru kita putus koneksi lama
        disconnect();

        BluetoothSocket tmp = null;
        try {
            Log.d("BluetoothHelper", "Mencoba koneksi ke: " + device.getName());

            // Cara 1: Secure Socket (Standar)
            tmp = device.createRfcommSocketToServiceRecord(SPP_UUID);
            tmp.connect();

            bluetoothSocket = tmp;
            outputStream = bluetoothSocket.getOutputStream();
            connectedDevice = device;
            Log.d("BluetoothHelper", "Koneksi Berhasil!");
            return true;

        } catch (IOException e) {
            Log.e("BluetoothHelper", "Gagal koneksi cara 1", e);
            try {
                // Cara 2: Fallback (Biasanya ampuh buat printer China/Murah)
                if (tmp != null) tmp.close();
                Log.d("BluetoothHelper", "Mencoba koneksi fallback...");

                tmp = device.createRfcommSocketToServiceRecord(SPP_UUID);
                // Kadang reflection method dibutuhkan, tapi kita coba simple retry dulu
                // Atau bisa gunakan createInsecureRfcommSocketToServiceRecord
                // tmp = device.createInsecureRfcommSocketToServiceRecord(SPP_UUID);

                // Tips: Printer thermal kadang butuh waktu
                Thread.sleep(1000);
                tmp.connect();

                bluetoothSocket = tmp;
                outputStream = bluetoothSocket.getOutputStream();
                connectedDevice = device;
                return true;
            } catch (Exception e2) {
                Log.e("BluetoothHelper", "Gagal koneksi total", e2);
                disconnect();
                return false;
            }
        }
    }

    public void disconnect() {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            bluetoothSocket = null;
            outputStream = null;
            // connectedDevice = null; // Opsional: jangan null-kan nama biar status di UI tetap ada walau putus
        }
    }

    public boolean printText(String text) {
        if (!isConnected() || outputStream == null) {
            Log.e("BluetoothHelper", "Gagal Print: Socket belum connect atau Stream null");
            return false;
        }

        try {
            // 1. Siapkan teks utama
            String msg = text;

            // 2. Tambahkan perintah Feed (Enter) sebanyak 3x langsung di String-nya
            // Supaya kertas keluar dan teks tidak terpotong
            msg += "\n\n\n";

            // 3. Konversi ke Bytes
            byte[] textBytes = msg.getBytes();

            // 4. (Opsional) Perintah Reset Printer (ESC @)
            // Kadang membantu, kadang bikin error di beberapa printer China.
            // Kita coba kirim langsung teksnya dulu dalam satu paket.

            // Tulis SEMUA data sekaligus (Teks + Enter jadi satu)
            outputStream.write(textBytes);

            // 5. Paksa data keluar (Flush)
            outputStream.flush();

            // 6. Tunggu sebentar biar printer "mengunyah" data
            try {
                Thread.sleep(300); // 300ms delay
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return true;
        } catch (IOException e) {
            Log.e("BluetoothHelper", "Error saat write stream", e);
            disconnect();
            return false;
        }
    }

    // Fungsi untuk mengatur posisi teks (0: Kiri, 1: Tengah, 2: Kanan)
    public boolean setAlign(int alignType) {
        if (!isConnected() || outputStream == null) return false;
        try {
            // Perintah ESC a n
            byte[] command = new byte[]{0x1B, 0x61, (byte)alignType};
            outputStream.write(command);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Fungsi Reset Printer (Balikin ke default: Rata Kiri, Font Normal)
    public void resetPrinter() {
        if (!isConnected() || outputStream == null) return;
        try {
            outputStream.write(new byte[]{0x1B, 0x40}); // ESC @
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}