package com.example.zen_study.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.zen_study.dto.QuizAttemptDetails;
import com.example.zen_study.repositories.impls.QuizRepositoryImpl;

public class QuizAttemptResultViewModel extends AndroidViewModel {
    private QuizRepositoryImpl quizRepository;
    private MutableLiveData<QuizAttemptDetails> attemptDetails = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();

    public QuizAttemptResultViewModel(Application application) {
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