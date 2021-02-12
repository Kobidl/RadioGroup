package com.komi.radiogroup;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.komi.structures.Group;
import com.komi.structures.User;

import java.util.Collections;
import java.util.List;

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

        //Set toolbar
        TextView titleTV = findViewById(R.id.toolbar_title);
        titleTV.setText(R.string.group_details);
        ImageButton backBtn = findViewById(R.id.toolbar_back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

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

    public void leaveGroup(View view) {

    }
}