package com.example.zen_study.components;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zen_study.R;
import com.example.zen_study.adapters.LanguageAdapter;
import com.example.zen_study.viewmodels.SharedSettingsViewModel;

import java.util.ArrayList;
import java.util.Map;

public class LanguageDialog extends Dialog {
    private SharedSettingsViewModel settingsViewModel;
    private OnLanguageSelectedListener listener;

    public interface OnLanguageSelectedListener {
        void onLanguageSelected(String languageCode);
    }

    public LanguageDialog(@NonNull Context context, SharedSettingsViewModel viewModel, OnLanguageSelectedListener listener) {
        super(context);
        this.settingsViewModel = viewModel;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_language);

        setupDialog();
    }

    private void setupDialog() {
        setTitle("Select Language");

        RecyclerView recyclerView = findViewById(R.id.languageRecyclerView);
        Button cancelButton = findViewById(R.id.cancelButton);

        // Get available languages
        Map<String, String> languages = settingsViewModel.getAvailableLanguages();
        String currentLanguage = settingsViewModel.getCurrentLanguage();

        // Setup adapter
        LanguageAdapter adapter = new LanguageAdapter(new ArrayList<>(languages.entrySet()), currentLanguage);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnLanguageSelectedListener(languageCode -> {
            if (listener != null) {
                listener.onLanguageSelected(languageCode);
            }
            dismiss();
        });

        cancelButton.setOnClickListener(v -> dismiss());
    }
}
