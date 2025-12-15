package com.theputras.posrentalps.model;


import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TvResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<Tv> data;

    public List<Tv> getData() { return data; }
}
