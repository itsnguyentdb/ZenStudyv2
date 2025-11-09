package com.example.quiz_clone.repositories.impls;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.quiz_clone.daos.FlashcardDeckDao;
import com.example.quiz_clone.daos.FlashcardTermDao;
import com.example.quiz_clone.helpers.AppDatabase;
import com.example.quiz_clone.models.FlashcardDeck;
import com.example.quiz_clone.models.FlashcardTerm;
import com.example.quiz_clone.repositories.FlashcardRepository;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

public class FlashcardRepositoryImpl implements FlashcardRepository {
    private final FlashcardDeckDao flashcardDeckDao;
    private final FlashcardTermDao flashcardTermDao;
    private final Executor executor;

    public FlashcardRepositoryImpl(Context context) {
        var instance = AppDatabase.getInstance(context);
        this.flashcardDeckDao = instance.flashcardDeckDao();
        this.flashcardTermDao = instance.flashcardTermDao();
        this.executor = instance.getQueryExecutor();
    }

    @Override
    public FlashcardDeck createFlashcardDeck(String title) {
        return flashcardDeckDao.save(FlashcardDeck.builder()
                .title(title)
                .createdTime(new Date())
                .lastUpdatedTime(new Date())
                .build()
        );
    }

    @Override
    public FlashcardDeck createFlashcardDeck(String title, String description) {
        final var result = new FlashcardDeck[1];
        executor.execute(() -> {
            result[0] = flashcardDeckDao.save(FlashcardDeck.builder()
                    .title(title)
                    .description(description)
                    .createdTime(new Date())
                    .lastUpdatedTime(new Date())
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

    @Override
    public void addTermToDeck(long deckId, String term, String definition) {
        flashcardTermDao.save(FlashcardTerm.builder()
                .flashcardDeckId(deckId)
                .term(term)
                .definition(definition)
                .build());
    }

    @Override
    public LiveData<FlashcardDeck> getDeckById(long deckId) {
        var entityLiveData = flashcardDeckDao.findByIdLiveData(deckId);
        return Objects.requireNonNull(entityLiveData).orElse(null);
    }

    @Override
    public LiveData<List<FlashcardDeck>> getAllDecks() {
        return flashcardDeckDao.findAllLiveData();
    }

    @Override
    public LiveData<List<FlashcardTerm>> getTermsForDeck(long deckId) {
        return flashcardTermDao.findByDeckId(deckId);
    }

    @Override
    public FlashcardDeck editFlashcardDeck(FlashcardDeck flashcardDeck) {
        flashcardDeck.setLastUpdatedTime(new Date());
        return flashcardDeckDao.save(flashcardDeck);
    }

    @Override
    public void deleteDeckWithTerms(long deckId) {
        executor.execute(() -> {
            flashcardTermDao.deleteByDeckId(deckId);
            flashcardDeckDao.deleteById(deckId);
        });
    }

    @Override
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
}
