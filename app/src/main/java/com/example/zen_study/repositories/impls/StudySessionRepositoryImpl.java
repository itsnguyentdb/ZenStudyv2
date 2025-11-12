package com.example.zen_study.repositories.impls;

import android.content.Context;

import com.example.zen_study.daos.PomodoroCycleDao;
import com.example.zen_study.daos.StudySessionDao;
import com.example.zen_study.helpers.AppDatabase;
import com.example.zen_study.models.PomodoroCycle;
import com.example.zen_study.models.StudySession;

import java.util.List;
import java.util.concurrent.Executor;

public class StudySessionRepositoryImpl {
    private final StudySessionDao studySessionDao;
    private final PomodoroCycleDao pomodoroCycleDao;
    private final Executor executor;

    public StudySessionRepositoryImpl(Context context) {
        var instance = AppDatabase.getInstance(context);
        studySessionDao = instance.studySessionDao();
        pomodoroCycleDao = instance.pomodoroCycleDao();
        executor = instance.getQueryExecutor();
    }

    public void getSessionsByTaskId(long taskId, OnStudySessionOperationComplete callback) {
        executor.execute(() -> {
            try {
                List<StudySession> sessions = studySessionDao.getSessionsByTaskId(taskId);
                if (callback != null) {
                    callback.onSuccess(sessions);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void insertSession(StudySession session, OnStudySessionOperationComplete callback) {
        executor.execute(() -> {
            try {
                studySessionDao.save(session);
                // Return updated list
                List<StudySession> sessions = studySessionDao.getSessionsByTaskId(session.getTaskId());
                if (callback != null) {
                    callback.onSuccess(sessions);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void insertPomodoroCycle(PomodoroCycle currentPomodoroCycle) {
        executor.execute(() -> {
            pomodoroCycleDao.save(currentPomodoroCycle);
        });
    }

    public interface OnStudySessionOperationComplete {
        void onSuccess(List<StudySession> sessions);

        void onError(Exception e);
    }
}
