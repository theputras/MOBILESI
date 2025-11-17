package com.theputras.firebaseapps;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.theputras.firebaseapps.adapter.FireAdapter;
import com.theputras.firebaseapps.models.FireModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView fireRecycler;
    FireAdapter adapter;
    ArrayList<FireModel> list = new ArrayList<>();

    FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fireRecycler = findViewById(R.id.fireRecycler);
        fireRecycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FireAdapter(list);
        fireRecycler.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadFirestoreRealtime();
    }

    private void loadFirestoreRealtime() {
        db.collection("fireData")
                .addSnapshotListener((value, error) -> {

                    if (error != null || value == null) return;

                    list.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        FireModel model = doc.toObject(FireModel.class);
                        if (model != null) list.add(model);
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}