package com.example.zen_study.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.zen_study.daos.common.AbstractGenericDao;
import com.example.zen_study.models.FlashcardDeck;

import java.util.List;

@Dao
public abstract class FlashcardDeckDao extends AbstractGenericDao<FlashcardDeck> {
    protected FlashcardDeckDao() {
        super("flashcard_deck");
    }
    @RawQuery(observedEntities = {FlashcardDeck.class})
    protected abstract LiveData<FlashcardDeck> _findByIdLiveData(SupportSQLiteQuery query);
    @RawQuery(observedEntities = {FlashcardDeck.class})
    protected abstract LiveData<List<FlashcardDeck>> _findAllLiveData(SupportSQLiteQuery query);
}
