package com.example.zen_study.viewmodels;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.zen_study.dto.QuizWithStats;
import com.example.zen_study.enums.QuizFilter;
import com.example.zen_study.models.Quiz;
import com.example.zen_study.repositories.impls.QuizRepositoryImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;

@Getter
public class QuizLibraryViewModel extends AndroidViewModel {
    private QuizRepositoryImpl quizRepository;
    private MutableLiveData<List<QuizWithStats>> quizzesWithStats = new MutableLiveData<>();
    private MutableLiveData<List<Quiz>> allQuizzes = new MutableLiveData<>();
    private MutableLiveData<List<Quiz>> filteredQuizzes = new MutableLiveData<>();
    private MutableLiveData<List<Quiz>> searchResults = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<QuizFilter> currentFilter = new MutableLiveData<>(QuizFilter.ALL);
    private MutableLiveData<String> currentSearchQuery = new MutableLiveData<>("");

    // Stats
    private MutableLiveData<Integer> totalQuizzes = new MutableLiveData<>(0);
    private MutableLiveData<Integer> totalAttempts = new MutableLiveData<>(0);
    private MutableLiveData<Integer> averageScore = new MutableLiveData<>(0);

    private MutableLiveData<Map<Long, Integer>> questionCounts = new MutableLiveData<>();
    private MutableLiveData<Map<Long, Integer>> attemptCounts = new MutableLiveData<>();
    private MutableLiveData<Map<Long, Integer>> averageScores = new MutableLiveData<>();
    private MutableLiveData<Map<Long, Date>> lastAttemptDates = new MutableLiveData<>();

    public QuizLibraryViewModel(Application application) {
        super(application);
        quizRepository = new QuizRepositoryImpl(application);
        loadAllQuizzesWithStats();
    }

    public LiveData<Map<Long, Integer>> getQuestionCounts() {
        return questionCounts;
    }

    public LiveData<Map<Long, Integer>> getAttemptCounts() {
        return attemptCounts;
    }

    public LiveData<Map<Long, Integer>> getAverageScores() {
        return averageScores;
    }

    public LiveData<Map<Long, Date>> getLastAttemptDates() {
        return lastAttemptDates;
    }

    public void loadQuizzes() {
        isLoading.setValue(true);
        new Thread(() -> {
            List<Quiz> quizzes = quizRepository.getAllQuizzes();
            allQuizzes.postValue(quizzes);
            applyCurrentFilter(quizzes);
            calculateStats(quizzes);
            isLoading.postValue(false);
        }).start();
    }

    public void setFilter(QuizFilter filter) {
        currentFilter.setValue(filter);
        List<Quiz> currentList = searchResults.getValue() != null ?
                searchResults.getValue() : allQuizzes.getValue();
        if (currentList != null) {
            applyFilter(currentList, filter);
        }
    }

    private void applyCurrentFilter(List<Quiz> quizzes) {
        QuizFilter filter = currentFilter.getValue();
        if (filter != null) {
            applyFilter(quizzes, filter);
        } else {
            filteredQuizzes.postValue(quizzes);
        }
    }

    private void applyFilter(List<Quiz> quizzes, QuizFilter filter) {
        List<Quiz> filtered = new ArrayList<>();

        switch (filter) {
            case ALL:
                filtered.addAll(quizzes);
                break;
            case RECENT:
                // Sort by creation date, most recent first
                filtered.addAll(quizzes);
                // You might want to sort by createdAt date
                break;
            case HIGH_SCORE:
                // Filter quizzes with high average scores
                for (Quiz quiz : quizzes) {
                    // Placeholder - you'll need to calculate actual average scores
                    if (getQuizAverageScore(quiz) >= 80) {
                        filtered.add(quiz);
                    }
                }
                break;
            case MOST_ATTEMPTED:
                // Filter quizzes with most attempts
                for (Quiz quiz : quizzes) {
                    if (getQuizAttemptCount(quiz) >= 5) { // Threshold
                        filtered.add(quiz);
                    }
                }
                break;
        }

        filteredQuizzes.postValue(filtered);
    }

    public void searchQuizzes(String query) {
        currentSearchQuery.setValue(query);

        if (query == null || query.trim().isEmpty()) {
            searchResults.setValue(null);
            applyCurrentFilter(allQuizzes.getValue());
            return;
        }

        isLoading.setValue(true);
        new Thread(() -> {
            List<Quiz> results = quizRepository.searchQuizzes(query.trim());
            searchResults.postValue(results);
            applyFilter(results, currentFilter.getValue());
            isLoading.postValue(false);
        }).start();
    }

    public void clearSearch() {
        currentSearchQuery.setValue("");
        searchResults.setValue(null);
        applyCurrentFilter(allQuizzes.getValue());
    }

    public void deleteQuiz(Quiz quiz) {
        isLoading.setValue(true);
        new Thread(() -> {
            quizRepository.deleteQuiz(quiz.getId());
            loadQuizzes();
        }).start();
    }

    public void copyQuiz(Quiz quiz, String newTitle, int timeLimit) {
        isLoading.setValue(true);
        new Thread(() -> {
            quizRepository.copyFromQuiz(quiz.getId(), newTitle, timeLimit);
            loadQuizzes();
        }).start();
    }

    private void calculateStats(List<Quiz> quizzes) {
        if (quizzes == null) return;

        int total = quizzes.size();
        int attempts = 0;
        int totalScore = 0;
        int scoredQuizzes = 0;

        for (Quiz quiz : quizzes) {
            int quizAttempts = getQuizAttemptCount(quiz);
            attempts += quizAttempts;

            int avgScore = getQuizAverageScore(quiz);
            if (avgScore > 0) {
                totalScore += avgScore;
                scoredQuizzes++;
            }
        }

        totalQuizzes.postValue(total);
        totalAttempts.postValue(attempts);

        int avg = scoredQuizzes > 0 ? totalScore / scoredQuizzes : 0;
        averageScore.postValue(avg);
    }

    private void loadAllQuizzesWithStats() {
        isLoading.postValue(true);

        new Thread(() -> {
            try {
                List<Quiz> quizzes = quizRepository.getAllQuizzes();

                // Load statistics for all quizzes
                Map<Long, Integer> questionCountMap = new HashMap<>();
                Map<Long, Integer> attemptCountMap = new HashMap<>();
                Map<Long, Integer> averageScoreMap = new HashMap<>();
                Map<Long, Date> lastAttemptDateMap = new HashMap<>();

                for (Quiz quiz : quizzes) {
                    long quizId = quiz.getId();
                    questionCountMap.put(quizId, quizRepository.getQuestionCount(quizId));
                    attemptCountMap.put(quizId, quizRepository.getAttemptCount(quizId));
                    averageScoreMap.put(quizId, quizRepository.getAverageScore(quizId));
                    lastAttemptDateMap.put(quizId, quizRepository.getLastAttemptDate(quizId));
                }

                // Post all data to main thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    allQuizzes.postValue(quizzes);
                    questionCounts.postValue(questionCountMap);
                    attemptCounts.postValue(attemptCountMap);
                    averageScores.postValue(averageScoreMap);
                    lastAttemptDates.postValue(lastAttemptDateMap);
                    isLoading.postValue(false);
                });

            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    isLoading.postValue(false);
                    // Handle error
                });
            }
        }).start();
    }


    // These methods need to be implemented based on your data
    private int getQuizAttemptCount(Quiz quiz) {
        return quizRepository.getQuizAttemptCountFromQuizId(quiz.getId());
    }

    private int getQuizAverageScore(Quiz quiz) {
        return quizRepository.getQuizAverageScoreOfQuizId(quiz.getId());
    }

}
