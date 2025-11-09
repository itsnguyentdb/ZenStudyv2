package com.example.quiz_clone.models;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

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
