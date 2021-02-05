package com.komi.radiogroup.firebase;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.komi.structures.Group;

import java.util.ArrayList;
import java.util.List;

public class FirebaseDatabaseHelper {
    private static FirebaseDatabaseHelper instance;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference users, userGroups;
    List<Group> groups;

    public static FirebaseDatabaseHelper getInstance() {

        if (instance == null)
            instance = new FirebaseDatabaseHelper();
        return instance;
    }

    public FirebaseDatabaseHelper() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        users = firebaseDatabase.getReference("users");
        userGroups = firebaseDatabase.getReference("user-groups");

        groups = new ArrayList<>();
    }



}
