package com.theputras.posrentalps.model;

import com.google.gson.annotations.SerializedName;

public class TransactionRequest {
    @SerializedName("nama_penyewa")
    public String namaPenyewa;

    @SerializedName("nomor_tv")
    public String nomorTv;

    @SerializedName("id_console")
    public int idConsole;

    @SerializedName("durasi_jam")
    public int durasiJam;

    @SerializedName("uang_bayar")
    public int uangBayar;

    public TransactionRequest(String nama, String tv, int console, int durasi, int bayar) {
        this.namaPenyewa = nama;
        this.nomorTv = tv;
        this.idConsole = console;
        this.durasiJam = durasi;
        this.uangBayar = bayar;
    }
}