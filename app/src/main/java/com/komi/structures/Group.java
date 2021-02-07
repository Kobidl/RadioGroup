package com.komi.structures;

import androidx.annotation.NonNull;

import java.util.List;

public class Group {

    private String groupID;
    private String adminID;
    private String groupName;
    private String profilePicturePath;
    private String groupDescription;
    private String timeStamp;
    private boolean isPrivate;
    private List<User> userList;

    public Group(String groupID, String adminID, String groupName, String profilePicturePath, String groupDescription, String timeStamp, boolean isPrivate,List<User> userList) {
        this.groupID = groupID;
        this.adminID = adminID;
        this.groupName = groupName;
        this.profilePicturePath = profilePicturePath;
        this.groupDescription = groupDescription;
        this.timeStamp = timeStamp;
        this.isPrivate = isPrivate;
        this.userList = userList;

    }

    public Group() {
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getAdminID() {
        return adminID;
    }

    public void setAdminID(String adminID) {
        this.adminID = adminID;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public List<User> getUserList() {
        return userList;
    }

    public void addUserToUserList(User user) {
        userList.add(user);
    }

    @NonNull
    @Override
    public String toString() {

        String string = "\nGroupID : " + groupID + "\nGroupName : " + groupName + "\n";

        return string;
    }
}
