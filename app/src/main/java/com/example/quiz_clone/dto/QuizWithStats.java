package com.example.quiz_clone.dto;

import com.example.quiz_clone.models.Quiz;
import com.example.quiz_clone.models.QuizAttempt;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QuizWithStats {
    private Quiz quiz;
    private int questionCount;
    private int attemptCount;
    private int averageScore;
    private Date lastAttemptDate;
    private QuizAttempt lastAttempt;

    public QuizWithStats(Quiz quiz, int questionCount, int attemptCount, int averageScore, Date lastAttemptDate) {
        this.quiz = quiz;
        this.questionCount = questionCount;
        this.attemptCount = attemptCount;
        this.averageScore = averageScore;
        this.lastAttemptDate = lastAttemptDate;
    }
}