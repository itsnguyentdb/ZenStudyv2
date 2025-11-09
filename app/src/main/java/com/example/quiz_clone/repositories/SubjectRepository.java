package com.example.quiz_clone.repositories;

import android.content.Context;

import com.example.quiz_clone.daos.ResourceDao;
import com.example.quiz_clone.daos.SubjectDao;
import com.example.quiz_clone.helpers.AppDatabase;

public class SubjectRepository {
    private final ResourceDao resourceDao;

    private final SubjectDao subjectDao;

    public SubjectRepository(Context context) {
        var instance = AppDatabase.getInstance(context);
        resourceDao = instance.resourceDao();
        subjectDao = instance.subjectDao();
    }

//    public List<Resource> getResourcesOfSubject(long subjectId) {
//
//    }
}
