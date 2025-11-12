package com.example.zen_study.daos.common;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;

import com.example.zen_study.models.BaseEntity;

import java.util.List;

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
