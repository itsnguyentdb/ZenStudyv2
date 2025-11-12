package com.example.zen_study.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizStats {
    private int averageScore;
    private String fastestAttempt;
    private String highestScoreDeck;
    private int highestScore;
    private int quizzesTaken;
}
