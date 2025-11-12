package com.example.zen_study.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zen_study.R;
import com.example.zen_study.activities.FlashcardDeckDetailsActivity;
import com.example.zen_study.activities.SaveFlashcardDeckActivity;
import com.example.zen_study.adapters.FlashcardLibraryAdapter;
import com.example.zen_study.models.FlashcardDeck;
import com.example.zen_study.models.Subject;
import com.example.zen_study.viewmodels.FlashcardLibraryViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FlashcardLibraryFragment extends Fragment {
    private FlashcardLibraryViewModel viewModel;
    private FlashcardLibraryAdapter adapter;
    private RecyclerView recyclerView;
    private LinearLayout layoutEmptyState;
    private EditText editTextSearch;
    private LinearLayout filterChips;
    private FloatingActionButton fabAddDeck;

    // Subject filter options
    private Long currentSubjectFilter = -1L; // -1 means "All"

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_flashcard_library, container, false);

        initViews(view);
        setupRecyclerView();
        setupSearch();
        setupFAB();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(FlashcardLibraryViewModel.class);
        observeViewModel();
        setupSubjectFilter();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_flashcard_decks);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        editTextSearch = view.findViewById(R.id.edit_text_search);
        filterChips = view.findViewById(R.id.flashcard_library_filter_chips);
        fabAddDeck = view.findViewById(R.id.fab_add_deck);
    }

    private void setupRecyclerView() {
        adapter = new FlashcardLibraryAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(deck -> {
            // Handle deck click - navigate to deck details
            if (getActivity() instanceof OnDeckSelectedListener) {
                ((OnDeckSelectedListener) getActivity()).onDeckSelected(deck);
            }
        });

        // Set up options menu click listener
        adapter.setOnOptionsClickListener((deck, view) -> showOptionsMenu(deck, view));
    }

    private void setupSearch() {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDecks(s.toString(), currentSubjectFilter);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupSubjectFilter() {
        // Remove all existing chips
        filterChips.removeAllViews();

        // Create "All" chip
        Chip allChip = new Chip(requireContext());
        allChip.setText("All");
        allChip.setCheckable(true);
        allChip.setChecked(true);
        allChip.setTag(-1L);

        allChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentSubjectFilter = -1L;
                // Uncheck other chips
                for (int i = 0; i < filterChips.getChildCount(); i++) {
                    View child = filterChips.getChildAt(i);
                    if (child instanceof Chip && child != buttonView) {
                        ((Chip) child).setChecked(false);
                    }
                }
                filterDecks(editTextSearch.getText().toString(), currentSubjectFilter);
            }
        });

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 8, 0);
        allChip.setLayoutParams(params);
        filterChips.addView(allChip);

        // Get subjects from ViewModel and create chips
        viewModel.getSubjects().observe(getViewLifecycleOwner(), subjects -> {
            if (subjects != null) {
                adapter.setSubjects(subjects);
                for (Subject subject : subjects) {
                    Chip chip = new Chip(requireContext());
                    chip.setText(subject.getName());
                    chip.setCheckable(true);
                    chip.setTag(subject.getId());

                    chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (isChecked) {
                            currentSubjectFilter = (Long) chip.getTag();
                            // Uncheck other chips
                            for (int i = 0; i < filterChips.getChildCount(); i++) {
                                View child = filterChips.getChildAt(i);
                                if (child instanceof Chip && child != buttonView) {
                                    ((Chip) child).setChecked(false);
                                }
                            }
                            filterDecks(editTextSearch.getText().toString(), currentSubjectFilter);
                        }
                    });

                    chip.setLayoutParams(params);
                    filterChips.addView(chip);
                }
            }
        });
    }

    private void setupFAB() {
        fabAddDeck.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SaveFlashcardDeckActivity.class);
            startActivity(intent);
        });
    }

    private void observeViewModel() {
        viewModel.getAllDecks().observe(getViewLifecycleOwner(), decks -> {
            adapter.submitList(decks);
            updateEmptyState(decks != null && decks.isEmpty());
        });
    }

    private void filterDecks(String query, Long subjectId) {
        viewModel.getAllDecks().observe(getViewLifecycleOwner(), decks -> {
            if (decks != null) {
                List<FlashcardDeck> filteredDecks = new ArrayList<>();

                for (FlashcardDeck deck : decks) {
                    boolean matchesSearch = query.isEmpty() ||
                            deck.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                            (deck.getDescription() != null && deck.getDescription().toLowerCase().contains(query.toLowerCase()));

                    boolean matchesSubject = (subjectId == -1L) ||
                            (Objects.equals(deck.getSubjectId(), subjectId));

                    if (matchesSearch && matchesSubject) {
                        filteredDecks.add(deck);
                    }
                }
                adapter.submitList(filteredDecks);
                updateEmptyState(filteredDecks.isEmpty());
            }
        });
    }

    private void showOptionsMenu(FlashcardDeck deck, View anchorView) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), anchorView);
        popupMenu.inflate(R.menu.menu_flashcard_deck_options);

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_study) {
                // Start studying this deck
                Intent intent = new Intent(getActivity(), FlashcardDeckDetailsActivity.class);
                intent.putExtra(FlashcardDeckDetailsActivity.EXTRA_DECK_ID, deck.getId());
                startActivity(intent);
                return true;
            } else if (id == R.id.action_edit) {
                // Edit this deck - navigate to SaveFlashcardDeckActivity with deck ID
                Intent intent = new Intent(getActivity(), SaveFlashcardDeckActivity.class);
                intent.putExtra(SaveFlashcardDeckActivity.EXTRA_DECK_ID, deck.getId());
                startActivity(intent);
                return true;
            } else if (id == R.id.action_delete) {
                // Show delete confirmation
                showDeleteConfirmation(deck);
                return true;
            }

            return false;
        });

        popupMenu.show();
    }

    private void showDeleteConfirmation(FlashcardDeck deck) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Deck")
                .setMessage("Are you sure you want to delete \"" + deck.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.delete(deck);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }

    public interface OnDeckSelectedListener {
        void onDeckSelected(FlashcardDeck deck);
    }

    public interface OnStudyDeckListener {
        void onStudyDeck(FlashcardDeck deck);
    }

    public interface OnEditDeckListener {
        void onEditDeck(FlashcardDeck deck);
    }
}