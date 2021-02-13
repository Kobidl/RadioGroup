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
import java.util.concurrent.TimeUnit;

public class FirebaseDatabaseHelper {

    public static final String DB_USERS = "users_DB";
    public static final String DB_GROUPS = "groups_DB";
    public static final String DB_GROUP_MESSAGES = "group_messages_DB";

    private static FirebaseDatabaseHelper instance;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    DatabaseReference usersListenerRef, groupListenerRef, groupMessageListenerRef;

    ValueEventListener usersListener, groupsListener, groupMessagesListener;

    final HashMap<String,User> users = new HashMap<>();
    final List<Group> groups = new ArrayList<>();
    final List<GroupMessage> groupMessages = new ArrayList<>();
    User user = null;

    // For join/leave group
    boolean userAddedToGroup, userRemovedFromGroup = false;

    // For groupsByUid
    boolean finishedFetching = false;
    List<Group> groupsByUid = new ArrayList<>();

    public static FirebaseDatabaseHelper getInstance() {

        if (instance == null)
            instance = new FirebaseDatabaseHelper();
        return instance;
    }

    public FirebaseDatabaseHelper() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        // Getting References
        usersListenerRef = firebaseDatabase.getReference().child(DB_USERS);
        groupListenerRef = firebaseDatabase.getReference().child(DB_GROUPS);
        groupMessageListenerRef = firebaseDatabase.getReference().child(DB_GROUP_MESSAGES);

        // Setting users listener
        setUsersListener();
        setGroupsListener();

    }

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

    private void setUsersListener() {

        usersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    User temp = snapshot1.getValue(User.class);
                    if(!users.containsKey(temp.getUID())){
                        users.put(temp.getUID(), temp);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        usersListenerRef.addListenerForSingleValueEvent(usersListener);
    }

    private void setGroupsListener() {

        groupsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groups.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()){

                    Group temp = snapshot1.getValue(Group.class);
                    if(!groups.contains(temp)){
                        groups.add(temp);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        groupListenerRef.addValueEventListener(groupsListener);
    }

    // GroupID will be user to filter messages to only save ones with provided GroupID
    public void setGroupMessageListener(final String groupID) {
        groupMessageListenerRef = firebaseDatabase.getReference().child(DB_GROUP_MESSAGES).child(groupID);

        groupMessagesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    GroupMessage temp = snapshot1.getValue(GroupMessage.class);
                    if(!groupMessages.contains(temp)){
                        if(temp.getGroup_ID().matches(groupID)) {
                            Log.i("dbtestlog", "Ondatachanges : adding groupMessage from groupID: " + temp.getGroup_ID());
                            groupMessages.add(temp);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        groupMessageListenerRef.addListenerForSingleValueEvent(groupMessagesListener);
    }

    public void removeListeners() {
        removeUsersListener();
        removeGroupsListener();
        //removeGroupMessagesListener(); GroupMessages listener may be null
    }

    public void removeUsersListener() {
        usersListenerRef.removeEventListener(usersListener);
        users.clear();
    }

    public void removeGroupsListener() {
        groupListenerRef.removeEventListener(groupsListener);
        groups.clear();
    }

    public void removeGroupMessagesListener() {
        groupMessageListenerRef.removeEventListener(groupMessagesListener);
        groupMessages.clear();
    }


    public interface GetUserCallback{
        void OnDataReceived(User user);
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

    public User getUserByUID(final String UID) { //This method must be ran async since it has a loop waiting for data

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    User temp = snapshot1.getValue(User.class);
                    if(temp.getUID().matches(UID)){
                        user = temp;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        databaseReference.addListenerForSingleValueEvent(listener);

        while (user == null){ // waiting until listener fetches the user
            try {
                TimeUnit.MILLISECONDS.sleep(250);
            }
            catch (InterruptedException ex){
                ex.printStackTrace();
            }
        }

        User temp = user;
        user = null;
        databaseReference.removeEventListener(listener);
        return temp;
    }

    public interface OnGroupDataChangedCallback{
        void onDataReceived(Group group);
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


    public List<Group> getGroupsByUID(final String UID){ //*****Must be run async
        finishedFetching = false;
        groupsByUid.clear();
        DatabaseReference reference = firebaseDatabase.getReference().child(DB_GROUPS);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    Group temp = snapshot1.getValue(Group.class);
                    if(temp.getUserMap().containsKey(UID)){
                        groupsByUid.add(temp);
                    }
                }
                finishedFetching = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        reference.addListenerForSingleValueEvent(valueEventListener);

        while(!finishedFetching){
            try {
                TimeUnit.MILLISECONDS.sleep(250);
            }
            catch (InterruptedException ex){
                ex.printStackTrace();
            }
        }
        reference.removeEventListener(valueEventListener);
        finishedFetching = false;
        return groupsByUid;
    }

    public List<Group> getGroupsByAdminID(final String UID) { //*****Must be run async
        finishedFetching = false;
        groupsByUid.clear();
        DatabaseReference reference = firebaseDatabase.getReference().child(DB_GROUPS);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    Group temp = snapshot1.getValue(Group.class);
                    if(temp.getAdminID().matches(UID)){
                        groupsByUid.add(temp);
                    }
                }
                finishedFetching = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        reference.addListenerForSingleValueEvent(valueEventListener);

        while(!finishedFetching){
            try {
                TimeUnit.MILLISECONDS.sleep(250);
            }
            catch (InterruptedException ex){
                ex.printStackTrace();
            }
        }
        reference.removeEventListener(valueEventListener);
        finishedFetching = false;
        return groupsByUid;
    }

    public HashMap<String,User> getUsers() {
        return users;
    }
    public List<Group> getGroups() { return groups;}
    public List<GroupMessage> getGroupMessages() { return groupMessages;}

    private void removeListenerFromRef(DatabaseReference reference, ValueEventListener listener) {


    }

}
