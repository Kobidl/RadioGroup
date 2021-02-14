package com.komi.radiogroup;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.komi.radiogroup.firebase.FirebaseDatabaseHelper;
import com.komi.structures.Group;
import com.komi.structures.User;

import java.lang.reflect.Array;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

import static com.komi.radiogroup.MainContainer.APP_URL;

public class GroupDetails extends AppCompatActivity {

    private static final int EDIT_DETAILS_RESULT_CODE = 1;
    Group group;
    CircularProgressButton leaveBtn;
    private String userId;
    private ImageView imageView;
    private TextView nameTV;
    private TextView descTV;

    private TextView privacyTV;

    private List<User> users;

    RecyclerView recyclerView;
    UsersAdapter usersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

         group = getIntent().getParcelableExtra("group");
         userId = getIntent().getStringExtra("user_id");
         final boolean isAdmin = group.getAdminID().equals(userId);

        final LinearLayout innerLayout = findViewById(R.id.group_details_inner_layout);

        imageView = findViewById(R.id.group_details_image);
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

        nameTV = findViewById(R.id.group_details_name);
        descTV = findViewById(R.id.group_details_desc);
        privacyTV = findViewById(R.id.group_details_privacy);

        setDetails();

        TextView membersTV = findViewById(R.id.group_details_members);
        membersTV.setText(getResources().getString(R.string.total) + ":" + group.getUserMap().size());

        recyclerView = findViewById(R.id.group_details_users_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        users = new ArrayList<>();

        usersAdapter = new UsersAdapter(users);
        usersAdapter.setListener(new UsersAdapter.UsersListener() {
            @Override
            public void onClick(int position) {
                User user = users.get(position);
                Intent intent = new Intent(GroupDetails.this, ProfileActivity.class);
                intent.putExtra("user_id", user.getUID());
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(usersAdapter);
        recyclerView.setNestedScrollingEnabled(false);

        List<String> keys = new ArrayList<String>(group.getUserMap().keySet());
        FirebaseDatabaseHelper.getInstance().setUsersInGroupListener(keys, new FirebaseDatabaseHelper.OnUsersInGroupDataChangedCallback() {
            @Override
            public void OnDataReceived(List<User> newUsers) {
                users = newUsers;
                usersAdapter.setUsers(newUsers);
                usersAdapter.notifyDataSetChanged();
            }
        });

        leaveBtn = findViewById(R.id.leave_group_btn);

        if(isAdmin){
            leaveBtn.setText(R.string.close_group);
            leaveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    leaveBtn.startAnimation();
                    FirebaseDatabaseHelper.getInstance().deleteGroup(group, new FirebaseDatabaseHelper.OnGroupDataChangedCallback() {
                        @Override
                        public void onDataReceived(Group group) {
                            final Intent intent = new Intent(GroupDetails.this, MainContainer.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    });
                }
            });
            MaterialButton editBtn = findViewById(R.id.group_details_edit_btn);
            editBtn.setVisibility(View.VISIBLE);
            editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(GroupDetails.this, EditGroupActivity.class);
                    intent.putExtra("group", group);
                    startActivityForResult(intent, EDIT_DETAILS_RESULT_CODE);
                }
            });
        }
        else {
            leaveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    leaveBtn.startAnimation();
                    FirebaseDatabaseHelper.getInstance().removeUserFromGroup(userId, group, new FirebaseDatabaseHelper.OnGroupDataChangedCallback() {
                        @Override
                        public void onDataReceived(Group group) {
                            final Intent intent = new Intent(GroupDetails.this, MainContainer.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    });
                }
            });
        }

        final MaterialButton copyLinkBtn = findViewById(R.id.group_details_link_btn);
        copyLinkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = APP_URL + group.getGroupID();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(url, url);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(GroupDetails.this, "Linked Copied", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void setDetails(){
        nameTV.setText(group.getGroupName());
        descTV.setText(group.getGroupDescription());
        privacyTV.setText(getResources().getIdentifier(group.isPrivate() ? "private_str":"public_str", "string", this.getPackageName()));
        if(group.getProfilePicturePath() != null) {
            Glide.with(imageView).load(group.getProfilePicturePath()).into(imageView);
        }else{
            //todo: show empty image
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if(requestCode == EDIT_DETAILS_RESULT_CODE){
            if(resultCode == Activity.RESULT_OK){
                group = (Group) data.getParcelableExtra("group");
                setDetails();
            }
        }
    }
}