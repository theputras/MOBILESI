package com.example.newproject.models;

public class Item {
    private int id;
    private String kode_item;
    private String nama_item;
    private String satuan;
    private long hargabeli;
    private long hargajual;

    public Item(int id, String kode_item, String nama_item, String satuan, long hargabeli, long hargajual) {
        this.id = id;
        this.kode_item = kode_item;
        this.nama_item = nama_item;
        this.satuan = satuan;
        this.hargabeli = hargabeli;
        this.hargajual = hargajual;
    }

    // Constructor tambahan buat request (tanpa ID)
    public Item(String kode_item, String nama_item, String satuan, long hargabeli, long hargajual) {
        this.kode_item = kode_item;
        this.nama_item = nama_item;
        this.satuan = satuan;
        this.hargabeli = hargabeli;
        this.hargajual = hargajual;
    }

    // Getters dan Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getKode_item() { return kode_item; }
    public void setKode_item(String kode_item) { this.kode_item = kode_item; }

    public String getNama_item() { return nama_item; }
    public void setNama_item(String nama_item) { this.nama_item = nama_item; }

    public String getSatuan() { return satuan; }
    public void setSatuan(String satuan) { this.satuan = satuan; }

    public long getHargabeli() { return hargabeli; }
    public void setHargabeli(long hargabeli) { this.hargabeli = hargabeli; }

    public long getHargajual() { return hargajual; }
    public void setHargajual(long hargajual) { this.hargajual = hargajual; }
}
