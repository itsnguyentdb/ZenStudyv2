package com.example.zen_study.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Entity(tableName = "subject", indices = {
        @Index(value = {"id", "name"}, unique = true),
})
@Data
@SuperBuilder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Subject extends BaseEntity implements Serializable {
    @NonNull
    @ColumnInfo(name = "name")
    private String name;
    private String description, colorCode;
    public static final String DEFAULT_COLOR_CODE = "";
}
