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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.messaging.FirebaseMessaging;
import com.komi.radiogroup.GroupActivity;
import com.komi.radiogroup.MainContainer;
import com.komi.radiogroup.R;
import com.komi.radiogroup.firebase.MyFirebaseMessagingService;
import com.komi.structures.Group;
import com.komi.structures.VoiceRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static com.komi.radiogroup.pages.Profile.SHARED_PREFS;

public class MusicPlayerService extends Service implements MediaPlayer.OnCompletionListener,MediaPlayer.OnPreparedListener{
    public static final String PLAYER_BROADCAST = "com.komi.radiogroup.songchanged";
    public static final String GROUP_LISTENING = "group_listening_id";

    NotificationManager manager;
    NotificationCompat.Builder builder;
    String channelId = "KM_MUSIC_CHANNEL";
    final int NOTIF_ID = 1;

    private MediaPlayer player = new MediaPlayer();
    private static Group group;
    ArrayList<VoiceRecord> voiceRecords;
    boolean playing = false;
    RemoteViews remoteViews;
    FirebaseMessaging messaging = FirebaseMessaging.getInstance();

    List<String> messages;

    private BroadcastReceiver broadcastReceiver;
    private String userId;

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

        //Intent playIntent = new Intent(this, MusicPlayerService.class);



        Intent closeIntent = new Intent(this, MusicPlayerService.class);
        closeIntent.putExtra("action", "close");
        PendingIntent closePendingIntent = PendingIntent.getService(this, 4, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.close_btn, closePendingIntent);

        builder.setCustomContentView(remoteViews);

        builder.setSmallIcon(android.R.drawable.ic_media_play);

        startForeground(NOTIF_ID, builder.build());

        messages = new ArrayList<>();

        registerReceiver();


    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(MyFirebaseMessagingService.FCM_MESSAGE_RECEIVER);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra("message");
                String senderId = intent.getStringExtra("sender_id");
                if(!senderId.equals(userId)) {
                    messages.add(message);
                    if (!playing)
                        playSong();
                }

            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,filter);
    }


    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        String command = intent.getStringExtra("action");

        switch (command){
            case "start_listening":
                if(!player.isPlaying()) {
                    group = (Group) intent.getParcelableExtra("group");
                    userId = intent.getStringExtra("user_id");
                    updateNotifView(group);
                    Intent openIntent = new Intent(this, GroupActivity.class);
                    openIntent.putExtra("group",group);
                    openIntent.putExtra("playing",true);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, openIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.setContentIntent(pendingIntent);
                    messaging.subscribeToTopic(group.getGroupID());
                    this.getSharedPreferences(SHARED_PREFS,MODE_PRIVATE).edit().putString(GROUP_LISTENING,group.getGroupID()).apply();
                }
                break;
            case "close":
                notify("closed");
                messaging.unsubscribeFromTopic(group.getGroupID());
                stopSelf();
                break;
            case "app_created":
                notify("created");
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
        messaging.unsubscribeFromTopic(group.getGroupID());
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        this.getSharedPreferences(SHARED_PREFS,MODE_PRIVATE).edit().remove(GROUP_LISTENING).apply();
    }

    private void playSong(){
        player.reset();
        try {
            //updateNotifView(song);
            if(messages.size() > 0) {
                playing = true;
                player.setDataSource(messages.get(0));
                player.prepareAsync();
                messages.remove(0);
                notify("start_playing");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateNotifView(Group group) {
        remoteViews.setTextViewText(R.id.notif_group_name, group.getGroupName());
        if (group.getProfilePicturePath() == null  || group.getProfilePicturePath().isEmpty()) {
            remoteViews.setViewPadding(R.id.notif_group_image,15,15,15,15);
            manager.notify(NOTIF_ID, builder.build());
        }else{
            remoteViews.setImageViewResource(R.id.notif_group_image,android.R.color.transparent);
            manager.notify(NOTIF_ID, builder.build());
            Glide.with(this).asBitmap().load(group.getProfilePicturePath()).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    remoteViews.setViewPadding(R.id.notif_group_image,0,0,0,0);
                    remoteViews.setImageViewBitmap(R.id.notif_group_image, resource);
                    manager.notify(NOTIF_ID, builder.build());
                }
            });
        }

    }

    private void notify(String command) {
        Intent intent = new Intent(PLAYER_BROADCAST);
        intent.putExtra("action",command);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if(messages.size()>0){
            playSong();
        }else{
            stopPlaying();
        }
        //stopSelf();
    }

    private void stopPlaying() {
        player.reset();
        playing = false;
        notify("stop_playing");
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
