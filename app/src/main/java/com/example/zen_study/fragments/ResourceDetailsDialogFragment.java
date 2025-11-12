package com.example.zen_study.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.zen_study.R;
import com.example.zen_study.adapters.ResourceLibraryAdapter;
import com.example.zen_study.models.Resource;
import com.example.zen_study.models.Task;
import com.example.zen_study.models.Subject;
import com.example.zen_study.models.FileMetadata;
import com.example.zen_study.viewmodels.ResourceLibraryViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ResourceDetailsDialogFragment extends DialogFragment {

    private static final String ARG_RESOURCE = "resource";
    private static final String ARG_TASK = "task";
    private static final String ARG_SUBJECT = "subject";
    private static final String ARG_FILE_METADATA = "file_metadata";

    private Resource resource;
    private Task task;
    private Subject subject;
    private FileMetadata fileMetadata;

    private ResourceLibraryViewModel viewModel;

    // Views
    private ImageView iconType;
    private TextView textTitle;
    private TextView textType;
    private TextView textFileName;
    private TextView textFileSize;
    private TextView textDuration;
    private TextView textUploadDate;
    private TextView textTask;
    private TextView textSubject;
    private TextView textDescription;
    private Button buttonOpen;
    private Button buttonEdit;
    private Button buttonDelete;
    private Button buttonClose;

    private ResourceLibraryAdapter.OnResourceClickListener listener;

    public static ResourceDetailsDialogFragment newInstance(Resource resource, Task task, Subject subject, FileMetadata fileMetadata) {
        ResourceDetailsDialogFragment fragment = new ResourceDetailsDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_RESOURCE, resource);
        args.putSerializable(ARG_TASK, task);
        args.putSerializable(ARG_SUBJECT, subject);
        args.putSerializable(ARG_FILE_METADATA, fileMetadata);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnResourceActionListener(ResourceLibraryAdapter.OnResourceClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog);

        Bundle args = getArguments();
        if (args != null) {
            resource = (Resource) args.getSerializable(ARG_RESOURCE);
            task = (Task) args.getSerializable(ARG_TASK);
            subject = (Subject) args.getSerializable(ARG_SUBJECT);
            fileMetadata = (FileMetadata) args.getSerializable(ARG_FILE_METADATA);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_resource_detail, container, false);
        initViews(view);
        populateData(view);
        setupClickListeners();
        return view;
    }

    private void initViews(View view) {
        iconType = view.findViewById(R.id.iconType);
        textTitle = view.findViewById(R.id.textTitle);
        textType = view.findViewById(R.id.textType);
        textFileName = view.findViewById(R.id.textFileName);
        textFileSize = view.findViewById(R.id.textFileSize);
        textDuration = view.findViewById(R.id.textDuration);
        textUploadDate = view.findViewById(R.id.textUploadDate);
        textTask = view.findViewById(R.id.textTask);
        textSubject = view.findViewById(R.id.textSubject);
        textDescription = view.findViewById(R.id.textDescription);

        buttonOpen = view.findViewById(R.id.resource_details_button_open);
        buttonEdit = view.findViewById(R.id.resource_details_button_edit);
        buttonDelete = view.findViewById(R.id.resource_details_button_delete);
        buttonClose = view.findViewById(R.id.buttonClose);
    }

    private void populateData(View view) {
        if (resource == null) return;

        // Set basic resource info
        textTitle.setText(resource.getTitle());
        textType.setText(resource.getType());

        // Set file type icon
        setFileTypeIcon(resource.getType());

        // Set file metadata if available
        if (fileMetadata != null) {
            textFileName.setText(getFileNameFromPath(fileMetadata.getPath()));
            textFileSize.setText(formatFileSize(fileMetadata.getSize()));

            if (fileMetadata.getDuration() != null) {
                textDuration.setText(formatDuration(fileMetadata.getDuration()));
                textDuration.setVisibility(View.VISIBLE);
            } else {
                textDuration.setVisibility(View.GONE);
            }

            if (fileMetadata.getUploadedAt() != null) {
                textUploadDate.setText(formatDate(fileMetadata.getUploadedAt()));
            }
        } else {
            textFileName.setText("Unknown");
            textFileSize.setText("Unknown size");
            textDuration.setVisibility(View.GONE);
        }

        // Set task and subject info
        if (task != null) {
            textTask.setText(task.getTitle());
            textTask.setVisibility(View.VISIBLE);
        } else {
            textTask.setVisibility(View.GONE);
            view.findViewById(R.id.layoutTask).setVisibility(View.GONE);
        }

        if (subject != null) {
            textSubject.setText(subject.getName());
            textSubject.setVisibility(View.VISIBLE);
        } else {
            textSubject.setVisibility(View.GONE);
            view.findViewById(R.id.layoutSubject).setVisibility(View.GONE);
        }

        // Set creation date
        if (resource.getCreatedAt() != null) {
            TextView textCreatedAt = view.findViewById(R.id.textCreatedAt);
            textCreatedAt.setText(formatDate(resource.getCreatedAt()));
        }

        // Set description if available (you might need to add this field to your Resource model)
        if (textDescription != null) {
            // textDescription.setText(resource.getDescription());
            textDescription.setVisibility(View.GONE); // Hide if no description field
            view.findViewById(R.id.layoutDescription).setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        buttonOpen.setOnClickListener(v -> {
            if (listener != null) {
                listener.onResourceClick(resource);
            }
            dismiss();
        });

        buttonEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onResourceEditClick(resource);
            }
            dismiss();
        });

        buttonDelete.setOnClickListener(v -> {
            showDeleteConfirmation();
        });

        buttonClose.setOnClickListener(v -> dismiss());
    }

    private void setFileTypeIcon(String fileType) {
        int iconRes;
        switch (fileType != null ? fileType.toUpperCase() : "FILE") {
            case "IMAGE":
                iconRes = R.drawable.ic_image;
                break;
            case "VIDEO":
                iconRes = R.drawable.ic_video;
                break;
            case "AUDIO":
                iconRes = R.drawable.ic_audio;
                break;
            case "PDF":
                iconRes = R.drawable.ic_pdf;
                break;
            case "TEXT":
                iconRes = R.drawable.ic_text;
                break;
            case "DOCUMENT":
                iconRes = R.drawable.ic_file;
                break;
            case "PRESENTATION":
                iconRes = R.drawable.ic_file;
                break;
            case "SPREADSHEET":
                iconRes = R.drawable.ic_file;
                break;
            default:
                iconRes = R.drawable.ic_file;
                break;
        }
        iconType.setImageResource(iconRes);
    }

    private String getFileNameFromPath(String path) {
        if (path == null) return "Unknown";
        int lastSlash = path.lastIndexOf('/');
        return lastSlash != -1 ? path.substring(lastSlash + 1) : path;
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";

        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        return String.format(Locale.getDefault(), "%.1f %s",
                size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    private String formatDuration(long durationMs) {
        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds % 60);
        }
    }

    private String formatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
        return dateFormat.format(date);
    }

    private void showDeleteConfirmation() {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Resource")
                .setMessage("Are you sure you want to delete \"" + resource.getTitle() + "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (listener != null) {
                        listener.onResourceDeleteClick(resource);
                    }
                    dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }
}