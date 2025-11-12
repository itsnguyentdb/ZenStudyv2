package com.example.zen_study.repositories;

import androidx.lifecycle.LiveData;

import com.example.zen_study.models.FlashcardDeck;
import com.example.zen_study.models.FlashcardTerm;

import java.util.List;

public interface FlashcardRepository {
    FlashcardDeck createFlashcardDeck(String title);

    FlashcardDeck createFlashcardDeck(String title, String description);

    void addTermToDeck(long deckId, String term, String definition);

    LiveData<FlashcardDeck> getDeckLiveDataById(long deckId);

    LiveData<List<FlashcardDeck>> getAllDecks();

    LiveData<List<FlashcardTerm>> getTermsForDeck(long deckId);

    FlashcardDeck editFlashcardDeck(FlashcardDeck flashcardDeck);

    void deleteDeckWithTerms(long deckId);

    FlashcardTerm updateFlashcardTerm(FlashcardTerm flashcardTerm);
}
