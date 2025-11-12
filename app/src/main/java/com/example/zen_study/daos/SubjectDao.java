package com.example.zen_study.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.RawQuery;
import androidx.room.Transaction;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.zen_study.daos.common.AbstractGenericDao;
import com.example.zen_study.models.Subject;

import java.util.List;
import java.util.Optional;

@Dao
public abstract class SubjectDao extends AbstractGenericDao<Subject> {
    protected SubjectDao() {
        super("subject");
    }

    @RawQuery
    protected abstract Subject _findByName(SupportSQLiteQuery query);

    @Transaction
    public Optional<Subject> findByName(String name) {
        SimpleSQLiteQuery query = new SimpleSQLiteQuery(
                "SELECT * FROM " + tableName + " WHERE name = ?",
                new Object[]{name}
        );
        var entity = _findById(query);
        return entity != null ? Optional.of(entity)
                : Optional.empty();
    }

    @RawQuery(observedEntities = {Subject.class})
    protected abstract LiveData<Subject> _findByIdLiveData(SupportSQLiteQuery query);

    @RawQuery(observedEntities = {Subject.class})
    protected abstract LiveData<List<Subject>> _findAllLiveData(SupportSQLiteQuery query);
}
