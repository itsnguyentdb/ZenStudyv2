package com.example.quiz_clone.utils;

import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.TimePicker;
import java.util.Calendar;
import java.util.Locale;

public class TimePickerUtils {

    public interface TimePickerListener {
        void onTimeSelected(int hour, int minute);
    }

    public static void showTimePicker(Context context, TimePickerListener listener) {
        showTimePicker(context, -1, -1, listener);
    }

    public static void showTimePicker(Context context, int initialHour, int initialMinute, TimePickerListener listener) {
        final Calendar calendar = Calendar.getInstance();
        int hour = initialHour != -1 ? initialHour : calendar.get(Calendar.HOUR_OF_DAY);
        int minute = initialMinute != -1 ? initialMinute : calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                context,
                (view, selectedHour, selectedMinute) -> {
                    if (listener != null) {
                        listener.onTimeSelected(selectedHour, selectedMinute);
                    }
                },
                hour,
                minute,
                false // 24-hour format
        );

        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();
    }

    public static String formatTime(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
    }

    public static String formatTimeWithAmPm(int hour, int minute) {
        String amPm = hour < 12 ? "AM" : "PM";
        int displayHour = hour % 12;
        if (displayHour == 0) displayHour = 12;

        return String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minute, amPm);
    }
}