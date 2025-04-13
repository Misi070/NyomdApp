package com.example.nyomdapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializálás
        mAuth = FirebaseAuth.getInstance();
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        // Automatikus beléptetés
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Ha már be van jelentkezve, irányítsuk át a MainActivity-re
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Kilépünk a LoginActivity-ből, hogy a back gomb ne hozza vissza
        } else {
            // Ha nincs bejelentkezve, mutatjuk a bejelentkezési képernyőt
            setupLoginAndRegisterButtons();
        }
    }

    private void setupLoginAndRegisterButtons() {
        // Gomb események
        findViewById(R.id.loginButton).setOnClickListener(v -> loginUser());
        findViewById(R.id.registerButton).setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Ellenőrizzük, hogy a mezők nincsenek-e üresen
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Kérlek töltsd ki az összes mezőt!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Bejelentkezés a Firebase segítségével
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sikeres bejelentkezés után átirányítjuk a MainActivity-be
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        // Ha hiba történik, akkor értesítjük a felhasználót
                        Toast.makeText(this, "Hiba: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
