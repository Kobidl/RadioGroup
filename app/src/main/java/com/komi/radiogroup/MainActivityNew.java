package com.komi.radiogroup;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.komi.radiogroup.pages.Explore;
import com.komi.radiogroup.pages.Groups;
import com.komi.radiogroup.pages.Profile;
import com.komi.radiogroup.pages.Welcome;

public class MainActivityNew extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    Profile profileFragment = new Profile();
    Groups groupsFragment = new Groups();
    Explore exploreFragment = new Explore();
    Welcome welcomeFragment = new Welcome();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setSelectedItemId(R.id.bottom_navigation_item_profile);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Toast.makeText(MainActivityNew.this, "selected:" + item.getTitle(), Toast.LENGTH_SHORT).show();
                switch (item.getItemId()){
                    case R.id.bottom_navigation_item_profile:
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, profileFragment).commit();
                        return true;
                    case R.id.bottom_navigation_item_explore:
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, exploreFragment).commit();
                        return true;
                    case R.id.bottom_navigation_item_group:
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, groupsFragment).commit();
                        return true;
                }
                return false;
            }
        });
    }

}