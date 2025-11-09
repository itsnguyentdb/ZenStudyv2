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

public class SubjectRepositoryImpl {
    private final ResourceDao resourceDao;

    private final SubjectDao subjectDao;

    public SubjectRepositoryImpl(Context context) {
        var instance = AppDatabase.getInstance(context);
        resourceDao = instance.resourceDao();
        subjectDao = instance.subjectDao();
    }

    public LiveData<List<Subject>> getAllSubjects() {
        return new MutableLiveData<>(subjectDao.findAll());
    }

    public LiveData<List<Resource>> getResourcesOfSubject(long subjectId) {
        return new MutableLiveData<>(resourceDao.findResourcesBySubjectId(subjectId));
    }
}
