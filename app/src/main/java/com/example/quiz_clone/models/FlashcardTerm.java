package com.example.quiz_clone.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;


@Entity(tableName = "flashcard_term", foreignKeys = {
        @ForeignKey(entity = FlashcardDeck.class, parentColumns = "id", childColumns = "flashcard_deck_id", onDelete = ForeignKey.CASCADE)
}, indices = {
        @Index(value = "id"),
})
@Data
@SuperBuilder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FlashcardTerm extends BaseEntity {
    @ColumnInfo(name = "flashcard_deck_id")
    private Long flashcardDeckId;
    private String term, definition;
    private int position, rating;
}
