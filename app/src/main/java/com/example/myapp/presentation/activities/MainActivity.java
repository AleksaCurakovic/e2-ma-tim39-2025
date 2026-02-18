package com.example.myapp.presentation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.myapp.R;
import com.example.myapp.domain.services.AuthManager;
import com.example.myapp.presentation.fragments.CategoryFragment;
import com.example.myapp.presentation.fragments.ProfileFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private AuthManager authManager;

    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        authManager = new AuthManager(getApplicationContext());
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                Fragment fragment = null;

                switch (tab.getPosition()) {
                    case 0:
                        //fragment = new HomeFragment();
                        break;

                    case 1:
                        fragment = new ProfileFragment();
                        break;

                    case 2:
                        fragment = new CategoryFragment();
                        break;

                    case 3:
                        //fragment = new TasksFragment();
                        break;
                }

                if (fragment != null) {
                    replaceFragment(fragment);
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}

            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = authManager.getCurrentFirebaseUser();
            if (currentUser == null) {
                startActivity(new Intent(this, RegisterActivity.class));
                finish();
            }
    }

    public void logout(View v) {
        authManager.logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}