package com.example.quiz_clone.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.quiz_clone.models.Subject;
import com.example.quiz_clone.models.Task;
import com.example.quiz_clone.repositories.impls.SubjectRepositoryImpl;
import com.example.quiz_clone.repositories.impls.TaskRepositoryImpl;

import java.util.List;
import java.util.Optional;

import lombok.Getter;

public class TaskLibraryViewModel extends AndroidViewModel {
    private final TaskRepositoryImpl taskRepository;
    private final SubjectRepositoryImpl subjectRepository;

    @Getter
    private final LiveData<List<Task>> allTasks;
    private final LiveData<List<Subject>> allSubjects;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<Task.TaskType> currentFilter = new MutableLiveData<>(null);

    public TaskLibraryViewModel(Application application) {
        super(application);
        taskRepository = new TaskRepositoryImpl(application);
        subjectRepository = new SubjectRepositoryImpl(application);
        allTasks = taskRepository.getAllTasks();
        allSubjects = subjectRepository.getAllSubjects();
    }

    public LiveData<List<Subject>> getSubjects() {
        return allSubjects;
    }

    public LiveData<List<Task>> getTasksBySubject(long subjectId) {
        return taskRepository.getTasksBySubject(subjectId);
    }

    public LiveData<List<Task>> getTodayTasks() {
        return taskRepository.getTodayTasks();
    }

    public LiveData<List<Task>> getTasksByStatus(Task.TaskType status) {
        return taskRepository.getTasksByStatus(status);
    }

    public void updateTask(Task task) {
        taskRepository.updateTask(task, null);
    }

    public void deleteTask(Task task) {
        taskRepository.deleteTaskWithSubtasks(task.getId());
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void setCurrentFilter(Task.TaskType filter) {
        currentFilter.setValue(filter);
    }

    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }

    public LiveData<Task.TaskType> getCurrentFilter() {
        return currentFilter;
    }

    public LiveData<List<Task>> getFilteredTasks() {
        // This would combine search and filter logic
        // Implementation depends on your repository structure
        return allTasks;
    }
}
