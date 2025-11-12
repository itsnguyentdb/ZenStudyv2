package com.example.zen_study.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zen_study.BaseActivity;
import com.example.zen_study.R;
import com.example.zen_study.helpers.SettingPreferences;
import com.example.zen_study.repositories.impls.SubjectRepositoryImpl;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class InitActivity extends AppCompatActivity {
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        var settingPreferences = new SettingPreferences(this);
        if (settingPreferences.isFirstInit()) {
            executor.execute(() -> {
                var subjectRepository = new SubjectRepositoryImpl(this);
                Arrays.stream(new SettingPreferences(this).BASE_SUBJECTS)
                        .forEach(subjectRepository::addSubject);
                new SettingPreferences(this).setFirstInit(false);
                runOnUiThread(this::startNextActivity);
            });
        } else {
            startNextActivity();
        }
    }

    private void startNextActivity() {
        startActivity(new Intent(this, BaseActivity.class));
        finish();
    }
}