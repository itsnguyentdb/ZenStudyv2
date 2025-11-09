package com.example.quiz_clone.repositories.impls;

import android.content.Context;

import com.example.quiz_clone.daos.ResourceDao;
import com.example.quiz_clone.daos.SubjectDao;
import com.example.quiz_clone.helpers.AppDatabase;

public class EventRepositoryImpl {
    public EventRepositoryImpl(Context context) {
        var instance = AppDatabase.getInstance(context);
    }

//    public List<Resource> getResourcesOfSubject(long subjectId) {
//
//    }
}
