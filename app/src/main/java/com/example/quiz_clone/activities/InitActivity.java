package com.example.quiz_clone.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.quiz_clone.BaseActivity;
import com.example.quiz_clone.R;
import com.example.quiz_clone.helpers.SettingPreferences;
import com.example.quiz_clone.models.BaseEntity;
import com.example.quiz_clone.repositories.impls.SubjectRepositoryImpl;

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