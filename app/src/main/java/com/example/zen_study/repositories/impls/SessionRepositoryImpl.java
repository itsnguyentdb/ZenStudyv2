package com.example.zen_study.repositories.impls;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.zen_study.daos.PomodoroCycleDao;
import com.example.zen_study.daos.StudySessionDao;
import com.example.zen_study.helpers.AppDatabase;
import com.example.zen_study.models.StudySession;

import java.util.List;
import java.util.concurrent.Executor;

public class SessionRepositoryImpl {
    private final StudySessionDao studySessionDao;
    private final PomodoroCycleDao pomodoroCycleDao;
    private final Executor executor;

    public SessionRepositoryImpl(Context context) {
        var instance = AppDatabase.getInstance(context);
        executor = instance.getQueryExecutor();
        studySessionDao = instance.studySessionDao();
        pomodoroCycleDao = instance.pomodoroCycleDao();
    }

    public LiveData<List<StudySession>> getAllSessions() {
        return new MutableLiveData<>(studySessionDao.findAll());
    }
}
