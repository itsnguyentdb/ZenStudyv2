package com.example.zen_study.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.zen_study.R;
import com.example.zen_study.dto.FlashcardStats;
import com.example.zen_study.dto.NotesStats;
import com.example.zen_study.dto.StudyOverview;
import com.example.zen_study.dto.TaskProgress;
import com.example.zen_study.viewmodels.StatsViewModel;
import com.github.lzyzsd.circleprogress.CircleProgress;

import java.util.Locale;

public class StatsOverviewFragment extends Fragment {
    private StatsViewModel viewModel;

    // Study Overview
    private TextView tvTotalTime, tvSessionsCompleted, tvCurrentStreak, tvAverageFocus;

    // Quick Stats
    private CircleProgress circleProgressTasks;
    private TextView tvFlashcardsCount, tvNotesCount;
    private TabNavigationInterface tabNavigation;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupProgressCircle();
        setupViewModel();
        observeData();
        setupClickListeners(view);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof TabNavigationInterface) {
            tabNavigation = (TabNavigationInterface) parentFragment;
        }
    }

    private void initViews(View view) {
        // Study Overview
        tvTotalTime = view.findViewById(R.id.tvTotalTime);
        tvSessionsCompleted = view.findViewById(R.id.tvSessionsCompleted);
        tvCurrentStreak = view.findViewById(R.id.tvCurrentStreak);
        tvAverageFocus = view.findViewById(R.id.tvAverageFocus);

        // Quick Stats
        circleProgressTasks = view.findViewById(R.id.circleProgressTasks);
        tvFlashcardsCount = view.findViewById(R.id.tvFlashcardsCount);
        tvNotesCount = view.findViewById(R.id.tvNotesCount);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(StatsViewModel.class);
    }

    private void observeData() {
        viewModel.getStudyOverview().observe(getViewLifecycleOwner(), this::updateStudyOverview);
        viewModel.getTaskProgress().observe(getViewLifecycleOwner(), this::updateTaskProgress);
        viewModel.getFlashcardStats().observe(getViewLifecycleOwner(), this::updateFlashcardStats);
        viewModel.getNotesStats().observe(getViewLifecycleOwner(), this::updateNotesStats);
    }

    private void setupProgressCircle() {
        circleProgressTasks.setMax(100);
        circleProgressTasks.setProgress(0);
    }

    private void setupClickListeners(View view) {
        // Task progress click
        view.findViewById(R.id.cardTaskProgress).setOnClickListener(v -> {
            if (tabNavigation != null) {
                tabNavigation.switchToTab(3); // Tasks tab
            }
        });

        // Flashcards click
        view.findViewById(R.id.cardFlashcards).setOnClickListener(v -> {
            if (tabNavigation != null) {
                tabNavigation.switchToTab(4); // Insights tab
            }
        });

        // Notes click
        view.findViewById(R.id.cardNotes).setOnClickListener(v -> {
            if (tabNavigation != null) {
                tabNavigation.switchToTab(4); // Insights tab
            }
        });
    }

    private void updateStudyOverview(StudyOverview overview) {
        if (overview == null) return;

        tvTotalTime.setText(overview.getTotalStudyTime());
        tvSessionsCompleted.setText(String.valueOf(overview.getSessionsCompleted()));
        tvCurrentStreak.setText(overview.getCurrentStreak() + " days 🔥");
        tvAverageFocus.setText(overview.getAverageFocus() + "%");
    }

    private void updateTaskProgress(TaskProgress progress) {
        if (progress == null) return;

        circleProgressTasks.setProgress(progress.getCompletionRate());
    }

    private void updateFlashcardStats(FlashcardStats stats) {
        if (stats == null) return;

        tvFlashcardsCount.setText(String.valueOf(stats.getCardsReviewed()));
    }

    private void updateNotesStats(NotesStats stats) {
        if (stats == null) return;

        tvNotesCount.setText(String.valueOf(stats.getNotesCount()));
    }

    // Helper method to format time for display
    private String formatTime(long totalMinutes) {
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        if (hours > 0) {
            return String.format(Locale.getDefault(), "%dh %dm", hours, minutes);
        } else {
            return String.format(Locale.getDefault(), "%dm", minutes);
        }
    }
}