package com.example.quiz_clone.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Entity(tableName = "subject", indices = {
        @Index(value = {"id", "name"}, unique = true),
})
@Data
@SuperBuilder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Subject extends BaseEntity {
    @NonNull
    @ColumnInfo(name = "name")
    private String name;
    private String description, colorCode;
    public static final String DEFAULT_COLOR_CODE = "";
}
