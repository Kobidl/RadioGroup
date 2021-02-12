package com.komi.radiogroup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.komi.radiogroup.pages.JoinGroupFragment;
import com.komi.structures.Group;

import static com.komi.radiogroup.pages.Profile.SHARED_PREFS;
import static com.komi.radiogroup.pages.Profile.SP_UID;

public class GroupActivity extends AppCompatActivity implements JoinGroupFragment.JoinGroupCallback {

    public static boolean listening = false;
    public static Group group;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private GroupTabAdapter adapter;
    String userId;
    private boolean isMember;
    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_activity_layout);

        group = (Group) getIntent().getParcelableExtra("group");
        listening = getIntent().getBooleanExtra("playing",false);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        userId = sharedPreferences.getString(SP_UID, null);

        isMember = group.getUserMap().containsKey(userId);
        isAdmin = group.getAdminID().equals(userId);

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
        if(isMember) {
            adapter.addFragment(new GroupRadioFragment(), "Radio");
            adapter.addFragment(new GroupTextFragment(), "Chat");
        }else{
            adapter.addFragment(new JoinGroupFragment(),"Details");
            tabLayout.setVisibility(View.GONE);
        }
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

    @Override
    public void onJoinedGroup(Group group) {
        this.group = group;
        isMember = group.getUserMap().containsKey(userId);

        adapter = new GroupTabAdapter(getSupportFragmentManager(),1);
        if(isMember) {
            tabLayout.setVisibility(View.VISIBLE);
            adapter.addFragment(new GroupRadioFragment(), "Radio");
            adapter.addFragment(new GroupTextFragment(), "Chat");
        }else{
            adapter.addFragment(new JoinGroupFragment(),"Details");
            tabLayout.setVisibility(View.GONE);
        }

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }
}
