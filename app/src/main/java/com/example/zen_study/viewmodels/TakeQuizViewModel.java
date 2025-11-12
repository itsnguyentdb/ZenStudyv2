package com.example.zen_study.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.zen_study.models.Quiz;
import com.example.zen_study.repositories.impls.QuizRepositoryImpl;

public class TakeQuizViewModel extends AndroidViewModel {
    private QuizRepositoryImpl quizRepository;
    private MutableLiveData<Quiz> quiz = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();

    public TakeQuizViewModel(Application application) {
        super(application);
        quizRepository = new QuizRepositoryImpl(application);
    }

    public LiveData<Quiz> getQuiz() {
        return quiz;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadQuiz(Long quizId) {
        isLoading.postValue(true);
        quizRepository.getQuizWithQuestions(quizId, new QuizRepositoryImpl.QuizLoadCallback() {
            @Override
            public void onQuizLoaded(Quiz loadedQuiz) {
                isLoading.postValue(false);
                quiz.postValue(loadedQuiz);
            }

            @Override
            public void onError(String errorMessage) {
                isLoading.postValue(false);
                error.postValue(errorMessage);
            }
        });
    }
}