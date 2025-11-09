package com.example.quiz_clone.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.quiz_clone.models.FlashcardTerm;
import com.example.quiz_clone.repositories.impls.FlashcardRepositoryImpl;

import java.util.List;

import androidx.lifecycle.LiveData;

import com.example.quiz_clone.models.FlashcardDeck;

public class FlashcardViewModel extends AndroidViewModel {
    private final FlashcardRepositoryImpl repository;
    private final MutableLiveData<Integer> currentCardIndex = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isShowingFront = new MutableLiveData<>(true);
    private final MutableLiveData<List<FlashcardTerm>> flashcardTerms = new MutableLiveData<>();
    private final MutableLiveData<FlashcardDeck> currentDeck = new MutableLiveData<>();

    private long deckId;

    public FlashcardViewModel(@NonNull Application application) {
        super(application);
        repository = new FlashcardRepositoryImpl(application);
    }

    public void setDeckId(long deckId) {
        this.deckId = deckId;
        loadDeckData();
    }

    private void loadDeckData() {
        // Observe deck information
        repository.getDeckById(deckId).observeForever(deck -> {
            if (deck != null) {
                currentDeck.postValue(deck);
            }
        });

        // Observe terms for the deck
        repository.getTermsForDeck(deckId).observeForever(terms -> {
            if (terms != null && !terms.isEmpty()) {
                flashcardTerms.postValue(terms);
                // Reset to first card when terms change
                currentCardIndex.postValue(0);
                isShowingFront.postValue(true);
            }
        });
    }

    // LiveData getters
    public LiveData<List<FlashcardTerm>> getFlashcardTerms() {
        return flashcardTerms;
    }

    public LiveData<FlashcardDeck> getCurrentDeck() {
        return currentDeck;
    }

    public LiveData<Integer> getCurrentCardIndex() {
        return currentCardIndex;
    }

    public LiveData<Boolean> getIsShowingFront() {
        return isShowingFront;
    }

    // Actions
    public void flipCard() {
        Boolean current = isShowingFront.getValue();
        if (current != null) {
            isShowingFront.postValue(!current);
        }
    }

    public void nextCard() {
        Integer currentIndex = currentCardIndex.getValue();
        List<FlashcardTerm> terms = flashcardTerms.getValue();

        if (currentIndex != null && terms != null && currentIndex < terms.size() - 1) {
            currentCardIndex.postValue(currentIndex + 1);
            isShowingFront.postValue(true);
        }
    }

    public void previousCard() {
        Integer currentIndex = currentCardIndex.getValue();
        if (currentIndex != null && currentIndex - 1 >= 0) {
            currentCardIndex.postValue(currentIndex - 1);
            isShowingFront.postValue(true);
        }
    }

    public void updateCardRating(int rating) {
        Integer currentIndex = currentCardIndex.getValue();
        List<FlashcardTerm> terms = flashcardTerms.getValue();

        if (currentIndex != null && terms != null && currentIndex < terms.size()) {
            FlashcardTerm term = terms.get(currentIndex);
            term.setRating(rating);
            repository.updateFlashcardTerm(term);
        }
    }

    @Nullable
    public FlashcardTerm getCurrentTerm() {
        Integer index = currentCardIndex.getValue();
        List<FlashcardTerm> terms = flashcardTerms.getValue();

        if (index != null && terms != null && index < terms.size()) {
            return terms.get(index);
        }
        return null;
    }

    public boolean hasNextCard() {
        Integer index = currentCardIndex.getValue();
        List<FlashcardTerm> terms = flashcardTerms.getValue();
        return index != null && terms != null && index < terms.size() - 1;
    }

    public boolean hasPreviousCard() {
        Integer index = currentCardIndex.getValue();
        return index != null && index > 0;
    }
}