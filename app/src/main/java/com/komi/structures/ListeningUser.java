package com.komi.structures;

import com.komi.radiogroup.interfaces.ListeningUserInterface;

public class ListeningUser implements ListeningUserInterface {

    private String userID;
    private Long timeInMillis;

    public ListeningUser(String userID, Long timeInMillis) {
        this.userID = userID;
        this.timeInMillis = timeInMillis;
    }

    public ListeningUser() {
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public Long getTimeInMillis() {
        return timeInMillis;
    }

    public void setTimeInMillis(Long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }
}
