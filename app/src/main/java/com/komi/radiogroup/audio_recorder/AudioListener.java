package com.komi.radiogroup.audio_recorder;


public interface AudioListener {

    void onStop(RecordingItem recordingItem);

    void onCancel();

    void onError(Exception e);
}
