package com.example.zen_study.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.zen_study.R;
import com.example.zen_study.models.Resource;
import com.example.zen_study.models.Task;
import com.example.zen_study.models.Subject;

import java.util.List;

import lombok.Setter;

public class ResourceInfoDialogFragment extends DialogFragment {

    public interface OnResourceInfoListener {
        void onResourceInfoConfirmed(String title, String type, Long taskId, Long subjectId, String description);
        void onResourceInfoCancelled();
        void onResourceDelete(Long resourceId);
    }

    private static final String ARG_FILE_NAME = "file_name";
    private static final String ARG_FILE_TYPE = "file_type";
    private static final String ARG_RESOURCE = "resource";
    private static final String ARG_IS_EDIT_MODE = "is_edit_mode";

    private OnResourceInfoListener listener;
    @Setter
    private List<Task> tasks;
    @Setter
    private List<Subject> subjects;
    @Setter
    private Long presetTaskId;
    private Long presetSubjectId;
    private String presetTaskTitle;
    private String presetSubjectName;

    private EditText editTitle;
    private Spinner spinnerType;
    private Spinner spinnerTask;
    private Spinner spinnerSubject;
    private EditText editDescription;
    private TextView textFileName;
    private TextView textContextHint;

    private String selectedType;
    private Long selectedTaskId;
    private Long selectedSubjectId;
    private boolean isEditMode = false;
    private Resource existingResource;

    private View dialogView; // Store the view reference

    public static ResourceInfoDialogFragment newInstance(String fileName, String fileType,
                                                         List<Task> tasks, List<Subject> subjects,
                                                         Long presetTaskId, Long presetSubjectId) {
        ResourceInfoDialogFragment fragment = new ResourceInfoDialogFragment();
        fragment.setTasks(tasks);
        fragment.setSubjects(subjects);
        fragment.setPresetTaskId(presetTaskId);
        fragment.setPresetSubjectId(presetSubjectId);
        Bundle args = new Bundle();
        args.putString(ARG_FILE_NAME, fileName);
        args.putString(ARG_FILE_TYPE, fileType);
        args.putBoolean(ARG_IS_EDIT_MODE, false);
        fragment.setArguments(args);
        return fragment;
    }

    public static ResourceInfoDialogFragment newInstanceForEdit(Resource resource,
                                                                List<Task> tasks,
                                                                List<Subject> subjects) {
        ResourceInfoDialogFragment fragment = new ResourceInfoDialogFragment();
        fragment.setTasks(tasks);
        fragment.setSubjects(subjects);
        Bundle args = new Bundle();
        args.putSerializable(ARG_RESOURCE, resource);
        args.putBoolean(ARG_IS_EDIT_MODE, true);
        fragment.setArguments(args);
        return fragment;
    }

    public void setPresetSubjectId(Long subjectId) {
        this.presetSubjectId = subjectId;
    }

    public void setPresetContext(String taskTitle, String subjectName) {
        this.presetTaskTitle = taskTitle;
        this.presetSubjectName = subjectName;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            isEditMode = args.getBoolean(ARG_IS_EDIT_MODE, false);
            if (isEditMode) {
                existingResource = (Resource) args.getSerializable(ARG_RESOURCE);
            }
        }

        try {
            listener = (OnResourceInfoListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException("Host activity must implement OnResourceInfoListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout and store the view reference
        dialogView = inflater.inflate(R.layout.dialog_resource_info, container, false);
        return dialogView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupSpinners();
        populateData(); // Now it's safe to populate data
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate the view here for the dialog
        dialogView = inflater.inflate(R.layout.dialog_resource_info, null);

        builder.setView(dialogView)
                .setTitle(getDialogTitle())
                .setPositiveButton(getPositiveButtonText(), null)
                .setNegativeButton("Cancel", (dialog, id) -> {
                    listener.onResourceInfoCancelled();
                });

        if (isEditMode) {
            builder.setNeutralButton("Delete", (dialog, which) -> {
                showDeleteConfirmation();
            });
        }

        AlertDialog dialog = builder.create();

        // Set up button listeners after dialog is shown
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> validateAndSubmit());

            // Initialize views and populate data here to ensure view is ready
            initViews(dialogView);
            setupSpinners();
            populateData();
        });

