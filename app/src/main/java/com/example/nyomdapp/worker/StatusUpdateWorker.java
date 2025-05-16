package com.example.nyomdapp.worker;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.example.nyomdapp.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.firebase.firestore.FirebaseFirestore;

public class StatusUpdateWorker extends ListenableWorker {

    public static final String KEY_DOCUMENT_ID = "documentId";
    public static final String KEY_STATUS = "status";

    private final SettableFuture<Result> future = SettableFuture.create();

    public StatusUpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        String documentId = getInputData().getString(KEY_DOCUMENT_ID);
        String status = getInputData().getString(KEY_STATUS);

        if (documentId == null || status == null) {
            future.set(Result.failure());
            return future;
        }

        int statusStep = -1;
        switch (status.toLowerCase()) {
            case "beérkezett":
                statusStep = 0;
                break;
            case "nyomtatás alatt":
                statusStep = 1;
                break;
            case "kinyomtatva":
                statusStep = 2;
                break;
            default:
                // Ha ismeretlen státusz, nem frissítünk
                future.set(Result.failure());
                return future;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("documents").document(documentId)
                .update("status", status, "statusUpdateStep", statusStep)
                .addOnSuccessListener(aVoid -> {
                    sendNotification(status);
                    future.set(Result.success());
                })
                .addOnFailureListener(e -> {
                    Log.e("StatusUpdateWorker", "Hiba frissítés közben: ", e);
                    future.set(Result.failure());
                });

        return future;
    }

    private void sendNotification(String status) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "document_status_channel")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Dokumentum státusz frissítve")
                .setContentText("Új státusz: " + status)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
