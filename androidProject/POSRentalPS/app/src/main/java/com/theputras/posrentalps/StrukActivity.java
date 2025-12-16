package com.theputras.posrentalps;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.theputras.posrentalps.utils.CartManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StrukActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_struk);

        LinearLayout container = findViewById(R.id.containerItems);
        TextView tvDate = findViewById(R.id.tvStrukDate);
        TextView tvName = findViewById(R.id.tvStrukName);
        TextView tvTotal = findViewById(R.id.tvStrukTotal);
        TextView tvCash = findViewById(R.id.tvStrukCash);
        TextView tvChange = findViewById(R.id.tvStrukChange);
        Button btnBack = findViewById(R.id.btnBackHome);

        // Set Header Data
        String date = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(new Date());
        tvDate.setText(date);

        String custName = getIntent().getStringExtra("CUSTOMER_NAME");
        tvName.setText("Customer: " + (custName.isEmpty() ? "Guest" : custName));

        int total = 0;

        // Dinamis Menambahkan Item ke Struk
        for (CartManager.CartDisplay item : CartManager.getInstance().getDisplayList()) {
            TextView tvItem = new TextView(this);
            tvItem.setText(item.displayName);
            tvItem.setTextSize(14);
            tvItem.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1.0f));

            TextView tvPrice = new TextView(this);
            tvPrice.setText(String.format("Rp %,d", item.price));
            tvPrice.setTextSize(14);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.addView(tvItem);
            row.addView(tvPrice);
            row.setPadding(0, 8, 0, 8);

            container.addView(row);
            total += item.price;
        }

        int cash = getIntent().getIntExtra("CASH_GIVEN", 0);
        int change = cash - total;

        tvTotal.setText(String.format("TOTAL: Rp %,d", total));
        tvCash.setText(String.format("TUNAI: Rp %,d", cash));
        tvChange.setText(String.format("KEMBALI: Rp %,d", change));

        // Bersihkan Keranjang setelah Struk tampil
        CartManager.getInstance().clear();

        btnBack.setOnClickListener(v -> finish());
    }
}