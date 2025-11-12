package com.example.zen_study.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.zen_study.R;
import com.example.zen_study.dto.FlashcardStats;
import com.example.zen_study.dto.NotesStats;
import com.example.zen_study.dto.QuizStats;
import com.example.zen_study.viewmodels.StatsViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class StatsInsightsFragment extends Fragment {
    private StatsViewModel viewModel;

    // Flashcard views
    private TextView tvDecksReviewed, tvCardsReviewed, tvFlashcardAccuracy;

    // Quiz views
    private TextView tvAverageScore, tvFastestAttempt, tvHighestScoreDeck;

    // Notes & Resources views
//    private TextView tvNotesCreated, tvResourcesAdded, tvMostLinkedSubject, tvLastUpdatedNote;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats_insights, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupViewModel();
        observeData();
        setupClickListeners(view);
    }

    private void initViews(View view) {
        // Flashcard views
        tvDecksReviewed = view.findViewById(R.id.tvDecksReviewed);
        tvCardsReviewed = view.findViewById(R.id.tvCardsReviewed);
        tvFlashcardAccuracy = view.findViewById(R.id.tvFlashcardAccuracy);

        // Quiz views
        tvAverageScore = view.findViewById(R.id.tvAverageScore);
        tvFastestAttempt = view.findViewById(R.id.tvFastestAttempt);
        tvHighestScoreDeck = view.findViewById(R.id.tvHighestScoreDeck);

        // Notes & Resources views
//        tvNotesCreated = view.findViewById(R.id.tvNotesCreated);
//        tvResourcesAdded = view.findViewById(R.id.tvResourcesAdded);
//        tvMostLinkedSubject = view.findViewById(R.id.tvMostLinkedSubject);
//        tvLastUpdatedNote = view.findViewById(R.id.tvLastUpdatedNote);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(StatsViewModel.class);
    }

    private void observeData() {
        viewModel.getFlashcardStats().observe(getViewLifecycleOwner(), this::updateFlashcardStats);
        viewModel.getQuizStats().observe(getViewLifecycleOwner(), this::updateQuizStats);
//        viewModel.getNotesStats().observe(getViewLifecycleOwner(), this::updateNotesStats);
    }

    private void setupClickListeners(View view) {
        view.findViewById(R.id.cardFlashcards).setOnClickListener(v -> showFlashcardDetails());
        view.findViewById(R.id.cardQuizzes).setOnClickListener(v -> showQuizDetails());
//        view.findViewById(R.id.cardNotes).setOnClickListener(v -> showNotesDetails());
    }

    private void updateFlashcardStats(FlashcardStats stats) {
        if (stats == null) return;

        tvDecksReviewed.setText(String.valueOf(stats.getDecksReviewed()));
        tvCardsReviewed.setText(String.valueOf(stats.getCardsReviewed()));

        String accuracyText = stats.getAccuracy() + "%";
//        if (stats.getAccuracyChange() > 0) {
//            accuracyText += " ↗";
//            tvFlashcardAccuracy.setTextColor(ContextCompat.getColor(requireContext(), R.color.success));
//        } else if (stats.getAccuracyChange() < 0) {
//            accuracyText += " ↘";
//            tvFlashcardAccuracy.setTextColor(ContextCompat.getColor(requireContext(), R.color.error));
//        } else {
//            tvFlashcardAccuracy.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondaryText));
//        }
        tvFlashcardAccuracy.setText(accuracyText);
    }

    private void updateQuizStats(QuizStats stats) {
        if (stats == null) return;

        tvAverageScore.setText("Average Score: " + stats.getAverageScore() + "%");
        tvFastestAttempt.setText("Fastest Attempt: " + stats.getFastestAttempt());
        tvHighestScoreDeck.setText("Best Deck: " + stats.getHighestScoreDeck());
    }

