package com.example.zen_study.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
@Entity(tableName = "quiz_question", foreignKeys = {
        @ForeignKey(entity = Quiz.class, parentColumns = "id", childColumns = "quiz_id", onDelete = ForeignKey.CASCADE)
}, indices = {
        @Index(value = "id"),
})
@Data
@SuperBuilder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class QuizQuestion extends BaseEntity {
    @ColumnInfo(name = "quiz_id")
    private Long quizId;
    private String questionText, questionType;
    private int position, points, timeLimit;

    private String optionA, optionB, optionC, optionD;
    private String correctAnswer;
    private String explanation;
}
