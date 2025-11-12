package com.example.zen_study.repositories.impls;

import android.content.Context;

import com.example.zen_study.daos.PomodoroCycleDao;
import com.example.zen_study.daos.QuizAttemptDao;
import com.example.zen_study.daos.StudySessionDao;
import com.example.zen_study.daos.TaskDao;
import com.example.zen_study.helpers.AppDatabase;

public class StatsRepositoryImpl {
    private final StudySessionDao studySessionDao;
    private final TaskDao taskDao;
    private final PomodoroCycleDao pomodoroCycleDao;
    private final QuizAttemptDao quizAttemptDao;

    public StatsRepositoryImpl(Context context) {
        var instance = AppDatabase.getInstance(context);
        studySessionDao = instance.studySessionDao();
        taskDao = instance.taskDao();
        pomodoroCycleDao = instance.pomodoroCycleDao();
        quizAttemptDao = instance.quizAttemptDao();
    }

//    public long getTotalMinutesToday() {
//
//    }
//
//    public double getTaskCompletionRate() {
//
//    }
}
