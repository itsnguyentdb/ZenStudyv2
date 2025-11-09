package com.example.quiz_clone.fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.quiz_clone.R;
import com.example.quiz_clone.models.FlashcardTerm;
import com.example.quiz_clone.viewmodels.FlashcardViewModel;

// FlashcardActivity.java
public class StudyFlashcardFragment extends Fragment {

    private FlashcardViewModel viewModel;
    private AnimatorSet flipOutAnimator;
    private AnimatorSet flipInAnimator;
    private boolean isFrontVisible = true;

    private CardView cardFront, cardBack;
    private TextView termText, definitionText, progressText, deckTitle;
    private Button prevBtn, nextBtn, ratingEasy, ratingMedium, ratingHard;


    private long deckId;

    public StudyFlashcardFragment() {
    }

    public static StudyFlashcardFragment newInstance(long deckId) {
        var fragment = new StudyFlashcardFragment();
        Bundle args = new Bundle();
        args.putLong("DECK_ID", deckId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            deckId = getArguments().getLong("DECK_ID", 1);
        }

        if (deckId == -1) {
            requireActivity().onBackPressed();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_flashcard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupAnimations();
        setupViewModel(deckId);
        setupClickListeners(view);
    }

    private void initViews(View view) {
        cardFront = view.findViewById(R.id.card_front);
        cardBack = view.findViewById(R.id.card_back);
        termText = view.findViewById(R.id.term_text);
        definitionText = view.findViewById(R.id.definition_text);
        progressText = view.findViewById(R.id.progress_text);
        deckTitle = view.findViewById(R.id.deck_title);
        prevBtn = view.findViewById(R.id.prev_btn);
        nextBtn = view.findViewById(R.id.next_btn);
        ratingEasy = view.findViewById(R.id.rating_easy);
        ratingMedium = view.findViewById(R.id.rating_medium);
        ratingHard = view.findViewById(R.id.rating_hard);
    }

    private void setupViewModel(long deckId) {
        viewModel = new ViewModelProvider(this).get(FlashcardViewModel.class);
        viewModel.setDeckId(deckId);

        // Observe deck data
        viewModel.getCurrentDeck().observe(getViewLifecycleOwner(), deck -> {
            if (deck != null) {
                deckTitle.setText(deck.getTitle());
            }
        });

        // Observe current card index and terms
        viewModel.getCurrentCardIndex().observe(getViewLifecycleOwner(), index -> {
            updateProgress();
            updateCurrentCard();
            updateNavigationButtons();
        });

        viewModel.getFlashcardTerms().observe(getViewLifecycleOwner(), terms -> {
            if (terms != null) {
                updateProgress();
                updateCurrentCard();
            }
        });

        // Observe card side
        viewModel.getIsShowingFront().observe(getViewLifecycleOwner(), isFront -> {
            if (isFront != null && isFront != isFrontVisible) {
                flipCard();
            }
        });
    }

    private void setupClickListeners(View view) {
        // Card flip
        view.findViewById(R.id.card_container).setOnClickListener(v -> {
            viewModel.flipCard();
        });

        // Navigation
        prevBtn.setOnClickListener(v -> viewModel.previousCard());
        nextBtn.setOnClickListener(v -> viewModel.nextCard());

        // Ratings
        ratingEasy.setOnClickListener(v -> viewModel.updateCardRating(1));
        ratingMedium.setOnClickListener(v -> viewModel.updateCardRating(2));
        ratingHard.setOnClickListener(v -> viewModel.updateCardRating(3));
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
    }

    private void flipCardWithoutAnimation() {
        if (!isFrontVisible) {
            cardFront.setVisibility(View.VISIBLE);
            cardBack.setVisibility(View.INVISIBLE);
            cardFront.setRotationY(0f);
            cardBack.setRotationY(-90f);
            isFrontVisible = true;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (flipOutAnimator != null) flipOutAnimator.cancel();
        if (flipInAnimator != null) flipInAnimator.cancel();
    }
}