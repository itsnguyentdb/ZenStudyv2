package com.example.quiz_clone.components;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.quiz_clone.R;
import com.example.quiz_clone.activities.StudySessionActivity;
import com.example.quiz_clone.utils.TimePickerUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

import lombok.Getter;

public class SessionTimeDialog extends Dialog {

    private Context context;
    private long taskId;
    @Getter
    private Calendar startTime;
    @Getter
    private Calendar endTime;

    private TextInputEditText editStartTime, editEndTime;
    private TextView textDurationPreview;
    private MaterialButton btnCancel, btnStart;

    public SessionTimeDialog(@NonNull Context context, long taskId) {
        super(context);
        this.context = context;
        this.taskId = taskId;
        initializeTimes();
    }

    private void initializeTimes() {
        startTime = Calendar.getInstance();
        endTime = Calendar.getInstance();
        endTime.add(Calendar.MINUTE, 25); // Default 25 minutes from now
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_session_time);

        initializeViews();
        setupClickListeners();
        updateTimeDisplays();
        updateDurationPreview();
    }

    private void initializeViews() {
        editStartTime = findViewById(R.id.edit_start_time);
        editEndTime = findViewById(R.id.edit_end_time);
        textDurationPreview = findViewById(R.id.text_duration_preview);
        btnCancel = findViewById(R.id.btn_cancel);
        btnStart = findViewById(R.id.btn_start_study_session);
    }

    private void setupClickListeners() {
        // Start Time Picker
        editStartTime.setOnClickListener(v -> showStartTimePicker());

        // End Time Picker
        editEndTime.setOnClickListener(v -> showEndTimePicker());

        // Cancel Button
        btnCancel.setOnClickListener(v -> dismiss());

        // Start Session Button
        btnStart.setOnClickListener(v -> startStudySession());
    }

    private void showStartTimePicker() {
        TimePickerUtils.showTimePicker(context, startTime.get(Calendar.HOUR_OF_DAY),
                startTime.get(Calendar.MINUTE), (hour, minute) -> {
                    startTime.set(Calendar.HOUR_OF_DAY, hour);
                    startTime.set(Calendar.MINUTE, minute);

                    // If end time is before start time, adjust end time
                    if (endTime.before(startTime)) {
                        endTime.setTime(startTime.getTime());
                        endTime.add(Calendar.MINUTE, 25);
                    }

                    updateTimeDisplays();
                    updateDurationPreview();
                });
    }

    private void showEndTimePicker() {
        TimePickerUtils.showTimePicker(context, endTime.get(Calendar.HOUR_OF_DAY),
                endTime.get(Calendar.MINUTE), (hour, minute) -> {
                    endTime.set(Calendar.HOUR_OF_DAY, hour);
                    endTime.set(Calendar.MINUTE, minute);

                    // Validate end time is after start time
                    if (endTime.before(startTime)) {
                        endTime.setTime(startTime.getTime());
                        endTime.add(Calendar.MINUTE, 25);
                    }

                    updateTimeDisplays();
                    updateDurationPreview();
                });
    }

    private void updateTimeDisplays() {
        editStartTime.setText(TimePickerUtils.formatTimeWithAmPm(
                startTime.get(Calendar.HOUR_OF_DAY),
                startTime.get(Calendar.MINUTE)));

        editEndTime.setText(TimePickerUtils.formatTimeWithAmPm(
                endTime.get(Calendar.HOUR_OF_DAY),
                endTime.get(Calendar.MINUTE)));
    }

    private void updateDurationPreview() {
        long durationMillis = endTime.getTimeInMillis() - startTime.getTimeInMillis();
        long durationMinutes = durationMillis / (60 * 1000);

        if (durationMinutes < 0) {
            textDurationPreview.setText("Invalid time range");
            textDurationPreview.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            btnStart.setEnabled(false);
        } else {
            String durationText;
            if (durationMinutes < 60) {
                durationText = String.format(Locale.getDefault(), "Duration: %d minutes", durationMinutes);
            } else {
                long hours = durationMinutes / 60;
                long minutes = durationMinutes % 60;
                if (minutes == 0) {
                    durationText = String.format(Locale.getDefault(), "Duration: %d hour%s",
                            hours, hours > 1 ? "s" : "");
                } else {
                    durationText = String.format(Locale.getDefault(), "Duration: %d hour%s %d minute%s",
                            hours, hours > 1 ? "s" : "", minutes, minutes > 1 ? "s" : "");
                }
            }

            textDurationPreview.setText(durationText);
//            textDurationPreview.setTextColor(context.getResources().getColor(R.color.green));
            btnStart.setEnabled(true);
        }
    }

    private void startStudySession() {
        // Validate times
        if (endTime.before(startTime)) {
            textDurationPreview.setText("End time must be after start time");
            textDurationPreview.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            return;
        }

        // Start StudySessionActivity with time data
        Intent intent = new Intent(context, StudySessionActivity.class);
        intent.putExtra(StudySessionActivity.EXTRA_TASK_ID, taskId);
        intent.putExtra(StudySessionActivity.EXTRA_START_TIME, startTime.getTimeInMillis());
        intent.putExtra(StudySessionActivity.EXTRA_END_TIME, endTime.getTimeInMillis());
        context.startActivity(intent);

        dismiss();
    }

}
