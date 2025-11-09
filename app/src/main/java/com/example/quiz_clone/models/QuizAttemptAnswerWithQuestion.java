package com.example.quiz_clone.models;

import androidx.room.Embedded;
import androidx.room.Relation;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

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
