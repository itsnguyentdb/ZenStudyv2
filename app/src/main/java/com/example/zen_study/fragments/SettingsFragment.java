package com.example.zen_study.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.zen_study.activities.BaseActivity;
import com.example.zen_study.R;
import com.example.zen_study.components.LanguageDialog;
import com.example.zen_study.viewmodels.SharedSettingsViewModel;

public class SettingsFragment extends Fragment {

    private SharedSettingsViewModel settingsViewModel;

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch darkModeSwitch;
    private TextView currentLanguageText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        settingsViewModel = new ViewModelProvider(this).get(SharedSettingsViewModel.class);

        initViews(view);
        setupObservers();
        setupClickListeners(view);
    }

    private void initViews(View view) {
        darkModeSwitch = view.findViewById(R.id.darkModeSwitch);
        currentLanguageText = view.findViewById(R.id.currentLanguageText);
    }

    private void setupObservers() {
        settingsViewModel.getDarkModeEnabled().observe(getViewLifecycleOwner(), isEnabled -> {
            darkModeSwitch.setChecked(isEnabled);
            applyTheme(isEnabled);
        });

        settingsViewModel.getAppLanguage().observe(getViewLifecycleOwner(), languageCode -> {
            String displayName = settingsViewModel.getLanguageDisplayName(languageCode);
            currentLanguageText.setText(displayName);
        });
    }

    private void setupClickListeners(View view) {
        view.findViewById(R.id.darkModeContainer).setOnClickListener(v -> {
            darkModeSwitch.setChecked(!darkModeSwitch.isChecked());
        });

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsViewModel.setDarkModeEnabled(isChecked);
        });

        view.findViewById(R.id.languageContainer).setOnClickListener(v -> showLanguageDialog());
    }

    private void showLanguageDialog() {
        if (getContext() == null) return;

        LanguageDialog dialog = new LanguageDialog(getContext(), settingsViewModel,
                languageCode -> {
                    settingsViewModel.setAppLanguage(languageCode);
                    showLanguageChangeDialog(languageCode);
                });
        dialog.show();
    }

    private void showLanguageChangeDialog(String newLanguageCode) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Language Changed")
                .setMessage("The app needs to restart to apply the language change. Restart now?")
                .setPositiveButton("Restart", (dialog, which) -> restartApp())
                .setNegativeButton("Later", null)
                .setCancelable(false)
                .show();
    }

    private void restartApp() {
        if (getContext() == null) return;
        Intent intent = new Intent(getContext(), BaseActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void applyTheme(boolean isDarkMode) {
        if (getActivity() == null) return;
        View rootView = getActivity().getWindow().getDecorView();
        rootView.setBackgroundColor(isDarkMode ? Color.BLACK : Color.WHITE);
    }
}
