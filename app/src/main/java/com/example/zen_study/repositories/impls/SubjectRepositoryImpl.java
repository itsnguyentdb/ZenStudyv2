package com.example.zen_study.repositories.impls;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.zen_study.daos.ResourceDao;
import com.example.zen_study.daos.SubjectDao;
import com.example.zen_study.helpers.AppDatabase;
import com.example.zen_study.models.Resource;
import com.example.zen_study.models.Subject;

import java.util.List;
import java.util.Optional;

public class SubjectRepositoryImpl {
    private final ResourceDao resourceDao;

    private final SubjectDao subjectDao;

    public SubjectRepositoryImpl(Context context) {
        var instance = AppDatabase.getInstance(context);
        resourceDao = instance.resourceDao();
        subjectDao = instance.subjectDao();
    }

    public LiveData<List<Subject>> getAllSubjects() {
        return subjectDao.findAllLiveData();
    }

    public LiveData<List<Resource>> getResourcesOfSubject(long subjectId) {
        return resourceDao.findResourcesBySubjectId(subjectId);
    }

    public Subject addSubject(String subjectTitle) {
        return subjectDao.save(Subject.builder()
                .name(subjectTitle)
                .build()
        );
    }

    public Subject updateSubject(Subject subject) {
        return subjectDao.save(subject);
    }

    public List<Subject> getAllSubjectsSync() {
        try {
            return subjectDao.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.List.of();
        }
    }

    public Subject getSubjectByIdSync(long subjectId) {
        try {
            return subjectDao.findById(subjectId).orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deleteSubjectById(long subjectId) {
        subjectDao.deleteById(subjectId);
    }

    public Optional<Subject> findSubjectById(long subjectId) {
        return subjectDao.findById(subjectId);
    }

    public Optional<Subject> findSubjectByName(String name) {
        return subjectDao.findByName(name);
    }
}