//    private void updateNotesStats(NotesStats stats) {
//        if (stats == null) return;
//
//        tvNotesCreated.setText("Notes: " + stats.getNotesCount());
//        tvResourcesAdded.setText("Resources: " + stats.getResourcesCount());
//        tvMostLinkedSubject.setText("Most Linked: " + stats.getMostLinkedSubject());
//
//        String lastUpdated = stats.getLastUpdatedNote();
//        if (lastUpdated.length() > 15) {
//            lastUpdated = lastUpdated.substring(0, 15) + "...";
//        }
//        tvLastUpdatedNote.setText("Last: " + lastUpdated);
//    }

    private void showFlashcardDetails() {
        FlashcardStats stats = viewModel.getFlashcardStats().getValue();
        if (stats == null) return;

        StringBuilder message = new StringBuilder();
        message.append("Decks Reviewed: ").append(stats.getDecksReviewed()).append("\n");
        message.append("Total Cards: ").append(stats.getCardsReviewed()).append("\n");
        message.append("Accuracy: ").append(stats.getAccuracy()).append("%\n");

        if (stats.getAccuracyChange() != 0) {
            String change = stats.getAccuracyChange() > 0 ? "improved" : "declined";
            message.append("Accuracy has ").append(change).append(" by ")
                    .append(Math.abs(stats.getAccuracyChange())).append("%\n");
        }

        message.append("\nMost Studied Deck: ").append(stats.getMostStudiedDeck());

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Flashcard Details")
                .setMessage(message.toString())
                .setPositiveButton("OK", null)
                .setNeutralButton("Review Flashcards", (dialog, which) -> {
                    navigateToFlashcards();
                })
                .show();
    }

    private void showQuizDetails() {
        QuizStats stats = viewModel.getQuizStats().getValue();
        if (stats == null) return;

        StringBuilder message = new StringBuilder();
        message.append("Quizzes Taken: ").append(stats.getQuizzesTaken()).append("\n");
        message.append("Average Score: ").append(stats.getAverageScore()).append("%\n");
        message.append("Fastest Completion: ").append(stats.getFastestAttempt()).append("\n");
        message.append("Highest Score: ").append(stats.getHighestScore()).append("%\n");
        message.append("Best Performing Deck: ").append(stats.getHighestScoreDeck());

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Quiz Performance Details")
                .setMessage(message.toString())
                .setPositiveButton("OK", null)
                .setNeutralButton("Take Quiz", (dialog, which) -> {
                    navigateToQuizzes();
                })
                .show();
    }

//    private void showNotesDetails() {
//        NotesStats stats = viewModel.getNotesStats().getValue();
//        if (stats == null) return;
//
//        StringBuilder message = new StringBuilder();
//        message.append("Total Notes: ").append(stats.getNotesCount()).append("\n");
//        message.append("Total Resources: ").append(stats.getResourcesCount()).append("\n");
//        message.append("Most Active Subject: ").append(stats.getMostLinkedSubject()).append("\n");
//        message.append("Last Updated: ").append(stats.getLastUpdatedNote()).append("\n");
//        message.append("Notes This Week: ").append(stats.getNotesThisWeek());
//
//        new MaterialAlertDialogBuilder(requireContext())
//                .setTitle("Notes & Resources Summary")
//                .setMessage(message.toString())
//                .setPositiveButton("OK", null)
//                .setNeutralButton("Create Note", (dialog, which) -> {
//                    navigateToNotes();
//                })
//                .show();
//    }

    private void navigateToFlashcards() {
        // Implementation to navigate to flashcards section
        Toast.makeText(requireContext(), "Navigating to Flashcards", Toast.LENGTH_SHORT).show();
    }

    private void navigateToQuizzes() {
        // Implementation to navigate to quizzes section
        Toast.makeText(requireContext(), "Navigating to Quizzes", Toast.LENGTH_SHORT).show();
    }

    private void navigateToNotes() {
        // Implementation to navigate to notes section
        Toast.makeText(requireContext(), "Navigating to Notes", Toast.LENGTH_SHORT).show();
    }
}
