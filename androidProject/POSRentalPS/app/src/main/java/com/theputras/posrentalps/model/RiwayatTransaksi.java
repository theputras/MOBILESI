package com.theputras.posrentalps.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RiwayatTransaksi {
    @SerializedName("id_transaksi")
    private String idTransaksi;

    @SerializedName("tanggal_transaksi")
    private String tanggalTransaksi;

    @SerializedName("nama_penyewa")
    private String namaPenyewa;

    @SerializedName("total_tagihan")
    private int totalTagihan;

    @SerializedName("uang_bayar")
    private int uangBayar;

    @SerializedName("uang_kembalian")
    private int uangKembalian;

    @SerializedName("metode_pembayaran")
    private String metodePembayaran;

    @SerializedName("status_pembayaran")
    private String statusPembayaran;

    // --- TAMBAHAN: List Detail Item ---
    @SerializedName("details")
    private List<DetailItem> details;

    // Getters
    public String getIdTransaksi() { return idTransaksi; }
    public String getTanggalTransaksi() { return tanggalTransaksi; }
    public String getNamaPenyewa() { return namaPenyewa; }
    public int getTotalTagihan() { return totalTagihan; }
    public int getUangBayar() { return uangBayar; }
    public int getUangKembalian() { return uangKembalian; }
    public String getMetodePembayaran() { return metodePembayaran; }
    public List<DetailItem> getDetails() { return details; }

    // --- INNER CLASS: Detail Item ---
    public static class DetailItem {
        @SerializedName("nama_item_snapshot")
        private String namaItem; // "Sewa TV 01 - Paket 1 Jam"

        @SerializedName("harga_satuan")
        private int hargaSatuan;

        @SerializedName("qty")
        private int qty;

        @SerializedName("subtotal")
        private int subtotal;

        public String getNamaItem() { return namaItem; }
        public int getHargaSatuan() { return hargaSatuan; }
        public int getQty() { return qty; }
        public int getSubtotal() { return subtotal; }
    }
}