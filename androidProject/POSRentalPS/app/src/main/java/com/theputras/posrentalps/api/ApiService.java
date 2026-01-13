package com.theputras.posrentalps.api;

import com.theputras.posrentalps.model.ApiResponse;
import com.theputras.posrentalps.model.CheckoutRequest;
import com.theputras.posrentalps.model.PaketSewa;
import com.theputras.posrentalps.model.RiwayatTransaksi;
import com.theputras.posrentalps.model.TransactionItem;
import com.theputras.posrentalps.model.TransactionRequest;
import com.theputras.posrentalps.model.TvResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
public interface ApiService {

    @FormUrlEncoded
    @POST("login")
    Call<ResponseBody> login(
            @Field("email") String email,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("register")
    Call<ResponseBody> register(
            @Field("name") String name,
            @Field("email") String email,
            @Field("password") String password,
            @Field("password_confirmation") String passwordConfirmation
    );

    @POST("logout")
    Call<ResponseBody> logout();
    // Sesuai Route::get('/tvs-available', ...)
//    @GET("tvs-available")
//    Call<TvResponse> getAvailableTvs();
    @GET("tvs")
    Call<TvResponse> getAvailableTvs();
    // Sesuai Route::get('/master/pakets', ...)
    @GET("master/pakets")
    Call<List<PaketSewa>> getPakets();

    // Sesuai Route::post('/transaksi', ...)
    // Note: Laravel biasanya return satu object transaksi yang baru dibuat
//    @POST("transaksi")
//    Call<ApiResponse<List<TransactionItem>>> saveTransaction(@Body CheckoutRequest request);
    @POST("transaksi")
    Call<ApiResponse<RiwayatTransaksi>> saveTransaction(@Body TransactionRequest request);

    @GET("transaksi/{id}")
    Call<ApiResponse<RiwayatTransaksi>> getTransactionDetail(@Path("id") String id);
    @GET("transaksi")
    Call<ApiResponse<List<TransactionItem>>> getHistory();

}