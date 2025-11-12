package com.example.zen_study.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.zen_study.models.Subject;
import com.example.zen_study.repositories.impls.SubjectRepositoryImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class SubjectLibraryViewModel extends AndroidViewModel {
    private final SubjectRepositoryImpl subjectRepository;
    private final LiveData<List<Subject>> allSubjects;

    public SubjectLibraryViewModel(@NonNull Application application) {
        super(application);
        subjectRepository = new SubjectRepositoryImpl(application);
        allSubjects = subjectRepository.getAllSubjects();
    }

    public LiveData<List<Subject>> getAllSubjects() {
        return allSubjects;
    }

    public Optional<String> validateUniqueName(String name, Long excludeId) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.of("Name cannot be empty");
        }
        List<Subject> current = allSubjects.getValue();
        if (current == null) current = new ArrayList<>();
        String target = name.trim().toLowerCase(Locale.ROOT);
        for (Subject s : current) {
            if (excludeId != null && s.getId() != null && s.getId().equals(excludeId)) {
                continue;
            }
            if (s.getName() != null && s.getName().trim().toLowerCase(Locale.ROOT).equals(target)) {
                return Optional.of("A subject with this name already exists");
            }
        }
        return Optional.empty();
    }

    public Subject addSubject(String name, String description) {
        Subject s = subjectRepository.addSubject(name.trim());
        if (s != null) {
            s.setDescription(description);
            s = subjectRepository.updateSubject(s);
        }
        return s;
    }

    public Subject addSubject(String name) {
        return subjectRepository.addSubject(name.trim());
    }

    public Subject updateSubject(Subject subject, String newName, String description) {
        subject.setName(newName.trim());
        subject.setDescription(description);
        return subjectRepository.updateSubject(subject);
    }

    public Subject updateSubject(Subject subject, String newName) {
        subject.setName(newName.trim());
        return subjectRepository.updateSubject(subject);
    }

    public void deleteSubject(Subject subject) {
        if (subject.getId() != null) {
            subjectRepository.deleteSubjectById(subject.getId());
        }
    }
}
