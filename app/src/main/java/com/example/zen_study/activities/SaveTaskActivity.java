package com.example.zen_study.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zen_study.R;
import com.example.zen_study.models.Subject;
import com.example.zen_study.models.Task;
import com.example.zen_study.repositories.impls.SubjectRepositoryImpl;
import com.example.zen_study.repositories.impls.TaskRepositoryImpl;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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
    private Spinner spinnerStatus;
    private Spinner spinnerRepeat;
    private LinearProgressIndicator progressBar;
    private MaterialButton buttonDelete;
    private MaterialButton btnCancel;
    private MaterialButton btnSave;
    private View layoutProgress;
    private TextInputLayout textInputLayoutTitle;

    // Subject dropdown
    private TextInputLayout textInputLayoutSubject;
    private MaterialAutoCompleteTextView autoSubject;
    private ArrayAdapter<String> subjectAdapter;
    private final List<Subject> subjectList = new ArrayList<>();
    private final Map<String, Long> subjectNameToId = new HashMap<>();
    private long selectedSubjectId = -1L;
    private Long pendingSubjectIdForPrefill = null;

    private Calendar deadlineCalendar;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    private TaskRepositoryImpl taskRepository;
    private SubjectRepositoryImpl subjectRepository;
    private int currentMode = MODE_CREATE;
    private Task existingTask;
    private long taskId = -1;

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
        setupSubjectDropdown();
        setupDeadlinePicker();
        setupDeleteButton();
        setupActionButtons();

        // Handle back press using dispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleCancel();
            }
        });

        loadIntentData();
    }

    private void initViews() {
        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDescription = findViewById(R.id.edit_text_description);
        editTextDuration = findViewById(R.id.edit_text_duration);
        textDeadline = findViewById(R.id.text_deadline);
        textProgress = findViewById(R.id.text_progress);
        chipGroupPriority = findViewById(R.id.chip_group_priority);
        spinnerStatus = findViewById(R.id.spinner_status);
        spinnerRepeat = findViewById(R.id.spinner_repeat);
        progressBar = findViewById(R.id.progress_bar);
        buttonDelete = findViewById(R.id.button_delete);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);
        layoutProgress = findViewById(R.id.layout_progress);
        textInputLayoutTitle = findViewById(R.id.text_input_layout_title);

        textInputLayoutSubject = findViewById(R.id.text_input_layout_subject);
        autoSubject = findViewById(R.id.auto_subject);

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

    private void setupSubjectDropdown() {
        subjectAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                new ArrayList<>()
        );
        autoSubject.setAdapter(subjectAdapter);
        autoSubject.setThreshold(0); // show suggestions immediately when focused

        autoSubject.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = (String) parent.getItemAtPosition(position);
                Long subjId = subjectNameToId.get(name);
                selectedSubjectId = subjId != null ? subjId : -1L;
            }
        });

        // Update selectedSubjectId when user types exact match
        autoSubject.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String txt = s != null ? s.toString().trim() : "";
                Long subjId = subjectNameToId.get(txt);
                if (subjId != null) {
                    selectedSubjectId = subjId;
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Observe subjects and populate dropdown
        subjectRepository.getAllSubjects().observe(this, subjects -> {
            subjectList.clear();
            subjectNameToId.clear();
            subjectAdapter.clear();

            if (subjects != null) {
                subjectList.addAll(subjects);
                for (Subject subject : subjects) {
                    subjectNameToId.put(subject.getName(), subject.getId());
                    subjectAdapter.add(subject.getName());
                }
            }
            subjectAdapter.notifyDataSetChanged();

            // Prefill selection
            if (currentMode == MODE_EDIT && pendingSubjectIdForPrefill != null) {
                setSubjectSelectionById(pendingSubjectIdForPrefill);
                pendingSubjectIdForPrefill = null;
            } else if (currentMode == MODE_CREATE) {
                // Default to first subject if available
                if (!subjectList.isEmpty() && selectedSubjectId == -1L) {
                    Subject first = subjectList.get(0);
                    autoSubject.setText(first.getName(), false);
                    selectedSubjectId = first.getId();
                }
            }
        });

        // Clicking the field opens the dropdown list
        autoSubject.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                autoSubject.showDropDown();
            }
        });
        autoSubject.setOnClickListener(v -> autoSubject.showDropDown());
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

        // Set subject (prefill after subjects list is loaded)
        long subjId = existingTask.getSubjectId();
        if (!subjectList.isEmpty()) {
            setSubjectSelectionById(subjId);
        } else {
            pendingSubjectIdForPrefill = subjId;
        }
    }

    private void setSubjectSelectionById(long subjectId) {
        Optional<Subject> found = subjectList.stream().filter(s -> s.getId() == subjectId).findFirst();
        if (found.isPresent()) {
            autoSubject.setText(found.get().getName(), false);
            selectedSubjectId = found.get().getId();
        } else {
            // fallback: try repository lookup (synchronous allowed)
            Optional<Subject> fromDb = subjectRepository.findSubjectById(subjectId);
            if (fromDb.isPresent()) {
                autoSubject.setText(fromDb.get().getName(), false);
                selectedSubjectId = fromDb.get().getId();
            }
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
            // Compare subject by id
            if (selectedSubjectId != -1L && selectedSubjectId != existingTask.getSubjectId()) {
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
                .subjectId(selectedSubjectId)
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

        if (selectedSubjectId == -1L) {
            textInputLayoutSubject.setError("Please select a subject");
            autoSubject.requestFocus();
            autoSubject.showDropDown();
            return false;
        } else {
            textInputLayoutSubject.setError(null);
        }

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
}
