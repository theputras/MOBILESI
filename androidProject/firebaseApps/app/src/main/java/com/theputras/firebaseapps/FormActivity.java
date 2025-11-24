package com.theputras.firebaseapps;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class FormActivity extends AppCompatActivity {

    private EditText etName, etDesc;
    private Button btnSave;
    private FirebaseFirestore db;
    private String id = null; // Kalau null berarti mode Create

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        etName = findViewById(R.id.etName);
        etDesc = findViewById(R.id.etDesc);
        btnSave = findViewById(R.id.btnSave);
        db = FirebaseFirestore.getInstance();

        // Cek apakah ada data yang dikirim (Mode Update)
        if (getIntent().hasExtra("id")) {
            id = getIntent().getStringExtra("id");
            etName.setText(getIntent().getStringExtra("name"));
            etDesc.setText(getIntent().getStringExtra("desc"));
            btnSave.setText("Update Data");
        }

        btnSave.setOnClickListener(v -> saveData());
    }

    private void saveData() {
        String name = etName.getText().toString();
        String desc = etDesc.getText().toString();

        if (name.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Isi semua data!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("desc", desc);

        if (id != null) {
            // Mode Update
            db.collection("fireData").document(id)
                    .update(data)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Berhasil Update", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Gagal Update", Toast.LENGTH_SHORT).show());
        } else {
            // Mode Create
            db.collection("fireData")
                    .add(data)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Berhasil Simpan", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Gagal Simpan", Toast.LENGTH_SHORT).show());
        }
    }
}