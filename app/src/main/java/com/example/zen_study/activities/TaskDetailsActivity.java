package com.example.zen_study.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zen_study.R;
import com.example.zen_study.adapters.StudySessionAdapter;
import com.example.zen_study.components.SessionTimeDialog;
import com.example.zen_study.models.StudySession;
import com.example.zen_study.models.Task;
import com.example.zen_study.viewmodels.TaskDetailsViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TaskDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "task_id";
    private static final int REQUEST_EDIT_TASK = 1001;
    private static final int REQUEST_STUDY_SESSION = 1002;

    private TaskDetailsViewModel viewModel;
    private StudySessionAdapter sessionAdapter;
    private long taskId;

    // Views
    private TextView textTaskTitle;
    private TextView textTaskDescription;
    private TextView textProgressPercentage;
    private TextView textDeadline;
    private TextView textDuration;
    private TextView textTimeSpent;
    private TextView textTotalSessions;
    private TextView textTotalTime;
    private TextView textAvgSession;
    private TextView textCompletionStatus;
    private LinearProgressIndicator progressBar;
    private Chip chipStatus;
    private Chip chipPriority;
    private Chip chipSubject;
    private MaterialButton btnStartSession;
    private MaterialButton btnCancel;
    private MaterialButton btnEdit;
    private RecyclerView recyclerViewSessions;
    private View layoutEmptySessions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        viewModel = new ViewModelProvider(this).get(TaskDetailsViewModel.class);
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        setupObservers();
        loadTaskData();
    }

    private void initViews() {
        textTaskTitle = findViewById(R.id.text_task_title);
        textTaskDescription = findViewById(R.id.text_task_description);
        textProgressPercentage = findViewById(R.id.text_progress_percentage);
        textDeadline = findViewById(R.id.text_deadline);
        textDuration = findViewById(R.id.text_duration);
        textTimeSpent = findViewById(R.id.text_time_spent);
        textTotalSessions = findViewById(R.id.text_total_sessions);
        textTotalTime = findViewById(R.id.text_total_time);
        textAvgSession = findViewById(R.id.text_avg_session);
        textCompletionStatus = findViewById(R.id.text_completion_status);
        progressBar = findViewById(R.id.progress_bar);
        chipStatus = findViewById(R.id.chip_status);
        chipPriority = findViewById(R.id.chip_priority);
        chipSubject = findViewById(R.id.chip_subject);
        btnStartSession = findViewById(R.id.btn_start_session);
        btnCancel = findViewById(R.id.btn_task_details_cancel);
        btnEdit = findViewById(R.id.btn__task_details_edit);
        recyclerViewSessions = findViewById(R.id.recycler_view_sessions);
        layoutEmptySessions = findViewById(R.id.layout_empty_sessions);
    }

    private void setupToolbar() {
//        setSupportActionBar(findViewById(R.id.toolbar));
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setDisplayShowHomeEnabled(true);
//        }
//
//        findViewById(R.id.toolbar).setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        sessionAdapter = new StudySessionAdapter();
        recyclerViewSessions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSessions.setAdapter(sessionAdapter);
    }

    private void setupClickListeners() {
        btnStartSession.setOnClickListener(v -> startStudySession());
        btnEdit.setOnClickListener(v -> editTask());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void setupObservers() {
        viewModel.getTask().observe(this, this::updateTaskUI);
        viewModel.getStudySessions().observe(this, this::updateStudySessions);
        viewModel.getIsLoading().observe(this, this::updateLoadingState);
        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTaskData() {
        taskId = getIntent().getLongExtra(EXTRA_TASK_ID, -1);
        if (taskId == -1) {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        viewModel.loadTaskData(taskId);
    }

    private void updateTaskUI(Task task) {
        if (task == null) return;

        textTaskTitle.setText(task.getTitle());
        textTaskDescription.setText(task.getDescription() != null ? task.getDescription() : "No description");

        // Progress
        int progress = (int) (task.getProgress() * 100);
        progressBar.setProgress(progress);
        textProgressPercentage.setText(progress + "%");

        // Status
        setupStatusChip(task.getStatus());

        // Priority
        setupPriorityChip(task.getPriority());

        // Subject
        setupSubjectChip(task.getSubjectId());

        // Deadline
        if (task.getDeadline() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            textDeadline.setText(sdf.format(task.getDeadline()));
        } else {
            textDeadline.setText("No deadline");
        }

        // Duration
        textDuration.setText(task.getExpectedDuration() + " minutes");

        // Time spent
        textTimeSpent.setText(task.getProgressDuration() + " minutes");

        // Completion status
        updateCompletionStatusText(task);
    }

    private void updateStudySessions(List<StudySession> sessions) {
        sessionAdapter.submitList(sessions);
        updateSessionStats(sessions);
        updateEmptySessionState(sessions.isEmpty());
    }

    private void updateSessionStats(List<StudySession> sessions) {
        int totalSessions = sessions.size();
        long totalTime = sessions.stream().mapToLong(StudySession::getDuration).sum();
        long avgSession = totalSessions > 0 ? totalTime / totalSessions : 0;

        textTotalSessions.setText(String.valueOf(totalSessions));
        textTotalTime.setText(formatDuration(totalTime));
        textAvgSession.setText(formatDuration(avgSession));
    }

    private String formatDuration(long minutes) {
        if (minutes < 60) {
            return minutes + "m";
        } else {
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;
            if (remainingMinutes > 0) {
                return hours + "h " + remainingMinutes + "m";
            } else {
                return hours + "h";
            }
        }
    }

    private void updateCompletionStatusText(Task task) {
        int totalStudyTime = task.getProgressDuration();
        int expectedDuration = task.getExpectedDuration();
        int remainingTime = Math.max(0, expectedDuration - totalStudyTime);

        if (task.getStatus() == Task.TaskType.COMPLETED) {
            textCompletionStatus.setText("✅ Task Completed!");
            textCompletionStatus.setTextColor(getColor(R.color.status_completed));
        } else if (task.getStatus() == Task.TaskType.OVERDUE) {
            textCompletionStatus.setText("⚠️ Overdue - " + remainingTime + " minutes remaining");
            textCompletionStatus.setTextColor(getColor(R.color.status_overdue));
        } else {
            textCompletionStatus.setText(remainingTime + " minutes remaining to complete");
            textCompletionStatus.setTextColor(getColor(R.color.status_in_progress));
        }
    }

    private void updateEmptySessionState(boolean isEmpty) {
        if (isEmpty) {
            layoutEmptySessions.setVisibility(View.VISIBLE);
            recyclerViewSessions.setVisibility(View.GONE);
        } else {
            layoutEmptySessions.setVisibility(View.GONE);
            recyclerViewSessions.setVisibility(View.VISIBLE);
        }
    }

    private void updateLoadingState(Boolean isLoading) {
        if (isLoading != null && isLoading) {
//            loadingProgress.setVisibility(View.VISIBLE);
        } else {
//            loadingProgress.setVisibility(View.GONE);
        }
    }

    private void setupStatusChip(Task.TaskType status) {
        switch (status) {
            case TODO:
                chipStatus.setText("To Do");
                chipStatus.setChipBackgroundColorResource(R.color.status_todo);
                break;
            case IN_PROGRESS:
                chipStatus.setText("In Progress");
                chipStatus.setChipBackgroundColorResource(R.color.status_in_progress);
                break;
            case COMPLETED:
                chipStatus.setText("Completed");
                chipStatus.setChipBackgroundColorResource(R.color.status_completed);
                break;
            case OVERDUE:
                chipStatus.setText("Overdue");
                chipStatus.setChipBackgroundColorResource(R.color.status_overdue);
                break;
        }
    }

    private void setupPriorityChip(int priority) {
        switch (priority) {
            case 1:
                chipPriority.setText("Low");
                chipPriority.setChipBackgroundColorResource(R.color.priority_low);
                break;
            case 2:
                chipPriority.setText("Medium");
                chipPriority.setChipBackgroundColorResource(R.color.priority_medium);
                break;
            case 3:
                chipPriority.setText("High");
                chipPriority.setChipBackgroundColorResource(R.color.priority_high);
                break;
            default:
                chipPriority.setText("Normal");
                chipPriority.setChipBackgroundColorResource(R.color.priority_normal);
        }
    }

    private void setupSubjectChip(long subjectId) {
        String subjectName = getSubjectNameById(subjectId);
        chipSubject.setText(subjectName);
    }

    private String getSubjectNameById(long subjectId) {
        switch ((int) subjectId) {
            case 1:
                return "Math";
            case 2:
                return "Science";
            case 3:
                return "History";
            case 4:
                return "Language";
            case 5:
                return "Computer Science";
            default:
                return "Other";
        }
    }

    private void startStudySession() {
        // Show the time selection dialog
        SessionTimeDialog dialog = new SessionTimeDialog(TaskDetailsActivity.this, taskId);
        dialog.show();

//        Window window = dialog.getWindow();
//        if (window != null) {
//            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        }
//        Intent intent = new Intent(this, StudySessionActivity.class);
//        intent.putExtra(StudySessionActivity.EXTRA_TASK_ID, taskId);
//        startActivityForResult(intent, REQUEST_STUDY_SESSION);
    }

    private void editTask() {
        Intent intent = new Intent(this, SaveTaskActivity.class);
        intent.putExtra(SaveTaskActivity.EXTRA_MODE, SaveTaskActivity.MODE_EDIT);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_EDIT_TASK && resultCode == RESULT_OK) {
            // Reload task data after editing
            viewModel.loadTaskData(taskId);
        } else if (requestCode == REQUEST_STUDY_SESSION && resultCode == RESULT_OK) {
            // The ViewModel will automatically update when new sessions are added
            // via the repository callbacks
            viewModel.loadTaskData(taskId);
        }
    }

    // Static method to start this activity
    public static void start(AppCompatActivity activity, long taskId) {
        Intent intent = new Intent(activity, TaskDetailsActivity.class);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        activity.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh task data every time the activity comes to foreground
        if (taskId != -1) {
            viewModel.loadTaskData(taskId);
        }
    }
}