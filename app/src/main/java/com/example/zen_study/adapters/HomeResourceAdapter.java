package com.example.zen_study.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zen_study.R;
import com.example.zen_study.models.Resource;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HomeResourceAdapter extends RecyclerView.Adapter<HomeResourceAdapter.ResourceViewHolder> {

    private List<Resource> resources;
    private OnResourceClickListener listener;

    public interface OnResourceClickListener {
        void onResourceClick(Resource resource);

        void onResourceActionClick(Resource resource);
    }

    public HomeResourceAdapter() {
        // Default constructor
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
        notifyDataSetChanged();
    }

    public void setOnResourceClickListener(OnResourceClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ResourceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_resource, parent, false);
        return new ResourceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResourceViewHolder holder, int position) {
        Resource resource = resources.get(position);
        holder.bind(resource);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onResourceClick(resource);
            }
        });

        holder.resourceAction.setOnClickListener(v -> {
            if (listener != null) {
                listener.onResourceActionClick(resource);
            }
        });
    }

    @Override
    public int getItemCount() {
        return resources != null ? resources.size() : 0;
    }

    static class ResourceViewHolder extends RecyclerView.ViewHolder {
        private TextView resourceTitle, resourceType, resourceUpdated, resourceIcon;
        private ImageButton resourceAction;

        public ResourceViewHolder(@NonNull View itemView) {
            super(itemView);
            resourceTitle = itemView.findViewById(R.id.resourceTitle);
            resourceType = itemView.findViewById(R.id.resourceType);
            resourceUpdated = itemView.findViewById(R.id.resourceUpdated);
            resourceIcon = itemView.findViewById(R.id.resourceIcon);
            resourceAction = itemView.findViewById(R.id.resourceAction);
        }

        public void bind(Resource resource) {
            resourceTitle.setText(resource.getTitle());
            resourceType.setText(resource.getType());

            // Set update time
            if (resource.getUpdatedAt() != null) {
                resourceUpdated.setText(getTimeAgo(resource.getUpdatedAt()));
            } else if (resource.getCreatedAt() != null) {
                resourceUpdated.setText(getTimeAgo(resource.getCreatedAt()));
            } else {
                resourceUpdated.setText("Unknown time");
            }

            // Set icon based on file type
            resourceIcon.setText(getFileTypeIcon(resource.getType()));
        }

        private String getTimeAgo(Date date) {
            long now = System.currentTimeMillis();
            long time = date.getTime();
            long diff = now - time;

            if (diff < TimeUnit.MINUTES.toMillis(1)) {
                return "Just now";
            } else if (diff < TimeUnit.HOURS.toMillis(1)) {
                long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
                return minutes + " minutes ago";
            } else if (diff < TimeUnit.DAYS.toMillis(1)) {
                long hours = TimeUnit.MILLISECONDS.toHours(diff);
                return hours + " hours ago";
            } else if (diff < TimeUnit.DAYS.toMillis(7)) {
                long days = TimeUnit.MILLISECONDS.toDays(diff);
                return days + " days ago";
            } else {
                SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                return format.format(date);
            }
        }

        private String getFileTypeIcon(String type) {
            if (type == null) return "📄";

            switch (type.toLowerCase()) {
                case "pdf":
                    return "📕";
                case "doc":
                case "docx":
                    return "📘";
                case "ppt":
                case "pptx":
                    return "📊";
                case "xls":
                case "xlsx":
                    return "📈";
                case "image":
                case "jpg":
                case "png":
                    return "🖼️";
                case "video":
                case "mp4":
                    return "🎬";
                case "audio":
                case "mp3":
                    return "🎵";
                case "link":
                case "url":
                    return "🔗";
                default:
                    return "📄";
            }
        }
    }
}
