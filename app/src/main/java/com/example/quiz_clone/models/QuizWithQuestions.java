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
public class QuizWithQuestions {
    @Embedded
    public Quiz quiz;
    @Relation(
            parentColumn = "id",
            entityColumn = "quiz_id"
    )
    public List<QuizQuestion> quizQuestions;
}
