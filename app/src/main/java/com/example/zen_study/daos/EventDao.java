package com.example.zen_study.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.zen_study.daos.common.AbstractGenericDao;
import com.example.zen_study.models.Event;

import java.util.List;

@Dao
public abstract class EventDao extends AbstractGenericDao<Event> {
    public EventDao() {
        super("event");
    }
    @RawQuery(observedEntities = {Event.class})
    protected abstract LiveData<Event> _findByIdLiveData(SupportSQLiteQuery query);
    @RawQuery(observedEntities = {Event.class})
    protected abstract LiveData<List<Event>> _findAllLiveData(SupportSQLiteQuery query);
}
