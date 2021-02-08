package com.komi.radiogroup.userlater;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.komi.radiogroup.GroupActivity;
import com.komi.radiogroup.R;
import com.komi.radiogroup.Song;
import com.komi.structures.VoiceRecord;

import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MusicPlayerService extends Service implements MediaPlayer.OnCompletionListener,MediaPlayer.OnPreparedListener{
    public static final String PLAYER_BROADCAST = "com.komi.radiogroup.songchanged";

    NotificationManager manager;
    NotificationCompat.Builder builder;
    String channelId = "KM_MUSIC_CHANNEL";
    final int NOTIF_ID = 1;

    private MediaPlayer player = new MediaPlayer();
    ArrayList<VoiceRecord> voiceRecords;
    int currentPlaying = -1;
    RemoteViews remoteViews;

    private BroadcastReceiver broadcastReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        voiceRecords = new ArrayList<>();

        player.setOnCompletionListener(this);
        player.setOnPreparedListener(this);
        player.reset();

        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        String channelName = "Group Channel";
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }
        builder = new NotificationCompat.Builder(this, channelId);
        builder.setNotificationSilent();

        remoteViews = new RemoteViews(getPackageName(), R.layout.music_notif);

        Intent playIntent = new Intent(this, MusicPlayerService.class);


        Intent closeIntent = new Intent(this, MusicPlayerService.class);
        closeIntent.putExtra("command", "close");
        PendingIntent closePendingIntent = PendingIntent.getService(this, 4, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.close_btn, closePendingIntent);

        builder.setCustomContentView(remoteViews);

        Intent intent = new Intent(this, GroupActivity.class);
        intent.putExtra("current_playing", 1);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        builder.setSmallIcon(android.R.drawable.ic_media_play);

        startForeground(NOTIF_ID, builder.build());

        registerReceiver();

    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(PLAYER_BROADCAST);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,filter);
    }


    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        String command = intent.getStringExtra("command");
        int song;
        switch (command){
            case "start_listening":
                if(!player.isPlaying()) {
//                    songs = intent.getParcelableArrayListExtra("list");
//                    try {
//                        player.setDataSource(songs.get(currentPlaying).getUrl());
//                        player.prepareAsync();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                }
                break;
//            case "play_song":
//                songs = intent.getParcelableArrayListExtra("list");
//                song = intent.getIntExtra("song_idx",-1);
//                if(song == -1){
//                    song = 0;
//                }
//                playSong(song);
//                break;
//            case "play":
//                if(songs == null){
//                    songs = intent.getParcelableArrayListExtra("songs");
//                }
//                song = intent.getIntExtra("song_idx",-1);
//                if(song == -1) {
//                    if (!player.isPlaying()) {
//                        player.start();
//                    }
//                }else if (!player.isPlaying() && song == currentPlaying){
//                    player.start();
//                    notify("resume");
//                } else{
//                    playSong(song);
//                }
//                break;
//            case "next":
//                if(player.isPlaying())
//                    player.stop();
//                playSong(currentPlaying+1);
//                break;
//            case "prev":
//                if(player.isPlaying())
//                    player.stop();
//                playSong(currentPlaying-1);
//                break;
            case "close":
                notify("stop");
                stopSelf();
                break;
//            case "pause":
//                if(player.isPlaying()) {
//                    player.pause();
//                    notify("pause");
//                }
//                break;
//            case "update_list":
//                songs = intent.getParcelableArrayListExtra("list");
//                currentPlaying = intent.getIntExtra("playing",currentPlaying);
//                break;
            case "app_created":
                notify("start");
                break;
        }

        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(player!=null) {
            if (player.isPlaying())
                player.stop();
            player.release();
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    private void playSong(int songIdx){
//        currentPlaying = songIdx;
//
//        if (currentPlaying == songs.size()) {
//            currentPlaying = 0;
//        }
//        else if (currentPlaying < 0) {
//            currentPlaying = songs.size() - 1;
//        }
//
//        player.reset();
//        try {
//            Song song = songs.get(currentPlaying);
//            updateNotifView(song);
//            player.setDataSource(song.getUrl());
//            player.prepareAsync();
//            notify("start");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private void updateNotifView(Song song) {
        remoteViews.setTextViewText(R.id.notif_song_name, song.getName());
        if (song.getImage() == null  || song.getImage().isEmpty()) {
            remoteViews.setViewPadding(R.id.notif_song_image,15,15,15,15);
            manager.notify(NOTIF_ID, builder.build());
        }else{
            remoteViews.setImageViewResource(R.id.notif_song_image,android.R.color.transparent);
            manager.notify(NOTIF_ID, builder.build());
            Glide.with(this).asBitmap().load(song.getImage()).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    remoteViews.setViewPadding(R.id.notif_song_image,0,0,0,0);
                    remoteViews.setImageViewBitmap(R.id.notif_song_image, resource);
                    manager.notify(NOTIF_ID, builder.build());
                }
            });
        }

    }

    private void notify(String command) {
        Intent intent = new Intent(PLAYER_BROADCAST);
        intent.putExtra("command",command);
        intent.putExtra("song_idx",currentPlaying);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        playSong(currentPlaying+1);
        //stopSelf();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        player.start();

//        Notification.Builder builder = new Notification.Builder(this);
//        builder.setSmallIcon(android.R.drawable.ic_media_play).setContentTitle("Play Music").setContentText("Playing legendary Bob, enjoy");
//        Intent intent = new Intent(this,MainActivity.class);
//        intent.putExtra("playing",true);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
//        builder.setContentIntent(pendingIntent);
//        startForeground(1,builder.build());
    }
}
