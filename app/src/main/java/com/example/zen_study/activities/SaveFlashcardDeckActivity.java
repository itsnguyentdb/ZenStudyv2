package com.example.zen_study.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zen_study.R;
import com.example.zen_study.adapters.SaveFlashcardAdapter;
import com.example.zen_study.databinding.ActivitySaveFlashcardDeckBinding;
import com.example.zen_study.models.FlashcardDeck;
import com.example.zen_study.models.FlashcardTerm;
import com.example.zen_study.models.Subject;
import com.example.zen_study.viewmodels.SaveFlashcardViewModel;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SaveFlashcardDeckActivity extends AppCompatActivity {

    private ActivitySaveFlashcardDeckBinding binding;
    private SaveFlashcardViewModel viewModel;
    private SaveFlashcardAdapter termAdapter;
    private List<FlashcardTerm> flashcardTerms = new ArrayList<>();
    private Long deckId = null;
    private FlashcardDeck existingDeck;
    private String selectedSubjectName = "Other";
    private Subject selectedSubject;

    public static final int RESULT_DECK_SAVED = 1;
    public static final String EXTRA_DECK_SAVED = "deck_saved";
    public static final String EXTRA_DECK_ID = "deck_id";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySaveFlashcardDeckBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new SaveFlashcardViewModel(getApplication());
        setupToolbar();
        setupSubjectChips();
        setupRecyclerView();
        setupClickListeners();

        deckId = getIntent().getLongExtra(EXTRA_DECK_ID, -1L);
        if (deckId != -1L) {
            loadExistingDeck();
        } else {
            updateEmptyState();
        }
    }

    private void setupToolbar() {
//        setSupportActionBar(binding.toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setTitle(deckId == null || deckId == -1L ? "Create New Deck" : "Edit Deck");
//
//        binding.toolbar.setNavigationOnClickListener(v -> handleCancel());
    }

    private void setupSubjectChips() {
        int[] chipIds = {
                R.id.chip_math, R.id.chip_science, R.id.chip_history,
                R.id.chip_language, R.id.chip_computer_science, R.id.chip_other
        };

        for (int chipId : chipIds) {
            Chip chip = binding.getRoot().findViewById(chipId);
            if (chip != null) {
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedSubjectName = chip.getText().toString();
                        for (int otherChipId : chipIds) {
                            if (otherChipId != chip.getId()) {
                                Chip otherChip = binding.getRoot().findViewById(otherChipId);
                                if (otherChip != null) {
                                    otherChip.setChecked(false);
                                }
                            }
                        }
                    }
                });
            }
        }

        Chip chipOther = binding.getRoot().findViewById(R.id.chip_other);
        if (chipOther != null) {
            chipOther.setChecked(true);
        }
    }

    private void setupRecyclerView() {
        termAdapter = new SaveFlashcardAdapter();
        binding.recyclerViewTerms.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewTerms.setAdapter(termAdapter);

        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                int from = viewHolder.getAdapterPosition();
                int to = target.getAdapterPosition();
                termAdapter.moveItem(from, to);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewTerms);
        termAdapter.setItemTouchHelper(itemTouchHelper);
    }

    private void setupClickListeners() {
        binding.btnCancel.setOnClickListener(v -> handleCancel());
        binding.btnAddFlashcard.setOnClickListener(v -> addNewTerm());
        binding.btnSave.setOnClickListener(v -> saveFlashcardDeck());
    }

    private void addNewTerm() {
        FlashcardTerm newTerm = FlashcardTerm.builder()
                .term("")
                .definition("")
                .position(flashcardTerms.size())
                .rating(0)
                .build();
        flashcardTerms.add(newTerm);
        termAdapter.submitList(new ArrayList<>(flashcardTerms));
        updateEmptyState();
    }

    private void updateEmptyState() {
        binding.layoutEmptyTerms.setVisibility(flashcardTerms.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void handleCancel() {
        if (hasUnsavedChanges()) {
            showUnsavedChangesDialog();
        } else {
            finish();
        }
    }

    private boolean hasUnsavedChanges() {
        String title = binding.editTextTitle.getText().toString().trim();
        String description = binding.editTextDescription.getText().toString().trim();

        if (deckId != null && deckId != -1L && existingDeck != null) {
            String existingSubjectName = getSubjectName(existingDeck.getSubjectId());
            if (!title.equals(existingDeck.getTitle()) ||
                    !description.equals(existingDeck.getDescription()) ||
                    !selectedSubjectName.equals(existingSubjectName)) {
                return true;
            }
            if (flashcardTerms.size() != existingDeck.getCardCount()) {
                return true;
            }
            return false;
        } else {
            return !TextUtils.isEmpty(title) || !TextUtils.isEmpty(description) || !flashcardTerms.isEmpty();
        }
    }

    private String getSubjectName(Long subjectId) {
        if (subjectId == null) return "Other";
        var subject = viewModel.getSubjectById(subjectId);
        return subject.map(Subject::getName).orElse("Other");
    }

    private void showUnsavedChangesDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Unsaved Changes")
                .setMessage("You have unsaved changes. Are you sure you want to discard them?")
                .setPositiveButton("Discard", (dialog, which) -> finish())
                .setNegativeButton("Keep Editing", null)
                .show();
    }

    private void loadExistingDeck() {
        existingDeck = viewModel.getDeckById(deckId).orElse(null);
        if (existingDeck == null) {
            Toast.makeText(this, "Deck not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.editTextTitle.setText(existingDeck.getTitle());
        binding.editTextDescription.setText(existingDeck.getDescription());

        Long subjectId = existingDeck.getSubjectId();
        if (subjectId != null) {
            var subjectOpt = viewModel.getSubjectById(subjectId);
            subjectOpt.ifPresent(subject -> {
                selectedSubject = subject;
                selectedSubjectName = subject.getName();
                int chipId = getChipIdForSubject(selectedSubjectName);
                if (chipId != -1) {
                    Chip chip = binding.getRoot().findViewById(chipId);
                    if (chip != null) {
                        chip.setChecked(true);
                    }
                }
            });
        }
        flashcardTerms = viewModel.getTermsForDeck(deckId);
        termAdapter.submitList(new ArrayList<>(flashcardTerms));
        updateEmptyState();
    }

    private int getChipIdForSubject(String subjectName) {
        switch (subjectName) {
            case "Math":
                return R.id.chip_math;
            case "Science":
                return R.id.chip_science;
            case "History":
                return R.id.chip_history;
            case "Language":
                return R.id.chip_language;
            case "Computer Science":
                return R.id.chip_computer_science;
            case "Other":
                return R.id.chip_other;
            default:
                return -1;
        }
    }

    private void saveFlashcardDeck() {
        String title = binding.editTextTitle.getText().toString().trim();
        String description = binding.editTextDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            binding.textInputLayoutTitle.setError("Deck title is required");
            return;
        }

        forceSaveAllTextChanges();

        flashcardTerms = termAdapter.getCurrentTerms();

        if (flashcardTerms.isEmpty()) {
            Toast.makeText(this, "Please add at least one flashcard", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate each term
        boolean hasInvalidTerm = false;
        int firstInvalidPosition = -1;

        for (int i = 0; i < flashcardTerms.size(); i++) {
            FlashcardTerm term = flashcardTerms.get(i);
            if (TextUtils.isEmpty(term.getTerm()) || TextUtils.isEmpty(term.getDefinition())) {
                hasInvalidTerm = true;
                firstInvalidPosition = i;
                break;
            }
        }

        if (hasInvalidTerm) {
            Toast.makeText(this, "Please fill in all terms and definitions", Toast.LENGTH_SHORT).show();

            // Optional: Scroll to the first invalid term
            if (firstInvalidPosition != -1) {
                binding.recyclerViewTerms.scrollToPosition(firstInvalidPosition);
            }
            return;
        }
        // Get or create subject
        var subjectOpt = viewModel.getSubjectByName(selectedSubjectName);
        selectedSubject = subjectOpt.orElseGet(() -> {
            return viewModel.saveSubject(selectedSubjectName);
        });
        System.out.println(selectedSubject.getId());
        Date now = new Date();
        FlashcardDeck deck = FlashcardDeck.builder()
                .id(deckId != -1L ? deckId : null)
                .title(title)
                .description(description)
                .subjectId(selectedSubject.getId())
                .createdAt(deckId != -1L && existingDeck != null ? existingDeck.getCreatedAt() : now)
                .lastUpdatedAt(now)
                .cardCount(flashcardTerms.size())
                .build();

        if (deckId == null || deckId == -1L) {
            viewModel.insertDeckWithTerms(deck, flashcardTerms);
            Toast.makeText(this, "Deck created successfully!", Toast.LENGTH_SHORT).show();
        } else {
            viewModel.updateDeckWithTerms(deck, flashcardTerms);
            Toast.makeText(this, "Deck updated successfully!", Toast.LENGTH_SHORT).show();
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_DECK_SAVED, true);
        setResult(RESULT_DECK_SAVED, resultIntent);
        finish();
    }

    private void forceSaveAllTextChanges() {
        // Clear focus from any currently focused view
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            currentFocus.clearFocus();
        }

        // Optional: If you need to manually update from EditTexts:
        for (int i = 0; i < binding.recyclerViewTerms.getChildCount(); i++) {
            View child = binding.recyclerViewTerms.getChildAt(i);
            RecyclerView.ViewHolder viewHolder = binding.recyclerViewTerms.getChildViewHolder(child);
            if (viewHolder instanceof SaveFlashcardAdapter.ViewHolder) {
                // This ensures any focused field loses focus and saves
                child.clearFocus();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handleCancel();
    }
}