package com.example.zen_study.adapters;

import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.zen_study.R;
import com.example.zen_study.models.Task;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HomeTaskAdapter extends RecyclerView.Adapter<HomeTaskAdapter.TaskViewHolder> {

    private List<Task> tasks;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public HomeTaskAdapter() {
        // Default constructor
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks != null ? tasks.size() : 0;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private TextView taskTitle, taskDescription, taskPriority, progressText;
        private TextView durationText, taskStatus, taskDeadline;
        private ProgressBar progressBar;
        private View priorityIndicator;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskDescription = itemView.findViewById(R.id.taskDescription);
            taskPriority = itemView.findViewById(R.id.taskPriority);
            progressText = itemView.findViewById(R.id.progressText);
            durationText = itemView.findViewById(R.id.durationText);
            taskStatus = itemView.findViewById(R.id.taskStatus);
            taskDeadline = itemView.findViewById(R.id.taskDeadline);
            progressBar = itemView.findViewById(R.id.progressBar);
            priorityIndicator = itemView.findViewById(R.id.priorityIndicator);
        }

        public void bind(Task task) {
            // Set basic task info
            taskTitle.setText(task.getTitle());
            taskDescription.setText(task.getDescription());

            // Set priority
            String priorityText = "P" + task.getPriority();
            taskPriority.setText(priorityText);

            // Set priority color
            int priorityColor = getPriorityColor(task.getPriority());
            taskPriority.setBackgroundColor(priorityColor);
            priorityIndicator.setBackgroundColor(priorityColor);

            // Set progress
            int progress = (int) (task.getProgress() * 100);
            progressBar.setProgress(progress);
            progressText.setText(progress + "%");

            // Set duration
            String duration = formatDuration(task.getProgressDuration()) + " / " +
                    formatDuration(task.getExpectedDuration());
            durationText.setText(duration);

            // Set status
            taskStatus.setText(task.getStatus().name());
            taskStatus.setBackgroundColor(getStatusColor(task.getStatus()));

            // Set deadline
            if (task.getDeadline() != null) {
                SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                taskDeadline.setText("Deadline: " + format.format(task.getDeadline()));
                taskDeadline.setVisibility(View.VISIBLE);
            } else {
                taskDeadline.setVisibility(View.GONE);
            }
        }

        private int getPriorityColor(int priority) {
            switch (priority) {
                case 1:
                    return Color.parseColor("#FF4444"); // High - Red
                case 2:
                    return Color.parseColor("#FF8800"); // Medium - Orange
                case 3:
                    return Color.parseColor("#FFBB33"); // Low - Yellow
                default:
                    return Color.parseColor("#99CC00"); // Default - Green
            }
        }

        private int getStatusColor(Task.TaskType status) {
            switch (status) {
                case TODO:
                    return Color.parseColor("#FFA726");
                case IN_PROGRESS:
                    return Color.parseColor("#29B6F6");
                case COMPLETED:
                    return Color.parseColor("#66BB6A");
                case OVERDUE:
                    return Color.parseColor("#EF5350");
                default:
                    return Color.parseColor("#BDBDBD");
            }
        }

        private String formatDuration(int minutes) {
            if (minutes < 60) {
                return minutes + "m";
            } else {
                int hours = minutes / 60;
                int mins = minutes % 60;
                return mins > 0 ? hours + "h " + mins + "m" : hours + "h";
            }
        }
    }
}
