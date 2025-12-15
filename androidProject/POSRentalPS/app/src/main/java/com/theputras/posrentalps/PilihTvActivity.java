package com.theputras.posrentalps;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.theputras.posrentalps.adapter.TvAdapter;
import com.theputras.posrentalps.api.ApiClient;
import com.theputras.posrentalps.api.ApiService;
import com.theputras.posrentalps.model.Tv;
import com.theputras.posrentalps.model.TvResponse;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PilihTvActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TvAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pilih_tv);

        recyclerView = findViewById(R.id.recyclerViewTv);
        progressBar = findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Panggil fungsi ambil data
        fetchAvailableTvs();
    }

    private void fetchAvailableTvs() {
        progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<TvResponse> call = apiService.getAvailableTvs();

        call.enqueue(new Callback<TvResponse>() {
            @Override
            public void onResponse(Call<TvResponse> call, Response<TvResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<Tv> tvList = response.body().getData();

                    // Setup Adapter
                    adapter = new TvAdapter(PilihTvActivity.this, tvList, new TvAdapter.OnTvClickListener() {
                        @Override
                        public void onTvClick(Tv tv) {
                            // Aksi saat TV dipilih
                            Toast.makeText(PilihTvActivity.this,
                                    "Kamu memilih " + tv.getNomorTv(),
                                    Toast.LENGTH_SHORT).show();

                            // TODO: Pindah ke halaman form transaksi bawa ID TV
                            // Intent intent = new Intent(PilihTvActivity.this, FormTransaksiActivity.class);
                            // intent.putExtra("TV_ID", tv.getId());
                            // startActivity(intent);
                        }
                    });

                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(PilihTvActivity.this, "Gagal mengambil data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TvResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PilihTvActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}