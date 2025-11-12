package com.example.zen_study.repositories;

import android.content.Context;

import com.example.zen_study.daos.ResourceDao;
import com.example.zen_study.daos.SubjectDao;
import com.example.zen_study.helpers.AppDatabase;

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
