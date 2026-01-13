package com.theputras.posrentalps.api;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.theputras.posrentalps.LoginActivity;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // Pastikan URL sudah HTTPS (Aman)
//    public static final String BASE_URL = "https://mobile.theputras.my.id/api/";
//    public static final String BASE_URL = "http://172.16.2.215/api/";
    public static final String BASE_URL = "http://172.16.64.34/putra_backend/public/api/";

    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();

                    // 1. Ambil Token
                    SharedPreferences sharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
                    String token = sharedPreferences.getString("token", "");

                    // 2. Pasang Token ke Header
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Accept", "application/json")
                            .header("Content-Type", "application/json");

                    if (!token.isEmpty()) {
                        requestBuilder.header("Authorization", "Bearer " + token);
                    }

                    Request request = requestBuilder.build();

                    // 3. Eksekusi Request & Tangkap Responnya
                    Response response = chain.proceed(request);

                    // --- LOGIKA AUTO LOGOUT (401) ---
                    if (response.code() == 401) {
                        // A. Hapus Data Sesi
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.apply();

                        // B. Paksa Pindah ke LoginActivity
                        // Kita pakai FLAG_ACTIVITY_NEW_TASK karena context bisa berasal dari mana saja
                        Intent intent = new Intent(context, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(intent);
                    }
                    // --------------------------------

                    return response;
                })
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit;
    }
}