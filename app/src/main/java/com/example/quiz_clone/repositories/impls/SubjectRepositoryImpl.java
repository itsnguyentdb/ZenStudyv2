package com.example.quiz_clone.repositories.impls;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.quiz_clone.daos.ResourceDao;
import com.example.quiz_clone.daos.SubjectDao;
import com.example.quiz_clone.helpers.AppDatabase;
import com.example.quiz_clone.models.Resource;
import com.example.quiz_clone.models.Subject;

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

    public Subject saveSubject(Subject subject) {
        return subjectDao.save(subject);
    }

    public void deleteSubject(long subjectId) {
        subjectDao.deleteById(subjectId);
    }

    public Optional<Subject> findSubjectById(long subjectId) {
        return subjectDao.findById(subjectId);
    }

    public Optional<Subject> findSubjectByName(String name) {
        return subjectDao.findByName(name);
    }
}
