package com.theputras.firebaseapps.models;

import com.google.firebase.firestore.Exclude; // Pastikan import ini ada

public class FireModel {
    private String id; // Tambahan field ID
    private String name;
    private String desc;

    public FireModel() { }

    public FireModel(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    // Tambahkan Getter & Setter untuk ID dengan @Exclude
    @Exclude
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }
}