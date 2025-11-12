// SaveQuizViewModel.java
package com.example.zen_study.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.zen_study.models.Quiz;
import com.example.zen_study.models.QuizQuestion;
import com.example.zen_study.repositories.impls.QuizRepositoryImpl;

import java.util.ArrayList;
import java.util.List;

public class SaveQuizViewModel extends AndroidViewModel {
    private QuizRepositoryImpl quizRepository;

    private MutableLiveData<Quiz> currentQuiz = new MutableLiveData<>();
    private MutableLiveData<List<QuizQuestion>> questions = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<Boolean> saveResult = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public SaveQuizViewModel(Application application) {
        super(application);
        quizRepository = new QuizRepositoryImpl(application);
    }

    public LiveData<Quiz> getCurrentQuiz() { return currentQuiz; }
    public LiveData<List<QuizQuestion>> getQuestions() { return questions; }
    public LiveData<Boolean> getSaveResult() { return saveResult; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void loadQuizData(long quizId) {
        isLoading.setValue(true);
        new Thread(() -> {
            try {
                Quiz quiz = quizRepository.getQuizById(quizId);
                currentQuiz.postValue(quiz);

                List<QuizQuestion> questionList = quizRepository.getQuestionsFromQuiz(quizId);
                questions.postValue(questionList);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isLoading.postValue(false);
            }
        }).start();
    }

    public void createQuiz(String title, String description, int timeLimit, List<QuizQuestion> questions) {
        isLoading.setValue(true);
        new Thread(() -> {
            try {
                // Create the quiz first
                Quiz newQuiz;
                if (description != null && !description.isEmpty()) {
                    newQuiz = quizRepository.createQuiz(title, description, timeLimit);
                } else {
                    newQuiz = quizRepository.createQuiz(title, timeLimit);
                }

                if (newQuiz != null && !questions.isEmpty()) {
                    // Add all questions to the quiz
                    for (int i = 0; i < questions.size(); i++) {
                        QuizQuestion question = questions.get(i);
                        question.setQuizId(newQuiz.getId());
                        question.setPosition(i);
                        quizRepository.addQuestionToQuiz(newQuiz.getId(), question);
                    }
                }

                saveResult.postValue(newQuiz != null);
            } catch (Exception e) {
                e.printStackTrace();
                saveResult.postValue(false);
            }
        }).start();
    }

    public void updateQuiz(long quizId, String title, String description, int timeLimit, List<QuizQuestion> questions) {
        isLoading.setValue(true);
        new Thread(() -> {
            try {
                Quiz existingQuiz = quizRepository.getQuizById(quizId);
                if (existingQuiz != null) {
                    existingQuiz.setTitle(title);
                    existingQuiz.setDescription(description);
                    existingQuiz.setTimeLimit(timeLimit);

                    Quiz updatedQuiz = quizRepository.updateQuiz(existingQuiz);

                    // Update questions
                    if (updatedQuiz != null) {
                        // Delete all existing questions
                        List<QuizQuestion> existingQuestions = quizRepository.getQuestionsFromQuiz(quizId);
                        for (QuizQuestion existingQuestion : existingQuestions) {
                            quizRepository.deleteQuestion(existingQuestion.getId());
                        }

                        // Add updated questions
                        for (int i = 0; i < questions.size(); i++) {
                            QuizQuestion question = questions.get(i);
                            question.setQuizId(quizId);
                            question.setPosition(i);
                            quizRepository.addQuestionToQuiz(quizId, question);
                        }
                    }

                    saveResult.postValue(updatedQuiz != null);
                } else {
                    saveResult.postValue(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
                saveResult.postValue(false);
            }
        }).start();
    }

    public void saveQuestion(Long quizId, QuizQuestion question, boolean isUpdate) {
        List<QuizQuestion> currentQuestions = questions.getValue();
        if (currentQuestions == null) {
            currentQuestions = new ArrayList<>();
        }

        if (isUpdate) {
            // Update existing question
            for (int i = 0; i < currentQuestions.size(); i++) {
                if (currentQuestions.get(i).getId() != null &&
                        currentQuestions.get(i).getId().equals(question.getId())) {
                    currentQuestions.set(i, question);
                    break;
                }
            }
        } else {
            // Add new question
            question.setPosition(currentQuestions.size());
            currentQuestions.add(question);
        }

        questions.postValue(new ArrayList<>(currentQuestions));
    }

    public void deleteQuestion(QuizQuestion question) {
        List<QuizQuestion> currentQuestions = questions.getValue();
        if (currentQuestions != null) {
            currentQuestions.remove(question);
            // Update positions
            for (int i = 0; i < currentQuestions.size(); i++) {
                currentQuestions.get(i).setPosition(i);
            }
            questions.postValue(new ArrayList<>(currentQuestions));
        }
    }

    public void reorderQuestions(int fromPosition, int toPosition) {
        List<QuizQuestion> currentQuestions = questions.getValue();
        if (currentQuestions != null && fromPosition >= 0 && fromPosition < currentQuestions.size()
                && toPosition >= 0 && toPosition < currentQuestions.size()) {

            QuizQuestion movedQuestion = currentQuestions.remove(fromPosition);
            currentQuestions.add(toPosition, movedQuestion);

            // Update all positions
            for (int i = 0; i < currentQuestions.size(); i++) {
                currentQuestions.get(i).setPosition(i);
            }

            questions.postValue(new ArrayList<>(currentQuestions));
        }
    }
}