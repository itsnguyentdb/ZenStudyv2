// FlashcardDecksFragment.java
package com.example.quiz_clone.fragments;

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
import com.example.quiz_clone.adapters.FlashcardLibraryAdapter;
import com.example.quiz_clone.models.FlashcardDeck;
import com.example.quiz_clone.viewmodels.FlashcardLibraryViewModel;

import java.util.ArrayList;
import java.util.List;

public class FlashcardLibraryFragment extends Fragment {
    private FlashcardLibraryViewModel viewModel;
    private FlashcardLibraryAdapter adapter;
    private RecyclerView recyclerView;
    private LinearLayout layoutEmptyState;
    private EditText editTextSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_flashcard_library, container, false);

        initViews(view);
        setupRecyclerView();
        setupSearch();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(FlashcardLibraryViewModel.class);
        observeViewModel();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_flashcard_decks);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        editTextSearch = view.findViewById(R.id.edit_text_search);
    }

    private void setupRecyclerView() {
        adapter = new FlashcardLibraryAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(deck -> {
            // Handle deck click - navigate to deck details or start studying
            if (getActivity() instanceof OnDeckSelectedListener) {
                ((OnDeckSelectedListener) getActivity()).onDeckSelected(deck);
            }
        });
    }

    private void setupSearch() {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDecks(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void observeViewModel() {
        viewModel.getAllDecks().observe(getViewLifecycleOwner(), decks -> {
            adapter.submitList(decks);
            updateEmptyState(decks != null && decks.isEmpty());
        });
    }

    private void filterDecks(String query) {
        // For now, we'll just show all decks
        // You can implement proper filtering logic here
        viewModel.getAllDecks().observe(getViewLifecycleOwner(), decks -> {
            if (decks != null) {
                if (query.isEmpty()) {
                    adapter.submitList(decks);
                } else {
                    List<FlashcardDeck> filteredDecks = new ArrayList<>();
                    for (FlashcardDeck deck : decks) {
                        if (deck.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                                deck.getDescription().toLowerCase().contains(query.toLowerCase())) {
                            filteredDecks.add(deck);
                        }
                    }
                    adapter.submitList(filteredDecks);
                }
            }
        });
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
}