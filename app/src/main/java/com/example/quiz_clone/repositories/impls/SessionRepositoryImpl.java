package com.example.quiz_clone.repositories.impls;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.quiz_clone.daos.PomodoroCycleDao;
import com.example.quiz_clone.daos.StudySessionDao;
import com.example.quiz_clone.helpers.AppDatabase;
import com.example.quiz_clone.models.StudySession;

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
