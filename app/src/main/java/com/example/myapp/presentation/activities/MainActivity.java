package com.example.myapp.presentation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapp.R;
import com.example.myapp.data.repositories.BossRepository;
import com.example.myapp.domain.models.Boss;
import com.example.myapp.presentation.fragments.BossFragment;
import com.example.myapp.presentation.fragments.CalendarFragment;
import com.example.myapp.presentation.fragments.CategoryFragment;
import com.example.myapp.presentation.fragments.CreateTaskFragment;
import com.example.myapp.presentation.fragments.EditTaskFragment;
import com.example.myapp.presentation.fragments.LevelFragment;
import com.example.myapp.presentation.fragments.ProfileFragment;
import com.example.myapp.presentation.fragments.TaskDetailFragment;
import com.example.myapp.presentation.fragments.TaskListFragment;
import com.example.myapp.presentation.fragments.UserStatsFragment;
import com.example.myapp.presentation.viewmodels.TaskViewModel;
import com.example.myapp.presentation.viewmodels.TaskViewModelFactory;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private FloatingActionButton fab;
    private Toolbar              toolbar;
    private TaskViewModel        taskViewModel;

    private TaskListFragment  taskListFragment;
    private CalendarFragment  calendarFragment;
    private ProfileFragment   profileFragment;
    private UserStatsFragment userStatsFragment;
    private BossFragment      bossFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        setContentView(R.layout.activity_main);

        toolbar   = findViewById(R.id.toolbar);
        bottomNav = findViewById(R.id.bottomNavigation);
        fab       = findViewById(R.id.fab);
        setSupportActionBar(toolbar);

        taskListFragment  = new TaskListFragment();
        calendarFragment  = new CalendarFragment();
        profileFragment   = new ProfileFragment();
        userStatsFragment = new UserStatsFragment();
        bossFragment      = new BossFragment();

        taskViewModel = new ViewModelProvider(this,
                new TaskViewModelFactory(this)).get(TaskViewModel.class);

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Level up → pokaži boss tab i navigiraj na bosa
        taskViewModel.levelUpOccurred.observe(this, result -> {
            if (result != null && result.leveledUp) {
                updateBossTab(true);
                bottomNav.setSelectedItemId(R.id.nav_boss);
            }
        });

        setupBottomNav();
        setupFab();

        if (savedInstanceState == null) {
            // Pokaži boss tab odmah ako postoje neporaženi bosovi iz prethodnih sesija
            checkAndShowBossTab();
            showMainFragment(taskListFragment, "Zadaci");
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                    bottomNav.setVisibility(View.VISIBLE);
                    int selectedId = bottomNav.getSelectedItemId();
                    if (selectedId == R.id.nav_tasks || selectedId == R.id.nav_calendar)
                        showFab(true);
                    if (getSupportActionBar() != null)
                        getSupportActionBar().setTitle(getTitleForSelectedNav(selectedId));
                } else {
                    finish();
                }
            }
        });
    }

    // ─────────────────────────────────────────
    // BOSS TAB
    // ─────────────────────────────────────────

    public void updateBossTab(boolean hasBosses) {
        bottomNav.getMenu().findItem(R.id.nav_boss).setVisible(hasBosses);
    }

    private void checkAndShowBossTab() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) return;
        Boss boss = new BossRepository(this).getNextBoss(firebaseUser.getUid());
        updateBossTab(boss != null);
    }

    // ─────────────────────────────────────────
    // BOTTOM NAV
    // ─────────────────────────────────────────

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_tasks) {
                showMainFragment(taskListFragment, "Zadaci");
                showFab(true);
                return true;
            } else if (id == R.id.nav_calendar) {
                showMainFragment(calendarFragment, "Kalendar");
                showFab(true);
                return true;
            } else if (id == R.id.nav_profile) {
                showMainFragment(profileFragment, "Profil");
                showFab(false);
                return true;
            } else if (id == R.id.nav_stats) {
                showMainFragment(userStatsFragment, "Statistike");
                showFab(false);
                return true;
            } else if (id == R.id.nav_boss) {
                showMainFragment(bossFragment, "Borba sa bosom");
                showFab(false);
                return true;
            }
            return false;
        });
    }

    // ─────────────────────────────────────────
    // FAB
    // ─────────────────────────────────────────

    private void setupFab() {
        fab.setOnClickListener(v -> navigateTo(new CreateTaskFragment()));
    }

    private void showFab(boolean show) {
        fab.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    // ─────────────────────────────────────────
    // NAVIGACIJA
    // ─────────────────────────────────────────

    private void showMainFragment(Fragment fragment, String title) {
        getSupportFragmentManager().popBackStack(
                null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(title);
    }

    public void navigateTo(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(getTitleForFragment(fragment));
        showFab(false);
        bottomNav.setVisibility(View.GONE);
    }

    private String getTitleForFragment(Fragment fragment) {
        if (fragment instanceof CreateTaskFragment) return "Novi zadatak";
        if (fragment instanceof EditTaskFragment)   return "Izmeni zadatak";
        if (fragment instanceof TaskDetailFragment) return "Detalji zadatka";
        if (fragment instanceof CategoryFragment)   return "Kategorije";
        if (fragment instanceof LevelFragment)      return "Nivoi";
        if (fragment instanceof BossFragment)       return "Borba sa bosom";
        return "";
    }

    private String getTitleForSelectedNav(int selectedId) {
        if (selectedId == R.id.nav_tasks)    return "Zadaci";
        if (selectedId == R.id.nav_calendar) return "Kalendar";
        if (selectedId == R.id.nav_profile)  return "Profil";
        if (selectedId == R.id.nav_stats)    return "Statistike";
        if (selectedId == R.id.nav_boss)     return "Borba sa bosom";
        return "";
    }

}