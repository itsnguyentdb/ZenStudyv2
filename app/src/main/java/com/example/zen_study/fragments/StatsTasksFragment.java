package com.example.zen_study.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.zen_study.R;
import com.example.zen_study.dto.TaskProgress;
import com.example.zen_study.models.Task;
import com.example.zen_study.viewmodels.StatsViewModel;
import com.github.lzyzsd.circleprogress.CircleProgress;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatsTasksFragment extends Fragment {

    private StatsViewModel viewModel;
    private CircleProgress circleProgressTasks;
    private TextView tvCompletionRate, tvTasksCompleted, tvUpcomingTasks;
    private ListView listViewUpcomingTasks;
    private TaskAdapter taskAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats_tasks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupProgressCircle();
        setupTaskList();
        setupViewModel();
        observeData();
        setupClickListeners(view);
    }

    private void initViews(View view) {
        circleProgressTasks = view.findViewById(R.id.circleProgressTasks);
        tvCompletionRate = view.findViewById(R.id.tvCompletionRate);
        tvTasksCompleted = view.findViewById(R.id.tvTasksCompleted);
        tvUpcomingTasks = view.findViewById(R.id.tvUpcomingTasks);
        listViewUpcomingTasks = view.findViewById(R.id.listViewUpcomingTasks);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(StatsViewModel.class);
    }

    private void observeData() {
        viewModel.getTaskProgress().observe(getViewLifecycleOwner(), this::updateTaskProgress);
        viewModel.getUpcomingTasks().observe(getViewLifecycleOwner(), this::updateUpcomingTasks);
    }

    private void setupProgressCircle() {
        circleProgressTasks.setMax(100);
        circleProgressTasks.setProgress(0);
    }

    private void setupTaskList() {
        taskAdapter = new TaskAdapter(requireContext(), new ArrayList<>());
        listViewUpcomingTasks.setAdapter(taskAdapter);

        listViewUpcomingTasks.setOnItemClickListener((parent, view, position, id) -> {
            Task task = taskAdapter.getItem(position);
            if (task != null) {
                showTaskDetails(task);
            }
        });
    }

    private void setupClickListeners(View view) {
        view.findViewById(R.id.cardTaskProgress).setOnClickListener(v -> {
            // Could navigate to tasks screen
            Toast.makeText(requireContext(), "Navigate to Tasks", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateTaskProgress(TaskProgress progress) {
        if (progress == null) return;

        circleProgressTasks.setProgress(progress.getCompletionRate());
        tvCompletionRate.setText(progress.getCompletionRate() + "% completion rate");
        tvTasksCompleted.setText(progress.getCompletedTasks() + "/" + progress.getTotalTasks() + " tasks completed");
    }

    private void updateUpcomingTasks(List<Task> upcomingTasks) {
        if (upcomingTasks == null || upcomingTasks.isEmpty()) {
            tvUpcomingTasks.setText("No upcoming tasks");
            taskAdapter.updateData(new ArrayList<>());
            return;
        }

        tvUpcomingTasks.setText(upcomingTasks.size() + " tasks due soon");
        taskAdapter.updateData(upcomingTasks);
    }

    private void showTaskDetails(Task task) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String dueDate = task.getDeadline() != null ? sdf.format(task.getDeadline()) : "No due date";

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(task.getTitle())
                .setMessage(String.format(Locale.getDefault(),
                        "Due: %s\nPriority: %s\nStatus: %s",
                        dueDate, getPriorityText(task.getPriority()), task.getStatus().toString()))
                .setPositiveButton("Mark Complete", (dialog, which) -> {
                    // markTaskComplete(task.getId());
                    Toast.makeText(requireContext(), "Marking task as complete", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Later", null)
                .show();
    }

    private String getPriorityText(int priority) {
        switch (priority) {
            case 1: return "Low";
            case 2: return "Medium";
            case 3: return "High";
            default: return "Normal";
        }
    }

    // TaskAdapter class
    private static class TaskAdapter extends ArrayAdapter<Task> {

        public TaskAdapter(@NonNull Context context, @NonNull List<Task> tasks) {
            super(context, R.layout.item_stats_task, tasks);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_stats_task, parent, false);
            }

            Task task = getItem(position);

            TextView tvTaskTitle = convertView.findViewById(R.id.tvTaskTitle);
            TextView tvDueDate = convertView.findViewById(R.id.tvDueDate);
            TextView tvSubject = convertView.findViewById(R.id.tvSubject);
            View priorityIndicator = convertView.findViewById(R.id.priorityIndicator);

            if (task != null) {
                tvTaskTitle.setText(task.getTitle());
                tvDueDate.setText(formatDueDate(task.getDeadline()));
                tvSubject.setText(getSubjectName(task.getSubjectId()));

                // Set priority color
                int priorityColor = getPriorityColor(task.getPriority());
                priorityIndicator.setBackgroundColor(priorityColor);
            }

            return convertView;
        }

        private String formatDueDate(Date dueDate) {
            if (dueDate == null) return "No due date";

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
            return "Due: " + sdf.format(dueDate);
        }

        private String getSubjectName(long subjectId) {
            // In a real app, you'd get this from your repository
            return "Subject " + subjectId;
        }

        private int getPriorityColor(int priority) {
            switch (priority) {
                case 1: return Color.GREEN;
                case 2: return Color.YELLOW;
                case 3: return Color.RED;
                default: return Color.GRAY;
            }
        }

        public void updateData(List<Task> newData) {
            clear();
            addAll(newData);
            notifyDataSetChanged();
        }
    }
}
