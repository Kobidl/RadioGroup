package com.komi.radiogroup.interfaces;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public interface UserInterface {

    String getUID();

    void setUID(String UID);

    String getUsername();

    void setUsername(String username);

    String getFullname();

    void setFullname(String fullname);

    String getBio();

    void setBio(String bio);

    String getProfilePicturePath();

    void setProfilePicturePath(String profilePicturePath);

    String getTimeStamp();

    void setTimeStamp(String timeStamp);

    String toString();

}
