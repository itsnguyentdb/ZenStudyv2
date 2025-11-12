package com.example.zen_study.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zen_study.R;
import com.example.zen_study.adapters.ReviewQuizAttemptAdapter;
import com.example.zen_study.models.QuizAttemptAnswer;
import com.example.zen_study.models.QuizQuestion;
import com.example.zen_study.viewmodels.ReviewQuizAttemptViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReviewQuizAttemptActivity extends AppCompatActivity {
    public static final String EXTRA_ATTEMPT_ID = "attempt_id";

    private ReviewQuizAttemptViewModel viewModel;
    private RecyclerView recyclerViewReview;
    private TextView textViewReviewProgress;
    private MaterialButton btnFinishReview;

    private List<QuizQuestion> questions = new ArrayList<>();
    private List<QuizAttemptAnswer> answers = new ArrayList<>();
    private ReviewQuizAttemptAdapter reviewQuizAttemptAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_quiz_attempt);

        initViews();
        setupViewModel();

        Long attemptId = getIntent().getLongExtra(EXTRA_ATTEMPT_ID, -1);
        if (attemptId != -1) {
            viewModel.loadAttemptDetails(attemptId);
        }
    }

    private void initViews() {
        recyclerViewReview = findViewById(R.id.recycler_view_review);
        textViewReviewProgress = findViewById(R.id.text_view_review_progress);
        btnFinishReview = findViewById(R.id.btn_finish_review);

        // Setup RecyclerView
        recyclerViewReview.setLayoutManager(new LinearLayoutManager(this));
        reviewQuizAttemptAdapter = new ReviewQuizAttemptAdapter();
        recyclerViewReview.setAdapter(reviewQuizAttemptAdapter);

        btnFinishReview.setOnClickListener(v -> finish());
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ReviewQuizAttemptViewModel.class);

        viewModel.getAttemptDetails().observe(this, details -> {
            if (details != null) {
                questions = details.getQuestions();
                answers = details.getAnswers();
                setupRecyclerView();
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            // Show/hide loading indicator
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                // Show error
            }
        });
    }

    private void setupRecyclerView() {
        if (questions.isEmpty()) return;

        reviewQuizAttemptAdapter.setData(questions, answers);
        updateProgressText();
    }

    private void updateProgressText() {
        textViewReviewProgress.setText(String.format(Locale.getDefault(),
                "Reviewing %d Questions", questions.size()));
    }
}