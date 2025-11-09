package com.example.quiz_clone.repositories;

import android.content.Context;

import com.example.quiz_clone.daos.QuizAttemptAnswerDao;
import com.example.quiz_clone.daos.QuizAttemptDao;
import com.example.quiz_clone.daos.QuizDao;
import com.example.quiz_clone.daos.QuizQuestionDao;
import com.example.quiz_clone.helpers.AppDatabase;
import com.example.quiz_clone.models.BaseEntity;
import com.example.quiz_clone.models.Quiz;
import com.example.quiz_clone.models.QuizAttempt;
import com.example.quiz_clone.models.QuizAttemptAnswer;
import com.example.quiz_clone.models.QuizAttemptAnswerWithQuestion;
import com.example.quiz_clone.models.QuizQuestion;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public interface QuizRepository {

    Quiz createQuiz(String title);

    Quiz createQuiz(String title, String description);

    Quiz editQuiz();

    Quiz copyFromQuiz(long quizId, String newTitle, String newDescription);

    Quiz copyFromQuiz(long quizId, String newTitle);

    List<Quiz> getAllQuizzes();

    List<Quiz> getAllQuizzesBySubjectId(long subjectId);

    QuizQuestion addQuestionToQuiz(long quizId, QuizQuestion quizQuestion);

    List<QuizQuestion> getQuestionsFromQuiz(long quizId);

    QuizAttempt startQuizAttempt(long quizId);

    QuizAttempt completeQuizAttempt(long attemptId);

    List<QuizAttempt> getQuizAttempts(long quizId);

    QuizAttemptAnswer saveAnswer(long attemptId, long questionId, String userAnswer);


    void saveAnswers(Iterable<? extends QuizAttemptAnswer> answers);

    List<QuizAttemptAnswer> getAnswersFromAttempt(long attemptId);

    List<QuizAttemptAnswerWithQuestion> getAnswersWithQuestionsFromAttempt(long attemptId);


    int getTotalScoreFromAttempt(long attemptId);

    List<Quiz> searchQuizzes(String query);

    int getTotalAttempts(long quizId);

    void deleteQuiz(long quizId);
}
