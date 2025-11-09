package com.example.quiz_clone.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.quiz_clone.daos.common.AbstractGenericDao;
import com.example.quiz_clone.models.FlashcardDeck;
import com.example.quiz_clone.models.Subject;

import java.util.List;

@Dao
public abstract class SubjectDao extends AbstractGenericDao<Subject> {
    protected SubjectDao() {
        super("subject");
    }
    @RawQuery(observedEntities = {Subject.class})
    protected abstract LiveData<Subject> _findByIdLiveData(SupportSQLiteQuery query);
    @RawQuery(observedEntities = {Subject.class})
    protected abstract LiveData<List<Subject>> _findAllLiveData(SupportSQLiteQuery query);
}
