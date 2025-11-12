package com.example.zen_study.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.zen_study.daos.common.AbstractGenericDao;
import com.example.zen_study.models.Resource;

import java.util.List;

@Dao
public abstract class ResourceDao extends AbstractGenericDao<Resource> {
    protected ResourceDao() {
        super("resource");
    }

    @Query("SELECT * FROM resource WHERE subject_id = :subjectId")
    public abstract LiveData<List<Resource>> findResourcesBySubjectId(long subjectId);

    @RawQuery(observedEntities = {Resource.class})
    protected abstract LiveData<Resource> _findByIdLiveData(SupportSQLiteQuery query);

    @RawQuery(observedEntities = {Resource.class})
    protected abstract LiveData<List<Resource>> _findAllLiveData(SupportSQLiteQuery query);
}
