package com.example.zen_study.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zen_study.R;
import com.example.zen_study.adapters.SubjectLibraryAdapter;
import com.example.zen_study.models.Subject;
import com.example.zen_study.viewmodels.SubjectLibraryViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SubjectLibraryFragment extends Fragment implements SubjectLibraryAdapter.SubjectActionListener {

    private SubjectLibraryViewModel viewModel;
    private SubjectLibraryAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyStateView;
    private EditText searchEditText;
    private ImageButton addButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subject_library, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SubjectLibraryViewModel.class);
        initViews(view);
        setupRecyclerView();
        setupObservers();
        setupSearch();
        setupAddButton();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_subjects);
        emptyStateView = view.findViewById(R.id.layout_empty_state);
        searchEditText = view.findViewById(R.id.edit_text_search_subject);
        addButton = view.findViewById(R.id.button_add_subject);
    }

    private void setupRecyclerView() {
        adapter = new SubjectLibraryAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getAllSubjects().observe(getViewLifecycleOwner(), subjects -> {
            applyFilter(searchEditText.getText().toString(), subjects);
        });
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                var subjects = viewModel.getAllSubjects().getValue();
                applyFilter(s.toString(), subjects);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupAddButton() {
        addButton.setOnClickListener(v -> showAddOrEditDialog(null));
    }

    private void applyFilter(String query, List<Subject> subjects) {
        if (subjects == null) subjects = new ArrayList<>();
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<Subject> filtered = new ArrayList<>();
        for (Subject s : subjects) {
            if (q.isEmpty() || (s.getName() != null && s.getName().toLowerCase(Locale.ROOT).contains(q))) {
                filtered.add(s);
            }
        }
        adapter.submitList(filtered);
        updateEmptyState(filtered.isEmpty());
    }

    private void updateEmptyState(boolean isEmpty) {
        emptyStateView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void showAddOrEditDialog(@Nullable Subject toEdit) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_subject_edit, null);
        EditText nameInput = dialogView.findViewById(R.id.edit_subject_name);
        EditText descInput = dialogView.findViewById(R.id.edit_subject_description);
        if (toEdit != null) {
            nameInput.setText(toEdit.getName());
            descInput.setText(toEdit.getDescription());
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(toEdit == null ? "Add subject" : "Edit subject")
                .setView(dialogView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
                String name = nameInput.getText().toString();
                String description = descInput.getText().toString();
                var err = viewModel.validateUniqueName(name, toEdit == null ? null : toEdit.getId());
                if (err.isPresent()) {
                    Toast.makeText(getContext(), err.get(), Toast.LENGTH_SHORT).show();
                    return; // keep dialog open
                }
                if (toEdit == null) {
                    viewModel.addSubject(name, description);
                    Toast.makeText(getContext(), "Subject add success", Toast.LENGTH_SHORT).show();
                } else {
                    viewModel.updateSubject(toEdit, name, description);
                    Toast.makeText(getContext(), "Subject update success", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    @Override
    public void onEdit(Subject subject) {
        showAddOrEditDialog(subject);
    }

    @Override
    public void onDelete(Subject subject) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete " + subject.getName() + "?")
                .setPositiveButton("Delete", (d, w) -> viewModel.deleteSubject(subject))
                .setNegativeButton("Cancel", null)
                .show();
    }
}