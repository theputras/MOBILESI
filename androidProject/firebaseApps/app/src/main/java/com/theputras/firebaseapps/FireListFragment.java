package com.theputras.firebaseapps;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;

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
    private FloatingActionButton fabAdd; // Tambahan
    private FireAdapter adapter;
    private ArrayList<FireModel> list = new ArrayList<>();
    private FirebaseFirestore db;

    // Variabel untuk melacak jumlah data sebelumnya (Sesuai Referensi)
    private int lastCount = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fire_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewFire);
        progressBar = view.findViewById(R.id.progressBarFire);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayoutFire);
        fabAdd = view.findViewById(R.id.fabAdd); // Inisialisasi FAB

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FireAdapter(list);
        recyclerView.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();

        // 1. Logic Tombol Tambah (Create)
        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), FormActivity.class));
        });

        // 2. Logic Klik Item -> Muncul Menu Update/Delete
        adapter.setOnItemClickListener(model -> {
            String[] options = {"Update", "Delete"};
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Pilih Aksi");
            builder.setItems(options, (dialog, which) -> {
                if (which == 0) {
                    // Update
                    Intent intent = new Intent(getContext(), FormActivity.class);
                    intent.putExtra("id", model.getId());
                    intent.putExtra("name", model.getName());
                    intent.putExtra("desc", model.getDesc());
                    startActivity(intent);
                } else {
                    // Delete
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
        db.collection("fireData").document(id)
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Data dihapus", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Gagal hapus", Toast.LENGTH_SHORT).show());
    }

    private void loadFirestoreRealtime() {
        progressBar.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setRefreshing(true);

        // PENTING: Nama collection disesuaikan dengan FormActivity kamu ("fireData")
        db.collection("fireData")
                .addSnapshotListener((value, error) -> {
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);

                    if (error != null) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Gagal load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    if (value == null) return;

                    // ==========================================
                    // LOGIKA NOTIFIKASI (Sesuai Referensi Github)
                    // ==========================================
                    int currentCount = value.size();

                    if (lastCount == -1) {
                        // Load pertama kali, jangan notif dulu
                        lastCount = currentCount;
                    } else {
                        if (currentCount > lastCount) {
                            // ADA DATA DITAMBAHKAN
                            int added = currentCount - lastCount;
                            lastCount = currentCount;

                            if (added > 0) {
                                showMahasiswaAddedNotification(added);
                                showUpdatePopup(added, true); // true = tambah
                            }
                        } else if (currentCount < lastCount) {
                            // ADA DATA DIHAPUS
                            int deleted = lastCount - currentCount;
                            lastCount = currentCount;

                            if (deleted > 0) {
                                showMahasiswaDeletedNotification(deleted);
                                showUpdatePopup(deleted, false); // false = hapus
                            }
                        } else {
                            // Jumlah sama, update lastCount aja
                            lastCount = currentCount;
                        }
                    }

                    // Update List RecyclerView
                    list.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        FireModel model = doc.toObject(FireModel.class);
                        if (model != null) {
                            model.setId(doc.getId()); // Set ID biar bisa di-update/delete
                            list.add(model);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    // ==========================
    // NOTIF: DATA DITAMBAHKAN
    // ==========================
    private void showMahasiswaAddedNotification(int addedCount) {
        if (getContext() == null) return;

        String title = "Data Mahasiswa Terupdate";
        String message;

        if (addedCount == 1) {
            message = "Ada 1 mahasiswa baru yang ditambahkan.";
        } else {
            message = "Ada " + addedCount + " mahasiswa baru yang ditambahkan.";
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(requireContext(), MainActivity.CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_foreground) // Pastikan icon ini ada
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(requireContext());

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        notificationManager.notify(1001, builder.build());
    }

    // ==========================
    // NOTIF: DATA DIHAPUS
    // ==========================
    private void showMahasiswaDeletedNotification(int deletedCount) {
        if (getContext() == null) return;

        String title = "Data Mahasiswa Terupdate";
        String message;

        if (deletedCount == 1) {
            message = "Ada 1 data mahasiswa yang dihapus.";
        } else {
            message = "Ada " + deletedCount + " data mahasiswa yang dihapus.";
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(requireContext(), MainActivity.CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(requireContext());

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        notificationManager.notify(1002, builder.build());
    }

    // ==========================
    // POPUP 15 DETIK (ADD / DELETE)
    // ==========================
    private void showUpdatePopup(int count, boolean isAddedAction) {
        if (!isAdded() || getContext() == null) return;

        String title = "Data Mahasiswa Terupdate";
        String message;

        if (isAddedAction) {
            // Popup tambah
            if (count == 1) {
                message = "Ada 1 mahasiswa baru yang ditambahkan.";
            } else {
                message = "Ada " + count + " mahasiswa baru yang ditambahkan.";
            }
        } else {
            // Popup hapus
            if (count == 1) {
                message = "Ada 1 data mahasiswa yang dihapus.";
            } else {
                message = "Ada " + count + " data mahasiswa yang dihapus.";
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .create();

        dialog.show();

        // Auto-dismiss setelah 5 detik
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dialog.isShowing() && isAdded()) {
                dialog.dismiss();
            }
        }, 5000);
    }

}