package com.example.nyomdapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nyomdapp.R;
import com.example.nyomdapp.model.UploadEntry;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;

public class UploadEntryAdapter extends RecyclerView.Adapter<UploadEntryAdapter.ViewHolder> {

    private Context context;
    private List<UploadEntry> documentList;

    public UploadEntryAdapter(Context context, List<UploadEntry> documentList) {
        this.context = context;
        this.documentList = documentList;
    }

    @NonNull
    @Override
    public UploadEntryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_document, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UploadEntryAdapter.ViewHolder holder, int position) {
        UploadEntry document = documentList.get(position);
        Context ctx = holder.itemView.getContext();

        holder.fileNameTextView.setText(document.getFileName());
        holder.copyCountTextView.setText(ctx.getString(R.string.copies, document.getCopies()));
        holder.statusTextView.setText(ctx.getString(R.string.status, document.getStatus()));
        holder.sizeTextView.setText(ctx.getString(R.string.size, document.getSize()));

        String formattedDate = "N/A";
        if (document.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy. MMMM d., HH:mm", java.util.Locale.getDefault());
            formattedDate = sdf.format(document.getTimestamp().toDate());
        }
        holder.uploadDateTextView.setText(ctx.getString(R.string.upload_date, formattedDate));

        if ("Beérkezett".equalsIgnoreCase(document.getStatus())) {
            holder.deleteButton.setVisibility(View.VISIBLE);
        } else {
            holder.deleteButton.setVisibility(View.GONE);
        }

        holder.deleteButton.setOnClickListener(v -> {
            Log.d("UploadEntryAdapter", "Delete button clicked for document ID: " + document.getDocumentId());
            new AlertDialog.Builder(ctx)
                    .setTitle(R.string.delete_document_title)
                    .setMessage(R.string.delete_document_message)
                    .setPositiveButton(R.string.yes, (dialog, which) -> deleteDocument(document.getDocumentId()))
                    .setNegativeButton(R.string.no, null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }

    private void deleteDocument(String documentId) {
        if (documentId == null || documentId.isEmpty()) {
            Log.e("DeleteDocument", "Document ID is null or empty. Cannot delete.");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d("DeleteDocument", "Attempting to delete document ID: " + documentId);

        db.collection("documents").document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("DeleteDocument", "Successfully deleted document ID: " + documentId);
                    for (int i = 0; i < documentList.size(); i++) {
                        if (documentList.get(i).getDocumentId().equals(documentId)) {
                            documentList.remove(i);
                            notifyItemRemoved(i);
                            notifyDataSetChanged(); // biztos frissítés minden esetre
                            Log.d("DeleteDocument", "Document removed from list and UI refreshed.");
                            break;
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DeleteDocument", "Error deleting document", e);
                });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView fileNameTextView;
        TextView copyCountTextView;
        TextView statusTextView;
        TextView uploadDateTextView;
        TextView sizeTextView;
        Button deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            fileNameTextView = itemView.findViewById(R.id.fileNameTextView);
            copyCountTextView = itemView.findViewById(R.id.copyCountTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            uploadDateTextView = itemView.findViewById(R.id.uploadDateTextView);
            sizeTextView = itemView.findViewById(R.id.sizeTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
