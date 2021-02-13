package com.komi.radiogroup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
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
import com.komi.radiogroup.firebase.FirebaseDatabaseHelper;
import com.komi.radiogroup.pages.GroupRadioFragment;
import com.komi.radiogroup.pages.GroupTextFragment;
import com.komi.radiogroup.pages.JoinGroupFragment;
import com.komi.radiogroup.userlater.MusicPlayerService;
import com.komi.structures.Group;

import static com.komi.radiogroup.pages.Profile.SHARED_PREFS;
import static com.komi.radiogroup.pages.Profile.SP_UID;
import static com.komi.radiogroup.userlater.MusicPlayerService.GROUP_LISTENING;

public class GroupActivity extends AppCompatActivity implements JoinGroupFragment.JoinGroupCallback {

    public static boolean listening = false;
    public static Group group;
    private NonSwipeableViewPager viewPager;
    private TabLayout tabLayout;
    private GroupTabAdapter adapter;
    String userId;
    private boolean isMember = false;
    private ImageView groupImageView;
    private TextView groupNameTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_activity_layout);

        listening = getIntent().getBooleanExtra("playing",false);
        group = (Group) getIntent().getParcelableExtra("group");

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        userId = sharedPreferences.getString(SP_UID, null);
        String groupListeningId = sharedPreferences.getString(GROUP_LISTENING,null);
        if(groupListeningId!=null && groupListeningId.equals(group.getGroupID())){
            listening = true;
        }

        ImageButton backBtn = findViewById(R.id.group_back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });

        groupImageView = findViewById(R.id.group_small_image);

        groupNameTV = findViewById(R.id.group_title_text);

        tabLayout = (TabLayout) findViewById(R.id.group_tab_layout);
        viewPager = (NonSwipeableViewPager) findViewById(R.id.group_view_pager);

        adapter = new GroupTabAdapter(getSupportFragmentManager(),1);

        if(group==null){
            String groupId = getIntent().getStringExtra("group_id");
            if(groupId!=null) {
                FirebaseDatabaseHelper.getInstance().getGroupById(groupId, new FirebaseDatabaseHelper.OnGroupDataChangedCallback() {
                    @Override
                    public void onDataReceived(Group newGroup) {
                        group = newGroup;
                        setUIGroupDetails();
                        viewPager.setAdapter(adapter);
                        tabLayout.setupWithViewPager(viewPager);
                    }
                });
            }
        }else{
            setUIGroupDetails();
            viewPager.setAdapter(adapter);
            tabLayout.setupWithViewPager(viewPager);
        }

        viewPager.setHorizontalScrollBarEnabled(false);
    }

    private void setUIGroupDetails(){
        isMember = group.getUserMap().containsKey(userId);
        if(group.getProfilePicturePath() != null){
            Glide.with(this)
                    .load(group.getProfilePicturePath())
                    .into(groupImageView);
        }else{
            //todo: Set default image
        }
        groupNameTV.setText(group.getGroupName());
        if(isMember) {
            adapter.addFragment(new GroupRadioFragment(), getResources().getString(R.string.radio_tab));
            adapter.addFragment(new GroupTextFragment(),  getResources().getString(R.string.chat_tab));
        }else{
            adapter.addFragment(new JoinGroupFragment(), "");
            tabLayout.setVisibility(View.GONE);
        }
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
        if(isMember) {
            Intent intent = new Intent(this, GroupDetails.class);
            intent.putExtra("group", group);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        }
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
