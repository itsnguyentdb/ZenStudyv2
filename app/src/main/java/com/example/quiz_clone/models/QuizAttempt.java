package com.example.quiz_clone.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
@Entity(tableName = "quiz_attempt", foreignKeys = {
        @ForeignKey(entity = Quiz.class, parentColumns = "id", childColumns = "quiz_id", onDelete = ForeignKey.CASCADE)
}, indices = {
        @Index(value = "id"),
})
@Data
@SuperBuilder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class QuizAttempt extends BaseEntity {
    @ColumnInfo(name = "quiz_id")
    private Long quizId;
    private Date startTime, endTime;
    private int score;
    private long duration;
}
