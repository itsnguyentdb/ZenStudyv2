package com.example.zen_study.fragments;

import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.zen_study.R;
import com.example.zen_study.activities.SaveTaskActivity;
import com.example.zen_study.activities.TaskDetailsActivity;
import com.example.zen_study.adapters.TaskLibraryAdapter;
import com.example.zen_study.models.Task;
import com.example.zen_study.viewmodels.TaskLibraryViewModel;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class TaskLibraryFragment extends Fragment implements TaskLibraryAdapter.OnTaskClickListener {

    private TaskLibraryViewModel viewModel;
    private TaskLibraryAdapter taskAdapter;
    private RecyclerView tasksRecyclerView;
    private View emptyStateView;
    private EditText searchEditText;
    private ChipGroup filterChipGroup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_library, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(TaskLibraryViewModel.class);
        initViews(view);
        setupRecyclerView();
        setupSearch();
        setupFilterChips();
        setupObservers();
        setupFab(view);
    }

    private void initViews(View view) {
        tasksRecyclerView = view.findViewById(R.id.recycler_view_tasks);
        emptyStateView = view.findViewById(R.id.layout_empty_state);
        searchEditText = view.findViewById(R.id.edit_text_search);
        filterChipGroup = view.findViewById(R.id.task_library_filter_chips);
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskLibraryAdapter(new ArrayList<>(), this);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tasksRecyclerView.setAdapter(taskAdapter);
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTasks(s.toString(), getSelectedFilter());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupFilterChips() {
        filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            filterTasks(searchEditText.getText().toString(), getSelectedFilter());
        });
    }

    private String getSelectedFilter() {
        int checkedChipId = filterChipGroup.getCheckedChipId();

        if (checkedChipId == R.id.chip_todo) return "TODO";
        if (checkedChipId == R.id.chip_in_progress) return "IN_PROGRESS";
        if (checkedChipId == R.id.chip_completed) return "COMPLETED";
        return "ALL";
    }

    private void filterTasks(String searchQuery, String filter) {
        // Remove the observer inside filterTasks - this creates multiple observers
        List<Task> filteredTasks = new ArrayList<>();

        // Get the current tasks from ViewModel (if you have a way to get them without observer)
        // Or better, use the tasks that are already being observed in setupObservers
        if (viewModel.getAllTasks().getValue() != null) {
            List<Task> allTasks = viewModel.getAllTasks().getValue();

            for (Task task : allTasks) {
                boolean matchesSearch = searchQuery.isEmpty() ||
                        task.getTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                        (task.getDescription() != null &&
                                task.getDescription().toLowerCase().contains(searchQuery.toLowerCase()));

                boolean matchesStatus = filter.equals("ALL") ||
                        task.getStatus().name().equals(filter);

                if (matchesSearch && matchesStatus) {
                    filteredTasks.add(task);
                }
            }
        }

        taskAdapter.updateTasks(filteredTasks);
        updateEmptyState(filteredTasks.isEmpty());
    }

    private void setupObservers() {
        viewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) {
                // Apply current filters whenever the underlying data changes
                filterTasks(searchEditText.getText().toString(), getSelectedFilter());
            } else {
                // Handle empty case
                taskAdapter.updateTasks(new ArrayList<>());
                updateEmptyState(true);
            }
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            emptyStateView.setVisibility(View.VISIBLE);
            tasksRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateView.setVisibility(View.GONE);
            tasksRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void setupFab(View view) {
        FloatingActionButton fab = view.findViewById(R.id.fab_add_task);
        fab.setOnClickListener(v -> {
            var intent = new Intent(getActivity(), SaveTaskActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onTaskClick(Task task) {
        var intent = new Intent(getActivity(), TaskDetailsActivity.class);
        intent.putExtra(TaskDetailsActivity.EXTRA_TASK_ID, task.getId());
        startActivity(intent);
    }

    @Override
    public void onTaskEdit(Task task) {
        // Navigate to edit task screen
        Bundle args = new Bundle();
        args.putLong("taskId", task.getId());
//        Navigation.findNavController(requireView()).navigate(
//                R.id.action_taskLibraryFragment_to_editTaskFragment,
//                args
//        );
    }

    @Override
    public void onTaskDelete(Task task) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete \"" + task.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteTask(task);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onTaskStatusChange(Task task, Task.TaskType newStatus) {
        task.setStatus(newStatus);
        viewModel.updateTask(task);
    }

    private void refreshTaskList() {
        // Simply trigger the filter with current search and filter states
        filterTasks(searchEditText.getText().toString(), getSelectedFilter());
    }

    @Override
    public void onResume() {
        super.onResume();
        // Force refresh from ViewModel
        if (viewModel != null) {
            // This will trigger the observer in setupObservers
            viewModel.getAllTasks(); // Add this method to your ViewModel if needed
        }
    }

}