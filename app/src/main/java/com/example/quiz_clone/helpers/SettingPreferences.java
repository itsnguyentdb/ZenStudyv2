package com.example.quiz_clone.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.LinkedHashMap;
import java.util.Map;

public class SettingPreferences {
    private SharedPreferences sharedPreferences;
    public static final String[] BASE_SUBJECTS = {"Math", "Science", "History", "Language", "Computer Science", "Art", "Music", "Other"};
    private static final String DEFAULT_LANGUAGE_CODE = "en";

    private static final String KEY_FIRST_INIT = "first_init";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_APP_LANGUAGES = "languages";

    public SettingPreferences(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isFirstInit() {
        return sharedPreferences.getBoolean(KEY_FIRST_INIT, true);
    }

    public void setFirstInit(boolean value) {
        sharedPreferences.edit().putBoolean(KEY_FIRST_INIT, value).apply();
    }

    public boolean isDarkModeEnabled() {
        return sharedPreferences.getBoolean(KEY_DARK_MODE, false);
    }

    public void setDarkModeEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
    }

    public String getCurrentLanguageCode() {
        return sharedPreferences.getString(KEY_APP_LANGUAGES, DEFAULT_LANGUAGE_CODE);
    }

    public LiveData<Boolean> getDarkModeLiveData() {
        var liveData = new MutableLiveData<Boolean>();
        liveData.setValue(isDarkModeEnabled());

        sharedPreferences.registerOnSharedPreferenceChangeListener((prefs, key) -> {
            if (KEY_DARK_MODE.equals(key)) {
                liveData.setValue(prefs.getBoolean(key, false));
            }
        });
        return liveData;
    }

    public String getAppLanguage() {
        return sharedPreferences.getString(KEY_APP_LANGUAGES, DEFAULT_LANGUAGE_CODE);
    }

    public void setAppLanguage(String languageCode) {
        sharedPreferences.edit().putString(KEY_APP_LANGUAGES, languageCode).apply();
    }

    public LiveData<String> getAppLanguageLiveData() {
        var liveData = new MutableLiveData<String>();
        liveData.setValue(getAppLanguage());

        sharedPreferences.registerOnSharedPreferenceChangeListener((prefs, key) -> {
            if (KEY_APP_LANGUAGES.equals(key)) {
                liveData.setValue(prefs.getString(key, DEFAULT_LANGUAGE_CODE));
            }
        });
        return liveData;
    }

    public String getLanguageDisplayName(String languageCode) {
        switch (languageCode) {
            case "system":
                return "System Default";
            case "en":
                return "English";
            case "es":
                return "Español";
            case "fr":
                return "Français";
            case "de":
                return "Deutsch";
            case "zh":
                return "中文";
            case "ja":
                return "日本語";
            case "ko":
                return "한국어";
            default:
                return "Unknown";
        }
    }

    public Map<String, String> getAvailableLanguages() {
        Map<String, String> languages = new LinkedHashMap<>();
        languages.put("system", "System Default");
        languages.put("en", "English");
        languages.put("es", "Español");
        languages.put("fr", "Français");
        languages.put("de", "Deutsch");
        languages.put("zh", "中文");
        languages.put("ja", "日本語");
        languages.put("ko", "한국어");
        return languages;
    }
}
