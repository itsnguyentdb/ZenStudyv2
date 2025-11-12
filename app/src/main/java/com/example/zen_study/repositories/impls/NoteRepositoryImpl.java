package com.example.zen_study.repositories.impls;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.zen_study.daos.NoteDao;
import com.example.zen_study.helpers.AppDatabase;
import com.example.zen_study.models.Note;

public class NoteRepositoryImpl {
    private final NoteDao noteDao;

    public NoteRepositoryImpl(Context context) {
        var instance = AppDatabase.getInstance(context);
        noteDao = instance.noteDao();
    }

    public void deleteNote(long noteId) {
        noteDao.deleteById(noteId);
    }

}
