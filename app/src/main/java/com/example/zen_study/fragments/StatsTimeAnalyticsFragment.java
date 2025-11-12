package com.example.zen_study.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.zen_study.R;
import com.example.zen_study.dto.DailyStudyTime;
import com.example.zen_study.dto.WeeklyAverage;
import com.example.zen_study.enums.TimePeriod;
import com.example.zen_study.viewmodels.StatsViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatsTimeAnalyticsFragment extends Fragment {

    private StatsViewModel viewModel;
    private BarChart barChartStudyTime;
    private LineChart lineChartWeeklyTrend;
    private ChipGroup chipGroupTimePeriod;
    private Chip chipWeek, chipMonth, chipYear;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats_time_analytics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewModel();
        initViews(view);
        setupCharts();
        setupTimePeriodFilter();
        observeData();
    }

    private void initViews(View view) {
        barChartStudyTime = view.findViewById(R.id.barChartStudyTime);
        lineChartWeeklyTrend = view.findViewById(R.id.lineChartWeeklyTrend);
        chipGroupTimePeriod = view.findViewById(R.id.chipGroupTimePeriod);
        chipWeek = view.findViewById(R.id.chipWeek);
        chipMonth = view.findViewById(R.id.chipMonth);
        chipYear = view.findViewById(R.id.chipYear);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(StatsViewModel.class);
    }

    private void observeData() {
        viewModel.getDailyStudyTime().observe(getViewLifecycleOwner(), this::updateBarChart);
        viewModel.getWeeklyTrend().observe(getViewLifecycleOwner(), this::updateLineChart);
    }

    private void setupTimePeriodFilter() {
        chipGroupTimePeriod.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int checkedId = checkedIds.get(0);
            TimePeriod timePeriod = TimePeriod.WEEK;

            if (checkedId == R.id.chipWeek) {
                timePeriod = TimePeriod.WEEK;
            } else if (checkedId == R.id.chipMonth) {
                timePeriod = TimePeriod.MONTH;
            } else if (checkedId == R.id.chipYear) {
                timePeriod = TimePeriod.YEAR;
            }

            viewModel.setTimePeriod(timePeriod);
        });

        // Default selection
        chipWeek.setChecked(true);
    }

    private void setupCharts() {
        setupBarChart();
        setupLineChart();
    }

    private void setupBarChart() {
        barChartStudyTime.getDescription().setEnabled(false);
        barChartStudyTime.getLegend().setEnabled(false);
        barChartStudyTime.setDrawGridBackground(false);
        barChartStudyTime.setDrawBorders(false);
        barChartStudyTime.setTouchEnabled(true);
        barChartStudyTime.setDragEnabled(true);
        barChartStudyTime.setScaleEnabled(true);
        barChartStudyTime.setPinchZoom(true);

        XAxis xAxis = barChartStudyTime.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7, true);

        YAxis yAxis = barChartStudyTime.getAxisLeft();
        yAxis.setDrawGridLines(true);
        yAxis.setAxisMinimum(0f);
        yAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.0fm", value);
            }
        });
        barChartStudyTime.getAxisRight().setEnabled(false);
    }

    private void setupLineChart() {
        lineChartWeeklyTrend.getDescription().setEnabled(false);
        lineChartWeeklyTrend.getLegend().setEnabled(false);
        lineChartWeeklyTrend.setDrawGridBackground(false);
        lineChartWeeklyTrend.setDrawBorders(false);
        lineChartWeeklyTrend.setTouchEnabled(true);
        lineChartWeeklyTrend.setDragEnabled(true);
        lineChartWeeklyTrend.setScaleEnabled(true);
        lineChartWeeklyTrend.setPinchZoom(true);

        XAxis xAxis = lineChartWeeklyTrend.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        YAxis yAxis = lineChartWeeklyTrend.getAxisLeft();
        yAxis.setDrawGridLines(true);
        yAxis.setAxisMinimum(0f);
        yAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.1fh", value);
            }
        });
        lineChartWeeklyTrend.getAxisRight().setEnabled(false);
    }

    private void updateBarChart(List<DailyStudyTime> dailyData) {
        if (dailyData == null || dailyData.isEmpty()) {
            setupEmptyBarChart();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < dailyData.size(); i++) {
            DailyStudyTime data = dailyData.get(i);
            entries.add(new BarEntry(i, data.getMinutes()));
            labels.add(data.getDayLabel());
        }

        BarDataSet dataSet = new BarDataSet(entries, "Study Minutes");
        dataSet.setColor(Color.parseColor("#2196F3"));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry barEntry) {
                return String.format(Locale.getDefault(), "%.0fm", barEntry.getY());
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        barChartStudyTime.setData(barData);

        // Set X-axis labels
        XAxis xAxis = barChartStudyTime.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(labels.size());

        barChartStudyTime.invalidate();
        barChartStudyTime.animateY(1000);
    }

    private void updateLineChart(List<WeeklyAverage> weeklyData) {
        if (weeklyData == null || weeklyData.isEmpty()) {
            setupEmptyLineChart();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < weeklyData.size(); i++) {
            WeeklyAverage data = weeklyData.get(i);
            entries.add(new Entry(i, data.getAverageHours()));
            labels.add(data.getWeekLabel());
        }

        LineDataSet dataSet = new LineDataSet(entries, "Weekly Average Hours");
        dataSet.setColor(Color.parseColor("#FF9800"));
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleColor(Color.parseColor("#FF9800"));
        dataSet.setCircleRadius(5f);
        dataSet.setFillColor(Color.parseColor("#FF9800"));
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPointLabel(Entry entry) {
                return String.format(Locale.getDefault(), "%.1fh", entry.getY());
            }
        });

        LineData lineData = new LineData(dataSet);
        lineChartWeeklyTrend.setData(lineData);

        // Set X-axis labels
        XAxis xAxis = lineChartWeeklyTrend.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(Math.min(labels.size(), 6));

        lineChartWeeklyTrend.invalidate();
        lineChartWeeklyTrend.animateY(1000);
    }

    private void setupEmptyBarChart() {
        barChartStudyTime.clear();
        barChartStudyTime.setNoDataText("No study data available for selected period");
        barChartStudyTime.setNoDataTextColor(Color.GRAY);
        barChartStudyTime.invalidate();
    }

    private void setupEmptyLineChart() {
        lineChartWeeklyTrend.clear();
        lineChartWeeklyTrend.setNoDataText("No trend data available for selected period");
        lineChartWeeklyTrend.setNoDataTextColor(Color.GRAY);
        lineChartWeeklyTrend.invalidate();
    }
}
