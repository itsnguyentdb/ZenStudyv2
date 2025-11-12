package com.example.zen_study.dto;

import com.example.zen_study.models.Quiz;
import com.example.zen_study.models.QuizAttempt;
import com.example.zen_study.models.QuizAttemptAnswer;
import com.example.zen_study.models.QuizQuestion;

import java.util.List;

import lombok.Getter;

@Getter
public class QuizAttemptDetails {
    private QuizAttempt attempt;
    private Quiz quiz;
    private List<QuizQuestion> questions;
    private List<QuizAttemptAnswer> answers;

    public QuizAttemptDetails(QuizAttempt attempt, Quiz quiz, List<QuizQuestion> questions, List<QuizAttemptAnswer> answers) {
        this.attempt = attempt;
        this.quiz = quiz;
        this.questions = questions;
        this.answers = answers;
    }


    // Helper methods
    public QuizAttemptAnswer getAnswerForQuestion(Long questionId) {
        for (QuizAttemptAnswer answer : answers) {
            if (answer.getQuestionId().equals(questionId)) {
                return answer;
            }
        }
        return null;
    }

    public int getTotalQuestions() {
        return questions != null ? questions.size() : 0;
    }

    public int getCorrectAnswersCount() {
        if (answers == null) return 0;
        int count = 0;
        for (QuizAttemptAnswer answer : answers) {
            if (answer.isCorrect()) {
                count++;
            }
        }
        return count;
    }

    public int getTotalPoints() {
        if (questions == null) return 0;
        int total = 0;
        for (QuizQuestion question : questions) {
            total += question.getPoints();
        }
        return total;
    }

    public int getEarnedPoints() {
        if (questions == null || answers == null) return 0;
        int earned = 0;
        for (QuizQuestion question : questions) {
            QuizAttemptAnswer answer = getAnswerForQuestion(question.getId());
            if (answer != null && answer.isCorrect()) {
                earned += question.getPoints();
            }
        }
        return earned;
    }
}