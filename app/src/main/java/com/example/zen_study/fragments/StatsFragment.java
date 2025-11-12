package com.example.zen_study.fragments;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.zen_study.R;
import com.example.zen_study.adapters.StatsPagerAdapter;
import com.example.zen_study.viewmodels.StatsViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class StatsFragment extends Fragment implements TabNavigationInterface {

    private StatsViewModel viewModel;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private StatsPagerAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewPager(view);
        setupViewModel();
    }


    private void setupViewPager(View view) {
        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);

        adapter = new StatsPagerAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(4); // Keep all fragments in memory for smooth switching

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Overview");
                    break;
                case 1:
                    tab.setText("Time");
                    break;
                case 2:
                    tab.setText("Subjects");
                    break;
                case 3:
                    tab.setText("Tasks");
                    break;
                case 4:
                    tab.setText("Insights");
                    break;
            }
        }).attach();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(StatsViewModel.class);
    }

    // Public method to switch to specific tab from other fragments\
    @Override
    public void switchToTab(int tabPosition) {
        if (viewPager != null && tabPosition >= 0 && tabPosition < adapter.getItemCount()) {
            viewPager.setCurrentItem(tabPosition, true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load data when fragment becomes visible
        if (viewModel != null) {
            viewModel.loadAllData();
        }
    }

    public TabNavigationInterface getTabNavigationInterface() {
        return this;
    }
}
interface TabNavigationInterface {
    void switchToTab(int tabPosition);
}