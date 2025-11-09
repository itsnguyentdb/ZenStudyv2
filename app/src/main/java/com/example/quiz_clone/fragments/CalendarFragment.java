package com.example.quiz_clone.fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.quiz_clone.R;
import com.example.quiz_clone.adapters.CalendarAdapter;
import com.example.quiz_clone.adapters.EventsAdapter;
import com.example.quiz_clone.decorations.GridSpacingItemDecoration;
import com.example.quiz_clone.dto.CalendarDay;
import com.example.quiz_clone.dto.CalendarEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class CalendarFragment extends Fragment {
    private RecyclerView calendarRecyclerView;
    private TextView monthYearText;
    private Button prevMonthBtn, nextMonthBtn;
    private RecyclerView eventsRecyclerView;

    private Calendar currentDate = Calendar.getInstance();
    private CalendarAdapter calendarAdapter;
    private EventsAdapter eventsAdapter;
    private Map<Long, List<CalendarEvent>> eventsMap = new HashMap<>();
    private List<CalendarDay> currentMonthDays = new ArrayList<>();

    // Customization options
    private int defaultDayBackground = Color.WHITE;
    private int selectedDayBackground = Color.parseColor("#2196F3");
    private int eventDayBackground = Color.parseColor("#E8F5E8");
    private int todayBackground = Color.parseColor("#FF4081");
    private int dayBorderColor = Color.parseColor("#E0E0E0");
    private int dayBorderWidth = 2; // dp

    public CalendarFragment() {
    }

    public static CalendarFragment newInstance(String param1, String param2) {
        CalendarFragment fragment = new CalendarFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupCalendar();
        loadSampleEvents();
        updateCalendar();
    }
    private void initViews(View view) {
        calendarRecyclerView = view.findViewById(R.id.calendar_recycler_view);
        monthYearText = view.findViewById(R.id.month_year_text);
        prevMonthBtn = view.findViewById(R.id.prev_month_btn);
        nextMonthBtn = view.findViewById(R.id.next_month_btn);
        eventsRecyclerView = view.findViewById(R.id.events_recycler_view);

        // Setup calendar grid
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.addItemDecoration(new GridSpacingItemDecoration(7, 1, true));

        // Setup events list
        eventsAdapter = new EventsAdapter(new ArrayList<>());
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        eventsRecyclerView.setAdapter(eventsAdapter);
    }

    private void setupCalendar() {
        calendarAdapter = new CalendarAdapter(currentMonthDays, new CalendarAdapter.OnDayClickListener() {
            @Override
            public void onDayClick(CalendarDay day) {
                handleDaySelection(day);
                showEventsForDate(day.getDate().getTime());
            }
        });
        calendarRecyclerView.setAdapter(calendarAdapter);

        prevMonthBtn.setOnClickListener(v -> {
            currentDate.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        nextMonthBtn.setOnClickListener(v -> {
            currentDate.add(Calendar.MONTH, 1);
            updateCalendar();
        });
    }

    private void updateCalendar() {
        updateMonthYearHeader();
        generateMonthDays();
        calendarAdapter.updateDays(currentMonthDays);
    }

    private void updateMonthYearHeader() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        monthYearText.setText(sdf.format(currentDate.getTime()));
    }

    private void generateMonthDays() {
        currentMonthDays.clear();

        Calendar calendar = (Calendar) currentDate.clone();
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Add previous month's days
        Calendar prevMonth = (Calendar) calendar.clone();
        prevMonth.add(Calendar.MONTH, -1);
        int prevMonthDays = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = firstDayOfWeek - 2; i >= 0; i--) {
            Date date = getDateForDay(prevMonth, prevMonthDays - i);
            currentMonthDays.add(new CalendarDay(date, false, false, false, false));
        }

        // Add current month's days
        Calendar today = Calendar.getInstance();
        for (int i = 1; i <= daysInMonth; i++) {
            Date date = getDateForDay(calendar, i);
            boolean isToday = isSameDay(date, today.getTime());
            boolean hasEvents = hasEvents(date);
            currentMonthDays.add(new CalendarDay(date, true, isToday, hasEvents, false));
        }

        // Add next month's days to complete grid
        int totalCells = 42; // 6 weeks * 7 days
        int remainingDays = totalCells - currentMonthDays.size();
        Calendar nextMonth = (Calendar) calendar.clone();
        nextMonth.add(Calendar.MONTH, 1);

        for (int i = 1; i <= remainingDays; i++) {
            Date date = getDateForDay(nextMonth, i);
            currentMonthDays.add(new CalendarDay(date, false, false, false, false));
        }
    }

    private Date getDateForDay(Calendar calendar, int day) {
        Calendar cal = (Calendar) calendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, day);
        return cal.getTime();
    }

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    private boolean hasEvents(Date date) {
        long timestamp = getDayStartTimestamp(date);
        return eventsMap.containsKey(timestamp) && !eventsMap.get(timestamp).isEmpty();
    }

    private void handleDaySelection(CalendarDay selectedDay) {
        // Deselect all days
        for (CalendarDay day : currentMonthDays) {
            day.setSelected(false);
        }

        // Select clicked day
        selectedDay.setSelected(true);
        calendarAdapter.updateDays(currentMonthDays);
    }

    private void showEventsForDate(long timestamp) {
        List<CalendarEvent> events = eventsMap.get(getDayStartTimestamp(new Date(timestamp)));

        if (events != null && !events.isEmpty()) {
            eventsAdapter.updateEvents(events);
        } else {
            eventsAdapter.updateEvents(new ArrayList<>());
        }
    }

    private long getDayStartTimestamp(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private void loadSampleEvents() {
        Calendar calendar = Calendar.getInstance();

        // Add events for various days
        addEvent(calendar, "Team Meeting", CalendarEvent.EventType.MEETING, Color.BLUE);
        addEvent(calendar, "Lunch with Client", CalendarEvent.EventType.APPOINTMENT, Color.GREEN);

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        addEvent(calendar, "Doctor Appointment", CalendarEvent.EventType.APPOINTMENT, Color.RED);

        calendar.add(Calendar.DAY_OF_MONTH, 3);
        addEvent(calendar, "Birthday Party", CalendarEvent.EventType.PERSONAL, Color.MAGENTA);

        calendar.add(Calendar.DAY_OF_MONTH, 2);
        addEvent(calendar, "Project Deadline", CalendarEvent.EventType.MEETING, Color.RED);

        // Add random events
        Random random = new Random();
        for (int i = 0; i < 15; i++) {
            calendar.add(Calendar.DAY_OF_MONTH, random.nextInt(10) + 1);
            addEvent(calendar, "Event " + (i + 1),
                    CalendarEvent.EventType.values()[random.nextInt(CalendarEvent.EventType.values().length)],
                    Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
        }
    }

    private void addEvent(Calendar calendar, String title, CalendarEvent.EventType type, int color) {
        long dayTimestamp = getDayStartTimestamp(calendar.getTime());

        CalendarEvent event = new CalendarEvent(title, type, color, calendar.getTimeInMillis());

        if (!eventsMap.containsKey(dayTimestamp)) {
            eventsMap.put(dayTimestamp, new ArrayList<>());
        }
        eventsMap.get(dayTimestamp).add(event);
    }

    // Customization methods
    public void setDefaultDayBackground(int color) {
        this.defaultDayBackground = color;
        calendarAdapter.notifyDataSetChanged();
    }

    public void setSelectedDayBackground(int color) {
        this.selectedDayBackground = color;
        calendarAdapter.notifyDataSetChanged();
    }

    public void setEventDayBackground(int color) {
        this.eventDayBackground = color;
        calendarAdapter.notifyDataSetChanged();
    }

    public void setTodayBackground(int color) {
        this.todayBackground = color;
        calendarAdapter.notifyDataSetChanged();
    }

    public void setDayBorderColor(int color) {
        this.dayBorderColor = color;
        calendarAdapter.notifyDataSetChanged();
    }

    public void setDayBorderWidth(int widthDp) {
        this.dayBorderWidth = widthDp;
        calendarAdapter.notifyDataSetChanged();
    }
}