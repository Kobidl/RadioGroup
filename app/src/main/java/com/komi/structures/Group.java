package com.komi.structures;

public class Group {

    String groupID;
    String groupName;
    String groupDescription;

    public Group(String groupID, String groupName, String groupDescription) {
        this.groupID = groupID;
        this.groupName = groupName;
        this.groupDescription = groupDescription;
    }

    public Group() {
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }
}
