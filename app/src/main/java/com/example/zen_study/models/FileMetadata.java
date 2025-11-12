package com.example.zen_study.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Entity(tableName = "file_metadata", foreignKeys = {
})
@Data
@SuperBuilder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FileMetadata extends BaseEntity implements Serializable {
    private String contentType;
    private Long size;
    private Long duration;
    private String md5Checksum;
    private String path;
    private Date uploadedAt;
}
