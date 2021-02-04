package com.komi.radiogroup;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static com.komi.radiogroup.MainActivity2.currentPlaying;
import static com.komi.radiogroup.MainActivity2.songs;
import static com.komi.radiogroup.MusicPlayerService.PLAYER_BROADCAST;

public class FullPageActivity extends Activity {

    BroadcastReceiver receiver;
    ImageView playBtn;

    int songIdx;
    boolean isPlaying = true;
    private SettingsContentObserver mSettingsContentObserver;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.full_page_layout);

        int song_idx = getIntent().getIntExtra("song_idx",0);

        songIdx = song_idx;
        playBtn = findViewById(R.id.full_play_btn);

        isPlaying = currentPlaying == song_idx;
        if(!isPlaying)
            playPauseMusic(playBtn);

        initSong();

        initReceiver();

        final SeekBar s = (SeekBar)findViewById(R.id.vol_seekbar);

        final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int a = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int c = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        s.setMax(a);
        s.setProgress(c);
        s.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, arg1, 0);
            }
        });

        mSettingsContentObserver = new SettingsContentObserver(this,new Handler());
        mSettingsContentObserver.setListener(new SettingsContentObserver.VolumeChanged() {
            @Override
            public void onChanged(int volume) {
                s.setProgress(volume);
            }
        });
        this.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mSettingsContentObserver );

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setHeight();
            }
        },100);

    }

    private void setHeight(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;

        LinearLayout controlLayout = findViewById(R.id.control_layout);

        LinearLayout imageLayout = findViewById(R.id.main_image_view);

        ViewGroup.LayoutParams imageParams = imageLayout.getLayoutParams();

        if(height - controlLayout.getHeight() - imageLayout.getHeight()  < 50) {
            int newHeight = height - controlLayout.getHeight() - 100;
            imageParams.height = newHeight;
            imageParams.width = newHeight;
            imageLayout.setLayoutParams(imageParams);
        }
    }

    private void initReceiver() {
        IntentFilter filter = new IntentFilter(PLAYER_BROADCAST);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String command = intent.getStringExtra("command");
                int song;
                switch (command) {
                    case "start":
                        song = intent.getIntExtra("song_idx", -1);
                        if (songIdx == song){
                            isPlaying = true;
                            playBtn.setImageResource(R.drawable.ic_baseline_pause_40);
                        }else {
                            songIdx = song;
                            initSong();
                        }
                        break;
                    case "stop":
                        onStopped();
                        break;
                    case "pause":
                        onPaused();
                        break;
                    case "resume":
                        onResumed();
                        break;
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,filter);
    }

    private void onResumed() {
        playBtn.setImageResource(R.drawable.ic_baseline_pause_40);
        isPlaying = true;
    }

    private void onPaused() {
        playBtn.setImageResource(R.drawable.ic_baseline_play_arrow_40);
        isPlaying = false;
    }

    private void onStopped() {
        playBtn.setImageResource(R.drawable.ic_baseline_play_arrow_40);
        isPlaying = false;
    }

    private void initSong() {
        Song song = songs.get(songIdx);
        isPlaying = true;
        playBtn.setImageResource(R.drawable.ic_baseline_pause_40);

        if (song != null) {
            ImageView imageView = findViewById(R.id.full_image);
            if (song.getImage() != null && !song.getImage().isEmpty()) {
                try {
                    imageView.setPadding(0, 0, 0, 0);
                    Glide.with(this)
                            .load(song.getImage())
                            .into(imageView);
                } catch (Exception e) {
                    imageView.setPadding(20, 20, 20, 20);
                    imageView.setBackgroundColor(Color.WHITE);
                    imageView.setImageResource(R.drawable.ic_music_note);
                }
            }else{
                imageView.setPadding(20, 20, 20, 20);
                imageView.setBackgroundColor(Color.WHITE);
                imageView.setImageResource(R.drawable.ic_music_note);
            }

            TextView nameTV = findViewById(R.id.full_song_name);
            nameTV.setText(song.getName());
            TextView urlTV = findViewById(R.id.full_song_url);
            urlTV.setText(song.getUrl());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
            this.getContentResolver().unregisterContentObserver(mSettingsContentObserver);
        }catch (Exception ignored){

        }
    }

    public void playPauseMusic(View view) {
        Intent intent = new Intent(
                this,MusicPlayerService.class);
        if(isPlaying){
            intent.putExtra("command","pause");
        }else{
            intent.putExtra("command","play");
            intent.putExtra("song_idx",songIdx);
            intent.putExtra("songs",songs);
        }
        startService(intent);
    }

    public void playPrev(View view) {
        if (isPlaying) {
            Intent intent = new Intent(
                    this, MusicPlayerService.class);
            intent.putExtra("command", "prev");
            startService(intent);
        }else{
            songIdx --;
            if(songIdx <0){
                songIdx = songs.size() -1;
            }
            playPauseMusic(view);
            initSong();
        }
    }

    public void stopMusic(View view) {
        Intent intent = new Intent(
                this, MusicPlayerService.class);

        intent.putExtra("command", "close");

        startService(intent);

    }

    public void playNext(View view) {
        if(isPlaying) {
            Intent intent = new Intent(
                    this, MusicPlayerService.class);

            intent.putExtra("command", "next");

            startService(intent);
        }else{
            songIdx++;
            if(songIdx == songs.size()){
                songIdx = 0;
            }
            playPauseMusic(view);
            initSong();
        }
    }

    public void goBack(View view) {
        finish();
    }
}
