package com.example.newproject;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return fragment berdasarkan posisi tab
        switch (position) {
            case 0:
                return new ItemListFragment(); // Posisi 0 untuk Item
            case 1:
                return new KonsumenListFragment(); // Posisi 1 untuk Konsumen
            case 2:
                return new FireListFragment();   // TAB BARU
            default:
                return new ItemListFragment(); // Default
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Jumlah tab kita ada 2 (Item dan Konsumen)
    }
}