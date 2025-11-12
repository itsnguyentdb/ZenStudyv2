package com.example.zen_study.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskProgress {
    private int completionRate;
    private int completedTasks;
    private int totalTasks;
    private int upcomingTasksCount;
}
