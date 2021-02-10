package com.komi.radiogroup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.komi.structures.GroupMessage;


import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.GroupMessageViewHolder>{

    public class GroupMessageViewHolder extends RecyclerView.ViewHolder {

        TextView tv_name_other, tv_name_user, tv_body, tv_time;

        public GroupMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_name_other = itemView.findViewById(R.id.tv_msg_name);
            tv_name_user = itemView.findViewById(R.id.tv_msg_name_user);
            tv_body = itemView.findViewById(R.id.tv_msg_body);
            tv_time = itemView.findViewById(R.id.tv_msg_time);
        }
    }

    private Context context;
    private List<GroupMessage> groupMessages;
    private FirebaseUser currentUser;

    public GroupMessageAdapter(Context context, List<GroupMessage> NgroupMessages) {
        this.context = context;
        this.groupMessages = NgroupMessages;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public GroupMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.text_message_layout,parent,false);
        return new GroupMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMessageViewHolder holder, int position) {
        GroupMessage groupMessage = groupMessages.get(position);

        if(groupMessage.getFrom_ID().matches(currentUser.getUid())){ // Case the message was send by the user
            holder.tv_name_user.setText("Me");
            holder.tv_name_other.setText("");
        }
        else{
            holder.tv_name_other.setText(groupMessage.getFrom_name());
            holder.tv_name_user.setText("");

        }
        holder.tv_body.setText(groupMessage.getBody());
        holder.tv_time.setText(getDate(groupMessage.getTimeInMillis()));

    }

    @Override
    public int getItemCount() {
        return groupMessages.size();
    }


    public String getDate(long milliSeconds)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm", Locale.FRANCE);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public void setGroupMessages(List<GroupMessage> groupMessages) {
        this.groupMessages = groupMessages;
    }
}