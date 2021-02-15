package com.komi.radiogroup.interfaces;

import com.komi.structures.Group;
import com.komi.structures.GroupMessage;
import com.komi.structures.User;

import java.util.List;

public interface FirebaseDatabaseHelperInterface {

    interface OnExploreDataChangedCallback{
        void onDataReceived(List<Group> groups);
    }

    interface OnGroupsDataChangedCallback{
        void onDataReceived(List<Group> groups);
    }

    interface OnUserDataChangedCallback{
        void onDataReceived(User user);
    }

    interface OnGroupDataChangedCallback{
        void onDataReceived(Group group);
    }

    interface GetUserCallback{
        void OnDataReceived(User user);
    }

    interface OnUsersInGroupDataChangedCallback{
        void OnDataReceived(List<User> users);
    }

    interface OnUsersListeningDataChangedCallback{
        void OnDataReceived(int integer);
    }

    void addUserToUsers(User user);


    void addGroupToGroups(final Group group, final OnGroupDataChangedCallback callback);

    void addGroupMessage(GroupMessage groupMessage);


    //Get methods
    void setUserByUidListener(final String UID, final OnUserDataChangedCallback callback);

    void removeUserByUIDListener();


    void setSortedGroupsByUIDListener(final String UID, final OnGroupsDataChangedCallback callback);

    void removeSortedGroupsByUIDListener();


    void setGroupsByAdminIDListener(final String UID, final boolean withPrivate, final OnGroupsDataChangedCallback callback);

    void removeGroupsByAdminIDListener();

    void setExploreListener(final String userID, final String subString, final OnExploreDataChangedCallback callback);

    void removeExploreListener();

    void setUsersInGroupListener(final List<String> userIDs, final OnUsersInGroupDataChangedCallback callback);

    void removeUsersInGroupListener();

    void addUserToGroup(final User user, final Group group, final OnGroupDataChangedCallback callback);

    void removeUserFromGroup(String userID, final Group group, final OnGroupDataChangedCallback callback);

    void deleteGroup(final Group group, final OnGroupDataChangedCallback callback);

    void getUserById(final String id, final GetUserCallback callback);

    void getGroupById(String groupId, final OnGroupDataChangedCallback callback);

    void updateUserListening(String groupID, String userID, boolean active);

    void getActiveUsers(String groupID, final OnUsersListeningDataChangedCallback callback);

}