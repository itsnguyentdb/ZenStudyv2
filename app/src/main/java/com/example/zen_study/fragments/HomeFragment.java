package com.example.zen_study.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zen_study.adapters.HomeResourceAdapter;
import com.example.zen_study.models.Resource;
import com.example.zen_study.models.Task;
import com.example.zen_study.viewmodels.ResourceLibraryViewModel;
import com.example.zen_study.viewmodels.TaskLibraryViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.example.zen_study.R;
import com.example.zen_study.adapters.HomeTaskAdapter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment {

    private RecyclerView todayTasksRecyclerView;
    private RecyclerView recentResourcesRecyclerView;
    private HomeTaskAdapter taskAdapter;
    private HomeResourceAdapter resourceAdapter;
    private TaskLibraryViewModel taskLibraryViewModel;
    private ResourceLibraryViewModel resourceLibraryViewModel;
    private BarChart studyProgressChart;
    private TextView emptyTasksText, emptyResourcesText, progressSummaryText, welcomeText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModels
        taskLibraryViewModel = new ViewModelProvider(this).get(TaskLibraryViewModel.class);
        resourceLibraryViewModel = new ViewModelProvider(this).get(ResourceLibraryViewModel.class);

        initializeViews(view);
        setupRecyclerViews();
        setupObservers();
        updateWelcomeMessage();
    }

    private void initializeViews(View view) {
        todayTasksRecyclerView = view.findViewById(R.id.todayTasksRecyclerView);
        recentResourcesRecyclerView = view.findViewById(R.id.recentResourcesRecyclerView);
        studyProgressChart = view.findViewById(R.id.studyProgressChart);
        emptyTasksText = view.findViewById(R.id.emptyTasksText);
        emptyResourcesText = view.findViewById(R.id.emptyResourcesText);
        progressSummaryText = view.findViewById(R.id.progressSummaryText);
        welcomeText = view.findViewById(R.id.welcomeText);
    }

    private void setupRecyclerViews() {
        // Setup tasks recycler view
        todayTasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        taskAdapter = new HomeTaskAdapter();
        todayTasksRecyclerView.setAdapter(taskAdapter);

        // Setup resources recycler view
        recentResourcesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        resourceAdapter = new HomeResourceAdapter();
        resourceAdapter.setOnResourceClickListener(new HomeResourceAdapter.OnResourceClickListener() {
            @Override
            public void onResourceClick(Resource resource) {
                // openResource(resource);
            }

            @Override
            public void onResourceActionClick(Resource resource) {
                // showResourceOptions(resource);
            }
        });
        recentResourcesRecyclerView.setAdapter(resourceAdapter);
    }

    private void setupObservers() {
        // Observe today's tasks
        taskLibraryViewModel.getTodayTasks().observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null && !tasks.isEmpty()) {
                emptyTasksText.setVisibility(View.GONE);
                todayTasksRecyclerView.setVisibility(View.VISIBLE);
                taskAdapter.setTasks(tasks);
                loadStudyProgress(tasks); // Pass tasks for progress calculation
            } else {
                emptyTasksText.setVisibility(View.VISIBLE);
                todayTasksRecyclerView.setVisibility(View.GONE);
                taskAdapter.setTasks(new ArrayList<>());
            }
        });

        // Observe all resources and take only recent ones
        resourceLibraryViewModel.getAllResources().observe(getViewLifecycleOwner(), resources -> {
            if (resources != null) {
                List<Resource> recentResources = getRecentResources(resources);
                if (!recentResources.isEmpty()) {
                    emptyResourcesText.setVisibility(View.GONE);
                    recentResourcesRecyclerView.setVisibility(View.VISIBLE);
                    resourceAdapter.setResources(recentResources);
                } else {
                    emptyResourcesText.setVisibility(View.VISIBLE);
                    recentResourcesRecyclerView.setVisibility(View.GONE);
                    resourceAdapter.setResources(new ArrayList<>());
                }
            }
        });

        // You can also observe all tasks if needed for the chart
        taskLibraryViewModel.getAllTasks().observe(getViewLifecycleOwner(), allTasks -> {
            if (allTasks != null) {
                List<Task> incomingTasks = getIncomingTasks(allTasks);
                if (!incomingTasks.isEmpty()) {
                    emptyTasksText.setVisibility(View.GONE);
                    todayTasksRecyclerView.setVisibility(View.VISIBLE);
                    taskAdapter.setTasks(incomingTasks);
                } else {
                    emptyTasksText.setVisibility(View.VISIBLE);
                    todayTasksRecyclerView.setVisibility(View.GONE);
                    taskAdapter.setTasks(new ArrayList<>());
                }
            }
        });
    }

    private void loadStudyProgress(List<Task> tasks) {
        if (tasks != null && !tasks.isEmpty()) {
            setupProgressChart(tasks);

            // Calculate total study time
            int totalStudyTime = 0;
            int completedTasks = 0;

            for (Task task : tasks) {
                totalStudyTime += task.getProgressDuration();
                if (task.getStatus() == Task.TaskType.COMPLETED) {
                    completedTasks++;
                }
            }

            String summary = String.format("Studied %s this week • %d tasks completed",
                    formatDuration(totalStudyTime), completedTasks);
            progressSummaryText.setText(summary);
        } else {
            progressSummaryText.setText("No study activity yet");
            setupEmptyChart();
        }
    }

    private void setupProgressChart(List<Task> tasks) {
        // Calculate study hours per day (this is simplified - you might want actual time data)
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < Math.min(tasks.size(), 7); i++) {
            Task task = tasks.get(i);
            float studyHours = task.getProgressDuration() / 60.0f; // Convert minutes to hours
            entries.add(new BarEntry(i, studyHours));
        }

        // If no entries, create some sample data

        BarDataSet dataSet = new BarDataSet(entries, "Study Hours");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        studyProgressChart.setData(barData);
        studyProgressChart.getDescription().setEnabled(false);
        studyProgressChart.getXAxis().setEnabled(false);
        studyProgressChart.getAxisLeft().setEnabled(false);
        studyProgressChart.getAxisRight().setEnabled(false);
        studyProgressChart.getLegend().setEnabled(false);
        studyProgressChart.animateY(1000);
        studyProgressChart.invalidate();
    }

    private void setupEmptyChart() {
        // Setup empty chart state
        studyProgressChart.clear();
        studyProgressChart.setNoDataText("No study data available");
        studyProgressChart.invalidate();
    }

    private List<Resource> getRecentResources(List<Resource> allResources) {
        // Sort by updated date and return only recent ones
        List<Resource> sorted = new ArrayList<>(allResources);
        sorted.sort((r1, r2) -> {
            Date date1 = r2.getUpdatedAt() != null ? r2.getUpdatedAt() : r2.getCreatedAt();
            Date date2 = r1.getUpdatedAt() != null ? r1.getUpdatedAt() : r1.getCreatedAt();

            if (date1 == null && date2 == null) return 0;
            if (date1 == null) return -1;
            if (date2 == null) return 1;

            return date2.compareTo(date1);
        });

        return sorted.subList(0, Math.min(sorted.size(), 5));
    }

    private List<Task> getIncomingTasks(List<Task> allTasks) {
        // Sort by updated date and return only recent ones
        List<Task> sorted = new ArrayList<>(allTasks);
        sorted.sort((r1, r2) -> {
            Date date1 = r2.getDeadline() != null ? r2.getDeadline() : r2.getCreatedAt();
            Date date2 = r1.getDeadline() != null ? r1.getDeadline() : r1.getCreatedAt();

            if (date1 == null && date2 == null) return 0;
            if (date1 == null) return -1;
            if (date2 == null) return 1;

            return date2.compareTo(date1);
        });

        return sorted.subList(0, Math.min(sorted.size(), 3));
    }

    private void updateWelcomeMessage() {
        // You can make this more dynamic based on time of day
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (hour < 12) {
            greeting = "Good morning!";
        } else if (hour < 18) {
            greeting = "Good afternoon!";
        } else {
            greeting = "Good evening!";
        }

        welcomeText.setText(greeting);
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