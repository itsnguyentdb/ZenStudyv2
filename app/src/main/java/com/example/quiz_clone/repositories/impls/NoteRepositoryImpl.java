package com.example.quiz_clone.repositories.impls;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.quiz_clone.daos.NoteDao;
import com.example.quiz_clone.helpers.AppDatabase;
import com.example.quiz_clone.models.Note;

import java.util.List;

public class NoteRepositoryImpl {
    private final NoteDao noteDao;

    public NoteRepositoryImpl(Context context) {
        var instance = AppDatabase.getInstance(context);
        noteDao = instance.noteDao();
    }

    public void deleteNote(long noteId) {
        noteDao.deleteById(noteId);
    }

    public LiveData<Note> getNoteById(long noteId) {
        return noteDao.findByIdLiveData(noteId).orElseThrow();
    }

//    public LiveData<List<Note>> getNotesByTask(long taskId) {
//
//    }
//
//    public LiveData<List<Note>> getNotesBySubject(long subjectId) {
//
//    }
//
//    public LiveData<List<Note>> searchNotes(String query) {
//
//    }
//
//    public LiveData<Integer> getNoteCountBySubject(long subjectId) {
//
//    }
}
