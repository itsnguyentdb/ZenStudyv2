package com.example.quiz_clone.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quiz_clone.R;
import com.example.quiz_clone.adapters.FlashcardTermDetailsAdapter;
import com.example.quiz_clone.models.FlashcardDeck;
import com.example.quiz_clone.models.FlashcardTerm;
import com.example.quiz_clone.viewmodels.FlashcardDeckDetailsViewModel;

import java.util.List;

public class FlashcardDeckDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_DECK_ID = "deck_id";

    private FlashcardDeckDetailsViewModel viewModel;
    private FlashcardTermDetailsAdapter adapter;

    private TextView tvDeckTitle;
    private TextView tvDeckDescription;
    private TextView tvCardCount;
    private Button btnStudyDeck;
    private Button btnEditDeck;
    private RecyclerView rvFlashcards;

    private long deckId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard_deck_details);

        // Get deck ID from intent
        deckId = getIntent().getLongExtra(EXTRA_DECK_ID, -1);
        if (deckId == -1) {
            Toast.makeText(this, "Invalid deck", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupViewModel();
        setupRecyclerView();
        setupClickListeners();
    }

    private void initViews() {
        tvDeckTitle = findViewById(R.id.tvDeckTitle);
        tvDeckDescription = findViewById(R.id.tvDeckDescription);
        tvCardCount = findViewById(R.id.tvCardCount);
        btnStudyDeck = findViewById(R.id.btnStudyDeck);
        btnEditDeck = findViewById(R.id.btnEditDeck);
        rvFlashcards = findViewById(R.id.rvFlashcards);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(FlashcardDeckDetailsViewModel.class);
        viewModel.init(deckId);

        // Observe deck data
        viewModel.getDeckLiveData().observe(this, deck -> {
            if (deck != null) {
                updateDeckInfo(deck);
            }
        });

        // Observe terms data
        viewModel.getTermsLiveData().observe(this, terms -> {
            if (terms != null) {
                adapter.setTerms(terms);
                updateCardCount(terms.size());
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new FlashcardTermDetailsAdapter(null);
        rvFlashcards.setLayoutManager(new LinearLayoutManager(this));
        rvFlashcards.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnStudyDeck.setOnClickListener(v -> startStudyActivity());
        btnEditDeck.setOnClickListener(v -> editDeck());
    }

    private void updateDeckInfo(FlashcardDeck deck) {
        tvDeckTitle.setText(deck.getTitle());
        tvDeckDescription.setText(deck.getDescription() != null ? deck.getDescription() : "No description");
    }

    private void updateCardCount(int count) {
        tvCardCount.setText(getResources().getQuantityString(
                R.plurals.card_count, count, count));
    }

    private void startStudyActivity() {
        List<FlashcardTerm> terms = viewModel.getTermsLiveData().getValue();
        if (terms == null || terms.isEmpty()) {
            Toast.makeText(this, "No flashcards to study", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, StudyFlashcardActivity.class);
        intent.putExtra(StudyFlashcardActivity.EXTRA_DECK_ID, deckId);
        startActivity(intent);
    }

    private void editDeck() {
        Intent intent = new Intent(this, SaveFlashcardDeckActivity.class);
        intent.putExtra(SaveFlashcardDeckActivity.EXTRA_DECK_ID, deckId);
        startActivity(intent);
    }
}