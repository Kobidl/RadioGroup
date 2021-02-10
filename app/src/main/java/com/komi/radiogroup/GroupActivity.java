package com.komi.radiogroup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.komi.radiogroup.pages.GroupRadioFragment;
import com.komi.radiogroup.pages.GroupTextFragment;
import com.komi.radiogroup.userlater.MusicPlayerService;
import com.komi.structures.Group;

import org.w3c.dom.Text;

import java.util.UUID;

public class GroupActivity extends AppCompatActivity {

    public static boolean listening = false;
    public static Group group;
    GroupRadioFragment groupRadioFragment = new GroupRadioFragment();
    GroupTextFragment groupTextFragment = new GroupTextFragment();

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

        groupTextFragment.setGroup(group);
        getSupportFragmentManager().beginTransaction().replace(R.id.group_frame_layout, groupTextFragment).commit();

        //getSupportFragmentManager().beginTransaction().replace(R.id.group_frame_layout, groupRadioFragment).commit();
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
