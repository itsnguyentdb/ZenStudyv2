package com.example.quiz_clone.dto;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CalendarDay {
    private Date date;
    private boolean isCurrentMonth;
    private boolean isToday;
    private boolean hasEvents;
    private boolean isSelected;

    public CalendarDay(Date date, boolean isCurrentMonth, boolean isToday, boolean hasEvents, boolean isSelected) {
        this.date = date;
        this.isCurrentMonth = isCurrentMonth;
        this.isToday = isToday;
        this.hasEvents = hasEvents;
        this.isSelected = isSelected;
    }

    // Getters and setters
    public Date getDate() { return date; }
    public boolean isCurrentMonth() { return isCurrentMonth; }
    public boolean isToday() { return isToday; }
    public boolean hasEvents() { return hasEvents; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }

    public String getDayNumber() {
        SimpleDateFormat sdf = new SimpleDateFormat("d", Locale.getDefault());
        return sdf.format(date);
    }

    public String getDayName() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());
        return sdf.format(date);
    }
}
