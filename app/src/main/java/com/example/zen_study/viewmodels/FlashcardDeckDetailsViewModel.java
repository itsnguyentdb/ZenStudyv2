package com.example.zen_study.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.zen_study.models.FlashcardDeck;
import com.example.zen_study.models.FlashcardTerm;
import com.example.zen_study.repositories.impls.FlashcardRepositoryImpl;

import java.util.List;

public class FlashcardDeckDetailsViewModel extends AndroidViewModel {
    private FlashcardRepositoryImpl repository;
    private MutableLiveData<FlashcardDeck> deckLiveData;
    private MutableLiveData<List<FlashcardTerm>> termsLiveData;

    private long deckId;

    public FlashcardDeckDetailsViewModel(Application application) {
        super(application);
        repository = new FlashcardRepositoryImpl(application);
        deckLiveData = new MutableLiveData<>();
        termsLiveData = new MutableLiveData<>();
    }

    public void init(long deckId) {
        this.deckId = deckId;
        loadDeck();
        loadTerms();
    }

    private void loadDeck() {
        new Thread(() -> {
            repository.getDeckById(deckId).ifPresent(deck -> {
                deckLiveData.postValue(deck);
            });
        }).start();
    }

    private void loadTerms() {
        new Thread(() -> {
            List<FlashcardTerm> terms = repository.getTermsForDeck(deckId);
            termsLiveData.postValue(terms);
        }).start();
    }

    public LiveData<FlashcardDeck> getDeckLiveData() {
        return deckLiveData;
    }

    public LiveData<List<FlashcardTerm>> getTermsLiveData() {
        return termsLiveData;
    }

    public void refresh() {
        loadDeck();
        loadTerms();
    }
}
