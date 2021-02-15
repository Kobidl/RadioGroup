package com.komi.radiogroup.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.komi.radiogroup.R;
import com.komi.structures.Group;
import com.skydoves.androidribbon.RibbonView;

import java.util.List;
import java.util.Locale;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupViewHolder> {

    private List<Group> groups;
    private GroupListener listener;
    private final Context context;
    private final String userId;
    private boolean isRTL;

    public interface GroupListener {
        void onClick(int position,View view);
    }

    public void setListener(GroupListener listener){
        this.listener = listener;
    }

    public GroupsAdapter(Context context,List<Group> groups){
        this.context = context;
        this.groups = groups;
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.isRTL = isRTL(Locale.getDefault());
    }

    private static boolean isRTL(Locale locale) {
        final int directionality = Character.getDirectionality(locale.getDisplayName().charAt(0));
        return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
                directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        ImageView imageView;
        TextView members;
        ImageView privacyIV;
        TextView privacyTV;
        TextView descTV;
        RibbonView ribbonView;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.group_name);
            members = itemView.findViewById(R.id.group_members);
            imageView = itemView.findViewById(R.id.group_card_image);
            descTV = itemView.findViewById(R.id.group_desc);
            privacyIV = itemView.findViewById(R.id.group_privacy_icon);
            privacyTV = itemView.findViewById(R.id.group_privacy_desc);
            ribbonView = (RibbonView) itemView.findViewById(R.id.group_ribbon);

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
        int membersSize = group.getUserMap().size();
        holder.members.setText(membersSize + " " + context.getResources().getString(R.string.members));
        holder.descTV.setText(group.getGroupDescription());
        holder.privacyIV.setImageResource(context.getResources().getIdentifier(group.isPrivate() ? "ic_private" : "ic_public", "drawable", context.getPackageName()));
        holder.privacyTV.setText(context.getResources().getIdentifier(group.isPrivate() ? "private_str" : "public_str", "string", context.getPackageName()));
        if(group.getProfilePicturePath() != null && !group.getProfilePicturePath().isEmpty()) {
            holder.imageView.setBackgroundColor(Color.TRANSPARENT);
            try {
            holder.imageView.setPadding(0,0,0,0);
                Glide.with(holder.itemView.getContext())
                        .load(group.getProfilePicturePath())
                        .into(holder.imageView);
            }catch (Exception e){
                holder.imageView.setPadding(10,10,10,10);
            }
        }else{
            holder.imageView.setImageResource(R.drawable.ic_baseline_image_24);
            holder.imageView.setBackgroundColor(Color.parseColor("#dddddd"));
        }
        if(!isRTL && userId.equals(group.getAdminID())){
            holder.ribbonView.setVisibility(View.VISIBLE);
        }else{
            holder.ribbonView.setVisibility(View.GONE);
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
