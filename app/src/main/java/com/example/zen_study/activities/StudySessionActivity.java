package com.example.zen_study.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.zen_study.R;
import com.example.zen_study.models.StudySession;
import com.example.zen_study.viewmodels.StudySessionViewModel;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

public class StudySessionActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_START_TIME = "start_time";
    public static final String EXTRA_END_TIME = "end_time";

    private StudySessionViewModel viewModel;
    private TextView textTimer, textSessionType, textRoundProgress;
    private CircularProgressIndicator progressCircle;
    private Button btnPlayPause, btnStop;
    private SwitchCompat switchMode;
    private TextInputEditText editWorkDuration, editBreakDuration, editRounds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_session);

        // Get intent data
        var taskId = getIntent().getLongExtra(EXTRA_TASK_ID, -1);
        var startTime = getIntent().getLongExtra(EXTRA_START_TIME, 0);
        var endTime = getIntent().getLongExtra(EXTRA_END_TIME, 0);

        if (taskId == -1) {
            finish();
            return;
        }

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(StudySessionViewModel.class);

        // Initialize session with task data
        viewModel.initializeSession(taskId, startTime, endTime);

        initializeViews();
        setupObservers();
        setupModeSwitch();
        setupButtons();
    }

    private void initializeViews() {
        textTimer = findViewById(R.id.text_timer);
        textSessionType = findViewById(R.id.text_session_type);
        textRoundProgress = findViewById(R.id.text_round_progress);
        progressCircle = findViewById(R.id.progress_circle);
        btnPlayPause = findViewById(R.id.btn_play_pause);
        btnStop = findViewById(R.id.btn_stop);
        switchMode = findViewById(R.id.switch_mode);
        editWorkDuration = findViewById(R.id.edit_work_duration);
        editBreakDuration = findViewById(R.id.edit_break_duration);
        editRounds = findViewById(R.id.edit_rounds);
    }

    private void setupObservers() {
        // Timer text observer
        viewModel.getTimerText().observe(this, timerText ->
                textTimer.setText(timerText));

        // Session type observer
        viewModel.getSessionType().observe(this, sessionType ->
                textSessionType.setText(sessionType));

        // Round progress observer
        viewModel.getRoundProgress().observe(this, roundProgress ->
                textRoundProgress.setText(roundProgress));

        // Progress percentage observer
        viewModel.getProgressPercentage().observe(this, progress ->
                progressCircle.setProgress(progress));

        // Timer running state observer
        viewModel.getTimerRunning().observe(this, isRunning -> {
            if (isRunning) {
                btnPlayPause.setText("Pause");
                btnPlayPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause, 0, 0, 0);
            } else {
                btnPlayPause.setText("Start");
                btnPlayPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play, 0, 0, 0);
            }
        });

        // Current mode observer
        viewModel.getCurrentMode().observe(this, mode -> {
            switchMode.setChecked(mode == StudySession.StudySessionMode.POMODORO);
            updatePomodoroSettingsVisibility(mode == StudySession.StudySessionMode.POMODORO);
        });
    }

    private void updatePomodoroSettingsVisibility(boolean isPomodoroMode) {
        if (isPomodoroMode) {
            findViewById(R.id.layout_pomodoro_settings).setVisibility(android.view.View.VISIBLE);
            textRoundProgress.setVisibility(android.view.View.VISIBLE);
        } else {
            findViewById(R.id.layout_pomodoro_settings).setVisibility(android.view.View.GONE);
            textRoundProgress.setVisibility(android.view.View.GONE);
        }
    }

    private void setupModeSwitch() {
        switchMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            StudySession.StudySessionMode mode = isChecked ?
                    StudySession.StudySessionMode.POMODORO :
                    StudySession.StudySessionMode.NORMAL;
            viewModel.setMode(mode);
            viewModel.stopTimer();
        });

        // Set initial mode
        viewModel.setMode(StudySession.StudySessionMode.NORMAL);
    }

    private void setupButtons() {
        btnPlayPause.setOnClickListener(v -> {
            Boolean isRunning = viewModel.getTimerRunning().getValue();
            if (isRunning != null && isRunning) {
                viewModel.pauseTimer();
            } else {
                // If in pomodoro mode, update settings first
                if (viewModel.getCurrentMode().getValue() == StudySession.StudySessionMode.POMODORO) {
                    updatePomodoroSettingsFromInputs();
                }
                viewModel.startTimer();
            }
        });

        btnStop.setOnClickListener(v -> viewModel.stopTimer());
    }

    private void updatePomodoroSettingsFromInputs() {
        try {
            int workMinutes = Integer.parseInt(editWorkDuration.getText().toString());
            int breakMinutes = Integer.parseInt(editBreakDuration.getText().toString());
            int rounds = Integer.parseInt(editRounds.getText().toString());

            viewModel.updatePomodoroSettings(workMinutes, breakMinutes, rounds);
        } catch (NumberFormatException e) {
            // Use default values if parsing fails
            viewModel.updatePomodoroSettings(25, 5, 4);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ViewModel will handle cleanup in onCleared()
    }
}