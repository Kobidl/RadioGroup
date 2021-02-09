package com.komi.radiogroup;

import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.komi.radiogroup.firebase.FirebaseDatabaseHelper;
import com.komi.radiogroup.firebase.FirebaseMessagingHelper;
import com.komi.radiogroup.pages.Welcome;

public class MainContainer extends AppCompatActivity implements Welcome.OnWelcomeFragmentListener {

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

        // Initializing Firebase Messaging
//        FirebaseMessagingHelper.getInstance(MainActivity.this).sendMessageToTopic("A", msg_et.getText().toString());


        //todo: check if registered by
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if(currentUser == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, welcomeFragment).commit();
        }else {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, mainFragment).commit();
        }
    }

    @Override
    public void onRegister(String name) {
        currentUser = firebaseAuth.getCurrentUser();
        updateDisplayName(name);
        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, mainFragment).commit();
    }

    @Override
    public void onLogin() {
        Toast.makeText(this, "hello login", Toast.LENGTH_SHORT).show();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, mainFragment).commit();
        currentUser = firebaseAuth.getCurrentUser();
    }

    private void updateDisplayName(String name) {
        if (currentUser != null) {
            currentUser.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(name).build()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(MainContainer.this, "User display name update successful", Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(MainContainer.this, "User display name update failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else
            Toast.makeText(MainContainer.this, "Cant update display name : Not logged in", Toast.LENGTH_SHORT).show();
    }

//
//    private void logoutUser() {
//        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
//        if (firebaseUser != null)
//            firebaseAuth.signOut();
//        onLogout();
//    }

}
