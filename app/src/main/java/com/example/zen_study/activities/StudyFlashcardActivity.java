package com.example.zen_study.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;

import com.example.zen_study.R;
import com.example.zen_study.models.FlashcardTerm;
import com.example.zen_study.viewmodels.StudyFlashcardViewModel;

public class StudyFlashcardActivity extends AppCompatActivity {
    public static final String EXTRA_DECK_ID = "deck_id";

    private StudyFlashcardViewModel viewModel;
    private AnimatorSet flipOutAnimator;
    private AnimatorSet flipInAnimator;
    private boolean isFrontVisible = true;

    private CardView cardFront, cardBack;
    private TextView termText, definitionText, progressText, deckTitle;
    private Button prevBtn, nextBtn, flipBtn;
    private long deckId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_flashcard);

        // Get deck ID from intent
        deckId = getIntent().getLongExtra(EXTRA_DECK_ID, -1);
        if (deckId == -1) {
            finish();
            return;
        }

        initViews();
        setupAnimations();
        setupViewModel();
        setupClickListeners();
    }

    private void initViews() {
        cardFront = findViewById(R.id.card_front);
        cardBack = findViewById(R.id.card_back);
        termText = findViewById(R.id.term_text);
        definitionText = findViewById(R.id.definition_text);
        progressText = findViewById(R.id.progress_text);
        deckTitle = findViewById(R.id.deck_title);
        prevBtn = findViewById(R.id.prev_btn);
        nextBtn = findViewById(R.id.next_btn);
        flipBtn = findViewById(R.id.flip_btn);

        // Rating buttons removed from layout, so remove references to them
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(StudyFlashcardViewModel.class);
        viewModel.setDeckId(deckId);

        // Observe deck data
        viewModel.getCurrentDeck().observe(this, deck -> {
            if (deck != null) {
                deckTitle.setText(deck.getTitle());
            }
        });

        // Observe current card index and terms
        viewModel.getCurrentCardIndex().observe(this, index -> {
            updateProgress();
            updateCurrentCard();
            updateNavigationButtons();
            updateFlipButtonText();
        });

        viewModel.getFlashcardTerms().observe(this, terms -> {
            if (terms != null) {
                updateProgress();
                updateCurrentCard();
            }
        });

        // Observe card side
        viewModel.getIsShowingFront().observe(this, isFront -> {
            if (isFront != null && isFront != isFrontVisible) {
                flipCard();
            }
        });
    }

    private void setupClickListeners() {
        // Card flip button
        flipBtn.setOnClickListener(v -> {
            viewModel.flipCard();
        });

        // Navigation buttons
        prevBtn.setOnClickListener(v -> viewModel.previousCard());
        nextBtn.setOnClickListener(v -> viewModel.nextCard());

        // Optional: Still allow tapping the card to flip
        findViewById(R.id.card_container).setOnClickListener(v -> {
            viewModel.flipCard();
        });
    }

    private void updateProgress() {
        Integer currentIndex = viewModel.getCurrentCardIndex().getValue();
        var terms = viewModel.getFlashcardTerms().getValue();

        if (currentIndex != null && terms != null) {
            progressText.setText(String.format("%d/%d", currentIndex + 1, terms.size()));
        }
    }

    private void updateNavigationButtons() {
        prevBtn.setEnabled(viewModel.hasPreviousCard());
        nextBtn.setEnabled(viewModel.hasNextCard());

        // Update button text based on position
        if (!viewModel.hasNextCard()) {
            nextBtn.setText("Finish");
        } else {
            nextBtn.setText("Next");
        }
    }

    private void updateFlipButtonText() {
        if (isFrontVisible) {
            flipBtn.setText("Show Definition");
        } else {
            flipBtn.setText("Show Term");
        }
    }

    private void updateCurrentCard() {
        FlashcardTerm currentTerm = viewModel.getCurrentTerm();
        if (currentTerm != null) {
            termText.setText(currentTerm.getTerm());
            definitionText.setText(currentTerm.getDefinition());

            if (!isFrontVisible) {
                flipCardWithoutAnimation();
            }
        }
        updateFlipButtonText();
    }

    private void setupAnimations() {
        // Create flip animations
        flipOutAnimator = new AnimatorSet();
        flipInAnimator = new AnimatorSet();

        ObjectAnimator flipOut = ObjectAnimator.ofFloat(cardFront, "rotationY", 0f, 90f);
        ObjectAnimator flipIn = ObjectAnimator.ofFloat(cardBack, "rotationY", -90f, 0f);

        flipOutAnimator.play(flipOut);
        flipInAnimator.play(flipIn);

        flipOutAnimator.setDuration(300);
        flipInAnimator.setDuration(300);

        // Set camera distance for better 3D effect
        float scale = getResources().getDisplayMetrics().density * 8000;
        cardFront.setCameraDistance(scale);
        cardBack.setCameraDistance(scale);
    }

    private void flipCard() {
        if (isFrontVisible) {
            // Flip to back
            cardBack.setRotationY(-90f);
            cardBack.setVisibility(View.VISIBLE);
            flipOutAnimator.start();
            flipInAnimator.start();
            cardFront.setVisibility(View.INVISIBLE);
        } else {
            // Flip to front
            cardFront.setVisibility(View.VISIBLE);
            cardBack.setVisibility(View.INVISIBLE);
            cardFront.setRotationY(0f);
            cardBack.setRotationY(-90f);
        }
        isFrontVisible = !isFrontVisible;
        updateFlipButtonText();
    }

    private void flipCardWithoutAnimation() {
        if (!isFrontVisible) {
            cardFront.setVisibility(View.VISIBLE);
            cardBack.setVisibility(View.INVISIBLE);
            cardFront.setRotationY(0f);
            cardBack.setRotationY(-90f);
            isFrontVisible = true;
            updateFlipButtonText();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (flipOutAnimator != null) flipOutAnimator.cancel();
        if (flipInAnimator != null) flipInAnimator.cancel();
    }

    // Static method to start this activity
    public static void start(AppCompatActivity activity, long deckId) {
        android.content.Intent intent = new android.content.Intent(activity, StudyFlashcardActivity.class);
        intent.putExtra(EXTRA_DECK_ID, deckId);
        activity.startActivity(intent);
    }
}