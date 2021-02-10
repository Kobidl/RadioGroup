package com.komi.radiogroup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.komi.structures.GroupMessage;

import java.util.List;

public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.GroupMessageViewHolder>{

    public class GroupMessageViewHolder extends RecyclerView.ViewHolder {

        TextView tv_name, tv_body, tv_time;

        public GroupMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_msg_name);
            tv_body = itemView.findViewById(R.id.tv_msg_body);
            tv_time = itemView.findViewById(R.id.tv_msg_time);
        }
    }

    private Context context;
    private List<GroupMessage> groupMessages;

    public GroupMessageAdapter(Context context, List<GroupMessage> groupMessages) {
        this.context = context;
        this.groupMessages = groupMessages;
    }

    @NonNull
    @Override
    public GroupMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.text_message_layout, null);
        return new GroupMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMessageViewHolder holder, int position) {
        GroupMessage groupMessage = groupMessages.get(position);

        holder.tv_name.setText(groupMessage.get);
    }

    @Override
    public int getItemCount() {
        return groupMessages.size();
    }



}
