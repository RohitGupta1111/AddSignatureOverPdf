package com.example.addsignatureoverpdf;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.FileViewHolder> {

    ArrayList<FileDetails> fileDetailsList;
    SimpleDateFormat dateFormat;
    public FileListAdapter(ArrayList<FileDetails> fileDetailsList) {
        this.fileDetailsList = fileDetailsList;
        dateFormat =  new SimpleDateFormat("dd/MM/yyyy");
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View fileDetailView = inflater.inflate(R.layout.file_detail_view,parent,false);
        FileViewHolder viewHolder = new FileViewHolder(fileDetailView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileDetails fileDetails = fileDetailsList.get(position);
        if(fileDetails.getFileName() != null) {
            holder.file_name_view.setText(fileDetails.getFileName());
        }

        if(fileDetails.getFileModifiedDate() != null) {
            holder.date_modified_view.setText(dateFormat.format(fileDetails.getFileModifiedDate()));
        }

        if(fileDetails.getFileBitmap() != null) {
            holder.file_image_preview.setImageBitmap(fileDetails.getFileBitmap());
        }
    }

    @Override
    public int getItemCount() {
        return fileDetailsList.size();
    }

    public class FileViewHolder extends RecyclerView.ViewHolder {
        TextView file_name_view;
        TextView date_modified_view;
        ImageView file_image_preview;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            file_name_view = (TextView) itemView.findViewById(R.id.file_name);
            date_modified_view = (TextView) itemView.findViewById(R.id.file_modify_date);
            file_image_preview = (ImageView) itemView.findViewById(R.id.pdf_preview);
        }
    }
}
