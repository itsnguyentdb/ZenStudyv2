package com.example.quiz_clone.repositories.impls;

import android.content.Context;

import com.example.quiz_clone.daos.ResourceDao;
import com.example.quiz_clone.daos.SubjectDao;
import com.example.quiz_clone.helpers.AppDatabase;

public class ResourceRepositoryImpl {
    private final ResourceDao resourceDao;
    private final SubjectDao subjectDao;

    public ResourceRepositoryImpl(Context context) {
        var instance = AppDatabase.getInstance(context);
        resourceDao = instance.resourceDao();
        subjectDao = instance.subjectDao();
    }
}
