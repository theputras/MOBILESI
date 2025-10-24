package com.example.newproject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

import java.util.List;

public interface ApiService {

    // Metode GET yang sudah ada
    // Ganti "items.json" dengan endpoint GET Anda
//    @GET("items.json")
    @GET("items")
    Call<List<Item>> getItems(
            @Header("Authorization") String auth,
            @Header("Accept") String accept
    );

    // Post data
    @POST("items")
    Call<Item> addItem(
            @Header("Authorization") String auth,
            @Header("Accept") String accept,
            @Body Item body
    );

    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("logout")
    Call<LogoutResponse> logout(@Header("Authorization") String token);

    // Metode POST untuk menambahkan item baru
    // Ganti "items" dengan endpoint POST API Anda yang sebenarnya
//    @POST("items")
//    Call<Item> createItem(@Body Item item); // Mengirim objek Item sebagai body request

    // Jika API Anda mengembalikan hanya pesan sukses (misal "message"),
    // Anda bisa mengganti Call<Item> menjadi Call<ResponseBody> atau Call<Void>
    // Contoh: Call<ResponseBody> createItem(@Body Item item);
}
