package com.example.zen_study.models;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import lombok.Data;

@Data
public class QuizAttemptWithAnswers {
    @Embedded
    public QuizAttempt quizAttempt;

    @Relation(
            parentColumn = "id",
            entityColumn = "attempt_id"
    )
    public List<QuizAttemptAnswer> attemptAnswers;
}
