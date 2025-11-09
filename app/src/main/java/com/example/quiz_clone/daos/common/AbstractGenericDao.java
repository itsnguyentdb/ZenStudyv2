package com.example.quiz_clone.daos.common;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Dao;
import androidx.room.RawQuery;
import androidx.room.Transaction;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.quiz_clone.models.BaseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class AbstractGenericDao<E extends BaseEntity> implements GenericDao<E> {
    protected final String tableName;

    @Override
    @Transaction
    public E save(E entity) {
        if (entity.getId() == null || entity.getId() == 0) {
            long newId = _insert(entity);
            if (newId <= -1) {
                Log.e("DAO", String.format(
                        "Failed to insert entity of type %s into the database: %s",
                        entity.getClass().getSimpleName(),
                        Objects.toString(entity)
                ));
            }
            entity.setId(newId);
            return entity;
        }
        if (_update(entity) > 0) {
            return Objects.requireNonNull(findById(entity.getId())).orElse(null);
        }
        Log.e("DAO", String.format(
                "Failed to update entity of type %s into the database: %s",
                entity.getClass().getSimpleName(),
                Objects.toString(entity)
        ));
        return entity;
    }

    @Transaction
    public List<E> insertAll(Iterable<? extends E> entities) {
        try {
            var entityList = new ArrayList<E>();
            entities.forEach(e -> {
                entityList.add(save(e));
            });
            return entityList;
        } catch (Exception e) {
            Log.e("DAO", String.format(
                    "Failed to update entity of type %s into the database: %s",
                    entities.getClass().getSimpleName(),
                    Objects.toString(entities)
            ));
            return null;
        }
    }

    protected abstract LiveData<E> _findByIdLiveData(SupportSQLiteQuery query);

    public Optional<LiveData<E>> findByIdLiveData(long id) {
        SimpleSQLiteQuery query = new SimpleSQLiteQuery(
                "SELECT * FROM " + tableName + " WHERE id = ?",
                new Object[]{id}
        );
        var entity = _findByIdLiveData(query);
        return entity != null ? Optional.of(entity)
                : Optional.empty();
    }

    @RawQuery
    protected abstract E _findById(SupportSQLiteQuery query);

    @Transaction
    public Optional<E> findById(long id) {
        SimpleSQLiteQuery query = new SimpleSQLiteQuery(
                "SELECT * FROM " + tableName + " WHERE id = ?",
                new Object[]{id}
        );
        var entity = _findById(query);
        return entity != null ? Optional.of(_findById(query))
                : Optional.empty();
    }

    protected abstract LiveData<List<E>> _findAllLiveData(SupportSQLiteQuery query);

    public LiveData<List<E>> findAllLiveData() {
        SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT * FROM " + tableName);
        return _findAllLiveData(query);
    }

    @RawQuery
    protected abstract List<E> _findAll(SupportSQLiteQuery query);

    @Transaction
    public List<E> findAll() {
        SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT * FROM " + tableName);
        return _findAll(query);
    }

    @RawQuery
    protected abstract int _deleteById(SupportSQLiteQuery query);

    @Transaction
    public void deleteById(long id) {
        SimpleSQLiteQuery query = new SimpleSQLiteQuery(
                "DELETE FROM " + tableName + " WHERE id = ?",
                new Object[]{id}
        );
        _deleteById(query);
    }

    @RawQuery
    protected abstract int _deleteAll(SupportSQLiteQuery query);

    @Transaction
    public void deleteAll() {
        var query = new SimpleSQLiteQuery("DELETE FROM " + tableName);
        _deleteAll(query);
    }

    @Transaction
    public void deleteAll(Iterable<E> entities) {
        List<E> entityList = new ArrayList<>();
        for (E entity : entities) {
            entityList.add(entity);
        }
        deleteAll(entityList);
    }

    @Transaction
    public void deleteAllInBatch(Iterable<E> entities) {
        List<Long> ids = new ArrayList<>();
        for (E entity : entities) {
            if (entity.getId() != null) {
                ids.add(entity.getId());
            }
        }
        if (!ids.isEmpty()) {
            deleteAllByIdsInBatch(ids);
        }
    }

    @Transaction
    public void deleteAllByIds(Iterable<? extends Long> ids) {
        for (Long id : ids) {
            deleteById(id);
        }
    }

    @RawQuery
    protected abstract int _deleteAllByIdsInBatch(SupportSQLiteQuery query);

    @Transaction
    public void deleteAllByIdsInBatch(Iterable<? extends Long> ids) {
        if (!ids.iterator().hasNext()) {
            return;
        }

        var queryBuilder = new StringBuilder("DELETE FROM ")
                .append(tableName)
                .append(" WHERE id IN (");

        List<Long> idList = new ArrayList<>();
        final int[] index = {0};
        ids.forEach(id -> {
            if (index[0] > 0) {
                queryBuilder.append(",");
            }
            queryBuilder.append("?");
            idList.add(id);
            index[0]++;
        });
        queryBuilder.append(")");

        var query = new SimpleSQLiteQuery(
                queryBuilder.toString(),
                idList.toArray()
        );
        _deleteAllByIdsInBatch(query);
    }

    @RawQuery
    protected abstract long _count(SupportSQLiteQuery query);

    @Transaction
    public long count() {
        SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT COUNT(id) FROM " + tableName);
        return _count(query);
    }
}
