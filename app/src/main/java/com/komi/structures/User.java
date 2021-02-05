package com.komi.structures;

public class User {

    String UID;
    String fullname;
    String description;
    String profilePicturePath;

    public User(String UID, String fullname, String description, String profilePicturePath) {
        this.UID = UID;
        this.fullname = fullname;
        this.description = description;
        this.profilePicturePath = profilePicturePath;
    }

    public User() {}

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }
}
