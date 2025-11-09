package com.example.quiz_clone.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.quiz_clone.daos.common.AbstractGenericDao;
import com.example.quiz_clone.models.FlashcardDeck;
import com.example.quiz_clone.models.Task;

import java.util.List;

@Dao
public abstract class TaskDao extends AbstractGenericDao<Task> {
    public TaskDao() {
        super("task");
    }

    @Query("SELECT * FROM task WHERE subject_id = :subjectId")
    public abstract List<Task> findTasksBySubjectId(long subjectId);

    @RawQuery(observedEntities = {Task.class})
    protected abstract LiveData<Task> _findByIdLiveData(SupportSQLiteQuery query);

    @RawQuery(observedEntities = {Task.class})
    protected abstract LiveData<List<Task>> _findAllLiveData(SupportSQLiteQuery query);
}
