package com.theputras.posrentalps.model;

import com.google.gson.annotations.SerializedName;

public class RiwayatTransaksi {
    @SerializedName("id_transaksi")
    private int id;

    @SerializedName("created_at")
    private String tanggal; // Format dari API biasanya "2025-12-12 10:00:00"

    @SerializedName("nama_pelanggan") // Sesuaikan dengan kolom database kamu
    private String namaPelanggan;

    @SerializedName("total_bayar")
    private int totalBayar;

    // Getter
    public int getId() { return id; }
    public String getTanggal() { return tanggal; }
    public String getNamaPelanggan() { return namaPelanggan; }
    public int getTotalBayar() { return totalBayar; }
}