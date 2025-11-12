package com.example.zen_study.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Entity(tableName = "resource"
        , foreignKeys = {
        @ForeignKey(entity = FileMetadata.class, parentColumns = "id", childColumns = "file_metadata_id", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = Task.class, parentColumns = "id", childColumns = "task_id", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = Subject.class, parentColumns = "id", childColumns = "subject_id", onDelete = ForeignKey.CASCADE),
}
        , indices = {
        @Index(value = "id"),
        @Index(value = "title")
})
@Data
@SuperBuilder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Resource extends BaseEntity implements Serializable {
    @ColumnInfo(name = "file_metadata_id")
    private Long fileMetadataId;
    @ColumnInfo(name = "task_id")
    private Long taskId;
    @ColumnInfo(name = "subject_id")
    private Long subjectId;
    private String title;
    private String type;
    private Date createdAt;
    private Date updatedAt;
}
