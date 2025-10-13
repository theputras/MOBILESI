package com.example.newproject;

public class Item {
    private int id;
    private String kode_item;
    private String nama_item;
    private String satuan;
    private int hargabeli;
    private int hargajual;

    public Item(int id, String kode_item, String nama_item, String satuan, int hargabeli, int hargajual) {
        this.id = id;
        this.kode_item = kode_item;
        this.nama_item = nama_item;
        this.satuan = satuan;
        this.hargabeli = hargabeli;
        this.hargajual = hargajual;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKode_item() {
        return kode_item;
    }

    public void setKode_item(String kode_item) {
        this.kode_item = kode_item;
    }

    public String getNama_item() {
        return nama_item;
    }

    public void setNama_item(String nama_item) {
        this.nama_item = nama_item;
    }

    public String getSatuan() {
        return satuan;
    }

    public void setSatuan(String satuan) {
        this.satuan = satuan;
    }

    public int getHargabeli() {
        return hargabeli;
    }

    public void setHargabeli(int hargabeli) {
        this.hargabeli = hargabeli;
    }

    public int getHargajual() {
        return hargajual;
    }

    public void setHargajual(int hargajual) {
        this.hargajual = hargajual;
    }
}
