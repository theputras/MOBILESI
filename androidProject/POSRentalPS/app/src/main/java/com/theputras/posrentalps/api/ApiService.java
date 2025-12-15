package com.theputras.posrentalps.api;

import com.theputras.posrentalps.model.ApiResponse;
import com.theputras.posrentalps.model.TransactionItem;
import com.theputras.posrentalps.model.TransactionRequest;
import com.theputras.posrentalps.model.TvResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    // Existing
    @GET("api/tvs/available")
    Call<TvResponse> getAvailableTvs();

    // --- NEW: History & Transaksi ---
    @GET("api/transaksi")
    Call<ApiResponse<List<TransactionItem>>> getHistory();

    @POST("api/transaksi")
    Call<ApiResponse<TransactionItem>> saveTransaction(@Body TransactionRequest request);
}