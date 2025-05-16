package com.example.nyomdapp.Activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nyomdapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private Button uploadButton;
    private Button profileButton;
    private Button navigateButton; // ÚJ
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        uploadButton = findViewById(R.id.uploadButton);
        profileButton = findViewById(R.id.profileButton);
        navigateButton = findViewById(R.id.navigateButton);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        uploadButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UploadActivity.class);
            startActivity(intent);
        });

        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileView.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        navigateButton.setOnClickListener(v -> {
            Log.d("MainActivity", "Navigáció gomb megnyomva");

            // Web alapú Maps URL, megbízhatóbb, mint a geo: URI
            String location = "https://www.google.hu/maps/place/SZTE+J%C3%B3zsef+Attila+Tanulm%C3%A1nyi+%C3%A9s+Inform%C3%A1ci%C3%B3s+K%C3%B6zpont/@46.2480947,20.1456586,17z/data=!4m6!3m5!1s0x4744887a08556bb3:0x87091c298cd2a2b1!8m2!3d46.2470397!4d20.1419773!16s%2Fg%2F122vxhss?entry=ttu&g_ep=EgoyMDI1MDUwNy4wIKXMDSoASAFQAw%3D%3D";

            try {
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(location));
                Intent chooser = Intent.createChooser(mapIntent, "Térkép megnyitása...");
                startActivity(chooser);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "Nem található térképalkalmazás", Toast.LENGTH_SHORT).show();
            }


        });


    }
}
