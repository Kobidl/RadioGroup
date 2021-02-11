package com.komi.radiogroup;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.komi.structures.Group;
import com.komi.structures.User;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class GroupDetails extends AppCompatActivity {

    Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

         group = getIntent().getParcelableExtra("group");

        final LinearLayout innerLayout = findViewById(R.id.group_details_inner_layout);

        final ImageView imageView = findViewById(R.id.group_details_image);
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();

        imageView.setMaxHeight(metrics.widthPixels-100);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.group_details);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24);

        if(group.getProfilePicturePath() != null) {
            Glide.with(imageView).load(group.getProfilePicturePath()).into(imageView);
        }else{
            //todo: show empty image
        }



        TextView nameTV = findViewById(R.id.group_details_name);
        nameTV.setText(group.getGroupName());
        TextView descTV = findViewById(R.id.group_details_desc);
        descTV.setText(group.getGroupDescription());
        TextView membersTV = findViewById(R.id.group_details_members);
        membersTV.setText(getResources().getString(R.string.total) + ":" + group.getUserMap().size());

        RecyclerView recyclerView = findViewById(R.id.group_details_users_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<User> users = Collections.list(Collections.enumeration(group.getUserMap().values()));

        UsersAdapter usersAdapter = new UsersAdapter(users);
        recyclerView.setAdapter(usersAdapter);
        recyclerView.setNestedScrollingEnabled(false);


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void leaveGroup(View view) {

    }
}