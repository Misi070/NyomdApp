package com.example.nyomdapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nyomdapp.R;
import com.example.nyomdapp.UploadItem;

import java.util.List;

public class UploadAdapter extends RecyclerView.Adapter<UploadAdapter.UploadViewHolder> {
    private List<UploadItem> uploadList;

    public static class UploadViewHolder extends RecyclerView.ViewHolder {
        TextView fileNameView, copyCountView, statusView;

        public UploadViewHolder(View itemView) {
            super(itemView);
            fileNameView = itemView.findViewById(R.id.textFileName);
            copyCountView = itemView.findViewById(R.id.textCopyCount);
            statusView = itemView.findViewById(R.id.textStatus);
        }
    }

    public UploadAdapter(List<UploadItem> uploadList) {
        this.uploadList = uploadList;
    }

    @Override
    public UploadViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.upload_item, parent, false);
        return new UploadViewHolder(v);
    }

    @Override
    public void onBindViewHolder(UploadViewHolder holder, int position) {
        UploadItem current = uploadList.get(position);
        holder.fileNameView.setText(current.getFileName());
        holder.copyCountView.setText("Darabszám: " + current.getCopyCount());
        holder.statusView.setText("Státusz: " + current.getStatus());
    }

    @Override
    public int getItemCount() {
        return uploadList.size();
    }
}
