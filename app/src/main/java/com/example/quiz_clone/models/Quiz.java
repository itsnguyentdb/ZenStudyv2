package com.example.quiz_clone.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Entity(tableName = "quiz", foreignKeys = {
        @ForeignKey(entity = Subject.class, parentColumns = "id", childColumns = "subject_id", onDelete = ForeignKey.CASCADE)
}, indices = {
        @Index(value = "id"),
        @Index(value = "title"),
        @Index(value = "createdAt"),
        @Index(value = "lastUpdatedAt"),
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Quiz extends BaseEntity {
    @ColumnInfo(name = "subject_id")
    private Long subjectId;
    private String title, description;
    private int timeLimit;

    @Ignore
    private List<QuizQuestion> questions;
}
