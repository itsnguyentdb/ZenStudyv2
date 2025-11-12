package com.example.zen_study.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zen_study.R;
import com.example.zen_study.activities.SaveResourceActivity;
import com.example.zen_study.adapters.ResourceLibraryAdapter;
import com.example.zen_study.models.FileMetadata;
import com.example.zen_study.models.Resource;
import com.example.zen_study.models.Task;
import com.example.zen_study.models.Subject;
import com.example.zen_study.viewmodels.ResourceLibraryViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ResourceLibraryFragment extends Fragment implements ResourceLibraryAdapter.OnResourceClickListener {

    private static final int PICK_FILE_REQUEST_CODE = 1001;
    private static final int CREATE_RESOURCE_REQUEST_CODE = 1002;
    private static final int EDIT_RESOURCE_REQUEST_CODE = 1003;
    private ResourceLibraryViewModel resourceViewModel;
    private ResourceLibraryAdapter resourceAdapter;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAddResource;
    private View emptyStateView;
    private Spinner spinnerTaskFilter;
    private Spinner spinnerSubjectFilter;
    private Spinner spinnerFileTypeFilter;
    private SearchView searchView;

    private List<Task> allTasks = new ArrayList<>();
    private List<Subject> allSubjects = new ArrayList<>();
    private Long selectedTaskId = null;
    private Long selectedSubjectId = null;
    private String selectedFileType = null;
    private String currentSearchQuery = "";

    public ResourceLibraryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resourceViewModel = new ViewModelProvider(this).get(ResourceLibraryViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_resource_library, container, false);
        initViews(view);
        setupRecyclerView();
        setupFilterSpinners();
        setupSearchView();
        setupObservers();
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewResources);
        fabAddResource = view.findViewById(R.id.fabAddResource);
        emptyStateView = view.findViewById(R.id.emptyStateView);
        spinnerTaskFilter = view.findViewById(R.id.spinnerTaskFilter);
        spinnerSubjectFilter = view.findViewById(R.id.spinnerSubjectFilter);
        spinnerFileTypeFilter = view.findViewById(R.id.spinnerFileTypeFilter);
        searchView = view.findViewById(R.id.searchView);

        fabAddResource.setOnClickListener(v -> startCreateResourceActivity());
    }

    private void setupRecyclerView() {
        resourceAdapter = new ResourceLibraryAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(resourceAdapter);
    }

    private void setupFilterSpinners() {
        // Setup task filter spinner
        ArrayAdapter<String> taskAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<String>()
        );
        taskAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTaskFilter.setAdapter(taskAdapter);

        // Setup subject filter spinner
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<String>()
        );
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubjectFilter.setAdapter(subjectAdapter);

        // Setup file type filter spinner
        ArrayAdapter<CharSequence> fileTypeAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.resource_types,
                android.R.layout.simple_spinner_item
        );
        fileTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFileTypeFilter.setAdapter(fileTypeAdapter);

        // Set up spinner listeners
        spinnerTaskFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedTaskId = null; // "All Tasks" selected
                } else {
                    selectedTaskId = allTasks.get(position - 1).getId();
                }
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTaskId = null;
            }
        });

        spinnerSubjectFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedSubjectId = null; // "All Subjects" selected
                } else {
                    selectedSubjectId = allSubjects.get(position - 1).getId();
                }
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedSubjectId = null;
            }
        });

        spinnerFileTypeFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedFileType = null; // "All Types" selected
                } else {
                    selectedFileType = parent.getItemAtPosition(position).toString();
                }
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedFileType = null;
            }
        });
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentSearchQuery = query;
                applyFilters();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText;
                applyFilters();
                return true;
            }
        });

        // Clear search when close button is clicked
        searchView.setOnCloseListener(() -> {
            currentSearchQuery = "";
            applyFilters();
            return false;
        });
    }

    private void setupObservers() {
        // Observe all resources
        resourceViewModel.getAllResources().observe(getViewLifecycleOwner(), resources -> {
            updateUI(resources);
        });

        // Observe tasks for filter
        resourceViewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
            allTasks = tasks != null ? tasks : new ArrayList<>();
            updateTaskFilterSpinner();
        });

        // Observe subjects for filter
        resourceViewModel.getAllSubjects().observe(getViewLifecycleOwner(), subjects -> {
            allSubjects = subjects != null ? subjects : new ArrayList<>();
            updateSubjectFilterSpinner();
        });

        // Observe operation results
        resourceViewModel.getOperationResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                if (result.isSuccess()) {
                    Toast.makeText(getContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Error: " + result.getMessage(), Toast.LENGTH_SHORT).show();
                }
                resourceViewModel.clearOperationResult();
            }
        });
    }

    private void updateTaskFilterSpinner() {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerTaskFilter.getAdapter();
        adapter.clear();
        adapter.add("All Tasks");

        for (Task task : allTasks) {
            adapter.add(task.getTitle());
        }
        adapter.notifyDataSetChanged();
    }

    private void updateSubjectFilterSpinner() {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerSubjectFilter.getAdapter();
        adapter.clear();
        adapter.add("All Subjects");

        for (Subject subject : allSubjects) {
            adapter.add(subject.getName());
        }
        adapter.notifyDataSetChanged();
    }

    private void applyFilters() {
        resourceViewModel.getFilteredResources(selectedTaskId, selectedSubjectId, selectedFileType, currentSearchQuery)
                .observe(getViewLifecycleOwner(), this::updateUI);
    }

    private void updateUI(List<Resource> resources) {
        resourceAdapter.updateResources(resources);

        if (resources.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);

            // Update empty state message based on active filters
            updateEmptyStateMessage();
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        }
    }

    private void updateEmptyStateMessage() {
        TextView emptyTitle = emptyStateView.findViewById(R.id.textEmptyTitle);
        TextView emptySubtitle = emptyStateView.findViewById(R.id.textEmptySubtitle);

        boolean hasActiveFilters = selectedTaskId != null || selectedSubjectId != null ||
                selectedFileType != null || !TextUtils.isEmpty(currentSearchQuery);

        if (hasActiveFilters) {
            emptyTitle.setText("No matching resources found");
            emptySubtitle.setText("Try adjusting your filters or search query");
            emptySubtitle.setVisibility(View.VISIBLE);
        } else {
            emptyTitle.setText("No Resources Yet");
            emptySubtitle.setText("Tap the + button to add your first resource");
            emptySubtitle.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_RESOURCE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(getContext(), "Resource created successfully", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == EDIT_RESOURCE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(getContext(), "Resource updated successfully", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startCreateResourceActivity() {
        Intent intent = new Intent(requireContext(), SaveResourceActivity.class);

        if (selectedTaskId != null) {
            intent.putExtra("taskId", selectedTaskId);
        }
        if (selectedSubjectId != null) {
            intent.putExtra("subjectId", selectedSubjectId);
        }

        startActivityForResult(intent, CREATE_RESOURCE_REQUEST_CODE);
    }

    // Resource click listeners
    @Override
    public void onResourceClick(Resource resource) {
        openResource(resource);
    }

    @Override
    public void onResourceEditClick(Resource resource) {
        startEditResourceActivity(resource);
    }

    @Override
    public void onResourceDeleteClick(Resource resource) {
        showDeleteConfirmationDialog(resource);
    }

    @Override
    public void onResourceInfoClick(Resource resource) {
        showResourceInfo(resource);
    }

    private void openResource(Resource resource) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, resource.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this resource: " + resource.getTitle());

        try {
            startActivity(Intent.createChooser(shareIntent, "Share Resource"));
        } catch (Exception e) {
            Toast.makeText(getContext(), "No sharing app available", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog(Resource resource) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Resource")
                .setMessage("Are you sure you want to delete \"" + resource.getTitle() + "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    resourceViewModel.deleteResource(resource.getId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startEditResourceActivity(Resource resource) {
        Intent intent = new Intent(requireContext(), SaveResourceActivity.class);

        // Pass the resource ID to trigger edit mode
        intent.putExtra("resourceId", resource.getId());

        // Optionally pass the current context for reference
        if (selectedTaskId != null) {
            intent.putExtra("taskId", selectedTaskId);
        }
        if (selectedSubjectId != null) {
            intent.putExtra("subjectId", selectedSubjectId);
        }

        startActivityForResult(intent, EDIT_RESOURCE_REQUEST_CODE);
    }

    private void showResourceInfo(Resource resource) {
        // Fetch additional data needed for the dialog
        Task task = null;
        Subject subject = null;
        FileMetadata fileMetadata = null; // You'll need to fetch this from your database

        if (resource.getTaskId() != null) {
            for (Task t : allTasks) {
                if (t.getId().equals(resource.getTaskId())) {
                    task = t;
                    break;
                }
            }
        }

        if (resource.getSubjectId() != null) {
            for (Subject s : allSubjects) {
                if (s.getId().equals(resource.getSubjectId())) {
                    subject = s;
                    break;
                }
            }
        }

        if (resource.getFileMetadataId() != null) {
            fileMetadata = resourceViewModel.getFileMetadata(resource.getFileMetadataId());
        }
        ResourceDetailsDialogFragment dialog = ResourceDetailsDialogFragment.newInstance(
                resource, task, subject, fileMetadata
        );
        dialog.setOnResourceActionListener(this);
        dialog.show(getParentFragmentManager(), "ResourceDetailDialog");
    }

    private String getResourceInfoText(Resource resource) {
        StringBuilder info = new StringBuilder();
        info.append("Title: ").append(resource.getTitle()).append("\n");
        info.append("Type: ").append(resource.getType()).append("\n");
        if (resource.getCreatedAt() != null) {
            info.append("Created: ").append(resource.getCreatedAt()).append("\n");
        }
        if (resource.getTaskId() != null) {
            info.append("Task ID: ").append(resource.getTaskId()).append("\n");
        }
        if (resource.getSubjectId() != null) {
            info.append("Subject ID: ").append(resource.getSubjectId()).append("\n");
        }
        return info.toString();
    }
}