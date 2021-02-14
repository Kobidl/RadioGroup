package com.komi.radiogroup.interfaces;

import android.os.Parcel;

import androidx.annotation.NonNull;

import com.komi.structures.User;

import java.util.HashMap;
import java.util.Map;

public interface GroupInterface {
    String getGroupID();

    void setGroupID(String groupID);

    String getAdminID();

    void setAdminID(String adminID);

    String getGroupName();

    void setGroupName(String groupName);

    String getProfilePicturePath();

    void setProfilePicturePath(String profilePicturePath);

    String getGroupDescription();

    void setGroupDescription(String groupDescription);

    Long getTimeStamp();

    void setTimeStamp(Long timeStamp);

    boolean isPrivate();

    void setPrivate(boolean aPrivate);

    Map<String,Long> getUserMap();

    void addUserToUserList(User user);

    void setUserMap(Map<String, Long> userMap);

    String toString();

    void removeUserFromList(String userId);
}
