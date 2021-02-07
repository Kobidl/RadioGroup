package com.komi.radiogroup.userlater;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;

public class SettingsContentObserver extends ContentObserver {
    int previousVolume;
    Context context;
    private VolumeChanged listener;

    interface VolumeChanged {
        void onChanged(int volume);
    }

    public void setListener(VolumeChanged listener){
        this.listener = listener;
    }

    public SettingsContentObserver(Context c, Handler handler) {
        super(handler);
        context=c;

        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        previousVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        if(listener != null){
            listener.onChanged(currentVolume);
        }
    }
}