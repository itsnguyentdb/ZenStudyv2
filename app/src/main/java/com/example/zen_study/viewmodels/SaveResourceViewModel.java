package com.example.zen_study.viewmodels;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.zen_study.models.Resource;
import com.example.zen_study.models.Task;
import com.example.zen_study.models.Subject;
import com.example.zen_study.repositories.impls.ResourceRepositoryImpl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import lombok.Getter;

public class SaveResourceViewModel extends AndroidViewModel {

    private final ResourceRepositoryImpl resourceRepository;

    //region Getters for LiveData
    // LiveData for lists
    @Getter
    private final LiveData<List<Resource>> allResources;
    @Getter
    private final LiveData<List<Task>> allTasks;
    @Getter
    private final LiveData<List<Subject>> allSubjects;

    // LiveData for single resource (for edit mode)
    private final MutableLiveData<Long> currentResourceId = new MutableLiveData<>();
    @Getter
    private final LiveData<Optional<Resource>> currentResource;

    // Operation results
    private final MutableLiveData<OperationResult> operationResult = new MutableLiveData<>();

    // Loading states
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> loadingMessage = new MutableLiveData<>("");

    public SaveResourceViewModel(@NonNull Application application) {
        super(application);
        resourceRepository = new ResourceRepositoryImpl(application.getApplicationContext());

        // Initialize LiveData
        allResources = resourceRepository.getAllResources();
        allTasks = resourceRepository.getAllTasks();
        allSubjects = resourceRepository.getAllSubjects();

        // Transform currentResourceId to currentResource
        currentResource = Transformations.switchMap(currentResourceId, resourceId -> {
            if (resourceId != null) {
                return resourceRepository.getResourceById(resourceId);
            } else {
                MutableLiveData<Optional<Resource>> emptyResult = new MutableLiveData<>();
                emptyResult.setValue(Optional.empty());
                return emptyResult;
            }
        });
    }

    public LiveData<OperationResult> getOperationResult() {
        return operationResult;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getLoadingMessage() {
        return loadingMessage;
    }
    //endregion

    //region Resource CRUD Operations
    public void createResource(Uri fileUri, Long taskId, Long subjectId, String title, String type) {
        setLoading(true, "Creating resource...");

        new Thread(() -> {
            try {
                Resource resource = resourceRepository.createResource(fileUri, taskId, subjectId, title, type);
                operationResult.postValue(new OperationResult(true, "Resource '" + title + "' created successfully", resource.getId()));
            } catch (Exception e) {
                operationResult.postValue(new OperationResult(false, "Failed to create resource: " + e.getMessage()));
            } finally {
                setLoading(false, "");
            }
        }).start();
    }

    public void updateResource(Long resourceId, String title, String type, Long taskId, Long subjectId) {
        setLoading(true, "Updating resource...");

        new Thread(() -> {
            try {
                resourceRepository.updateResource(resourceId, title, type, taskId, subjectId);
                operationResult.postValue(new OperationResult(true, "Resource updated successfully", resourceId));
            } catch (Exception e) {
                operationResult.postValue(new OperationResult(false, "Failed to update resource: " + e.getMessage()));
            } finally {
                setLoading(false, "");
            }
        }).start();
    }

    public void deleteResource(Long resourceId) {
        setLoading(true, "Deleting resource...");

        new Thread(() -> {
            try {
                resourceRepository.deleteResource(resourceId);
                operationResult.postValue(new OperationResult(true, "Resource deleted successfully", resourceId));
            } catch (Exception e) {
                operationResult.postValue(new OperationResult(false, "Failed to delete resource: " + e.getMessage()));
            } finally {
                setLoading(false, "");
            }
        }).start();
    }

    public void loadResourceForEdit(Long resourceId) {
        setLoading(true, "Loading resource...");
        currentResourceId.postValue(resourceId);
    }
    //endregion

    //region Filtering
    public LiveData<List<Resource>> getFilteredResources(Long taskId, Long subjectId) {
        return Transformations.map(allResources, resources -> {
            if (resources == null) return null;

            List<Resource> filtered = resources;

            // Apply task filter
            if (taskId != null) {
                filtered = filterByTask(filtered, taskId);
            }

            // Apply subject filter
            if (subjectId != null) {
                filtered = filterBySubject(filtered, subjectId);
            }

            return filtered;
        });
    }

    private List<Resource> filterByTask(List<Resource> resources, Long taskId) {
        java.util.List<Resource> filtered = new java.util.ArrayList<>();
        for (Resource resource : resources) {
            if (taskId.equals(resource.getTaskId())) {
                filtered.add(resource);
            }
        }
        return filtered;
    }

    private List<Resource> filterBySubject(List<Resource> resources, Long subjectId) {
        java.util.List<Resource> filtered = new java.util.ArrayList<>();
        for (Resource resource : resources) {
            if (subjectId.equals(resource.getSubjectId())) {
                filtered.add(resource);
            }
        }
        return filtered;
    }
    //endregion

    //region Utility Methods
    public void clearOperationResult() {
        operationResult.setValue(null);
    }

    public void setCurrentResourceId(Long resourceId) {
        currentResourceId.setValue(resourceId);
    }

    public void setLoading(boolean loading, String message) {
        isLoading.postValue(loading);
        loadingMessage.postValue(message);
    }

    public void resetState() {
        currentResourceId.setValue(null);
        operationResult.setValue(null);
        isLoading.setValue(false);
        loadingMessage.setValue("");
    }
    //endregion

    //region Operation Result Class
    @Getter
    public static class OperationResult {
        private final boolean success;
        private final String message;
        private final Long resourceId;

        public OperationResult(boolean success, String message) {
            this(success, message, null);
        }

        public OperationResult(boolean success, String message, Long resourceId) {
            this.success = success;
            this.message = message;
            this.resourceId = resourceId;
        }

    }
    //endregion
}
