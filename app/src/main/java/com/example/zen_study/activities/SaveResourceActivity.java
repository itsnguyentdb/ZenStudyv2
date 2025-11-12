package com.example.zen_study.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.zen_study.R;
import com.example.zen_study.fragments.ResourceInfoDialogFragment;
import com.example.zen_study.models.Resource;
import com.example.zen_study.models.Task;
import com.example.zen_study.models.Subject;
import com.example.zen_study.viewmodels.SaveResourceViewModel;

import java.util.ArrayList;
import java.util.List;

public class SaveResourceActivity extends AppCompatActivity
        implements ResourceInfoDialogFragment.OnResourceInfoListener {

    private static final int PICK_FILE_REQUEST_CODE = 1001;

    private SaveResourceViewModel resourceViewModel;
    private Uri selectedFileUri;
    private String selectedFileName;
    private String selectedFileType;

    // Mode and existing resource data
    private boolean isEditMode = false;
    private Long existingResourceId;
    private Resource existingResource;

    // Optional preset task/subject when starting from specific contexts
    private Long presetTaskId;
    private Long presetSubjectId;
    private String presetTaskTitle;
    private String presetSubjectName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_resource);

        // Initialize ViewModel
        resourceViewModel = new ViewModelProvider(this).get(SaveResourceViewModel.class);

        // Get intent extras and determine mode
        handleIntentExtras();

        // Setup observers
        setupObservers();

        // Initialize based on mode
        if (isEditMode) {
            setupEditMode();
        } else {
            setupCreateMode();
        }
    }

    private void handleIntentExtras() {
        Intent intent = getIntent();
        if (intent != null) {
            // Check if we're in edit mode
            if (intent.hasExtra("resourceId")) {
                isEditMode = true;
                existingResourceId = intent.getLongExtra("resourceId", -1);
                if (existingResourceId == -1) {
                    finishWithError("Invalid resource ID");
                    return;
                }
            }

            // Get preset context for create mode
            if (intent.hasExtra("taskId")) {
                presetTaskId = intent.getLongExtra("taskId", -1);
                if (presetTaskId == -1) presetTaskId = null;
                presetTaskTitle = intent.getStringExtra("taskTitle");
            }
            if (intent.hasExtra("subjectId")) {
                presetSubjectId = intent.getLongExtra("subjectId", -1);
                if (presetSubjectId == -1) presetSubjectId = null;
                presetSubjectName = intent.getStringExtra("subjectName");
            }
        }
    }

    private void setupObservers() {
        // Observe loading state
        resourceViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                showLoadingState(resourceViewModel.getLoadingMessage().getValue());
            } else {
                hideLoadingState();
            }
        });

        // Observe specific resource for edit mode
        if (isEditMode) {
            resourceViewModel.getCurrentResource().observe(this, resourceOpt -> {
                if (resourceOpt != null && resourceOpt.isPresent()) {
                    existingResource = resourceOpt.get();
                    showEditDialog();
                    resourceViewModel.setLoading(false, "");
                } else if (resourceOpt != null) {
                    finishWithError("Resource not found");
                }
            });
        }

        // Observe operation results
        resourceViewModel.getOperationResult().observe(this, result -> {
            if (result != null) {
                handleOperationResult(result);
            }
        });

        // Observe tasks and subjects for dropdowns
        resourceViewModel.getAllTasks().observe(this, tasks -> {
            // Data loaded, ready for dialog
        });

        resourceViewModel.getAllSubjects().observe(this, subjects -> {
            // Data loaded, ready for dialog
        });
    }

    private void setupCreateMode() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Add New Resource");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        openFilePicker();
    }

    private void setupEditMode() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Edit Resource");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        resourceViewModel.loadResourceForEdit(existingResourceId);
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        String[] mimeTypes = {
                "image/*", "video/*", "audio/*",
                "application/pdf", "text/*",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select a file"), PICK_FILE_REQUEST_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                selectedFileUri = data.getData();
                selectedFileName = getFileNameFromUri(selectedFileUri);
                selectedFileType = getFileTypeFromUri(selectedFileUri);
                showCreateDialog();
            } else {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        }
    }

    private void showCreateDialog() {
        resourceViewModel.getAllTasks().observe(this, tasks -> {
            resourceViewModel.getAllSubjects().observe(this, subjects -> {
                ResourceInfoDialogFragment dialog = ResourceInfoDialogFragment.newInstance(
                        selectedFileName,
                        selectedFileType,
                        tasks != null ? tasks : new ArrayList<>(),
                        subjects != null ? subjects : new ArrayList<>(),
                        presetTaskId,
                        presetSubjectId
                );

                if (presetTaskTitle != null || presetSubjectName != null) {
                    dialog.setPresetContext(presetTaskTitle, presetSubjectName);
                }

                dialog.show(getSupportFragmentManager(), "ResourceInfoDialog");
            });
        });
    }

    private void showEditDialog() {
        resourceViewModel.getAllTasks().observe(this, tasks -> {
            resourceViewModel.getAllSubjects().observe(this, subjects -> {
                ResourceInfoDialogFragment dialog = ResourceInfoDialogFragment.newInstanceForEdit(
                        existingResource,
                        tasks != null ? tasks : new ArrayList<>(),
                        subjects != null ? subjects : new ArrayList<>()
                );
                dialog.show(getSupportFragmentManager(), "ResourceInfoDialog");
            });
        });
    }

    @Override
    public void onResourceInfoConfirmed(String title, String type, Long taskId, Long subjectId, String description) {
        if (isEditMode) {
            updateResource(title, type, taskId, subjectId);
        } else {
            createResource(title, type, taskId, subjectId);
        }
    }

    @Override
    public void onResourceInfoCancelled() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    @Override
    public void onResourceDelete(Long resourceId) {
        resourceViewModel.deleteResource(resourceId);
    }

    private void createResource(String title, String type, Long taskId, Long subjectId) {
        Long finalTaskId = taskId != null ? taskId : presetTaskId;
        Long finalSubjectId = subjectId != null ? subjectId : presetSubjectId;
        resourceViewModel.createResource(selectedFileUri, finalTaskId, finalSubjectId, title, type);
    }

    private void updateResource(String title, String type, Long taskId, Long subjectId) {
        resourceViewModel.updateResource(existingResourceId, title, type, taskId, subjectId);
    }

    private void handleOperationResult(SaveResourceViewModel.OperationResult result) {
        if (result.isSuccess()) {
            setResult(Activity.RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, result.getMessage(), Toast.LENGTH_LONG).show();
        }
        resourceViewModel.clearOperationResult();
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (fileName == null) {
            fileName = uri.getLastPathSegment();
        }
        return fileName;
    }

    private String getFileTypeFromUri(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        if (mimeType != null) {
            if (mimeType.startsWith("image")) return "IMAGE";
            else if (mimeType.startsWith("video")) return "VIDEO";
            else if (mimeType.startsWith("audio")) return "AUDIO";
            else if (mimeType.equals("application/pdf")) return "PDF";
            else if (mimeType.startsWith("text")) return "TEXT";
            else if (mimeType.contains("word") || mimeType.contains("document")) return "DOCUMENT";
        }
        return "FILE";
    }

    private void showLoadingState(String message) {
        // Implement your loading UI
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void hideLoadingState() {
        // Hide loading UI
    }

    private void finishWithError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        setResult(Activity.RESULT_CANCELED);
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }
}