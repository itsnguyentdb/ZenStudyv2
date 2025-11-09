package com.example.quiz_clone.models;

import androidx.room.Entity;
import androidx.room.Index;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity(tableName = "subject", indices = {
        @Index(value = "id"),
        @Index(value = "name")
})
@Data
@Builder

@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Subject extends BaseEntity {
    private String name, description, colorCode;
    private static final String DEFAULT_COLOR_CODE = "";
}
