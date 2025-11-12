package com.example.zen_study.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Transaction;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.zen_study.daos.common.AbstractGenericDao;
import com.example.zen_study.models.BaseEntity;
import com.example.zen_study.models.FlashcardTerm;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Dao
public abstract class FlashcardTermDao extends AbstractGenericDao<FlashcardTerm> {
    protected FlashcardTermDao() {
        super("flashcard_term");
    }

    @Query("SELECT * FROM flashcard_term WHERE flashcard_deck_id = :deckId ORDER BY position ASC")
    public abstract LiveData<List<FlashcardTerm>> findByDeckId(long deckId);

    // Add this synchronous method
    @Query("SELECT * FROM flashcard_term WHERE flashcard_deck_id = :deckId ORDER BY position ASC")
    public abstract List<FlashcardTerm> findByDeckIdSync(long deckId);

    @Query("SELECT COUNT(id) from flashcard_term WHERE flashcard_deck_id = :deckId")
    public abstract int getFlashcardTermCount(long deckId);

    @Query("SELECT MAX(position) from flashcard_term WHERE flashcard_deck_id = :deckId")
    public abstract Integer getMaxPosition(long deckId);

    @Query("UPDATE flashcard_term SET position = :newPosition WHERE id = :termId")
    public abstract void updatePosition(long termId, int newPosition);

    @Transaction
    public FlashcardTerm insertWithAutoPosition(FlashcardTerm flashcardTerm) {
        var maxPosition = getMaxPosition(flashcardTerm.getFlashcardDeckId());
        var newPosition = (maxPosition != null ? maxPosition + 1 : 0);
        flashcardTerm.setPosition(newPosition);
        return save(flashcardTerm);
    }

    @Transaction
    public boolean reorderQuestions(long deckId) {
        try {
            var termIds = findByDeckIdSync(deckId).stream().map(FlashcardTerm::getId)
                    .collect(Collectors.toList());
            var idx = new AtomicInteger(1);
            termIds.forEach(id -> {
                updatePosition(id, idx.get());
                idx.incrementAndGet();
            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Transaction
    public boolean reorderQuestions(long deckId, Iterable<? extends Long> termIds) {
        try {
            var idSet = findByDeckId(deckId).getValue().stream()
                    .map(BaseEntity::getId)
                    .collect(Collectors.toCollection(HashSet::new));
            for (var id : termIds) {
                if (!idSet.contains(id)) {
                    return false;
                }
            }
            var idx = new AtomicInteger(1);
            termIds.forEach(id -> {
                updatePosition(id, idx.get());
                idx.incrementAndGet();
            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Query("UPDATE flashcard_term SET position = position - 1 WHERE flashcard_deck_id = :deckId AND position > :fromPosition")
    abstract void decrementPositions(long deckId, int fromPosition);

    @Transaction
    public boolean deleteTermThenReorder(long termId) {
        try {
            var term = findById(termId).orElseThrow();
            var deckId = term.getFlashcardDeckId();
            var deletedPosition = term.getPosition();

            deleteById(termId);
            decrementPositions(deckId, deletedPosition);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Query("DELETE FROM flashcard_term WHERE flashcard_deck_id = :deckId")
    public abstract void deleteByDeckId(long deckId);
    @RawQuery(observedEntities = {FlashcardTerm.class})
    protected abstract LiveData<FlashcardTerm> _findByIdLiveData(SupportSQLiteQuery query);
    @RawQuery(observedEntities = {FlashcardTerm.class})
    protected abstract LiveData<List<FlashcardTerm>> _findAllLiveData(SupportSQLiteQuery query);
}
