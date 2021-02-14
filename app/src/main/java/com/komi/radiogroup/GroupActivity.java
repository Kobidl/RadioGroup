package com.komi.radiogroup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.komi.radiogroup.firebase.FirebaseDatabaseHelper;
import com.komi.radiogroup.pages.GroupRadioFragment;
import com.komi.radiogroup.pages.GroupTextFragment;
import com.komi.radiogroup.pages.JoinGroupFragment;
import com.komi.structures.Group;

import static com.komi.radiogroup.pages.Profile.SHARED_PREFS;
import static com.komi.radiogroup.pages.Profile.SP_UID;
import static com.komi.radiogroup.services.MusicPlayerService.GROUP_LISTENING;

public class GroupActivity extends AppCompatActivity implements JoinGroupFragment.JoinGroupCallback {

    private static final int GROUP_DETAILS_RESULT_CODE = 1;
    private boolean listening = false;
    private Group group;
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

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        String groupListeningId = sharedPreferences.getString(GROUP_LISTENING,null);
//        if(groupListeningId!=null && groupListeningId.equals(group.getGroupID())){
//            listening = true;
//        }

        listening = MainContainer.playingGroup.equals(group.getGroupID());

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
            final FrameLayout loader = findViewById(R.id.loader);
            ImageView loaderIV = findViewById(R.id.loader_image_view);
            final AnimationDrawable loaderAnimation = (AnimationDrawable) loaderIV.getDrawable();
            loaderAnimation.start();

            String groupId = getIntent().getStringExtra("group_id");
            if(groupId!=null) {
                FirebaseDatabaseHelper.getInstance().getGroupById(groupId, new FirebaseDatabaseHelper.OnGroupDataChangedCallback() {
                    @Override
                    public void onDataReceived(Group newGroup) {
                        group = newGroup;
                        setUIGroupDetails();
                        viewPager.setAdapter(adapter);
                        tabLayout.setupWithViewPager(viewPager);
                        loader.setVisibility(View.GONE);
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
            adapter.addFragment(new GroupRadioFragment(group,listening), getResources().getString(R.string.radio_tab));
            adapter.addFragment(new GroupTextFragment(group),  getResources().getString(R.string.chat_tab));
        }else{
            adapter.addFragment(new JoinGroupFragment(group), "");
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
            startActivityForResult(intent,GROUP_DETAILS_RESULT_CODE);
        }
    }

    @Override
    public void onJoinedGroup(Group group) {
        this.group = group;
        isMember = group.getUserMap().containsKey(userId);

        adapter = new GroupTabAdapter(getSupportFragmentManager(),1);
        if(isMember) {
            tabLayout.setVisibility(View.VISIBLE);
            adapter.addFragment(new GroupRadioFragment(group, listening), "Radio");
            adapter.addFragment(new GroupTextFragment(group), "Chat");
        }else{
            adapter.addFragment(new JoinGroupFragment(group),"Details");
            tabLayout.setVisibility(View.GONE);
        }

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GROUP_DETAILS_RESULT_CODE){
            if(resultCode == Activity.RESULT_OK){
                group = (Group) data.getParcelableExtra("group");
                groupNameTV.setText(group.getGroupName());
            }
        }
    }
}
