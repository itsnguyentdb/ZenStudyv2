package com.example.zen_study.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.zen_study.models.FlashcardDeck;
import com.example.zen_study.models.Subject;
import com.example.zen_study.repositories.impls.FlashcardRepositoryImpl;
import com.example.zen_study.repositories.impls.SubjectRepositoryImpl;

import java.util.List;
import java.util.Optional;

import lombok.Getter;

public class FlashcardLibraryViewModel extends AndroidViewModel {
    private FlashcardRepositoryImpl flashcardRepository;
    private SubjectRepositoryImpl subjectRepository;
    @Getter
    private LiveData<List<FlashcardDeck>> allDecks;
    private LiveData<List<Subject>> allSubjects;

    public FlashcardLibraryViewModel(Application application) {
        super(application);
        flashcardRepository = new FlashcardRepositoryImpl(application);
        subjectRepository = new SubjectRepositoryImpl(application);
        allDecks = flashcardRepository.getAllDecks();
        allSubjects = subjectRepository.getAllSubjects();
    }

    public LiveData<List<Subject>> getSubjects() {
        return allSubjects;
    }

    public void update(FlashcardDeck deck) {
        flashcardRepository.editFlashcardDeck(deck);
    }

    public void delete(FlashcardDeck deck) {
        flashcardRepository.deleteDeckWithTerms(deck.getId());
    }

    public Optional<Subject> getSubjectById(long subjectId) {
        return subjectRepository.findSubjectById(subjectId);
    }
}