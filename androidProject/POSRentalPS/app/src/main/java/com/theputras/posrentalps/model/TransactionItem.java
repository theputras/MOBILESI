package com.theputras.posrentalps.model;

import com.google.gson.annotations.SerializedName;

public class TransactionItem {
    @SerializedName("id_transaksi")
    public Long idTransaksi;

    @SerializedName("nama_penyewa")
    public String namaPenyewa;

    @SerializedName("tv_id")
    public String nomorTv;

    @SerializedName("total_tagihan")
    public int totalTagihan;

    @SerializedName("id_paket")
    public int durasiJam;

    @SerializedName("tanggal_transaksi")
    public String tanggalTransaksi;

    @SerializedName("console")
    public JenisConsole console; // Relasi nested object
}