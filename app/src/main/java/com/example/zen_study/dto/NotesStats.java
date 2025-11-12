package com.example.zen_study.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotesStats {
    private int notesCount;
    private int resourcesCount;
    private String mostLinkedSubject;
    private String lastUpdatedNote;
    private int notesThisWeek;
}
