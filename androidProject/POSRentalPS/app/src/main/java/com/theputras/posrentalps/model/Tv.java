package com.theputras.posrentalps.model;



import com.google.gson.annotations.SerializedName;

public class Tv {
    @SerializedName("id")
    private int id;

    @SerializedName("nomor_tv")
    private String nomorTv;

    @SerializedName("status")
    private String status;

    // Relasi ke object JenisConsole
    @SerializedName("jenis_console")
    private JenisConsole jenisConsole;

    // Getters
    public int getId() { return id; }
    public String getNomorTv() { return nomorTv; }
    public String getStatus() { return status; }
    public JenisConsole getJenisConsole() { return jenisConsole; }
}
