package com.example.quiz_clone.models;

import androidx.room.Entity;
import androidx.room.Index;

import java.util.Date;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity(tableName = "flashcard_deck", indices = {
        @Index(value = "id")
})
@Data
@Builder

@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FlashcardDeck extends BaseEntity {
    private String title, description;
    private Date createdTime, lastUpdatedTime;
}
