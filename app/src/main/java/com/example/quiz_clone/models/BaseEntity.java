package com.example.quiz_clone.models;

import androidx.room.PrimaryKey;

import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@Data
@SuperBuilder
public class BaseEntity {
    @PrimaryKey(autoGenerate = true)
    protected Long id;
    protected Date createdAt, lastUpdatedAt;
}
