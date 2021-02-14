package com.komi.radiogroup;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.komi.radiogroup.pages.Profile;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView titleTV = findViewById(R.id.toolbar_title);
        titleTV.setText("");

        ImageButton backBtn = findViewById(R.id.toolbar_back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        String user_id = getIntent().getStringExtra("user_id");

        Profile profile = new Profile(user_id);
        getSupportFragmentManager().beginTransaction().replace(R.id.profile_frame_layout, profile).commit();
    }
}