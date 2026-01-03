package com.theputras.posrentalps.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothUtil {
    private static BluetoothUtil instance;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private Context context;

    private static final String KEY_PAPER_WIDTH = "paper_width_chars";
    // Default 58mm = 32 Karakter
    private static final int DEFAULT_WIDTH = 32;

    // UUID standar untuk printer thermal / Serial Port Profile (SPP)
    private static final UUID UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String PREF_NAME = "PrinterPref";
    private static final String KEY_MAC = "printer_mac";

    private BluetoothUtil(Context context) {
        this.context = context.getApplicationContext();
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static synchronized BluetoothUtil getInstance(Context context) {
        if (instance == null) {
            instance = new BluetoothUtil(context);
        }
        return instance;
    }

    public void savePrinterWidth(int charCount) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(KEY_PAPER_WIDTH, charCount).apply();
    }

    public int getPrinterWidth() {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(KEY_PAPER_WIDTH, DEFAULT_WIDTH);
    }



    // Ambil daftar device yang sudah dipairing
    public Set<BluetoothDevice> getPairedDevices() {
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            try {
                return bluetoothAdapter.getBondedDevices(); // Butuh permission check di Activity
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // Simpan alamat MAC printer
    public void savePrinterMac(String macAddress) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_MAC, macAddress).apply();
    }

    public String getSavedPrinterMac() {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_MAC, null);
    }

    // Logic Koneksi
    public void connect(String macAddress, ConnectionListener listener) {
        new Thread(() -> {
            try {
                if (socket != null && socket.isConnected()) {
                    listener.onConnected(); // Sudah konek
                    return;
                }

                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);
                try {
                    socket = device.createRfcommSocketToServiceRecord(UUID_SPP);
                    socket.connect();
                    outputStream = socket.getOutputStream();

                    new Handler(Looper.getMainLooper()).post(listener::onConnected);
                } catch (SecurityException | IOException e) {
                    closeConnection();
                    new Handler(Looper.getMainLooper()).post(() -> listener.onFailed(e.getMessage()));
                }
            } catch (IllegalArgumentException e) {
                new Handler(Looper.getMainLooper()).post(() -> listener.onFailed("Alamat MAC tidak valid"));
            }
        }).start();
    }

    // Auto Connect ke device tersimpan
    public void autoConnect(ConnectionListener listener) {
        String savedMac = getSavedPrinterMac();
        if (savedMac != null) {
            connect(savedMac, listener);
        } else {
            listener.onFailed("Belum ada printer dipilih");
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public void print(byte[] data) throws IOException {
        if (outputStream != null) {
            outputStream.write(data);
            outputStream.flush();
        } else {
            throw new IOException("Printer belum terkoneksi");
        }
    }

    public void closeConnection() {
        try {
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
            socket = null;
            outputStream = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printLine() throws IOException {
        int width = getPrinterWidth(); // Ambil dinamis
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < width; i++) {
            line.append("-");
        }
        line.append("\n");
        print(line.toString().getBytes());
    }

    public String alignLeftRight(String left, String right) {
        int width = getPrinterWidth(); // Ambil dinamis
        int space = width - left.length() - right.length();
        if (space < 1) space = 1;

        StringBuilder sb = new StringBuilder();
        sb.append(left);
        for (int i = 0; i < space; i++) {
            sb.append(" ");
        }
        sb.append(right);
        return sb.toString();
    }

    public interface ConnectionListener {
        void onConnected();
        void onFailed(String message);
    }



    // --- ESC/POS COMMANDS SEDERHANA ---
    public static final byte[] RESET = {0x1B, 0x40};
    public static final byte[] ALIGN_LEFT = {0x1B, 0x61, 0x00};
    public static final byte[] ALIGN_CENTER = {0x1B, 0x61, 0x01};
    public static final byte[] ALIGN_RIGHT = {0x1B, 0x61, 0x02};
    public static final byte[] TEXT_BOLD_ON = {0x1B, 0x45, 0x01};
    public static final byte[] TEXT_BOLD_OFF = {0x1B, 0x45, 0x00};
    public static final byte[] TEXT_SIZE_NORMAL = {0x1D, 0x21, 0x00};
    public static final byte[] TEXT_SIZE_LARGE = {0x1D, 0x21, 0x11};
    public static final byte[] FEED_LINE = {0x0A};


}