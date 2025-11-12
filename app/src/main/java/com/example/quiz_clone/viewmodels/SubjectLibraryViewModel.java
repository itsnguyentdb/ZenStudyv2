package com.example.quiz_clone.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.quiz_clone.models.Subject;
import com.example.quiz_clone.repositories.impls.SubjectRepositoryImpl;

import java.util.ArrayList;
import java.util.List;

public class SubjectLibraryViewModel extends AndroidViewModel {
    private final SubjectRepositoryImpl subjectRepository;

    private final LiveData<List<Subject>> allSubjects;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");

    public SubjectLibraryViewModel(@NonNull Application application) {
        super(application);
        subjectRepository = new SubjectRepositoryImpl(application);
        allSubjects = subjectRepository.getAllSubjects();
    }

    public LiveData<List<Subject>> getAllSubjects() {
        return allSubjects;
    }

    public void setSearchQuery(String q) {
        searchQuery.setValue(q);
    }

    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }

    public Subject addSubject(String name) {
        return subjectRepository.addSubject(name);
    }

    public void updateSubject(Subject subject) {
        subjectRepository.saveSubject(subject);
    }

    public void deleteSubject(long subjectId) {
        subjectRepository.deleteSubject(subjectId);
    }

    public List<Subject> filter(List<Subject> list, String query) {
        if (list == null) return new ArrayList<>();
        if (query == null || query.trim().isEmpty()) return list;
        String q = query.toLowerCase();
        List<Subject> out = new ArrayList<>();
        for (Subject s : list) {
            String name = s.getName() != null ? s.getName() : "";
            String desc = s.getDescription() != null ? s.getDescription() : "";
            if (name.toLowerCase().contains(q) || desc.toLowerCase().contains(q)) {
                out.add(s);
            }
        }
        return out;
    }
}

