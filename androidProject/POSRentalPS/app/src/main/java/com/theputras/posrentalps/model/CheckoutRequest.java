package com.theputras.posrentalps.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CheckoutRequest {
    @SerializedName("nama_penyewa")
    public String namaPenyewa;

    @SerializedName("uang_bayar")
    public int uangBayar;

    @SerializedName("metode_pembayaran")
    public String metodePembayaran;

    @SerializedName("items")
    public List<ItemDetail> items;

    // Inner class untuk detail item
    public static class ItemDetail {
        @SerializedName("tv_id")
        public int tvId;

        @SerializedName("id_paket")
        public int idPaket;

        public ItemDetail(int tvId, int idPaket) {
            this.tvId = tvId;
            this.idPaket = idPaket;
        }
    }
}