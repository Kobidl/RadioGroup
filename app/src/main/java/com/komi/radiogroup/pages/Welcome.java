package com.komi.radiogroup.pages;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.komi.radiogroup.MainContainer;
import com.komi.radiogroup.R;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class Welcome extends Fragment
{
    private FirebaseAuth firebaseAuth;

    private boolean loginMode = true;

    TextInputLayout nameContainer;
    EditText nameET;
    EditText emailET;
    EditText passwordET;
    TextView titleTV;
    TextView bottomLabelTV;
    TextView bottomLabelActionTV;
    CircularProgressButton loginSignupBtn;
    View rootView;

    public interface OnWelcomeFragmentListener{
        void onRegister(String name);
        void onLogin();
    }

    OnWelcomeFragmentListener callback;

    public Welcome() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callback = (OnWelcomeFragmentListener) context;
    }

    public static Welcome newInstance(String param1, String param2) {
        Welcome fragment = new Welcome();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        firebaseAuth = FirebaseAuth.getInstance();
        rootView = inflater.inflate(R.layout.fragment_welcome, container, false);
        loginMode = true;
        nameContainer = rootView.findViewById(R.id.register_name_container);
        nameET = rootView.findViewById(R.id.register_name);
        emailET = rootView.findViewById(R.id.email);
        passwordET = rootView.findViewById(R.id.password) ;
        titleTV = rootView.findViewById(R.id.welcome_mode_title);
        bottomLabelTV = rootView.findViewById(R.id.change_mode_title);
        bottomLabelActionTV = rootView.findViewById(R.id.change_mode_btn);
        loginSignupBtn = (CircularProgressButton) rootView.findViewById(R.id.btn_login_signup);

        bottomLabelActionTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginMode = !loginMode;
                if(loginMode){
                    nameContainer.setVisibility(View.GONE);
                    titleTV.setText(R.string.signin);
                    bottomLabelTV.setText(R.string.signupdesc);
                    bottomLabelActionTV.setText(R.string.signup);
                    loginSignupBtn.setText(R.string.signin);
                }else {
                    nameContainer.setVisibility(View.VISIBLE);
                    titleTV.setText(R.string.signup);
                    bottomLabelTV.setText(R.string.logindesc);
                    bottomLabelActionTV.setText(R.string.signin);
                    loginSignupBtn.setText(R.string.signup);
                }

            }
        });

        loginSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = String.valueOf(emailET.getText()).trim().toLowerCase();
                String password = String.valueOf(passwordET.getText()).trim();
                if(email.isEmpty() || password.isEmpty()) return;
                loginSignupBtn.startAnimation();
                if(loginMode){
                    onLogin(email,password);
                }else {
                    String name = nameET.getText().toString();
                    onRegister(email,password,name);
                }
            }
        });
        return rootView;
    }

    public void onRegister(String email, String password, final String name) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                loginSignupBtn.revertAnimation();
                if (task.isSuccessful()) {
                    Toast.makeText(rootView.getContext(), "Register Successful", Toast.LENGTH_SHORT).show();
                    callback.onLogin();
                }
                else
                    Toast.makeText(rootView.getContext(), "Register Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onLogin(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                loginSignupBtn.revertAnimation();
                if(task.isSuccessful()) {
                    Toast.makeText(rootView.getContext(), "User login successful", Toast.LENGTH_SHORT).show();
                    String name = nameET.getText().toString();
                    callback.onRegister(name);
                }
                else
                    Toast.makeText(rootView.getContext(), "User login failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}