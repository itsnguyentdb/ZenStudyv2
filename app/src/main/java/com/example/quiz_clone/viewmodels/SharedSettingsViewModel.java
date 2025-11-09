package com.example.quiz_clone.viewmodels;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.quiz_clone.repositories.impls.SettingsRepositoryImpl;

import java.util.Map;

public class SharedSettingsViewModel extends AndroidViewModel {
    private SettingsRepositoryImpl settingsRepository;

    private MutableLiveData<Boolean> darkModeEnabled = new MutableLiveData<>();
    private MutableLiveData<String> appLanguage = new MutableLiveData<>();

    public SharedSettingsViewModel(@NonNull Application application) {
        super(application);
        settingsRepository = new SettingsRepositoryImpl(application.getApplicationContext());
        loadInitialSettings();
        observeSettingsChanges();
    }

    private void observeSettingsChanges() {
        settingsRepository.getDarkModeLiveData().observeForever(isEnabled -> {
            darkModeEnabled.setValue(isEnabled);
        });
        settingsRepository.getAppLanguageLiveData().observeForever(languageCode -> {
            appLanguage.setValue(languageCode);
        });
    }

    private void loadInitialSettings() {
        darkModeEnabled.setValue(settingsRepository.isDarkModeEnabled());
        appLanguage.setValue(settingsRepository.getAppLanguage());
    }

    public LiveData<Boolean> getDarkModeEnabled() {
        return darkModeEnabled;
    }

    public void toggleDarkMode() {
        var currentValue = settingsRepository.isDarkModeEnabled();
        settingsRepository.setDarkModeEnabled(!currentValue);
    }

    public boolean getCurrentDarkMode() {
        return settingsRepository.isDarkModeEnabled();
    }

    public void setDarkModeEnabled(boolean enabled) {
        settingsRepository.setDarkModeEnabled(enabled);
    }

    public void setAppLanguage(String languageCode) {
        settingsRepository.setAppLanguage(languageCode);
    }

    public String getCurrentLanguage() {
        return settingsRepository.getAppLanguage();
    }

    public LiveData<String> getAppLanguage() {
        return appLanguage;
    }

    public Map<String, String> getAvailableLanguages() {
        return settingsRepository.getAvailableLanguages();
    }

    public String getLanguageDisplayName(String languageCode) {
        return settingsRepository.getLanguageDisplayName(languageCode);
    }
}
