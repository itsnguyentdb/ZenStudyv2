package com.example.zen_study.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.zen_study.daos.common.AbstractGenericDao;
import com.example.zen_study.models.Note;

import java.util.List;

@Dao
public abstract class NoteDao extends AbstractGenericDao<Note> {
    public NoteDao() {
        super("note");
    }

    @Query("SELECT * FROM note a INNER JOIN study_session b ON a.session_id = b.id WHERE b.task_id = :taskId")
    public abstract LiveData<List<Note>> findNotesByTaskIdLiveData(long taskId);

    @RawQuery(observedEntities = {Note.class})
    protected abstract LiveData<Note> _findByIdLiveData(SupportSQLiteQuery query);

    @RawQuery(observedEntities = {Note.class})
    protected abstract LiveData<List<Note>> _findAllLiveData(SupportSQLiteQuery query);
}
