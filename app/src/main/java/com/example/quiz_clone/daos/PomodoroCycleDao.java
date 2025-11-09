package com.example.quiz_clone.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.quiz_clone.daos.common.AbstractGenericDao;
import com.example.quiz_clone.models.FlashcardDeck;
import com.example.quiz_clone.models.PomodoroCycle;

import java.util.List;

@Dao
public abstract class PomodoroCycleDao extends AbstractGenericDao<PomodoroCycle> {
    public PomodoroCycleDao() {
        super("pomodoro_cycle");
    }

    @RawQuery(observedEntities = {PomodoroCycle.class})
    protected abstract LiveData<PomodoroCycle> _findByIdLiveData(SupportSQLiteQuery query);

    @RawQuery(observedEntities = {PomodoroCycle.class})
    protected abstract LiveData<List<PomodoroCycle>> _findAllLiveData(SupportSQLiteQuery query);
}
