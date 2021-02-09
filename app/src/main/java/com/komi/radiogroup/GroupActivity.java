package com.komi.radiogroup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.komi.radiogroup.userlater.MusicPlayerService;
import com.komi.structures.Group;

import org.w3c.dom.Text;

import java.util.UUID;

public class GroupActivity extends AppCompatActivity {

    private boolean listening = false;
    Button startStopListening;
    Group group;

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

        TextView nameTV = findViewById(R.id.group_name_tv);
        TextView descTV = findViewById(R.id.group_desc_tv);
        TextView membersTV = findViewById(R.id.group_members_tv);
        membersTV.setText(getResources().getText(R.string.members) + ":" + group.getUserList().size());

        startStopListening = findViewById(R.id.btn_start_listening);
        startStopListening.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listening = !listening;
                if(listening)
                    playMusic();
                else
                    stopMusic();
            }
        });

        nameTV.setText(group.getGroupName());
        descTV.setText(group.getGroupDescription());

        ImageView imageView = findViewById(R.id.group_image_view);
        Glide.with(this)
                    .load(group.getProfilePicturePath())
                    .into(imageView);


    }

    /* If back button pressed on toolbar */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void playMusic(){
        Intent intent = new Intent(
                this, MusicPlayerService.class);
        intent.putExtra("command","start_listening");
        intent.putExtra("group",group);
        startService(intent);
        startStopListening.setText(R.string.stop_listening);
    }

    private void stopMusic(){
        try {
            Intent intent = new Intent(this, MusicPlayerService.class);
            stopService(intent);
            startStopListening.setText(R.string.start_listening);
        }catch (Exception e){

        }
    }

}
