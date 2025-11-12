package com.example.zen_study.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zen_study.R;
import com.example.zen_study.models.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TaskLibraryAdapter extends RecyclerView.Adapter<TaskLibraryAdapter.TaskViewHolder> {
    private List<Task> tasks;
    private final OnTaskClickListener listener;

    public TaskLibraryAdapter(List<Task> tasks, OnTaskClickListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_card, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task, listener);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void updateTasks(List<Task> newTasks) {
        Log.d("TaskAdapter", "Updating tasks, count: " + newTasks.size());
        this.tasks = newTasks;
        notifyDataSetChanged();
        Log.d("TaskAdapter", "Adapter item count after update: " + getItemCount());
    }
    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView descriptionTextView;
        private final TextView deadlineTextView;
        private final TextView progressTextView;
        private final Chip priorityChip;
        private final LinearProgressIndicator progressBar;
        private final ImageButton editButton;
        private final ImageButton deleteButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.text_task_title);
            descriptionTextView = itemView.findViewById(R.id.text_task_description);
            deadlineTextView = itemView.findViewById(R.id.text_task_deadline);
            progressTextView = itemView.findViewById(R.id.text_task_progress);
            priorityChip = itemView.findViewById(R.id.chip_priority);
            progressBar = itemView.findViewById(R.id.progress_bar);
            editButton = itemView.findViewById(R.id.button_edit);
            deleteButton = itemView.findViewById(R.id.button_delete);
        }

        public void bind(Task task, OnTaskClickListener listener) {
            // Set basic task info
            titleTextView.setText(task.getTitle());
            descriptionTextView.setText(task.getDescription());

            // Format and set deadline
            if (task.getDeadline() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
                deadlineTextView.setText(sdf.format(task.getDeadline()));
            } else {
                deadlineTextView.setText("No deadline");
            }

            // Set progress
            int progress = (int) (task.getProgress() * 100);
            progressBar.setProgress(progress);
            progressTextView.setText(progress + "%");

            // Set priority chip
            setupPriorityChip(task.getPriority());

            // Set status-based styling
            setupStatusStyling(task.getStatus());

            // Click listeners
            itemView.setOnClickListener(v -> listener.onTaskClick(task));
            editButton.setOnClickListener(v -> listener.onTaskEdit(task));
            deleteButton.setOnClickListener(v -> listener.onTaskDelete(task));

            // Long press for quick status change
            itemView.setOnLongClickListener(v -> {
                showStatusChangeDialog(task, listener);
                return true;
            });
        }

        private void setupPriorityChip(int priority) {
            switch (priority) {
                case 1:
                    priorityChip.setText("Low");
                    priorityChip.setChipBackgroundColorResource(R.color.priority_low);
                    break;
                case 2:
                    priorityChip.setText("Medium");
                    priorityChip.setChipBackgroundColorResource(R.color.priority_medium);
                    break;
                case 3:
                    priorityChip.setText("High");
                    priorityChip.setChipBackgroundColorResource(R.color.priority_high);
                    break;
                default:
                    priorityChip.setText("Normal");
                    priorityChip.setChipBackgroundColorResource(R.color.priority_normal);
            }
        }

        private void setupStatusStyling(Task.TaskType status) {
            switch (status) {
                case COMPLETED:
                    progressBar.setIndicatorColor(itemView.getContext().getColor(R.color.status_completed));
                    break;
                case IN_PROGRESS:
                    progressBar.setIndicatorColor(itemView.getContext().getColor(R.color.status_in_progress));
                    break;
                case OVERDUE:
                    progressBar.setIndicatorColor(itemView.getContext().getColor(R.color.status_overdue));
                    break;
                default:
                    progressBar.setIndicatorColor(itemView.getContext().getColor(R.color.status_todo));
            }
        }

        private void showStatusChangeDialog(Task task, OnTaskClickListener listener) {
            // Implement quick status change dialog
            // You can use BottomSheetDialog or AlertDialog
        }
    }

    public interface OnTaskClickListener {
        void onTaskClick(Task task);

        void onTaskEdit(Task task);

        void onTaskDelete(Task task);

        void onTaskStatusChange(Task task, Task.TaskType newStatus);
    }
}
