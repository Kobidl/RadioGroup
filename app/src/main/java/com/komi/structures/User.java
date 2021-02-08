package com.komi.structures;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class User implements Serializable {

    private String UID;
    private String username;
    private String fullname;
    private String bio;
    private String profilePicturePath;
    private String timeStamp;

    public User(String UID, String username, String fullname, String bio, String profilePicturePath, String timeStamp) {
        this.UID = UID;
        this.username = username;
        this.fullname = fullname;
        this.bio = bio;
        this.profilePicturePath = profilePicturePath;
        this.timeStamp = timeStamp;
    }

    public User(String UID, String username, String fullname) {
        this.UID = UID;
        this.username = username;
        this.fullname = fullname;
        this.bio = bio;
        this.profilePicturePath = profilePicturePath;
        this.timeStamp = timeStamp;
    }

    public User() {}

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    @NonNull
    @Override
    public String toString() {

        String string = "\nUsername : " + username + "\nUID : " + UID + "\nFullname : " + fullname;
        return string;
    }
}
