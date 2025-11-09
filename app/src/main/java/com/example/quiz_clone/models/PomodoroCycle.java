package com.example.quiz_clone.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity(tableName = "pomodoro_cycle", foreignKeys = {
        @ForeignKey(entity = StudySession.class, parentColumns = "id", childColumns = "session_id")
})
@Data
@Builder

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
