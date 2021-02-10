package com.komi.radiogroup;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.komi.radiogroup.pages.GroupRadioFragment;
import com.komi.radiogroup.pages.GroupTextFragment;
import com.komi.structures.Group;

public class GroupActivity extends AppCompatActivity {

    public static boolean listening = false;
    public static Group group;
    GroupRadioFragment groupRadioFragment = new GroupRadioFragment();
    GroupTextFragment groupTextFragment = new GroupTextFragment();
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private GroupTabAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_activity_layout);

        group = (Group) getIntent().getSerializableExtra("group");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(group.getGroupName());
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24);

        tabLayout = (TabLayout) findViewById(R.id.group_tab_layout);
        viewPager = (ViewPager) findViewById(R.id.group_view_pager);

        adapter = new GroupTabAdapter(getSupportFragmentManager(),1);
        adapter.addFragment(new GroupRadioFragment(), "Radio");
        adapter.addFragment(new GroupTextFragment(), "Chat");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        //groupTextFragment.setGroup(group);
        //getSupportFragmentManager().beginTransaction().replace(R.id.group_frame_layout, groupTextFragment).commit();

//        getSupportFragmentManager().beginTransaction().replace(R.id.group_frame_layout, groupRadioFragment).commit();
    }

    /* If back button pressed on toolbar */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
