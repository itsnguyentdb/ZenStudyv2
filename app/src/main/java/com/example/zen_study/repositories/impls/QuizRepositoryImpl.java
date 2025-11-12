package com.example.zen_study.repositories.impls;

import android.content.Context;

import com.example.zen_study.daos.QuizAttemptAnswerDao;
import com.example.zen_study.daos.QuizAttemptDao;
import com.example.zen_study.daos.QuizDao;
import com.example.zen_study.daos.QuizQuestionDao;
import com.example.zen_study.dto.QuizAttemptDetails;
import com.example.zen_study.helpers.AppDatabase;
import com.example.zen_study.models.BaseEntity;
import com.example.zen_study.models.Quiz;
import com.example.zen_study.models.QuizAttempt;
import com.example.zen_study.models.QuizAttemptAnswer;
import com.example.zen_study.models.QuizAttemptAnswerWithQuestion;
import com.example.zen_study.models.QuizQuestion;
import com.example.zen_study.models.QuizWithQuestions;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class QuizRepositoryImpl {
    public interface QuizLoadCallback {
        void onQuizLoaded(Quiz quiz);

        void onError(String msg);
    }

    public interface QuizAttemptDetailsCallback {
        void onDetailsLoaded(QuizAttemptDetails details);

        void onError(String errorMessage);
    }

    private final QuizDao quizDao;
    private final QuizQuestionDao quizQuestionDao;
    private final QuizAttemptDao quizAttemptDao;
    private final QuizAttemptAnswerDao quizAttemptAnswerDao;
    private final Executor executor;
    private final android.os.Handler mainHandler;

    public QuizRepositoryImpl(Context context) {
        var instance = AppDatabase.getInstance(context);
        quizDao = instance.quizDao();
        quizQuestionDao = instance.quizQuestionDao();
        quizAttemptDao = instance.quizAttemptDao();
        quizAttemptAnswerDao = instance.quizAttemptAnswerDao();
        executor = instance.getQueryExecutor();
        mainHandler = new android.os.Handler(context.getMainLooper());
    }

    public Quiz createQuiz(String title, int timeLimit) {
        return quizDao.save(Quiz.builder()
                .title(title)
                .timeLimit(timeLimit)
                .createdAt(new Date())
                .lastUpdatedAt(new Date())
                .build()
        );
    }

    public Quiz createQuiz(String title, String description, int timeLimit) {
        return quizDao.save(Quiz.builder()
                .title(title)
                .timeLimit(timeLimit)
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

    public Quiz copyFromQuiz(long quizId, String newTitle, String newDescription, int timeLimit) {
        var quiz = quizDao.findById(quizId);
        if (quiz == null) {
            return null;
        }
        var copiedQuiz = createQuiz(newTitle, newDescription, timeLimit);
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

    public Quiz copyFromQuiz(long quizId, String newTitle, int timeLimit) {
        var quiz = quizDao.findById(quizId).orElseThrow();
        var copiedQuiz = createQuiz(newTitle, quiz.getDescription(), timeLimit);
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

    public QuizAttempt saveQuizAttempt(QuizAttempt attempt) {
        return quizAttemptDao.save(attempt);
    }

    public QuizAttemptAnswer saveAnswer(long attemptId, long questionId, String userAnswer, boolean isCorrect) {
        return quizAttemptAnswerDao.insert(QuizAttemptAnswer.builder()
                .attemptId(attemptId)
                .questionId(questionId)
                .userAnswer(userAnswer)
                .isCorrect(isCorrect)
                .build()
        );
    }

    public void getQuizWithQuestions(Long quizId, QuizLoadCallback callback) {
        executor.execute(() -> {
            try {
                QuizWithQuestions quizWithQuestions = quizDao.getQuizWithQuestions(quizId);
                if (quizWithQuestions != null) {
                    Quiz quiz = quizWithQuestions.getQuiz();
                    quiz.setQuestions(quizWithQuestions.getQuestions());
                    // Post to main thread
                    mainHandler.post(() -> callback.onQuizLoaded(quiz));
                } else {
                    mainHandler.post(() -> callback.onError("Quiz not found"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void getQuizAttemptWithDetails(Long attemptId, QuizAttemptDetailsCallback callback) {
        executor.execute(() -> {
            try {
                // Get attempt with answers
                var attemptWithAnswers = quizAttemptDao.getAttemptWithAnswers(attemptId);
                if (attemptWithAnswers == null) {
                    mainHandler.post(() -> callback.onError("Attempt not found"));
                    return;
                }

                QuizAttempt attempt = attemptWithAnswers.getQuizAttempt();
                List<QuizAttemptAnswer> answers = attemptWithAnswers.getAttemptAnswers();

                // Get the quiz with questions
                QuizWithQuestions quizWithQuestions = quizDao.getQuizWithQuestions(attempt.getQuizId());
                if (quizWithQuestions == null) {
                    mainHandler.post(() -> callback.onError("Quiz not found"));
                    return;
                }

                Quiz quiz = quizWithQuestions.quiz;
                List<QuizQuestion> questions = quizWithQuestions.questions;

                // Create a complete attempt details object
                QuizAttemptDetails details = new QuizAttemptDetails(attempt, quiz, questions, answers);
                mainHandler.post(() -> callback.onDetailsLoaded(details));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    // Alternative method if you prefer separate queries
    public void loadQuizWithQuestions(Long quizId, QuizLoadCallback callback) {
        executor.execute(() -> {
            try {
                Quiz quiz = quizDao.findById(quizId).orElse(null);
                if (quiz != null) {
                    List<QuizQuestion> questions = getQuestionsFromQuiz(quizId);
                    quiz.setQuestions(questions);
                    callback.onQuizLoaded(quiz);
                } else {
                    callback.onError("Quiz not found");
                }
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public List<QuizAttemptAnswer> getAnswersFromAttempt(long attemptId) {
        return quizAttemptDao.getAttemptWithAnswers(attemptId).attemptAnswers;
    }

    public List<QuizAttemptAnswerWithQuestion> getAnswersWithQuestionsFromAttempt(long attemptId) {
        return quizAttemptAnswerDao.getAnswersWithQuestions(attemptId);
    }

    public int getQuestionCount(long quizId) {
        return quizQuestionDao.findByQuizId(quizId).size();
    }

    public int getAttemptCount(long quizId) {
        return quizAttemptDao.findByQuizId(quizId).size();
    }

    public int getAverageScore(long quizId) {
        List<QuizAttempt> attempts = quizAttemptDao.findByQuizId(quizId);
        if (attempts.isEmpty()) {
            return 0;
        }

        int totalScore = 0;
        for (QuizAttempt attempt : attempts) {
            totalScore += attempt.getScore();
        }
        return totalScore / attempts.size();
    }

    public Date getLastAttemptDate(long quizId) {
        List<QuizAttempt> attempts = quizAttemptDao.findByQuizId(quizId);
        if (attempts.isEmpty()) {
            return null;
        }

        // Sort by start time descending and get the most recent
        attempts.sort((a1, a2) -> a2.getStartTime().compareTo(a1.getStartTime()));
        return attempts.get(0).getStartTime();
    }

    public QuizAttempt getLastAttempt(long quizId) {
        List<QuizAttempt> attempts = quizAttemptDao.findByQuizId(quizId);
        if (attempts.isEmpty()) {
            return null;
        }

        // Sort by start time descending and get the most recent
        attempts.sort((a1, a2) -> a2.getStartTime().compareTo(a1.getStartTime()));
        return attempts.get(0);
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

    public Quiz getQuizById(long quizId) {
        return quizDao.findById(quizId).orElse(null);
    }

    public Quiz updateQuiz(Quiz existingQuiz) {
        return quizDao.save(existingQuiz);
    }

    public void deleteQuestion(Long id) {
        quizQuestionDao.deleteById(id);
    }

    public int getQuizAverageScoreOfQuizId(Long id) {
        var quizAttempts = quizAttemptDao.findByQuizId(id);
        if (quizAttempts.isEmpty()) {
            return 0;
        }

        int totalScore = 0;
        for (QuizAttempt attempt : quizAttempts) {
            totalScore += attempt.getScore();
        }
        return totalScore / quizAttempts.size();
    }

    public int getQuizAttemptCountFromQuizId(Long id) {
        return quizAttemptDao.findByQuizId(id).size();
    }
}
