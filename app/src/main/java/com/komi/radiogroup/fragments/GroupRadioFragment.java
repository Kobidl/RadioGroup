package com.komi.radiogroup.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.komi.radiogroup.R;
import com.komi.radiogroup.audio_recorder.AudioListener;
import com.komi.radiogroup.audio_recorder.AudioRecordButton;
import com.komi.radiogroup.audio_recorder.RecordingItem;
import com.komi.radiogroup.firebase.FirebaseDatabaseHelper;
import com.komi.radiogroup.firebase.FirebaseMessagingHelper;
import com.komi.radiogroup.services.ChannelPlayerService;
import com.komi.structures.Group;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.komi.radiogroup.services.ChannelPlayerService.PLAYER_BROADCAST;


public class GroupRadioFragment extends Fragment {


    private static final long TIME_CHECK_ACTIVE = 1000 * 5;
    private static final int WRITE_PERMISSION_REQUEST = 1;
    private boolean listening = false;

    private AudioRecordButton mAudioRecordButton;

    private Button startStopListening;
    View rootView;

    private String userId;
    private BroadcastReceiver broadcastReceiver;
    private ImageView statusImage;
    protected AnimationDrawable statusAnimation;
    private TextView startBtnHelperTV;
    private TextView recordHelperTV;
    private TextView activeUsersTV;
    Group group;
    private int activeUsers = 0;

    private Handler radioHandler = new Handler();
    private Timer timer = new Timer();
    
    public GroupRadioFragment(Group group, boolean listening) {
        this.group = group;
        this.listening = listening;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        group = (Group) this.getArguments().getParcelable("group");
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_group_radio, container, false);

        requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO, READ_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQUEST);

        mAudioRecordButton = (AudioRecordButton) rootView.findViewById(R.id.group_audio_record_button);

        startBtnHelperTV = rootView.findViewById(R.id.join_group_btn_helper);
        recordHelperTV  = rootView.findViewById(R.id.record_btn_helper);
        activeUsersTV = rootView.findViewById(R.id.active_users_tv);
        setActiveUsers(activeUsers);

        statusImage = rootView.findViewById(R.id.radio_status_img);

        userId = Settings.Secure.getString(getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        mAudioRecordButton.setOnAudioListener(new AudioListener() {
            @Override
            public void onStop(RecordingItem recordingItem) {
                sendMessage(recordingItem);
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(Exception e) {
                Log.d("MainActivity", "Error: " + e.getMessage());
            }
        });

        startStopListening = rootView.findViewById(R.id.btn_start_listening);
        startStopListening.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listening = !listening;
                if(listening) {
                    playMusic();
                    getActiveUsers();
                }
                else {
                    stopMusic();
                    getActiveUsers();
                }
            }
        });



        if(listening){//Init the page if listening
            mAudioRecordButton.setVisibility(View.VISIBLE);
            startStopListening.setText(R.string.stop_listening);
            Objects.requireNonNull(getActivity()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            startStopListening.setBackgroundColor(rootView.getContext().getColor(R.color.red));
            startBtnHelperTV.setText(R.string.click_here_leave_channel);
            recordHelperTV.setVisibility(View.VISIBLE);
            statusImage.setImageResource(R.drawable.online_animation);
            statusAnimation = (AnimationDrawable) statusImage.getDrawable();
            statusAnimation.start();
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) statusImage.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        }

        registerReceiver();

        startCheckingActiveUsers();

        return rootView;
    }

    private void startCheckingActiveUsers(){
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                FirebaseDatabaseHelper.getInstance().getActiveUsers(group.getGroupID(), new FirebaseDatabaseHelper.OnUsersListeningDataChangedCallback() {
                    @Override
                    public void OnDataReceived(final int number) {
                        radioHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                getActiveUsers();
                            }
                        });
                    }
                });
            }
        }, 0,TIME_CHECK_ACTIVE);
    }

    private void getActiveUsers() {
        FirebaseDatabaseHelper.getInstance().getActiveUsers(group.getGroupID(), new FirebaseDatabaseHelper.OnUsersListeningDataChangedCallback() {
            @Override
            public void OnDataReceived(final int number) {
                radioHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setActiveUsers(number);
                    }
                });
            }
        });
    }

    private void setActiveUsers(int num) {
        activeUsers = num;
        try {
            if (getActivity() != null) {
                activeUsersTV.setText(getResources().getString(R.string.current_active_users) + "  " + num);
            }
        }catch (Exception e){}
    }

    private void sendMessage(RecordingItem recordingItem) {
        FirebaseMessagingHelper.getInstance(rootView.getContext()).sendMessageToTopic(recordingItem.getFileUrl(),userId,group.getGroupID());
    }

    private void playMusic(){
        //tops old ones
        stopMusic();

        //Starting new channel service
        Intent intent = new Intent(rootView.getContext(), ChannelPlayerService.class);
        intent.putExtra("action","start_listening");
        intent.putExtra("user_id",userId);
        intent.putExtra("group",group);
        rootView.getContext().startService(intent);

        //Change layout view
        statusImage.setImageResource(R.drawable.online_animation);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) statusImage.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        statusAnimation = (AnimationDrawable) statusImage.getDrawable();
        statusAnimation.start();
        mAudioRecordButton.setVisibility(View.VISIBLE);

        startStopListening.setText(R.string.stop_listening);
        startStopListening.setBackgroundColor(rootView.getContext().getColor(R.color.red));
        startBtnHelperTV.setText(R.string.click_here_leave_channel);
        recordHelperTV.setVisibility(View.VISIBLE);

        //Prevent screen turn off
        Objects.requireNonNull(getActivity()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void stopMusic(){
        try {
            statusImage.setImageResource(R.drawable.offline);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) statusImage.getLayoutParams();
            params.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
            mAudioRecordButton.setVisibility(View.GONE);
            startStopListening.setText(R.string.start_listening);
            Intent intent = new Intent(rootView.getContext(), ChannelPlayerService.class);
            rootView.getContext().stopService(intent);
            startStopListening.setBackgroundColor(rootView.getContext().getColor(R.color.colorPrimary));
            recordHelperTV.setVisibility(View.GONE);
            startBtnHelperTV.setText(R.string.click_here_go_live);
            startCheckingActiveUsers();

            //Remove preventing screen off
            Objects.requireNonNull(getActivity()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }catch (Exception e){

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == WRITE_PERMISSION_REQUEST){
            for(int result : grantResults){
                if(result != PackageManager.PERMISSION_GRANTED ){
                    mAudioRecordButton.setEnabled(false);
                    if(recordHelperTV!=null)
                        recordHelperTV.setText(R.string.no_permissions);
                }
            }
        }
    }

    private void registerReceiver() {//Getting commands from notif
        IntentFilter filter = new IntentFilter(PLAYER_BROADCAST);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String command = intent.getStringExtra("action");
                switch (command){
                    case "closed":
                        stopMusic();
                        getActiveUsers();
                        break;
                    case "start_playing":
                        mAudioRecordButton.setEnabled(false);
                        break;
                    case "stop_playing":
                        mAudioRecordButton.setEnabled(true);
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(rootView.getContext()).registerReceiver(broadcastReceiver,filter);
    }

    @Override
    public void onDestroyView() {
        LocalBroadcastManager.getInstance(rootView.getContext()).unregisterReceiver(broadcastReceiver);
        Objects.requireNonNull(getActivity()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        timer.cancel();
        super.onDestroyView();
    }

}