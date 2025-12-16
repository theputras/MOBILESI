package com.theputras.posrentalps.api;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // Pastikan URL sudah HTTPS (Aman)
    public static final String BASE_URL = "http://172.16.60.39/";

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {

//            // 1. Logger (CCTV) - Biarkan tetap ada untuk debugging
//            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
//            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // 2. Client dengan Header Wajib Laravel
            OkHttpClient client = new OkHttpClient.Builder()
//                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request.Builder requestBuilder = original.newBuilder()
                                .header("Accept", "application/json") // WAJIB: Agar Laravel sadar ini API
                                .header("Content-Type", "application/json"); // Opsional tapi best practice

                        Request request = requestBuilder.build();
                        return chain.proceed(request);
                    })
                    .build();

            // 3. Retrofit Build
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}