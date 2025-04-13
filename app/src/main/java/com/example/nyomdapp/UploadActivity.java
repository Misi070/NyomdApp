package com.example.nyomdapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class UploadActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;
    private Uri fileUri;
    private EditText copiesEditText;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        firestore = FirebaseFirestore.getInstance();

        copiesEditText = findViewById(R.id.copiesEditText);

        Button chooseFileButton = findViewById(R.id.chooseFileButton);
        Button uploadButton = findViewById(R.id.uploadButton);

        chooseFileButton.setOnClickListener(v -> openFileChooser());
        uploadButton.setOnClickListener(v -> uploadFile());
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            fileUri = data.getData();
            Toast.makeText(this, "Fájl kiválasztva", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Fájl kiválasztása nem sikerült", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadFile() {
        ProgressBar uploadProgressBar = findViewById(R.id.uploadProgressBar);
        uploadProgressBar.setVisibility(View.VISIBLE);

        if (fileUri == null) {
            Toast.makeText(this, "Kérlek válassz fájlt!", Toast.LENGTH_SHORT).show();
            return;
        }

        String copies = copiesEditText.getText().toString().trim();
        if (copies.isEmpty()) {
            Toast.makeText(this, "Kérlek add meg a példányszámot!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ellenőrzés, hogy a példányszám pozitív szám-e
        int numCopies;
        try {
            numCopies = Integer.parseInt(copies);
            if (numCopies <= 0) {
                Toast.makeText(this, "A példányszámnak pozitív számnak kell lennie!", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Érvénytelen példányszám!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        // Egyedi fájlnév generálása (ellenőrizve a fájl URI-ját)
        String fileName = generateFileName(userId);

        // Firebase Storage referencia
        StorageReference fileRef = storage.getReference().child("upload/" + userId + "/" + fileName);

        // Fájl feltöltése
        fileRef.putFile(fileUri).addOnSuccessListener(taskSnapshot -> {

            // Fájl URL lekérése
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // A fájl letöltési URL-jét és egyéb adatokat mentjük a Firestore-ba
                saveFileDataToFirestore(uri.toString(), numCopies, userId);
            }).addOnFailureListener(e -> {
                Toast.makeText(UploadActivity.this, "Hiba a fájl URL lekérésében!", Toast.LENGTH_SHORT).show();
                Log.e("UploadActivity", "Hiba a fájl URL lekérésében", e);
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Hiba a fájl feltöltésekor!", Toast.LENGTH_SHORT).show();
            Log.e("UploadActivity", "Hiba a fájl feltöltésénél", e);
        });
        uploadProgressBar.setVisibility(View.GONE);
    }

    private String generateFileName(String userId) {
        String fileName = "file_" + System.currentTimeMillis() + "_" + (fileUri != null ? fileUri.getLastPathSegment() : "unknown");
        return fileName;
    }

    private void saveFileDataToFirestore(String fileUrl, int numCopies, String userId) {
        // Adatok előkészítése a Firestore-ba történő mentéshez
        Map<String, Object> fileData = new HashMap<>();
        fileData.put("fileUrl", fileUrl);
        fileData.put("copies", numCopies);
        fileData.put("userId", userId);
        fileData.put("timestamp", FieldValue.serverTimestamp());  // Időbélyeg hozzáadása

        // Dokumentum hozzáadása a Firestore-ba
        firestore.collection("documents").add(fileData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(UploadActivity.this, "Fájl sikeresen feltöltve!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UploadActivity.this, "Hiba a fájl mentésekor!", Toast.LENGTH_SHORT).show();
                    Log.e("UploadActivity", "Hiba a Firestore mentésnél", e);
                });
    }
}
