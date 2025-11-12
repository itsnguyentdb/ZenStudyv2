package com.example.zen_study.models;

import androidx.room.Embedded;
import androidx.room.Relation;

import lombok.Data;

@Data
public class QuizAttemptAnswerWithQuestion {
    @Embedded
    public QuizAttemptAnswer answer;

    @Relation(
            parentColumn = "question_id",
            entityColumn = "id"
    )
    public QuizQuestion question;
}
