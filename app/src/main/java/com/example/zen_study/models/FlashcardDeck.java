package com.example.zen_study.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity(tableName = "flashcard_deck", foreignKeys = {
        @ForeignKey(entity = Subject.class, parentColumns = "id", childColumns = "subject_id")
}, indices = {
        @Index(value = "id")
})
@Data
@SuperBuilder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FlashcardDeck extends BaseEntity {
    @ColumnInfo(name = "subject_id")
    private Long subjectId;
    private String title, description;
    private int cardCount;
}
