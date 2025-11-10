package com.example.quiz_clone.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.quiz_clone.R;
import com.example.quiz_clone.models.QuizQuestion;
import com.example.quiz_clone.models.QuizAttempt;
import com.example.quiz_clone.models.QuizAttemptAnswer;
import com.example.quiz_clone.repositories.impls.QuizRepositoryImpl;
import com.example.quiz_clone.viewmodels.TakeQuizViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class TakeQuizActivity extends AppCompatActivity {
    public static String EXTRA_QUIZ_ID = "quiz_id";
    private TakeQuizViewModel viewModel;
    private QuizRepositoryImpl quizRepository;
    private TextView textViewTimer, textViewQuestionProgress, textViewQuestion;
    private TextView textOptionA, textOptionB, textOptionC, textOptionD;
    private RadioGroup radioGroupOptions;
    private RadioButton radioOptionA, radioOptionB, radioOptionC, radioOptionD;
    private MaterialButton btnPrevious, btnNext, btnSubmit;

    private List<QuizQuestion> questions = new ArrayList<>();
    private String[] userAnswers;
    private int currentQuestionIndex = 0;
    private CountDownTimer countDownTimer;
    private long timeRemaining = 0;
    private Date startTime;
    private Long quizId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_quiz);

        initViews();
        setupViewModel();
        setupClickListeners();

        startTime = new Date();
        quizId = getIntent().getLongExtra(EXTRA_QUIZ_ID, -1);
        quizRepository = new QuizRepositoryImpl(this);

        if (quizId != -1) {
            viewModel.loadQuiz(quizId);
        }
    }

    private void initViews() {
        textViewTimer = findViewById(R.id.text_view_timer);
        textViewQuestionProgress = findViewById(R.id.text_view_question_progress);
        textViewQuestion = findViewById(R.id.text_view_question);

        textOptionA = findViewById(R.id.text_option_a);
        textOptionB = findViewById(R.id.text_option_b);
        textOptionC = findViewById(R.id.text_option_c);
        textOptionD = findViewById(R.id.text_option_d);

        radioGroupOptions = findViewById(R.id.radio_group_options);
        radioOptionA = findViewById(R.id.radio_option_a);
        radioOptionB = findViewById(R.id.radio_option_b);
        radioOptionC = findViewById(R.id.radio_option_c);
        radioOptionD = findViewById(R.id.radio_option_d);

        btnPrevious = findViewById(R.id.btn_previous);
        btnNext = findViewById(R.id.btn_next);
        btnSubmit = findViewById(R.id.btn_submit);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TakeQuizViewModel.class);

        viewModel.getQuiz().observe(this, quiz -> {
            if (quiz != null && quiz.getQuestions() != null) {
                questions = quiz.getQuestions();
                // Initialize userAnswers array with null values
                userAnswers = new String[questions.size()];

                if (questions.isEmpty()) {
                    Toast.makeText(this, "This quiz has no questions", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                startTimer(quiz.getTimeLimit());
                displayQuestion(currentQuestionIndex);
                updateNavigationButtons();
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                // Show loading indicator
                btnSubmit.setEnabled(false);
                btnNext.setEnabled(false);
                btnPrevious.setEnabled(false);
            } else {
                // Hide loading indicator
                btnSubmit.setEnabled(true);
                updateNavigationButtons();
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void setupClickListeners() {
        btnPrevious.setOnClickListener(v -> {
            saveCurrentAnswer();
            currentQuestionIndex--;
            displayQuestion(currentQuestionIndex);
            updateNavigationButtons();
        });

        btnNext.setOnClickListener(v -> {
            saveCurrentAnswer();
            currentQuestionIndex++;
            displayQuestion(currentQuestionIndex);
            updateNavigationButtons();
        });

        btnSubmit.setOnClickListener(v -> {
            showSubmitConfirmation();
        });
        radioOptionA.setOnClickListener(v -> {
            System.out.println("Radio A clicked, checked: " + radioOptionA.isChecked());
            saveCurrentAnswer();
        });

        radioOptionB.setOnClickListener(v -> {
            System.out.println("Radio B clicked, checked: " + radioOptionB.isChecked());
            saveCurrentAnswer();
        });

        radioOptionC.setOnClickListener(v -> {
            System.out.println("Radio C clicked, checked: " + radioOptionC.isChecked());
            saveCurrentAnswer();
        });

        radioOptionD.setOnClickListener(v -> {
            System.out.println("Radio D clicked, checked: " + radioOptionD.isChecked());
            saveCurrentAnswer();
        });

        // Also keep the radio group listener
        radioGroupOptions.setOnCheckedChangeListener((group, checkedId) -> {
            System.out.println("RadioGroup changed, checkedId: " + checkedId);
            saveCurrentAnswer();
        });

//        radioGroupOptions.setOnCheckedChangeListener((group, checkedId) -> {
//            saveCurrentAnswer();
//        });
    }

    private void startTimer(int timeLimitMinutes) {
        if (timeLimitMinutes <= 0) {
            textViewTimer.setText("No Limit");
            return;
        }

        long totalTimeMillis = timeLimitMinutes * 60 * 1000L;
        timeRemaining = totalTimeMillis;

        countDownTimer = new CountDownTimer(totalTimeMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;
                updateTimerText(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                timeRemaining = 0;
                textViewTimer.setText("00:00");
                autoSubmitQuiz();
            }
        }.start();
    }

    private void updateTimerText(long millisUntilFinished) {
        int minutes = (int) (millisUntilFinished / 1000) / 60;
        int seconds = (int) (millisUntilFinished / 1000) % 60;
        String timeLeft = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        textViewTimer.setText(timeLeft);
    }

    private void displayQuestion(int questionIndex) {
        if (questions == null || questions.isEmpty() || questionIndex < 0 || questionIndex >= questions.size()) {
            return;
        }

        QuizQuestion question = questions.get(questionIndex);

        textViewQuestion.setText(question.getQuestionText());
        textViewQuestionProgress.setText(String.format(Locale.getDefault(), "%d/%d", questionIndex + 1, questions.size()));

        // Display options
        textOptionA.setText(question.getOptionA());
        textOptionB.setText(question.getOptionB());
        textOptionC.setText(question.getOptionC() != null ? question.getOptionC() : "");
        textOptionD.setText(question.getOptionD() != null ? question.getOptionD() : "");

        // Show/hide options based on availability
        radioOptionC.setVisibility(question.getOptionC() != null && !question.getOptionC().isEmpty() ? View.VISIBLE : View.GONE);
        radioOptionD.setVisibility(question.getOptionD() != null && !question.getOptionD().isEmpty() ? View.VISIBLE : View.GONE);

        // Remove the listener temporarily to prevent triggering saveCurrentAnswer during setup
        radioGroupOptions.setOnCheckedChangeListener(null);

        // Clear all radio buttons first
        radioOptionA.setChecked(false);
        radioOptionB.setChecked(false);
        radioOptionC.setChecked(false);
        radioOptionD.setChecked(false);

        // Restore user's answer from array
        String userAnswer = userAnswers[questionIndex];
        if (userAnswer != null && !userAnswer.isEmpty()) {
            switch (userAnswer) {
                case "A":
                    radioOptionA.setChecked(true);
                    break;
                case "B":
                    radioOptionB.setChecked(true);
                    break;
                case "C":
                    radioOptionC.setChecked(true);
                    break;
                case "D":
                    radioOptionD.setChecked(true);
                    break;
            }
        }

        // Restore the listener after setting up the question
        radioGroupOptions.setOnCheckedChangeListener((group, checkedId) -> {
            System.out.println("RadioGroup changed, checkedId: " + checkedId);
            saveCurrentAnswer();
        });
    }

    private void saveCurrentAnswer() {
        int checkedId = radioGroupOptions.getCheckedRadioButtonId();

        System.out.println("DEBUG: checkedId = " + checkedId);
        System.out.println("DEBUG: RadioGroup child count = " + radioGroupOptions.getChildCount());

        // Check each radio button's state
        System.out.println("DEBUG: Radio A checked = " + radioOptionA.isChecked());
        System.out.println("DEBUG: Radio B checked = " + radioOptionB.isChecked());
        System.out.println("DEBUG: Radio C checked = " + radioOptionC.isChecked());
        System.out.println("DEBUG: Radio D checked = " + radioOptionD.isChecked());

        String selectedAnswer = "";

        if (checkedId == R.id.radio_option_a) {
            selectedAnswer = "A";
        } else if (checkedId == R.id.radio_option_b) {
            selectedAnswer = "B";
        } else if (checkedId == R.id.radio_option_c) {
            selectedAnswer = "C";
        } else if (checkedId == R.id.radio_option_d) {
            selectedAnswer = "D";
        } else {
            // If checkedId is -1, try checking individual buttons
            if (radioOptionA.isChecked()) {
                selectedAnswer = "A";
                System.out.println("DEBUG: Found A via direct check");
            } else if (radioOptionB.isChecked()) {
                selectedAnswer = "B";
                System.out.println("DEBUG: Found B via direct check");
            } else if (radioOptionC.isChecked()) {
                selectedAnswer = "C";
                System.out.println("DEBUG: Found C via direct check");
            } else if (radioOptionD.isChecked()) {
                selectedAnswer = "D";
                System.out.println("DEBUG: Found D via direct check");
            }
        }

        System.out.println("Selected: " + selectedAnswer);

        // Always store the answer (even if empty) for the current question
        if (userAnswers != null && currentQuestionIndex >= 0 && currentQuestionIndex < userAnswers.length) {
            userAnswers[currentQuestionIndex] = selectedAnswer;
            System.out.println("Saved answer for question " + currentQuestionIndex + ": " + selectedAnswer);
        }
    }

    private void updateNavigationButtons() {
        btnPrevious.setEnabled(currentQuestionIndex > 0);
        btnNext.setEnabled(currentQuestionIndex < questions.size() - 1);
        btnSubmit.setVisibility(currentQuestionIndex == questions.size() - 1 ? View.VISIBLE : View.GONE);
    }

    private void showSubmitConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Submit Quiz")
                .setMessage("Are you sure you want to submit your answers?")
                .setPositiveButton("Submit", (dialog, which) -> submitQuiz())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void autoSubmitQuiz() {
        saveCurrentAnswer();
        submitQuiz();
    }

    private void submitQuiz() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        Date endTime = new Date();
        long duration = (endTime.getTime() - startTime.getTime()) / 1000; // in seconds

        // Calculate score
        int totalPoints = 0;
        int earnedPoints = 0;
        int correctAnswers = 0;

        // Create a list to track correctness for each question
        List<Boolean> answerCorrectness = new ArrayList<>();

        for (int i = 0; i < questions.size(); i++) {
            QuizQuestion question = questions.get(i);
            totalPoints += question.getPoints();

            // Get user answer from array (could be null or empty)
            String userAnswer = userAnswers[i];
            boolean isCorrect = userAnswer != null && userAnswer.equals(question.getCorrectAnswer());
            answerCorrectness.add(isCorrect);

            if (isCorrect) {
                earnedPoints += question.getPoints();
                correctAnswers++;
            }

            // Debug logging
            System.out.println("Question " + i + ": User Answer = " + userAnswer +
                    ", Correct Answer = " + question.getCorrectAnswer() +
                    ", Is Correct = " + isCorrect);
        }

        int scorePercentage = totalPoints > 0 ? (earnedPoints * 100) / totalPoints : 0;

        // Create and save quiz attempt
        QuizAttempt attempt = quizRepository.startQuizAttempt(quizId);
        attempt.setEndTime(endTime);
        attempt.setScore(scorePercentage);
        attempt.setDuration(duration);

        // Save the completed attempt
        attempt = quizRepository.saveQuizAttempt(attempt);

        // Save individual answers with correctness
        for (int i = 0; i < questions.size(); i++) {
            QuizQuestion question = questions.get(i);
            String userAnswer = userAnswers[i];
            boolean isCorrect = answerCorrectness.get(i);

            quizRepository.saveAnswer(attempt.getId(), question.getId(),
                    userAnswer != null ? userAnswer : "", isCorrect);
        }

        // Navigate to result activity
        Intent intent = new Intent(this, QuizAttemptResultActivity.class);
        intent.putExtra(QuizAttemptResultActivity.EXTRA_ATTEMPT_ID, attempt.getId());
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}