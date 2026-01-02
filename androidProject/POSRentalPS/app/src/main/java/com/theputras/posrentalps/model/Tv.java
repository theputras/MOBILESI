package com.theputras.posrentalps.model;

import com.google.gson.annotations.SerializedName;

public class Tv {
    @SerializedName("id")
    public int TVid; // Nama field di JSON 'id', di Java 'TVid'

    @SerializedName("nomor_tv")
    public String nomorTv;

    @SerializedName("status")
    public String status; // 'available', 'booked', 'maintenance'

    @SerializedName("rental_end_time")
    public String rentalEndTime;

    @SerializedName("id_console")
    public int idConsole;

    @SerializedName("jenis_console")
    public JenisConsole jenisConsole;

    public Tv() {}

    // --- PERBAIKAN: TAMBAHKAN GETTER INI AGAR ADAPTER TIDAK ERROR ---
    public int getId() {
        return TVid;
    }
    // ---------------------------------------------------------------

    public String getNamaConsole() {
        if (jenisConsole != null && jenisConsole.getNamaConsole() != null) {
            return jenisConsole.getNamaConsole();
        }
        return "Null12";
    }

    public String getNomorTv() { return nomorTv; }
    public String getStatus() { return status; }
    public JenisConsole getJenisConsole() { return jenisConsole; }
}