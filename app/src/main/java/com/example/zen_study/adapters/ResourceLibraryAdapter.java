package com.example.zen_study.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zen_study.R;
import com.example.zen_study.models.Resource;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ResourceLibraryAdapter extends RecyclerView.Adapter<ResourceLibraryAdapter.ResourceViewHolder> {
    private List<Resource> resources;
    private final OnResourceClickListener listener;

    public interface OnResourceClickListener {
        void onResourceClick(Resource resource);

        void onResourceEditClick(Resource resource);

        void onResourceDeleteClick(Resource resource);

        void onResourceInfoClick(Resource resource);
    }

    public ResourceLibraryAdapter(List<Resource> resources, OnResourceClickListener listener) {
        this.resources = resources;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ResourceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_resource_card, parent, false);
        return new ResourceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResourceViewHolder holder, int position) {
        Resource resource = resources.get(position);
        holder.bind(resource, listener);
    }

    @Override
    public int getItemCount() {
        return resources.size();
    }

    public void updateResources(List<Resource> newResources) {
        this.resources = newResources;
        notifyDataSetChanged();
    }

    static class ResourceViewHolder extends RecyclerView.ViewHolder {
        private final TextView textTitle;
        private final TextView textType;
        private final TextView textDate;
        private final ImageView iconType;
        private final ImageView buttonDelete;
        private final ImageView buttonEdit;

        public ResourceViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textType = itemView.findViewById(R.id.textType);
            textDate = itemView.findViewById(R.id.textDate);
            iconType = itemView.findViewById(R.id.iconType);
            buttonDelete = itemView.findViewById(R.id.resource_library_button_delete);
            buttonEdit = itemView.findViewById(R.id.resource_library_button_edit);
        }

        public void bind(Resource resource, OnResourceClickListener listener) {
            textTitle.setText(resource.getTitle());
            textType.setText(resource.getType());

            // Format date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            if (resource.getCreatedAt() != null) {
                textDate.setText(dateFormat.format(resource.getCreatedAt()));
            }

            // Set icon based on file type
            setFileTypeIcon(resource.getType());

            // Click listeners
            itemView.setOnClickListener(v -> listener.onResourceClick(resource));
            buttonDelete.setOnClickListener(v -> listener.onResourceDeleteClick(resource));
            buttonEdit.setOnClickListener(v -> listener.onResourceEditClick(resource));
        }

        private void setFileTypeIcon(String fileType) {
            int iconRes;
            switch (fileType != null ? fileType.toUpperCase() : "FILE") {
                case "IMAGE":
                    iconRes = R.drawable.ic_image;
                    break;
                case "VIDEO":
                    iconRes = R.drawable.ic_video;
                    break;
                case "AUDIO":
                    iconRes = R.drawable.ic_audio;
                    break;
                case "PDF":
                    iconRes = R.drawable.ic_pdf;
                    break;
                case "TEXT":
                    iconRes = R.drawable.ic_text;
                    break;
                default:
                    iconRes = R.drawable.ic_file;
                    break;
            }
            iconType.setImageResource(iconRes);
        }
    }
}
