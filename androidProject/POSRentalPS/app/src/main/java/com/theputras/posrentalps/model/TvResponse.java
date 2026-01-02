package com.theputras.posrentalps.model;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TvResponse {
    @SerializedName("data")
    private List<Tv> data;

    public List<Tv> getData() {
        return data;
    }
}