package com.example.quiz_clone.models;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import lombok.Data;

@Data
public class QuizWithQuestions {
    @Embedded
    public Quiz quiz;
    @Relation(
            parentColumn = "id",
            entityColumn = "quiz_id"
    )
    public List<QuizQuestion> questions;
}
