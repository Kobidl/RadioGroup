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

import java.util.List;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupViewHolder> {

    private List<Group> groups;
    private GroupListener listener;

    public interface GroupListener {
        void onClick(int position,View view);
    }

    public void setListener(GroupListener listener){
        this.listener = listener;
    }

    public GroupsAdapter(List<Group> groups){
        this.groups = groups;
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        ImageView imageView;
        TextView members;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.group_name);
            members = itemView.findViewById(R.id.group_members);
            imageView = itemView.findViewById(R.id.group_card_image);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener !=null){
                        listener.onClick(getAdapterPosition(),view);
                    }
                }
            });
//            itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//                    Vibrator v = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE);
//                    // Vibrate for 500 milliseconds
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
//                    } else {
//                        //deprecated in API 26
//                        v.vibrate(100);
//                    }
//                    return false;
//                }
//            });

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
        holder.members.setText(membersSize.toString());

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
