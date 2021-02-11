package com.komi.structures;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Group implements Parcelable {

    private String groupID = "";
    private String adminID = "";
    private String groupName = "";
    private String profilePicturePath = "";
    private String groupDescription = "";
    private String timeStamp = "";
    private boolean isPrivate = false;
    private Map<String,User> userMap = new HashMap<>();

    public Group(String groupID, String adminID, String groupName, String profilePicturePath, String groupDescription, String timeStamp, boolean isPrivate,HashMap<String,User> userMap) {
        this.groupID = groupID;
        this.adminID = adminID;
        this.groupName = groupName;
        this.profilePicturePath = profilePicturePath;
        this.groupDescription = groupDescription;
        this.timeStamp = timeStamp;
        this.isPrivate = isPrivate;
        this.userMap = userMap;

    }

    public Group() {
        userMap = new HashMap<>();
    }


    protected Group(Parcel in) {
        groupID = in.readString();
        adminID = in.readString();
        groupName = in.readString();
        profilePicturePath = in.readString();
        groupDescription = in.readString();
        timeStamp = in.readString();
        isPrivate = in.readByte() != 0;
        if(userMap == null){
            userMap = new HashMap<>();
        }
        in.readMap(userMap,User.class.getClassLoader());
    }

    public static final Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

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

    public Map<String,User> getUserMap() {
        return userMap;
    }

    public void addUserToUserList(User user) {
        userMap.put(user.getUID(), user);
    }

    public void setUserMap(Map<String, User> userMap) {
        this.userMap = userMap;
    }

    @NonNull
    @Override
    public String toString() {

        String string = "\nGroupID : " + groupID + "\nGroupName : " + groupName + "\n";

        return string;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(groupID);
        parcel.writeString(adminID);
        parcel.writeString(groupName);
        parcel.writeString(profilePicturePath);
        parcel.writeString(groupDescription);
        parcel.writeString(timeStamp);
        parcel.writeByte((byte) (isPrivate ? 1 : 0));
        if(userMap == null){
            userMap = new HashMap<>();
        }
        parcel.writeMap(userMap);
    }

}
