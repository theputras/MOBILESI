package com.example.newproject.models;

import com.google.gson.annotations.SerializedName;

public class Konsumen {
    // Constructor untuk Tambah Data (tanpa id, created_at, updated_at)
    public Konsumen(int id, String kodeKonsumen, String namaKonsumen, String telp, String alamat, String perusahaan, String keterangan) {
        this.id = id;
        this.kodeKonsumen = kodeKonsumen;
        this.namaKonsumen = namaKonsumen;
        this.telp = telp;
        this.alamat = alamat;
        this.perusahaan = perusahaan;
        this.keterangan = keterangan;
    }
    public Konsumen(String kodeKonsumen, String namaKonsumen, String telp, String alamat, String perusahaan, String keterangan) {
        this.kodeKonsumen = kodeKonsumen;
        this.namaKonsumen = namaKonsumen;
        this.telp = telp;
        this.alamat = alamat;
        this.perusahaan = perusahaan;
        this.keterangan = keterangan;
    }
    @SerializedName("id")
    private int id;

    @SerializedName("kodekonsumen")
    private String kodeKonsumen; // Menggunakan camelCase untuk nama variabel Java

    @SerializedName("namakonsumen")
    private String namaKonsumen; // Menggunakan camelCase

    @SerializedName("telp")
    private String telp;

    @SerializedName("alamat")
    private String alamat;

    @SerializedName("perusahaan")
    private String perusahaan;

    @SerializedName("keterangan")
    private String keterangan; // Bisa null, jadi pakai String

    @SerializedName("created_at")
    private String createdAt; // Simpan sebagai String saja, karena bisa null

    @SerializedName("updated_at")
    private String updatedAt; // Simpan sebagai String

    // --- Getters ---
    // (Gson biasanya hanya butuh getter untuk deserialisasi)

    public int getId() {
        return id;
    }

    public String getKodeKonsumen() {
        return kodeKonsumen;
    }

    public String getNamaKonsumen() {
        return namaKonsumen;
    }

    public String getTelp() {
        return telp;
    }

    public String getAlamat() {
        return alamat;
    }

    public String getPerusahaan() {
        return perusahaan;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    // --- Setters (Opsional, jika dibutuhkan) ---
    // public void setId(int id) { this.id = id; }
    // ... dan seterusnya ...

    // --- Constructor (Opsional, bisa berguna) ---
    // public Konsumen(int id, String kodeKonsumen, ...) { ... }
}