        return dialog;
    }

    private String getDialogTitle() {
        return isEditMode ? "Edit Resource" : "Add Resource Information";
    }

    private String getPositiveButtonText() {
        return isEditMode ? "Update Resource" : "Add Resource";
    }

    private void initViews(View view) {
        if (view == null) return;

        textFileName = view.findViewById(R.id.textFileName);
        editTitle = view.findViewById(R.id.editTitle);
        spinnerType = view.findViewById(R.id.spinnerType);
        spinnerTask = view.findViewById(R.id.spinnerTask);
        spinnerSubject = view.findViewById(R.id.spinnerSubject);
        editDescription = view.findViewById(R.id.editDescription);
        textContextHint = view.findViewById(R.id.textContextHint);
    }

    private void setupSpinners() {
        if (spinnerType == null || spinnerTask == null || spinnerSubject == null) return;

        // Type spinner
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.resource_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedType = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedType = "File";
            }
        });

        // Task spinner
        ArrayAdapter<String> taskAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item);
        taskAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskAdapter.add("No Task");
        if (tasks != null) {
            for (Task task : tasks) {
                taskAdapter.add(task.getTitle());
            }
        }
        spinnerTask.setAdapter(taskAdapter);

        spinnerTask.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTaskId = (position == 0) ? null : tasks.get(position - 1).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTaskId = null;
            }
        });

        // Subject spinner
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectAdapter.add("No Subject");
        if (subjects != null) {
            for (Subject subject : subjects) {
                subjectAdapter.add(subject.getName());
            }
        }
        spinnerSubject.setAdapter(subjectAdapter);

        spinnerSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSubjectId = (position == 0) ? null : subjects.get(position - 1).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedSubjectId = null;
            }
        });
    }

    private void populateData() {
        if (isEditMode) {
            populateEditData();
        } else {
            populateCreateData();
        }
    }

    private void populateCreateData() {
        Bundle args = getArguments();
        if (args == null || textFileName == null || editTitle == null) return;

        String fileName = args.getString(ARG_FILE_NAME);
        String fileType = args.getString(ARG_FILE_TYPE);

        textFileName.setText(fileName);
        editTitle.setText(getTitleFromFileName(fileName));
        setSpinnerSelection(spinnerType, mapFileTypeToResourceType(fileType));

        // Set preset values
        if (presetTaskId != null) {
            setSpinnerSelectionById(spinnerTask, presetTaskId, tasks);
        }
        if (presetSubjectId != null) {
            setSpinnerSelectionById(spinnerSubject, presetSubjectId, subjects);
        }

        // Show context hint
        if (presetTaskTitle != null || presetSubjectName != null) {
            showContextHint();
        }
    }

    private void populateEditData() {
        if (existingResource == null || textFileName == null) return;

        // Use the stored dialogView to find views
        if (dialogView != null) {
            TextView selectedFileLabel = dialogView.findViewById(R.id.textSelectedFileLabel);
            if (selectedFileLabel != null) {
                selectedFileLabel.setVisibility(View.GONE);
            }
        }

        textFileName.setVisibility(View.GONE);

        editTitle.setText(existingResource.getTitle());
        setSpinnerSelection(spinnerType, existingResource.getType());

        if (existingResource.getTaskId() != null) {
            setSpinnerSelectionById(spinnerTask, existingResource.getTaskId(), tasks);
        }
        if (existingResource.getSubjectId() != null) {
            setSpinnerSelectionById(spinnerSubject, existingResource.getSubjectId(), subjects);
        }
    }

    private void showContextHint() {
        if (textContextHint == null) return;

        StringBuilder context = new StringBuilder("This resource will be linked to: ");
        if (presetTaskTitle != null) {
            context.append("Task: ").append(presetTaskTitle);
        }
        if (presetSubjectName != null) {
            if (presetTaskTitle != null) context.append(", ");
            context.append("Subject: ").append(presetSubjectName);
        }
        textContextHint.setText(context.toString());
        textContextHint.setVisibility(View.VISIBLE);
    }

    private String getTitleFromFileName(String fileName) {
        if (fileName == null) return "";
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }

    private String mapFileTypeToResourceType(String fileType) {
        switch (fileType) {
            case "IMAGE": return "Image";
            case "VIDEO": return "Video";
            case "AUDIO": return "Audio";
            case "PDF": return "PDF";
            case "TEXT": return "Text";
            case "DOCUMENT": return "Document";
            default: return "File";
        }
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (spinner == null || value == null) return;

        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (adapter == null) return;

        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void setSpinnerSelectionById(Spinner spinner, Long id, List<?> items) {
        if (spinner == null || id == null || items == null) return;

        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (adapter == null) return;

        for (int i = 0; i < items.size(); i++) {
            Object item = items.get(i);
            Long itemId = null;

            if (item instanceof Task) {
                itemId = ((Task) item).getId();
            } else if (item instanceof Subject) {
                itemId = ((Subject) item).getId();
            }

            if (id.equals(itemId)) {
                spinner.setSelection(i + 1); // +1 for "No Task/Subject" option
                break;
            }
        }
    }

    private void validateAndSubmit() {
        if (editTitle == null) return;

        String title = editTitle.getText().toString().trim();

        if (title.isEmpty()) {
            editTitle.setError("Please enter a title");
            editTitle.requestFocus();
            return;
        }

        if (selectedType == null) {
            selectedType = "File";
        }

        String description = "";
        if (editDescription != null) {
            description = editDescription.getText().toString().trim();
        }

        listener.onResourceInfoConfirmed(title, selectedType, selectedTaskId, selectedSubjectId, description);
        dismiss();
    }

    private void showDeleteConfirmation() {
        if (existingResource == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Resource")
                .setMessage("Are you sure you want to delete \"" + existingResource.getTitle() + "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (listener != null) {
                        listener.onResourceDelete(existingResource.getId());
                    }
                    dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}