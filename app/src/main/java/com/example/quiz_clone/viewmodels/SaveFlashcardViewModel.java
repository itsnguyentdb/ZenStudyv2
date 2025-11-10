package com.example.quiz_clone.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.room.Transaction;

import com.example.quiz_clone.models.FlashcardDeck;
import com.example.quiz_clone.models.FlashcardTerm;
import com.example.quiz_clone.models.Subject;
import com.example.quiz_clone.repositories.impls.FlashcardRepositoryImpl;
import com.example.quiz_clone.repositories.impls.SubjectRepositoryImpl;

import java.util.List;
import java.util.Optional;

public class SaveFlashcardViewModel extends AndroidViewModel {
    private final FlashcardRepositoryImpl flashcardRepository;
    private final SubjectRepositoryImpl subjectRepository;

    public SaveFlashcardViewModel(@NonNull Application application) {
        super(application);
        flashcardRepository = new FlashcardRepositoryImpl(application);
        subjectRepository = new SubjectRepositoryImpl(application);
    }

    @Transaction
    public void insertDeckWithTerms(FlashcardDeck deck, List<FlashcardTerm> terms) {
        var createdDeck = flashcardRepository.createFlashcardDeck(deck.getTitle(), deck.getDescription(), deck.getSubjectId());
        System.out.println(createdDeck.toString());
        terms.forEach(term -> {
            flashcardRepository.addTermToDeck(createdDeck.getId(), term.getTerm(), term.getDefinition(), term.getPosition());
            System.out.println(term);
        });
        createdDeck.setCardCount(terms.size());
        flashcardRepository.editFlashcardDeck(createdDeck);
    }

    @Transaction
    public void updateDeckWithTerms(FlashcardDeck deck, List<FlashcardTerm> terms) {
        flashcardRepository.editFlashcardDeck(deck);
        flashcardRepository.deleteTermsByDeckId(deck.getId());
        terms.forEach(term -> {
            term.setFlashcardDeckId(deck.getId());
            flashcardRepository.addTermToDeck(deck.getId(), term.getTerm(), term.getDefinition(), term.getPosition());
        });
    }

    public Optional<Subject> getSubjectById(long subjectId) {
        return subjectRepository.findSubjectById(subjectId);
    }

    public Optional<Subject> getSubjectByName(String name) {
        return subjectRepository.findSubjectByName(name);
    }

    public Optional<FlashcardDeck> getDeckById(long deckId) {
        return flashcardRepository.getDeckById(deckId);
    }

    public List<FlashcardTerm> getTermsForDeck(long deckId) {
        return flashcardRepository.getTermsForDeck(deckId);
    }

    public Subject saveSubject(String name) {
        return subjectRepository.addSubject(name);
    }
}
