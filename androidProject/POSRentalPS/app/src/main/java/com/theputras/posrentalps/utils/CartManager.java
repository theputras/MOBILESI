package com.theputras.posrentalps.utils;

import com.theputras.posrentalps.model.TransactionRequest;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;

    // Class wrapper untuk tampilan di Cart
    public static class CartDisplay {
        public TransactionRequest request;
        public String displayName;
        public int price;
        public int qty; // TAMBAHAN: Menyimpan jumlah pesanan

        public CartDisplay(TransactionRequest req, String name, int prc, int qty) {
            this.request = req;
            this.displayName = name;
            this.price = prc;
            this.qty = qty;
        }
    }

    private List<CartDisplay> displayItems = new ArrayList<>();

    private CartManager() {}

    public static synchronized CartManager getInstance() {
        if (instance == null) instance = new CartManager();
        return instance;
    }

    // UPDATE: Tambahkan parameter qty
    public void addItem(TransactionRequest req, String displayName, int price, int qty) {
        // Kita simpan request dasarnya, qty-nya nanti diurus pas looping di PaymentActivity
        displayItems.add(new CartDisplay(req, displayName, price, qty));
    }

    public List<CartDisplay> getDisplayList() { return displayItems; }

    public void clear() {
        displayItems.clear();
    }

    // UPDATE: Hitung total dikali qty
    public int getTotalPrice() {
        int total = 0;
        for (CartDisplay item : displayItems) {
            total += (item.price * item.qty);
        }
        return total;
    }

    // ... method sebelumnya ...

    // Hapus item berdasarkan index
    public void removeItem(int position) {
        if (position >= 0 && position < displayItems.size()) {
            displayItems.remove(position);
        }
    }

    // Tambah Qty
    public void increaseQty(int position) {
        if (position >= 0 && position < displayItems.size()) {
            CartDisplay item = displayItems.get(position);
            item.qty = item.qty + 1;
        }
    }

    // Kurang Qty (Minimal 1)
    public void decreaseQty(int position) {
        if (position >= 0 && position < displayItems.size()) {
            CartDisplay item = displayItems.get(position);
            if (item.qty > 1) {
                item.qty = item.qty - 1;
            }
        }
    }
}