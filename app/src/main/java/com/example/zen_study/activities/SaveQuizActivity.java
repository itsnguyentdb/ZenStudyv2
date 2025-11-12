package com.example.zen_study.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zen_study.R;
import com.example.zen_study.adapters.SaveQuestionsAdapter;
import com.example.zen_study.models.Quiz;
import com.example.zen_study.models.QuizQuestion;
import com.example.zen_study.viewmodels.SaveQuizViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SaveQuizActivity extends AppCompatActivity {
    private SaveQuizViewModel viewModel;
    // UI Components
    private EditText editTextTitle;
    private EditText editTextDescription;
    private EditText editTextTimeLimit;
    private TextInputLayout textInputLayoutTimeLimit;
    private TextInputLayout textInputLayoutTitle;
    private RecyclerView recyclerViewQuestions;
    private SaveQuestionsAdapter questionsAdapter;
    private LinearLayout layoutEmptyQuestions;
    private MaterialButton btnAddQuestion;
    private MaterialButton btnSave;
    private MaterialButton btnCancel;
    private boolean isEditing = false;
    private Long quizId = null;
    private List<QuizQuestion> questions = new ArrayList<>();
    private ItemTouchHelper itemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_quiz);
        setupToolbar();
        initViews();
        setupViewModel();
        setupRecyclerView();
        setupClickListeners();
        setupTextWatchers();
        loadIntentData();
    }

    private void setupToolbar() {
//        setSupportActionBar(findViewById(R.id.toolbar));
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setTitle("Create Quiz");
//        }
    }

    private void initViews() {
        textInputLayoutTitle = findViewById(R.id.text_input_layout_title);
        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDescription = findViewById(R.id.edit_text_description);
        textInputLayoutTimeLimit = findViewById(R.id.text_input_layout_time_limit);
        editTextTimeLimit = findViewById(R.id.edit_text_time_limit);
        recyclerViewQuestions = findViewById(R.id.recycler_view_questions);
        layoutEmptyQuestions = findViewById(R.id.layout_empty_questions);
        btnAddQuestion = findViewById(R.id.btn_add_question);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(SaveQuizViewModel.class);
        viewModel.getCurrentQuiz().observe(this, quiz -> {
            if (quiz != null) {
                populateQuizData(quiz);
            }
        });
        viewModel.getQuestions().observe(this, questions -> {
            this.questions = questions;
            questionsAdapter.setQuestions(questions);
            updateQuestionsUI(questions);
        });
        viewModel.getSaveResult().observe(this, result -> {
            btnSave.setEnabled(true);
            if (result != null) {
                if (result) {
                    Toast.makeText(this,
                            isEditing ? "Quiz updated successfully" : "Quiz created successfully",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to save quiz", Toast.LENGTH_SHORT).show();
                }
            }
        });
        viewModel.getIsLoading().observe(this, isLoading -> {
            btnSave.setEnabled(!isLoading);
        });
    }

    private void setupRecyclerView() {
        recyclerViewQuestions.setLayoutManager(new LinearLayoutManager(this));
        questionsAdapter = new SaveQuestionsAdapter(new ArrayList<>());
        recyclerViewQuestions.setAdapter(questionsAdapter);
        // Setup drag to reorder
        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                reorderQuestions(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // No swipe action
            }
        };
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerViewQuestions);
        questionsAdapter.setItemTouchHelper(itemTouchHelper);
        questionsAdapter.setOnQuestionDeleteClickListener(question -> {
            deleteQuestion(question);
        });
    }

    private void setupClickListeners() {
        btnAddQuestion.setOnClickListener(v -> {
            addNewQuestion();
        });
        btnSave.setOnClickListener(v -> {
            saveQuiz();
        });
        btnCancel.setOnClickListener(v -> {
            onBackPressed();
        });
        // Add question from empty state
        layoutEmptyQuestions.setOnClickListener(v -> {
            addNewQuestion();
        });
    }

    private void setupTextWatchers() {
        editTextTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateTitle();
            }
        });
        editTextTimeLimit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateTimeLimit();
            }
        });
    }

    private void loadIntentData() {
        if (getIntent() != null && getIntent().hasExtra("quizId")) {
            quizId = getIntent().getLongExtra("quizId", -1);
            if (quizId != -1) {
                isEditing = true;
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Edit Quiz");
                }
                viewModel.loadQuizData(quizId);
            }
        }
    }

    private void populateQuizData(Quiz quiz) {
        if (quiz.getTitle() != null) {
            editTextTitle.setText(quiz.getTitle());
        }
        if (quiz.getDescription() != null) {
            editTextDescription.setText(quiz.getDescription());
        }
        editTextTimeLimit.setText(String.valueOf(quiz.getTimeLimit()));
    }

    private void updateQuestionsUI(List<QuizQuestion> questions) {
        if (questions == null || questions.isEmpty()) {
            layoutEmptyQuestions.setVisibility(View.VISIBLE);
            recyclerViewQuestions.setVisibility(View.GONE);
        } else {
            layoutEmptyQuestions.setVisibility(View.GONE);
            recyclerViewQuestions.setVisibility(View.VISIBLE);
        }
    }

    private boolean validateTitle() {
        String title = editTextTitle.getText().toString().trim();
        if (title.isEmpty()) {
            textInputLayoutTitle.setError("Title is required");
            return false;
        } else {
            textInputLayoutTitle.setError(null);
            return true;
        }
    }

    private boolean validateTimeLimit() {
        String timeLimitStr = editTextTimeLimit.getText().toString().trim();
        if (timeLimitStr.isEmpty()) {
            textInputLayoutTimeLimit.setError("Time limit is required");
            return false;
        }
        try {
            int timeLimit = Integer.parseInt(timeLimitStr);
            if (timeLimit < 0) {
                textInputLayoutTimeLimit.setError("Time limit cannot be negative");
                return false;
            }
            textInputLayoutTimeLimit.setError(null);
            return true;
        } catch (NumberFormatException e) {
            textInputLayoutTimeLimit.setError("Please enter a valid number");
            return false;
        }
    }

    private boolean validateQuestions() {
        if (questions.isEmpty()) {
            Toast.makeText(this, "Please add at least one question to the quiz", Toast.LENGTH_LONG).show();
            return false;
        }

        boolean isValid = true;
        for (int i = 0; i < questions.size(); i++) {
            QuizQuestion question = questions.get(i);
            SaveQuestionsAdapter.QuestionViewHolder holder = (SaveQuestionsAdapter.QuestionViewHolder) recyclerViewQuestions.findViewHolderForAdapterPosition(i);

            // Clear previous errors if holder is visible
            if (holder != null) {
                holder.textInputLayoutQuestion.setError(null);
                holder.textInputLayoutOptionA.setError(null);
                holder.textInputLayoutOptionB.setError(null);
                holder.textInputLayoutOptionC.setError(null);
                holder.textInputLayoutOptionD.setError(null);
                holder.textInputLayoutPoints.setError(null);
            }

            // Validate question text
            if (question.getQuestionText() == null || question.getQuestionText().trim().isEmpty()) {
                if (holder != null) {
                    holder.textInputLayoutQuestion.setError("Required");
                }
                isValid = false;
            }

            // Validate points
            if (question.getPoints() <= 0) {
                if (holder != null) {
                    holder.textInputLayoutPoints.setError("Points must be greater than 0");
                }
                isValid = false;
            }

            // Validate options
            int optionCount = 0;
            if (question.getOptionA() != null && !question.getOptionA().trim().isEmpty())
                optionCount++;
            if (question.getOptionB() != null && !question.getOptionB().trim().isEmpty())
                optionCount++;
            if (question.getOptionC() != null && !question.getOptionC().trim().isEmpty())
                optionCount++;
            if (question.getOptionD() != null && !question.getOptionD().trim().isEmpty())
                optionCount++;

            if (optionCount < 2) {
                Toast.makeText(this, "Each question must have at least 2 options", Toast.LENGTH_LONG).show();
                isValid = false;
            }

            // Validate correct answer
            if (question.getCorrectAnswer() == null || question.getCorrectAnswer().isEmpty()) {
                Toast.makeText(this, "Each question must have a correct answer selected", Toast.LENGTH_LONG).show();
                isValid = false;
            } else {
                // Verify that the selected correct answer has non-empty text
                boolean validCorrect = false;
                TextInputLayout errorLayout = null;

                switch (question.getCorrectAnswer()) {
                    case "A":
                        validCorrect = question.getOptionA() != null && !question.getOptionA().trim().isEmpty();
                        if (holder != null) errorLayout = holder.textInputLayoutOptionA;
                        break;
                    case "B":
                        validCorrect = question.getOptionB() != null && !question.getOptionB().trim().isEmpty();
                        if (holder != null) errorLayout = holder.textInputLayoutOptionB;
                        break;
                    case "C":
                        validCorrect = question.getOptionC() != null && !question.getOptionC().trim().isEmpty();
                        if (holder != null) errorLayout = holder.textInputLayoutOptionC;
                        break;
                    case "D":
                        validCorrect = question.getOptionD() != null && !question.getOptionD().trim().isEmpty();
                        if (holder != null) errorLayout = holder.textInputLayoutOptionD;
                        break;
                }

                if (!validCorrect) {
                    if (errorLayout != null) {
                        errorLayout.setError("Cannot select empty option as correct");
                    }
                    isValid = false;
                }
            }
        }

        if (!isValid) {
            Toast.makeText(this, "Please fix the errors in questions", Toast.LENGTH_LONG).show();
        }
        return isValid;
    }

    private void saveQuiz() {
        if (!validateTitle() || !validateTimeLimit() || !validateQuestions()) {
            return;
        }
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        int timeLimit = Integer.parseInt(editTextTimeLimit.getText().toString().trim());
        btnSave.setEnabled(false);
        if (isEditing && quizId != null) {
            viewModel.updateQuiz(quizId, title, description, timeLimit, questions);
        } else {
            viewModel.createQuiz(title, description, timeLimit, questions);
        }
    }

    private void addNewQuestion() {
        QuizQuestion newQuestion = QuizQuestion.builder()
                .questionText("")
                .questionType("multiple_choice")
                .optionA("")
                .optionB("")
                .optionC("")
                .optionD("")
                .correctAnswer("")
                .explanation("")
                .position(questions.size())
                .points(1)
                .build();
        questions.add(newQuestion);
        questionsAdapter.notifyItemInserted(questions.size() - 1);
        updateQuestionsUI(questions);
        // Optionally scroll to the new item
        recyclerViewQuestions.scrollToPosition(questions.size() - 1);
    }

    private void deleteQuestion(QuizQuestion question) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Question")
                .setMessage("Are you sure you want to delete this question?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int position = questions.indexOf(question);
                    questions.remove(question);
                    questionsAdapter.notifyItemRemoved(position);
                    updateQuestionsUI(questions);
                    // Update positions
                    for (int i = position; i < questions.size(); i++) {
                        questions.get(i).setPosition(i);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void reorderQuestions(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(questions, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(questions, i, i - 1);
            }
        }
        // Update positions
        for (int i = 0; i < questions.size(); i++) {
            questions.get(i).setPosition(i);
        }
        questionsAdapter.notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (hasUnsavedChanges()) {
            showUnsavedChangesDialog();
        } else {
            super.onBackPressed();
        }
    }

    private boolean hasUnsavedChanges() {
        String currentTitle = editTextTitle.getText().toString().trim();
        String currentDescription = editTextDescription.getText().toString().trim();
        boolean quizDataChanged = !currentTitle.isEmpty() || !currentDescription.isEmpty();
        boolean hasQuestions = !questions.isEmpty();
        return quizDataChanged || hasQuestions;
    }

    private void showUnsavedChangesDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Unsaved Changes")
                .setMessage("You have unsaved changes. Do you want to discard them?")
                .setPositiveButton("Discard", (dialog, which) -> {
                    super.onBackPressed();
                })
                .setNegativeButton("Keep Editing", null)
                .show();
    }
}