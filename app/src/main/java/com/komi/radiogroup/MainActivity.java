package com.komi.radiogroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class MainActivity extends AppCompatActivity {

    // Firebase Authentication Variables
    private FirebaseAuth firebaseAuth;

    private Button btn_signup, btn_login, btn_logout;
    TextView tv_userStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Initializing Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance();

        // Initializing Layout listeners and getting references
        tv_userStatus = findViewById(R.id.tv_user_status);

        btn_signup = findViewById(R.id.btn_sign_up);
        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.signup_dialog, null);
                final EditText et_username = dialogView.findViewById(R.id.et_email);
                final EditText et_fullname = dialogView.findViewById(R.id.et_fullname);
                final EditText et_password = dialogView.findViewById(R.id.et_password);

                builder.setView(dialogView).setPositiveButton("Register", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String username = et_username.getText().toString();
                        String fullname = et_fullname.getText().toString();
                        String password = et_password.getText().toString();

                        // Register the user
                        registerUser(username, fullname, password);
                    }
                }).show();
            }
        });

        btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.login_dialog, null);
                final EditText et_username = dialogView.findViewById(R.id.et_email);
                final EditText et_password = dialogView.findViewById(R.id.et_password);

                builder.setView(dialogView).setPositiveButton("Login", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String username = et_username.getText().toString();
                        String password = et_password.getText().toString();

                        //Sign in the user
                        loginUser(username, password);
                    }
                }).show();
            }
        });

        btn_logout = findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        Button btn_recorder = findViewById(R.id.btn_recorder);
        btn_recorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,RecorderActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser != null) {
            onLogin();
        }

    }

    private void registerUser(String username, final String fullname, String password) {

        firebaseAuth.createUserWithEmailAndPassword(username, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    updateDisplayName(fullname);
                    Toast.makeText(MainActivity.this, "Register Successful", Toast.LENGTH_SHORT).show();
                    onLogin();
                }
                else
                    Toast.makeText(MainActivity.this, "Register Failed", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("failed",e.toString());
            }
        });

    }

    private void updateDisplayName(final String fullname) {

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(fullname).build()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(MainActivity.this, "User display name update successful", Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(MainActivity.this, "User display name update failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else
            Toast.makeText(MainActivity.this, "Cant update display name : Not logged in", Toast.LENGTH_SHORT).show();
    }

    private void loginUser(String username, String password) {

        firebaseAuth.signInWithEmailAndPassword(username,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "User login successful", Toast.LENGTH_SHORT).show();
                    onLogin();
                }
                else
                    Toast.makeText(MainActivity.this, "User login failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logoutUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null)
            firebaseAuth.signOut();
        onLogout();
    }

    private void onLogin() {
        tv_userStatus.setText("Logged in as: " + firebaseAuth.getCurrentUser().getDisplayName());
    }

    private void onLogout() {
        tv_userStatus.setText("Logged out");
    }

}