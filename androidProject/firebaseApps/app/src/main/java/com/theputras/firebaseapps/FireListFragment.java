package com.theputras.firebaseapps;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.theputras.firebaseapps.adapter.FireAdapter;
import com.theputras.firebaseapps.models.FireModel;

import java.util.ArrayList;

public class FireListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabAdd;
    private FireAdapter adapter;
    private ArrayList<FireModel> list = new ArrayList<>();
    private FirebaseFirestore db;

    // Variabel untuk melacak jumlah data sebelumnya
    private int lastCount = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fire_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewFire);
        progressBar = view.findViewById(R.id.progressBarFire);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayoutFire);
        fabAdd = view.findViewById(R.id.fabAdd);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Note: Pastikan Constructor Adapter sesuai (Context, List)
        adapter = new FireAdapter(getContext(), list);
        recyclerView.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();

        // 1. Logic Tombol Tambah (Create)
        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), FormActivity.class));
        });

        // 2. Logic Klik Item -> Muncul Menu Update/Delete
        adapter.setOnItemClickListener(model -> {
            String[] options = {"Cek Data (Print)", "Update Data", "Hapus Data"};

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Pilih Aksi: " + model.getNama());
            builder.setItems(options, (dialog, which) -> {
                if (which == 0) {
                    // --- 1. CEK DATA (Pindah ke DetailActivity) ---
                    // Jangan ke DataActivity lagi, nanti looping!
                    Intent intent = new Intent(getContext(), DetailActivity.class);

                    intent.putExtra("id", model.getId());
                    intent.putExtra("prodi", model.getProdi());
                    intent.putExtra("nama", model.getNama());
                    intent.putExtra("nim", model.getNim());
                    intent.putExtra("ttl", model.getTtl());
                    intent.putExtra("umur", model.getUmur());
                    startActivity(intent);

                } else if (which == 1) {
                    // --- 2. UPDATE ---
                    Intent intent = new Intent(getContext(), FormActivity.class);
                    intent.putExtra("id", model.getId());
                    intent.putExtra("prodi", model.getProdi());
                    intent.putExtra("nama", model.getNama());
                    intent.putExtra("nim", model.getNim());
                    intent.putExtra("ttl", model.getTtl());
                    intent.putExtra("umur", model.getUmur());
                    startActivity(intent);

                } else {
                    // --- 3. DELETE ---
                    deleteData(model.getId());
                }
            });
            builder.show();
        });

        loadFirestoreRealtime();
        swipeRefreshLayout.setOnRefreshListener(this::loadFirestoreRealtime);

        return view;
    }

    private void deleteData(String id) {
        // Hapus dari collection 'mahasiswa'
        db.collection("mahasiswa").document(id)
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Data berhasil dihapus", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Gagal hapus", Toast.LENGTH_SHORT).show());
    }

    private void loadFirestoreRealtime() {
        progressBar.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setRefreshing(true);

        db.collection("mahasiswa") // Pastikan nama collection sama dengan di FormActivity
                .addSnapshotListener((value, error) -> {
                    // Cek jika fragment sudah tidak aktif agar tidak crash
                    if (!isAdded() || getContext() == null) return;

                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);

                    if (error != null) {
                        Toast.makeText(getContext(), "Gagal load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value == null) return;

                    // --- LOGIKA NOTIFIKASI ---
                    int currentCount = value.size();

                    if (lastCount == -1) {
                        lastCount = currentCount;
                    } else {
                        if (currentCount > lastCount) {
                            int added = currentCount - lastCount;
                            showMahasiswaAddedNotification(added);
                            showUpdatePopup(added, true);
                        } else if (currentCount < lastCount) {
                            int deleted = lastCount - currentCount;
                            showMahasiswaDeletedNotification(deleted);
                            showUpdatePopup(deleted, false);
                        }
                        lastCount = currentCount;
                    }

                    list.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        FireModel model = doc.toObject(FireModel.class);
                        if (model != null) {
                            model.setId(doc.getId());
                            list.add(model);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    // ==========================
    // NOTIFIKASI & POPUP
    // ==========================

    private void showMahasiswaAddedNotification(int addedCount) {
        if (getContext() == null) return;
        String message = addedCount == 1 ? "Ada 1 mahasiswa baru ditambahkan." : "Ada " + addedCount + " mahasiswa baru ditambahkan.";

        sendNotification("Data Mahasiswa Bertambah", message, 1001);
    }

    private void showMahasiswaDeletedNotification(int deletedCount) {
        if (getContext() == null) return;
        String message = deletedCount == 1 ? "Ada 1 data mahasiswa dihapus." : "Ada " + deletedCount + " data mahasiswa dihapus.";

        sendNotification("Data Mahasiswa Berkurang", message, 1002);
    }

    private void sendNotification(String title, String message, int notifId) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Jika izin belum ada, return (atau minta izin di sini)
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), MainActivity.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat.from(requireContext()).notify(notifId, builder.build());
    }

    private void showUpdatePopup(int count, boolean isAddedAction) {
        if (!isAdded() || getContext() == null) return;

        String title = "Update Data";
        String message;

        if (isAddedAction) {
            message = count + " Mahasiswa Baru Telah Ditambahkan!";
        } else {
            message = count + " Data Mahasiswa Telah Dihapus!";
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .create();

        dialog.show();

        // Auto-dismiss 5 detik
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dialog.isShowing() && isAdded()) {
                dialog.dismiss();
            }
        }, 5000);
    }
}