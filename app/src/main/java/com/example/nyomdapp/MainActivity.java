package com.example.nyomdapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private Button uploadButton;
    private Button profileButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializálás
        mAuth = FirebaseAuth.getInstance();
        uploadButton = findViewById(R.id.uploadButton);
        profileButton = findViewById(R.id.profileButton);

        // Ellenőrizd, hogy a felhasználó be van-e jelentkezve
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Ha nincs bejelentkezve, irányítsd a bejelentkezési oldalra
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        // Dokumentum feltöltése activity megnyitása
        uploadButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UploadActivity.class);
            startActivity(intent);
        });

        // Profil oldal megnyitása
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileView.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }
}
