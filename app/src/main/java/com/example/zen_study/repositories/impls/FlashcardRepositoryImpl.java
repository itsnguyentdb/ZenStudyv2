package com.example.zen_study.repositories.impls;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.zen_study.daos.FlashcardDeckDao;
import com.example.zen_study.daos.FlashcardTermDao;
import com.example.zen_study.helpers.AppDatabase;
import com.example.zen_study.models.FlashcardDeck;
import com.example.zen_study.models.FlashcardTerm;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;

public class FlashcardRepositoryImpl {
    private final FlashcardDeckDao flashcardDeckDao;
    private final FlashcardTermDao flashcardTermDao;
    private final Executor executor;

    public FlashcardRepositoryImpl(Context context) {
        var instance = AppDatabase.getInstance(context);
        this.flashcardDeckDao = instance.flashcardDeckDao();
        this.flashcardTermDao = instance.flashcardTermDao();
        this.executor = instance.getQueryExecutor();
    }

    public FlashcardDeck createFlashcardDeck(String title, String description, long subjectId) {
        final var result = new FlashcardDeck[1];
        executor.execute(() -> {
            result[0] = flashcardDeckDao.save(FlashcardDeck.builder()
                    .title(title)
                    .description(description)
                    .subjectId(subjectId)
                    .createdAt(new Date())
                    .lastUpdatedAt(new Date())
                    .build()
            );
        });
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return result[0] != null ? result[0] : null;
    }

    public void addTermToDeck(long deckId, String term, String definition, int position) {
        flashcardTermDao.save(FlashcardTerm.builder()
                .flashcardDeckId(deckId)
                .term(term)
                .definition(definition)
                .position(position)
                .build());
    }

    public LiveData<FlashcardDeck> getDeckLiveDataById(long deckId) {
        var entityLiveData = flashcardDeckDao.findByIdLiveData(deckId);
        return Transformations.map(entityLiveData, optional -> optional.orElse(null));
    }

    public Optional<FlashcardDeck> getDeckById(long deckId) {
        return flashcardDeckDao.findById(deckId);
    }

    public LiveData<List<FlashcardDeck>> getAllDecks() {
        return flashcardDeckDao.findAllLiveData();
    }

    public LiveData<List<FlashcardTerm>> getTermsLiveDataForDeck(long deckId) {
        return flashcardTermDao.findByDeckId(deckId);
    }

    public List<FlashcardTerm> getTermsForDeck(long deckId) {
        final List<FlashcardTerm>[] result = new List[1];
        executor.execute(() -> {
            result[0] = flashcardTermDao.findByDeckIdSync(deckId);
        });

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return result[0] != null ? result[0] : new ArrayList<>();
    }

    public FlashcardDeck editFlashcardDeck(FlashcardDeck flashcardDeck) {
        flashcardDeck.setLastUpdatedAt(new Date());
        return flashcardDeckDao.save(flashcardDeck);
    }

    public void deleteDeckWithTerms(long deckId) {
        executor.execute(() -> {
            flashcardTermDao.deleteByDeckId(deckId);
            flashcardDeckDao.deleteById(deckId);
        });
    }

    public FlashcardTerm updateFlashcardTerm(FlashcardTerm flashcardTerm) {
        final FlashcardTerm[] result = new FlashcardTerm[1];
        executor.execute(() -> {
            result[0] = flashcardTermDao.save(flashcardTerm);
        });

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return result[0] != null ? result[0] : flashcardTerm;
    }

    public void deleteTermsByDeckId(long deckId) {
        flashcardTermDao.deleteByDeckId(deckId);
    }
}
