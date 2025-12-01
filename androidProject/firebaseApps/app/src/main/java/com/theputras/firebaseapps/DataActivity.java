package com.theputras.firebaseapps;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class DataActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Layout ini nanti isinya <fragment> yang mengarah ke FireListFragment
        setContentView(R.layout.activity_data);
    }
}