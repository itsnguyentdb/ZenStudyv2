package com.example.zen_study.repositories.impls;

import android.content.Context;

import com.example.zen_study.helpers.AppDatabase;

public class EventRepositoryImpl {
    public EventRepositoryImpl(Context context) {
        var instance = AppDatabase.getInstance(context);
    }

//    public List<Resource> getResourcesOfSubject(long subjectId) {
//
//    }
}
