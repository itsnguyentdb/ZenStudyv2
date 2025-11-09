package com.example.quiz_clone.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import java.util.Date;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity(tableName = "study_session", foreignKeys = {
        @ForeignKey(entity = Task.class, parentColumns = "id", childColumns = "task_id", onDelete = ForeignKey.CASCADE),
})
@Data
@Builder

@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StudySession extends BaseEntity {
    @ColumnInfo(name = "task_id")
    private long taskId;
    private Date startTime;
    private long duration;
    private Date endTime;

    private StudySessionMode mode;
    private Date createdAt;
    private Date lastUpdatedAt;

    public enum StudySessionMode {
        NORMAL,
        POMODORO
    }
}
