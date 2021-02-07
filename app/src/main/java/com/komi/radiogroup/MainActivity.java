package com.komi.radiogroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.VoiceInteractor;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.komi.radiogroup.firebase.FirebaseDatabaseHelper;
import com.komi.radiogroup.firebase.FirebaseMessagingHelper;
import com.komi.structures.Group;
import com.komi.structures.GroupMessage;
import com.komi.structures.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // Firebase Authentication Variables
    private FirebaseAuth firebaseAuth;

    // Firebase Database Variables
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    // Test Views
    private Button btn_signup, btn_login, btn_logout;
    TextView tv_userStatus;
    CheckBox groupA_cb, groupB_cb;
    EditText msg_et;
    Button send_btn;
    FirebaseMessaging firebaseMessaging;
    BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Initializing Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance();

        // Initializing Firebase Database
        FirebaseDatabaseHelper.getInstance();

        // Initializing Firebase Messaging
        firebaseMessaging = FirebaseMessaging.getInstance();

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


        // Testing firebase messaging
        groupA_cb = findViewById(R.id.group_a_cb);
        groupB_cb = findViewById(R.id.group_b_cb);
        msg_et = findViewById(R.id.et_msg_txt);

        firebaseMessaging.unsubscribeFromTopic("A");
        firebaseMessaging.unsubscribeFromTopic("B");

        groupA_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    firebaseMessaging.subscribeToTopic("A");
                }
                else
                    firebaseMessaging.unsubscribeFromTopic("A");
            }
        });

        groupB_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    firebaseMessaging.subscribeToTopic("B");
                }
                else
                    firebaseMessaging.unsubscribeFromTopic("B");
            }
        });

        send_btn = findViewById(R.id.btn_send);
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (groupA_cb.isChecked()){
                    FirebaseMessagingHelper.getInstance(MainActivity.this).sendMessageToTopic("A", msg_et.getText().toString());
                }
                else if (groupB_cb.isChecked()){
                    FirebaseMessagingHelper.getInstance(MainActivity.this).sendMessageToTopic("B", msg_et.getText().toString());
                }
                else
                    return;

            }
        });

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("MyFirebaseMessagingService", "New message received: " + intent.getStringExtra("message"));
            }
        };

        IntentFilter filter = new IntentFilter("message_received");
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,filter);
        
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Releasing Firebase DB update listeners
        FirebaseDatabaseHelper.getInstance().removeListeners();

        // releasing text lcb
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    ///////// Firebase Authorization Methods

    private void registerUser(final String username, final String fullname, String password) {

        firebaseAuth.createUserWithEmailAndPassword(username, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    updateDisplayName(fullname); // To set a fullname for a new user, it must first be registered, then the fullname needs to be updated.
                    Toast.makeText(MainActivity.this, "Register Successful", Toast.LENGTH_SHORT).show();
                    FirebaseDatabaseHelper.getInstance().addUserToUsers(new User(FirebaseAuth.getInstance().getCurrentUser().getUid(), username, fullname));
                    onLogin();
                }
                else
                    Toast.makeText(MainActivity.this, "Register Failed", Toast.LENGTH_SHORT).show();
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

                    }
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

    // UI updated should be called from this function
    private void onLogin() {
        tv_userStatus.setText("Logged in as: " + firebaseAuth.getCurrentUser().getDisplayName());
    }

    // UI updated should be called from this function
    private void onLogout() {
        tv_userStatus.setText("Logged out");
    }


    ///////// End of Firebase Authorization Methods

}