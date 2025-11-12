package com.example.zen_study.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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
    private static final String ARG_TASKS = "tasks";
    private static final String ARG_SUBJECTS = "subjects";
    private static final String ARG_PRESET_TASK_ID = "preset_task_id";
    private static final String ARG_PRESET_SUBJECT_ID = "preset_subject_id";

    private OnResourceInfoListener listener;
    private List<Task> tasks;
    private List<Subject> subjects;
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

    public static ResourceInfoDialogFragment newInstance(String fileName, String fileType,
                                                         List<Task> tasks, List<Subject> subjects,
                                                         Long presetTaskId, Long presetSubjectId) {
        ResourceInfoDialogFragment fragment = new ResourceInfoDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILE_NAME, fileName);
        args.putString(ARG_FILE_TYPE, fileType);
        args.putBoolean(ARG_IS_EDIT_MODE, false);
        args.putSerializable(ARG_TASKS, new java.util.ArrayList<>(tasks));
        args.putSerializable(ARG_SUBJECTS, new java.util.ArrayList<>(subjects));
        if (presetTaskId != null) args.putLong(ARG_PRESET_TASK_ID, presetTaskId);
        if (presetSubjectId != null) args.putLong(ARG_PRESET_SUBJECT_ID, presetSubjectId);
        fragment.setArguments(args);
        return fragment;
    }

    public static ResourceInfoDialogFragment newInstanceForEdit(Resource resource,
                                                                List<Task> tasks,
                                                                List<Subject> subjects) {
        ResourceInfoDialogFragment fragment = new ResourceInfoDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_RESOURCE, resource);
        args.putBoolean(ARG_IS_EDIT_MODE, true);
        args.putSerializable(ARG_TASKS, new java.util.ArrayList<>(tasks));
        args.putSerializable(ARG_SUBJECTS, new java.util.ArrayList<>(subjects));
        fragment.setArguments(args);
        return fragment;
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
            tasks = (List<Task>) args.getSerializable(ARG_TASKS);
            subjects = (List<Subject>) args.getSerializable(ARG_SUBJECTS);
            presetTaskId = args.containsKey(ARG_PRESET_TASK_ID) ? args.getLong(ARG_PRESET_TASK_ID) : null;
            presetSubjectId = args.containsKey(ARG_PRESET_SUBJECT_ID) ? args.getLong(ARG_PRESET_SUBJECT_ID) : null;
        }

        try {
            listener = (OnResourceInfoListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException("Host activity must implement OnResourceInfoListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialog_resource_info, null);

        initViews(dialogView);
        setupSpinners();
        populateData();

        builder.setView(dialogView)
                .setTitle(getDialogTitle())
                .setPositiveButton(getPositiveButtonText(), null)
                .setNegativeButton("Cancel", (dialog, id) -> {
                    if (listener != null) {
                        listener.onResourceInfoCancelled();
                    }
                });

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> validateAndSubmit());
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
        textFileName = view.findViewById(R.id.textFileName);
        editTitle = view.findViewById(R.id.editTitle);
        spinnerType = view.findViewById(R.id.spinnerType);
        spinnerTask = view.findViewById(R.id.spinnerTask);
        spinnerSubject = view.findViewById(R.id.spinnerSubject);
        editDescription = view.findViewById(R.id.editDescription);
        textContextHint = view.findViewById(R.id.textContextHint);
    }

    private void setupSpinners() {
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
                selectedTaskId = (position == 0) ? null : (tasks != null && position - 1 < tasks.size() ? tasks.get(position - 1).getId() : null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTaskId = null;
            }
        });

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
                selectedSubjectId = (position == 0) ? null : (subjects != null && position - 1 < subjects.size() ? subjects.get(position - 1).getId() : null);
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
        if (args == null) return;

        String fileName = args.getString(ARG_FILE_NAME);
        String fileType = args.getString(ARG_FILE_TYPE);

        if (textFileName != null) {
            textFileName.setText(fileName);
        }
        if (editTitle != null) {
            editTitle.setText(getTitleFromFileName(fileName));
        }
        setSpinnerSelection(spinnerType, mapFileTypeToResourceType(fileType));

        // Set preset values
        if (presetTaskId != null) {
            setSpinnerSelectionById(spinnerTask, presetTaskId, tasks);
        }
        if (presetSubjectId != null) {
            setSpinnerSelectionById(spinnerSubject, presetSubjectId, subjects);
        }

        // Show context hint
        if ((presetTaskTitle != null || presetSubjectName != null) && textContextHint != null) {
            showContextHint();
        }
    }

    private void populateEditData() {
        if (existingResource == null) return;

        // Hide file name for edit mode
        if (textFileName != null) {
            textFileName.setVisibility(View.GONE);
        }

        if (editTitle != null) {
            editTitle.setText(existingResource.getTitle());
        }

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
            case "IMAGE":
                return "Image";
            case "VIDEO":
                return "Video";
            case "AUDIO":
                return "Audio";
            case "PDF":
                return "PDF";
            case "TEXT":
                return "Text";
            case "DOCUMENT":
                return "Document";
            default:
                return "File";
        }
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (spinner == null || value == null) return;

        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (adapter == null) return;

        for (int i = 0; i < adapter.getCount(); i++) {
            if (value.equalsIgnoreCase(adapter.getItem(i).toString())) {
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
                spinner.setSelection(i + 1);
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

        if (listener != null) {
            listener.onResourceInfoConfirmed(title, selectedType, selectedTaskId, selectedSubjectId, description);
        }
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

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        if (listener != null) {
            listener.onResourceInfoCancelled();
        }
    }
}