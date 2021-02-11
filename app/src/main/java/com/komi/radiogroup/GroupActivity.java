package com.komi.radiogroup;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
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

        group = (Group) getIntent().getParcelableExtra("group");
        listening = getIntent().getBooleanExtra("playing",false);

        ImageButton backBtn = findViewById(R.id.group_back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });

        ImageView groupImageView = findViewById(R.id.group_small_image);
        if(group.getProfilePicturePath() != null){
            Glide.with(this)
                    .load(group.getProfilePicturePath())
                    .into(groupImageView);
        }else{
            //todo: Set default image
        }

        TextView groupNameTV = findViewById(R.id.group_title_text);
        groupNameTV.setText(group.getGroupName());

        tabLayout = (TabLayout) findViewById(R.id.group_tab_layout);
        viewPager = (ViewPager) findViewById(R.id.group_view_pager);

        adapter = new GroupTabAdapter(getSupportFragmentManager(),1);
        adapter.addFragment(new GroupRadioFragment(), "Radio");
        adapter.addFragment(new GroupTextFragment(), "Chat");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

    }

    private void goBack() {
        Intent intent = new Intent(this,MainContainer.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    public void showGroupDetails(View view) {
        Intent intent = new Intent(this,GroupDetails.class);
        intent.putExtra("group",group);
        startActivity(intent);
    }
}
