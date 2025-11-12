package com.example.zen_study.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;
@Entity(tableName = "quiz_attempt_answer",
        primaryKeys = {"attempt_id", "question_id"},
        foreignKeys = {
                @ForeignKey(entity = QuizAttempt.class, parentColumns = "id", childColumns = "attempt_id", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = QuizQuestion.class, parentColumns = "id", childColumns = "question_id", onDelete = ForeignKey.CASCADE)
        }, indices = {
//        @Index(value = "id"),
})
@Data
@SuperBuilder
@AllArgsConstructor
public class QuizAttemptAnswer {
    @NonNull
    @ColumnInfo(name = "attempt_id")
    private Long attemptId;
    @NonNull
    @ColumnInfo(name = "question_id")
    private Long questionId;
    private String userAnswer;
    private boolean isCorrect;

    public QuizAttemptAnswer() {
        attemptId = 0L;
        questionId = 0L;
    }
}
