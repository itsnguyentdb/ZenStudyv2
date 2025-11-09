package com.example.quiz_clone.dto;

import com.example.quiz_clone.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CalendarEvent {
    private String title;
    private EventType type;
    private int color;
    private long timeInMillis;
    private String description;
    private String location;

    public enum EventType {
        MEETING("Meeting", R.drawable.ic_meeting),
        APPOINTMENT("Appointment", R.drawable.ic_appointment),
        PERSONAL("Personal", R.drawable.ic_personal),
        BIRTHDAY("Birthday", R.drawable.ic_birthday),
        REMINDER("Reminder", R.drawable.ic_reminder),
        WORK("Work", R.drawable.ic_work),
        HEALTH("Health", R.drawable.ic_health);

        private String displayName;
        private int iconRes;

        EventType(String displayName, int iconRes) {
            this.displayName = displayName;
            this.iconRes = iconRes;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getIconRes() {
            return iconRes;
        }
    }

    public CalendarEvent(String title, EventType type, int color, long timeInMillis) {
        this.title = title;
        this.type = type;
        this.color = color;
        this.timeInMillis = timeInMillis;
        this.description = "";
        this.location = "";
    }

    public CalendarEvent(String title, EventType type, int color, long timeInMillis, String description, String location) {
        this.title = title;
        this.type = type;
        this.color = color;
        this.timeInMillis = timeInMillis;
        this.description = description;
        this.location = location;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public EventType getType() {
        return type;
    }

    public int getColor() {
        return color;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setTimeInMillis(long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // Helper methods
    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timeInMillis));
    }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
        return sdf.format(new Date(timeInMillis));
    }

    public String getDuration() {
        // Default 1 hour duration, you can modify this based on your needs
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        long endTime = timeInMillis + (60 * 60 * 1000); // +1 hour
        return sdf.format(new Date(timeInMillis)) + " - " + sdf.format(new Date(endTime));
    }

    public boolean isAllDay() {
        // Check if event is all day (starts at 00:00 and ends at 23:59)
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeInMillis);
        return cal.get(Calendar.HOUR_OF_DAY) == 0 &&
                cal.get(Calendar.MINUTE) == 0;
    }
}