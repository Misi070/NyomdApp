package com.example.nyomdapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileView extends AppCompatActivity {

    private TextView welcomeTextView;
    private Button logoutButton;
    private ListView documentsListView;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        // Inicializálás
        mAuth = FirebaseAuth.getInstance();
        welcomeTextView = findViewById(R.id.welcomeTextView);
        logoutButton = findViewById(R.id.logoutButton);
        documentsListView = findViewById(R.id.documentsListView);

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String email = currentUser.getEmail();

            if (email != null) {
                int atIndex = email.indexOf('@');
                if (atIndex != -1) {
                    email = email.substring(0, atIndex);
                }
            }

            welcomeTextView.setText("Üdvözöljük, " + email);
        }

        // Beégetett adatok a ListView-hoz
        String[] documents = {"Document 1", "Document 2", "Document 3"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, documents);
        documentsListView.setAdapter(adapter);

        // Kijelentkezés logika
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ProfileView.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
