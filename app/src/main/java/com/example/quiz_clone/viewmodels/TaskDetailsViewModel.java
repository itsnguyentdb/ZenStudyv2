package com.example.quiz_clone.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.quiz_clone.models.StudySession;
import com.example.quiz_clone.models.Task;
import com.example.quiz_clone.repositories.impls.StudySessionRepositoryImpl;
import com.example.quiz_clone.repositories.impls.TaskRepositoryImpl;

import java.util.List;

public class TaskDetailsViewModel extends AndroidViewModel {

    private final TaskRepositoryImpl taskRepository;
    private final StudySessionRepositoryImpl studySessionRepository;

    private final MutableLiveData<Task> task = new MutableLiveData<>();
    private final MutableLiveData<List<StudySession>> studySessions = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public TaskDetailsViewModel(@NonNull Application application) {
        super(application);
        taskRepository = new TaskRepositoryImpl(application);
        studySessionRepository = new StudySessionRepositoryImpl(application);
    }

    public void loadTaskData(long taskId) {
        isLoading.setValue(true);

        // Load task
        taskRepository.getTaskById(taskId, new TaskRepositoryImpl.OnTaskOperationComplete() {
            @Override
            public void onSuccess(Task loadedTask) {
                task.postValue(loadedTask);
                loadStudySessions(taskId);
            }

            @Override
            public void onError(Exception e) {
                isLoading.postValue(false);
                error.postValue("Failed to load task: " + e.getMessage());
            }
        });
    }

    private void loadStudySessions(long taskId) {
        studySessionRepository.getSessionsByTaskId(taskId, new StudySessionRepositoryImpl.OnStudySessionOperationComplete() {
            @Override
            public void onSuccess(List<StudySession> sessions) {
                studySessions.postValue(sessions);
                isLoading.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                isLoading.postValue(false);
                error.postValue("Failed to load study sessions: " + e.getMessage());
            }
        });
    }

    public void addStudySession(StudySession session) {
        studySessionRepository.insertSession(session, new StudySessionRepositoryImpl.OnStudySessionOperationComplete() {
            @Override
            public void onSuccess(List<StudySession> sessions) {
                studySessions.postValue(sessions);
                // Update task progress
                updateTaskProgress();
            }

            @Override
            public void onError(Exception e) {
                error.postValue("Failed to save session: " + e.getMessage());
            }
        });
    }

    private void updateTaskProgress() {
        Task currentTask = task.getValue();
        if (currentTask != null) {
            List<StudySession> sessions = studySessions.getValue();
            if (sessions != null) {
                int totalStudyTime = sessions.stream().mapToInt(session -> (int) session.getDuration()).sum();
                float progress = Math.min(1.0f, (float) totalStudyTime / currentTask.getExpectedDuration());

                currentTask.setProgress(progress);
                currentTask.setProgressDuration(totalStudyTime);

                // Auto-update status
                updateTaskStatusAutomatically(currentTask, totalStudyTime);

                taskRepository.updateTask(currentTask, new TaskRepositoryImpl.OnTaskOperationComplete() {
                    @Override
                    public void onSuccess(Task task) {
                        // Task updated successfully
                    }

                    @Override
                    public void onError(Exception e) {
                        error.postValue("Failed to update task progress: " + e.getMessage());
                    }
                });
            }
        }
    }

    private void updateTaskStatusAutomatically(Task task, int totalStudyTime) {
        if (totalStudyTime >= task.getExpectedDuration()) {
            task.setStatus(Task.TaskType.COMPLETED);
        } else if (task.getDeadline() != null && System.currentTimeMillis() > task.getDeadline().getTime()) {
            task.setStatus(Task.TaskType.OVERDUE);
        } else if (totalStudyTime > 0) {
            task.setStatus(Task.TaskType.IN_PROGRESS);
        } else {
            task.setStatus(Task.TaskType.TODO);
        }
    }

    // Getters for LiveData
    public LiveData<Task> getTask() { return task; }
    public LiveData<List<StudySession>> getStudySessions() { return studySessions; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getError() { return error; }
}