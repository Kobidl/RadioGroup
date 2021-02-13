package com.komi.radiogroup.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.komi.structures.Group;
import com.komi.structures.GroupMessage;
import com.komi.structures.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class FirebaseDatabaseHelper {

    public static final String DB_USERS = "users_DB";
    public static final String DB_GROUPS = "groups_DB";
    public static final String DB_GROUP_MESSAGES = "group_messages_DB";

    private static FirebaseDatabaseHelper instance;
    FirebaseDatabase firebaseDatabase;


    DatabaseReference usersListenerRef, userByUidListenerRef, groupListenerRef, groupMessageListenerRef, exploreListenerRef, adminGroupsListenerRef;

    ValueEventListener usersListener, userByUidListener, groupsListener, groupMessagesListener, exploreListener, adminGroupsListener;


    public static FirebaseDatabaseHelper getInstance() {

        if (instance == null)
            instance = new FirebaseDatabaseHelper();
        return instance;
    }

    public FirebaseDatabaseHelper() {
        firebaseDatabase = FirebaseDatabase.getInstance();

    }

    // Interfaces used for callbacks
    public interface OnExploreDataChangedCallback{
        void onDataReceived(List<Group> groups);
    }

    public interface OnGroupsDataChangedCallback{
        void onDataReceived(List<Group> groups);
    }

    public interface OnUserDataChangedCallback{
        void onDataReceived(User user);
    }

    public interface OnGroupDataChangedCallback{
        void onDataReceived(Group group);
    }

    public interface GetUserCallback{
        void OnDataReceived(User user);
    }

    public interface OnUsersInGroupDataChangedCallback{
        void OnDataReceived(List<User> users);
    }

    // Add methods
    public void addUserToUsers(User user) {
        DatabaseReference reference = firebaseDatabase.getReference(DB_USERS);

        reference.child(user.getUID()).setValue(user);
    }



    public void addGroupToGroups(final Group group, final OnGroupDataChangedCallback callback) {
        DatabaseReference reference = firebaseDatabase.getReference(DB_GROUPS);

        reference.child(group.getGroupID()).setValue(group).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                callback.onDataReceived(group);
            }
        });
    }

    public void addGroupMessage(GroupMessage groupMessage) {
        DatabaseReference reference = firebaseDatabase.getReference(DB_GROUP_MESSAGES);

        reference.child(groupMessage.getGroup_ID()).child(groupMessage.getMsg_ID()).setValue(groupMessage);
    }



    // Get methods
    public void setUserByUidListener(final String UID, final OnUserDataChangedCallback callback) {

        userByUidListenerRef = firebaseDatabase.getReference().child(DB_USERS);
        userByUidListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    User user = snapshot1.getValue(User.class);
                    if(user.getUID().matches(UID)){
                        callback.onDataReceived(user);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        userByUidListenerRef.addValueEventListener(userByUidListener);
    }

    public void removeUserByUIDListener() {
        userByUidListenerRef.removeEventListener(userByUidListener);
    }


    public void setGroupsByUIDListener(final String UID, final OnGroupsDataChangedCallback callback) {

        groupListenerRef = firebaseDatabase.getReference().child(DB_GROUPS);
        groupsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Group> groupList = new ArrayList<>();
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    Group temp = snapshot1.getValue(Group.class);
                    if(temp.getUserMap().containsKey(UID)){
                        groupList.add(temp);
                    }
                }
                callback.onDataReceived(groupList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        groupListenerRef.addValueEventListener(groupsListener);
    }

    public void removeGroupsByUIDListener() {
        groupListenerRef.removeEventListener(groupsListener);
    }


    public void setGroupsByAdminIDListener(final String UID, final OnGroupsDataChangedCallback callback) {

        adminGroupsListenerRef = firebaseDatabase.getReference().child(DB_GROUPS);
        adminGroupsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Group> groups = new ArrayList<>();
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    Group temp = snapshot1.getValue(Group.class);
                    if(temp.getAdminID().matches(UID)){
                        groups.add(temp);
                    }
                }
                callback.onDataReceived(groups);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        adminGroupsListenerRef.addListenerForSingleValueEvent(adminGroupsListener);
    }

    public void removeGroupsByAdminIDListener(){
        adminGroupsListenerRef.removeEventListener(adminGroupsListener);
    }


    public void setExploreListener(final String userID, final String subString, final OnExploreDataChangedCallback callback) {

        exploreListenerRef = firebaseDatabase.getReference().child(DB_GROUPS);
        exploreListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Group> groups = new ArrayList<>();
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    Group temp = snapshot1.getValue(Group.class);
                    if(!temp.getUserMap().containsKey(userID) && !temp.isPrivate() && temp.getGroupName().contains(subString) ){ //if user not in group and group is public and group name contains substring
                        groups.add(temp);
                    }
                }
                callback.onDataReceived(groups);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        exploreListenerRef.addValueEventListener(exploreListener);
    }

    public void removeExploreListener(){
        exploreListenerRef.removeEventListener(exploreListener);
    }

    public void setUsersInGroupListener(final List<String> userIDs, final OnUsersInGroupDataChangedCallback callback) {

        usersListenerRef = firebaseDatabase.getReference().child(DB_USERS);
        usersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<User> users = new ArrayList<>();
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    User temp = snapshot1.getValue(User.class);
                    if(userIDs.contains(temp.getUID())){
                        users.add(temp);
                    }
                }
                callback.OnDataReceived(users);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        usersListenerRef.addValueEventListener(usersListener);
    }

    public void removeUsersInGroupListener(){
        usersListenerRef.removeEventListener(usersListener);
    }

    public void addUserToGroup(final User user, final Group group, final OnGroupDataChangedCallback callback){ // ******Must be run async
        Log.d("user",user.getUID());
        final DatabaseReference reference = firebaseDatabase.getReference().child(DB_GROUPS);
        group.addUserToUserList(user);
        reference.child(group.getGroupID()).setValue(group).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("add_user","success");

                callback.onDataReceived(group);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("add_user","fail");
                group.removeUserFromList(user.getUID());
                callback.onDataReceived(group);
            }
        });
    }


    public void removeUserFromGroup(String userID, final Group group, final OnGroupDataChangedCallback callback){
        DatabaseReference reference = firebaseDatabase.getReference().child(DB_GROUPS);
        final Group newGroup = group;
        newGroup.removeUserFromList(userID);
        reference.child(group.getGroupID()).setValue(group).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                callback.onDataReceived(newGroup);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onDataReceived(group);
            }
        });
    }

    public void deleteGroup(final Group group, final OnGroupDataChangedCallback callback) {
        DatabaseReference reference = firebaseDatabase.getReference().child(DB_GROUPS);
        reference.child(group.getGroupID()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                callback.onDataReceived(null);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onDataReceived(group);
            }
        });
    }

    public void getUserById(final String id, final GetUserCallback callback){
        DatabaseReference databaseReference = firebaseDatabase.getReference().child(DB_USERS);
        databaseReference.child(id).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                User u = (User)dataSnapshot.getValue(User.class);
                callback.OnDataReceived(u);
            }
        });
    }

    public void getGroupById(String groupId,final OnGroupDataChangedCallback callback) {
        DatabaseReference databaseReference = firebaseDatabase.getReference().child(DB_GROUPS);
        databaseReference.child(groupId).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                Group group = (Group) dataSnapshot.getValue(Group.class);
                callback.onDataReceived(group);
            }
        });
    }
}