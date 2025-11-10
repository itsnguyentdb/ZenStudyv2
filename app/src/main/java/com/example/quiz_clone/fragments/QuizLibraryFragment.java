package com.example.quiz_clone.fragments;

import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.quiz_clone.R;
import com.example.quiz_clone.activities.SaveFlashcardDeckActivity;
import com.example.quiz_clone.activities.SaveQuizActivity;
import com.example.quiz_clone.activities.TakeQuizActivity;
import com.example.quiz_clone.adapters.QuizLibraryAdapter;
import com.example.quiz_clone.enums.QuizFilter;
import com.example.quiz_clone.models.Quiz;
import com.example.quiz_clone.viewmodels.QuizLibraryViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class QuizLibraryFragment extends Fragment {
    private QuizLibraryViewModel viewModel;
    private QuizLibraryAdapter adapter;

    // UI Components
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout layoutEmptyState, layoutSearchEmpty;
    private TextView textTotalQuizzes, textTotalAttempts, textAverageScore;
    private TextView textEmptySubtitle, textSearchEmptySubtitle;
    private EditText editTextSearch;
    private MaterialButton buttonCreateFirstQuiz;
    private FloatingActionButton fabAddQuiz;

    // Filter chips
    private Chip chipAll, chipRecent, chipHighScore, chipMostAttempted;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz_library, container, false);
        initViews(view);
        setupViewModel();
        setupRecyclerView();
        setupClickListeners();
        setupSearch();
        setupFilterChips(view); // Pass the view to setupFilterChips
        observeViewModel();
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_quizzes);
        progressBar = view.findViewById(R.id.progress_bar_loading);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        layoutSearchEmpty = view.findViewById(R.id.layout_search_empty);

        // Stats
        textTotalQuizzes = view.findViewById(R.id.text_total_quizzes);
        textTotalAttempts = view.findViewById(R.id.text_total_attempts);
        textAverageScore = view.findViewById(R.id.text_average_score);

        // Search
        editTextSearch = view.findViewById(R.id.edit_text_search);

        // Empty states
        textEmptySubtitle = view.findViewById(R.id.text_empty_subtitle);
        textSearchEmptySubtitle = view.findViewById(R.id.text_search_empty_subtitle);
        buttonCreateFirstQuiz = view.findViewById(R.id.button_create_first_quiz);

        // FAB
        fabAddQuiz = view.findViewById(R.id.fab_add_quiz);

        // Filter chips
        chipAll = view.findViewById(R.id.chip_all);
        chipRecent = view.findViewById(R.id.chip_recent);
        chipHighScore = view.findViewById(R.id.chip_high_score);
        chipMostAttempted = view.findViewById(R.id.chip_most_attempted);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(QuizLibraryViewModel.class);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new QuizLibraryAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        fabAddQuiz.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), SaveQuizActivity.class);
                    startActivity(intent);
                }
        );

        adapter.setOnQuizClickListener(quiz -> {
//            navigateToQuizDetails(quiz);
        });

        adapter.setOnTakeQuizClickListener(quiz -> {
            var intent = new Intent(getActivity(), TakeQuizActivity.class);
            intent.putExtra(TakeQuizActivity.EXTRA_QUIZ_ID, quiz.getId());
            startActivity(intent);
        });

        adapter.setOnViewResultsClickListener(quiz -> {
//            navigateToQuizResults(quiz);
        });

        adapter.setOnQuizMenuClickListener((quiz, v) -> {
//            showQuizOptionsDialog(quiz);
        });
    }

    private void setupSearch() {
        editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = editTextSearch.getText().toString().trim();
                viewModel.searchQuizzes(query);
                hideKeyboard();
                return true;
            }
            return false;
        });

        // Real-time search
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    viewModel.clearSearch();
                } else {
                    viewModel.searchQuizzes(query);
                }
            }
        });
    }

    private void setupFilterChips(View view) {
        // Use the passed view instead of getView()
        ChipGroup chipGroup = view.findViewById(R.id.quiz_library_filter_chips);

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                chipAll.setChecked(true);
                return;
            }

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip_all) {
                viewModel.setFilter(QuizFilter.ALL);
            } else if (checkedId == R.id.chip_recent) {
                viewModel.setFilter(QuizFilter.RECENT);
            } else if (checkedId == R.id.chip_high_score) {
                viewModel.setFilter(QuizFilter.HIGH_SCORE);
            } else if (checkedId == R.id.chip_most_attempted) {
                viewModel.setFilter(QuizFilter.MOST_ATTEMPTED);
            }
        });
    }

    private void observeViewModel() {
        viewModel.getAllQuizzes().observe(getViewLifecycleOwner(), quizzes -> {
            adapter.setQuizzes(quizzes);
            updateUIState(quizzes);
            // Don't update stats here anymore - we'll do it in separate observers
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getSearchResults().observe(getViewLifecycleOwner(), searchResults -> {
            if (searchResults != null) {
                adapter.setQuizzes(searchResults);
                updateSearchUIState(searchResults);
            }
        });

        // Observe statistics
        viewModel.getQuestionCounts().observe(getViewLifecycleOwner(), questionCounts -> {
            if (questionCounts != null) {
                adapter.setQuestionCounts(questionCounts);
            }
        });

        viewModel.getAttemptCounts().observe(getViewLifecycleOwner(), attemptCounts -> {
            if (attemptCounts != null) {
                adapter.setAttemptCounts(attemptCounts);
            }
        });

        viewModel.getAverageScores().observe(getViewLifecycleOwner(), averageScores -> {
            if (averageScores != null) {
                adapter.setAverageScores(averageScores);
            }
        });

        viewModel.getLastAttemptDates().observe(getViewLifecycleOwner(), lastAttemptDates -> {
            if (lastAttemptDates != null) {
                adapter.setLastAttemptDates(lastAttemptDates);
            }
        });
    }

    private void updateUIState(List<Quiz> quizzes) {
        if (quizzes == null || quizzes.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            layoutSearchEmpty.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            layoutSearchEmpty.setVisibility(View.GONE);
        }
    }

    private void updateSearchUIState(List<Quiz> searchResults) {
        String query = editTextSearch.getText().toString().trim();
        boolean isSearching = !query.isEmpty();

        if (isSearching) {
            if (searchResults.isEmpty()) {
                layoutSearchEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                layoutEmptyState.setVisibility(View.GONE);
                textSearchEmptySubtitle.setText("No results for \"" + query + "\"");
            } else {
                layoutSearchEmpty.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                layoutEmptyState.setVisibility(View.GONE);
            }
        }
    }

    private void updateStats(List<Quiz> quizzes) {
        if (quizzes == null) return;

        int totalQuizzes = quizzes.size();
        int totalAttempts = calculateTotalAttempts(quizzes);
        int averageScore = calculateAverageScore(quizzes);

        textTotalQuizzes.setText(String.valueOf(totalQuizzes));
        textTotalAttempts.setText(String.valueOf(totalAttempts));
        textAverageScore.setText(averageScore + "%");
    }

    private int calculateTotalAttempts(List<Quiz> quizzes) {
        return viewModel.getTotalAttempts().getValue();
    }

    private int calculateAverageScore(List<Quiz> quizzes) {
        // Implement based on your data
        return viewModel.getAverageScore().getValue();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editTextSearch.getWindowToken(), 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh quiz data when fragment becomes visible
        refreshQuizData();
    }

    private void refreshQuizData() {
        if (viewModel != null) {
            // This will trigger the observers and refresh the list
            viewModel.getAllQuizzes().observe(getViewLifecycleOwner(), quizzes -> {
                if (quizzes != null) {
                    adapter.setQuizzes(quizzes);
                    updateUIState(quizzes);
                }
            });
        }
    }
}