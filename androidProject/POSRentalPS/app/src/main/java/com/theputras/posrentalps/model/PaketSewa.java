package com.theputras.posrentalps.model;
import com.google.gson.annotations.SerializedName;

public class PaketSewa {
    @SerializedName("id_paket")
    public int idPaket;
    @SerializedName("nama_paket")
    public String namaPaket;
    @SerializedName("durasi_menit")
    public int durasiMenit;
    @SerializedName("harga")
    public int harga;
    @SerializedName("nama_console")
    public String namaConsole; // Dari join table di backend
}