package com.theputras.firebaseapps.models;

public class FireModel {
    private String id;
    private String prodi; // Pengganti 'Mahasiswa'
    private String nama;
    private String nim;
    private String ttl;
    private String umur;

    // Constructor Kosong (Wajib untuk Firestore)
    public FireModel() {
    }

    // Constructor Lengkap
    public FireModel(String id, String prodi, String nama, String nim, String ttl, String umur) {
        this.id = id;
        this.prodi = prodi;
        this.nama = nama;
        this.nim = nim;
        this.ttl = ttl;
        this.umur = umur;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProdi() {
        return prodi;
    }

    public void setProdi(String prodi) {
        this.prodi = prodi;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getNim() {
        return nim;
    }

    public void setNim(String nim) {
        this.nim = nim;
    }

    public String getTtl() {
        return ttl;
    }

    public void setTtl(String ttl) {
        this.ttl = ttl;
    }

    public String getUmur() {
        return umur;
    }

    public void setUmur(String umur) {
        this.umur = umur;
    }
}