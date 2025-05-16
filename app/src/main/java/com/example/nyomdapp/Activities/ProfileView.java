package com.example.nyomdapp.Activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.nyomdapp.R;
import com.example.nyomdapp.worker.StatusUpdateWorker;
import com.example.nyomdapp.model.UploadEntry;
import com.example.nyomdapp.adapters.UploadEntryAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class ProfileView extends AppCompatActivity {

    private static final String CHANNEL_ID = "document_status_channel";
    private TextView welcomeTextView;
    private Button logoutButton;
    private RecyclerView documentsRecyclerView;
    private TextView emptyListMessage;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ListenerRegistration documentListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        welcomeTextView = findViewById(R.id.welcomeTextView);
        logoutButton = findViewById(R.id.logoutButton);
        documentsRecyclerView = findViewById(R.id.documentsRecyclerView);
        emptyListMessage = findViewById(R.id.emptyListMessage);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            if (email != null && email.contains("@")) {
                email = email.substring(0, email.indexOf('@'));
            }
            welcomeTextView.setText("Üdvözöljük, " + email);
        }

        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(ProfileView.this, LoginActivity.class));
            finish();
        });

        createNotificationChannel();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1001
                );
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        listenToUserDocuments();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (documentListener != null) {
            documentListener.remove();
            documentListener = null;
            Log.d("ProfileView", "Firestore listener leiratkozva.");
        }
    }

    private void listenToUserDocuments() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();
        documentListener = db.collection("documents")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("ProfileView", "Hiba dokumentumfigyelés közben", e);
                        Toast.makeText(this, "Hiba dokumentumfigyelés közben", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        ArrayList<UploadEntry> documentList = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            UploadEntry upload = doc.toObject(UploadEntry.class);
                            upload.setDocumentId(doc.getId());
                            documentList.add(upload);

                            if ("Beérkezett".equalsIgnoreCase(upload.getStatus()) && upload.getStatusUpdateStep() == 0) {
                                updateDocumentStatus(db, doc.getId(), upload.getCopies());
                            }
                        }

                        if (documentList.isEmpty()) {
                            emptyListMessage.setVisibility(View.VISIBLE);
                        } else {
                            emptyListMessage.setVisibility(View.GONE);
                        }

                        UploadEntryAdapter adapter = new UploadEntryAdapter(this, documentList);
                        documentsRecyclerView.setAdapter(adapter);
                        documentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                    }
                });
    }

    private void updateDocumentStatus(FirebaseFirestore db, String documentId, int copies) {
        db.collection("documents")
                .document(documentId)
                .update("statusUpdateStep", 1)
                .addOnSuccessListener(aVoid -> Log.d("StatusUpdate", "statusUpdateStep = 1 beállítva"))
                .addOnFailureListener(e -> Log.e("StatusUpdate", "Nem sikerült statusUpdateStep-et frissíteni", e));

        Data data1 = new Data.Builder()
                .putString(StatusUpdateWorker.KEY_DOCUMENT_ID, documentId)
                .putString(StatusUpdateWorker.KEY_STATUS, "Nyomtatás alatt")
                .build();

        OneTimeWorkRequest printingRequest = new OneTimeWorkRequest.Builder(StatusUpdateWorker.class)
                .setInitialDelay(30, TimeUnit.SECONDS)
                .setInputData(data1)
                .build();

        WorkManager.getInstance(this).enqueue(printingRequest);

        Data data2 = new Data.Builder()
                .putString(StatusUpdateWorker.KEY_DOCUMENT_ID, documentId)
                .putString(StatusUpdateWorker.KEY_STATUS, "Kinyomtatva")
                .build();

        OneTimeWorkRequest readyRequest = new OneTimeWorkRequest.Builder(StatusUpdateWorker.class)
                .setInitialDelay(30 + (copies * 2L), TimeUnit.SECONDS)
                .setInputData(data2)
                .build();

        WorkManager.getInstance(this).enqueue(readyRequest);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Document Status Channel";
            String description = "Értesítések a dokumentum státusz frissítéseiről";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Értesítési engedély megadva", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Az értesítések le vannak tiltva", Toast.LENGTH_SHORT).show();
        }
    }
}
