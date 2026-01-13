package com.theputras.posrentalps;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.theputras.posrentalps.api.ApiClient;
import com.theputras.posrentalps.api.ApiService;
import com.theputras.posrentalps.databinding.ActivityRegisterBinding;
import java.io.IOException;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.getClient(this).create(ApiService.class);

        binding.btnRegister.setOnClickListener(v -> {
            String nama = binding.etNameRegister.getText().toString();
            String email = binding.etEmailRegister.getText().toString();
            String pass = binding.etPasswordRegister.getText().toString();
            String confirmPass = binding.etConfirmPasswordRegister.getText().toString();

            if (nama.isEmpty() || email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Semua data harus diisi!", Toast.LENGTH_SHORT).show();
            } else if (!pass.equals(confirmPass)) {
                Toast.makeText(this, "Password konfirmasi tidak sama!", Toast.LENGTH_SHORT).show();
            } else {
                registerProcess(nama, email, pass, confirmPass);
            }
        });

        binding.tvGoToLogin.setOnClickListener(v -> {
            finish();
        });
    }

    private void registerProcess(String name, String email, String password, String passwordConfirm) {
        binding.btnRegister.setEnabled(false);
        binding.btnRegister.setText("Loading...");

        apiService.register(name, email, password, passwordConfirm).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                binding.btnRegister.setEnabled(true);
                binding.btnRegister.setText("DAFTAR");

                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Registrasi Berhasil! Silakan Login.", Toast.LENGTH_LONG).show();
                    // Kembali ke halaman Login
                    finish();
                } else {
                    try {
                        // Tampilkan error dari backend (misal: email sudah dipakai)
                        String errorBody = response.errorBody().string();
                        Toast.makeText(RegisterActivity.this, "Gagal: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(RegisterActivity.this, "Registrasi Gagal.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                binding.btnRegister.setEnabled(true);
                binding.btnRegister.setText("DAFTAR");
                Toast.makeText(RegisterActivity.this, "Koneksi Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}