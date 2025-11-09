package com.example.quiz_clone.repositories.impls;

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

public class QuizRepositoryImpl {
    private final QuizDao quizDao;
    private final QuizQuestionDao quizQuestionDao;
    private final QuizAttemptDao quizAttemptDao;
    private final QuizAttemptAnswerDao quizAttemptAnswerDao;

    public QuizRepositoryImpl(Context context) {
        var instance = AppDatabase.getInstance(context);
        quizDao = instance.quizDao();
        quizQuestionDao = instance.quizQuestionDao();
        quizAttemptDao = instance.quizAttemptDao();
        quizAttemptAnswerDao = instance.quizAttemptAnswerDao();
    }

    public Quiz createQuiz(String title) {
        return quizDao.save(Quiz.builder()
                .title(title)
                .createdAt(new Date())
                .lastUpdatedAt(new Date())
                .build()
        );
    }

    public Quiz createQuiz(String title, String description) {
        return quizDao.save(Quiz.builder()
                .title(title)
                .description(description)
                .createdAt(new Date())
                .lastUpdatedAt(new Date())
                .build()
        );
    }

//    public Quiz editQuiz() {
//        quiz.setLastUpdatedAt(new Date());
//        return quizDao.save(quiz);
//    }

    public Quiz copyFromQuiz(long quizId, String newTitle, String newDescription) {
        var quiz = quizDao.findById(quizId);
        if (quiz == null) {
            return null;
        }
        var copiedQuiz = createQuiz(newTitle, newDescription);
        var questions = getQuestionsFromQuiz(quizId);
        var copiedQuestions = questions.stream()
                .map(q ->
                        QuizQuestion.builder()
                                .quizId(copiedQuiz.getId())
                                .questionText(q.getQuestionText())
                                .questionType(q.getQuestionType())
                                .position(q.getPosition())
                                .points(q.getPoints())
                                .timeLimit(q.getTimeLimit())
                                .optionA(q.getOptionA())
                                .optionB(q.getOptionB())
                                .optionC(q.getOptionC())
                                .optionD(q.getOptionD())
                                .correctAnswer(q.getCorrectAnswer())
                                .explanation(q.getExplanation())
                                .build()
                ).collect(Collectors.toList());
        quizQuestionDao.insertAll(copiedQuestions);
        return copiedQuiz;
    }

    public Quiz copyFromQuiz(long quizId, String newTitle) {
        var quiz = quizDao.findById(quizId).orElseThrow();
        var copiedQuiz = createQuiz(newTitle, quiz.getDescription());
        var questions = getQuestionsFromQuiz(quizId);
        var copiedQuestions = questions.stream()
                .map(q ->
                        QuizQuestion.builder()
                                .quizId(copiedQuiz.getId())
                                .questionText(q.getQuestionText())
                                .questionType(q.getQuestionType())
                                .position(q.getPosition())
                                .points(q.getPoints())
                                .timeLimit(q.getTimeLimit())
                                .optionA(q.getOptionA())
                                .optionB(q.getOptionB())
                                .optionC(q.getOptionC())
                                .optionD(q.getOptionD())
                                .correctAnswer(q.getCorrectAnswer())
                                .explanation(q.getExplanation())
                                .build()
                ).collect(Collectors.toList());
        quizQuestionDao.insertAll(copiedQuestions);
        return copiedQuiz;
    }

    public List<Quiz> getAllQuizzes() {
        return quizDao.findAll();
    }

    public List<Quiz> getAllQuizzesBySubjectId(long subjectId) {
        return quizDao.findBySubjectId(subjectId);
    }

    public QuizQuestion addQuestionToQuiz(long quizId, QuizQuestion quizQuestion) {
        quizQuestion.setQuizId(quizId);
        return quizQuestionDao.save(quizQuestion);
    }

    public List<QuizQuestion> getQuestionsFromQuiz(long quizId) {
        return quizQuestionDao.findByQuizId(quizId);
    }

