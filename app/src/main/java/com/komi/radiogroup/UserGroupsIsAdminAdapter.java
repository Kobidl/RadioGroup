package com.komi.radiogroup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.komi.structures.Group;
import com.komi.structures.User;

import java.util.ArrayList;
import java.util.List;

public class UserGroupsIsAdminAdapter extends RecyclerView.Adapter<UserGroupsIsAdminAdapter.UserGroupViewHolder> {

    List<Group> groups = new ArrayList<>();
    User user;

    public class UserGroupViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        ImageView imageView;
        TextView memberCount;

        public UserGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_group_name);
            imageView = itemView.findViewById(R.id.group_card_image);
            memberCount = itemView.findViewById(R.id.tv_group_members);
            View smallGroupLayout = itemView.findViewById(R.id.small_group_layout);
            smallGroupLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener!=null){
                        listener.onClick(getAdapterPosition());
                    }
                }
            });
        }
    }

    public interface UserGroupListener{
        void onClick(int position);
    }

    UserGroupListener listener;

    public void setListener(UserGroupListener listener) {
        this.listener = listener;
    }

    public UserGroupsIsAdminAdapter(List<Group> groups) {
        this.groups = groups;
    }

    @NonNull
    @Override
    public UserGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_layout_small,parent,false);
        return new UserGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserGroupViewHolder holder, int position) {
        Group group = groups.get(position);
        holder.name.setText(group.getGroupName());
        Integer members = group.getUserMap().size();
        holder.memberCount.setText(members.toString());

        if(group.getProfilePicturePath() != null && !group.getProfilePicturePath().isEmpty()) {
            try {
                holder.imageView.setPadding(0,0,0,0);
                Glide.with(holder.itemView.getContext())
                        .load(group.getProfilePicturePath())
                        .into(holder.imageView);
            }catch (Exception e){
                holder.imageView.setPadding(10,10,10,10);
            }
        }

    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }
}
