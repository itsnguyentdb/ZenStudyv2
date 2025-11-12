package com.example.zen_study.repositories;

import com.example.zen_study.models.Quiz;
import com.example.zen_study.models.QuizAttempt;
import com.example.zen_study.models.QuizAttemptAnswer;
import com.example.zen_study.models.QuizAttemptAnswerWithQuestion;
import com.example.zen_study.models.QuizQuestion;

import java.util.List;

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
