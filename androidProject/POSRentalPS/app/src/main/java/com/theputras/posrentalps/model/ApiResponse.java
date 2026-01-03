package com.theputras.posrentalps.model;
import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {

    @SerializedName("success")
    public boolean success; // Ubah jadi public
    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public T data;

    public ApiResponse() {}
}