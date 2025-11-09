package com.example.quiz_clone.models;

import androidx.room.PrimaryKey;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BaseEntity {
    @PrimaryKey(autoGenerate = true)
    protected Long id;
}
