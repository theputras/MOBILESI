package com.example.newproject;

import com.example.newproject.models.Item;
import com.example.newproject.models.Konsumen;

import okhttp3.ResponseBody; // Import ResponseBody
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE; // Import DELETE
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT; // Import PUT
import retrofit2.http.Path; // Import Path
import java.util.List;

public interface ApiService {

    // --- AUTH ---
    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("logout")
    Call<LogoutResponse> logout(@Header("Authorization") String token);
    // Di ApiService.java
    @POST("logout-all") // Sesuaikan endpoint
    Call<ResponseBody> logoutAll(@Header("Authorization") String token); // Ganti ResponseBody jika perlu

    // --- ITEMS ---
    @GET("items")
    Call<List<Item>> getItems(
            @Header("Authorization") String auth,
            @Header("Accept") String accept
    );

    @POST("items")
    Call<Item> addItem( // Kembalian mungkin Item yang baru dibuat
                        @Header("Authorization") String auth,
                        @Header("Accept") String accept,
                        @Body Item body
    );

    // GET Item by ID (Baru)
    @GET("items/{id}")
    Call<List<Item>> getItemById( // Kembalian biasanya satu objek Item
                            @Header("Authorization") String auth,
                            @Header("Accept") String accept,
                            @Path("id") int itemId // Mengambil {id} dari URL
    );

    // UPDATE Item by ID (Baru) - Pakai PUT
    @PUT("items/{id}") // Atau @PATCH jika API support partial update
    Call<Item> updateItem( // Kembalian mungkin Item yang sudah diupdate
                           @Header("Authorization") String auth,
                           @Header("Accept") String accept,
                           @Path("id") int itemId,
                           @Body Item body // Mengirim data Item yang sudah diubah
    );

    // DELETE Item by ID (Baru)
    @DELETE("items/{id}") // Parameter di URL pakai {item} di Laravel, tapi biasa ID int
    Call<ResponseBody> deleteItem( // Kembalian seringkali kosong atau hanya status sukses
                                   @Header("Authorization") String auth,
                                   @Header("Accept") String accept,
                                   @Path("id") int itemId // Mengambil {id} dari URL
    );

    // --- KONSUMEN ---
    @GET("konsumens")
    Call<List<Konsumen>> getKonsumen(
            @Header("Authorization") String auth,
            @Header("Accept") String accept
    );

    @POST("konsumens") // Ganti dari "konsumen" ke "konsumens" sesuai route
    Call<Konsumen> addKonsumen(
            @Header("Authorization") String auth,
            @Header("Accept") String accept,
            @Body Konsumen body
    );

    // GET Konsumen by ID (Baru)
    @GET("konsumens/{id}")
    Call<List<Konsumen>> getKonsumenById( // Kembalian satu objek Konsumen
                                    @Header("Authorization") String auth,
                                    @Header("Accept") String accept,
                                    @Path("id") int konsumenId // Mengambil {id} dari URL
    );

    // UPDATE Konsumen by ID (Baru) - Pakai PUT
    @PUT("konsumens/{id}") // Atau @PATCH. Parameter URL di Laravel {konsumen}, tapi pakai ID int
    Call<Konsumen> updateKonsumen( // Kembalian mungkin Konsumen yang diupdate
                                   @Header("Authorization") String auth,
                                   @Header("Accept") String accept,
                                   @Path("id") int konsumenId,
                                   @Body Konsumen body // Mengirim data Konsumen yang sudah diubah
    );

    // DELETE Konsumen by ID (Baru)
    @DELETE("konsumens/{id}")
    Call<ResponseBody> deleteKonsumen( // Kembalian seringkali kosong
                                       @Header("Authorization") String auth,
                                       @Header("Accept") String accept,
                                       @Path("id") int konsumenId // Mengambil {id} dari URL
    );

}