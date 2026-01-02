package com.theputras.posrentalps.model;

import com.google.gson.annotations.SerializedName;

public class TransactionItem {
    @SerializedName("id_transaksi")
    public Long idTransaksi;

    @SerializedName("nama_penyewa")
    public String namaPenyewa;

    @SerializedName("total_tagihan")
    public int totalTagihan;

    @SerializedName("tanggal_transaksi")
    public String tanggalTransaksi;

    // --- STRUKTUR BARU SESUAI JSON ---

    // Objek TV (Berisi nomor_tv)
    @SerializedName("tv")
    public Tv tv;

    // Objek Console (Berisi nama_console)
    @SerializedName("console")
    public JenisConsole console;

    // Helper untuk mengambil Nomor TV dengan aman
    public String getDisplayTv() {
        if (tv != null && tv.nomorTv != null) {
            return tv.nomorTv;
        }
        return "TV -";
    }

    // Helper untuk mengambil Nama Console dengan aman
    public String getDisplayConsole() {
        if (console != null && console.getNamaConsole() != null) {
            return console.getNamaConsole();
        }
        // Fallback: Cek dari dalam objek TV jika di root console null
        if (tv != null && tv.getJenisConsole() != null) {
            return tv.getJenisConsole().getNamaConsole();
        }
        return "";
    }
}