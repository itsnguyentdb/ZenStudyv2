package com.example.quiz_clone.viewmodels;

import android.app.Application;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.quiz_clone.models.StudySession;
import com.example.quiz_clone.models.PomodoroCycle;
import com.example.quiz_clone.repositories.impls.StudySessionRepositoryImpl;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class StudySessionViewModel extends AndroidViewModel {
    private final StudySessionRepositoryImpl repository;

    // Timer related LiveData
    private final MutableLiveData<String> timerText = new MutableLiveData<>();
    private final MutableLiveData<String> sessionType = new MutableLiveData<>();
    private final MutableLiveData<String> roundProgress = new MutableLiveData<>();
    private final MutableLiveData<Integer> progressPercentage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> timerRunning = new MutableLiveData<>(false);
    private final MutableLiveData<StudySession.StudySessionMode> currentMode =
            new MutableLiveData<>(StudySession.StudySessionMode.NORMAL);

    // Timer state
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;
    private boolean isTimerRunning = false;

    // Pomodoro specific variables
    private boolean isWorkPhase = true;
    private int currentRound = 1;
    private int totalRounds = 4;
    private long workDurationMillis = 25 * 60 * 1000; // 25 minutes
    private long breakDurationMillis = 5 * 60 * 1000;  // 5 minutes
    private static final long NORMAL_SESSION_DURATION = 25 * 60 * 1000;

    // Current session data
    private StudySession currentSession;
    private PomodoroCycle currentPomodoroCycle;
    private long taskId;
    private Date plannedStartTime;
    private Date plannedEndTime;

    public StudySessionViewModel(@NonNull Application application) {
        super(application);
        this.repository = new StudySessionRepositoryImpl(application);
        initializeTimer();
    }

    // Initialize with task data
    public void initializeSession(long taskId, long startTime, long endTime) {
        this.taskId = taskId;
        this.plannedStartTime = new Date(startTime);
        this.plannedEndTime = new Date(endTime);

        // Set initial timer based on planned duration for normal mode
        if (currentMode.getValue() == StudySession.StudySessionMode.NORMAL) {
            timeLeftInMillis = endTime - startTime;
            updateTimerDisplay();
        }
    }

    private void initializeTimer() {
        resetTimer();
    }

    // Timer control methods
    public void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        // Create current session when timer starts
        if (currentSession == null) {
            createNewSession();
        }

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerDisplay();
                updateProgressCircle();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                timeLeftInMillis = 0;
                updateTimerDisplay();
                timerRunning.setValue(false);

                if (currentMode.getValue() == StudySession.StudySessionMode.POMODORO) {
                    handlePomodoroCycleCompletion();
                } else {
                    handleNormalSessionCompletion();
                }
            }
        }.start();

        isTimerRunning = true;
        timerRunning.setValue(true);
    }

    public void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        timerRunning.setValue(false);
    }

    public void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        saveSessionData();
        resetTimer();
    }

    public void resetTimer() {
        isTimerRunning = false;
        timerRunning.setValue(false);

        if (currentMode.getValue() == StudySession.StudySessionMode.POMODORO) {
            updatePomodoroSettings();
        } else {
            timeLeftInMillis = NORMAL_SESSION_DURATION;
            updateTimerDisplay();
        }

        progressPercentage.setValue(100);
    }

    // Mode management
    public void setMode(StudySession.StudySessionMode mode) {
        currentMode.setValue(mode);
        updateUIForMode();
        resetTimer();
    }

    private void updateUIForMode() {
        if (currentMode.getValue() == StudySession.StudySessionMode.POMODORO) {
            sessionType.setValue("Work Time");
            roundProgress.setValue("Round 1/" + totalRounds);
            updatePomodoroSettings();
        } else {
            sessionType.setValue("Normal Session");
            roundProgress.setValue("");
            timeLeftInMillis = NORMAL_SESSION_DURATION;
            updateTimerDisplay();
        }
    }

    // Pomodoro settings
    public void updatePomodoroSettings(int workMinutes, int breakMinutes, int rounds) {
        this.workDurationMillis = workMinutes * 60 * 1000L;
        this.breakDurationMillis = breakMinutes * 60 * 1000L;
        this.totalRounds = rounds;

        isWorkPhase = true;
        currentRound = 1;
        timeLeftInMillis = workDurationMillis;
        updateTimerDisplay();
        updatePomodoroProgress();
    }

    private void updatePomodoroSettings() {
        // Use current settings
        isWorkPhase = true;
        currentRound = 1;
        timeLeftInMillis = workDurationMillis;
        updateTimerDisplay();
        updatePomodoroProgress();
    }

    // Session creation and saving
    private void createNewSession() {
        currentSession = StudySession.builder()
                .taskId(taskId)
                .startTime(new Date())
                .duration(0)
                .endTime(null)
                .mode(currentMode.getValue())
                .build();

        // If in pomodoro mode, create pomodoro cycle
        if (currentMode.getValue() == StudySession.StudySessionMode.POMODORO) {
            currentPomodoroCycle = PomodoroCycle.builder()
                    .sessionId(0) // Will be set after session is inserted
                    .workMinutes((int) (workDurationMillis / (60 * 1000)))
                    .breakMinutes((int) (breakDurationMillis / (60 * 1000)))
                    .rounds(totalRounds)
                    .completedRounds(0)
                    .build();
        }
    }

    private void saveSessionData() {
        if (currentSession != null) {
            // Update session end time and duration
            currentSession.setEndTime(new Date());
            long duration = currentSession.getEndTime().getTime() - currentSession.getStartTime().getTime();
            currentSession.setDuration(duration);

            // Save to database
            repository.insertStudySession(currentSession);

            // Save pomodoro cycle if exists
            if (currentPomodoroCycle != null && currentSession.getId() != 0) {
                currentPomodoroCycle.setSessionId(currentSession.getId());
                currentPomodoroCycle.setCompletedRounds(currentRound - 1); // Current round is the next one
                repository.insertPomodoroCycle(currentPomodoroCycle);
            }
        }

        // Reset current session
        currentSession = null;
        currentPomodoroCycle = null;
    }

    // Timer completion handlers
    private void handlePomodoroCycleCompletion() {
        if (isWorkPhase) {
            // Work phase completed, start break
            isWorkPhase = false;
            timeLeftInMillis = breakDurationMillis;
            sessionType.setValue("Break Time");
            startTimer();
        } else {
            // Break completed, start next work phase or finish
            currentRound++;
            if (currentRound <= totalRounds) {
                isWorkPhase = true;
                timeLeftInMillis = workDurationMillis;
                sessionType.setValue("Work Time");
                updatePomodoroProgress();
                startTimer();
            } else {
                // All rounds completed
                sessionType.setValue("Session Completed!");
                saveSessionData();
            }
        }
    }

    private void handleNormalSessionCompletion() {
        sessionType.setValue("Session Completed!");
        saveSessionData();
    }

    // UI update methods
    private void updateTimerDisplay() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        timerText.setValue(timeLeftFormatted);
    }

    private void updateProgressCircle() {
        long totalDuration = currentMode.getValue() == StudySession.StudySessionMode.POMODORO ?
                (isWorkPhase ? workDurationMillis : breakDurationMillis) :
                NORMAL_SESSION_DURATION;

        int progress = (int) ((timeLeftInMillis * 100) / totalDuration);
        progressPercentage.setValue(progress);
    }

    private void updatePomodoroProgress() {
        roundProgress.setValue(String.format("Round %d/%d", currentRound, totalRounds));
    }

    // Getters for LiveData
    public LiveData<String> getTimerText() {
        return timerText;
    }

    public LiveData<String> getSessionType() {
        return sessionType;
    }

    public LiveData<String> getRoundProgress() {
        return roundProgress;
    }

    public LiveData<Integer> getProgressPercentage() {
        return progressPercentage;
    }

    public LiveData<Boolean> getTimerRunning() {
        return timerRunning;
    }

    public LiveData<StudySession.StudySessionMode> getCurrentMode() {
        return currentMode;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}