package com.theputras.posrentalps.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.theputras.posrentalps.R;

public class AccountFragment extends Fragment {

    private SwitchMaterial switchDarkMode;
    private LinearLayout btnAppInfo, btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        switchDarkMode = view.findViewById(R.id.switchDarkMode);
        btnAppInfo = view.findViewById(R.id.btnAppInfo);
        btnLogout = view.findViewById(R.id.btnLogout);

        // 1. SETUP DARK MODE (Simpan pilihan user di memori HP)
        SharedPreferences prefs = requireContext().getSharedPreferences("AppSetting", Context.MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);

        // Set posisi switch sesuai simpanan
        switchDarkMode.setChecked(isDarkMode);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("dark_mode", isChecked);
            editor.apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                Toast.makeText(getContext(), "Mode Gelap Aktif", Toast.LENGTH_SHORT).show();
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                Toast.makeText(getContext(), "Mode Terang Aktif", Toast.LENGTH_SHORT).show();
            }
        });

        // 2. SETUP INFO APLIKASI
        btnAppInfo.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Rental PS Santuy")
                    .setMessage("Versi 1.0.0 (Beta)\n\nDibuat oleh: Putra Nur\nTeknologi: Android Java & Laravel 11")
                    .setPositiveButton("Oke", null)
                    .show();
        });

        // 3. SETUP LOGOUT
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Konfirmasi")
                    .setMessage("Apakah Anda yakin ingin keluar dari aplikasi?")
                    .setPositiveButton("Ya, Keluar", (dialog, which) -> {
                        // Karena belum ada Login Activity, kita tutup aplikasi saja
                        requireActivity().finishAffinity();
                        System.exit(0);
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });
    }
}