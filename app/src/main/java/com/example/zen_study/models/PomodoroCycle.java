package com.example.zen_study.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
@Entity(tableName = "pomodoro_cycle", foreignKeys = {
        @ForeignKey(entity = StudySession.class, parentColumns = "id", childColumns = "session_id")
})
@Data
@SuperBuilder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PomodoroCycle extends BaseEntity {
    @ColumnInfo(name = "session_id")
    private long sessionId;
    private int workMinutes;
    private int breakMinutes;
    private int rounds;
    private int completedRounds;
}
