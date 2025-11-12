package com.example.zen_study.repositories.impls;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.zen_study.helpers.SettingPreferences;

import java.util.Map;

public class SettingsRepositoryImpl {
    private final SettingPreferences settingPreferences;

    public SettingsRepositoryImpl(Context context) {
        settingPreferences = new SettingPreferences(context);
    }

    public LiveData<Boolean> getDarkModeLiveData() {
        return settingPreferences.getDarkModeLiveData();
    }

    public boolean isDarkModeEnabled() {
        return settingPreferences.isDarkModeEnabled();
    }

    public void setDarkModeEnabled(boolean enabled) {
        settingPreferences.setDarkModeEnabled(enabled);
    }

    public String getAppLanguage() {
        return settingPreferences.getAppLanguage();
    }

    public void setAppLanguage(String languageCode) {
        settingPreferences.setAppLanguage(languageCode);
    }

    public LiveData<String> getAppLanguageLiveData() {
        return settingPreferences.getAppLanguageLiveData();
    }

    public Map<String, String> getAvailableLanguages() {
        return settingPreferences.getAvailableLanguages();
    }

    public String getLanguageDisplayName(String languageCode) {
        return settingPreferences.getLanguageDisplayName(languageCode);
    }
}
