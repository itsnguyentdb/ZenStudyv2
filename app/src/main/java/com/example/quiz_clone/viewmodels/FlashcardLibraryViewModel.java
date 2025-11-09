package com.example.quiz_clone.viewmodels;


import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.quiz_clone.models.FlashcardDeck;
import com.example.quiz_clone.repositories.FlashcardRepository;
import com.example.quiz_clone.repositories.impls.FlashcardRepositoryImpl;

import java.util.List;

import lombok.Getter;

public class FlashcardLibraryViewModel extends AndroidViewModel {
    private FlashcardRepositoryImpl repository;
    @Getter
    private LiveData<List<FlashcardDeck>> allDecks;

    public FlashcardLibraryViewModel(Application application) {
        super(application);
        repository = new FlashcardRepositoryImpl(application);
        allDecks = repository.getAllDecks();
    }

    public void insert(FlashcardDeck deck) {
        repository.createFlashcardDeck(deck.getTitle(), deck.getDescription());
    }

    public void update(FlashcardDeck deck) {
        repository.editFlashcardDeck(deck);
    }

    public void delete(FlashcardDeck deck) {
        repository.deleteDeckWithTerms(deck.getId());
    }
}