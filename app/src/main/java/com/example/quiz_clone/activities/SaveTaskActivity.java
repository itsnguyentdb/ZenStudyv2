package com.example.quiz_clone.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quiz_clone.R;
import com.example.quiz_clone.models.Subject;
import com.example.quiz_clone.models.Task;
import com.example.quiz_clone.repositories.impls.SubjectRepositoryImpl;
import com.example.quiz_clone.repositories.impls.TaskRepositoryImpl;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SaveTaskActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_MODE = "mode";
    public static final int MODE_CREATE = 0;
    public static final int MODE_EDIT = 1;

    private TextInputEditText editTextTitle;
    private TextInputEditText editTextDescription;
    private TextInputEditText editTextDuration;
    private TextView textDeadline;
    private TextView textProgress;
    private ChipGroup chipGroupPriority;
    private ChipGroup chipGroupSubject;
    private Spinner spinnerStatus;
    private Spinner spinnerRepeat;
    private LinearProgressIndicator progressBar;
    private MaterialButton buttonDelete;
    private MaterialButton btnCancel;
    private MaterialButton btnSave;
    private View layoutProgress;
    private TextInputLayout textInputLayoutTitle;

    private Calendar deadlineCalendar;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    private TaskRepositoryImpl taskRepository;
    private SubjectRepositoryImpl subjectRepository;
    private int currentMode = MODE_CREATE;
    private Task existingTask;
    private long taskId = -1;
    private String selectedSubjectName = "Other";
    private Subject selectedSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_task);

        taskRepository = new TaskRepositoryImpl(this);
        subjectRepository = new SubjectRepositoryImpl(this);
        initViews();
        setupToolbar();
        setupSpinners();
        setupPriorityChips();
        setupSubjectChips();
        setupDeadlinePicker();
        setupDeleteButton();
        setupActionButtons();
        loadIntentData();
    }

    private void initViews() {
        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDescription = findViewById(R.id.edit_text_description);
        editTextDuration = findViewById(R.id.edit_text_duration);
        textDeadline = findViewById(R.id.text_deadline);
        textProgress = findViewById(R.id.text_progress);
        chipGroupPriority = findViewById(R.id.chip_group_priority);
        chipGroupSubject = findViewById(R.id.chip_group_subject);
        spinnerStatus = findViewById(R.id.spinner_status);
        spinnerRepeat = findViewById(R.id.spinner_repeat);
        progressBar = findViewById(R.id.progress_bar);
        buttonDelete = findViewById(R.id.button_delete);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);
        layoutProgress = findViewById(R.id.layout_progress);
        textInputLayoutTitle = findViewById(R.id.text_input_layout_title);

        deadlineCalendar = Calendar.getInstance();
    }

    private void setupToolbar() {
//        setSupportActionBar(findViewById(R.id.toolbar));
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setDisplayShowHomeEnabled(true);
//        }
//
//        findViewById(R.id.toolbar).setNavigationOnClickListener(v -> handleCancel());
    }

    private void setupSpinners() {
        // Status spinner
        ArrayAdapter<Task.TaskType> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Task.TaskType.values()
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        // Repeat type spinner
        ArrayAdapter<Task.TaskRepeatType> repeatAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Task.TaskRepeatType.values()
        );
        repeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRepeat.setAdapter(repeatAdapter);
    }

    private void setupPriorityChips() {
        // Set single selection mode programmatically
        chipGroupPriority.setSingleSelection(true);

        chipGroupPriority.setOnCheckedStateChangeListener((group, checkedIds) -> {
            Log.d("SaveTaskActivity", "Priority chips checked: " + checkedIds);
            if (!checkedIds.isEmpty()) {
                int selectedId = checkedIds.get(0);
                Log.d("SaveTaskActivity", "Selected priority chip ID: " + selectedId);
            } else {
                Log.d("SaveTaskActivity", "No priority chip selected");
                // Ensure one is always selected by defaulting to medium
                chipGroupPriority.check(R.id.chip_priority_medium);
            }
        });
    }

    private void setupSubjectChips() {
        int[] chipIds = {
                R.id.chip_math, R.id.chip_science, R.id.chip_history,
                R.id.chip_language, R.id.chip_computer_science, R.id.chip_other
        };

        for (int chipId : chipIds) {
            Chip chip = findViewById(chipId);
            if (chip != null) {
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedSubjectName = chip.getText().toString();
                        // Uncheck other chips
                        for (int otherChipId : chipIds) {
                            if (otherChipId != chip.getId()) {
                                Chip otherChip = findViewById(otherChipId);
                                if (otherChip != null) {
                                    otherChip.setChecked(false);
                                }
                            }
                        }
                    }
                });
            }
        }

        // Set default selection
        Chip chipOther = findViewById(R.id.chip_other);
        if (chipOther != null) {
            chipOther.setChecked(true);
        }
    }

    private void setupDeadlinePicker() {
        findViewById(R.id.button_set_deadline).setOnClickListener(v -> showDatePickerDialog());
        findViewById(R.id.button_clear_deadline).setOnClickListener(v -> clearDeadline());
    }

    private void setupDeleteButton() {
        buttonDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void setupActionButtons() {
        btnCancel.setOnClickListener(v -> handleCancel());
        btnSave.setOnClickListener(v -> saveTask());
    }

    private void loadIntentData() {
        Intent intent = getIntent();
        currentMode = intent.getIntExtra(EXTRA_MODE, MODE_CREATE);
        taskId = intent.getLongExtra(EXTRA_TASK_ID, -1);
        if (currentMode == MODE_EDIT && taskId != -1) {
            loadTaskForEditing();
        } else {
            setupCreateMode();
        }
    }

    private void loadTaskForEditing() {
        Log.d("SaveTaskActivity", "Loading task with ID: " + taskId);

        taskRepository.getTaskById(taskId, new TaskRepositoryImpl.OnTaskOperationComplete() {
            @Override
            public void onSuccess(Task task) {
                runOnUiThread(() -> {
                    Log.d("SaveTaskActivity", "Task loaded successfully: " + (task != null));
                    if (task != null) {
                        Log.d("SaveTaskActivity", "Task title: " + task.getTitle());
                        existingTask = task;
                        populateFormWithTaskData();
                        setupEditMode();
                    } else {
                        Toast.makeText(SaveTaskActivity.this, "Task not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Log.e("SaveTaskActivity", "Error loading task: " + e.getMessage());
                    Toast.makeText(SaveTaskActivity.this, "Error loading task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void populateFormWithTaskData() {
        System.out.println("Populating: " + existingTask);
        if (existingTask == null) return;

        editTextTitle.setText(existingTask.getTitle());
        editTextDescription.setText(existingTask.getDescription());
        editTextDuration.setText(String.valueOf(existingTask.getExpectedDuration()));

        // Set deadline
        if (existingTask.getDeadline() != null) {
            deadlineCalendar.setTime(existingTask.getDeadline());
            updateDeadlineText();
            findViewById(R.id.button_clear_deadline).setVisibility(View.VISIBLE);
        }

        // Set priority
        setPriorityChip(existingTask.getPriority());

        // Set status
        setSpinnerSelection(spinnerStatus, existingTask.getStatus());

        // Set repeat type
        setSpinnerSelection(spinnerRepeat, existingTask.getRepeatType());

        // Set progress
        int progress = (int) (existingTask.getProgress() * 100);
        progressBar.setProgress(progress);
        textProgress.setText(progress + "%");

        // Set subject
        setSubjectChip(existingTask.getSubjectId());
    }

    private void setSubjectChip(long subjectId) {
        // In a real app, you'd fetch the subject name from the database
        // For now, we'll map common subject IDs to names
        String subjectName = getSubjectNameById(subjectId);
        int chipId = getChipIdForSubject(subjectName);

        if (chipId != -1) {
            Chip chip = findViewById(chipId);
            if (chip != null) {
                chip.setChecked(true);
                selectedSubjectName = subjectName;
            }
        }
    }

    private String getSubjectNameById(long subjectId) {
        // This would normally come from your database
        // For now, using a simple mapping
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

    private int getChipIdForSubject(String subjectName) {
        switch (subjectName) {
            case "Math":
                return R.id.chip_math;
            case "Science":
                return R.id.chip_science;
            case "History":
                return R.id.chip_history;
            case "Language":
                return R.id.chip_language;
            case "Computer Science":
                return R.id.chip_computer_science;
            case "Other":
                return R.id.chip_other;
            default:
                return -1;
        }
    }

    private void setPriorityChip(int priority) {
        Log.d("SaveTaskActivity", "Setting priority chip to: " + priority);
        switch (priority) {
            case 1:
                chipGroupPriority.check(R.id.chip_priority_low);
                break;
            case 3:
                chipGroupPriority.check(R.id.chip_priority_high);
                break;
            default:
                chipGroupPriority.check(R.id.chip_priority_medium);
        }

        // Verify the selection was applied
        int checkedId = chipGroupPriority.getCheckedChipId();
        Log.d("SaveTaskActivity", "After setting, checked chip ID: " + checkedId);
    }


    private <T> void setSpinnerSelection(Spinner spinner, T value) {
        ArrayAdapter<T> adapter = (ArrayAdapter<T>) spinner.getAdapter();
        if (adapter != null) {
            int position = adapter.getPosition(value);
            if (position >= 0) {
                spinner.setSelection(position);
            }
        }
    }

    private void setupCreateMode() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Create Task");
        }

        // Set default values for create mode
        spinnerStatus.setSelection(0); // TODO
        spinnerRepeat.setSelection(0); // NONE
        findViewById(R.id.chip_priority_medium).performClick();

        // Hide edit-only elements
        layoutProgress.setVisibility(View.GONE);
        buttonDelete.setVisibility(View.GONE);
    }

    private void setupEditMode() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Edit Task");
        }

        // Show edit-only elements
        layoutProgress.setVisibility(View.VISIBLE);
        buttonDelete.setVisibility(View.VISIBLE);
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    deadlineCalendar.set(Calendar.YEAR, year);
                    deadlineCalendar.set(Calendar.MONTH, month);
                    deadlineCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDeadlineText();
                    findViewById(R.id.button_clear_deadline).setVisibility(View.VISIBLE);
                },
                deadlineCalendar.get(Calendar.YEAR),
                deadlineCalendar.get(Calendar.MONTH),
                deadlineCalendar.get(Calendar.DAY_OF_MONTH)
        );

        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void clearDeadline() {
        deadlineCalendar = Calendar.getInstance();
        textDeadline.setText("Not set");
        findViewById(R.id.button_clear_deadline).setVisibility(View.GONE);
    }

    private void updateDeadlineText() {
        textDeadline.setText(dateFormat.format(deadlineCalendar.getTime()));
    }

    private void handleCancel() {
        if (hasUnsavedChanges()) {
            showUnsavedChangesDialog();
        } else {
            finish();
        }
    }

    private boolean hasUnsavedChanges() {
        String title = editTextTitle.getText() != null ? editTextTitle.getText().toString().trim() : "";
        String description = editTextDescription.getText() != null ? editTextDescription.getText().toString().trim() : "";

        if (currentMode == MODE_EDIT && existingTask != null) {
            if (!title.equals(existingTask.getTitle()) ||
                    !description.equals(existingTask.getDescription())) {
                return true;
            }
            // Add subject comparison
            if (!selectedSubjectName.equals(getSubjectNameById(existingTask.getSubjectId()))) {
                return true;
            }
            return false;
        } else {
            return !TextUtils.isEmpty(title) || !TextUtils.isEmpty(description);
        }
    }

    private void showUnsavedChangesDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Unsaved Changes")
                .setMessage("You have unsaved changes. Are you sure you want to discard them?")
                .setPositiveButton("Discard", (dialog, which) -> finish())
                .setNegativeButton("Keep Editing", null)
                .show();
    }

    private int getSelectedPriority() {
        int checkedChipId = chipGroupPriority.getCheckedChipId();
        Log.d("SaveTaskActivity", "Getting priority - Checked Chip ID: " + checkedChipId);

        if (checkedChipId == R.id.chip_priority_low) {
            Log.d("SaveTaskActivity", "Priority: LOW (1)");
            return 1;
        }
        if (checkedChipId == R.id.chip_priority_medium) {
            Log.d("SaveTaskActivity", "Priority: MEDIUM (2)");
            return 2;
        }
        if (checkedChipId == R.id.chip_priority_high) {
            Log.d("SaveTaskActivity", "Priority: HIGH (3)");
            return 3;
        }

        // Fallback: if no chip is selected, default to medium and select it
        Log.w("SaveTaskActivity", "No priority selected, defaulting to MEDIUM (2)");
        chipGroupPriority.check(R.id.chip_priority_medium);
        return 2;
    }

    private long getSelectedSubjectId() {
        // In a real app, you'd get this from the database
        // For now, using a simple mapping
        switch (selectedSubjectName) {
            case "Math":
                return 1;
            case "Science":
                return 2;
            case "History":
                return 3;
            case "Language":
                return 4;
            case "Computer Science":
                return 5;
            default:
                return 6; // Other
        }
    }

    private Task createTaskFromInput() {
        String title = editTextTitle.getText() != null ? editTextTitle.getText().toString().trim() : "";
        String description = editTextDescription.getText() != null ? editTextDescription.getText().toString().trim() : "";
        String durationText = editTextDuration.getText() != null ? editTextDuration.getText().toString().trim() : "60";

        int duration = 60;
        try {
            duration = Integer.parseInt(durationText);
        } catch (NumberFormatException e) {
            duration = 60;
        }

        Date deadline = null;
        if (!textDeadline.getText().toString().equals("Not set")) {
            deadline = deadlineCalendar.getTime();
        }

        Task.TaskBuilder builder = Task.builder()
                .title(title)
                .description(description)
                .deadline(deadline)
                .priority(getSelectedPriority())
                .expectedDuration(duration)
                .repeatType((Task.TaskRepeatType) spinnerRepeat.getSelectedItem())
                .status((Task.TaskType) spinnerStatus.getSelectedItem())
                .subjectId(getSelectedSubjectId())
                .parentTaskId(0); // You might want to handle parent task selection

        if (currentMode == MODE_EDIT && existingTask != null) {
            // Preserve existing values for edit mode
            builder
                    .progressDuration(existingTask.getProgressDuration())
                    .progress(existingTask.getProgress())
                    .level(existingTask.getLevel())
                    .createdAt(existingTask.getCreatedAt())
                    .lastUpdatedAt(new Date())
                    .id(existingTask.getId());
        } else {
            // Set defaults for create mode
            builder.progressDuration(0)
                    .progress(0.0f)
                    .level(0)
                    .createdAt(new Date())
                    .lastUpdatedAt(new Date());
        }

        return builder.build();
    }

    private boolean validateInput() {
        String title = editTextTitle.getText() != null ? editTextTitle.getText().toString().trim() : "";

        if (title.isEmpty()) {
            textInputLayoutTitle.setError("Task title is required");
            editTextTitle.requestFocus();
            return false;
        }

        textInputLayoutTitle.setError(null);
        return true;
    }

    private void saveTask() {
        if (!validateInput()) {
            return;
        }

        Task task = createTaskFromInput();

        TaskRepositoryImpl.OnTaskOperationComplete callback = new TaskRepositoryImpl.OnTaskOperationComplete() {
            @Override
            public void onSuccess(Task task) {
                runOnUiThread(() -> {
                    setResult(RESULT_OK);
                    System.out.println("Saved task: " + task);
                    finish();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(SaveTaskActivity.this, "Error saving task: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        };

        if (currentMode == MODE_EDIT) {
            taskRepository.updateTask(task, callback);
        } else {
            taskRepository.insertTask(task, callback);
        }
    }

    private void deleteTask() {
        if (existingTask != null) {
            taskRepository.deleteTaskWithSubtasks(existingTask.getId());
            setResult(RESULT_OK);
            finish();
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteTask())
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handleCancel();
    }

}