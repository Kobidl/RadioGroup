package com.komi.structures;

public class GroupMessage {

    public static final int MESSAGE_TYPE_TEXT = 1;
    public static final int MESSAGE_TYPE_RECORDING = 2;

    private String msg_ID;
    private String group_ID;
    private String from_ID;
    private String duration;
    private String timeStamp;
    private String body;
    private int type;

    public GroupMessage(String msg_ID, String group_ID, String from_ID, String duration, String timeStamp, String body, int type) {
        this.msg_ID = msg_ID;
        this.group_ID = group_ID;
        this.from_ID = from_ID;
        this.duration = duration;
        this.timeStamp = timeStamp;
        this.body = body;
        this.type = type;
    }

    public GroupMessage() {}

    public String getFrom_ID() {
        return from_ID;
    }

    public void setFrom_ID(String from_ID) {
        this.from_ID = from_ID;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMsg_ID() {
        return msg_ID;
    }

    public void setMsg_ID(String msg_ID) {
        this.msg_ID = msg_ID;
    }

    public String getGroup_ID() {
        return group_ID;
    }

    public void setGroup_ID(String group_ID) {
        this.group_ID = group_ID;
    }
}
