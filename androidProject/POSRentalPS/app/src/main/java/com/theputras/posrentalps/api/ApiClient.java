package com.theputras.posrentalps.api;


import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // Ganti IP ini dengan IP Laptop kamu (ipconfig/ifconfig)
    // Jangan localhost! Emulator gak kenal localhost laptop.
    private static final String BASE_URL = "https://mobile.theputras.my.id/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
