package com.theputras.posrentalps.api;

import com.theputras.posrentalps.model.ApiResponse;
import com.theputras.posrentalps.model.PaketSewa;
import com.theputras.posrentalps.model.TransactionItem;
import com.theputras.posrentalps.model.TransactionRequest;
import com.theputras.posrentalps.model.TvResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    // Sesuai Route::get('/tvs-available', ...)
    @GET("api/tvs-available")
    Call<TvResponse> getAvailableTvs();

    // Sesuai Route::get('/master/pakets', ...)
    @GET("api/master/pakets")
    Call<List<PaketSewa>> getPakets();

    // Sesuai Route::post('/transaksi', ...)
    // Note: Laravel biasanya return satu object transaksi yang baru dibuat
    @POST("api/transaksi")
    Call<ApiResponse<TransactionItem>> saveTransaction(@Body TransactionRequest request);

}