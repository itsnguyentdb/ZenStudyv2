package com.example.quiz_clone.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity(tableName = "task", foreignKeys = {
        @ForeignKey(entity = Subject.class, parentColumns = "id", childColumns = "subject_id", onDelete = ForeignKey.CASCADE),
//        @ForeignKey(entity = Task.class, parentColumns = "id", childColumns = "parent_task_id", onDelete = ForeignKey.CASCADE),
})
@Data
@SuperBuilder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Task extends BaseEntity {
    @ColumnInfo(name = "subject_id")
    private long subjectId;
    @ColumnInfo(name = "parent_task_id")
    private long parentTaskId;
    private String title;
    private String description;
    private Date deadline;
    private int priority;
    private int expectedDuration;
    private int progressDuration;
    private float progress;
    private int level;
    private TaskRepeatType repeatType;
    private TaskType status;

    public enum TaskType {
        TODO,
        IN_PROGRESS,
        COMPLETED,
        OVERDUE
    }

    public enum TaskRepeatType {
        NONE,
        DAILY,
        WEEKLY,
        MONTHLY
    }
}
