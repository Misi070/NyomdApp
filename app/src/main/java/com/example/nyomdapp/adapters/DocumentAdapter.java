package com.example.nyomdapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nyomdapp.R;

import java.util.List;


public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {

    private List<Document> documentList;

    public DocumentAdapter(List<Document> documentList) {
        this.documentList = documentList;
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.document_item, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DocumentViewHolder holder, int position) {
        Document document = documentList.get(position);
        holder.fileUrlTextView.setText("File: " + document.getFileUrl());
        holder.copiesTextView.setText("Copies: " + document.getCopies());
        holder.statusTextView.setText("Status: " + document.getStatus());
    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }

    public static class DocumentViewHolder extends RecyclerView.ViewHolder {

        TextView fileUrlTextView;
        TextView copiesTextView;
        TextView statusTextView;

        public DocumentViewHolder(View itemView) {
            super(itemView);
            fileUrlTextView = itemView.findViewById(R.id.fileUrlTextView);
            copiesTextView = itemView.findViewById(R.id.copiesTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
        }
    }
}
