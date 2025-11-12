package com.example.zen_study.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.zen_study.fragments.StatsInsightsFragment;
import com.example.zen_study.fragments.StatsOverviewFragment;
import com.example.zen_study.fragments.StatsSubjectsFragment;
import com.example.zen_study.fragments.StatsTasksFragment;
import com.example.zen_study.fragments.StatsTimeAnalyticsFragment;

public class StatsPagerAdapter extends FragmentStateAdapter {

    private static final int TAB_COUNT = 5;

    public StatsPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return new StatsTimeAnalyticsFragment();
            case 2:
                return new StatsSubjectsFragment();
            case 3:
                return new StatsTasksFragment();
            case 4:
                return new StatsInsightsFragment();
            default:
                return new StatsOverviewFragment();
        }
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
}
