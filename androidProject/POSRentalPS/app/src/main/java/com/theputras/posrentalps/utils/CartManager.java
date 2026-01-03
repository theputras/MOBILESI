package com.theputras.posrentalps.utils;

import com.theputras.posrentalps.model.PaketSewa;
import com.theputras.posrentalps.model.Tv;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private List<CartDisplay> cartItems;

    private CartManager() {
        cartItems = new ArrayList<>();
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public List<CartDisplay> getCartItems() {
        return cartItems;
    }

    public void addItem(Tv tv, PaketSewa paket) {
        cartItems.add(new CartDisplay(tv, paket));
    }

    // --- FITUR BARU: Hapus Item ---
    public void removeItem(int position) {
        if (position >= 0 && position < cartItems.size()) {
            cartItems.remove(position);
        }
    }

    public void clearCart() {
        cartItems.clear();
    }

    public int getTotalPrice() {
        int total = 0;
        for (CartDisplay item : cartItems) {
            // Cek null safety agar tidak crash
            if (item.getPaket() != null) {
                total += item.getPaket().harga;
            }
        }
        return total;
    }

    // --- Inner Class ---
    public static class CartDisplay {
        private Tv tv;
        private PaketSewa paket;

        public CartDisplay(Tv tv, PaketSewa paket) {
            this.tv = tv;
            this.paket = paket;
        }

        public Tv getTv() { return tv; }
        public PaketSewa getPaket() { return paket; }

        public String getDisplayName() {
            // Null safety
            String noTv = (tv != null) ? tv.getNomorTv() : "?";
            String namaPaket = (paket != null) ? paket.namaPaket : "?";
            return noTv + " - " + namaPaket;
        }

        public int getPrice() {
            return (paket != null) ? paket.harga : 0;
        }
    }
}