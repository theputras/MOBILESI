package com.theputras.firebaseapps;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.theputras.firebaseapps.models.FireModel;

public class FormActivity extends AppCompatActivity {

    private EditText etNama, etNim, etProdi, etTtl, etUmur;
    private Button btnSimpan;
    private TextView tvTitle;

    private FirebaseFirestore db;
    private String id = ""; // Untuk menampung ID jika edit mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        db = FirebaseFirestore.getInstance();

        // Inisialisasi View
        tvTitle = findViewById(R.id.tv_title_form);
        etNama = findViewById(R.id.et_nama);
        etNim = findViewById(R.id.et_nim);
        etProdi = findViewById(R.id.et_prodi);
        etTtl = findViewById(R.id.et_ttl);
        etUmur = findViewById(R.id.et_umur);
        btnSimpan = findViewById(R.id.btn_simpan);

        // Cek apakah ini mode Edit? (Menerima data dari Adapter)
        if (getIntent().hasExtra("id")) {
            id = getIntent().getStringExtra("id");
            String nama = getIntent().getStringExtra("nama");
            String nim = getIntent().getStringExtra("nim");
            String prodi = getIntent().getStringExtra("prodi");
            String ttl = getIntent().getStringExtra("ttl");
            String umur = getIntent().getStringExtra("umur");

            // Set data ke EditText
            etNama.setText(nama);
            etNim.setText(nim);
            etProdi.setText(prodi);
            etTtl.setText(ttl);
            etUmur.setText(umur);

            btnSimpan.setText("UPDATE DATA");
            tvTitle.setText("Edit Data Mahasiswa");
        }

        btnSimpan.setOnClickListener(v -> {
            String sNama = etNama.getText().toString();
            String sNim = etNim.getText().toString();
            String sProdi = etProdi.getText().toString();
            String sTtl = etTtl.getText().toString();
            String sUmur = etUmur.getText().toString();

            if (sNama.isEmpty() || sNim.isEmpty()) {
                Toast.makeText(this, "Nama dan NIM wajib diisi!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Buat object model
            // ID dikosongkan dulu, nanti diisi otomatis atau pakai ID lama
            FireModel model = new FireModel(id, sProdi, sNama, sNim, sTtl, sUmur);

            if (id != null && !id.isEmpty()) {
                updateData(model);
            } else {
                saveData(model);
            }
        });
    }

    private void saveData(FireModel model) {
        // Simpan data baru -> .add() akan generate ID otomatis
        db.collection("mahasiswa") // Nama koleksi di Firestore diganti jadi 'mahasiswa'
                .add(model)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(FormActivity.this, "Berhasil Menambahkan Data", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(FormActivity.this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateData(FireModel model) {
        // Update data berdasarkan ID -> .set() atau .update()
        db.collection("mahasiswa").document(id)
                .set(model) // .set akan menimpa data lama dengan data baru
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(FormActivity.this, "Berhasil Update Data", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(FormActivity.this, "Gagal Update", Toast.LENGTH_SHORT).show();
                });
    }
}