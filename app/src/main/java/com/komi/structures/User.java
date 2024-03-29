package com.komi.structures;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.komi.radiogroup.interfaces.UserInterface;

import java.io.Serializable;

public class User implements UserInterface,Parcelable {

    private String UID = "";
    private String username = "";
    private String fullname = "";
    private String bio = "";
    private String profilePicturePath = "";
    private String timeStamp = "";

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

    protected User(Parcel in) {
        UID = in.readString();
        username = in.readString();
        fullname = in.readString();
        bio = in.readString();
        profilePicturePath = in.readString();
        timeStamp = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(UID);
        parcel.writeString(username);
        parcel.writeString(fullname);
        parcel.writeString(bio);
        parcel.writeString(profilePicturePath);
        parcel.writeString(timeStamp);
    }
}
