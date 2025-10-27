package com.example.newproject;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView textViewRegister;
    // 2. Buat teks lengkapnya
    String text = "Don't have an account? Sign up";
    SpannableString ss = new SpannableString(text);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_main);

        etEmail = findViewById(R.id.editTextUsername);
        etPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.btnToRegister);

        // 3. Set listener untuk tombol login
        btnLogin.setOnClickListener(v -> doLogin());

        // 4. Panggil method untuk setup teks "Sign up"
        setupSignUpText();


    }
    private void setupSignUpText() {
        // 5. Semua logika untuk membuat teks "Sign up" bisa diklik
        //    sekarang ada di dalam method ini, yang dipanggil dari onCreate.

        String text = "Don't have an account? Sign up";
        SpannableString ss = new SpannableString(text);

        // Buat bagian yang bisa diklik (ClickableSpan)
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Tulis aksi pindah halaman di sini
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false); // Hilangkan garis bawah
                ds.setFakeBoldText(true);   // Buat teks jadi tebal

                // Cara yang lebih aman untuk mengambil warna (hindari crash jika R.color.purple_500 tidak ada)
                // Coba ganti dengan warna primary dari tema Anda
                try {
                    // Coba ambil warna dari R.color
                    ds.setColor(    ContextCompat.getColor(LoginActivity.this, R.color.purple_500));
                } catch (Exception e) {
                    // Jika gagal, pakai warna default (misal, warna text hint)
                    ds.setColor(ds.linkColor);
                }
            }
        };

        // Terapkan ClickableSpan ke kata "Sign up"
        int startIndex = text.indexOf("Sign up");
        int endIndex = startIndex + "Sign up".length();
        ss.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set teks dan MovementMethod-nya ke TextView
        textViewRegister.setText(ss);
        textViewRegister.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void doLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        ApiService apiService = RetrofitClient.getInstance().getRetrofitInstance().create(ApiService.class);
        Call<LoginResponse> call = apiService.login(new LoginRequest(email, password));

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getAccessToken();
                    // DEBUG LOG TOKEN
                    Log.d(TAG, "Token saat GET items: " + token);
                    SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                    prefs.edit().putString("access_token", token).apply();
                    Toast.makeText(LoginActivity.this, "Login sukses", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Login gagal", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
