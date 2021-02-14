package com.komi.radiogroup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.komi.radiogroup.firebase.FirebaseDatabaseHelper;
import com.komi.radiogroup.firebase.FirebaseMessagingHelper;
import com.komi.radiogroup.pages.Welcome;
import com.komi.structures.Group;
import com.komi.structures.User;

import static com.komi.radiogroup.MainFragment.logout;

public class MainContainer extends AppCompatActivity implements Welcome.OnWelcomeFragmentListener,MainFragment.MainFragmentListener {


    public static String APP_URL = "https://www.radiogroup.com/invite/";
    public static String playingGroup = "";

    public static final String SHARED_PREFS = "radioGroup_sp";
    public static final String SP_UID = "latest_uid";
    private static final String SP_FULLNAME = "latest_fullname";
    private static final String SP_BIO = "latest_bio";
    private static final String SP_IMAGE = "latest_image";

    private FirebaseAuth firebaseAuth;
    Welcome welcomeFragment = new Welcome();
    MainFragment mainFragment = new MainFragment();

    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);

        firebaseAuth = FirebaseAuth.getInstance();

        // Initializing Firebase Database
        FirebaseDatabaseHelper.getInstance();

        Intent intent = getIntent();
        Uri initUrl = intent.getData();

        if (initUrl != null) {
            Intent gIntent = new Intent(this, GroupActivity.class);
            gIntent.putExtra("group_id", initUrl.toString().replace(APP_URL, "").replace("/", ""));
            startActivity(gIntent);
        }


        // Initializing Firebase Messaging
//        FirebaseMessagingHelper.getInstance(MainActivity.this).sendMessageToTopic("A", msg_et.getText().toString());

        //todo: check if registered by
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, welcomeFragment).commit();
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, mainFragment).commit();
        }

        FirebaseDatabaseHelper.getInstance().setUserByUidListener(currentUser.getUid(), new FirebaseDatabaseHelper.OnUserDataChangedCallback() {
            @Override
            public void onDataReceived(User user) {

                // Saving latest profile info to shared preferences
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(SP_UID, firebaseAuth.getCurrentUser().getUid());
                editor.putString(SP_FULLNAME, user.getFullname());
                editor.putString(SP_BIO, user.getBio());
                editor.putString(SP_IMAGE, user.getProfilePicturePath());
                editor.apply();
            }
        });
    }



    @Override
    public void onRegister(String name) {
        currentUser = firebaseAuth.getCurrentUser();
        updateDisplayName(name);
        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, mainFragment).commit();

        // Adding new user to database
        User newUser = new User();
        newUser.setUID(currentUser.getUid());
        newUser.setUsername(currentUser.getEmail());
        newUser.setFullname(name);
        newUser.setBio("Bio");
        FirebaseDatabaseHelper.getInstance().addUserToUsers(newUser);
    }

    @Override
    public void onLogin() {
        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, mainFragment).commit();
        currentUser = firebaseAuth.getCurrentUser();
    }

    private void updateDisplayName(String name) {
        if (currentUser != null) {
            currentUser.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(name).build()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){

                    }
                }
            });
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Uri initUrl = intent.getData();
        if(initUrl!=null){
            Intent gIntent = new Intent(this,GroupActivity.class);
            gIntent.putExtra("group_id",initUrl.toString().replace(APP_URL,"").replace("/",""));
            startActivity(gIntent);
        }


    }

    @Override
    public void onLogout() {
        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, welcomeFragment).commit();
    }

    //    private void logoutUser() {
//        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
//        if (firebaseUser != null)
//            firebaseAuth.signOut();
//        onLogout();
//    }

}
