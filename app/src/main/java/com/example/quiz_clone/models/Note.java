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

@Entity(tableName = "note", foreignKeys = {
        @ForeignKey(entity = Subject.class, parentColumns = "id", childColumns = "subject_id", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = StudySession.class, parentColumns = "id", childColumns = "session_id", onDelete = ForeignKey.CASCADE),
})
@Data
@Builder

@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Note extends BaseEntity {
    @ColumnInfo(name = "subject_id")
    private long subjectId;
    @ColumnInfo(name = "session_id")
    private long sessionId;
    private String context;
    private String tags;
    private String imageUri;
    private Date createdAt;
}
