package com.komi.radiogroup.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.komi.radiogroup.R;
import com.komi.structures.User;

import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private List<User> users = new ArrayList<>();

    public UsersAdapter(List<User> users,String groupAdmin){
        this.users = users;
        this.groupAdmin = groupAdmin;
    }
    public String groupAdmin;

    public interface UsersListener {
        void onClick(int position);
    }

    UsersListener listener;

    public void setListener(UsersListener listener) {
        this.listener = listener;
    }

    public class UserViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        ImageView imageView;
        TextView bio;
        TextView adminTV;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.user_card_name);
            bio = itemView.findViewById(R.id.user_card_bio);
            imageView = itemView.findViewById(R.id.user_card_image);
            CardView mainView = itemView.findViewById(R.id.user_card_view);
            mainView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onClick(getAdapterPosition());
                    }
                }
            });
            adminTV = itemView.findViewById(R.id.user_card_admin);
        }
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_card_layout,parent,false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {

        User user = users.get(position);
        holder.name.setText(user.getFullname());
        holder.bio.setText(user.getBio());

        if(user.getProfilePicturePath() != null && !user.getProfilePicturePath().isEmpty()) {
            try {
            holder.imageView.setPadding(0,0,0,0);
                Glide.with(holder.itemView.getContext())
                        .load(user.getProfilePicturePath())
                        .into(holder.imageView);
            }catch (Exception e){
                holder.imageView.setPadding(10,10,10,10);
            }
        }

        if(user.getUID().equals(groupAdmin)){
            holder.adminTV.setVisibility(View.VISIBLE);
        }else{
            holder.adminTV.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void setUsers(List<User> users){
        this.users = users;
    }
}
