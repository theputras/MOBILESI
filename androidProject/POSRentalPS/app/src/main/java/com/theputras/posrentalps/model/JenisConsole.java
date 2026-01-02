package com.theputras.posrentalps.model;

import com.google.gson.annotations.SerializedName;

public class JenisConsole {

    @SerializedName("id_console")
    public int idConsole;

    @SerializedName("nama_console") // KUNCI UTAMA: Harus sama persis dengan JSON
    public String namaConsole;

    // Tambahkan ini biar lengkap sesuai JSON
    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("updated_at")
    public String updatedAt;

    // Constructor Kosong (Wajib buat GSON)
    public JenisConsole() {}

    // Getter (Optional, tapi boleh ada)
    public String getNamaConsole() {
        return namaConsole;
    }
}