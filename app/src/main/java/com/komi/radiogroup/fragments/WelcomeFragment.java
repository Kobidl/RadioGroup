package com.komi.radiogroup.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.komi.radiogroup.R;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class WelcomeFragment extends Fragment
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
    TextView errorTV;
    CircularProgressButton loginSignupBtn;
    View rootView;

    public interface OnWelcomeFragmentListener{
        void onRegister(String name);
        void onLogin();
    }

    OnWelcomeFragmentListener callback;

    public WelcomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callback = (OnWelcomeFragmentListener) context;
    }

    public static WelcomeFragment newInstance(String param1, String param2) {
        WelcomeFragment fragment = new WelcomeFragment();
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
        errorTV= rootView.findViewById(R.id.tv_error);

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
                String name = nameET.getText().toString();
                errorTV.setText("");

                if(loginMode){
                    if(email.isEmpty() || password.isEmpty()){
                        errorTV.setText(R.string.please_fill_fields);
                        return;
                    }
                }
                else{
                    if(email.isEmpty() || password.isEmpty() || name.isEmpty()){
                        errorTV.setText(R.string.please_fill_fields);
                        return;
                    }
                }

                loginSignupBtn.startAnimation();
                if(loginMode){
                    onLogin(email,password);
                }else {
                    onRegister(email,password,name);
                }
            }
        });
        return rootView;
    }

    public void onRegister(final String email, final String password, final String name) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                loginSignupBtn.revertAnimation();
                if (task.isSuccessful()) {
                    String name = nameET.getText().toString();
                    callback.onRegister(name);
                }
                else {
                    displayError(task);
                }
            }
        });
    }

    public void onLogin(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                loginSignupBtn.revertAnimation();
                if(task.isSuccessful()) {
                    callback.onLogin();
                }
                else {
                    displayError(task);
                }
            }
        });
    }

    public void displayError(Task<AuthResult> task) {
        String err = task.getException().getMessage();
        switch (err){

            case "The password is invalid or the user does not have a password.":
                errorTV.setText(R.string.wrong_password);
                break;
            case "There is no user record corresponding to this identifier. The user may have been deleted.":
                errorTV.setText(R.string.user_doesnt_exist);
                break;
            case "We have blocked all requests from this device due to unusual activity. Try again later. [ Access to this account has been temporarily disabled due to many failed login attempts. You can immediately restore it by resetting your password or you can try again later. ]":
                errorTV.setText(R.string.too_many_logins);
                break;
            case "The email address is already in use by another account.":
                errorTV.setText(R.string.email_in_use);
                break;
            case "The given password is invalid. [ Password should be at least 6 characters ]":
                errorTV.setText(R.string.password_weak);
                break;

        }
        Log.i("firelog", err);
    }
}