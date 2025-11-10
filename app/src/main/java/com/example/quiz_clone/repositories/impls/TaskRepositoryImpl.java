package com.example.quiz_clone.repositories.impls;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.example.quiz_clone.daos.SubjectDao;
import com.example.quiz_clone.daos.TaskDao;
import com.example.quiz_clone.helpers.AppDatabase;
import com.example.quiz_clone.models.Task;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

public class TaskRepositoryImpl {
    private final TaskDao taskDao;
    private final SubjectDao subjectDao;
    private final Executor executor;
    private final Handler mainHandler;


    public TaskRepositoryImpl(Context context) {
        var instance = AppDatabase.getInstance(context);
        subjectDao = instance.subjectDao();
        taskDao = instance.taskDao();
        executor = instance.getQueryExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void insertTask(Task task, OnTaskOperationComplete callback) {
        executor.execute(() -> {
            try {
                task.setCreatedAt(new Date());
                task.setLastUpdatedAt(new Date());
                var savedTask = taskDao.save(task);
                if (callback != null) {
                    mainHandler.post(() -> callback.onSuccess(savedTask));
                }
            } catch (Exception e) {
                if (callback != null) {
                    mainHandler.post(() -> callback.onError(e));
                }
            }
        });
    }

    public void updateTask(Task task, OnTaskOperationComplete callback) {
        executor.execute(() -> {
            try {
                task.setLastUpdatedAt(new Date());
                var savedTask = taskDao.save(task);
                if (callback != null) {
                    mainHandler.post(() -> callback.onSuccess(savedTask));
                }
            } catch (Exception e) {
                if (callback != null) {
                    mainHandler.post(() -> callback.onError(e));
                }
            }
        });
    }

    public LiveData<List<Task>> getTasksBySubject(long subjectId) {
        return taskDao.getTasksBySubject(subjectId);
    }

    public LiveData<List<Task>> getTodayTasks() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date start = calendar.getTime();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date end = calendar.getTime();

        return taskDao.getTasksByDateRange(start, end);
    }

    public void updateTaskProgress(long taskId, float progress, int progressDuration) {
        executor.execute(() -> {
            taskDao.updateProgress(taskId, progress, progressDuration);
        });
    }

    public void deleteTaskWithSubtasks(Long taskId) {
        taskDao.deleteTasksByParentId(taskId);
        taskDao.deleteById(taskId);
    }

    public LiveData<List<Task>> getTasksByStatus(Task.TaskType status) {
        return null;
    }

    public LiveData<List<Task>> getAllTasks() {
        return taskDao.getRootTasks();
    }

    public void getTaskById(long taskId, OnTaskOperationComplete callback) {
        executor.execute(() -> {
            try {
                // Simulate network/database delay
                Thread.sleep(500);

                // Create a mock task for testing
                Task task = taskDao.findById(taskId).orElse(null);

                if (task != null) {
                    callback.onSuccess(task);
                } else {
                    callback.onError(new Exception("Task not found with ID: " + taskId));
                }
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }


    public interface OnTaskOperationComplete {
        void onSuccess(Task task);

        void onError(Exception e);
    }
}
