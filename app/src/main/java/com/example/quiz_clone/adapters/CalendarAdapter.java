package com.example.quiz_clone.adapters;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import com.example.quiz_clone.R;
import com.example.quiz_clone.dto.CalendarDay;

import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayViewHolder> {

    private List<CalendarDay> days;
    private OnDayClickListener dayClickListener;
    private Typeface customFont;

    // Customizable styles
    private int defaultDayBackground = Color.WHITE;
    private int selectedDayBackground = Color.parseColor("#2196F3");
    private int eventDayBackground = Color.parseColor("#E8F5E8");
    private int todayBackground = Color.parseColor("#FF4081");
    private int currentMonthTextColor = Color.BLACK;
    private int otherMonthTextColor = Color.GRAY;
    private int selectedTextColor = Color.WHITE;
    private int todayTextColor = Color.WHITE;
    private int dayBorderColor = Color.parseColor("#E0E0E0");
    private int dayBorderWidth = 2; // dp

    public interface OnDayClickListener {
        void onDayClick(CalendarDay day);
    }

    public CalendarAdapter(List<CalendarDay> days, OnDayClickListener listener) {
        this.days = days;
        this.dayClickListener = listener;
    }

    public void updateDays(List<CalendarDay> newDays) {
        this.days = newDays;
        notifyDataSetChanged();
    }

    public void setCustomFont(Typeface font) {
        this.customFont = font;
        notifyDataSetChanged();
    }

    // Style setters
    public void setDefaultDayBackground(int color) {
        this.defaultDayBackground = color;
        notifyDataSetChanged();
    }

    public void setSelectedDayBackground(int color) {
        this.selectedDayBackground = color;
        notifyDataSetChanged();
    }

    public void setEventDayBackground(int color) {
        this.eventDayBackground = color;
        notifyDataSetChanged();
    }

    public void setTodayBackground(int color) {
        this.todayBackground = color;
        notifyDataSetChanged();
    }

    public void setDayBorderColor(int color) {
        this.dayBorderColor = color;
        notifyDataSetChanged();
    }

    public void setDayBorderWidth(int widthDp) {
        this.dayBorderWidth = widthDp;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        var view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        CalendarDay day = days.get(position);
        holder.bind(day);
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    class DayViewHolder extends RecyclerView.ViewHolder {
        private TextView dayNumber;
        private TextView dayName;
        private ImageView eventIndicator;
        private CardView dayCard;
        private View borderView;

        DayViewHolder(View itemView) {
            super(itemView);
            dayNumber = itemView.findViewById( R.id.day_number);
            dayName = itemView.findViewById(R.id.day_name);
            eventIndicator = itemView.findViewById(R.id.event_indicator);
            dayCard = itemView.findViewById(R.id.day_card);
            borderView = itemView.findViewById(R.id.border_view);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && dayClickListener != null) {
                    dayClickListener.onDayClick(days.get(position));
                }
            });
        }

        void bind(CalendarDay day) {
            dayNumber.setText(day.getDayNumber());
            dayName.setText(day.getDayName());

            // Apply custom font if set
            if (customFont != null) {
                dayNumber.setTypeface(customFont);
                dayName.setTypeface(customFont);
            }

            // Setup border
            setupBorder();

            // Determine background and text colors based on day state
            int backgroundColor = getBackgroundColor(day);
            int textColor = getTextColor(day);

            dayCard.setCardBackgroundColor(backgroundColor);
            dayNumber.setTextColor(textColor);
            dayName.setTextColor(textColor);

            // Show event indicator if day has events
            eventIndicator.setVisibility(day.hasEvents() ? View.VISIBLE : View.GONE);

            // Additional styling for today
            if (day.isToday()) {
                dayNumber.setTypeface(Typeface.DEFAULT_BOLD);
                dayName.setTypeface(Typeface.DEFAULT_BOLD);
            } else {
                dayNumber.setTypeface(Typeface.DEFAULT);
                dayName.setTypeface(Typeface.DEFAULT);
            }

            // Scale animation for selected day
            if (day.isSelected()) {
                dayCard.animate().scaleX(1.1f).scaleY(1.1f).setDuration(200).start();
            } else {
                dayCard.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
            }
        }

        private void setupBorder() {
            GradientDrawable borderDrawable = new GradientDrawable();
            borderDrawable.setShape(GradientDrawable.RECTANGLE);
            borderDrawable.setStroke(dpToPx(dayBorderWidth), dayBorderColor);
            borderDrawable.setColor(Color.TRANSPARENT);
            borderView.setBackground(borderDrawable);
        }

        private int getBackgroundColor(CalendarDay day) {
            if (day.isSelected()) {
                return selectedDayBackground;
            } else if (day.isToday()) {
                return todayBackground;
            } else if (day.hasEvents() && day.isCurrentMonth()) {
                return eventDayBackground;
            } else if (day.isCurrentMonth()) {
                return defaultDayBackground;
            } else {
                return Color.TRANSPARENT;
            }
        }

        private int getTextColor(CalendarDay day) {
            if (day.isSelected()) {
                return selectedTextColor;
            } else if (day.isToday()) {
                return todayTextColor;
            } else if (day.isCurrentMonth()) {
                return currentMonthTextColor;
            } else {
                return otherMonthTextColor;
            }
        }

        private int dpToPx(int dp) {
            return (int) (dp * itemView.getContext().getResources().getDisplayMetrics().density);
        }
    }
}
