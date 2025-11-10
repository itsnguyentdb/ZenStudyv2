package com.example.quiz_clone.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.quiz_clone.R;
import com.example.quiz_clone.dto.QuizAttemptDetails;
import com.example.quiz_clone.viewmodels.QuizAttemptResultViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class QuizAttemptResultActivity extends AppCompatActivity {
    public static String EXTRA_ATTEMPT_ID = "attempt_d";
    private QuizAttemptResultViewModel viewModel;
    private TextView textViewScore, textViewScoreDetails, textViewDuration;
    private TextView textViewPoints, textViewStartTime, textViewEndTime, textViewQuizTitle;
    private CircularProgressIndicator progressIndicator;
    private MaterialButton btnReview, btnFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_attempt_result);

        initViews();
        setupViewModel();
        setupClickListeners();

        // Get attempt ID from intent
        Long attemptId = getIntent().getLongExtra(EXTRA_ATTEMPT_ID, -1);
        if (attemptId != -1) {
            viewModel.loadAttemptDetails(attemptId);
        } else {
            // Fallback to using data from intent extras
            displayResultsFromIntent();
        }
    }

    private void initViews() {
        textViewScore = findViewById(R.id.text_view_score);
        textViewScoreDetails = findViewById(R.id.text_view_score_details);
        textViewDuration = findViewById(R.id.text_view_duration);
        textViewPoints = findViewById(R.id.text_view_points);
        textViewStartTime = findViewById(R.id.text_view_start_time);
        textViewEndTime = findViewById(R.id.text_view_end_time);
        textViewQuizTitle = findViewById(R.id.text_view_quiz_title);
        progressIndicator = findViewById(R.id.progress_indicator);
        btnReview = findViewById(R.id.btn_review);
        btnFinish = findViewById(R.id.btn_finish);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(QuizAttemptResultViewModel.class);

        viewModel.getAttemptDetails().observe(this, details -> {
            if (details != null) {
                displayResults(details);
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            // Show/hide loading indicator
            if (isLoading != null && isLoading) {
                // Show loading
            } else {
                // Hide loading
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                // Show error message
            }
        });
    }

    private void setupClickListeners() {
        btnReview.setOnClickListener(v -> {
            Long attemptId = getIntent().getLongExtra(EXTRA_ATTEMPT_ID, -1);
            if (attemptId != -1) {
                Intent intent = new Intent(this, ReviewQuizAttemptActivity.class);
                intent.putExtra(ReviewQuizAttemptActivity.EXTRA_ATTEMPT_ID, attemptId);
                startActivity(intent);
            }
        });

        btnFinish.setOnClickListener(v -> {
            finish();
        });
    }

    private void displayResults(QuizAttemptDetails details) {
        // Display quiz title
        if (details.getQuiz() != null && details.getQuiz().getTitle() != null) {
            textViewQuizTitle.setText(details.getQuiz().getTitle());
        }

        // Calculate score percentage
        int totalPoints = details.getTotalPoints();
        int earnedPoints = details.getEarnedPoints();
        int scorePercentage = totalPoints > 0 ? (earnedPoints * 100) / totalPoints : 0;

        // Display score
        textViewScore.setText(String.format(Locale.getDefault(), "%d%%", scorePercentage));
        textViewScoreDetails.setText(String.format(Locale.getDefault(),
                "%d/%d Correct", details.getCorrectAnswersCount(), details.getTotalQuestions()));

        // Set progress indicator
        progressIndicator.setProgress(scorePercentage);

        // Display duration
        long duration = details.getAttempt().getDuration();
        long minutes = duration / 60;
        long seconds = duration % 60;
        textViewDuration.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));

        // Display points
        textViewPoints.setText(String.format(Locale.getDefault(),
                "%d/%d", earnedPoints, totalPoints));

        // Display times
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        textViewStartTime.setText(timeFormat.format(details.getAttempt().getStartTime()));
        textViewEndTime.setText(timeFormat.format(details.getAttempt().getEndTime()));
    }

    private void displayResultsFromIntent() {
        // Fallback method if we don't have attemptId but have data from TakeQuizActivity
        int totalQuestions = getIntent().getIntExtra("totalQuestions", 0);
        int correctAnswers = getIntent().getIntExtra("correctAnswers", 0);
        int totalPoints = getIntent().getIntExtra("totalPoints", 0);
        int earnedPoints = getIntent().getIntExtra("earnedPoints", 0);
        long duration = getIntent().getLongExtra("duration", 0);

        int scorePercentage = totalPoints > 0 ? (earnedPoints * 100) / totalPoints : 0;

        textViewScore.setText(String.format(Locale.getDefault(), "%d%%", scorePercentage));
        textViewScoreDetails.setText(String.format(Locale.getDefault(), "%d/%d Correct", correctAnswers, totalQuestions));
        progressIndicator.setProgress(scorePercentage);

        long minutes = duration / 60;
        long seconds = duration % 60;
        textViewDuration.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
        textViewPoints.setText(String.format(Locale.getDefault(), "%d/%d", earnedPoints, totalPoints));

        // Hide review button since we don't have attempt details
        btnReview.setVisibility(View.GONE);
    }
}