package com.komi.radiogroup.firebase;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.komi.structures.Group;
import com.komi.structures.GroupMessage;
import com.komi.structures.User;

import java.util.ArrayList;
import java.util.List;

public class FirebaseDatabaseHelper {

    public static final String DB_USERS = "users_DB";
    public static final String DB_GROUPS = "groups_DB";
    public static final String DB_GROUP_MESSAGES = "group_messages_DB";

    private static FirebaseDatabaseHelper instance;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    DatabaseReference usersListenerRef;

    final List<User> users = new ArrayList<>();

    public static FirebaseDatabaseHelper getInstance() {

        if (instance == null)
            instance = new FirebaseDatabaseHelper();
        return instance;
    }

    public FirebaseDatabaseHelper() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

    }

    public void addUserToUsers(User user) {
        DatabaseReference reference = firebaseDatabase.getReference(DB_USERS);

        reference.child(user.getUID()).setValue(user);
    }

    public void addGroupToGroups(Group group) {
        DatabaseReference reference = firebaseDatabase.getReference(DB_GROUPS);

        reference.child(group.getGroupID()).setValue(group);
    }

    public void addGroupMessage(GroupMessage groupMessage) {
        DatabaseReference reference = firebaseDatabase.getReference(DB_GROUP_MESSAGES);

        reference.child(groupMessage.getGroup_ID()).child(groupMessage.getMsg_ID()).setValue(groupMessage);
    }

    public ValueEventListener getUsersListener() {
        users.clear();

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    User temp = snapshot1.getValue(User.class);
                    if(!users.contains(temp)){
                        Log.i("userslog", "Ondatachanges : adding user:" + temp.getFullname());
                        users.add(temp);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        return listener;
    }

    public List<User> getUsers() {
        return users;
    }
}
