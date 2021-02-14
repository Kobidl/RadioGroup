package com.komi.radiogroup.interfaces;

import androidx.annotation.NonNull;

public interface GroupMessageInterface {

    String getFrom_ID();

    void setFrom_ID(String from_ID);

    String getDuration();

    void setDuration(String duration);

    long getTimeInMillis();

    void setTimeInMillis(long timeInMillis);

    String getBody();

    void setBody(String body);

    int getType();

    void setType(int type);

    String getMsg_ID();

    void setMsg_ID(String msg_ID);

    String getGroup_ID();

    void setGroup_ID(String group_ID);

    String getFrom_name();

    void setFrom_name(String from_name);

    String toString();
}
