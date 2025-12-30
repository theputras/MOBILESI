package com.theputras.posrentalps.model;

import com.google.gson.annotations.SerializedName;

public class Tv {
    @SerializedName("id")
    private int id; // Ini Primary Key

    @SerializedName("nomor_tv")
    private String nomorTv;

    @SerializedName("status")
    private String status; // 'available', 'booked'

    @SerializedName("jenis_console")
    private JenisConsole jenisConsole;

    // Tambahkan constructor kosong (Dibutuhkan GSON)
    public Tv() {}

    public int getId() { return id; }
    public String getNomorTv() { return nomorTv; }
    public String getStatus() { return status; }
    public JenisConsole getJenisConsole() { return jenisConsole; }
}