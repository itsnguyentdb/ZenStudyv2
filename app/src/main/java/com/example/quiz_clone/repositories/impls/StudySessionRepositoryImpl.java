package com.example.quiz_clone.repositories.impls;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.quiz_clone.daos.PomodoroCycleDao;
import com.example.quiz_clone.daos.StudySessionDao;
import com.example.quiz_clone.daos.TaskDao;
import com.example.quiz_clone.helpers.AppDatabase;
import com.example.quiz_clone.models.StudySession;

import java.util.List;
import java.util.concurrent.Executor;

public class StudySessionRepositoryImpl {
    private final StudySessionDao studySessionDao;
    private final Executor executor;

    public StudySessionRepositoryImpl(Context context) {
        var instance = AppDatabase.getInstance(context);
        studySessionDao = instance.studySessionDao();
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

    public interface OnStudySessionOperationComplete {
        void onSuccess(List<StudySession> sessions);

        void onError(Exception e);
    }
}
