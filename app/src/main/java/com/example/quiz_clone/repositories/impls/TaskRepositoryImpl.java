package com.example.quiz_clone.repositories.impls;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.quiz_clone.daos.ResourceDao;
import com.example.quiz_clone.daos.SubjectDao;
import com.example.quiz_clone.daos.TaskDao;
import com.example.quiz_clone.helpers.AppDatabase;
import com.example.quiz_clone.models.Task;

import java.util.List;

public class TaskRepositoryImpl {
    private final TaskDao taskDao;
    private final SubjectDao subjectDao;


    public TaskRepositoryImpl(Context context) {
        var instance = AppDatabase.getInstance(context);
        subjectDao = instance.subjectDao();
        taskDao = instance.taskDao();
    }

    public void saveTask(Task task) {
        taskDao.save(task);
    }

    public LiveData<List<Task>> getTasksBySubjectId(long subjectId) {
        return new MutableLiveData<>(taskDao.findTasksBySubjectId(subjectId));
    }
}
