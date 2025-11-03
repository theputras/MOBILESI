package com.example.uts;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
public interface ApiService {

    // Base URL: https://alamat.thecloudalert.com/
    // Full path: api/kabkota/get/?d_provinsi_id=1
    @GET("kabkota/get/")
    Call<KabKotaResponse> getKabKota(@Query("d_provinsi_id") int provinsiId);
}
