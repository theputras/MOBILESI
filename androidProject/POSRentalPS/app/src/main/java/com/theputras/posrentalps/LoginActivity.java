package com.theputras.posrentalps;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.theputras.posrentalps.api.ApiClient;
import com.theputras.posrentalps.api.ApiService;
import com.theputras.posrentalps.databinding.ActivityLoginBinding;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private ApiService apiService;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inisialisasi API dan SharedPreferences
        apiService = ApiClient.getClient(this).create(ApiService.class);
        sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE);

        // Cek jika user sudah login sebelumnya, langsung lempar ke MainActivity
        if (sharedPreferences.contains("token")) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmailLogin.getText().toString();
            String password = binding.etPasswordLogin.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan Password harus diisi!", Toast.LENGTH_SHORT).show();
            } else {
                loginProcess(email, password);
            }
        });

        binding.tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void loginProcess(String email, String password) {
        binding.btnLogin.setEnabled(false);
        binding.btnLogin.setText("Loading...");

        apiService.login(email, password).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                binding.btnLogin.setEnabled(true);
                binding.btnLogin.setText("LOGIN");

                if (response.isSuccessful()) {
                    try {
                        String result = response.body().string();
                        JSONObject jsonObject = new JSONObject(result);

                        // --- PERBAIKAN DISINI ---
                        // 1. Ambil token dengan kunci "access_token" (bukan "token")
                        String token = jsonObject.optString("access_token");

                        // 2. Ambil nama dari objek "user"
                        String name = "User"; // Default jika gagal ambil nama
                        if (jsonObject.has("user")) {
                            JSONObject userObj = jsonObject.getJSONObject("user");
                            name = userObj.optString("name");
                        }

                        if (!token.isEmpty()) {
                            // Simpan ke SharedPreferences
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("token", token);
                            editor.putString("name", name);
                            editor.apply();

                            Toast.makeText(LoginActivity.this, "Selamat datang, " + name, Toast.LENGTH_SHORT).show();

                            // Pindah ke Main Activity
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Token tidak ditemukan dalam respon server.", Toast.LENGTH_SHORT).show();
                        }
                        // ------------------------

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, "Gagal memproses data login.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Login Gagal! Cek Email/Password.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                binding.btnLogin.setEnabled(true);
                binding.btnLogin.setText("LOGIN");
                Toast.makeText(LoginActivity.this, "Koneksi Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}