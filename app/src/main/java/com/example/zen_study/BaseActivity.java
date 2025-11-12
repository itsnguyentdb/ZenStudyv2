package com.example.zen_study;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zen_study.fragments.FlashcardLibraryFragment;
import com.example.zen_study.fragments.QuizLibraryFragment;
import com.example.zen_study.fragments.ResourceLibraryFragment;
import com.example.zen_study.fragments.SettingsFragment;
import com.example.zen_study.fragments.StatsFragment;
import com.example.zen_study.adapters.BottomNavigationSubMenuAdapter;
import com.example.zen_study.fragments.CalendarFragment;
import com.example.zen_study.fragments.HomeFragment;
import com.example.zen_study.fragments.TaskLibraryFragment;
import com.example.zen_study.fragments.SubjectLibraryFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends AppCompatActivity implements BottomNavigationSubMenuAdapter.OnSubMenuItemClickListener {
    protected BottomNavigationView bottomNavigationView;
    private View subMenuView;
    private RecyclerView subMenuRecyclerView;
    private boolean isSubMenuVisible = false;

    private List<BottomNavigationSubMenuAdapter.SubMenuItem> subMenuItems;
    private BottomNavigationSubMenuAdapter bottomNavigationSubMenuAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_base);

        setupBottomNavigation();
        setupSubMenuItems();
        setupSubMenu();
        setupOnBackPressed();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private void setupSubMenuItems() {
        subMenuItems = new ArrayList<>();
        subMenuItems.add(new BottomNavigationSubMenuAdapter.SubMenuItem("Tasks", R.drawable.ic_task_selector, TaskLibraryFragment.class));
        subMenuItems.add(new BottomNavigationSubMenuAdapter.SubMenuItem("Subjects", R.drawable.ic_info_selector, SubjectLibraryFragment.class));
        subMenuItems.add(new BottomNavigationSubMenuAdapter.SubMenuItem("Flashcards", R.drawable.ic_flashcard_selector, FlashcardLibraryFragment.class));
        subMenuItems.add(new BottomNavigationSubMenuAdapter.SubMenuItem("Quizzes", R.drawable.ic_quiz_selector, QuizLibraryFragment.class));
        subMenuItems.add(new BottomNavigationSubMenuAdapter.SubMenuItem("Resources", R.drawable.ic_flashcard_selector, ResourceLibraryFragment.class));
    }

    private void setupSubMenu() {
        subMenuView = LayoutInflater.from(this).inflate(R.layout.layout_bottom_nav_submenu, null);
        subMenuRecyclerView = subMenuView.findViewById(R.id.recycler_submenu);
        var gridLayoutManager = new GridLayoutManager(this, 4);
        subMenuRecyclerView.setLayoutManager(gridLayoutManager);
        bottomNavigationSubMenuAdapter = new BottomNavigationSubMenuAdapter(subMenuItems, this);
        subMenuRecyclerView.setAdapter(bottomNavigationSubMenuAdapter);
        FrameLayout overlayContainer = findViewById(R.id.submenu_overlay_container);

        overlayContainer.post(() -> {
            int bottomNavHeight = bottomNavigationView.getHeight();

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            params.bottomMargin = bottomNavHeight;

            subMenuView.setLayoutParams(params);
        });

        overlayContainer.addView(subMenuView);
        subMenuView.setVisibility(View.GONE);

        subMenuView.setElevation(16f);
        overlayContainer.setElevation(15f);
    }


    private void setupOnBackPressed() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isSubMenuVisible) {
                    hideSubMenu();
                    return;
                }
                if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                    getSupportFragmentManager().popBackStack();
                    return;
                }
                finish();
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (item.getItemId() == R.id.navigation_calendar) {
                loadFragment(new CalendarFragment());
                return true;
            } else if (item.getItemId() == R.id.navigation_study) {
                if (isSubMenuVisible) {
                    hideSubMenu();
                } else {
                    showSubMenu();
                }
                return true;
            } else if (item.getItemId() == R.id.navigation_stats) {
                loadFragment(new StatsFragment());
                return true;
            } else if (item.getItemId() == R.id.navigation_settings) {
                loadFragment(new SettingsFragment());
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
        hideSubMenu();
        System.out.println(isSubMenuVisible);
    }

    private void showSubMenu() {
        if (!isSubMenuVisible) {
            subMenuView.setVisibility(View.VISIBLE);
            isSubMenuVisible = true;
        }
    }

    private void hideSubMenu() {
        if (isSubMenuVisible) {
            subMenuView.setVisibility(View.GONE);
            isSubMenuVisible = false;
        }
    }


    @Override
    public void onSubMenuItemClick(BottomNavigationSubMenuAdapter.SubMenuItem item) {
        try {
            var fragment = item.getFragmentClass().newInstance();
            loadFragment(fragment);
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }
}