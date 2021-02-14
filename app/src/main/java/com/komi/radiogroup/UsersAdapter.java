package com.komi.radiogroup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.komi.structures.Group;
import com.komi.structures.User;

import java.net.CookieHandler;
import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

//    private UserListener listener;
    private List<User> users = new ArrayList<>();

//    public interface UserListener {
//        void onClick(int position, View view);
//    }
//
//    public void setListener(UserListener listener){
//        this.listener = listener;
//    }

    public UsersAdapter(List<User> users){
        this.users = users;
    }

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
//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if(listener !=null){
//                        listener.onClick(getAdapterPosition(),view);
//                    }
//                }
//            });
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

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void setUsers(List<User> users){
        this.users = users;
    }
}
