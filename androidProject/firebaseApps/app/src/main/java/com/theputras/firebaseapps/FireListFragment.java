package com.theputras.firebaseapps;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.theputras.firebaseapps.adapter.FireAdapter;
import com.theputras.firebaseapps.models.FireModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class FireListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FireAdapter adapter;
    private ArrayList<FireModel> list = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fire_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewFire);
        progressBar = view.findViewById(R.id.progressBarFire);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayoutFire);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FireAdapter(list);
        recyclerView.setAdapter(adapter);
        // Inisialisasi Firestore
        db = FirebaseFirestore.getInstance();

        loadFirestoreRealtime();

        swipeRefreshLayout.setOnRefreshListener(this::loadFirestoreRealtime);

        return view;
    }
    // Fungsi untuk memuat data dari Firestore secara realtime
    private void loadFirestoreRealtime() {
        progressBar.setVisibility(View.VISIBLE);
        // Mengambil data dari koleksi "fireData"
        db.collection("fireData")
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                        return;
                    }

                    list.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        FireModel model = doc.toObject(FireModel.class);
                        if (model != null) {
                            list.add(model);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                });
    }


}