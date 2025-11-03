package com.example.uts;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KabKotaActivity extends AppCompatActivity {

    private KabKotaAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kabkota);

        RecyclerView rv = findViewById(R.id.rvKabKota);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new KabKotaAdapter();
        rv.setAdapter(adapter);

        // d_provinsi_id = 1 (sesuai contoh)
        loadKabKota(1);
    }

    private void loadKabKota(int provId) {
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.getKabKota(provId).enqueue(new Callback<KabKotaResponse>() {
            @Override
            public void onResponse(Call<KabKotaResponse> call, Response<KabKotaResponse> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    KabKotaResponse body = resp.body();
                    if (body.status == 200) {
                        Toast.makeText(KabKotaActivity.this,"Data sudah ada", Toast.LENGTH_SHORT).show();
                        adapter.setData(body.result);
                    } else {
                        Toast.makeText(KabKotaActivity.this, body.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(KabKotaActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<KabKotaResponse> call, Throwable t) {
                Toast.makeText(KabKotaActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
