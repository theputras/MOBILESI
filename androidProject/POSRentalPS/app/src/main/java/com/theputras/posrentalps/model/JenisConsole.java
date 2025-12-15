package com.theputras.posrentalps.model;


import com.google.gson.annotations.SerializedName;

public class JenisConsole {
    @SerializedName("id_console")
    private int idConsole;

    @SerializedName("nama_console")
    private String namaConsole;

    // Getter
    public String getNamaConsole() { return namaConsole; }
}