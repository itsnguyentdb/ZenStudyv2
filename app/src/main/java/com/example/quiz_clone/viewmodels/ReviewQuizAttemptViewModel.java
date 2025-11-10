package com.example.quiz_clone.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.quiz_clone.dto.QuizAttemptDetails;
import com.example.quiz_clone.repositories.impls.QuizRepositoryImpl;

public class ReviewQuizAttemptViewModel extends AndroidViewModel {
    private QuizRepositoryImpl quizRepository;
    private MutableLiveData<QuizAttemptDetails> attemptDetails = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();

    public ReviewQuizAttemptViewModel(Application application) {
        super(application);
        quizRepository = new QuizRepositoryImpl(application);
    }

    public LiveData<QuizAttemptDetails> getAttemptDetails() {
        return attemptDetails;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadAttemptDetails(Long attemptId) {
        if (attemptId == null || attemptId == -1) {
            error.setValue("Invalid attempt ID");
            return;
        }

        isLoading.postValue(true);
        quizRepository.getQuizAttemptWithDetails(attemptId, new QuizRepositoryImpl.QuizAttemptDetailsCallback() {
            @Override
            public void onDetailsLoaded(QuizAttemptDetails details) {
                isLoading.postValue(false);
                attemptDetails.postValue(details);
            }

            @Override
            public void onError(String errorMessage) {
                isLoading.postValue(false);
                error.postValue(errorMessage);
            }
        });
    }
}
