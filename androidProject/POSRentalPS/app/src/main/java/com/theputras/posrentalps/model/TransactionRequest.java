package com.theputras.posrentalps.model;

import com.google.gson.annotations.SerializedName;

public class TransactionRequest {
    @SerializedName("nama_penyewa")
    public String namaPenyewa;

    @SerializedName("tv_id") // Sesuai dengan validation Laravel 'tv_id'
    public int tvId;

    @SerializedName("id_paket") // Sesuai validation 'id_paket'
    public int idPaket;

    @SerializedName("uang_bayar")
    public int uangBayar;

    // Tambahan untuk fitur QRIS nanti
    @SerializedName("metode_pembayaran")
    public String metodePembayaran;

    // Constructor
    public TransactionRequest(String nama, int tvId, int idPaket, int bayar) {
        this.namaPenyewa = nama;
        this.tvId = tvId;
        this.idPaket = idPaket;
        this.uangBayar = bayar;
        this.metodePembayaran = "TUNAI"; // Default
    }
}