    public QuizAttempt startQuizAttempt(long quizId) {
        return quizAttemptDao.save(QuizAttempt.builder()
                .quizId(quizId)
                .startTime(new Date())
                .build()
        );
    }

    public QuizAttempt completeQuizAttempt(long attemptId) {
        var attempt = quizAttemptDao.findById(attemptId).orElseThrow();
        attempt.setEndTime(new Date());
        attempt.setScore(getTotalScoreFromAttemptWithUpdatingAnswerCorrectState(attemptId));
        attempt.setDuration(attempt.getEndTime().getTime() - attempt.getStartTime().getTime());
        return quizAttemptDao.save(attempt);
    }

    public List<QuizAttempt> getQuizAttempts(long quizId) {
        return quizAttemptDao.findByQuizId(quizId);
    }

    public QuizAttemptAnswer saveAnswer(long attemptId, long questionId, String userAnswer) {
        return quizAttemptAnswerDao.insert(QuizAttemptAnswer.builder()
                .attemptId(attemptId)
                .questionId(questionId)
                .userAnswer(userAnswer)
                .build()
        );
    }


//    public void saveAnswers(Iterable<? extends QuizAttemptAnswer> answers) {
//        quizAttemptAnswerDao.insertAll(answers);
//    }

    public List<QuizAttemptAnswer> getAnswersFromAttempt(long attemptId) {
        return quizAttemptDao.getAttemptWithAnswers(attemptId).attemptAnswers;
    }

    public List<QuizAttemptAnswerWithQuestion> getAnswersWithQuestionsFromAttempt(long attemptId) {
        return quizAttemptAnswerDao.getAnswersWithQuestions(attemptId);
    }

    private int getTotalScoreFromAttemptWithUpdatingAnswerCorrectState(long attemptId) {
        var answersWithQuestions = getAnswersWithQuestionsFromAttempt(attemptId);
        var totalScore = 0;
        var length = answersWithQuestions.size();
        for (int i = 0; i < length; i++) {
            var aQ = answersWithQuestions.get(i);
            if (aQ.answer.getUserAnswer().equalsIgnoreCase(aQ.question.getCorrectAnswer())) {
                totalScore += aQ.question.getPoints();
                aQ.answer.setCorrect(true);
                answersWithQuestions.set(i, aQ);
            }
        }
        quizAttemptAnswerDao.insertAll(
                answersWithQuestions.parallelStream().map(aQ -> aQ.answer)
                        .collect(Collectors.toList())
        );
        return totalScore;
    }

    public int getTotalScoreFromAttempt(long attemptId) {
        var answersWithQuestions = getAnswersWithQuestionsFromAttempt(attemptId);
        var totalScore = new AtomicInteger(0);
        answersWithQuestions.forEach(aQ -> {
            if (aQ.answer.getUserAnswer().equalsIgnoreCase(aQ.question.getCorrectAnswer())) {
                totalScore.incrementAndGet();
            }
        });
        return totalScore.get();
    }

    public List<Quiz> searchQuizzes(String query) {
        return quizDao.searchByTitleOrDescription(query);
    }

    public int getTotalAttempts(long quizId) {
        return quizAttemptDao.findByQuizId(quizId).size();
    }

    public void deleteQuiz(long quizId) {
        var questions = getQuestionsFromQuiz(quizId);
        var attempts = getQuizAttempts(quizId);
        quizAttemptAnswerDao.deleteAllByAttemptIdsInBatch(attempts.parallelStream()
                .map(BaseEntity::getId)
                .collect(Collectors.toList())
        );
        quizAttemptAnswerDao.deleteAllByQuestionIdsInBatch(questions.parallelStream()
                .map(BaseEntity::getId)
                .collect(Collectors.toList())
        );
        quizAttemptDao.deleteAllInBatch(attempts);
        quizQuestionDao.deleteAllInBatch(questions);
        quizDao.deleteById(quizId);
    }
}
