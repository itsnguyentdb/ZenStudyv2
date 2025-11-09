package com.example.quiz_clone.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.quiz_clone.R;
import com.example.quiz_clone.fragments.StudyFlashcardFragment;
import com.example.quiz_clone.helpers.AppDatabase;
import com.example.quiz_clone.repositories.impls.FlashcardRepositoryImpl;

public class LoadingFlashcardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loading_flashcard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        var executor = AppDatabase.getInstance(this).getQueryExecutor();

        executor.execute(() -> {
            try {
                var flashcardRepository = new FlashcardRepositoryImpl(this);
//                flashcardRepository.deleteDeckWithTerms(1);
                // Check if deck exists (now on background thread)
                var tryFind = flashcardRepository.getDeckById(1).getValue();
                long deckId;

                if (tryFind == null) {
                    // Create deck and terms on background thread
                    var deck = flashcardRepository.createFlashcardDeck("Test title", "Test description");
                    deckId = deck.getId();
                    flashcardRepository.addTermToDeck(deckId, "Test A", "Test A Definition");
                    flashcardRepository.addTermToDeck(deckId, "Test B", "Test B Definition");
                    flashcardRepository.addTermToDeck(deckId, "Test C", "Test C Definition");
                    flashcardRepository.addTermToDeck(deckId, "Test D", "Test D Definition");
                    flashcardRepository.addTermToDeck(deckId, "Test E", "Test E Definition");

                } else {
                    deckId = tryFind.getId();
                }

                // Switch to main thread to start activity
                runOnUiThread(() -> {
                    var intent = new Intent(this, StudyFlashcardFragment.class);
                    intent.putExtra("DECK_ID", deckId);
                    startActivity(intent);
                    finish();
                });

            } catch (Exception e) {
                e.printStackTrace();
                // Handle error - maybe show a toast and finish
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }
}