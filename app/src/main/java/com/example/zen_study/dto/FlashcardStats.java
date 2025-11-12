package com.example.zen_study.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardStats {
    private int decksReviewed;
    private int cardsReviewed;
    private int accuracy;
    private int accuracyChange;
    private String mostStudiedDeck;
}
