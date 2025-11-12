package com.example.zen_study.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.zen_study.models.FileMetadata;
import com.example.zen_study.models.Resource;
import com.example.zen_study.models.Task;
import com.example.zen_study.models.Subject;
import com.example.zen_study.repositories.impls.ResourceRepositoryImpl;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class ResourceLibraryViewModel extends AndroidViewModel {

    private final ResourceRepositoryImpl resourceRepository;

    @Getter
    private final LiveData<List<Resource>> allResources;
    @Getter
    private final LiveData<List<Task>> allTasks;
    @Getter
    private final LiveData<List<Subject>> allSubjects;
    private final MutableLiveData<OperationResult> operationResult = new MutableLiveData<>();

    public ResourceLibraryViewModel(@NonNull Application application) {
        super(application);
        resourceRepository = new ResourceRepositoryImpl(application.getApplicationContext());

        allResources = resourceRepository.getAllResources();
        allTasks = resourceRepository.getAllTasks();
        allSubjects = resourceRepository.getAllSubjects();
    }

    public LiveData<OperationResult> getOperationResult() {
        return operationResult;
    }

    public LiveData<List<Resource>> getFilteredResources(Long taskId, Long subjectId, String fileType, String searchQuery) {
        return Transformations.map(allResources, resources -> {
            if (resources == null) return new ArrayList<>();

            List<Resource> filtered = new ArrayList<>(resources);

            // Apply task filter
            if (taskId != null) {
                filtered = filterByTask(filtered, taskId);
            }

            // Apply subject filter
            if (subjectId != null) {
                filtered = filterBySubject(filtered, subjectId);
            }

            // Apply file type filter
            if (fileType != null && !fileType.equals("All Types")) {
                filtered = filterByFileType(filtered, fileType);
            }

            // Apply search query
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                filtered = filterBySearchQuery(filtered, searchQuery.trim());
            }

            return filtered;
        });
    }

    private List<Resource> filterByTask(List<Resource> resources, Long taskId) {
        List<Resource> filtered = new ArrayList<>();
        for (Resource resource : resources) {
            if (taskId.equals(resource.getTaskId())) {
                filtered.add(resource);
            }
        }
        return filtered;
    }

    private List<Resource> filterBySubject(List<Resource> resources, Long subjectId) {
        List<Resource> filtered = new ArrayList<>();
        for (Resource resource : resources) {
            if (subjectId.equals(resource.getSubjectId())) {
                filtered.add(resource);
            }
        }
        return filtered;
    }

    private List<Resource> filterByFileType(List<Resource> resources, String fileType) {
        List<Resource> filtered = new ArrayList<>();
        for (Resource resource : resources) {
            if (fileType.equalsIgnoreCase(resource.getType())) {
                filtered.add(resource);
            }
        }
        return filtered;
    }

    private List<Resource> filterBySearchQuery(List<Resource> resources, String searchQuery) {
        List<Resource> filtered = new ArrayList<>();
        String queryLower = searchQuery.toLowerCase();

        for (Resource resource : resources) {
            if (resource.getTitle() != null && resource.getTitle().toLowerCase().contains(queryLower)) {
                filtered.add(resource);
            }
        }
        return filtered;
    }

    public void createResource(android.net.Uri fileUri, Long taskId, Long subjectId, String title, String type) {
        new Thread(() -> {
            try {
                Resource resource = resourceRepository.createResource(fileUri, taskId, subjectId, title, type);
                operationResult.postValue(new OperationResult(true, "Resource '" + title + "' created successfully"));
            } catch (Exception e) {
                operationResult.postValue(new OperationResult(false, "Failed to create resource: " + e.getMessage()));
            }
        }).start();
    }

    public void deleteResource(Long resourceId) {
        new Thread(() -> {
            try {
                resourceRepository.deleteResource(resourceId);
                operationResult.postValue(new OperationResult(true, "Resource deleted successfully"));
            } catch (Exception e) {
                operationResult.postValue(new OperationResult(false, "Failed to delete resource: " + e.getMessage()));
            }
        }).start();
    }

    public void clearOperationResult() {
        operationResult.setValue(null);
    }

    public FileMetadata getFileMetadata(long fileMetadataId) {
        return resourceRepository.getFileMetadataById(fileMetadataId).orElse(null);
    }

    public static class OperationResult {
        private final boolean success;
        private final String message;

        public OperationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}