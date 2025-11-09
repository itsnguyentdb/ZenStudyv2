package com.example.quiz_clone.daos.common;

import androidx.lifecycle.LiveData;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;

import com.example.quiz_clone.models.BaseEntity;

import java.util.List;
import java.util.Optional;

public interface GenericDao<E extends BaseEntity> {
    @Insert
    long _insert(E entity);

    @Insert
    void _insertAll(List<? extends E> entities);

    @Update
    int _update(E entity);

    @Delete
    void delete(E entity);

    E save(E entity);

    @Delete
    void deleteAll(List<E> entities);

}
