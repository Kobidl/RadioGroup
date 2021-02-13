package com.komi.radiogroup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.komi.structures.Group;

import org.w3c.dom.Text;

import java.util.List;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupViewHolder> {

    private List<Group> groups;
    private GroupListener listener;
    private Context context;

    public interface GroupListener {
        void onClick(int position,View view);
    }

    public void setListener(GroupListener listener){
        this.listener = listener;
    }

    public GroupsAdapter(Context context,List<Group> groups){
        this.context = context;
        this.groups = groups;

    }

    public class GroupViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        ImageView imageView;
        TextView members;
        ImageView privacyIV;
        TextView privacyTV;
        TextView descTV;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.group_name);
            members = itemView.findViewById(R.id.group_members);
            imageView = itemView.findViewById(R.id.group_card_image);
            descTV = itemView.findViewById(R.id.group_desc);
            privacyIV = itemView.findViewById(R.id.group_privacy_icon);
            privacyTV = itemView.findViewById(R.id.group_privacy_desc);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener !=null){
                        listener.onClick(getAdapterPosition(),view);
                    }
                }
            });

        }
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_layout,parent,false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groups.get(position);
        holder.name.setText(group.getGroupName());
        Integer membersSize = group.getUserMap().size();
        holder.members.setText(membersSize.toString() + " " + context.getResources().getString(R.string.members));
        holder.descTV.setText(group.getGroupDescription());
        holder.privacyIV.setImageResource(context.getResources().getIdentifier(group.isPrivate() ? "ic_private" : "ic_public", "drawable", context.getPackageName()));
        holder.privacyTV.setText(context.getResources().getIdentifier(group.isPrivate() ? "private_str" : "public_str", "string", context.getPackageName()));
        if(group.getProfilePicturePath() != null && !group.getProfilePicturePath().isEmpty()) {
            try {
            holder.imageView.setPadding(0,0,0,0);
                Glide.with(holder.itemView.getContext())
                        .load(group.getProfilePicturePath())
                        .into(holder.imageView);
            }catch (Exception e){
                holder.imageView.setPadding(10,10,10,10);
            }
        }else{
            holder.imageView.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }
}
