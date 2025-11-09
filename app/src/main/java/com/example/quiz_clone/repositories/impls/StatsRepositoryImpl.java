package com.example.quiz_clone.repositories.impls;

import android.content.Context;

import com.example.quiz_clone.daos.PomodoroCycleDao;
import com.example.quiz_clone.daos.QuizAttemptDao;
import com.example.quiz_clone.daos.StudySessionDao;
import com.example.quiz_clone.daos.TaskDao;
import com.example.quiz_clone.helpers.AppDatabase;

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
