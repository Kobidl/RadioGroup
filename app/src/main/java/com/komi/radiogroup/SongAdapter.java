package com.komi.radiogroup;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Vibrator;

import static com.komi.radiogroup.MainActivity2.currentPlaying;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private final List<Song> songs;
    private MySongListener listener;

    interface MySongListener {
        void onSongClicked(int position,View view);
        void onPlayClicked(int position,View view);
    }

    public void setListener(MySongListener listener){
        this.listener = listener;
    }

    public SongAdapter(List<Song> songs){
        this.songs = songs;
    }

    public class SongViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        ImageView imageView;
        TextView url;
        ImageButton playSongBtn;
        boolean playing = false;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.song_name);
            url = itemView.findViewById(R.id.song_url);
            imageView = itemView.findViewById(R.id.song_card_image);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener !=null){
                        listener.onSongClicked(getAdapterPosition(),view);
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Vibrator v = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE);
                    // Vibrate for 500 milliseconds
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        //deprecated in API 26
                        v.vibrate(100);
                    }
                    return false;
                }
            });
            playSongBtn = itemView.findViewById(R.id.play_song_btn);
            playSongBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener!=null){
                        listener.onPlayClicked(getAdapterPosition(), view);
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_layout,parent,false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.name.setText(song.getName());
        if(song.getImage() != null && !song.getImage().isEmpty()) {
            try {
            holder.imageView.setPadding(0,0,0,0);
                Glide.with(holder.itemView.getContext())
                        .load(song.getImage())
                        .into(holder.imageView);
            }catch (Exception e){
                holder.imageView.setPadding(10,10,10,10);
            }
        }
        if(currentPlaying != position && holder.playing){
            holder.playSongBtn.setImageResource(R.drawable.ic_baseline_play_arrow_40);
            holder.imageView.clearAnimation();
            holder.playing = false;
        }else if(currentPlaying == position && !holder.playing){
            holder.playing = true;
        }
        if(holder.playing && holder.imageView.getAnimation() == null){
            RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(4000);
            rotate.setInterpolator(new LinearInterpolator());
            rotate.setRepeatCount(Animation.INFINITE);
            holder.imageView.startAnimation(rotate);
        }
        holder.url.setText(song.getUrl());
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }
}
