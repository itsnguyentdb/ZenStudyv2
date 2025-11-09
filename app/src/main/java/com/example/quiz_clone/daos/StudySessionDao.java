package com.example.quiz_clone.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.quiz_clone.daos.common.AbstractGenericDao;
import com.example.quiz_clone.models.FlashcardDeck;
import com.example.quiz_clone.models.StudySession;

import java.util.List;

@Dao
public abstract class StudySessionDao extends AbstractGenericDao<StudySession> {
    protected StudySessionDao() {
        super("study_session");
    }

    @Query("SELECT * FROM study_session WHERE task_id = :taskId")
    public abstract LiveData<List<StudySession>> findStudySessionsByTaskIdLiveData(long taskId);
    @RawQuery(observedEntities = {StudySession.class})
    protected abstract LiveData<StudySession> _findByIdLiveData(SupportSQLiteQuery query);
    @RawQuery(observedEntities = {StudySession.class})
    protected abstract LiveData<List<StudySession>> _findAllLiveData(SupportSQLiteQuery query);
}
