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

import java.net.CookieHandler;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

//    private UserListener listener;
    private List<User> users;

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

    public class UserViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        ImageView imageView;
        TextView username;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.user_card_name);
            username = itemView.findViewById(R.id.user_card_username);
            imageView = itemView.findViewById(R.id.user_card_image);
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
        holder.username.setText(user.getUsername());

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
}
