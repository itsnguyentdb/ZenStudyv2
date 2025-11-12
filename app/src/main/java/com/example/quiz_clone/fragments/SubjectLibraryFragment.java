package com.example.quiz_clone.fragments;

// Added comment to retrigger compilation and ensure adapter import is recognized
// Subject library fragment displays list of subjects with search/add/edit/delete.

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quiz_clone.R;
import com.example.quiz_clone.adapters.SubjectLibraryAdapter;
import com.example.quiz_clone.models.Subject;
import com.example.quiz_clone.viewmodels.SubjectLibraryViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class SubjectLibraryFragment extends Fragment implements SubjectLibraryAdapter.Listener {
    private SubjectLibraryViewModel viewModel;
    private SubjectLibraryAdapter adapter;
    private RecyclerView recyclerView;
    private LinearLayout layoutEmpty;
    private EditText searchEditText;
    private FloatingActionButton fabAdd;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_subject_library, container, false);
        recyclerView = v.findViewById(R.id.recycler_view_subjects);
        layoutEmpty = v.findViewById(R.id.layout_empty_state_subjects);
        searchEditText = v.findViewById(R.id.edit_text_search_subjects);
        fabAdd = v.findViewById(R.id.fab_add_subject);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SubjectLibraryViewModel.class);

        adapter = new SubjectLibraryAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        viewModel.getAllSubjects().observe(getViewLifecycleOwner(), subjects -> {
            applyFilterAndShow(subjects, viewModel.getSearchQuery().getValue());
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s.toString());
                List<Subject> current = viewModel.getAllSubjects().getValue();
                applyFilterAndShow(current, s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        fabAdd.setOnClickListener(v -> showAddOrEditDialog(null));
    }

    private void applyFilterAndShow(List<Subject> list, String query) {
        List<Subject> filtered = viewModel.filter(list, query);
        adapter.submitList(filtered);
        boolean empty = filtered == null || filtered.isEmpty();
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void showAddOrEditDialog(@Nullable Subject subject) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_edit_subject, null);
        EditText name = dialogView.findViewById(R.id.input_subject_name);
        EditText desc = dialogView.findViewById(R.id.input_subject_description);

        boolean isEdit = subject != null;
        if (isEdit) {
            name.setText(subject.getName());
            desc.setText(subject.getDescription());
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(isEdit ? "Edit Subject" : "Add Subject")
                .setView(dialogView)
                .setPositiveButton(isEdit ? "Save" : "Add", (d, w) -> {
                    String n = name.getText().toString().trim();
                    String ds = desc.getText().toString().trim();
                    if (n.isEmpty()) return;
                    if (isEdit) {
                        subject.setName(n);
                        subject.setDescription(ds);
                        viewModel.updateSubject(subject);
                    } else {
                        Subject s = viewModel.addSubject(n);
                        s.setDescription(ds);
                        viewModel.updateSubject(s);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onSubjectClick(Subject subject) {
        // Could navigate to a subject detail/resources screen in future
    }

    @Override
    public void onSubjectEdit(Subject subject) {
        showAddOrEditDialog(subject);
    }

    @Override
    public void onSubjectDelete(Subject subject) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Subject")
                .setMessage("Are you sure you want to delete '" + subject.getName() + "'? This cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> viewModel.deleteSubject(subject.getId()))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
