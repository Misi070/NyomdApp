package com.example.nyomdapp.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nyomdapp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UploadActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST_CODE = 1;
    private Button chooseFileButton;
    private Button uploadButton;
    private TextInputEditText copiesEditText;
    private Uri selectedFileUri;
    private String selectedFileName;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ImageView uploadSuccesImageView;
    private Spinner sizeSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        uploadSuccesImageView = findViewById(R.id.uploadSuccessImageView);
        chooseFileButton = findViewById(R.id.chooseFileButton);
        uploadButton = findViewById(R.id.uploadButton);
        copiesEditText = findViewById(R.id.copiesEditText);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        chooseFileButton.setOnClickListener(v -> openFileChooser());

        sizeSpinner = findViewById(R.id.sizeSpinner);

        // Spinner adatainak betöltése
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.document_sizes,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sizeSpinner.setAdapter(adapter);


        uploadButton.setOnClickListener(v -> {
            if (selectedFileUri == null) {
                Toast.makeText(this, "Nincs fájl kiválasztva!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isOnMobileData()) {
                new AlertDialog.Builder(this)
                        .setTitle("Mobilinternet észlelve")
                        .setMessage("Mobilinternetet használsz. Biztosan szeretnél fájlt feltölteni?")
                        .setPositiveButton("Igen", (dialog, which) -> continueUpload())
                        .setNegativeButton("Mégsem", null)
                        .show();
            } else {
                continueUpload();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "Válassz egy fájlt"), PICK_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedFileUri = data.getData();
            selectedFileName = getFileName(selectedFileUri);
            Toast.makeText(this, "Fájl kiválasztva: " + selectedFileName, Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    private boolean isOnMobileData() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnected() &&
                activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    private void continueUpload() {
        String copiesStr = copiesEditText.getText().toString().trim();
        if (copiesStr.isEmpty()) {
            copiesEditText.setError("Add meg a példányszámot!");
            return;
        }

        int copies = Integer.parseInt(copiesStr);
        String selectedSize = sizeSpinner.getSelectedItem().toString();

        uploadFileNameToFirestore(selectedFileName, copies, selectedSize);
    }


    private void uploadFileNameToFirestore(String fileName, int copies, String size) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "anon";

        Map<String, Object> documentData = new HashMap<>();
        documentData.put("fileName", fileName);
        documentData.put("copies", copies);
        documentData.put("size", size);
        documentData.put("status", "Beérkezett");
        documentData.put("timestamp", Timestamp.now());
        documentData.put("userId", userId);

        db.collection("documents")
                .document()
                .set(documentData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Sikeres mentés Firestore-ba", Toast.LENGTH_SHORT).show();
                    startUploadSuccessAnimationAndRedirect();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hiba történt: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void startUploadSuccessAnimationAndRedirect() {
        uploadSuccesImageView.setVisibility(View.VISIBLE);
        Animation tickAnim = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        uploadSuccesImageView.startAnimation(tickAnim);

        uploadSuccesImageView.postDelayed(() -> {
            View rootView = findViewById(android.R.id.content);
            Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
            rootView.startAnimation(fadeOut);

            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    Intent intent = new Intent(UploadActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
        }, 1000);
    }
}
