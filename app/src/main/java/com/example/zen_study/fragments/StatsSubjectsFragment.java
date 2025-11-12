package com.example.zen_study.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.zen_study.R;
import com.example.zen_study.dto.SubjectStats;
import com.example.zen_study.viewmodels.StatsViewModel;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class StatsSubjectsFragment extends Fragment {

    private StatsViewModel viewModel;
    private PieChart pieChartSubjects;
    private ListView listViewSubjects;
    private SubjectAdapter subjectAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats_subjects, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupPieChart();
        setupListView();
        setupViewModel();
        observeData();
    }

    private void initViews(View view) {
        pieChartSubjects = view.findViewById(R.id.pieChartSubjects);
        listViewSubjects = view.findViewById(R.id.listViewSubjects);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(StatsViewModel.class);
    }

    private void observeData() {
        viewModel.getSubjectBreakdown().observe(getViewLifecycleOwner(), this::updateSubjectBreakdown);
    }

    private void setupPieChart() {
        pieChartSubjects.getDescription().setEnabled(false);
        pieChartSubjects.setDrawHoleEnabled(true);
        pieChartSubjects.setHoleRadius(40f);
        pieChartSubjects.setTransparentCircleRadius(45f);
        pieChartSubjects.setEntryLabelColor(Color.BLACK);
        pieChartSubjects.setEntryLabelTextSize(12f);
        pieChartSubjects.setRotationEnabled(true);
        pieChartSubjects.setHighlightPerTapEnabled(true);

        Legend legend = pieChartSubjects.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setXEntrySpace(7f);
        legend.setYEntrySpace(0f);
        legend.setYOffset(0f);
    }

    private void setupListView() {
        subjectAdapter = new SubjectAdapter(requireContext(), new ArrayList<>());
        listViewSubjects.setAdapter(subjectAdapter);

        // Set item click listener
        listViewSubjects.setOnItemClickListener((parent, view, position, id) -> {
            SubjectStats subject = subjectAdapter.getItem(position);
            if (subject != null) {
                showSubjectDetails(subject);
            }
        });
    }

    private void updateSubjectBreakdown(List<SubjectStats> subjectStats) {
        if (subjectStats == null || subjectStats.isEmpty()) {
            setupEmptyPieChart();
            subjectAdapter.updateData(new ArrayList<>());
            return;
        }

        updatePieChart(subjectStats);
        updateSubjectList(subjectStats);
    }

    private void updatePieChart(List<SubjectStats> subjectStats) {
        List<PieEntry> entries = new ArrayList<>();
        int[] colors = generateColors(subjectStats.size());

        for (SubjectStats stats : subjectStats) {
            entries.add(new PieEntry(stats.getPercentage(), stats.getSubjectName()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setValueLinePart1OffsetPercentage(80f);
        dataSet.setValueLinePart1Length(0.5f);
        dataSet.setValueLinePart2Length(0.4f);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueFormatter(new PercentFormatter(pieChartSubjects));

        PieData pieData = new PieData(dataSet);

        pieChartSubjects.setData(pieData);
        pieChartSubjects.invalidate();
        pieChartSubjects.animateY(1000);
    }

    private void updateSubjectList(List<SubjectStats> subjectStats) {
        // Sort by percentage (descending)
        List<SubjectStats> sortedStats = new ArrayList<>(subjectStats);
        sortedStats.sort((s1, s2) -> Float.compare(s2.getPercentage(), s1.getPercentage()));

        subjectAdapter.updateData(sortedStats);
    }

    private int[] generateColors(int count) {
        int[] colors = new int[count];
        float[] hsv = new float[]{0f, 0.7f, 0.9f};

        for (int i = 0; i < count; i++) {
            hsv[0] = (i * 137.5f) % 360; // Golden angle approximation
            colors[i] = Color.HSVToColor(hsv);
        }

        return colors;
    }

    private void setupEmptyPieChart() {
        pieChartSubjects.clear();
        pieChartSubjects.setNoDataText("No subject data available");
        pieChartSubjects.setNoDataTextColor(Color.GRAY);
        pieChartSubjects.invalidate();
    }

    private void showSubjectDetails(SubjectStats subject) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(subject.getSubjectName())
                .setMessage(String.format(Locale.getDefault(),
                        "Total Time: %s\nPercentage: %.1f%%",
                        subject.getStudyTime(), subject.getPercentage()))
                .setPositiveButton("OK", null)
                .show();
    }

    // SubjectAdapter class
    private static class SubjectAdapter extends ArrayAdapter<SubjectStats> {

        public SubjectAdapter(@NonNull Context context, @NonNull List<SubjectStats> subjects) {
            super(context, R.layout.item_stats_subject, subjects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_stats_subject, parent, false);
            }

            SubjectStats subject = getItem(position);

            TextView tvSubjectName = convertView.findViewById(R.id.tvSubjectName);
            TextView tvStudyTime = convertView.findViewById(R.id.tvStudyTime);
            TextView tvPercentage = convertView.findViewById(R.id.tvPercentage);
            View colorView = convertView.findViewById(R.id.colorView);

            if (subject != null) {
                tvSubjectName.setText(subject.getSubjectName());
                tvStudyTime.setText(subject.getStudyTime());
                tvPercentage.setText(String.format(Locale.getDefault(), "%.1f%%", subject.getPercentage()));

                // Set color based on subject name hash
                int color = getColorForSubject(subject.getSubjectName());
                colorView.setBackgroundColor(color);
            }

            return convertView;
        }

        private int getColorForSubject(String subjectName) {
            int hash = subjectName.hashCode();
            Random random = new Random(hash);
            float hue = random.nextFloat() * 360;
            float[] hsv = new float[]{hue, 0.7f, 0.9f};
            return Color.HSVToColor(hsv);
        }

        public void updateData(List<SubjectStats> newData) {
            clear();
            addAll(newData);
            notifyDataSetChanged();
        }
    }
}
