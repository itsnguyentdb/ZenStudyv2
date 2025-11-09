package com.example.quiz_clone.repositories.impls;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.quiz_clone.daos.PomodoroCycleDao;
import com.example.quiz_clone.daos.StudySessionDao;
import com.example.quiz_clone.daos.TaskDao;
import com.example.quiz_clone.helpers.AppDatabase;
import com.example.quiz_clone.models.StudySession;

import java.util.List;

public class StudySessionRepositoryImpl {
    private final StudySessionDao studySessionDao;
    private final PomodoroCycleDao pomodoroCycleDao;
    private final TaskDao taskDao;

    public StudySessionRepositoryImpl(Context context) {
        var instance = AppDatabase.getInstance(context);
        studySessionDao = instance.studySessionDao();
        pomodoroCycleDao = instance.pomodoroCycleDao();
        taskDao = instance.taskDao();
    }

    public StudySession saveSession(StudySession studySession) {
        return studySessionDao.save(studySession);
    }

    public void deleteStudySession(StudySession studySession) {
        studySessionDao.delete(studySession);
    }

    public void deleteStudySession(long studySessionId) {
        studySessionDao.deleteById(studySessionId);
    }

    public LiveData<StudySession> getStudySessionById(long studySessionId) {
        return studySessionDao.findByIdLiveData(studySessionId).orElseThrow();
    }

    public LiveData<List<StudySession>> getStudySessionsByTaskId(long taskId) {
        return studySessionDao.findStudySessionsByTaskIdLiveData(taskId);
    }
//    public int getTotalPomodoroRounds
}
