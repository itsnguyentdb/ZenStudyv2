package com.example.zen_study.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyOverview {
    private String totalStudyTime;
    private int sessionsCompleted;
    private int currentStreak;
    private int averageFocus;
}
